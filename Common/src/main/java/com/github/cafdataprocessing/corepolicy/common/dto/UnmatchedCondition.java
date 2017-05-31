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
package com.github.cafdataprocessing.corepolicy.common.dto;import com.fasterxml.jackson.annotation.JsonProperty;import com.github.cafdataprocessing.corepolicy.common.ApiStrings;import com.github.cafdataprocessing.corepolicy.common.dto.conditions.Condition;import com.github.cafdataprocessing.corepolicy.common.dto.conditions.FieldCondition;import com.github.cafdataprocessing.corepolicy.common.dto.conditions.ConditionKey;/** * * This is here so we know exactly what conditions have been evaluated, and the results. * We already have the counterpart MatchedCondition and both together give a picture of all evaluated * conditions and the match results. */public class UnmatchedCondition extends ConditionKey {    private String reference;    @JsonProperty(ApiStrings.UnmatchedConditions.Arguments.FIELD_NAME)    private String fieldName;    public UnmatchedCondition() {}    public UnmatchedCondition(String reference, Condition condition){        this.reference = reference;        id = condition.id;        name = condition.name;        conditionType = condition.conditionType;        if (condition instanceof FieldCondition) {            fieldName = ((FieldCondition) condition).field;        }    }    /**     * The reference for the document that the condition was unmatched against.     * @return     */    public String getReference() {        return reference;    }    /**     * The reference for the document that the condition was unmatched against.     * @param reference     */    public void setReference(String reference) {        this.reference = reference;    }    public String getFieldName() {        return fieldName;    }    public void setFieldName(String fieldName) {        this.fieldName = fieldName;    }}