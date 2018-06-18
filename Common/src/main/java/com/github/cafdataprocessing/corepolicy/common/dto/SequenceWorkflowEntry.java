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

/**
 * Defines a single entry on a workflow
 */
public class SequenceWorkflowEntry extends DtoBase{

    @FilterName(ApiStrings.SequenceWorkflowEntry.Arguments.SEQUENCE_ID)
    @JsonProperty(ApiStrings.SequenceWorkflowEntry.Arguments.SEQUENCE_ID)
    public Long collectionSequenceId;

    @JsonProperty(ApiStrings.SequenceWorkflowEntry.Arguments.ORDER)
    @SortName(ApiStrings.SequenceWorkflowEntry.Arguments.ORDER)
    public Short order;

    @FilterName(ApiStrings.SequenceWorkflowEntry.Arguments.SEQUENCE)
    @JsonProperty(ApiStrings.SequenceWorkflowEntry.Arguments.SEQUENCE)
    @SortName(ApiStrings.SequenceWorkflowEntry.Arguments.SEQUENCE)
    public CollectionSequence collectionSequence;

    @FilterName(ApiStrings.SequenceWorkflowEntry.Arguments.SEQUENCE_WORKFLOW_ID)
    @JsonProperty(ApiStrings.SequenceWorkflowEntry.Arguments.SEQUENCE_WORKFLOW_ID)
    public Long sequenceWorkflowId;
}
