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
package com.github.cafdataprocessing.corepolicy;

import com.github.cafdataprocessing.corepolicy.common.EnvironmentSnapshot;
import com.github.cafdataprocessing.corepolicy.common.dto.ClassifyDocumentResult;
import com.github.cafdataprocessing.corepolicy.common.Document;

import java.util.Collection;

/**
 * The policy engine is responsible for the evaluation of a document against collections and conditions and the execution
 * of any policy associated with those collections.
 * Only policies for which a PolicyHandler has been registered will be executed.
 */
public interface PolicyEngine {
    /**
     * Get the EnvironmentSnapshot from the cache.
     * @return
     */
    EnvironmentSnapshot getEnvironmentSnapshot(long collectionSequenceId);

    /**
     * Invalidate the EnvironmentSnapshot cache for this collection sequence.
     */
    void invalidateCache(long collectionSequenceId);

    /**
     * Register a policy handler with the PolicyEngine, this will be used to resolve policy conflicts and execute policy.
     * @param policyHandler
     */
    void registerPolicyHandler(PolicyHandler policyHandler);

    /**
     * Evaluate a document for collection membership and policy, resolve policy conflicts where a suitable PolicyHandler is registered.
     * @param document
     * @return
     */
    ClassifyDocumentResult classify(long collectionSequenceId, Document document);

    /**
     * Execute the requested policies on the supplied document, if a suitable policy handler is not registered no action is taken.
     * @param collectionSequenceId
     * @param document
     * @param policyIds
     */
    void execute(Long collectionSequenceId, Document document, Collection<Long> policyIds);
}
