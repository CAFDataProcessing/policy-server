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
import com.github.cafdataprocessing.corepolicy.common.dto.CollectionSequence;
import com.github.cafdataprocessing.corepolicy.common.exceptions.InvalidFieldValueCpeException;
import com.github.cafdataprocessing.corepolicy.common.exceptions.excpetionErrors.InvalidFieldValueErrors;
import com.github.cafdataprocessing.corepolicy.repositories.v2.CollectionSequenceRepository;
import com.github.cafdataprocessing.corepolicy.repositories.v2.ExecutionContext;
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
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 */
@Component
public class CollectionSequenceRepositoryImpl extends HibernateBaseRepositoryImpl<CollectionSequence> implements CollectionSequenceRepository {

    @Autowired
    public CollectionSequenceRepositoryImpl(UserContext userContext, ApplicationContext context ) {
        super(userContext, CollectionSequence.class, "CollectionSequence", context);
    }

    @Override
    public Collection<CollectionSequence> retrieve(ExecutionContext executionContext, String name) {
        Session session = getSession(executionContext);

        // we can use the base criteria, just add on the name restriction.
        Criteria criteria = createThisObjectsBaseCriteria(session);
        criteria.add(Restrictions.eq("name", name).ignoreCase());

        List<CollectionSequence> items = criteria.list();
        evict(session, items);
        forceLazyInitializedPropsToNull(items);
        return items;
    }

    @Override
    protected void forceLazyInitializedPropsToNull( Collection<CollectionSequence> items ) {

        // nothing to do here, as it has no lazy initialized props!
        return;
    }

    @Override
    protected Collection<Order> getSortFields(boolean ascending){

        final Collection<Order> ascSortFields = Arrays.asList(Order.asc("name").ignoreCase(), Order.asc("id"));
        final Collection<Order> descSortFields = Arrays.asList(Order.desc("name").ignoreCase(), Order.desc("id"));

        return ascending ? ascSortFields : descSortFields;
    }

    private RuntimeException handleCreateUpdateException(Exception exception) {
        if(exception instanceof ConstraintViolationException)
        {
            ConstraintViolationException constraintViolationException = (ConstraintViolationException) exception;
            String constraintName = getConstrainName(constraintViolationException);

            if(constraintName!=null) {
                InvalidFieldValueErrors errors = null;
                if (constraintName.toLowerCase().startsWith("fk_collection_sequence_excluded_fragment_id")) {
                    errors = InvalidFieldValueErrors.NO_MATCHING_FRAGMENT_CONDITION;
                } else if (constraintName.toLowerCase().startsWith("fk_collection_sequence_default_collection_id")) {
                    errors = InvalidFieldValueErrors.NO_MATCHING_DEFAULT_COLLECTION;
                }

                if (errors != null) {
                    return new InvalidFieldValueCpeException(errors, exception);
                }
            }
        }

        return new RuntimeException(exception);
    }

    private String getConstrainName(ConstraintViolationException constraintValidationException) {
        String constraintName = constraintValidationException.getConstraintName();

        if(constraintName == null) {
            //For my sql we need to pull the name out manually
            Throwable cause = constraintValidationException.getCause();
            String message = cause.getMessage();
            Pattern pattern = Pattern.compile(".*CONSTRAINT `([^`]*)`.*");
            Matcher matcher = pattern.matcher(message);
            if(matcher.matches()) {
                constraintName = matcher.toMatchResult().group(1);
            }
        } else {
            constraintName = constraintName.replace("\"", "");
        }

        return constraintName;
    }

    @Override
    protected RuntimeException handleCreateException(Exception exception) {
        return handleCreateUpdateException(exception);
    }

    @Override
    protected RuntimeException handleUpdateException(Exception exception) {
        return handleCreateUpdateException(exception);
    }
}
