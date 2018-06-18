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

import com.github.cafdataprocessing.corepolicy.common.UserContext;
import com.github.cafdataprocessing.corepolicy.repositories.v2.CollectionSequenceEntryRepository;
import com.github.cafdataprocessing.corepolicy.repositories.v2.ExecutionContext;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 *
 */
@Component
public class CollectionSequenceEntryRepositoryImpl extends HibernateBaseRepositoryImpl<CollectionSequenceEntryRepository.Item> implements CollectionSequenceEntryRepository {

    public static final String SQL_GET_COLLECTION_SQUENCE_IDS_BY_COLLECTION_IDS =
            "Select distinct(cse.collection_sequence_id)"
                    + " FROM tbl_collection_sequence_entry cse"
                    + " JOIN tbl_collection_sequence_entry_collection csec"
                    + " ON cse.id = csec.collection_sequence_entry_id"
                    + " WHERE csec.collection_id in (%s)";


    @Autowired
    public CollectionSequenceEntryRepositoryImpl(UserContext userContext, ApplicationContext context ){
        super(userContext, CollectionSequenceEntryRepository.Item.class, "CollectionSequenceEntryRepository$Item", context);
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
    protected CollectionSequenceEntryRepository.Item create(Session session, CollectionSequenceEntryRepository.Item t){
        t.id = null;
        if(t.id != null) {
            throw new RuntimeException("Id should be null");
        }

        preSave(t, session);
        session.save(t);

        //Flush saves changes
        session.flush();
        //Evict so we get a new object returned
        evict(session, t);

        updateCollectionCount(session, t.collectionSequenceId);

        return retrieveSingleItem(session, t.id);
    }

    @Override
    protected CollectionSequenceEntryRepository.Item update(Session session, CollectionSequenceEntryRepository.Item t){
        preSave(t, session);
        session.update(t);

        //Flush saves changes
        session.flush();
        //Evict so we get a new object returned
        evict(session, t);

        updateCollectionCount(session, t.collectionSequenceId);

        return retrieveSingleItem(session, t.id);
    }

    @Override
    protected void delete(Session session, Long id){
        Item item = retrieveSingleItem(session, id);


        //noinspection JpaQlInspection
        String hql = "delete from " + objectName + " where id= :id and projectId= :projectId";
        int i = session.createQuery(hql)
                .setLong("id", id)
                .setString("projectId", userContext.getProjectId())
                .executeUpdate();

        if(i == 0){
            throw new RuntimeException("Item could not be deleted");
        }

        updateCollectionCount(session, item.collectionSequenceId);
    }

    @Override
    protected Collection<Order> getSortFields(boolean ascending) {
        final Collection<Order> ascSortFields = Arrays.asList( Order.asc( "collectionSequenceEntry.order" ), 
                                                               Order.asc( "id" ) );
        final Collection<Order> descSortFields = Arrays.asList( Order.desc( "collectionSequenceEntry.order" ),
                                                                Order.desc( "id" ) );

        return ascending ? ascSortFields : descSortFields;
    }

    @Override
    public Collection<Item> retrieveForCollectionSequence(ExecutionContext executionContext, Long collectionSequenceId) {
        Session session = getSession(executionContext);

        Criteria criteria = createCriteriaCommon(session, null, null )
                    .add(Restrictions.eq("collectionSequenceId", collectionSequenceId));
                            
        List<Item> items = criteria.list();
        evict(session, items);
        forceLazyInitializedPropsToNull(items);
        return items;
    }

    @Override
    protected void forceLazyInitializedPropsToNull( Collection<CollectionSequenceEntryRepository.Item> items ) {

        // nothing to do here, as it has no lazy initialized props!
        return;
    }

    @Override
    public void deleteAll(ExecutionContext executionContext, Long collectionSequenceId) {
        Session session = getSession(executionContext);
        String hql = "delete from CollectionSequenceEntryRepository$Item where collectionSequenceId= :collectionSequenceId and projectId= :projectId";
        int i = session.createQuery(hql)
                .setLong("collectionSequenceId", collectionSequenceId)
                .setString("projectId", userContext.getProjectId())
                .executeUpdate();
    }

    static final String selectCollectionCountSQL =
            "(SELECT COUNT(csInner.id) FROM tbl_collection_sequence csInner " +
            "LEFT JOIN tbl_collection_sequence_entry cse ON cse.collection_sequence_id = csInner.id " +
            "LEFT JOIN tbl_collection_sequence_entry_collection csec ON csec.collection_sequence_entry_id = cse.id " +
            "WHERE csInner.id = :sequenceId) ";
    private void updateCollectionCount(Session session, Long collectionSequenceId){
        //todo try to rewrite as hql
        BigInteger collectionCount = (BigInteger)session.createSQLQuery(selectCollectionCountSQL).setLong("sequenceId", collectionSequenceId).uniqueResult();

        session.createQuery("update CollectionSequence set collectionCount=:count where id=:sequenceId")
                .setInteger("count", collectionCount.intValue())
                .setLong("sequenceId", collectionSequenceId)
                .executeUpdate();
    }
}
