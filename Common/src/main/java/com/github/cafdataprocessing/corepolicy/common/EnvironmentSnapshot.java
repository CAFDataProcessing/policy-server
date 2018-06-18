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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.github.cafdataprocessing.corepolicy.common.dto.*;
import com.github.cafdataprocessing.corepolicy.common.dto.conditions.Condition;
import org.joda.time.DateTime;

import java.util.Map;

/**
 * Interface for a snapshot of the core policy environment representing the objects available.
 */
@JsonDeserialize(using = EnvironmentSnapshotDeserializer.class)
@JsonSerialize(using = EnvironmentSnapshotSerializer.class)
public interface EnvironmentSnapshot {
    DateTime getCreateDate();
    DateTime getPersistedDate();
    DateTime getCollectionSequenceLastModifiedDate();

    String getFingerprint();
    boolean getInvalidatedCache();

    /**
     * A unique identifier for the snapshot
     * @return
     */
    String getInstanceId();

    /**
     * Get the collection sequence id for this snapshot
     * @return
     */
    Long getCollectionSequenceId();

    /**
     * Get the matching sequence
     * @param sequenceId the id of the sequence to return
     * @return the matching sequence
     */
    CollectionSequence getSequence(Long sequenceId);

    /**
     * Get the matching document collection
     * @param collectionId the id of the document collection
     * @return the matching document collection
     */
    DocumentCollection getCollection(Long collectionId);

    /**
     * Get the matching condition
     * @param conditionId the id of the condition
     * @return the matching condition
     */
    Condition getCondition(Long conditionId);

    /**
     * Get the matching lexicon
     * @param lexiconId the id of the lexicon
     * @return the matching lexicon
     */
    Lexicon getLexicon(Long lexiconId);
    /**
     * Get the field label for a name
     * @param labelName the label name
     * @return the field label for the name
     */
    FieldLabel getFieldLabel(String labelName);

    /**
     * Get the matching policy
     * @param policyId the id of the policy
     * @return the matching policy
     */
    Policy getPolicy(Long policyId);

    /**
     * Get the matching policy type
     * @param policyTypeId the id of the policy type
     * @return the matching policy type
     */
    PolicyType getPolicyType(Long policyTypeId);

    Map<Long, CollectionSequence> getCollectionSequences();
    Map<Long, DocumentCollection> getCollections();
    Map<Long, Condition> getConditions();
    Map<Long, Lexicon> getLexicons();
    Map<Long, Policy> getPolicies();
    Map<Long, PolicyType> getPolicyTypes();
    Map<String, FieldLabel> getFieldLabels();
}
