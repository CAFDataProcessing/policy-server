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
package com.github.cafdataprocessing.corepolicy.common.dto;import com.fasterxml.jackson.annotation.JsonCreator;import com.fasterxml.jackson.annotation.JsonValue;import com.github.cafdataprocessing.corepolicy.common.dto.conditions.Condition;import com.github.cafdataprocessing.corepolicy.common.dto.conditions.ConditionKey;import com.github.cafdataprocessing.corepolicy.common.dto.conditions.FieldCondition;/** * */public class UnevaluatedCondition extends ConditionKey {    public Reason reason;    public String field;    @JsonCreator    UnevaluatedCondition(){}    public UnevaluatedCondition(Condition condition, Reason reason){        this.reason = reason;        this.id = condition.id;        this.conditionType = condition.conditionType;        this.name = condition.name;        if(condition instanceof FieldCondition) {            this.field = ((FieldCondition) condition).field;        }    }    public static enum Reason {        MISSING_FIELD,        MISSING_SERVICE;        @JsonCreator        public static Reason forValue(String value) {            return Reason.valueOf(value.toUpperCase());        }        @JsonValue        public String toValue() {            return this.name().toLowerCase();        }    }}