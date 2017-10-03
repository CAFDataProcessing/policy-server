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
package com.github.cafdataprocessing.corepolicy.booleanagent;

import com.github.cafdataprocessing.corepolicy.common.exceptions.CpeException;
import com.github.cafdataprocessing.corepolicy.common.shared.MetadataValue;
import com.github.cafdataprocessing.corepolicy.domainModels.BooleanAgentDocuments;

import java.util.Collection;

/**
 *
 */
public interface BooleanAgentServices {
    boolean getAvailable();
    BooleanAgentQueryResult query(String instanceId, Collection<MetadataValue> fieldValues) throws Exception;
    void create(String instanceId, BooleanAgentDocuments documents) throws CpeException;
    void delete(String instanceId);
    boolean existForInstanceId(String instanceId);
    void isValidExpression(String string);
    Collection<Term> doTermGetInfo(String text);

    boolean canConnect();
}
