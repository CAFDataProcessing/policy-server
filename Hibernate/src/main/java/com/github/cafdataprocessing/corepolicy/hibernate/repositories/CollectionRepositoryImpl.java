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

import com.github.cafdataprocessing.corepolicy.common.AnnotationHelper;
import com.github.cafdataprocessing.corepolicy.common.UserContext;
import com.github.cafdataprocessing.corepolicy.common.dto.Filter;
import com.github.cafdataprocessing.corepolicy.common.dto.PageOfResults;
import com.github.cafdataprocessing.corepolicy.common.dto.PageRequest;
import com.github.cafdataprocessing.corepolicy.common.dto.Sort;
import com.github.cafdataprocessing.corepolicy.hibernate.dto.HibernateCollectionPolicy;
import com.github.cafdataprocessing.corepolicy.repositories.v2.CollectionRepository;
import com.github.cafdataprocessing.corepolicy.repositories.v2.ExecutionContext;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.*;


/**
 *
 */
@Component
public class CollectionRepositoryImpl extends HibernateBaseRepository<CollectionRepository.Item> implements CollectionRepository {


    private String objectName = "CollectionRepository$Item";
    private Class<Item> typeParameterClass = Item.class;

    @Autowired
    public CollectionRepositoryImpl(UserContext userContext, ApplicationContext context ){
        super(userContext, context);
    }

    @Override
    public void associatePolicyWithCollection(ExecutionContext executionContext, long policyId, long collectionId) {
        Session session = getSession(executionContext);
        HibernateCollectionPolicy collectionPolicy = new HibernateCollectionPolicy();
        collectionPolicy.collectionId = collectionId;
        collectionPolicy.policyId = policyId;
        session.save(collectionPolicy);
    }

    @Override
    public void dissociatePolicyFromCollection(ExecutionContext executionContext, long policyId, long collectionId) {
        Session session = getSession(executionContext);
        String hql = "delete from HibernateCollectionPolicy where collectionId= :collectionId and policyId=:policyId and projectId= :projectId";
        int i = session.createQuery(hql)
                .setLong("collectionId", collectionId)
                .setLong("policyId", policyId)
                .setString("projectId", userContext.getProjectId())
                .executeUpdate();

        if(i == 0){
            throw new RuntimeException("Item could not be deleted");
        }
    }

    @Override
    public Set<Long> getPolicyIdsForCollection(ExecutionContext executionContext, long collectionId) {
        Session session = getSession(executionContext);

        String hql = "select policyId from HibernateCollectionPolicy where collectionId= :collectionId and projectId= :projectId";
        List<Long> list = session.createQuery(hql)
                .setLong("collectionId", collectionId)
                .setString("projectId", userContext.getProjectId())
                .list();

        HashSet<Long> ids = new HashSet<>();
        if (list != null) {
            ids.addAll(list);
        }
        return ids;
    }

    @Override
    public Set<Long> getCollectionIdsForPolicy(ExecutionContext executionContext, long policyId) {
        Session session = getSession(executionContext);

        String hql = "select collectionId from HibernateCollectionPolicy where policyId= :policyId and projectId= :projectId";
        List<Long> list = session.createQuery(hql)
                .setLong("policyId", policyId)
                .setString("projectId", userContext.getProjectId())
                .list();

        HashSet<Long> ids = new HashSet<>();
        if (list != null) {
            ids.addAll(list);
        }
        return ids;
    }

    @Override
    protected <T1 extends Item> T1 create(Session session, T1 t) {
        if(t.collection.id != null) {
            throw new RuntimeException("Id should be null");
        }

        t.collection.id = null;

        preSave(t, session);
        session.save(t);

        //Flush saves changes
        session.flush();
        //Evict so we get a new object returned
        evict(session, t);

        return (T1)retrieveSingleItem(session, t.collection.id);
    }

    @Override
    protected <T1 extends Item> T1 update(Session session, T1 t) {

        preSave(t, session);
        session.update(t);

        //Flush saves changes
        session.flush();
        //Evict so we get a new object returned
        evict(session, t);

        return (T1)retrieveSingleItem(session, t.collection.id);
    }

    @Override
    protected void delete(Session session, Long id) {
        String hql = "delete from " + objectName + " where id= :id and collection.projectId= :projectId";
        int i = session.createQuery(hql)
                .setLong("id", id)
                .setString("projectId", userContext.getProjectId())
                .executeUpdate();

        if(i == 0){
            throw new RuntimeException("Item could not be deleted");
        }
    }

    @Override
    protected PageOfResults<Item> retrievePage(Session session, PageRequest pageRequest) {
        return retrievePage(session, pageRequest, null, null);
    }


    @Override
    protected PageOfResults<Item> retrievePage(Session session, PageRequest pageRequest, Filter filter) {
        Collection<Item> items;
        {
            Criteria criteria = createItemBaseCriteria(pageRequest, session, filter);

            // add on some additional filter clauses.
            addCriterionBasedOnFilter(filter, session, criteria, typeParameterClass);

            items = criteria.list();

            // ensure any items, are disassociated from the DB. so any changes to these objects aren't written
            // back.  We only support updates via update/create calls.
            evict(session, items);

            // Ensure we set lazy initialized fields to null, to prevent a rea  d trying to access DB outside this scope
            // i.e. when sessionproxy is gone as it will fail.
                forceLazyInitializedPropsToNull(items);
        }
        Long resultCount;
        {
            Criteria criteria = createThisObjectsBaseCriteria(session, filter);

            // add on some additional filter clauses.
            addCriterionBasedOnFilter(filter, session, criteria, typeParameterClass);

            // add row count.
            criteria.setProjection(Projections.rowCount());

            resultCount = (Long) criteria.uniqueResult();
        }

        PageOfResults<Item> pageOfResults = new PageOfResults<>();
        pageOfResults.results = items;
        pageOfResults.totalhits = resultCount;

        return pageOfResults;
    }

    @Override
    protected PageOfResults<Item> retrievePage(Session session, PageRequest pageRequest, Filter filter, Sort sort) {
        Collection<Item> items;
        {
            Criteria criteria = createItemBaseCriteria(pageRequest, session, filter, sort);

            // add on some additional filter clauses.
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
            Criteria criteria = createThisObjectsBaseCriteria(session, filter);

            // add on some additional filter clauses.
            addCriterionBasedOnFilter(filter, session, criteria, typeParameterClass);

            // add row count.
            criteria.setProjection(Projections.rowCount());

            resultCount = (Long) criteria.uniqueResult();
        }

        PageOfResults<Item> pageOfResults = new PageOfResults<>();
        pageOfResults.results = items;
        pageOfResults.totalhits = resultCount;

        return pageOfResults;
    }

    @Override
    protected PageOfResults<Item> retrievePage(Session session, PageRequest pageRequest, Sort sort) {
        Collection<Item> items;
        {
            Criteria criteria = createItemBaseCriteria( pageRequest, session, null, sort );

            // add on some additional filter clauses.
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

            // add on some additional filter clauses.

            // add row count.
            criteria.setProjection(Projections.rowCount());

            resultCount = (Long) criteria.uniqueResult();
        }

        PageOfResults<Item> pageOfResults = new PageOfResults<>();
        pageOfResults.results = items;
        pageOfResults.totalhits = resultCount;

        return pageOfResults;
    }

    @Override
    protected void forceLazyInitializedPropsToNull(Collection<Item> items) {

        if ( items == null || items.size() == 0 )
            return;

        // Ensure we set lazy initialized fields to null, to prevent a read trying to access DB outside this scope
        // i.e. when sessionproxy is gone as it will fail.
        items.stream().filter(u -> u.collection != null).forEach(u -> u.collection.policyIds = null);
    }

    @Override
    protected Collection<Item> retrieve(Session session, Collection<Long> ids) {
        Criteria criteria = createThisObjectsBaseCriteria(session);

        // add on the single item restriction.
        criteria.add(Restrictions.in("id", ids));

        List<Item> items = criteria.list();
        evict(session, items);
        //check all items are returned
        if(!ids.stream().distinct().allMatch(u->items.stream().anyMatch(i-> Objects.equals(i.collection.id, u)))){
            throw new RuntimeException("Could not return items for all ids");
        }
        return items;
    }

    @Override
    protected Criteria createThisObjectsBaseCriteria(Session session) {
        return createThisObjectsBaseCriteria(session, null);
    }

    @Override
    protected Criteria createThisObjectsBaseCriteria(Session session, Filter filter ) {

        // by default we use the type parameter class of this repository.
        // if we have a filter, it may be that we need the real concrete class which is
        // used as a discriminator at the backend.
        Class<?> criteriaClass = ( filter == null) ? typeParameterClass : AnnotationHelper.getRealRequestClassType(typeParameterClass, filter);

        return session.createCriteria(typeParameterClass)
                .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
                .add(Restrictions.or(
                        Restrictions.eq("collection.projectId", userContext.getProjectId()),
                        Restrictions.isNull("collection.projectId")));
    }

    @Override
    protected Criteria createItemBaseCriteria(PageRequest pageRequest, Session session, Filter filter, Sort sort) {
        Criteria criteria = createThisObjectsBaseCriteria(session, filter);
        
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
        // I have moved all the ordering into the relevant classes, if they need to override
        // default ordering.
        addCriterionForPagedResults(pageRequest, criteria);
        return criteria;
    }

    @Override
    protected Criteria createItemBaseCriteria(PageRequest pageRequest, Session session) {
        return createItemBaseCriteria(pageRequest, session, null, null);
    }

    @Override
    protected Criteria createItemBaseCriteria(PageRequest pageRequest, Session session, Filter filter) {

        return createItemBaseCriteria(pageRequest, session, filter, null);
    }

    @Override
    protected Collection<Order> getSortFields(boolean ascending){

        final Collection<Order> ascSortFields = Arrays.asList(Order.asc("collection.name").ignoreCase(), Order.asc("id"));
        final Collection<Order> descSortFields = Arrays.asList(Order.desc("collection.name").ignoreCase(), Order.desc("id"));

        return ascending ? ascSortFields : descSortFields;
    }

    @Override
    protected Collection<Order> getSortFields(Criteria criteria, Sort sort, Class<?> requestClassType) {
        Collection<Order> orders = new ArrayList<>();
        orders = addCriteriaBasedOnSort(criteria, sort, requestClassType);
        if (orders == null || orders.isEmpty()) {
            return getSortFields(true);
        } else {
            return orders;
        }
    }

    @Override
    protected RuntimeException handleCreateException(Exception exception) {
        return new RuntimeException(exception);
    }

    @Override
    protected RuntimeException handleUpdateException(Exception exception) {
        return new RuntimeException(exception);
    }
}
