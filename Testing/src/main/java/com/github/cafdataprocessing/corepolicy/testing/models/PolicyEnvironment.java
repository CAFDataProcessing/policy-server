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
package com.github.cafdataprocessing.corepolicy.testing.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.cafdataprocessing.corepolicy.common.dto.*;
import com.github.cafdataprocessing.corepolicy.common.dto.conditions.Condition;

import java.util.Collection;

/**
 *
 */
public class PolicyEnvironment {
    @JsonProperty("lexicons")
    public Collection<Lexicon> lexicons;
    @JsonProperty("lexicon_expressions")
    public Collection<LexiconExpression> lexiconExpressions;
    @JsonProperty("collection_sequences")
    public Collection<CollectionSequence> collectionSequences;
    @JsonProperty("collection_sequence_entries")
    public Collection<CollectionSequenceEntry> collectionSequenceEntries;
    @JsonProperty("document_collections")
    public Collection<DocumentCollection> documentCollections;
    @JsonProperty("conditions")
    public Collection<Condition> conditions;
    @JsonProperty("policy_types")
    public Collection<PolicyType> policyTypes;
    @JsonProperty("policies")
    public Collection<Policy> policies;
}
