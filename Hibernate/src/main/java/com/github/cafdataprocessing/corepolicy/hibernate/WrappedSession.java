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
package com.github.cafdataprocessing.corepolicy.hibernate;

import org.hibernate.*;
import org.hibernate.jdbc.ReturningWork;
import org.hibernate.jdbc.Work;
import org.hibernate.procedure.ProcedureCall;
import org.hibernate.stat.SessionStatistics;

import java.io.Serializable;
import java.sql.Connection;

/**
 * Wrapper class to hold a Session and whether it has been updated
 */
public class WrappedSession implements Session {

    private Session session;
    private Boolean updated = false;

    public WrappedSession(Session session) {
        this.session = session;
    }

    @Override
    public SharedSessionBuilder sessionWithOptions() {
        return session.sessionWithOptions();
    }

    @Override
    public void flush() throws HibernateException {
        session.flush();
    }

    @Override
    public void setFlushMode(FlushMode flushMode) {
        session.setFlushMode(flushMode);
    }

    @Override
    public FlushMode getFlushMode() {
        return session.getFlushMode();
    }

    @Override
    public void setCacheMode(CacheMode cacheMode) {
        session.setCacheMode(cacheMode);
    }

    @Override
    public CacheMode getCacheMode() {
        return session.getCacheMode();
    }

    @Override
    public SessionFactory getSessionFactory() {
        return session.getSessionFactory();
    }

    @Override
    public Connection close() throws HibernateException {
        return session.close();
    }

    @Override
    public void cancelQuery() throws HibernateException {
        session.cancelQuery();
    }

    @Override
    public boolean isOpen() {
        return session.isOpen();
    }

    @Override
    public boolean isConnected() {
        return session.isConnected();
    }

    @Override
    public boolean isDirty() throws HibernateException {
        return session.isDirty();
    }

    @Override
    public boolean isDefaultReadOnly() {
        return session.isDefaultReadOnly();
    }

    @Override
    public void setDefaultReadOnly(boolean b) {
        session.setDefaultReadOnly(b);
    }

    @Override
    public Serializable getIdentifier(Object o) {
        return session.getIdentifier(o);
    }

    @Override
    public boolean contains(Object o) {
        return session.contains(o);
    }

    @Override
    public void evict(Object o) {
        session.evict(o);
    }

    @Override
    public Object load(Class aClass, Serializable serializable, LockMode lockMode) {
        return session.load(aClass, serializable, lockMode);
    }

    @Override
    public Object load(Class aClass, Serializable serializable, LockOptions lockOptions) {
        return session.load(aClass, serializable, lockOptions);
    }

    @Override
    public Object load(String s, Serializable serializable, LockMode lockMode) {
        return session.load(s, serializable, lockMode);
    }

    @Override
    public Object load(String s, Serializable serializable, LockOptions lockOptions) {
        return session.load(s, serializable, lockOptions);
    }

    @Override
    public Object load(Class aClass, Serializable serializable) {
        return session.load(aClass, serializable);
    }

    @Override
    public Object load(String s, Serializable serializable) {
        return session.load(s, serializable);
    }

    @Override
    public void load(Object o, Serializable serializable) {
        session.load(o, serializable);
    }

    @Override
    public void replicate(Object o, ReplicationMode replicationMode) {
        session.replicate(o, replicationMode);
    }

    @Override
    public void replicate(String s, Object o, ReplicationMode replicationMode) {
        session.replicate(s, o, replicationMode);
    }

    @Override
    public Serializable save(Object o) {
        updated = true;
        return session.save(o);
    }

    @Override
    public Serializable save(String s, Object o) {
        updated = true;
        return session.save(s, o);
    }

    @Override
    public void saveOrUpdate(Object o) {
        updated = true;
        session.saveOrUpdate(o);
    }

    @Override
    public void saveOrUpdate(String s, Object o) {
        updated = true;
        session.saveOrUpdate(s, o);
    }

    @Override
    public void update(Object o) {
        updated = true;
        session.update(o);
    }

    @Override
    public void update(String s, Object o) {
        updated = true;
        session.update(s, o);
    }

    @Override
    public Object merge(Object o) {
        return session.merge(o);
    }

    @Override
    public Object merge(String s, Object o) {
        return session.merge(s, o);
    }

    @Override
    public void persist(Object o) {
        session.persist(o);
    }

    @Override
    public void persist(String s, Object o) {
        session.persist(s, o);
    }

    @Override
    public void delete(Object o) {
        updated = true;
        session.delete(o);
    }

    @Override
    public void delete(String s, Object o) {
        updated = true;
        session.delete(s, o);
    }

    @Override
    public void lock(Object o, LockMode lockMode) {
        updated = true;
        session.lock(o, lockMode);
    }

    @Override
    public void lock(String s, Object o, LockMode lockMode) {
        updated = true;
        session.lock(s, o, lockMode);
    }

    @Override
    public LockRequest buildLockRequest(LockOptions lockOptions) {
        return session.buildLockRequest(lockOptions);
    }

    @Override
    public void refresh(Object o) {
        session.refresh(o);
    }

    @Override
    public void refresh(String s, Object o) {
        session.refresh(s, o);
    }

    @Override
    public void refresh(Object o, LockMode lockMode) {
        session.refresh(o, lockMode);
    }

    @Override
    public void refresh(Object o, LockOptions lockOptions) {
        session.refresh(o, lockOptions);
    }

    @Override
    public void refresh(String s, Object o, LockOptions lockOptions) {
        session.refresh(s, o, lockOptions);
    }

    @Override
    public LockMode getCurrentLockMode(Object o) {
        return session.getCurrentLockMode(o);
    }

    @Override
    public Query createFilter(Object o, String s) {
        return session.createFilter(o, s);
    }

    @Override
    public void clear() {
        session.clear();
    }

    @Override
    public Object get(Class aClass, Serializable serializable) {
        return session.get(aClass, serializable);
    }

    @Override
    public Object get(Class aClass, Serializable serializable, LockMode lockMode) {
        return session.get(aClass, serializable, lockMode);
    }

    @Override
    public Object get(Class aClass, Serializable serializable, LockOptions lockOptions) {
        return session.get(aClass, serializable, lockOptions);
    }

    @Override
    public Object get(String s, Serializable serializable) {
        return session.get(s, serializable);
    }

    @Override
    public Object get(String s, Serializable serializable, LockMode lockMode) {
        return session.get(s, serializable, lockMode);
    }

    @Override
    public Object get(String s, Serializable serializable, LockOptions lockOptions) {
        return session.get(s, serializable, lockOptions);
    }

    @Override
    public String getEntityName(Object o) {
        return session.getEntityName(o);
    }

    @Override
    public IdentifierLoadAccess byId(String s) {
        return session.byId(s);
    }

    @Override
    public IdentifierLoadAccess byId(Class aClass) {
        return session.byId(aClass);
    }

    @Override
    public NaturalIdLoadAccess byNaturalId(String s) {
        return session.byNaturalId(s);
    }

    @Override
    public NaturalIdLoadAccess byNaturalId(Class aClass) {
        return session.byNaturalId(aClass);
    }

    @Override
    public SimpleNaturalIdLoadAccess bySimpleNaturalId(String s) {
        return session.bySimpleNaturalId(s);
    }

    @Override
    public SimpleNaturalIdLoadAccess bySimpleNaturalId(Class aClass) {
        return session.bySimpleNaturalId(aClass);
    }

    @Override
    public Filter enableFilter(String s) {
        return session.enableFilter(s);
    }

    @Override
    public Filter getEnabledFilter(String s) {
        return session.getEnabledFilter(s);
    }

    @Override
    public void disableFilter(String s) {
        session.disableFilter(s);
    }

    @Override
    public SessionStatistics getStatistics() {
        return session.getStatistics();
    }

    @Override
    public boolean isReadOnly(Object o) {
        return session.isReadOnly(o);
    }

    @Override
    public void setReadOnly(Object o, boolean b) {
        session.setReadOnly(o, b);
    }

    @Override
    public void doWork(Work work) throws HibernateException {
        session.doWork(work);
    }

    @Override
    public <T> T doReturningWork(ReturningWork<T> returningWork) throws HibernateException {
        return session.doReturningWork(returningWork);
    }

    @Override
    public Connection disconnect() {
        return session.disconnect();
    }

    @Override
    public void reconnect(Connection connection) {
        session.reconnect(connection);
    }

    @Override
    public boolean isFetchProfileEnabled(String s) throws UnknownProfileException {
        return session.isFetchProfileEnabled(s);
    }

    @Override
    public void enableFetchProfile(String s) throws UnknownProfileException {
        session.enableFetchProfile(s);
    }

    @Override
    public void disableFetchProfile(String s) throws UnknownProfileException {
        session.disableFetchProfile(s);
    }

    @Override
    public TypeHelper getTypeHelper() {
        return session.getTypeHelper();
    }

    @Override
    public LobHelper getLobHelper() {
        return session.getLobHelper();
    }

    @Override
    public void addEventListeners(SessionEventListener... sessionEventListeners) {
        session.addEventListeners(sessionEventListeners);
    }

    @Override
    public String getTenantIdentifier() {
        return session.getTenantIdentifier();
    }

    @Override
    public Transaction beginTransaction() {
        return session.beginTransaction();
    }

    @Override
    public Transaction getTransaction() {
        return session.getTransaction();
    }

    @Override
    public Query getNamedQuery(String s) {
        return new WrappedQuery(session.getNamedQuery(s), this);
    }

    @Override
    public Query createQuery(String s) {
        return new WrappedQuery(session.createQuery(s), this);
    }

    @Override
    public SQLQuery createSQLQuery(String s) {
        return session.createSQLQuery(s);
    }

    @Override
    public ProcedureCall getNamedProcedureCall(String s) {
        return session.getNamedProcedureCall(s);
    }

    @Override
    public ProcedureCall createStoredProcedureCall(String s) {
        return session.createStoredProcedureCall(s);
    }

    @Override
    public ProcedureCall createStoredProcedureCall(String s, Class... classes) {
        return session.createStoredProcedureCall(s, classes);
    }

    @Override
    public ProcedureCall createStoredProcedureCall(String s, String... strings) {
        return session.createStoredProcedureCall(s, strings);
    }

    @Override
    public Criteria createCriteria(Class aClass) {
        return session.createCriteria(aClass);
    }

    @Override
    public Criteria createCriteria(Class aClass, String s) {
        return session.createCriteria(aClass, s);
    }

    @Override
    public Criteria createCriteria(String s) {
        return session.createCriteria(s);
    }

    @Override
    public Criteria createCriteria(String s, String s1) {
        return session.createCriteria(s, s1);
    }

    public Boolean isUpdated() {
        return updated;
    }
    public void wasUpdated() {
        updated = true;
    }
}
