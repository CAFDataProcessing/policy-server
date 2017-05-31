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
package com.github.cafdataprocessing.corepolicy.booleanagent;

import com.github.cafdataprocessing.corepolicy.common.exceptions.CpeException;
import com.github.cafdataprocessing.corepolicy.common.shared.MetadataValue;
import com.github.cafdataprocessing.corepolicy.domainModels.BooleanAgentDocuments;
import org.apache.commons.lang3.NotImplementedException;

import java.util.Collection;

/**
 *
 */
public class BooleanAgentServicesNotAvailableImpl implements BooleanAgentServices {
    @Override
    public boolean getAvailable() {
        return false;
    }

    @Override
    public BooleanAgentQueryResult query(String instanceId, Collection<MetadataValue> fieldValues) throws Exception {
        throw new NotImplementedException("query is not available");
    }

    @Override
    public void create(String instanceId, BooleanAgentDocuments documents) throws CpeException {
        throw new NotImplementedException("create is not available");
    }

    @Override
    public void delete(String instanceId) {
        throw new NotImplementedException("delete is not available");
    }

    @Override
    public boolean existForInstanceId(String instanceId) {
        throw new NotImplementedException("existsForInstanceId is not available");
    }

    @Override
    public void isValidExpression(String string) {
        throw new NotImplementedException("isValidExpression is not available");
    }

    @Override
    public Collection<Term> doTermGetInfo(String text) {
        throw new NotImplementedException("doTermGetInfo is not available");
    }

    @Override
    public boolean canConnect() {
        return false;
    }
}
