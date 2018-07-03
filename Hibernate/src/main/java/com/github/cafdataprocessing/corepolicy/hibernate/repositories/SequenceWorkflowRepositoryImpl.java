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

import com.github.cafdataprocessing.corepolicy.repositories.v2.ExecutionContext;
import com.google.common.base.Strings;
import com.github.cafdataprocessing.corepolicy.common.AdminUserContext;
import com.github.cafdataprocessing.corepolicy.common.UserContext;
import com.github.cafdataprocessing.corepolicy.common.dto.SequenceWorkflow;
import com.github.cafdataprocessing.corepolicy.common.dto.SequenceWorkflowEntry;
import com.github.cafdataprocessing.corepolicy.repositories.v2.SequenceWorkflowRepository;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collection;

/**
 * Hibernate implementation of workflow repository for operations on workflows using hibernate backend
 */
@Component
public class SequenceWorkflowRepositoryImpl extends HibernateBaseRepositoryImpl<SequenceWorkflow> implements SequenceWorkflowRepository {


    @Autowired
    public SequenceWorkflowRepositoryImpl(UserContext userContext, ApplicationContext context) {
        super(userContext, SequenceWorkflow.class, "SequenceWorkflow", context);
    }

    @Override
    protected Collection<Order> getSortFields(boolean ascending) {

        final Collection<Order> ascSortFields = Arrays.asList(Order.asc("name").ignoreCase(), Order.asc("id"));
        final Collection<Order> descSortFields = Arrays.asList(Order.desc("name").ignoreCase(), Order.desc("id"));

        return ascending ? ascSortFields : descSortFields;
    }

    @Override
    public SequenceWorkflow update(ExecutionContext executionContext, SequenceWorkflow t) {
        return update(getSession(executionContext), t);
    }

    protected <T1 extends SequenceWorkflow> T1 update(Session session, T1 t){
        t = preSave(t, session);
        session.update(t);

        //Flush saves changes
        session.flush();
        //Evict so we get a new object returned
        evict(session, t);
        return (T1)retrieveSingleItem(session, t.id);
    }


    @Override
    protected <R extends SequenceWorkflow> R preSave(SequenceWorkflow item, Session session){

        // During the creation call, we assign the order to the item, depending on the incoming
        // order in the list, if they dont have any order themselves.

        // ( If we have an id field, then the item has been saved before )
        if ( item.id != null && item.sequenceWorkflowEntries != null )
        {
            deleteSequenceWorkflowEntries(session, item.id);
        }

        // ensure correct ordering now of the workflow entries.
        short maxOrder = 0;
        maxOrder = getMaxOrder(item, maxOrder);

        // default our starting value.
        if ( maxOrder == 0 )
        {
            maxOrder = 100;
        }
        else {
            maxOrder+=100;
        }

        // Now assign all fields a unique order if they dont have any.
        // Note we will assign any null entries, after the last max order field
        for (SequenceWorkflowEntry entry : item.sequenceWorkflowEntries ) {

            entry.order = ( entry.order != null ) ? entry.order : maxOrder;
            maxOrder+=100;
        }

        // The above is used to correctly order the entries, but they are not actually saved during this save call.
        // We still have our repository interface, split into 2 for this object, and its subobject the entries.
        // As such we need to disassociate them now before the save / flush / evict happens.
        item.sequenceWorkflowEntries = null;
        
        // Check in preSave that we are allowed to add items with a projectId of null.
        // This is only allowed for certain types, when api.admin.basedata=true
        if (!Strings.isNullOrEmpty(userContext.getProjectId()))
        {
            return (R)item;
        }

        // check is the adminUserContext allowing administration of base data.
        AdminUserContext adminUserContext = applicationContext.getBean(AdminUserContext.class);
        if ( adminUserContext != null && adminUserContext.isAllowAdministration() ) {
            return (R) item;
        }

        throw new RuntimeException("Unable to save item - projectId is null");
    }

    private void deleteSequenceWorkflowEntries(Session session, Long sequenceWorkflowId) {
        // ensure any entries already in the DB, are deleted, we recreate this list
        // each time, to ensure correct ordering.
        String hql = "delete from SequenceWorkflowEntryRepository$Item where sequenceWorkflowId= :sequenceWorkflowId and projectId= :projectId";
        session.createQuery(hql)
                .setLong("sequenceWorkflowId", sequenceWorkflowId)
                .setString("projectId", userContext.getProjectId())
                .executeUpdate();
    }

    private Short getMaxOrder(SequenceWorkflow t, Short maxOrder) {
        for (SequenceWorkflowEntry entry : t.sequenceWorkflowEntries ) {
            if (entry.order != null && entry.order > maxOrder) {
                maxOrder = entry.order;
            }
        }
        return maxOrder;
    }

    @Override
    protected void forceLazyInitializedPropsToNull(Collection<SequenceWorkflow> items) {

        //The collectionSequence on the entry is only used for filter/sorting as such we are not using it
        // to populate the Set of SequenceWorkflow entries in a single call.  The interface is still a 2 call interface
        // as such retrieves the main workflow, then the entries and associates the 2.  We could add a includeSequenceWorkflowEntries
        // here which would be better for paging.
        for(SequenceWorkflow item:items){
           
            //remove whatever is here to avoid someone trying to retrieve after the session is gone.
            item.sequenceWorkflowEntries = null;
        }
        
        return;
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