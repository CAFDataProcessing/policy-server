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
package com.github.cafdataprocessing.corepolicy.common.dto;import com.fasterxml.jackson.annotation.JsonProperty;import com.fasterxml.jackson.databind.JsonNode;import com.fasterxml.jackson.databind.annotation.JsonDeserialize;import com.github.cafdataprocessing.corepolicy.common.ApiStrings;import com.github.cafdataprocessing.corepolicy.common.JsonNullAwareDeserializer;/** * */public class PolicyType extends DtoBase {    public String name;    public String description;    @JsonDeserialize(using = JsonNullAwareDeserializer.class)    public JsonNode definition;    @JsonProperty(ApiStrings.PolicyType.Arguments.POLICY_TYPE_INTERNAL_NAME)    public String shortName;    @JsonProperty(ApiStrings.PolicyType.Arguments.POLICY_CONFLICT_RESOLUTION_MODE)    public ConflictResolutionMode conflictResolutionMode;}