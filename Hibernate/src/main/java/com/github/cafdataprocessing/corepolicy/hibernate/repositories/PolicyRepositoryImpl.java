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

import com.github.cafdataprocessing.corepolicy.common.UserContext;
import com.github.cafdataprocessing.corepolicy.common.dto.Policy;
import com.github.cafdataprocessing.corepolicy.repositories.v2.PolicyRepository;
import org.hibernate.criterion.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collection;

/**
 * Hibernate implementation of Policy repository for operations on policies using hibernate backend
 */
@Component
public class PolicyRepositoryImpl extends HibernateBaseRepositoryImpl<Policy> implements PolicyRepository {

    Class<Policy> typeParameterClass = Policy.class;
    private UserContext userContext;

    @Autowired
    public PolicyRepositoryImpl(UserContext userContext, ApplicationContext context ){ super(userContext, Policy.class, "Policy", context); }

    @Override
    protected Collection<Order> getSortFields(boolean ascending){

        final Collection<Order> ascSortFields = Arrays.asList(Order.asc("name").ignoreCase(), Order.asc("id"));
        final Collection<Order> descSortFields = Arrays.asList(Order.desc("name").ignoreCase(), Order.desc("id"));

        return ascending ? ascSortFields : descSortFields;
    }

    @Override
    protected void forceLazyInitializedPropsToNull( Collection<Policy> items ) {

        // nothing to do here, as it has no lazy initialized props!
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
