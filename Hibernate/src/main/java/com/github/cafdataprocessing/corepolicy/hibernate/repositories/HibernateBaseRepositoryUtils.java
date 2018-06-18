/*
 * Copyright 2015-2018 Micro Focus or one of its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.cafdataprocessing.corepolicy.hibernate.repositories;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.github.cafdataprocessing.corepolicy.common.dto.Filter;
import com.github.cafdataprocessing.corepolicy.common.dto.PageRequest;
import com.github.cafdataprocessing.corepolicy.common.dto.Sort;
import com.github.cafdataprocessing.corepolicy.common.dto.conditions.Condition;
import com.github.cafdataprocessing.corepolicy.common.dto.conditions.ConditionType;
import com.github.cafdataprocessing.corepolicy.common.dto.conditions.FragmentCondition;
import com.google.common.base.Strings;
import com.github.cafdataprocessing.corepolicy.common.AnnotationHelper;
import com.github.cafdataprocessing.corepolicy.common.ApiStrings;
import com.github.cafdataprocessing.corepolicy.common.UserContext;
import com.github.cafdataprocessing.corepolicy.common.exceptions.ConditionEngineException;
import com.github.cafdataprocessing.corepolicy.common.shared.ErrorCodes;
import com.github.cafdataprocessing.corepolicy.common.shared.FilterHelper;
import com.github.cafdataprocessing.corepolicy.repositories.v2.CollectionRepository;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.*;

import org.hibernate.internal.CriteriaImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for Hibernate Repository implementations.
 */
public abstract class HibernateBaseRepositoryUtils {

    protected UserContext userContext;
    protected ApplicationContext applicationContext;
    protected Logger logger = LoggerFactory.getLogger(HibernateBaseRepository.class);

    public HibernateBaseRepositoryUtils(UserContext userContext,
                                        ApplicationContext applicationContext) {
        this.userContext = userContext;
        this.applicationContext = applicationContext;
    }

    protected void validatePageRequest(PageRequest pageRequest) {
        if (pageRequest.max_page_results < 1) {
            throw new IllegalArgumentException(
                    "The field max_page_results should be >0.");
        }

        if (pageRequest.start < 1) {
            throw new IllegalArgumentException("The field start should be >0.");
        }
    }

    protected void addCriterionForPagedResults(PageRequest pageRequest,
                                               Criteria criteria) {
        criteria.setMaxResults(pageRequest.max_page_results.intValue());
        criteria.setFirstResult(pageRequest.start.intValue() - 1);
    }

    protected void addCriterionBasedOnFilter(Filter filter, Session session,
                                             Criteria criteria,
                                             Class<?> requestClassType) {

        // add on some additional filter clauses if present.
        if (filter == null) {
            return;
        }

        // Certain types of objects, have more specific polymorphic types.
        // Check early on in this loop, if we happen to be in one of these requestedClass types,
        // and if so, see if we can get a more concrete type.
        Class<?> realRequestedObjectType = AnnotationHelper.getRealRequestClassType(
                requestClassType, filter);

        // find out what we filter on, this will be either the json property name from the object requested
        // e.g. retrieveCondition( Filter { "type"="lexicon" and "value"=5 } )
        // or it can be a relational filter subObjectType.propertyX e.g. retrieveCollectionSequence ( Filter : { "collection_sequence_entries.collection_ids" : 5 } )
        Iterator<String> fieldNames = filter.fieldNames();

        while (fieldNames.hasNext()) {
            String fieldName = fieldNames.next();

            // In some cases, the fieldName used by the filter isn't exactly the same as that used
            // internally - as some classes are wrapped at the hibernate layer.
            String lookupByFieldName = checkAlterFilterName(requestClassType,
                    fieldName);

            // Every Filter field must have a @FilterName annotation, if not we dont deem it to be
            // a valid filter - so it throws.
            List<Field> fields = AnnotationHelper.getPropertyForFilterName(
                    lookupByFieldName, realRequestedObjectType);

            // Now we know its a valid filter - get the value for the criterion.
            JsonNode node = filter.get(fieldName);

            // check if its null
            if (node.getNodeType() == JsonNodeType.NULL) {
                // we dont have any valid value here - throw as this is invalid.
                throw new IllegalArgumentException(
                        "retrievePage on Field: " + fieldName);
            }

            // the last property is the one we build the criterion on, so get it now.
            Field actualPropertyForRestriction = (Field) fields.toArray()[fields.size() - 1];

            String propertyName = actualPropertyForRestriction.getName();

            // we have to perform a join to bring in the sub objects.
            String fullPropertyName = null;

            for (Field field : fields) {
                if (Strings.isNullOrEmpty(fullPropertyName)) {
                    fullPropertyName = field.getName();
                    continue;
                }

                // either append onto existing with a .
                fullPropertyName += "." + field.getName();
            }

            // for now certain types aren't mapped / related.  As such we can't perform joins using
            // the criteria api or HQL.  Fallback to using SQL directly to overcome this in meantime.
            if (addSpecialCaseSqlQuery(session, criteria,
                    realRequestedObjectType, node,
                    actualPropertyForRestriction,
                    propertyName, fullPropertyName)) {

                continue;
            }

            if (fields.size() == 1) {
                // If we have a single object, just go ahead and add its criterion, we dont need an alias for simple case,
                // and we know we can exit completely.
                Criterion criterion = getCriterionForProperty(criteria,
                        realRequestedObjectType,
                        node,
                        actualPropertyForRestriction,
                        propertyName);
                criteria.add(criterion);
                continue;
            }
            // add on a join between this object and others if required, and return the alias name for qualifying future props.
            String alias = addJoinIfRequired(criteria, fields, requestClassType, propertyName, fullPropertyName);

            String propertyRef = aliasProperty(alias, propertyName);

            // If we have added an alias then we need to use this to reference to any properties which are only available via the alias...
            Criterion criterion = getCriterionForProperty(criteria,
                    realRequestedObjectType,
                    node,
                    actualPropertyForRestriction,
                    propertyRef);

            criteria.add(criterion);
        }
    }

    private String aliasProperty(String alias, String propertyName) {
        String propertyRef = Strings.isNullOrEmpty(alias) ? propertyName : alias + "." + propertyName;
        return propertyRef;
    }

    protected Collection<Order> addCriteriaBasedOnSort(Criteria criteria,
                                                       Sort sort,
                                                       Class<?> requestClassType) {
        if (sort == null) {
            return null;
        }

        Collection<Order> orders = new ArrayList<>();
        // Certain types of objects, have more specific polymorphic types.
        // Check early on in this loop, if we happen to be in one of these requestedClass types,
        // and if so, see if we can get a more concrete type.
        Class<?> realRequestedObjectType = AnnotationHelper.getRealRequestClassType(
                requestClassType, sort);
        Iterator<String> fieldNames = sort.fieldNames();
        while (fieldNames.hasNext()) {
            String fieldName = fieldNames.next();

            // In some cases, the fieldName used by the sort isn't exactly the same as that used
            // internally - as some classes are wrapped at the hibernate layer.
            String lookupByFieldName = checkAlterFilterName(requestClassType,
                    fieldName);

            // Every sort field must have a @SortName annotation, if not we don't deem it to be
            // a valid filter - so it throws.
            List<Field> fields = AnnotationHelper.getPropertyForSortName(
                    lookupByFieldName, realRequestedObjectType);

            // Now we know its a valid filter - get the value for the criterion.
            JsonNode node = sort.get(fieldName);

            // check if its null
            if (node.getNodeType() == JsonNodeType.NULL) {
                // we dont have any valid value here - throw as this is invalid.
                throw new IllegalArgumentException(
                        "retrievePage on Field: " + fieldName);
            }

            // the last property is the one we build the criterion on, so get it now.
            Field actualPropertyForRestriction = (Field) fields.toArray()[fields.size() - 1];

            String propertyName = actualPropertyForRestriction.getName();

            // we have to perform a join to bring in the sub objects.
            String fullPropertyName = null;

            for (Field field : fields) {
                if (Strings.isNullOrEmpty(fullPropertyName)) {
                    fullPropertyName = field.getName();
                    continue;
                }

                // either append onto existing with a .
                fullPropertyName += "." + field.getName();
            }

            String alias = addJoinIfRequired(criteria, fields, requestClassType, propertyName, fullPropertyName);
            resolveOrders(orders, fields, propertyName, fullPropertyName, node,
                    alias);

        }
        return orders;
    }

    public Boolean checkForAlias(Criteria criteria, String alias) {
        Field f = null;
        try {
            f = criteria.getClass().getDeclaredField("subcriteriaList");
            f.setAccessible(true);
            List<CriteriaImpl.Subcriteria> subCriteria = (List<CriteriaImpl.Subcriteria>) f.get(
                    criteria);
            if (subCriteria.size() == 0) {
                return false;
            }

            for (CriteriaImpl.Subcriteria subCrit : subCriteria) {
                if (subCrit.getAlias().contains(alias)) {
                    return true;
                }
            }

        } catch (NoSuchFieldException e) {
            throw new RuntimeException("Failed to create alias", e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to create alias", e);
        }
        return false;
    }

    /**
     * *
     * Obtain / change the property filter name, to cater for internal wrapper
     * classes.
     *
     * @param requestClassType The type of the request class.
     * @param fieldName The provided field name for filter.
     * @return The filter name that should be used.
     */
    private String checkAlterFilterName(Class<?> requestClassType,
                                        String fieldName) {
        // some of types, we have wrapped with internal hibernate classes, if this is the case we are in a pickle.
        // 1) The class is different completed e.g.
        // documentcollection->condition->id maps to hibernate collectionrepository$item->conditionId.
        // 2) Property X on Class Y X->Y.  Is actually wrapped as Wrapper Z->Y->X so we need to add the abstraction
        // property name to the filter.
        // N.B. Eventually we could remove this if the wrapper classes / schema is resolved to match! PD-628
        if (requestClassType == CollectionRepository.Item.class && fieldName.equalsIgnoreCase(
                ApiStrings.DocumentCollections.Arguments.POLICY_IDS)) {
            // we need to add on the wrapper property name which abstracts the DocumentCollection class.
            return CollectionRepository.Item.CollectionWrapperFilterName + "." + fieldName;
        }

        if (requestClassType == SequenceWorkflowEntryRepositoryImpl.Item.class) {
            // if requesting sequence workflow id, just return normal field name as is, anything else references via
            // our internal representation of the $Item object sequenceWorkflowEntry
            if (fieldName.equalsIgnoreCase(
                    ApiStrings.SequenceWorkflowEntry.Arguments.SEQUENCE_WORKFLOW_ID)) {
                return fieldName;
            }
            // we need to add on the wrapper property name which abstracts the DocumentCollection class.
            return SequenceWorkflowEntryRepositoryImpl.Item.WrapperFilterName + "." + fieldName;
        }

        return fieldName;
    }

    /**
     * Internal method, which determines by the type of the JavaObject what type
     * of field restriction is to be used as the criterion. N.B. We cannot
     * reliably use the Json node type as it changes depending on the value
     * begin represented. (i.e. 1 created using long constructor will still
     * transmit as an Int depending on min / max value. )
     */
    private Criterion getCriterionForProperty(Criteria criteria,
                                              Class<?> realRequestedObjectType,
                                              JsonNode node,
                                              Field actualProperty,
                                              String propertyName) {
        Object realValue = FilterHelper.getValueFromNode(node, actualProperty);

        // We have a special case, in hibernate (Discrimators) which doesn't perform criterion mapping.
        // As such we can restrict by using the "class" keyword in the where clause.
        if (isFieldRepresentedByDiscriminator(actualProperty,
                realRequestedObjectType)) {
            return Restrictions.eq("class", realValue);
        }

        // if the real value is an array and need to perform the .in restriction instead.
        if (FilterHelper.isFieldRepresentedByArrayType(actualProperty)) {

            // some of the lists / sets I have found that depending on wether they are subobjects or not have
            // to be referred to be the "propid.elements", "alias.elements", or just "elements" keyword.
            String arrayPropName = propertyName + "." + "elements";

            // Add the object as an array ( restriction.in )
            return Restrictions.in(arrayPropName, (Object[]) realValue);
        }

        // single entity so ( restriction.eq )
        return Restrictions.eq(propertyName, realValue);
    }

    private Order getOrderForSort(JsonNode node, String propertyName) {
        Boolean ascendingOrder = node.asBoolean();
        if (ascendingOrder) {
            return Order.asc(propertyName);
        } else {
            return Order.desc(propertyName);
        }

    }

    private String addJoinIfRequired(Criteria criteria, List<Field> fields,
                                     Class<?> requestClassType,
                                     String propertyName,
                                     String fullPropertyName) {

        // some of our items, require us to specify a join, so we can access subobject fields for ordering / filtering.
        // check fields.count > 1 so has to be of type collectionSequence.name 
        if (fields.size() <= 1) {
            return null;
        }

        String alias = null;
        String joinOnObject = null;

        // check real class type and describe the join.

        // go through the fields, and check if any of them require a join and if so add it now.
        long index = 0;
        for (Field field : fields) {

            index++;
            joinOnObject = buildPropertyName(joinOnObject, field.getName());

            // this is really just the .Item property dereference, so we dont need to join
            // just add its name to the joinOnObject.  Only check on the first pass through on a given
            // object type!  If its sorting for a simple property like projectId or id then it wont need to be
            // dereferenced via its Item prop.
            if ((index == 1) && AnnotationHelper.isWrappedItemField(requestClassType, field)) {
                continue;
            }

            // if its joingable, ie.. sequenceWorkflowEntry.collectionSequence.name, then join for it and any subobjects now.
            if (isJoinableField(field)) {
                alias = createAliasFromPropertyName( field.getName() );
                checkAndAddAliasCriteria(criteria, alias, joinOnObject);
                continue;
            }
        }


        // special case policyIds for now, it should really be aliased I think, but as collection is already in use it causing issues.
        if (requestClassType == CollectionRepository.Item.class
                && fields.stream().filter(u -> u.getName().equalsIgnoreCase(
                "policyIds")).findAny().isPresent()) {
            // Watch this case, we DONT WANT TO JOIN to alias outside scope of the restriction otherwise its uses it
            // when collection.projectId is specified on the item. As such collection.policyIds is alias'd to policyIds
            // so that a simple policyIds.elements works as expected.
            checkAndAddAliasCriteria(criteria, propertyName, fullPropertyName);
            return alias;
        }

        if (!Strings.isNullOrEmpty(alias)) {
            return alias;
        }


        // I think that any fields, that have more than 1 item may be a previous candidate for a join.

        // using previous behaviour, exception is that the restriction is not a subcriteria, its now a full criterion
        // which increases its scope, IF ANY PROBLEMS ARRISE CONSIDER using
        // createAlias(fullPropertyName, propertyName, INNER_JOIN, Restriction.eq("propName", 1 ) subrestrictions.
        //
        // checkAndAddAliasCriteria(criteria, propertyName, fullPropertyName);
        // criteria.createAlias( fullPropertyName, propertyName );
        return null;
    }

    public static String createAliasFromPropertyName(String propertyName) {
        // take the alias as first letter, and each captial letter after that.
        String aliasInUse = null;

        // adding _alias to the property name, to ensure it different from normal properties in use!
        return propertyName + "_alias";
    }

    private boolean isJoinableField(Field field) {
        // we can either look at the class type, and decide its not a simple type like bool / long /string and decide
        // it needs to be joined on, or add an annoation we can lookup.. for now we have went for the simple option as I dont
        // want to have a @Joinable annoation on our public API which is only hibernate specific. if it comes to this, add a switch
        // statement or something internally to avoid it being in our public api!
        if ((field.getType() == String.class) ||
                (field.getType() == Long.class) ||
                (field.getType() == boolean.class) ||
                (field.getType() == Integer.class) ||
                (field.getType() == Short.class)) {
            return false;
        }

        // if its an array / list type, make our mind up by whats the actual member in the list.
        if (( field.getType() == List.class ) ||
                ( field.getType() == Set.class ) ||
                ( field.getType() == Array.class ) ||
                ( field.getType() == Collection.class )) {

            Class<?> realClass = AnnotationHelper.getRealClassType(field);

            if ((realClass == String.class) ||
                    (realClass == Long.class) ||
                    (realClass == boolean.class) ||
                    (realClass == Integer.class) ||
                    (realClass == Short.class)) {
                return false;
            }
        }

        //logger.debug("Adding type as joinable..." + field.getType());
        return true;
    }


    private String buildPropertyName(String currentAlias, String newAlias) {
        if (Strings.isNullOrEmpty(currentAlias)) {
            return newAlias;
        }

        return currentAlias + "." + newAlias;
    }

    private boolean checkAndAddAliasCriteria(Criteria criteria, String alias, String joinOnObject) {
        if (!checkForAlias(criteria, alias)) {
            logger.debug("creating alias on: " + joinOnObject + " called: " + alias);
            criteria.createAlias(joinOnObject, alias);
            return true;
        }

        logger.debug("Simply referring to existing alias called: " + alias);
        return false;
    }

    private boolean addSpecialCaseSqlQuery(Session session, Criteria criteria,
                                           Class<?> realRequestedObjectType,
                                           JsonNode node, Field actualProperty,
                                           String propertyName,
                                           String fullPropertyName) {

        // I hate have to do this, using SQL in hibernate isn't good practice.  but with current schema
        // not relating objects to their children objects via mappings we are stuck to:
        // 1) run an HQL query per object, and then iterate resultant objects, and build
        // your parent objects HQL query.
        // e.g. GetCollectionSequence by id loads parent CollectionSequence then another query for
        // its collectionSequenceentries.
        // if we overcome this, remove this code, and go back to JOINs on sub objects.
        if (fullPropertyName.equalsIgnoreCase(
                "collectionSequenceEntries.collectionIds")) {

            String ids = FilterHelper.getValueFromNodeAsString(node,
                    actualProperty);

            String getSequencesByColl = String.format(
                    CollectionSequenceEntryRepositoryImpl.SQL_GET_COLLECTION_SQUENCE_IDS_BY_COLLECTION_IDS,
                    ids);

            // create an in clause to restrict our collection sequence by results
            // of sql query.
            String sqlQuery = String.format("id in (%s)", getSequencesByColl);

            criteria.add(Restrictions.sqlRestriction(sqlQuery));
            return true;
        }

        if (realRequestedObjectType == CollectionRepository.Item.class
                && fullPropertyName.equalsIgnoreCase("conditionId")) {
            // This case is where we want all the collections which contain a condition id.
            // As a given id may be a fragment, it may be part of multiple collections as such this
            // becomes more difficult.
            // In mysql I iterate in a SP, but haven' the luxury of this here!
            Object conditionId = FilterHelper.getValueFromNode(node,
                    actualProperty);

            // get all collections for this condition.
            Collection collectionIds = new ArrayList<>();

            getConditionCollectionMembership(session, conditionId,
                    collectionIds);

            if (collectionIds.size() == 0) {
                // if we have no collections at the end, just look for id 0, which will return no results!
                collectionIds.add(0L);
            }

            criteria.add(Restrictions.in("conditionId", collectionIds));
            return true;
        }

        // we haven't done any queries here, just let it continue on.
        return false;
    }

    private void getConditionCollectionMembership(Session session,
                                                  Object conditionId,
                                                  Collection topLevelConditionIds) {

        // we need an entirely seperate query first on the conditions table!
        Criteria subConditionCriteria = session.createCriteria(Condition.class);
        subConditionCriteria.add(Restrictions.eq("projectId",
                userContext.getProjectId()));
        subConditionCriteria.add(Restrictions.eq("id", conditionId));

        Condition condition = (Condition) subConditionCriteria.uniqueResult();

        if (condition == null) {
            // we didn't find nay result matching this id, if its the one specified by the user, it should
            // be validated, others we dont have to as we looked them up.
            throw new ConditionEngineException(ErrorCodes.GENERIC_ERROR,
                    "Unable to locate condition specified: " + conditionId);
        }

        // Only when we get to a top level node, should we check if:
        // 1) is_fragment=true, and as such get the parent FRAGMENT conditino which points to this.
        // 2) parentId=null and not fragment, and as such its the top level node - add to collection list.
        if (condition.parentConditionId != null) {
            // we have another condition to look up, let it continue on.
            getConditionCollectionMembership(session,
                    condition.parentConditionId,
                    topLevelConditionIds);
            return;
        }

        // Parent conditionID is null, check if its a fragment before we use it as a collection lookup condition
        if (condition.isFragment) {
            // If the conditionType is Fragment, then it has not collections, it lives on its own at present,
            // i.e. orphaned condition.
            if (condition.conditionType == ConditionType.FRAGMENT) {
                return;
            }

            // We need a seperate query to find all Fragment Condition Types pointing to this.
            Criteria fragmentMatch = session.createCriteria(
                    FragmentCondition.class);
            fragmentMatch.add(Restrictions.eq("projectId",
                    userContext.getProjectId()));
            fragmentMatch.add(Restrictions.eq("value", condition.id));

            List<Condition> list = fragmentMatch.list();

            // for each condition, call with its children.
            for (Condition item : list) {
                getConditionCollectionMembership(session, item.id,
                        topLevelConditionIds);
            }

            return;
        }

        // ok it has no parent, and isn't a fragment, add it to the collection lookup list.
        topLevelConditionIds.add(condition.id);
    }

    private void resolveOrders(Collection<Order> orders, List<Field> fields,
                               String propertyName, String fullPropertyName,
                               JsonNode node, String alias) {
        if (Strings.isNullOrEmpty(alias)) {
            if (fields.size() == 1) {
                orders.add(getOrderForSort(node, propertyName));
            }

            if (fields.size() > 1) {
                orders.add(getOrderForSort(node, fullPropertyName));
            }
        } else {
            orders.add(getOrderForSort(node, alias + "." + propertyName));
        }
    }

    /**
     * Is this field represented by a discriminator in hibernate. Certain types,
     * may only be discriminators on certain objects, if this is the case,
     * restrict this on class type as well.
     *
     * @param field The field to check.
     * @return Whether the field represents a discriminator or not.
     */
    protected static boolean isFieldRepresentedByDiscriminator(Field field,
                                                               Class<?> requestedObjectType) {

        // list all types of discriminators, if they are only that on certain classes, restrict
        // this even further if required.
        if (field.getType() == ConditionType.class) {
            return true;
        }

        return false;
    }

    /**
     * *
     * Must be implemented as this is used to create the basic criteria used to
     * select the given item type. You can then add count / sub order / paging /
     * restrictions on specific ids etc on top of this if required.
     *
     * @param session Session details.
     * @return Created basic criteria for object type.
     */
    protected abstract Criteria createThisObjectsBaseCriteria(Session session);

    /**
     * *
     * Must be implemented if you support paging.
     *
     * @param pageRequest The page request containing paging details.
     * @param session Session details.
     * @return Created criteria for item.
     */
    protected abstract Criteria createItemBaseCriteria(PageRequest pageRequest,
                                                       Session session);

    /**
     * *
     * Base implementation for non polymorphic classes is to just use the same
     * method as that without the filter This is because it enough to use the
     * base typeParameterClass which the repository is created from. You only
     * need to override if the Db type uses discriminators at the hibernate
     * backend to specialize the type to db columns.
     *
     * @param pageRequest The page request containing paging details.
     * @param session Session details.
     * @param filter Filter to apply when building criteria.
     * @return Created criteria for item.
     */
    protected abstract Criteria createItemBaseCriteria(PageRequest pageRequest,
                                                       Session session,
                                                       Filter filter);

    /**
     * *
     * Base implementation for non polymorphic classes is to just use the same
     * method as that without the filter This is because it enough to use the
     * base typeParameterClass which the repository is created from. You only
     * need to override your type uses discriminators at the hibernate backend
     * to specialize the type to db columns.
     *
     * @param session Session details.
     * @param filter Filter to use on criteria.
     * @return Created criteria.
     */
    protected abstract Criteria createThisObjectsBaseCriteria(Session session,
                                                              Filter filter);

    /**
     * *
     * Base implementation for non polymorphic classes is to just use the same
     * method as that without the filter This is because it enough to use the
     * base typeParameterClass which the repository is created from. You only
     * need to override if the Db type uses discriminators at the hibernate
     * backend to specialize the type to db columns.
     *
     * @param pageRequest The page request containing paging details.
     * @param session Session details.
     * @param filter Filter to use on criteria.
     * @param sort Details of how to sort the items.
     * @return Created criteria.
     */
    protected abstract Criteria createItemBaseCriteria(PageRequest pageRequest,
                                                       Session session,
                                                       Filter filter, Sort sort);

}
