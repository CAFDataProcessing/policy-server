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

import com.github.cafdataprocessing.corepolicy.repositories.v2.ExecutionContext;
import com.github.cafdataprocessing.corepolicy.repositories.v2.PolicyTypeRepository;
import com.google.common.base.Strings;
import com.github.cafdataprocessing.corepolicy.common.UserContext;
import com.github.cafdataprocessing.corepolicy.common.dto.PolicyType;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collection;

/**
 *
 */
@Component
public class PolicyTypeRepositoryImpl extends HibernateBaseRepositoryImpl<PolicyType> implements PolicyTypeRepository {



    @Autowired
    public PolicyTypeRepositoryImpl(UserContext userContext, ApplicationContext context ) {
        super(userContext, PolicyType.class, "PolicyType", context);
    }

    @Override
    protected Collection<Order> getSortFields(boolean ascending){

        final Collection<Order> ascSortFields = Arrays.asList(Order.asc("name").ignoreCase(), Order.asc("id"));
        final Collection<Order> descSortFields = Arrays.asList(Order.desc("name").ignoreCase(), Order.desc("id"));

        return ascending ? ascSortFields : descSortFields;
    }

    @Override
    public PolicyType retrieve(ExecutionContext executionContext, String short_name) {
        Session session = getSession(executionContext);
        Criteria criteria = session.createCriteria(typeParameterClass)
                .add(Restrictions.eq("shortName", short_name).ignoreCase())
                .add(Restrictions.or(
                                Restrictions.eq("projectId", userContext.getProjectId()),
                                Restrictions.isNull("projectId"))
                );

        Collection<PolicyType> items = criteria.list();

        // ensure any items, are disassociated from the DB. so any changes to these objects aren't written
        // back.  We only support updates via update/create calls.
        evict( session, items );

        forceLazyInitializedPropsToNull( items );

        switch( items.size() ) {
            case 0: {
                return null;
            }
            case 1: {
                return items.stream().findFirst().get();
            }
            case 2: {
                // Now if we have 2 items we need to decide which are going to return
                // we give user precedence over base data.
                return items.stream().filter(u->!Strings.isNullOrEmpty( u.getProjectId())).findFirst().get();
            }
            default:
                throw new RuntimeException("Unexpected result - obtained non-unique results for policy type internal name.");
        }

    }

    @Override
    protected void forceLazyInitializedPropsToNull( Collection<PolicyType> items ) {

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
;