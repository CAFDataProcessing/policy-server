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

import com.google.common.collect.Multimap;
import com.github.cafdataprocessing.corepolicy.common.dto.LexiconExpressionId;

/**
 * Interface defining the required methods for a BooleanAgentQueryResult implementation.
 */
public interface BooleanAgentQueryResult {
    Multimap<Long, String> getConditionIdTerms();

    void setConditionIdTerms(Multimap<Long, String> conditionIdTerms);

    Multimap<LexiconExpressionId, String> getLexiconExpressionIdTerms();

    void setLexiconExpressionIdTerms(Multimap<LexiconExpressionId, String> lexiconExpressionIdTerms);
}
