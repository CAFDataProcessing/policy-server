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

import com.github.cafdataprocessing.corepolicy.common.dto.*;
import com.google.common.base.Strings;
import com.github.cafdataprocessing.corepolicy.common.AnnotationHelper;
import com.github.cafdataprocessing.corepolicy.common.UserContext;
import com.github.cafdataprocessing.corepolicy.common.exceptions.InvalidFieldValueCpeException;
import com.github.cafdataprocessing.corepolicy.common.exceptions.excpetionErrors.InvalidFieldValueErrors;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.context.ApplicationContext;

import java.util.*;

/**
 * Extension of the HibernateBaseRepository class with further methods
 */
public abstract class HibernateBaseRepositoryImpl<T extends DtoBase> extends HibernateBaseRepository<T> {

    protected Class<T> typeParameterClass;
    protected String objectName;

    public HibernateBaseRepositoryImpl(UserContext userContext, Class<T> classType, String objectName, ApplicationContext applicationContext) {
        super(userContext, applicationContext);

        this.typeParameterClass = classType;
        this.objectName = objectName;
    }

    protected <T1 extends T> T1 create(Session session, T1 t) {
        t.id = null;
        if (t.id != null) {
            throw new RuntimeException("Id should be null");
        }

        t = preSave(t, session);
        session.save(t);

        //Flush saves changes
        session.flush();
        //Evict so we get a new object returned
        evict(session, t);

        return (T1) retrieveSingleItem(session, t.id);
    }

    protected <T1 extends T> T1 update(Session session, T1 t) {
        t = preSave(t, session);
        session.update(t);

        //Flush saves changes
        session.flush();
        //Evict so we get a new object returned
        evict(session, t);
        return (T1) retrieveSingleItem(session, t.id);
    }

    protected void delete(Session session, Long id) {

        // Check in preSave that we are allowed to add items with a projectId of null.
        // This is only allowed for certain types, when api.admin.basedata=true
        if (Strings.isNullOrEmpty(userContext.getProjectId())) {
            throw new RuntimeException("Unable to delete item " + objectName + " - projectId is null");
        }

        //noinspection JpaQlInspection
        String hql = "delete from " + objectName + " where id= :id and projectId= :projectId";
        int i = session.createQuery(hql)
                .setLong("id", id)
                .setString("projectId", userContext.getProjectId())
                .executeUpdate();

        if (i == 0) {
            throw new RuntimeException("Item could not be deleted");
        }
    }

    protected PageOfResults<T> retrievePage(Session session, PageRequest pageRequest) {
        return retrievePage(session, pageRequest, null, null);
    }

    protected PageOfResults<T> retrievePage(Session session, PageRequest pageRequest, Filter filter) {
        Collection<T> items;
        {
            Criteria criteria = createItemBaseCriteria(pageRequest, session);

            addCriterionBasedOnFilter(filter, session, criteria, typeParameterClass);

            items = criteria.list();

            // ensure any items, are disassociated from the DB. so any changes to these objects aren't written
            // back.  We only support updates via update/create calls.
            evict(session, items);

            // Ensure we set lazy initialized fields to null, to prevent a read trying to access DB outside this scope
            // i.e. when sessionproxy is gone as it will fail.
            forceLazyInitializedPropsToNull(items);
        }
        Long resultCount;
        {
            Criteria criteria = createThisObjectsBaseCriteria(session);

            addCriterionBasedOnFilter(filter, session, criteria, typeParameterClass);

            // add on the row count.
            criteria.setProjection(Projections.rowCount());

            resultCount = (Long) criteria.uniqueResult();
        }

        PageOfResults<T> pageOfResults = new PageOfResults<>();
        pageOfResults.results = items;
        pageOfResults.totalhits = resultCount;

        return pageOfResults;
    }

    protected PageOfResults<T> retrievePage(Session session, PageRequest pageRequest, Filter filter, Sort sort) {
        Collection<T> items;
        Long resultCount;
        {
            Criteria criteria;
            criteria = createItemBaseCriteria(pageRequest, session, filter, sort);
            addCriterionBasedOnFilter(filter, session, criteria, typeParameterClass);
            items = criteria.list();

            // ensure any items, are disassociated from the DB. so any changes to these objects aren't written
            // back.  We only support updates via update/create calls.
            evict(session, items);

            // Ensure we set lazy initialized fields to null, to prevent a read trying to access DB outside this scope
            // i.e. when sessionproxy is gone as it will fail.
            forceLazyInitializedPropsToNull(items);
        }

        {
            Criteria criteria = createThisObjectsBaseCriteria(session);

            addCriterionBasedOnFilter(filter, session, criteria, typeParameterClass);

            // add on the row count.
            criteria.setProjection(Projections.rowCount());

            resultCount = (Long) criteria.uniqueResult();
        }

        PageOfResults<T> pageOfResults = new PageOfResults<>();
        pageOfResults.results = items;
        pageOfResults.totalhits = resultCount;

        return pageOfResults;
    }

    protected PageOfResults<T> retrievePage(Session session, PageRequest pageRequest, Sort sort) {
        Collection<T> items;
        Long resultCount;
        {
            Criteria criteria = createItemBaseCriteria(pageRequest, session, null, sort);
            items = criteria.list();

            // ensure any items, are disassociated from the DB. so any changes to these objects aren't written
            // back.  We only support updates via update/create calls.
            evict(session, items);

            // Ensure we set lazy initialized fields to null, to prevent a read trying to access DB outside this scope
            // i.e. when sessionproxy is gone as it will fail.
            forceLazyInitializedPropsToNull(items);
        }

        {
            Criteria criteria = createThisObjectsBaseCriteria(session);
            criteria.setProjection(Projections.rowCount());
            resultCount = (Long) criteria.uniqueResult();
        }

        PageOfResults<T> pageOfResults = new PageOfResults<>();
        pageOfResults.results = items;
        pageOfResults.totalhits = resultCount;

        return pageOfResults;
    }


    protected Collection<T> retrieve(Session session, Collection<Long> ids) {
        Criteria criteria = createThisObjectsBaseCriteria(session);

        // Sort the information based on the default sort order.
        addSortOrder( null, criteria );
        
        // add in the id specific restriction.
        criteria.add(Restrictions.in("id", ids));

        List<T> items = criteria.list();
        evict(session, items);
        //check all items are returned
        if (!ids.stream().distinct().allMatch(u -> items.stream().anyMatch(i -> Objects.equals(i.id, u)))) {
            if (ids.size() == 1) {
                if (objectName.equalsIgnoreCase("CollectionSequence")) {
                    throw new InvalidFieldValueCpeException(InvalidFieldValueErrors.NO_MATCHING_COLLECTION_SEQUENCE);
                }

                throw new RuntimeException("Could not find a match for the " + objectName + " requested.");

            }

            throw new RuntimeException("Could not find a match for all " + objectName + " items requested.");

        }

        // Ensure we set lazy initialized fields to null, to prevent a read trying to access DB outside this scope
        // i.e. when sessionproxy is gone as it will fail.
        forceLazyInitializedPropsToNull(items);

        return items;
    }

    @Override
    protected Criteria createThisObjectsBaseCriteria(Session session) {
        return createThisObjectsBaseCriteria( session, null );
    }
    
    @Override
    protected Criteria createThisObjectsBaseCriteria(Session session, Filter filter) {

        // by default we use the type parameter class of this repository.
        // if we have a filter, it may be that we need the real concrete class which is
        // used as a discriminator at the backend.
        Class<?> criteriaClass = (filter == null) ? typeParameterClass : AnnotationHelper.getRealRequestClassType(typeParameterClass, filter);
        return session.createCriteria(criteriaClass)
                .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
                .add(Restrictions.or(
                        Restrictions.eqOrIsNull("projectId", userContext.getProjectId()),
                        Restrictions.isNull("projectId")
                ));
    }
    
    @Override
    protected Criteria createItemBaseCriteria(PageRequest pageRequest, Session   session) {

        return createItemBaseCriteria(pageRequest, session, null, null);
    }

    @Override
    protected Criteria createItemBaseCriteria(PageRequest pageRequest, Session session, Filter filter) {

        return createItemBaseCriteria(pageRequest, session, filter, null);
    }

    @Override
    protected Criteria createItemBaseCriteria(PageRequest pageRequest, Session session, Filter filter, Sort sort) {
        Criteria criteria = createCriteriaCommon(session, filter, sort);
        addCriterionForPagedResults(pageRequest, criteria);
        return criteria;
    }

    protected Criteria createCriteriaCommon( Session session, Filter filter, Sort sort ) {
        Criteria criteria = createThisObjectsBaseCriteria(session, filter);
        addSortOrder( sort, criteria );
        return criteria;
    }

    protected void addSortOrder( Sort sort, Criteria criteria ) {
        Collection<Order> orders;
        if (sort != null) {
            orders = getSortFields(criteria, sort, typeParameterClass);
        } else {
            orders = getSortFields(true);
        }
        if (orders != null) {
            for (Order sortField : orders) {
                criteria.addOrder(sortField);
            }
        }
    }

    @Override
    protected Collection<Order> getSortFields(Criteria criteria, Sort sort, Class<?> requestClassType) {

        Collection<Order> orders = new ArrayList<>();
        if (sort == null) {
            orders.add(Order.asc("id"));
            return orders;
        }
        orders = addCriteriaBasedOnSort(criteria, sort, requestClassType);
        //If the passed in sort doesn't specify the id then default to ascending.
        if (orders.stream().filter(e -> e.getPropertyName().equalsIgnoreCase("id")).count() == 0) {
            orders.add(Order.asc("id"));
        }
        return orders;
    }

}
