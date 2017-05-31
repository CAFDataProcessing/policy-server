/*
 * Copyright 2015-2017 Hewlett Packard Enterprise Development LP.
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
import com.github.cafdataprocessing.corepolicy.repositories.v2.ExecutionContext;
import com.google.common.base.Strings;
import com.github.cafdataprocessing.corepolicy.common.UserContext;
import com.github.cafdataprocessing.corepolicy.repositories.v2.SequenceWorkflowEntryRepository;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Hibernate implementation of workflow entry repository for operations on workflow entries using hibernate backend
 */
@Component
public class SequenceWorkflowEntryRepositoryImpl extends HibernateBaseRepositoryImpl<SequenceWorkflowEntryRepository.Item> implements SequenceWorkflowEntryRepository {

    public static final String SQL_GET_SQUENCE_WORKFLOW_IDS_BY_COLLECTIONSEQUENCE_IDS =
            "Select distinct(sw.id)"
                    + " FROM tbl_sequence_workflow sw"  
                    + " JOIN tbl_sequence_workflow_sequences swe"
                    + " ON sw.id = swe.sequence_workflow_id"
                    + " WHERE swe.collection_sequence_id in (%s)";


    @Autowired
    public SequenceWorkflowEntryRepositoryImpl(UserContext userContext, ApplicationContext context) {
        super(userContext, SequenceWorkflowEntryRepository.Item.class, "SequenceWorkflowEntryRepository$Item", context);
    }

    @Override
    protected RuntimeException handleCreateException(Exception exception) {
        return new RuntimeException(exception);
    }

    @Override
    protected RuntimeException handleUpdateException(Exception exception) {
        return new RuntimeException(exception);
    }

    @Override
    protected SequenceWorkflowEntryRepository.Item create(Session session, SequenceWorkflowEntryRepository.Item t) {
            t.id = null;
        if (t.id != null) {
            throw new RuntimeException("Id should be null");
        }

        preSave(t, session);
        session.save(t);

        //Flush saves changes
        session.flush();
        //Evict so we get a new object returned
        evict(session, t);

        return retrieveSingleItem(session, t.id);
    }

    @Override
    protected SequenceWorkflowEntryRepository.Item update(Session session, SequenceWorkflowEntryRepository.Item t) {
        preSave(t, session);
        session.update(t);

        //Flush saves changes
        session.flush();
        //Evict so we get a new object returned
        evict(session, t);


        return retrieveSingleItem(session, t.id);
    }

    @Override
    protected void delete(Session session, Long id) {
        Item item = retrieveSingleItem(session, id);


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

    @Override
    protected Collection<Order> getSortFields(boolean ascending) {
        final Collection<Order> ascSortFields = Arrays.asList(Order.asc("sequenceWorkflowEntry.order").ignoreCase(), Order.asc("id"));
        final Collection<Order> descSortFields = Arrays.asList(Order.desc("sequenceWorkflowEntry.order").ignoreCase(), Order.desc("id"));

        return ascending ? ascSortFields : descSortFields;
    }

    @Override
    public Collection<Item> retrieveForSequenceWorkflow(ExecutionContext executionContext, Long sequenceWorkflowId) {
        Session session = getSession(executionContext);

        // Create common criteria for this object, and add an addition restriction to only return this workflow id.
        Criteria criteria = createCriteriaCommon(session, null, null)
                .add(Restrictions.eq("sequenceWorkflowId", sequenceWorkflowId));

        List<Item> items = criteria.list();
        evict(session, items);
        forceLazyInitializedPropsToNull(items);
        return items;
    }

    @Override
    public PageOfResults<Item> retrievePage(ExecutionContext executionContext, PageRequest pageRequest, Filter filter, Sort sort, Boolean includeCollectionSequence) {
        validatePageRequest( pageRequest );
        Session session = getSession(executionContext);

        Collection<Item> items;
        Long resultCount;
        {
            Criteria criteria = createItemBaseCriteria(pageRequest, session, filter, sort, includeCollectionSequence);
            
            addCriterionBasedOnFilter(filter, session, criteria, typeParameterClass);
            items = criteria.list();

            forceLazyInitializedPropsToNull(items,includeCollectionSequence);
            // ensure any items, are disassociated from the DB. so any changes to these objects aren't written
            // back.  We only support updates via update/create calls.
            evict(session, items);
        }

        {
            Criteria criteria = createThisObjectsBaseCriteria(session);

            addCriterionBasedOnFilter(filter, session, criteria, typeParameterClass);

            // add on the row count.
            criteria.setProjection(Projections.rowCount());

            resultCount = (Long) criteria.uniqueResult();
        }

        PageOfResults<Item> pageOfResults = new PageOfResults<>();
        pageOfResults.results = items;
        pageOfResults.totalhits = resultCount;

        return pageOfResults;
    }


    @Override
    protected void forceLazyInitializedPropsToNull(Collection<SequenceWorkflowEntryRepository.Item> items) {

        forceLazyInitializedPropsToNull(items,false);
        return;
    }

    protected void forceLazyInitializedPropsToNull(Collection<SequenceWorkflowEntryRepository.Item> items, Boolean includeCollectionSequences) {

        for(SequenceWorkflowEntryRepository.Item item:items){
            if(includeCollectionSequences){
                //Touch collectionSequence to force hibernate to retrieve it now
                Long id = item.sequenceWorkflowEntry.collectionSequence.id;
                CollectionSequence cs = item.sequenceWorkflowEntry.collectionSequence;

            } else {
                //remove whatever is here to avoid someone trying to retrieve after the session is gone.
                item.sequenceWorkflowEntry.collectionSequence = null;
            }
        }
        return;
    }

    @Override
    public void deleteAll(ExecutionContext executionContext, Long sequenceWorkflowId) {
        Session session = getSession(executionContext);
        String hql = "delete from SequenceWorkflowEntryRepository$Item where sequenceWorkflowId= :sequenceWorkflowId and projectId= :projectId";
        int i = session.createQuery(hql)
                .setLong("sequenceWorkflowId", sequenceWorkflowId)
                .setString("projectId", userContext.getProjectId())
                .executeUpdate();
    }

    @Override
    public SequenceWorkflowEntryRepository.Item preSave(SequenceWorkflowEntryRepository.Item item, Session session){
        //The collectionSequence on the entry is only used for filter/sorting, remove before updating.
        if(item.sequenceWorkflowEntry.collectionSequence!=null){
            item.sequenceWorkflowEntry.collectionSequence = null;
        }

        if (!Strings.isNullOrEmpty(userContext.getProjectId()))
        {
            return item;
        }

        throw new RuntimeException("Unable to save item - projectId is null");

    }

    public Criteria createItemBaseCriteria(PageRequest pageRequest, Session session, Filter filter, Sort sort, Boolean includeCollectionSequence){
        Criteria criteria = createItemBaseCriteria(pageRequest,session,filter,sort);
        
        if(!includeCollectionSequence) {
            return criteria;
        }

        String alias = createAliasFromPropertyName("collectionSequence");

        if(!checkForAlias(criteria, alias)){

            criteria.createAlias("sequenceWorkflowEntry.collectionSequence", alias);
        }
        return criteria;
    }


}
