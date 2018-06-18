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
import com.github.cafdataprocessing.corepolicy.common.ApiStrings;
import com.github.cafdataprocessing.corepolicy.common.dto.conditions.Condition;
import com.github.cafdataprocessing.corepolicy.common.dto.conditions.FieldCondition;
import com.github.cafdataprocessing.corepolicy.common.dto.conditions.ConditionType;

import java.util.ArrayList;
import java.util.Collection;

/**
 * A condition that matched against a document.
 * This is a public Api class, please do not add, extend from
 * other classes which affect how this appears to the consumer.
 */
public class MatchedCondition {
    @JsonProperty(ApiStrings.BaseCrud.Arguments.ID)
    public Long id;
    public String name;
    public ConditionType type;

    private String reference;
    private Collection<String> terms;

    @JsonProperty("matched_lexicon_expressions")
    private Collection<MatchedLexiconExpression> matchedLexiconExpressions;

    @JsonProperty("field_name")
    private String fieldName;


    // default constructor for use when serialisation from json directly.
    public MatchedCondition(){}

    public MatchedCondition(String reference, Condition condition){
        this.reference = reference;
        id = condition.id;
        name = condition.name;
        type = condition.conditionType;
        terms = new ArrayList<>();
        matchedLexiconExpressions = new ArrayList<>();
        if(condition instanceof FieldCondition){
            fieldName = ((FieldCondition)condition).field;
        }
    }

    /**
     * The terms that matched for a ContentExpressionCondition.
     * @return Collection of the matching terms
     */
    public Collection<String> getTerms() {
        return terms;
    }

    /**
     * The terms that matched for a ContentExpressionCondition.
     * @param terms
     */
    public void setTerms(Collection<String> terms) {
        this.terms = terms;
    }

    /**
     * The matched lexicon expressions.
     * @return Collection of matching lexicon expressions
     */
    public Collection<MatchedLexiconExpression> getMatchedLexiconExpressions() {
        return matchedLexiconExpressions;
    }

    /**
     * The matched lexicon expressions.
     * @param matchedLexiconExpressions
     */
    public void setMatchedLexiconExpressions(Collection<MatchedLexiconExpression> matchedLexiconExpressions) {
        this.matchedLexiconExpressions = matchedLexiconExpressions;
    }

    /**
     * The reference for the document that the condition matched against.
     * @return
     */
    public String getReference() {
        return reference;
    }

    /**
     * The reference for the document that the condition matched against.
     * @param reference
     */
    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }
}
