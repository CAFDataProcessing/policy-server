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
package com.github.cafdataprocessing.corepolicy.common.dto.conditions;import com.fasterxml.jackson.annotation.JsonIgnoreProperties;import com.fasterxml.jackson.annotation.JsonProperty;import com.github.cafdataprocessing.corepolicy.common.ApiStrings;/** * */@JsonIgnoreProperties({ApiStrings.Conditions.Arguments.DESC,ApiStrings.Conditions.Arguments.CONDITION,ApiStrings.Conditions.Arguments.OPERATOR,ApiStrings.Conditions.Arguments.CHILDREN,ApiStrings.Conditions.Arguments.LANGUAGE,ApiStrings.Conditions.Arguments.FIELD})public class FragmentCondition extends Condition {    public FragmentCondition(){conditionType=ConditionType.FRAGMENT;}    @JsonProperty(ApiStrings.Conditions.Arguments.VALUE)    public Long value;}