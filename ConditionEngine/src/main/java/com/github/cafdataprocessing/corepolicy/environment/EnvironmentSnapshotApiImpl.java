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
package com.github.cafdataprocessing.corepolicy.environment;

import com.github.cafdataprocessing.corepolicy.common.ClassificationApi;
import com.github.cafdataprocessing.corepolicy.common.EnvironmentSnapshot;
import com.github.cafdataprocessing.corepolicy.common.PolicyApi;
import com.github.cafdataprocessing.corepolicy.common.dto.*;
import com.github.cafdataprocessing.corepolicy.common.dto.conditions.*;
import com.github.cafdataprocessing.corepolicy.common.domainModels.EnvironmentSnapshotImpl;
import com.github.cafdataprocessing.corepolicy.common.exceptions.BackEndRequestFailedCpeException;
import com.github.cafdataprocessing.corepolicy.common.exceptions.CpeException;
import com.github.cafdataprocessing.corepolicy.common.exceptions.excpetionErrors.BackEndRequestFailedErrors;
import com.github.cafdataprocessing.corepolicy.common.shared.CorePolicyLogger;
import com.github.cafdataprocessing.corepolicy.repositories.EnvironmentSnapshotRepository;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

/**
 *
 */
public class EnvironmentSnapshotApiImpl implements EnvironmentSnapshotApi {
    private final ClassificationApi classificationApi;
    private final PolicyApi policyApi;
    private final EnvironmentSnapshotRepository archiveApi;
    private final static Logger logger = org.slf4j.LoggerFactory.getLogger(EnvironmentSnapshotApiImpl.class);

    @Autowired
    public EnvironmentSnapshotApiImpl(ClassificationApi classificationApi, PolicyApi policyApi, EnvironmentSnapshotRepository archiveApi){
        this.classificationApi = classificationApi;
        this.policyApi = policyApi;
        this.archiveApi = archiveApi;
        logger.trace("new EnvironmentSnapshotApiImpl");
    }

    @Override
    public EnvironmentSnapshot get(long collectionSequenceId, DateTime dateTime, DateTime csLastModifiedDateTime) {
        return initialize(collectionSequenceId, dateTime, csLastModifiedDateTime);
    }

    /**
     * Populates the the fingerprints of the snapshot impl and records this to the db.
     */
    private void recordSnapshot(EnvironmentSnapshotImpl snapshot){
        EnvironmentSnapshotFingerprintGenerator.populate(snapshot);
        this.archiveApi.recordSnapshot(snapshot);
    }


    private EnvironmentSnapshotImpl initialize(Long collectionSequenceId, DateTime dateTime, DateTime csLastModifiedDateTime) {

        try(CorePolicyLogger timingLogger = new CorePolicyLogger("EnvinromentSnapshotImpl::initialize")) {

            logger.debug("initialize - Supplied previous snapshot collectionSequenceId: " + collectionSequenceId + " DateTime: " + dateTime + " CSLastModifiedTime: " + csLastModifiedDateTime);

            Optional<CollectionSequence> optionalCollectionSequence = classificationApi.retrieveCollectionSequences(Arrays.asList(collectionSequenceId)).stream().findFirst();
            if (!optionalCollectionSequence.isPresent()) {
                throw new RuntimeException(String.format("Collection sequence %s does not exist.", collectionSequenceId));
            }
            CollectionSequence collectionSequence = optionalCollectionSequence.get();

            logger.debug( "initialize - CollectionSequence LastModified: " + collectionSequence.lastModified );

            //if nothing has changes since the last snapshot, return null to signal no change.
            // Dont use the createTime of the snapshot - we are interested in its contents, not the creation time.
            if (csLastModifiedDateTime != null && collectionSequence.lastModified.getMillis() <= csLastModifiedDateTime.getMillis()) {
                logger.debug( "initialize - No collection sequence changed detected" );
                return null;
            }

            EnvironmentSnapshotImpl environmentSnapshot = new EnvironmentSnapshotImpl();

            environmentSnapshot.setCreateDate(DateTime.now(DateTimeZone.UTC));
            environmentSnapshot.setCollectionSequenceLastModifiedDate( collectionSequence.lastModified );
            environmentSnapshot.setCollectionSequenceId(collectionSequenceId);
            environmentSnapshot.getCollectionSequences().put(collectionSequence.id, collectionSequence);

            if (collectionSequence.defaultCollectionId != null) {
                addCollection(environmentSnapshot, collectionSequence.defaultCollectionId);
            }

            for (CollectionSequenceEntry collectionSequenceEntry : collectionSequence.collectionSequenceEntries) {
                addCollections(environmentSnapshot, collectionSequenceEntry);
            }

            //remove null field labels. If they are there with a null val, they will be different than the
            //web ones, plus we shouldn't actually need to store that they are null, only that they are not!
            Iterator<Map.Entry<String, FieldLabel>> labelIter = environmentSnapshot.getFieldLabels().entrySet().iterator();
            while (labelIter.hasNext()) {
                Map.Entry<String, FieldLabel> entry = labelIter.next();
                if (entry.getValue() == null) {
                    labelIter.remove();
                }
            }

            recordSnapshot(environmentSnapshot);
            return environmentSnapshot;
        }
    }

    private void addCollections(EnvironmentSnapshotImpl environmentSnapshot, CollectionSequenceEntry collectionSequenceEntry)  {
        for (Long collectionId: collectionSequenceEntry.collectionIds){
            addCollection(environmentSnapshot, collectionId);
        }
    }

    private void addCollection(EnvironmentSnapshotImpl environmentSnapshot, Long collectionId) throws CpeException {
        if(collectionId==null || environmentSnapshot.getCollections().containsKey(collectionId)){
            return;
        }

        DocumentCollection documentCollection = classificationApi.retrieveCollections(Arrays.asList(collectionId), true, true).stream().findFirst().get();
        if(documentCollection==null){
            throw new BackEndRequestFailedCpeException(BackEndRequestFailedErrors.InvalidDataDetected, new Exception("Collection id "+ collectionId + " does not exist."));
        }

        //Add the conditions
        addCondition(environmentSnapshot, documentCollection.condition);

        if(documentCollection.policyIds != null) {
            documentCollection.policyIds.forEach(id -> addPolicy(environmentSnapshot, id));
        }

        environmentSnapshot.getCollections().put(documentCollection.id, documentCollection);
    }

    private void addCondition(EnvironmentSnapshotImpl environmentSnapshot, Condition condition) {
        if(condition==null || environmentSnapshot.getConditions().containsKey(condition.id)){
            return;
        }

        environmentSnapshot.getConditions().put(condition.id, condition);

        if (condition instanceof FragmentCondition) {
            FragmentCondition fragmentCondition = (FragmentCondition) condition;
            if(!environmentSnapshot.getConditions().containsKey(fragmentCondition.value)) {
                Collection<Condition> retrieveConditions = classificationApi.retrieveConditions(Arrays.asList(fragmentCondition.value), true);
                if (retrieveConditions.size() > 0) {
                    Condition retrievedCondition = retrieveConditions.stream().findFirst().get();
                    addCondition(environmentSnapshot, retrievedCondition);
                }
            }
        } else if (condition instanceof NotCondition) {
            addCondition(environmentSnapshot, ((NotCondition)condition).condition);
        } else if (condition instanceof BooleanCondition) {
            for (Condition childCondition : ((BooleanCondition) condition).children) {
                addCondition(environmentSnapshot, childCondition);
            }
        } else if (condition instanceof LexiconCondition) {
            addLexicon(environmentSnapshot, ((LexiconCondition)condition).value);
        }
        if (condition instanceof FieldCondition) {
            String fieldName = ((FieldCondition) condition).field;
            addFieldLabel(environmentSnapshot, fieldName);
        }
    }

    private void addLexicon(EnvironmentSnapshotImpl environmentSnapshot, Long lexiconId) {
        if(environmentSnapshot.getLexicons().containsKey(lexiconId)) {
            return;
        }

        Collection<Long> lexiconIds = new ArrayList<>();
        lexiconIds.add(lexiconId);
        Lexicon lexicon = classificationApi.retrieveLexicons(lexiconIds).stream().findFirst().get();

        environmentSnapshot.getLexicons().put(lexicon.id, lexicon);
    }

    private void addPolicy(EnvironmentSnapshotImpl environmentSnapshot, Long policyId){
        if(environmentSnapshot.getPolicies().containsKey(policyId)) {
            return;
        }

        Policy policy = policyApi.retrievePolicy(policyId);
        addPolicyType(environmentSnapshot, policy.typeId);
        environmentSnapshot.getPolicies().put(policyId, policy);
    }

    private void addPolicyType(EnvironmentSnapshotImpl environmentSnapshot, Long policyTypeId){
        if(environmentSnapshot.getPolicyTypes().containsKey(policyTypeId)) {
            return;
        }

        PolicyType policyType = policyApi.retrievePolicyType(policyTypeId);
        environmentSnapshot.getPolicyTypes().put(policyTypeId, policyType);
    }

    private void addFieldLabel(EnvironmentSnapshotImpl environmentSnapshot, String addFieldName){
        if(environmentSnapshot.getFieldLabels().containsKey(addFieldName)) {
            return;
        }

        FieldLabel fieldLabel = classificationApi.retrieveFieldLabel(addFieldName);
        environmentSnapshot.getFieldLabels().put(addFieldName, fieldLabel);
    }
}
