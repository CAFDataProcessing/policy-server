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
package com.github.cafdataprocessing.corepolicy.common.dto.conditions;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.cafdataprocessing.corepolicy.common.ApiStrings;
import com.github.cafdataprocessing.corepolicy.common.FilterName;
import com.github.cafdataprocessing.corepolicy.common.SortName;

/**
 * A condition that can be evaluated against a document
 */
//@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
@JsonIgnoreProperties(ApiStrings.Conditions.Arguments.DESC)
public abstract class Condition extends ConditionKey {
    @FilterName(ApiStrings.Conditions.Arguments.IS_FRAGMENT)
    @JsonProperty(ApiStrings.Conditions.Arguments.IS_FRAGMENT)
    public boolean isFragment;

    public Integer order;

    public ConditionTarget target = ConditionTarget.CONTAINER;


    @JsonProperty(ApiStrings.Conditions.Arguments.INCLUDE_DESCENDANTS)
    public Boolean includeDescendants = false;

    @FilterName(ApiStrings.Conditions.Arguments.NOTES)
    @SortName(ApiStrings.Conditions.Arguments.NOTES)
    public String notes;

    @JsonProperty(ApiStrings.Conditions.Arguments.PARENT_CONDITION_ID)
    public Long parentConditionId;
}
