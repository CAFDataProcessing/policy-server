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
package com.github.cafdataprocessing.corepolicy.api;

import com.github.cafdataprocessing.corepolicy.testing.IntegrationTestBase;
import com.github.cafdataprocessing.corepolicy.common.PolicyApi;
import com.github.cafdataprocessing.corepolicy.repositories.RepositoryConnectionProvider;
import com.github.cafdataprocessing.corepolicy.repositories.RepositoryType;

import java.security.InvalidParameterException;
import java.sql.Connection;

/**
 *
 */
public class PolicyApiTestBase extends IntegrationTestBase {
    protected PolicyApi sut;

    public PolicyApiTestBase() {
        sut = genericApplicationContext.getBean(PolicyApi.class);
    }

    @Override
    protected Connection getConnectionToClearDown() {
        if(apiProperties.getMode().equalsIgnoreCase("direct")) {
            RepositoryConnectionProvider repositoryConnectionProvider = genericApplicationContext.getBean(RepositoryConnectionProvider.class);
            return repositoryConnectionProvider.getConnection(RepositoryType.POLICY);
        }
        throw new InvalidParameterException(apiProperties.getMode());
    }
}
