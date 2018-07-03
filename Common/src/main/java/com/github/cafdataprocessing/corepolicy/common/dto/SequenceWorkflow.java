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
import com.github.cafdataprocessing.corepolicy.common.FilterName;
import com.github.cafdataprocessing.corepolicy.common.SortName;

import java.util.LinkedList;
import java.util.List;

/**
 * Defines a Workflow which is made up of entries for sequences that comprise the workflow.
 */
public class SequenceWorkflow extends DtoBase {

    @FilterName(ApiStrings.SequenceWorkflow.Arguments.NAME)
    @JsonProperty(ApiStrings.SequenceWorkflow.Arguments.NAME)
    public String name;

    @JsonProperty(ApiStrings.SequenceWorkflow.Arguments.DESCRIPTION)
    public String description;

    @FilterName(ApiStrings.SequenceWorkflow.Arguments.SEQUENCE_ENTRIES)
    @SortName(ApiStrings.SequenceWorkflow.Arguments.SEQUENCE_ENTRIES)
    @JsonProperty(ApiStrings.SequenceWorkflow.Arguments.SEQUENCE_ENTRIES)
    public List<SequenceWorkflowEntry> sequenceWorkflowEntries = new LinkedList<>();

    @JsonProperty(ApiStrings.SequenceWorkflow.Arguments.NOTES)
    public String notes;
}
