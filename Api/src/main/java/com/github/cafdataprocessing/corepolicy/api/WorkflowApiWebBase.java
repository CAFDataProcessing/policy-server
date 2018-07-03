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
package com.github.cafdataprocessing.corepolicy.api;

import com.github.cafdataprocessing.corepolicy.common.ApiProperties;
import com.github.cafdataprocessing.corepolicy.common.WorkflowApi;
import com.github.cafdataprocessing.corepolicy.common.dto.*;
import com.github.cafdataprocessing.corepolicy.common.dtoweb.RetrieveAdditional;
import com.github.cafdataprocessing.corepolicy.common.dtoweb.RetrieveRequest;
import org.apache.http.NameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collection;

/**
 * Base class for Workflow Api methods when in web mode.
 */
public abstract class WorkflowApiWebBase extends WebApiBase implements WorkflowApi {

    private static final Logger logger = LoggerFactory.getLogger(WorkflowApiWebBase.class);

    public WorkflowApiWebBase(ApiProperties apiProperties) {
        super(apiProperties);
    }

    protected Logger getLogger() {
        return logger;
    }

    @Override
    public SequenceWorkflow create(SequenceWorkflow sequenceWorkflow) {
        return makeSingleRequest(WebApiAction.CREATE, sequenceWorkflow, DtoBase.class);
    }

    @Override
    public SequenceWorkflow update(SequenceWorkflow sequenceWorkflow) {
        return makeSingleRequest(WebApiAction.UPDATE, sequenceWorkflow, DtoBase.class);
    }

    @Override
    public void deleteSequenceWorkflow(Long id) {
        makeDeleteRequest(id, ItemType.SEQUENCE_WORKFLOW);
    }

    @Override
    public SequenceWorkflow retrieveSequenceWorkflow(Long id) {
        return retrieveSequenceWorkflows(Arrays.asList(id)).stream().findFirst().get();
    }

    @Override
    public PageOfResults<SequenceWorkflow> retrieveSequenceWorkflowsPage(PageRequest pageRequest) {
        return getRelatedItemPageOfResults(pageRequest, ItemType.SEQUENCE_WORKFLOW, null, null, SequenceWorkflow.class);
    }

    @Override
    public PageOfResults<SequenceWorkflow> retrieveSequenceWorkflowsPage(PageRequest pageRequest, Filter filter) {

        return getRelatedItemPageOfResults(pageRequest, ItemType.SEQUENCE_WORKFLOW, filter, SequenceWorkflow.class);
    }

    @Override
    public Collection<SequenceWorkflow> retrieveSequenceWorkflows(Collection<Long> ids) {
        RetrieveRequest retrieveRequest = new RetrieveRequest();
        retrieveRequest.type = ItemType.SEQUENCE_WORKFLOW;
        retrieveRequest.id = ids;

        final Collection<NameValuePair> params = createParams(retrieveRequest);

        return makeMultipleRequest(WebApiAction.RETRIEVE, params, SequenceWorkflow.class);
    }

    @Override
    public PageOfResults<SequenceWorkflowEntry> retrieveSequenceWorkflowEntriesPage(PageRequest pageRequest, Filter filter, Sort sort) {
        return getRelatedItemPageOfResults(pageRequest, ItemType.SEQUENCE_WORKFLOW_ENTRY, filter, sort, SequenceWorkflowEntry.class);
    }

    @Override
    public PageOfResults<SequenceWorkflowEntry> retrieveSequenceWorkflowEntriesPage(PageRequest pageRequest, Filter filter, Sort sort, Boolean includeCollectionSequences) {
        RetrieveRequest retrieveRequest = new RetrieveRequest();
        retrieveRequest.type = ItemType.SEQUENCE_WORKFLOW_ENTRY;
        retrieveRequest.additional = new RetrieveAdditional();
        retrieveRequest.additional.sort = sort;
        retrieveRequest.additional.filter = filter;
        retrieveRequest.start = pageRequest.start;
        retrieveRequest.max_page_results = pageRequest.max_page_results;
        retrieveRequest.additional.includeCollectionSequences = includeCollectionSequences;
        final Collection<NameValuePair> params = createParams(retrieveRequest);

        return makePagedRequest(WebApiAction.RETRIEVE, params, SequenceWorkflowEntry.class);
    }


    @Override
    public PageOfResults<SequenceWorkflowEntry> retrieveSequenceWorkflowEntriesPage(PageRequest pageRequest, Filter filter, Boolean includeCollectionSequences) {
        RetrieveRequest retrieveRequest = new RetrieveRequest();
        retrieveRequest.type = ItemType.SEQUENCE_WORKFLOW_ENTRY;
        retrieveRequest.additional = new RetrieveAdditional();
        retrieveRequest.additional.filter = filter;
        retrieveRequest.start = pageRequest.start;
        retrieveRequest.max_page_results = pageRequest.max_page_results;
        retrieveRequest.additional.includeCollectionSequences = includeCollectionSequences;
        final Collection<NameValuePair> params = createParams(retrieveRequest);

        return makePagedRequest(WebApiAction.RETRIEVE, params, SequenceWorkflowEntry.class);
    }

    @Override
    public PageOfResults<SequenceWorkflowEntry> retrieveSequenceWorkflowEntriesPage(PageRequest pageRequest) {
        return getRelatedItemPageOfResults(pageRequest, ItemType.SEQUENCE_WORKFLOW_ENTRY, null, null, SequenceWorkflowEntry.class);
    }

    @Override
    public PageOfResults<SequenceWorkflowEntry> retrieveSequenceWorkflowEntriesPage(PageRequest pageRequest, Filter filter) {
        return getRelatedItemPageOfResults(pageRequest, ItemType.SEQUENCE_WORKFLOW_ENTRY, filter, SequenceWorkflowEntry.class);
    }

}
