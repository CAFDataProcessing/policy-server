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

import org.hibernate.*;
import org.hibernate.transform.ResultTransformer;
import org.hibernate.type.Type;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

/**
 * Wrapper class to hold a query and a WrappedSession
 */
public class WrappedQuery implements Query {

    private Query query;
    private WrappedSession wrappedSession;

    public WrappedQuery(Query query, WrappedSession wrappedSession){
        this.query = query;
        this.wrappedSession = wrappedSession;
    }

    @Override
    public String getQueryString() {
        return query.getQueryString();
    }

    @Override
    public Integer getMaxResults() {
        return query.getMaxResults();
    }

    @Override
    public Query setMaxResults(int i) {
        return query.setMaxResults(i);
    }

    @Override
    public Integer getFirstResult() {
        return query.getFirstResult();
    }

    @Override
    public Query setFirstResult(int i) {
        return query.setFirstResult(i);
    }

    @Override
    public FlushMode getFlushMode() {
        return query.getFlushMode();
    }

    @Override
    public Query setFlushMode(FlushMode flushMode) {
        return query.setFlushMode(flushMode);
    }

    @Override
    public CacheMode getCacheMode() {
        return query.getCacheMode();
    }

    @Override
    public Query setCacheMode(CacheMode cacheMode) {
        return query.setCacheMode(cacheMode);
    }

    @Override
    public boolean isCacheable() {
        return query.isCacheable();
    }

    @Override
    public Query setCacheable(boolean b) {
        return query.setCacheable(b);
    }

    @Override
    public String getCacheRegion() {
        return query.getCacheRegion();
    }

    @Override
    public Query setCacheRegion(String s) {
        return query.setCacheRegion(s);
    }

    @Override
    public Integer getTimeout() {
        return query.getTimeout();
    }

    @Override
    public Query setTimeout(int i) {
        return query.setTimeout(i);
    }

    @Override
    public Integer getFetchSize() {
        return query.getFetchSize();
    }

    @Override
    public Query setFetchSize(int i) {
        return query.setFetchSize(i);
    }

    @Override
    public boolean isReadOnly() {
        return query.isReadOnly();
    }

    @Override
    public Query setReadOnly(boolean b) {
        return query.setReadOnly(b);
    }

    @Override
    public Type[] getReturnTypes() {
        return query.getReturnTypes();
    }

    @Override
    public LockOptions getLockOptions() {
        return query.getLockOptions();
    }

    @Override
    public Query setLockOptions(LockOptions lockOptions) {
        return query.setLockOptions(lockOptions);
    }

    @Override
    public Query setLockMode(String s, LockMode lockMode) {
        return query.setLockMode(s, lockMode);
    }

    @Override
    public String getComment() {
        return query.getComment();
    }

    @Override
    public Query setComment(String s) {
        return query.setComment(s);
    }

    @Override
    public Query addQueryHint(String s) {
        return query.addQueryHint(s);
    }

    @Override
    public String[] getReturnAliases() {
        return query.getReturnAliases();
    }

    @Override
    public String[] getNamedParameters() {
        return query.getNamedParameters();
    }

    @Override
    public Iterator iterate() {
        return query.iterate();
    }

    @Override
    public ScrollableResults scroll() {
        return query.scroll();
    }

    @Override
    public ScrollableResults scroll(ScrollMode scrollMode) {
        return query.scroll(scrollMode);
    }

    @Override
    public List list() {
        return query.list();
    }

    @Override
    public Object uniqueResult() {
        return query.uniqueResult();
    }

    @Override
    public int executeUpdate() {
        wrappedSession.wasUpdated();
        return query.executeUpdate();
    }

    @Override
    public Query setParameter(int i, Object o, Type type) {
        return query.setParameter(i, o, type);
    }

    @Override
    public Query setParameter(String s, Object o, Type type) {
        return query.setParameter(s, o, type);
    }

    @Override
    public Query setParameter(int i, Object o) {
        return query.setParameter(i, o);
    }

    @Override
    public Query setParameter(String s, Object o) {
        return query.setParameter(s,o);
    }

    @Override
    public Query setParameters(Object[] objects, Type[] types) {
        return query.setParameters(objects, types);
    }

    @Override
    public Query setParameterList(String s, Collection collection, Type type) {
        return query.setParameterList(s, collection, type);
    }

    @Override
    public Query setParameterList(String s, Collection collection) {
        return query.setParameterList(s, collection);
    }

    @Override
    public Query setParameterList(String s, Object[] objects, Type type) {
        return query.setParameterList(s, objects, type);
    }

    @Override
    public Query setParameterList(String s, Object[] objects) {
        return query.setParameterList(s, objects);
    }

    @Override
    public Query setProperties(Object o) {
        return query.setProperties(o);
    }

    @Override
    public Query setProperties(Map map) {
        return query.setProperties(map);
    }

    @Override
    public Query setString(int i, String s) {
        return query.setString(i, s);
    }

    @Override
    public Query setCharacter(int i, char c) {
        return query.setCharacter(i, c);
    }

    @Override
    public Query setBoolean(int i, boolean b) {
        return query.setBoolean(i, b);
    }

    @Override
    public Query setByte(int i, byte b) {
        return query.setByte(i, b);
    }

    @Override
    public Query setShort(int i, short i1) {
        return query.setShort(i, i1);
    }

    @Override
    public Query setInteger(int i, int i1) {
        return query.setInteger(i, i1);
    }

    @Override
    public Query setLong(int i, long l) {
        return query.setLong(i, l);
    }

    @Override
    public Query setFloat(int i, float v) {
        return query.setFloat(i, v);
    }

    @Override
    public Query setDouble(int i, double v) {
        return query.setDouble(i, v);
    }

    @Override
    public Query setBinary(int i, byte[] bytes) {
        return query.setBinary(i, bytes);
    }

    @Override
    public Query setText(int i, String s) {
        return query.setText(i, s);
    }

    @Override
    public Query setSerializable(int i, Serializable serializable) {
        return query.setSerializable(i, serializable);
    }

    @Override
    public Query setLocale(int i, Locale locale) {
        return query.setLocale(i, locale);
    }

    @Override
    public Query setBigDecimal(int i, BigDecimal bigDecimal) {
        return query.setBigDecimal(i, bigDecimal);
    }

    @Override
    public Query setBigInteger(int i, BigInteger bigInteger) {
        return query.setBigInteger(i, bigInteger);
    }

    @Override
    public Query setDate(int i, Date date) {
        return query.setDate(i, date);
    }

    @Override
    public Query setTime(int i, Date date) {
        return query.setTime(i, date);
    }

    @Override
    public Query setTimestamp(int i, Date date) {
        return query.setTimestamp(i, date);
    }

    @Override
    public Query setCalendar(int i, Calendar calendar) {
        return query.setCalendar(i, calendar);
    }

    @Override
    public Query setCalendarDate(int i, Calendar calendar) {
        return query.setCalendarDate(i, calendar);
    }

    @Override
    public Query setString(String s, String s1) {
        return query.setString(s, s1);
    }

    @Override
    public Query setCharacter(String s, char c) {
        return query.setCharacter(s, c);
    }

    @Override
    public Query setBoolean(String s, boolean b) {
        return query.setBoolean(s, b);
    }

    @Override
    public Query setByte(String s, byte b) {
        return query.setByte(s, b);
    }

    @Override
    public Query setShort(String s, short i) {
        return query.setShort(s, i);
    }

    @Override
    public Query setInteger(String s, int i) {
        return query.setInteger(s, i);
    }

    @Override
    public Query setLong(String s, long l) {
        return query.setLong(s, l);
    }

    @Override
    public Query setFloat(String s, float v) {
        return query.setFloat(s, v);
    }

    @Override
    public Query setDouble(String s, double v) {
        return query.setDouble(s, v);
    }

    @Override
    public Query setBinary(String s, byte[] bytes) {
        return query.setBinary(s, bytes);
    }

    @Override
    public Query setText(String s, String s1) {
        return query.setText(s, s1);
    }

    @Override
    public Query setSerializable(String s, Serializable serializable) {
        return query.setSerializable(s, serializable);
    }

    @Override
    public Query setLocale(String s, Locale locale) {
        return query.setLocale(s, locale);
    }

    @Override
    public Query setBigDecimal(String s, BigDecimal bigDecimal) {
        return query.setBigDecimal(s, bigDecimal);
    }

    @Override
    public Query setBigInteger(String s, BigInteger bigInteger) {
        return query.setBigInteger(s, bigInteger);
    }

    @Override
    public Query setDate(String s, Date date) {
        return query.setDate(s, date);
    }

    @Override
    public Query setTime(String s, Date date) {
        return query.setTime(s, date);
    }

    @Override
    public Query setTimestamp(String s, Date date) {
        return query.setTimestamp(s, date);
    }

    @Override
    public Query setCalendar(String s, Calendar calendar) {
        return query.setCalendar(s, calendar);
    }

    @Override
    public Query setCalendarDate(String s, Calendar calendar) {
        return query.setCalendarDate(s, calendar);
    }

    @Override
    public Query setEntity(int i, Object o) {
        return query.setEntity(i, o);
    }

    @Override
    public Query setEntity(String s, Object o) {
        return query.setEntity(s, o);
    }

    @Override
    public Query setResultTransformer(ResultTransformer resultTransformer) {
        return query.setResultTransformer(resultTransformer);
    }
}
