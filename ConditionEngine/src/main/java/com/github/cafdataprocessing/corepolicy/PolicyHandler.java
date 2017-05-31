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
package com.github.cafdataprocessing.corepolicy;

import com.github.cafdataprocessing.corepolicy.common.dto.Policy;
import com.github.cafdataprocessing.corepolicy.common.Document;

import java.util.Collection;

/**
 * A PolicyHandler is responsible for the execution of the policy.
 */
public interface PolicyHandler {
    /**
     * Return the policy type id that this PolicyHandler is configured for.
     * @return
     */
    long getPolicyTypeId();

    /**
     * Resolve policy conflicts for the policy type this PolicyHandler is configured for.
     * @param document
     * @param policies
     * @return
     */
    Collection<Policy> resolve(Document document, Collection<Policy> policies);

    /**
     * Handle the execution of the supplied policy.
     * @param document
     * @param policy
     * @return A result indicating if processing should continue
     */
    ProcessingAction handle(Document document, Policy policy, Long collectionSequence);
}