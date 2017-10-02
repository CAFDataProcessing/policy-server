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
package com.github.cafdataprocessing.corepolicy.common.domainModels;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.github.cafdataprocessing.corepolicy.common.EnvironmentSnapshot;
import com.github.cafdataprocessing.corepolicy.common.EnvironmentSnapshotDeserializer;
import com.github.cafdataprocessing.corepolicy.common.dto.*;
import com.github.cafdataprocessing.corepolicy.common.dto.conditions.Condition;
import org.joda.time.DateTime;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Implementation of an EnvironmentSnapshot
 */
@JsonDeserialize(using = EnvironmentSnapshotDeserializer.class)
public class EnvironmentSnapshotImpl implements EnvironmentSnapshot {
    private Long collectionSequenceId;
    private String instanceId = UUID.randomUUID().toString();
    private DateTime createDate;
    private DateTime persistedDate;
    private DateTime collectionSequenceLastModifiedDate;
    private boolean invalidatedCache;

    public String fingerprint;
    public HashMap<Long,CollectionSequence> collectionSequences = new HashMap<>();
    public HashMap<Long,DocumentCollection> collections = new HashMap<>();
    public HashMap<Long,Condition> conditions = new HashMap<>();
    public HashMap<String,FieldLabel> fieldLabels = new HashMap<>();
    public HashMap<Long,Lexicon> lexicons = new HashMap<>();
    public HashMap<Long,Policy> policies = new HashMap<>();
    public HashMap<Long,PolicyType> policyTypes = new HashMap<>();

    public EnvironmentSnapshotImpl(){
    }

    @Override
    public String getFingerprint(){
        return this.fingerprint;
    }

    @Override
    public CollectionSequence getSequence(Long sequenceId) {
        return getCollectionSequences().get(sequenceId);
    }

    @Override
    public DocumentCollection getCollection(Long collectionId) {
        return getCollections().get(collectionId);
    }

    @Override
    public Condition getCondition(Long conditionId) {
        return getConditions().get(conditionId);
    }

    @Override
    public Lexicon getLexicon(Long lexiconId) {
        return getLexicons().get(lexiconId);
    }

    @Override
    public FieldLabel getFieldLabel(String labelName) {
        return fieldLabels.get(labelName);
    }

    @Override
    public Policy getPolicy(Long policyId) {
        return getPolicies().get(policyId);
    }

    @Override
    public PolicyType getPolicyType(Long policyTypeId) {
        return getPolicyTypes().get(policyTypeId);
    }

    @Override
    public String getInstanceId() {
        return instanceId;
    }

    @Override
    public boolean getInvalidatedCache() { return invalidatedCache; }

    public void setInvalidatedCache(boolean value) { invalidatedCache = value; }

    public Long getCollectionSequenceId(){
        return collectionSequenceId;
    }

    public void setInstanceId(String instanceId){
        this.instanceId = instanceId;
    }

    public DateTime getCreateDate() {
        return createDate;
    }

    public void setCreateDate(DateTime createDate) {
        this.createDate = createDate;
    }

    public DateTime getPersistedDate() {
        return persistedDate;
    }

    public void setPersistedDate(DateTime persistedDate) {
        this.persistedDate = persistedDate;
    }

    public DateTime getCollectionSequenceLastModifiedDate() {
        return collectionSequenceLastModifiedDate;
    }

    public void setCollectionSequenceLastModifiedDate(DateTime collectionSequenceLastModifiedDate) {
        this.collectionSequenceLastModifiedDate = collectionSequenceLastModifiedDate;
    }

    public void setCollectionSequenceId(Long collectionSequenceId) {
        this.collectionSequenceId = collectionSequenceId;
    }

    public Map<Long, CollectionSequence> getCollectionSequences() {
        return collectionSequences;
    }

    public Map<Long, DocumentCollection> getCollections() {
        return collections;
    }

    public Map<Long, Condition> getConditions() {
        return conditions;
    }

    public Map<Long, Lexicon> getLexicons() {
        return lexicons;
    }

    public Map<Long, Policy> getPolicies() {
        return policies;
    }

    public Map<Long, PolicyType> getPolicyTypes() {
        return policyTypes;
    }

    @Override
    public Map<String, FieldLabel> getFieldLabels() {
        return fieldLabels;
    }
}
