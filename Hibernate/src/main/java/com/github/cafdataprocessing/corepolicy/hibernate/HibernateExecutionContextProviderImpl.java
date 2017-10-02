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

import com.github.cafdataprocessing.corepolicy.common.UserContext;
import com.github.cafdataprocessing.corepolicy.repositories.ExecutionContextProvider;
import com.github.cafdataprocessing.corepolicy.repositories.v2.ExecutionContext;
import com.github.cafdataprocessing.corepolicy.repositories.RepositoryType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Context provider for Hibernate
 */
public class HibernateExecutionContextProviderImpl implements ExecutionContextProvider {

    private HibernateSessionFactory hibernateSessionFactory;
    private UserContext userContext;
    private static final Logger logger = LoggerFactory.getLogger(HibernateExecutionContextProviderImpl.class);

    @Autowired
    public HibernateExecutionContextProviderImpl(HibernateSessionFactory hibernateSessionFactory, UserContext userContext){
        this.hibernateSessionFactory = hibernateSessionFactory;
        this.userContext = userContext;
    }

    @Override
    public ExecutionContext getExecutionContext(RepositoryType repositoryType) {
        return new HibernateExecutionContextImpl(hibernateSessionFactory.getSession(), userContext);
    }

    /**
     * A forcible close on the execution context / connection pool.
     * Ensure all sessions are finished any outstanding work!
     */
    @Override
    public void closeExecutionContext() {

        logger.info("HibernateExecutionContextProviderImpl:closeExecutionContext");
        
        if (hibernateSessionFactory != null) {
            hibernateSessionFactory.closeSession();
            hibernateSessionFactory = null;
        }

        if (userContext != null) {
            userContext = null;
        }
    }
}
