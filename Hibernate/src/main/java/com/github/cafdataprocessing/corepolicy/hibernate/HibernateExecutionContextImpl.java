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
package com.github.cafdataprocessing.corepolicy.hibernate;

import com.github.cafdataprocessing.corepolicy.common.UserContext;
import com.github.cafdataprocessing.corepolicy.common.VersionNumber;
import com.github.cafdataprocessing.corepolicy.common.dto.ReleaseHistory;
import com.github.cafdataprocessing.corepolicy.repositories.RepositoryType;
import com.github.cafdataprocessing.corepolicy.repositories.VersionCheckStatus;
import com.github.cafdataprocessing.corepolicy.repositories.v2.ExecutionContext;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Order;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Hibernate based implementation of Execution context.
 */
public class HibernateExecutionContextImpl implements ExecutionContext {

    private WrappedSession session;
    private final UserContext userContext;
    private static Logger logger = LoggerFactory.getLogger(HibernateExecutionContextImpl.class);
    private static VersionCheckStatus versionCheckStatus;

    public HibernateExecutionContextImpl(Session session, UserContext userContext){
        this.userContext = userContext;
        this.session = new WrappedSession(session);
        try {
            this.checkVersion();
        } catch (Exception e) {
           throw new RuntimeException("Current DB Version is incompatible with the current api version.");
        }
    }

    private void checkVersion() throws Exception {

        try
        {
            if(versionCheckStatus!=null) {
                if (versionCheckStatus.isVersionSupported()) {
                    // version is supported, so just exit now
                    return;
                }
                // version isn't supported, so rethrow the original error which got us here.
                throw versionCheckStatus.getExceptionInfo();
            }
            try {
                Criteria criteria = session.createCriteria(ReleaseHistory.class)
                        .addOrder(Order.desc("majorVersion"))
                        .addOrder(Order.desc("minorVersion"))
                        .addOrder(Order.desc("revision"))
                        .setMaxResults(1);

                ReleaseHistory historyItem = (ReleaseHistory) criteria.uniqueResult();
                VersionNumber.isSupportedVersion(historyItem);
                versionCheckStatus = new VersionCheckStatus(RepositoryType.COREPOLICY);
            }
            catch (Exception e){
                logger.error("DB Error retrieving release history information.", e );

                // throw a generic error message which is user friendly
                throw new Exception("Current DB Version is incompatible with the current api version.");
            }
        }
        catch (Exception e){
            versionCheckStatus = new VersionCheckStatus(RepositoryType.COREPOLICY, e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public <R> R retry(Function<?, R> retry) {
        Transaction transaction = null;
        try {
            transaction = this.session.beginTransaction();
            R result = retry.apply(null);
            transaction.commit();
            return result;
        } catch (Exception e) {
            if(transaction != null) {
                try {
                    transaction.rollback();
                } catch (Exception ignored) {
                }
            }
            if(e instanceof RuntimeException){
                throw e;
            }
            throw new RuntimeException(e);
        }
    }

    @Override
    public void retryNoReturn(Consumer<?> retry) {
        Transaction transaction = null;
        try {
            transaction = this.session.beginTransaction();
            retry.accept(null);
            transaction.commit();
        } catch (Exception e) {
            if(transaction != null) {
                try {
                    transaction.rollback();
                } catch (Exception ignored) {
                }
            }
            if(e instanceof RuntimeException){
                throw e;
            }
            throw new RuntimeException(e);
        }
    }

    @Override
    public <R> R retryNonTransactional(Function<?, R> retry) {

        try {
            R result = retry.apply(null);
            return result;
        } catch (Exception e) {
            if(e instanceof RuntimeException){
                throw e;
            }
            throw new RuntimeException(e);
        }
    }

    public Session getSession(){
        return session;
    }

    @Override
    public void close() throws Exception {

        if(session.isUpdated()) {
            try {
                Transaction transaction = session.beginTransaction();
                String hql = "update CollectionSequence set lastModified= :date where projectId= :projectId";
                session.createQuery(hql).setParameter("date", DateTime.now(DateTimeZone.UTC)).setString("projectId", userContext.getProjectId()).executeUpdate();
                transaction.commit();
            } catch (Exception e) {

            }
        }

        session.close();
    }
}
