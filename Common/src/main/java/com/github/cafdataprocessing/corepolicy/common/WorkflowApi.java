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
package com.github.cafdataprocessing.corepolicy.common;

import com.github.cafdataprocessing.corepolicy.common.dto.*;


import java.util.Collection;

/**
 * Interface defining the API that should be implemented for workflows
 */
public interface WorkflowApi {

    // SequenceWorkflow
    SequenceWorkflow create(SequenceWorkflow t);

    SequenceWorkflow update(SequenceWorkflow t);

    void deleteSequenceWorkflow(Long id);

    SequenceWorkflow retrieveSequenceWorkflow(Long id);

    PageOfResults<SequenceWorkflow> retrieveSequenceWorkflowsPage(PageRequest pageRequest);
    PageOfResults<SequenceWorkflow> retrieveSequenceWorkflowsPage(PageRequest pageRequest, Filter filter );
    PageOfResults<SequenceWorkflowEntry> retrieveSequenceWorkflowEntriesPage (PageRequest pageRequest);
    PageOfResults<SequenceWorkflowEntry> retrieveSequenceWorkflowEntriesPage (PageRequest pageRequest, Filter filter, Sort sort);
    PageOfResults<SequenceWorkflowEntry> retrieveSequenceWorkflowEntriesPage (PageRequest pageRequest, Filter filter, Sort sort, Boolean includeCollectionSequences);
    PageOfResults<SequenceWorkflowEntry> retrieveSequenceWorkflowEntriesPage(PageRequest pageRequest, Filter filter);
    PageOfResults<SequenceWorkflowEntry> retrieveSequenceWorkflowEntriesPage(PageRequest pageRequest, Filter filter, Boolean includeCollectionSequences);

    Collection<SequenceWorkflow> retrieveSequenceWorkflows(Collection<Long> ids);
}
