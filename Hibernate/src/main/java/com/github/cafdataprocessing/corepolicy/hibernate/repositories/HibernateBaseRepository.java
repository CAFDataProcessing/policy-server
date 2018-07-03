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

import com.github.cafdataprocessing.corepolicy.hibernate.HibernateExecutionContextImpl;
import com.github.cafdataprocessing.corepolicy.repositories.v2.ExecutionContext;
import com.google.common.base.Strings;
import com.github.cafdataprocessing.corepolicy.common.AdminUserContext;
import com.github.cafdataprocessing.corepolicy.common.ApiProperties;
import com.github.cafdataprocessing.corepolicy.common.UserContext;
import com.github.cafdataprocessing.corepolicy.common.dto.Filter;
import com.github.cafdataprocessing.corepolicy.common.dto.PageOfResults;
import com.github.cafdataprocessing.corepolicy.common.dto.PageRequest;
import com.github.cafdataprocessing.corepolicy.common.dto.Sort;
import com.github.cafdataprocessing.corepolicy.common.exceptions.InvalidFieldValueCpeException;
import com.github.cafdataprocessing.corepolicy.common.exceptions.excpetionErrors.InvalidFieldValueErrors;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.context.ApplicationContext;

import java.util.Collection;
import java.util.Collections;

/**
 * Base class to build hibernate repository implementations on
 */
public abstract class HibernateBaseRepository<T> extends HibernateBaseRepositoryUtils {

    private ApiProperties apiProperties;

    HibernateBaseRepository(UserContext userContext, ApplicationContext applicationContext){
        super(userContext, applicationContext);

        apiProperties = applicationContext.getBean(ApiProperties.class);
    }

    protected Session getSession(ExecutionContext executionContext){
        return getSession( executionContext, false );
    }

    protected Session getSession(ExecutionContext executionContext, boolean allowAdministration){
        // ensure we have our thread scoped admin context correctly setup.
        createAdminContext(allowAdministration);
        //Assuming this is always a HibernateExecutionContextImpl which I dont really like...
        return ((HibernateExecutionContextImpl)executionContext).getSession();
    }

    private void createAdminContext(boolean allowAdministration)
    {
        validateAllowAdministration(allowAdministration);
        AdminUserContext adminUserContext = applicationContext.getBean(AdminUserContext.class);
        adminUserContext.setAllowAdministration(allowAdministration);
    }

    private void validateAllowAdministration(boolean allowAdministration){

        if ( allowAdministration && !apiProperties.getAdminBaseDataEnabled() )
        {
            throw new RuntimeException("Administration Api is currently disabled.");
        }
    }

    public T create(ExecutionContext executionContext, T t) {
        return create(executionContext, t, false);
    }

    public T create(ExecutionContext executionContext, T t, boolean allowAdministration) {
        try {
            return create(getSession(executionContext, allowAdministration), t);
        }
        catch (Exception exception) {
            throw handleCreateException(exception);
        }
    }

    public T update(ExecutionContext executionContext, T t) {
        return update(executionContext, t, false);
    }

    public T update(ExecutionContext executionContext, T t, boolean allowAdministration) {
        try {
            return update(getSession(executionContext, allowAdministration), t);
        }
        catch (Exception exception) {
            throw handleUpdateException(exception);
        }
    }

    public void delete(ExecutionContext executionContext, Long id) {
       delete(executionContext, id, false);
    }

    public void delete(ExecutionContext executionContext, Long id, boolean allowAdministration) {
        try {
            delete(getSession(executionContext, allowAdministration), id);
        } catch (ConstraintViolationException e) {
            throw new InvalidFieldValueCpeException(InvalidFieldValueErrors.CANNOT_DELETE_HAS_DEPENDANT_ITEMS, e);
        }
    }

    protected abstract RuntimeException handleCreateException(Exception exception);
    protected abstract RuntimeException handleUpdateException(Exception exception);

    public PageOfResults<T> retrievePage(ExecutionContext executionContext, PageRequest pageRequest) {
        validatePageRequest(pageRequest);
        return retrievePage(getSession(executionContext), pageRequest);
    }

    public PageOfResults<T> retrievePage(ExecutionContext executionContext, PageRequest pageRequest, Filter filter ) {
        validatePageRequest(pageRequest);
        return retrievePage(getSession(executionContext), pageRequest, filter);
    }

    public PageOfResults<T> retrievePage(ExecutionContext executionContext, PageRequest pageRequest, Filter filter, Sort sort){
        validatePageRequest(pageRequest);
        return retrievePage(getSession(executionContext),pageRequest,filter,sort);
    }

    public PageOfResults<T> retrievePage(ExecutionContext executionContext, PageRequest pageRequest, Sort sort){
        validatePageRequest(pageRequest);
        return retrievePage(getSession(executionContext),pageRequest,sort);
    }

    public Collection<T> retrieve(ExecutionContext executionContext, Collection<Long> ids) {
        return retrieve(getSession(executionContext), ids);
    }

    protected void evict(Session session,T item){
        if(item !=null) {
            session.evict(item);
        }
    }

    protected void evict(Session session, Collection<T> items){
        for (T item: items) {
            evict(session, item);
        }
    }

    protected <R extends T> R preSave(T item, Session session){

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

//    public T retrieve(ExecutionContext executionContext, Long id) {
//        return retrieve(getSession(executionContext), id);
//    }

    protected T retrieveSingleItem(Session session, Long id) {
        Collection<T> items = retrieve(session, Collections.singletonList(id));
        if (items.size() > 1) {
            throw new RuntimeException("To many items returned");
        }
        if (items.size() == 0) {
            return null;
        }
        return items.stream().findFirst().get();
    }

    protected abstract void forceLazyInitializedPropsToNull( Collection<T> items );

    protected abstract <T1 extends T> T1 create(Session session, T1 t);
    protected abstract <T1 extends T> T1 update(Session session, T1 t);
    protected abstract void delete(Session session, Long id);
    protected abstract PageOfResults<T> retrievePage(Session session, PageRequest pageRequest);
    protected abstract PageOfResults<T> retrievePage(Session session, PageRequest pageRequest, Filter filter);
    protected abstract PageOfResults<T> retrievePage(Session session, PageRequest pageRequest, Filter filter, Sort sort);
    protected abstract PageOfResults<T> retrievePage(Session session, PageRequest pageRequest, Sort sort);
    protected abstract Collection<T>  retrieve(Session session, Collection<Long> ids);

    protected abstract Collection<Order> getSortFields(boolean ascending);
    protected abstract Collection<Order> getSortFields(Criteria criteria,Sort sort, Class<?> requestClassType);

}
