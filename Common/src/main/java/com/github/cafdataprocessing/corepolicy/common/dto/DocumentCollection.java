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
package com.github.cafdataprocessing.corepolicy.common.dto;import com.fasterxml.jackson.annotation.JsonProperty;import com.fasterxml.jackson.databind.annotation.JsonDeserialize;import com.github.cafdataprocessing.corepolicy.common.ApiStrings;import com.github.cafdataprocessing.corepolicy.common.DtoDeserializer;import com.github.cafdataprocessing.corepolicy.common.FilterName;import com.github.cafdataprocessing.corepolicy.common.dto.conditions.Condition;import java.util.Set;/** * Holds the details of a document collection */public class DocumentCollection extends DtoBase {    public String name;    public String description;    @JsonDeserialize(using = DtoDeserializer.class)    public Condition condition;    @FilterName(ApiStrings.DocumentCollections.Arguments.POLICY_IDS)    @JsonProperty(ApiStrings.DocumentCollections.Arguments.POLICY_IDS)    public Set<Long> policyIds;    public String fingerprint;}