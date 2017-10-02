/*
 * Copyright 2015-2017 EntIT Software LLC, a Micro Focus company.
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

import com.github.cafdataprocessing.corepolicy.common.dto.Filter;
import com.github.cafdataprocessing.corepolicy.common.dto.PageOfResults;
import com.github.cafdataprocessing.corepolicy.common.dto.PageRequest;
import com.github.cafdataprocessing.corepolicy.common.dto.Sort;
import com.github.cafdataprocessing.corepolicy.common.dto.conditions.BooleanCondition;
import com.github.cafdataprocessing.corepolicy.common.dto.conditions.Condition;
import com.github.cafdataprocessing.corepolicy.common.dto.conditions.NotCondition;
import com.github.cafdataprocessing.corepolicy.hibernate.HibernateExecutionContextImpl;
import com.github.cafdataprocessing.corepolicy.repositories.v2.ConditionRepository;
import com.github.cafdataprocessing.corepolicy.repositories.v2.ExecutionContext;
import com.google.common.base.Strings;
import com.github.cafdataprocessing.corepolicy.common.AnnotationHelper;
import com.github.cafdataprocessing.corepolicy.common.ApiStrings;
import com.github.cafdataprocessing.corepolicy.common.UserContext;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 *
 */
@Component
public class ConditionRepositoryImpl extends HibernateBaseRepositoryUtils implements ConditionRepository {

    Class<Condition> typeParameterClass = Condition.class;

    @Autowired
    public ConditionRepositoryImpl(UserContext userContext,  ApplicationContext context ){
        super(userContext, context);
    }

    @Override
    public Collection<Item> retrieve(ExecutionContext executionContext, Collection<Long> ids, Boolean includeChildren) {
        Session session = getSession(executionContext);

        Criteria criteria = session.createCriteria(typeParameterClass)
                .add(Restrictions.in("id", ids))
                .add(Restrictions.eq("projectId", userContext.getProjectId()))
                .setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);

        List<Condition> conditions = criteria.list();
        //check all items are returned
        if(!ids.stream().distinct().allMatch(u->conditions.stream().anyMatch(i-> Objects.equals(i.id, u)))){
            throw new RuntimeException("Could not return conditions for all ids");
        }
        loadChildren(session, conditions, includeChildren);
        return toItem(conditions);
    }

    @Override
    public void deleteChildren(ExecutionContext executionContext, Long id) {
        Session session = getSession(executionContext);

        String hql = "delete from Condition where parentConditionId= :parentConditionId and projectId= :projectId";
        session.createQuery(hql).setLong("parentConditionId", id).setString("projectId", userContext.getProjectId()).executeUpdate();
    }

    @Override
    public Item create(ExecutionContext executionContext, Item t) {
        Session session = getSession(executionContext);

        t.condition.id = null;
        if(t.condition.id != null) {
            throw new RuntimeException("Id should be null");
        }

        preSave(t, session);
        session.save(t.condition);

        session.flush();
        session.evict(t.condition);

        //Add update collection
        if(t.attachToCollectionId!=null){
            {
                //Select item
                String hql = "select conditionId from CollectionRepository$Item where id= :id and collection.projectId= :projectId";
                Long attachedConditionId = (Long)session.createQuery(hql)
                        .setLong("id", t.attachToCollectionId)
                        .setString("projectId", userContext.getProjectId())
                        .uniqueResult();

                //delete attached condition
                if (attachedConditionId != null) {
                    delete(executionContext, attachedConditionId);
                }
            }

            {
                String updateHql = "update CollectionRepository$Item set conditionId=:conditionId where id= :id and collection.projectId= :projectId";
                int i = session.createQuery(updateHql)
                        .setLong("id", t.attachToCollectionId)
                        .setLong("conditionId", t.condition.id)
                        .setString("projectId", userContext.getProjectId())
                        .executeUpdate();
            }
        }

        return retrieve(executionContext, Arrays.asList(t.condition.id)).stream().findFirst().get();
    }

    // @Override
    protected <R extends Item> R preSave(Item t, Session session){

        // Check in preSave that we are allowed to add items with a projectId of null.
        // This is only allowed for certain types, when api.admin.basedata=true
        if (Strings.isNullOrEmpty(userContext.getProjectId()))
        {
            throw new RuntimeException("Unable to save item - projectId is null");
        }


        Long parentConditionId = t.parentConditionId;
        t.condition.parentConditionId = parentConditionId;

        // Default isfragment=true if boolean, not, collection are not provided.
        if( parentConditionId == null && ( t.attachToCollectionId == null ) ) {
            t.condition.isFragment = true;
        }
        else {
            t.condition.isFragment = false;
            if(t.condition.order == null) {
                Criteria criteria = session
                        .createCriteria(typeParameterClass)
                        .add(Restrictions.or(Restrictions.eq("projectId", userContext.getProjectId()), Restrictions.isNull("projectId")))
                        .add(Restrictions.eq("parentConditionId", parentConditionId))
                        .setProjection(Projections.max("order"));
                Integer order = (Integer) criteria.uniqueResult();

                //To match mysql
                t.condition.order = order ==null ? 100 : order + 100;
            }
        }
        return (R)t;
    }

    @Override
    public Item update(ExecutionContext executionContext, Item t) {
        Session session = getSession(executionContext);

        preSave(t, session);
        session.createSQLQuery("update tbl_condition set type=:TYPE WHERE id=:ID AND project_id=:PROJECT_ID")
                .setString("TYPE", t.condition.conditionType.toString())
                .setLong("ID", t.condition.id)
                .setString("PROJECT_ID", userContext.getProjectId())
                .executeUpdate();

        session.update(t.condition);

        session.flush();
        session.evict(t.condition);

        return retrieve(executionContext, Arrays.asList(t.condition.id)).stream().findFirst().get();
    }

    @Override
    public void delete(ExecutionContext executionContext, Long id) {
        Session session = getSession(executionContext);

        String hql = "delete from Condition where id= :conditionId and projectId= :projectId";
        session.createQuery(hql).setLong("conditionId", id).setString("projectId", userContext.getProjectId()).executeUpdate();
    }

    @Override
    public PageOfResults<Item> retrievePage(ExecutionContext executionContext, PageRequest pageRequest) {
        // we can now use the same page method as the Filter restriction does.
        // N.B. The default when no filter is specified is is_Fragment=true.
        return retrievePage(executionContext, pageRequest, null, null);
    }


    @Override
    public PageOfResults<Item> retrievePage(ExecutionContext executionContext, PageRequest pageRequest, Filter filter ) {
        return retrievePage(executionContext,pageRequest,filter,null);
    }

    @Override
    public PageOfResults<Item> retrievePage(ExecutionContext executionContext, PageRequest pageRequest, Filter filter, Sort sort) {

        validatePageRequest( pageRequest );

        Session session = getSession(executionContext);

        Collection<Condition> conditions;
        {
            Criteria criteria = createItemBaseCriteria(pageRequest, session, filter, sort);

            addCriterionBasedOnFilter(filter, session, criteria, typeParameterClass);

            //To do dont return children
            conditions = criteria.list();
        }
        Long resultCount;
        {
            Criteria criteria = createThisObjectsBaseCriteria( session, filter );

            addCriterionBasedOnFilter(filter, session, criteria, typeParameterClass);

            // Add additional rowCount projection.
            criteria.setProjection(Projections.rowCount());

            resultCount = (Long) criteria.uniqueResult();
        }

        // we aren't loading children for now, but it does correctly set items to null,
        // to illustrate we didn't request them.
        loadChildren(session, conditions, false);

        PageOfResults<Item> pageOfResults = new PageOfResults<>();
        pageOfResults.results = toItem(conditions);
        pageOfResults.totalhits = resultCount;

        return pageOfResults;
    }

    @Override
    public PageOfResults<Item> retrievePage(ExecutionContext executionContext, PageRequest pageRequest, Sort sort) {
        return retrievePage(executionContext, pageRequest, null, sort);
    }

    @Override
    public Collection<Item> retrieve(ExecutionContext executionContext, Collection<Long> ids) {
        return retrieve(executionContext, ids, false);
    }

    private Collection<Item> toItem(Collection<Condition> conditions) {
        if(conditions == null) {
            return null;
        }
        return conditions.stream().map(u-> {
                    Item item = new Item();
                    item.condition = u;
                    item.parentConditionId = u.parentConditionId;
                    return item;
                }).collect(Collectors.toList());
    }

    /**
     * Load or remove the children based on the includeChildren property
     * @param session the session
     * @param conditions the conditions to load children for
     * @param includeChildren indicates if we should load the children or clear the proxy object
     */
    private void loadChildren(Session session, Collection<Condition> conditions, boolean includeChildren) {
        if (conditions == null) {
            return;
        }

        //todo find a way to do this automatically
        for (Condition condition : conditions) {
            if (includeChildren) {
                //Load the children recursively
                if (condition instanceof NotCondition) {
                    Criteria criteria = createItemBaseCriteriaForParentConditionId(session, null, condition.id, null);

                    ((NotCondition) condition).condition = (Condition) criteria.uniqueResult();
                    Condition child = ((NotCondition) condition).condition;

                    loadChildren(session, Collections.singletonList(child), true);
                } else if (condition instanceof BooleanCondition) {
                    Criteria criteria = createItemBaseCriteriaForParentConditionId(session, null, condition.id, null);

                    criteria.addOrder(Order.asc("order"));
                    criteria.addOrder(Order.asc("name").ignoreCase());

                    List<Condition> children = criteria.list();
                    ((BooleanCondition) condition).children = children == null ? new ArrayList<>() : children;
//                    Hibernate.initialize(children);

                    loadChildren(session, children, true);
                }
            } else {
                //Detach the condition from the session so updates wont be saved
//                session.evict(condition);

                //Remove the children
                if (condition instanceof NotCondition) {
                    ((NotCondition) condition).condition = null;
                } else if (condition instanceof BooleanCondition) {
                    ((BooleanCondition) condition).children = null;
                }
            }
        }
    }

    @Override
    protected Criteria createThisObjectsBaseCriteria(Session session) {
        return createThisObjectsBaseCriteria(session, null);
    }

    @Override
    protected Criteria createThisObjectsBaseCriteria(Session session, Filter filter ) {

        // If the filter isn't specified, then default isFragment boolean to true.
        // Now if we do have a filter specified, then we have 2 exceptions.
        // 1) IF they are filtering on is_fragment
        // 2) IF they are filtering on value=xx ( treat same as single condition retrieve. )
        // For both of these conditions dont add an isFragment boolean, leave to later on
        // If it truely is a restriction it will be added with other filter fields.
        Boolean isFragmentRestriction =
                (filter != null &&
                        (filter.has(ApiStrings.Conditions.Arguments.IS_FRAGMENT) || filter.has(ApiStrings.Conditions.Arguments.VALUE))) ?
                        null : true;

        // If we have no filter - default the isFragment to true.
        return createItemBaseCriteriaForParentConditionId(session, filter, null, isFragmentRestriction);
    }

    @Override
    protected Criteria createItemBaseCriteria(PageRequest pageRequest, Session session, Filter filter, Sort sort) {
        Boolean isFragmentRestriction =
                (filter != null &&
                        (filter.has(ApiStrings.Conditions.Arguments.IS_FRAGMENT) || filter.has(ApiStrings.Conditions.Arguments.VALUE))) ?
                        null : true;

        Criteria criteria = createItemBaseCriteriaForParentConditionId(session, filter, null, isFragmentRestriction);
        createSortCritera(criteria,sort);
        addCriterionForPagedResults(pageRequest, criteria);
        return criteria;
    }

    // private implementation of our base criteria needs to know if parentConditionID is to be used in restriction or null
    // to bring back parent conditions.
    private Criteria createItemBaseCriteriaForParentConditionId(Session session, Filter filter, Long parentConditionId, Boolean isFragment){

        // by default we use the type parameter class of this repository.
        // if we have a filter, it may be that we need the real concrete class which is
        // used as a discriminator at the backend.
        Class<?> criteriaClass = ( filter == null ) ? typeParameterClass : AnnotationHelper.getRealRequestClassType(typeParameterClass, filter);

        Criteria criteria = session.createCriteria(criteriaClass);

        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
                .add(Restrictions.eq("projectId", userContext.getProjectId()))
                .add(parentConditionId == null ?
                        Restrictions.isNull("parentConditionId") : Restrictions.eq("parentConditionId", parentConditionId));

        if ( isFragment != null ) {
            criteria.add(Restrictions.eq("isFragment", isFragment));
        }

        return criteria;
    }

    private void createSortCritera(Criteria criteria,Sort sort){
        Collection<Order> orders = new ArrayList<>();
        if (sort != null) {
            orders = addCriteriaBasedOnSort(criteria, sort, Condition.class);
        } else {
            orders = Arrays.asList(Order.asc("name").ignoreCase(), Order.asc("id"));
        }
        if (orders.stream().filter(e -> e.getPropertyName().equalsIgnoreCase("id")).count() == 0) {
            orders.add(Order.asc("id"));
        }
        for (Order order : orders) {
            criteria.addOrder(order);
        }
    }




    @Override
    protected Criteria createItemBaseCriteria(PageRequest pageRequest, Session session, Filter filter) {
        Criteria criteria = createThisObjectsBaseCriteria(session, filter);

        // now add on additional ordering and LIMIT result criteria.
        createSortCritera( criteria, null );

        addCriterionForPagedResults(pageRequest, criteria);

        return criteria;
    }

    @Override
    protected Criteria createItemBaseCriteria(PageRequest pageRequest, Session session) {
        Criteria criteria = createThisObjectsBaseCriteria(session);

        // now add on additional ordering and LIMIT result criteria.
        createSortCritera( criteria, null );

        addCriterionForPagedResults(pageRequest, criteria);

        return criteria;
    }

    protected Session getSession(ExecutionContext executionContext){
        //Assuming this is always a HibernateExecutionContextImpl which I dont really like...
        return ((HibernateExecutionContextImpl) executionContext).getSession();
    }
}
