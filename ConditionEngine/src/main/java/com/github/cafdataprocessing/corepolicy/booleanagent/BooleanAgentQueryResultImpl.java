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
package com.github.cafdataprocessing.corepolicy.booleanagent;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.github.cafdataprocessing.corepolicy.common.dto.LexiconExpressionId;

/**
 *
 */
public class BooleanAgentQueryResultImpl implements BooleanAgentQueryResult {
    /**
     * For each condition id store the terms for that condition id
     */
    private Multimap<Long, String> conditionIdTerms = ArrayListMultimap.create();

    /**
     * For each lexicon expression id store the terms for that lexicon expression id
     */
    private Multimap<LexiconExpressionId, String> lexiconExpressionIdTerms = ArrayListMultimap.create();

    @Override
    public Multimap<Long, String> getConditionIdTerms() {
        return conditionIdTerms;
    }

    @Override
    public void setConditionIdTerms(Multimap<Long, String> conditionIdTerms) {
        this.conditionIdTerms = conditionIdTerms;
    }

    @Override
    public Multimap<LexiconExpressionId, String> getLexiconExpressionIdTerms() {
        return lexiconExpressionIdTerms;
    }

    @Override
    public void setLexiconExpressionIdTerms(Multimap<LexiconExpressionId, String> lexiconExpressionIdTerms) {
        this.lexiconExpressionIdTerms = lexiconExpressionIdTerms;
    }
}
