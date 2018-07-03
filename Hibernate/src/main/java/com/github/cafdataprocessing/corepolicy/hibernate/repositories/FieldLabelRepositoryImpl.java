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
import com.github.cafdataprocessing.corepolicy.common.dto.FieldLabel;
import com.github.cafdataprocessing.corepolicy.common.exceptions.InvalidFieldValueCpeException;
import com.github.cafdataprocessing.corepolicy.common.exceptions.excpetionErrors.InvalidFieldValueErrors;
import com.github.cafdataprocessing.corepolicy.repositories.v2.ExecutionContext;
import com.github.cafdataprocessing.corepolicy.repositories.v2.FieldLabelRepository;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collection;

/**
 * Hibernate implementation for FieldLabel operations
 */
@Component
public class FieldLabelRepositoryImpl extends HibernateBaseRepositoryImpl<FieldLabel> implements FieldLabelRepository {

    @Autowired
    public FieldLabelRepositoryImpl(UserContext userContext, ApplicationContext context ){
        super(userContext, FieldLabel.class, "FieldLabel", context);
    }

    @Override
    protected RuntimeException handleCreateException(Exception exception) {
        if(exception instanceof ConstraintViolationException) {
            return new InvalidFieldValueCpeException(InvalidFieldValueErrors.FIELD_LABEL_NAME_MUST_BE_UNIQUE, exception);
        }
        return new RuntimeException(exception);
    }

    @Override
    protected RuntimeException handleUpdateException(Exception exception) {
        if(exception instanceof ConstraintViolationException) {
            return new InvalidFieldValueCpeException(InvalidFieldValueErrors.FIELD_LABEL_NAME_MUST_BE_UNIQUE, exception);
        }
        return new RuntimeException(exception);
    }

    @Override
    public FieldLabel retrieve(ExecutionContext executionContext, String labelName) {
        Session session = getSession(executionContext);

        Criteria criteria = session.createCriteria(typeParameterClass)
                .add(Restrictions.eq("name", labelName).ignoreCase())
                .add(Restrictions.or(
                                Restrictions.eq("projectId", userContext.getProjectId()),
                                Restrictions.isNull("projectId"))
                );

        FieldLabel item = (FieldLabel)criteria.uniqueResult();
        evict(session, item);

        // Ensure we set lazy initialized fields to null, to prevent a read trying to access DB outside this scope
        // i.e. when sessionproxy is gone as it will fail.
        forceLazyInitializedPropsToNull(Arrays.asList(item));

        return item;
    }

    @Override
    protected void forceLazyInitializedPropsToNull( Collection<FieldLabel> items ) {

        // nothing to do here, as it has no lazy initialized props!
        return;
    }

    @Override
    protected Collection<Order> getSortFields(boolean ascending){

        final Collection<Order> ascSortFields = Arrays.asList(Order.asc("name").ignoreCase(), Order.asc("id"));
        final Collection<Order> descSortFields = Arrays.asList(Order.desc("name").ignoreCase(), Order.desc("id"));

        return ascending ? ascSortFields : descSortFields;
    }
}
