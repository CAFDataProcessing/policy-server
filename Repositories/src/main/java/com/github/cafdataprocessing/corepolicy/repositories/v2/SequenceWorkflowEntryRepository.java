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
package com.github.cafdataprocessing.corepolicy.repositories.v2;

import com.github.cafdataprocessing.corepolicy.common.ApiStrings;
import com.github.cafdataprocessing.corepolicy.common.FilterName;
import com.github.cafdataprocessing.corepolicy.common.HibernateWrappedItem;
import com.github.cafdataprocessing.corepolicy.common.SortName;
import com.github.cafdataprocessing.corepolicy.common.dto.*;

import java.util.Collection;

/**
 * Defines methods that should be available on a SequenceWorkflowEntryRepository repository
 */
public interface SequenceWorkflowEntryRepository extends Repository<SequenceWorkflowEntryRepository.Item>{

    Collection<Item> retrieveForSequenceWorkflow(ExecutionContext executionContext, Long sequenceWorkflowId);
    PageOfResults<Item> retrievePage(ExecutionContext executionContext, PageRequest pageRequest, Filter filter, Sort sort, Boolean includeCollectionSequence);

    void deleteAll(ExecutionContext executionContext, Long sequenceWorkflowId);

    public static class Item extends DtoBase {
       
        // internal field, only put on by the hibernate annoation mapping layer, not seen by users, so shouldn't be in their filter/sort.
        public static final String WrapperFilterName = "sequence_workflow_entry";

        @FilterName(ApiStrings.SequenceWorkflowEntry.Arguments.SEQUENCE_WORKFLOW_ID)
        public Long sequenceWorkflowId;

        @FilterName(WrapperFilterName)
        @SortName(WrapperFilterName)
        @HibernateWrappedItem("sequenceWorkflowEntry")
        public SequenceWorkflowEntry sequenceWorkflowEntry;
    }
}
