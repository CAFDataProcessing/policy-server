/*
 * Copyright 2015-2017 EntIT Software LLC, a Micro Focus company.
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
package com.github.cafdataprocessing.corepolicy.web;

import com.github.cafdataprocessing.corepolicy.common.ClassificationApi;
import com.github.cafdataprocessing.corepolicy.common.EnvironmentSnapshot;
import com.github.cafdataprocessing.corepolicy.common.PolicyApi;
import com.github.cafdataprocessing.corepolicy.common.WorkflowApi;
import com.github.cafdataprocessing.corepolicy.common.dto.*;
import com.github.cafdataprocessing.corepolicy.common.dto.conditions.Condition;
import com.github.cafdataprocessing.corepolicy.common.dtoweb.DeleteRequest;
import com.github.cafdataprocessing.corepolicy.common.dtoweb.DeleteResponse;
import com.github.cafdataprocessing.corepolicy.common.dtoweb.DeleteResult;
import com.github.cafdataprocessing.corepolicy.common.dtoweb.RetrieveRequest;
import com.github.cafdataprocessing.corepolicy.common.dtoweb.UpdateRequest;
import com.github.cafdataprocessing.corepolicy.common.exceptions.ConditionEngineException;
import com.github.cafdataprocessing.corepolicy.common.shared.ErrorCodes;
import com.github.cafdataprocessing.corepolicy.environment.EnvironmentSnapshotApi;

import com.google.common.base.Strings;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 *
 */
@Component
public class ControllerApi {

    private final static Logger logger = LoggerFactory.getLogger(ControllerApi.class);

    private ClassificationApi classificationApi;
    private PolicyApi policyApi;
    private WorkflowApi workflowApi;
    private final EnvironmentSnapshotApi environmentSnapshotApi;

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    public ControllerApi(ClassificationApi classificationApi, PolicyApi policyApi, EnvironmentSnapshotApi environmentSnapshotApi, WorkflowApi workflowApi) {
        this.classificationApi = classificationApi;
        this.policyApi = policyApi;
        this.environmentSnapshotApi = environmentSnapshotApi;
        this.workflowApi = workflowApi;
    }

    public DtoBase create(DtoBase request) {

        if(request instanceof Condition) {
            return classificationApi.create((Condition) request);
        }
        else if(request instanceof Policy){
            return policyApi.create((Policy) request);
        }
        else if(request instanceof PolicyType){
            return policyApi.create((PolicyType) request);
        }
        else if(request instanceof Lexicon){
            return classificationApi.create((Lexicon) request);
        }
        else if(request instanceof LexiconExpression){
            return classificationApi.create((LexiconExpression) request);
        }
        else if(request instanceof FieldLabel){
            return classificationApi.create((FieldLabel) request);
        }
        else if(request instanceof CollectionSequence){
            return classificationApi.create((CollectionSequence) request);
        }
        else if(request instanceof DocumentCollection){
            return classificationApi.create((DocumentCollection) request);
        }
        else if (request instanceof SequenceWorkflow){
            return workflowApi.create((SequenceWorkflow) request);
        }

        throw new ConditionEngineException(ErrorCodes.INVALID_JOB_ACTION_PARAMETER_VALUE, request.getClass().getName());
    }

    public DtoBase update(UpdateRequest updateRequest) {
        
        return update( updateRequest.objectToUpdate, updateRequest.updateBehaviour );
    }
    
    public DtoBase update(DtoBase request, UpdateBehaviourType updateBehaviour ) {
        if(request instanceof Condition) {
            return classificationApi.update((Condition) request, updateBehaviour);
        }
        else if(request instanceof Policy){
            checkValidUpdate(updateBehaviour);
            return policyApi.update((Policy) request);
        }
        else if(request instanceof PolicyType){
            checkValidUpdate(updateBehaviour);
            return policyApi.update((PolicyType) request);
        }
        else if(request instanceof Lexicon){
            return classificationApi.update((Lexicon) request, updateBehaviour);
        }
        else if(request instanceof LexiconExpression){
            return classificationApi.update((LexiconExpression) request, updateBehaviour);
        }
        else if(request instanceof FieldLabel){
            return classificationApi.update((FieldLabel) request, updateBehaviour);
        }
        else if(request instanceof CollectionSequence){
            return classificationApi.update((CollectionSequence) request, updateBehaviour);
        }
        else if(request instanceof DocumentCollection){
            return classificationApi.update((DocumentCollection)request, updateBehaviour);
        }
        else if (request instanceof SequenceWorkflow) {
            checkValidUpdate(updateBehaviour);
            return workflowApi.update((SequenceWorkflow) request);
        }

        throw new ConditionEngineException(ErrorCodes.INVALID_JOB_ACTION_PARAMETER_VALUE, request.getClass().getName());
    }


    private void checkValidUpdate(UpdateBehaviourType updateBehaviour){
        if(updateBehaviour != null && updateBehaviour != UpdateBehaviourType.REPLACE) {
            throw new UnsupportedOperationException(updateBehaviour.toValue() + " behaviour is not supported for this API.");
        }
    }

    public PageOfResults retrieve(RetrieveRequest retrieveRequest) {
        PageOfResults retrieveResponse = new PageOfResults();
        switch (retrieveRequest.type) {
            case COLLECTION_SEQUENCE: {
                if(retrieveRequest.additional!=null && retrieveRequest.additional.includeChildren != null && retrieveRequest.additional.includeChildren){
                    DateTime previousSnapshotTime = retrieveRequest.additional.datetime == null
                            ? null
                            : retrieveRequest.additional.datetime.toDateTime();

                    if ( retrieveRequest.id == null )
                        throw new ConditionEngineException(ErrorCodes.MISSING_REQUIRED_PARAMETERS, "No Collection Sequence Id parameter specified.");

                    // we dont really mind or care when the source env snapshot was created, only the cs last modified time and if we have
                    // a more recent CS.
                    EnvironmentSnapshot environmentSnapshot = environmentSnapshotApi.get(retrieveRequest.id.stream().findFirst().get(), null, previousSnapshotTime);

                    retrieveResponse.totalhits = environmentSnapshot == null ? 0L : 1L;
                    retrieveResponse.results = environmentSnapshot == null ? new ArrayList<>() : Arrays.asList(environmentSnapshot);
                    return retrieveResponse;
                }

                if (retrieveRequest.additional != null && !Strings.isNullOrEmpty(retrieveRequest.additional.name) ) {
                    Collection<CollectionSequence> collectionSequences = classificationApi.retrieveCollectionSequencesByName(retrieveRequest.additional.name);
                    retrieveResponse = new PageOfResults();
                    retrieveResponse.results = collectionSequences.stream().collect(Collectors.toList());
                    retrieveResponse.totalhits = (long) retrieveResponse.results.size();
                }else if (( retrieveRequest.additional != null ) && ( retrieveRequest.additional.filter != null )) {
                    PageOfResults<CollectionSequence> pageOfResults;
                    if (retrieveRequest.additional.sort != null) {
                        pageOfResults = classificationApi.retrieveCollectionSequencesPage(retrieveRequest, Filter.create(retrieveRequest.additional.filter), Sort.create(retrieveRequest.additional.sort));
                    } else {
                        pageOfResults = classificationApi.retrieveCollectionSequencesPage(retrieveRequest, Filter.create(retrieveRequest.additional.filter));
                    }
                    retrieveResponse.totalhits = pageOfResults.totalhits;
                    retrieveResponse.results = pageOfResults.results.stream().collect(Collectors.toList());
                } else if ((retrieveRequest.additional != null) && (retrieveRequest.additional.sort != null)) {//& retrieveRequest.additional.sort.textValue().equalsIgnoreCase("null"))) {
                    PageOfResults<CollectionSequence> pageOfResults = classificationApi.retrieveCollectionSequencesPage(retrieveRequest, Sort.create(retrieveRequest.additional.sort));
                    retrieveResponse.totalhits = pageOfResults.totalhits;
                    retrieveResponse.results = pageOfResults.results.stream().collect(Collectors.toList());
                }else {
                    retrieveResponse = doRetrieve(retrieveRequest,
                            classificationApi::retrieveCollectionSequences,
                            classificationApi::retrieveCollectionSequencesPage);
                }

                return retrieveResponse;
            }
            case COLLECTION: {
                if (retrieveRequest.id != null
                        && retrieveRequest.additional != null
                        && (retrieveRequest.additional.includeChildren != null || retrieveRequest.additional.includeCondition != null)) {
                    //Defaulting additional booleans to false here, one may be present but the other null
                    Collection<DocumentCollection> collections = classificationApi.retrieveCollections(
                            retrieveRequest.id
                            , retrieveRequest.additional.includeCondition == null ? false : retrieveRequest.additional.includeCondition
                            , retrieveRequest.additional.includeChildren == null ? false : retrieveRequest.additional.includeChildren);
                    retrieveResponse.results = collections.stream().collect(Collectors.toList());
                    retrieveResponse.totalhits = (long) retrieveResponse.results.size();
                    return retrieveResponse;
                }
                else if (( retrieveRequest.additional != null ) && ( retrieveRequest.additional.filter != null )) {
//                    PageOfResults<DocumentCollection> pageOfResults = classificationApi.retrieveCollectionsPage(retrieveRequest, Filter.create(retrieveRequest.additional.filter));
                    PageOfResults<DocumentCollection> pageOfResults;
                    if (retrieveRequest.additional.sort != null) {
                        pageOfResults = classificationApi.retrieveCollectionsPage(retrieveRequest, Filter.create(retrieveRequest.additional.filter), Sort.create(retrieveRequest.additional.sort));
                    } else {
                        pageOfResults = classificationApi.retrieveCollectionsPage(retrieveRequest, Filter.create(retrieveRequest.additional.filter));
                    }
                    retrieveResponse.totalhits = pageOfResults.totalhits;
                    retrieveResponse.results = pageOfResults.results.stream().collect(Collectors.toList());
                    return retrieveResponse;
                }
                else if (retrieveRequest.additional != null && retrieveRequest.additional.sort != null){
                    PageOfResults<DocumentCollection> pageOfResults = classificationApi.retrieveCollectionsPage(retrieveRequest, Filter.create(retrieveRequest.additional.filter), Sort.create(retrieveRequest.additional.sort));
                    retrieveResponse.totalhits = pageOfResults.totalhits;
                    retrieveResponse.results = pageOfResults.results.stream().collect(Collectors.toList());
                    return retrieveResponse;
                }

                return doRetrieve(retrieveRequest,
                        classificationApi::retrieveCollections,
                        classificationApi::retrieveCollectionsPage);
            }
            case CONDITION: {
                if (retrieveRequest.id != null && !retrieveRequest.id.isEmpty()) {
                    Boolean includeChildren = retrieveRequest.additional == null
                            ? null
                            : retrieveRequest.additional.includeChildren;
                    Collection<Condition> conditions = classificationApi.retrieveConditions(retrieveRequest.id, includeChildren);
                    retrieveResponse.results = conditions.stream().collect(Collectors.toList());
                    retrieveResponse.totalhits = (long) retrieveResponse.results.size();
                    return retrieveResponse;
                } else if (( retrieveRequest.additional != null ) && ( retrieveRequest.additional.filter != null )) {

//                    PageOfResults<Condition> pageOfResults = classificationApi.retrieveConditionsPage(retrieveRequest, Filter.create(retrieveRequest.additional.filter));
                    PageOfResults<Condition> pageOfResults;
                    if(retrieveRequest.additional.sort!=null){
                        pageOfResults = classificationApi.retrieveConditionsPage(retrieveRequest, Filter.create(retrieveRequest.additional.filter), Sort.create(retrieveRequest.additional.sort));
                    } else {
                        pageOfResults = classificationApi.retrieveConditionsPage(retrieveRequest, Filter.create(retrieveRequest.additional.filter), null);
                    }
                    retrieveResponse.totalhits = pageOfResults.totalhits;
                    retrieveResponse.results = pageOfResults.results.stream().collect(Collectors.toList());
                    return retrieveResponse;
                } else if (( retrieveRequest.additional != null) && (retrieveRequest.additional.sort != null)) {
                    PageOfResults<Condition> pageOfResults = classificationApi.retrieveConditionsPage(retrieveRequest, Sort.create(retrieveRequest.additional.sort));
                    retrieveResponse.totalhits = pageOfResults.totalhits;
                    retrieveResponse.results = pageOfResults.results.stream().collect(Collectors.toList());
                    return retrieveResponse;
                }

                return doRetrieve(retrieveRequest,
                        null,
                        classificationApi::retrieveConditionFragmentsPage);
            }
            case FIELD_LABEL: {

                if (( retrieveRequest.additional != null ) && ( retrieveRequest.additional.filter != null )) {
                    return doRetrieveByFilter(retrieveRequest);
                }

                return doRetrieve(retrieveRequest,
                        u -> Arrays.asList(classificationApi.retrieveFieldLabel(u)),
                        classificationApi::retrieveFieldLabelPage,
                        u -> {
                            if (u.additional == null || u.additional.name == null) {
                                return null;
                            } else {
                                return u.additional.name;
                            }
                        });
            }
            case LEXICON: {

                if (( retrieveRequest.additional != null ) && ( retrieveRequest.additional.filter != null )) {
                    return doRetrieveByFilter(retrieveRequest);
                }

                return doRetrieve(retrieveRequest,
                        classificationApi::retrieveLexicons,
                        classificationApi::retrieveLexiconsPage);
            }
            case LEXICON_EXPRESSION: {

                if (( retrieveRequest.additional != null ) && ( retrieveRequest.additional.filter != null )) {
                    return doRetrieveByFilter(retrieveRequest);
                }
                return doRetrieve(retrieveRequest,
                        classificationApi::retrieveLexiconExpressions,
                        classificationApi::retrieveLexiconExpressionsPage);
            }
            case POLICY: {
                if (( retrieveRequest.additional != null ) && ( retrieveRequest.additional.filter != null )) {
                    return doRetrieveByFilter(retrieveRequest);
                }
                return doRetrieve(retrieveRequest,
                        policyApi::retrievePolicies,
                        policyApi::retrievePoliciesPage);
            }
            case POLICY_TYPE: {
                if(retrieveRequest.additional != null && !Strings.isNullOrEmpty(retrieveRequest.additional.name)){
                    retrieveResponse.totalhits = 1L;
                    PolicyType policyType = policyApi.retrievePolicyTypeByName(retrieveRequest.additional.name);
                    retrieveResponse.results = new ArrayList<>(1);
                    retrieveResponse.results.add(policyType);
                    return retrieveResponse;
                }

                if (( retrieveRequest.additional != null ) && ( retrieveRequest.additional.filter != null )) {
                    return doRetrieveByFilter(retrieveRequest);
                }

                return doRetrieve(retrieveRequest,
                        policyApi::retrievePolicyTypes,
                        policyApi::retrievePolicyTypesPage);
//                        PolicyTypeWeb::put);
            }
            case SEQUENCE_WORKFLOW: {
                if (( retrieveRequest.additional != null ) && ( retrieveRequest.additional.filter != null )) {
                    return doRetrieveByFilter(retrieveRequest);
                }
                return doRetrieve(retrieveRequest,
                        workflowApi::retrieveSequenceWorkflows,
                        workflowApi::retrieveSequenceWorkflowsPage);
            }
            case SEQUENCE_WORKFLOW_ENTRY: {
                if (retrieveRequest.additional != null && retrieveRequest.additional.filter != null) {
                    return workflowApi.retrieveSequenceWorkflowEntriesPage(retrieveRequest, Filter.create(retrieveRequest.additional.filter),
                            retrieveRequest.additional.sort == null ? null : Sort.create(retrieveRequest.additional.sort),
                            retrieveRequest.additional.includeCollectionSequences == null ? false : retrieveRequest.additional.includeCollectionSequences);
                } else if (retrieveRequest.additional != null && retrieveRequest.additional.sort != null) {
                    return workflowApi.retrieveSequenceWorkflowEntriesPage(retrieveRequest, null, Sort.create(retrieveRequest.additional.sort),
                            retrieveRequest.additional.includeCollectionSequences == null ? false : retrieveRequest.additional.includeCollectionSequences);
                } else {
                    return workflowApi.retrieveSequenceWorkflowEntriesPage(retrieveRequest);
                }
            }
            default: {
                throw new ConditionEngineException(ErrorCodes.INVALID_JOB_ACTION_PARAMETER_VALUE, retrieveRequest.type.toValue());
            }
        }
    }

    public DeleteResponse delete(DeleteRequest deleteRequest) {
        DeleteResponse deleteResponse = new DeleteResponse();
        deleteResponse.result = new ArrayList<>();

        Consumer<Long> deleter;

        switch (deleteRequest.type) {
            case COLLECTION_SEQUENCE: {
                deleter = classificationApi::deleteCollectionSequence;
                break;
            }
            case COLLECTION: {
                deleter = classificationApi::deleteCollection;
                break;
            }
            case CONDITION: {
                deleter = classificationApi::deleteCondition;
                break;
            }
            case LEXICON: {
                deleter = classificationApi::deleteLexicon;
                break;
            }
            case LEXICON_EXPRESSION: {
                deleter = classificationApi::deleteLexiconExpression;
                break;
            }
            case POLICY: {
                deleter = policyApi::deletePolicy;
                break;
            }
            case POLICY_TYPE: {
                deleter = policyApi::deletePolicyType;
                break;
            }
            case FIELD_LABEL: {
                deleter = classificationApi::deleteFieldLabel;
                break;
            }
            case SEQUENCE_WORKFLOW: {
                deleter = workflowApi::deleteSequenceWorkflow;
                break;
            }
            default: {
                throw new ConditionEngineException(ErrorCodes.INVALID_JOB_ACTION_PARAMETER_VALUE, deleteRequest.type.toValue());
            }
        }

        for (Long id : deleteRequest.id) {
            deleteResponse.result.add(delete(id, deleter));
        }

        return deleteResponse;

    }

    DeleteResult delete(Long id, Consumer<Long> deleter) {
        DeleteResult deleteResult = new DeleteResult();
        deleteResult.id = id;
        try {
            deleter.accept(id);
            deleteResult.success = true;
        }
        catch (Exception e) {
            logger.error("Error deleting an item id " + id, e);
            deleteResult.success = false;
            deleteResult.errorMessage = e.getMessage();
        }
        return deleteResult;
    }

    private <T> PageOfResults doRetrieve(RetrieveRequest retrieveRequest,
                                            Function<Collection<Long>, Collection<T>> idRetrieval,
                                            Function<RetrieveRequest, PageOfResults<T>> pagedRetrieval
    ) {
        return doRetrieve(retrieveRequest, idRetrieval, pagedRetrieval, u -> u.id);
    }


    private <T> PageOfResults doRetrieveByFilter(RetrieveRequest retrieveRequest) {

        switch( retrieveRequest.type ) {
            case COLLECTION_SEQUENCE: {
                PageOfResults<CollectionSequence> pageOfResults = classificationApi.retrieveCollectionSequencesPage(retrieveRequest, Filter.create(retrieveRequest.additional.filter));
                return pageOfResults;
            }
            case COLLECTION: {
                PageOfResults<DocumentCollection> pageOfResults = classificationApi.retrieveCollectionsPage(retrieveRequest, Filter.create(retrieveRequest.additional.filter));
                return pageOfResults;
            }
            case CONDITION: {
                PageOfResults<Condition> pageOfResults = classificationApi.retrieveConditionsPage(retrieveRequest, Filter.create(retrieveRequest.additional.filter));
                return pageOfResults;
            }
            case FIELD_LABEL: {
                PageOfResults<FieldLabel> pageOfResults = classificationApi.retrieveFieldLabelPage(retrieveRequest, Filter.create(retrieveRequest.additional.filter));
                return pageOfResults;
            }
            case LEXICON: {
                PageOfResults<Lexicon> pageOfResults = classificationApi.retrieveLexiconsPage(retrieveRequest, Filter.create(retrieveRequest.additional.filter));
                return pageOfResults;
            }
            case LEXICON_EXPRESSION: {
                PageOfResults<LexiconExpression> pageOfResults = classificationApi.retrieveLexiconExpressionsPage(retrieveRequest, Filter.create(retrieveRequest.additional.filter));
                return pageOfResults;
            }
            case POLICY: {
                PageOfResults<Policy> pageOfResults = policyApi.retrievePoliciesPage(retrieveRequest, Filter.create(retrieveRequest.additional.filter));
                return pageOfResults;
            }
            case POLICY_TYPE: {
                PageOfResults<PolicyType> pageOfResults = policyApi.retrievePolicyTypesPage(retrieveRequest, Filter.create(retrieveRequest.additional.filter));
                return pageOfResults;
            }
            case SEQUENCE_WORKFLOW: {
                PageOfResults<SequenceWorkflow> pageOfResults = workflowApi.retrieveSequenceWorkflowsPage(retrieveRequest, Filter.create(retrieveRequest.additional.filter));
                return pageOfResults;
            }
            default:
                throw new UnsupportedOperationException("Filter unsupported on object type: " + retrieveRequest.type);
        }


    }

    /**
     * Gets the results depending on the request
     * @param retrieveRequest the request info
     * @param listRetrieval gets the list of results
     * @param pagedRetrieval gets a page of results
     * @param retrieveRequestConverter converts the retrieve request into the item to be passed to the list retrieval
//     * @param converter converts the response to the web form
     * @param <T> the type of the response
     * @param <T1> the type of the object passed to listRetrieval
     * @return the retrieveResponse
     */
    private <T, T1> PageOfResults doRetrieve(RetrieveRequest retrieveRequest,
                                                Function<T1, Collection<T>> listRetrieval,
                                                Function<RetrieveRequest, PageOfResults<T>> pagedRetrieval,
                                                Function<RetrieveRequest, T1> retrieveRequestConverter

    ) {
        PageOfResults retrieveResponse = new PageOfResults();

        T1 retrieveRequestResult = retrieveRequestConverter.apply(retrieveRequest);
        if (retrieveRequestResult != null) {
            if (listRetrieval != null) {
                Collection<T> results = listRetrieval.apply(retrieveRequestConverter.apply(retrieveRequest));
                retrieveResponse.results = results.stream().filter(u -> u != null).collect(Collectors.toList());
                retrieveResponse.totalhits = (long) retrieveResponse.results.size();
            }
        } else {
            PageOfResults<T> pageOfResults = pagedRetrieval.apply(retrieveRequest);
            retrieveResponse.totalhits = pageOfResults.totalhits;
            retrieveResponse.results = pageOfResults.results.stream().collect(Collectors.toList());
        }
        return retrieveResponse;
    }

}
