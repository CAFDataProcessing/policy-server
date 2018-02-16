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
package com.github.cafdataprocessing.corepolicy;

import com.github.cafdataprocessing.corepolicy.common.*;
import com.github.cafdataprocessing.corepolicy.common.dto.*;
import com.github.cafdataprocessing.corepolicy.common.exceptions.BackEndRequestFailedCpeException;
import com.github.cafdataprocessing.corepolicy.common.exceptions.CpeException;
import com.github.cafdataprocessing.corepolicy.document.DocumentUnderEvaluationImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * The policy engine is responsible for the evaluation of a document against collections and conditions and the execution
 * of any policy associated with those collections.
 * Only policies for which a PolicyHandler has been registered will be executed.
 */
public class PolicyEngineImpl implements PolicyEngine {
    private final PolicyApi policyApi;
    private final EnvironmentSnapshotCache environmentSnapshotCache;
    private final ConditionEngine conditionEngine;
    private final ApiProperties apiProperties;
    private ConditionEngineMetadata conditionEngineMetadata;
    private final Collection<PolicyHandler> policyHandlers = new ArrayList<>();
    private static Logger logger = LoggerFactory.getLogger(PolicyEngineImpl.class);

    @Autowired
    public PolicyEngineImpl(PolicyApi policyApi, EnvironmentSnapshotCache environmentSnapshotCache, ConditionEngine conditionEngine, ConditionEngineMetadata conditionEngineMetadata, ApiProperties apiProperties){
        this.policyApi = policyApi;
        this.environmentSnapshotCache = environmentSnapshotCache;
        this.conditionEngine = conditionEngine;
        this.conditionEngineMetadata = conditionEngineMetadata;
        this.apiProperties = apiProperties;
    }

    @Override
    public EnvironmentSnapshot getEnvironmentSnapshot(long collectionSequenceId) {
        try {
            return environmentSnapshotCache.get(collectionSequenceId);
        } catch(CpeException e) {
            throw e;
        } catch(Exception e) {
            throw new BackEndRequestFailedCpeException(e);
        }
    }

    @Override
    public void invalidateCache(long collectionSequenceId) {
        environmentSnapshotCache.invalidate(collectionSequenceId);
    }

    /**
     * This method adds the policy handler to the collection of handlers registered
     * @param policyHandler handler to add to the collection of registered handlers, handler must not be null.
     * @throws NullPointerException thrown when policyHandler is null
     */
    @Override
    public void registerPolicyHandler(PolicyHandler policyHandler)
    {
        Objects.requireNonNull(policyHandler);
        policyHandlers.add(policyHandler);
    }

    @Override
    public ClassifyDocumentResult classify(long collectionSequenceId, Document document) {
        DocumentUnderEvaluationImpl documentUnderEvaluation = new DocumentUnderEvaluationImpl(document, conditionEngineMetadata, apiProperties);
        documentUnderEvaluation.addMetadataString(DocumentFields.KV_Metadata_Present_FieldName, String.valueOf(document.getFullMetadata()));

        EnvironmentSnapshot environmentSnapshot = getEnvironmentSnapshot(collectionSequenceId);

        // Use the same snapshot across both calls, JIC it gets invalidated between the 2.
        ConditionEngineResult conditionEngineResult = conditionEngine.evaluate(documentUnderEvaluation,
                collectionSequenceId,
                environmentSnapshot);

        ClassifyDocumentResult classifyResult = ClassifyDocumentResult.create(conditionEngineResult, conditionEngineMetadata);

        classifyResult.resolvedPolicies = resolvePolicies(environmentSnapshot, document, conditionEngineResult);

        return classifyResult;
    }

    private Collection<Long> resolvePolicies(EnvironmentSnapshot environmentSnapshot, Document document, ConditionEngineResult conditionEngineResult) {
        Collection<Long> resolvedPolicies = new ArrayList<>();
        Collection<CollectionPolicy> collectionPolicies = new ArrayList<>();

        for(MatchedCollection matchedCollection: conditionEngineResult.matchedCollections){
            collectionPolicies.addAll(matchedCollection.getPolicies());
        }

        Collection<Long> policyIds = collectionPolicies.stream().map(CollectionPolicy::getId).distinct().collect(Collectors.toList());
        Collection<Policy> policies = getPolicies(environmentSnapshot, policyIds);
        Collection<Long> distinctPolicyTypeIds = policies.stream().map(p -> p.typeId).distinct().collect(Collectors.toList());

        for(Long policyTypeId:distinctPolicyTypeIds){
            PolicyType policyType = environmentSnapshot.getPolicyType(policyTypeId);
            Collection<Policy> policiesOfType = policies.stream().filter(p -> p.typeId.equals(policyTypeId)).collect(Collectors.toList());

            if(policyType.conflictResolutionMode==null || policyType.conflictResolutionMode.equals(ConflictResolutionMode.PRIORITY)){
                Optional<Integer> highestPriority = policiesOfType.stream().map(p -> p.priority).max((p1,p2) -> p1 - p2);
                if(highestPriority.isPresent()){
                    resolvedPolicies.addAll(policiesOfType.stream().filter(p -> p.priority.equals(highestPriority.get()))
                            .map(p -> p.id).collect(Collectors.toList()));
                }
                else{
                    resolvedPolicies.addAll(policiesOfType.stream().map(p -> p.id).collect(Collectors.toList()));
                }
            }
            else if (policyType.conflictResolutionMode.equals(ConflictResolutionMode.CUSTOM)){
                Optional<PolicyHandler> policyHandler = policyHandlers.stream().filter(ph -> ph.getPolicyTypeId()==policyTypeId).findFirst();
                if(policyHandler.isPresent()){
                    PolicyHandler policyHandler1 = policyHandler.get();
                    resolvedPolicies.addAll(policyHandler1.resolve(document, policiesOfType).stream().map(p -> p.id).collect(Collectors.toList()));
                }
            }
        }

        return resolvedPolicies;
    }

    @Override
    public void execute(Long collectionSequenceId, Document document, Collection<Long> policyIds) {
        Collection<Policy> policies = null;
        if (collectionSequenceId != null) {
            EnvironmentSnapshot environmentSnapshot = getEnvironmentSnapshot(collectionSequenceId);
            policies = getPolicies(environmentSnapshot, policyIds);
        }else {
            policies = getPolicies(policyIds);
        }

        Collection<Long> distinctPolicyTypeIds = policies.stream().map(p -> p.typeId).distinct().collect(Collectors.toList());

        for (Long policyTypeId : distinctPolicyTypeIds) {
            Collection<Policy> policiesOfType = policies.stream().filter(p -> p.typeId.equals(policyTypeId)).collect(Collectors.toList());

            Optional<PolicyHandler> policyHandler = policyHandlers.stream().filter(ph -> ph != null && ph.getPolicyTypeId() == policyTypeId).findFirst();
            if (policyHandler.isPresent()) {
                for (Policy policy : policiesOfType) {
                    ProcessingAction processingAction = policyHandler.get().handle(document, policy, collectionSequenceId);

                    if(processingAction == ProcessingAction.STOP_PROCESSING) {
                        return;
                    }
                }
            }
            else {
                logger.warn("No handler registered for policy type id " + policyTypeId);
            }
        }
    }

    private Collection<Policy> getPolicies(Collection<Long> policyIds) {
        return policyApi.retrievePolicies(policyIds);
    }

    private Collection<Policy> getPolicies(EnvironmentSnapshot environmentSnapshot, Collection<Long> policyIds) {
        Collection<Policy> policies = new ArrayList<>();
        for(Long policyId: policyIds){
            final Policy policy = environmentSnapshot.getPolicy(policyId);
            if(policy==null){
                throw new BackEndRequestFailedCpeException(new RuntimeException(String.format("Policy id %s not found.", String.valueOf(policyId))));
            }
            policies.add(policy);
        }
        return policies;
    }
}
