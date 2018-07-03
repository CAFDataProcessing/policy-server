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
package com.github.cafdataprocessing.corepolicy.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Holds the details of a matching lexicon expression.
 */
public class MatchedLexiconExpression {
    @JsonProperty("lexicon_expression_id")
    private Long lexiconExpressionId;
    private Collection<String> terms;

    public MatchedLexiconExpression() {
        this.terms = new LinkedList<>();
    }

    /**
     * The id of the lexicon expression that matched.
     * @return the id
     */
    public Long getLexiconExpressionId() {
        return lexiconExpressionId;
    }

    /**
     * The id of the lexicon expression that matched.
     * @param lexiconExpressionId
     */
    public void setLexiconExpressionId(Long lexiconExpressionId) {
        this.lexiconExpressionId = lexiconExpressionId;
    }

    /**
     * A list of matching terms for the lexicon expression match.
     * @return Collection of the matching terms
     */
    public Collection<String> getTerms() {
        return terms;
    }

    /**
     * A list of matching terms for the lexicon expression match.
     * @param terms
     */
    public void setTerms(Collection<String> terms) {
        this.terms = terms;
    }
}
