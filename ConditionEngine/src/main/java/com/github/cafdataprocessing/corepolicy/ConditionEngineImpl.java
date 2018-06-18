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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.cafdataprocessing.corepolicy.common.ConditionEngineMetadata;
import com.github.cafdataprocessing.corepolicy.common.EnvironmentSnapshot;
import com.github.cafdataprocessing.corepolicy.common.dto.*;
import com.github.cafdataprocessing.corepolicy.conditionEvaluators.ConditionEvaluationResult;
import com.google.common.base.Stopwatch;
import com.github.cafdataprocessing.corepolicy.common.dto.conditions.Condition;
import com.github.cafdataprocessing.corepolicy.common.exceptions.BackEndRequestFailedCpeException;
import com.github.cafdataprocessing.corepolicy.common.exceptions.CpeException;
import com.github.cafdataprocessing.corepolicy.common.exceptions.excpetionErrors.BackEndRequestFailedErrors;
import com.github.cafdataprocessing.corepolicy.conditionEvaluators.EvaluateCondition;
import com.github.cafdataprocessing.corepolicy.document.DocumentUnderEvaluation;
import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

/**
 * This class will classify a document against a given CollectionSequence.
 */
public class ConditionEngineImpl implements ConditionEngine {
    private final static Logger logger = LoggerFactory.getLogger(ConditionEngineImpl.class);

    private EvaluateCondition evaluateCondition;
    private EnvironmentSnapshotCache environmentSnapshotCache;
    private ConditionEngineMetadata conditionEngineMetadata;


    @Autowired
    public ConditionEngineImpl(
            EvaluateCondition evaluateCondition,
            EnvironmentSnapshotCache environmentSnapshotCache,
            ConditionEngineMetadata conditionEngineMetadata
    ) {
        this.evaluateCondition = evaluateCondition;
        this.environmentSnapshotCache = environmentSnapshotCache;
        this.conditionEngineMetadata = conditionEngineMetadata;
    }

    @Override
    public ConditionEngineResult evaluate(DocumentUnderEvaluation document, long collectionSequenceId) throws CpeException {
        EnvironmentSnapshot environmentSnapshot = getConditionEngineRepository(collectionSequenceId);

        return evaluate(document, collectionSequenceId, environmentSnapshot);
    }

    @Override
    public ConditionEngineResult evaluate(DocumentUnderEvaluation document, long collectionSequenceId, EnvironmentSnapshot environmentSnapshot) throws CpeException {

        //before any timings, We will attempt to recover any previous evaluation info from the document
        ConditionEngineResult previousResult = conditionEngineMetadata.createResultFromMetadata(document.getMetadata());

        Stopwatch stopwatch = Stopwatch.createStarted();
        ConditionEngineResult conditionEngineResult = new ConditionEngineResult();

        conditionEngineResult.reference =document.getReference();

        Stopwatch getEnvironmentStopwatch = Stopwatch.createStarted();
        CollectionSequence collectionSequence = environmentSnapshot.getSequence(collectionSequenceId);
        document.logTime("Evaluate-GetEnvironment", getEnvironmentStopwatch);

        logger.trace("Using environment: " + environmentSnapshot.getInstanceId() + " Created: " + environmentSnapshot.getCreateDate() + " CollSequence LastModified: " + environmentSnapshot.getCollectionSequenceLastModifiedDate());

        if (!collectionSequence.evaluationEnabled) {
            document.logTime("Evaluate", stopwatch);
            logger.trace("Collection Sequence " + collectionSequence.id + " evaluation disabled");
            return conditionEngineResult;
        }

        Collection<CollectionSequenceEntry> collectionSequenceEntries = collectionSequence.collectionSequenceEntries;
        for (CollectionSequenceEntry entry : collectionSequenceEntries) {

            boolean matchedTheCollection = false;

            Stopwatch allCollectionsStopwatch = Stopwatch.createStarted();
            Collection<Long> collectionIds = entry.collectionIds;
            for (Long collectionId : collectionIds) {
                Stopwatch collectionStopwatch = Stopwatch.createStarted();
                if(evaluateCollection(collectionSequence, document, conditionEngineResult, environmentSnapshot, collectionId)){
                    matchedTheCollection = true;
                }
                document.logTime("Evaluate-Collection"+collectionId, collectionStopwatch);
            }
            document.logTime("Evaluate-AllCollections", allCollectionsStopwatch);

            //stop evaluating any more collection sequence entries if we need to stop on match.
            if (matchedTheCollection && entry.stopOnMatch){
                document.logTime("Evaluate", stopwatch);
                return conditionEngineResult;
            }
        }

        if(conditionEngineResult.matchedCollections.isEmpty()){
            if(collectionSequence.defaultCollectionId !=null && collectionSequence.defaultCollectionId != 0){
                assignDefaultCollection(conditionEngineResult, environmentSnapshot, collectionSequence);
            }
        }

        document.logTime("Evaluate", stopwatch);

        if(conditionEngineResult.matchedCollections!=null){
            conditionEngineResult.matchedCollections.forEach(mc -> {
                if(mc.getPolicies()!=null){
                    mc.getPolicies().forEach(p -> {
                        p.setName(CollectionPolicy.unknownPolicyName);
                        try{
                            Policy policy = environmentSnapshot.getPolicy(p.getId());
                            if(policy!=null){
                                //The classification API encodes the name of a rule into the description in JSON format
                                if(policy.name.startsWith("CLASSIFICATION_CONDITION_ID:")){
                                    try{

                                        ObjectMapper objectMapper = new ObjectMapper();
                                        String unescaped = StringEscapeUtils.unescapeEcmaScript(policy.description);
                                        ObjectNode objectNode = objectMapper.readValue(unescaped.substring(1, unescaped.length()-1),
                                                ObjectNode.class);
                                        if(objectNode.has("name")){
                                            p.setName(objectNode.get("name").asText());
                                        }
                                    }
                                    catch(Exception e){
                                        logger.error("Detected a policy created by the classification API and failed to extract real name from the description.", e);
                                        p.setName(policy.name);
                                    }
                                }
                                else {
                                    p.setName(policy.name);
                                }

                            }
                        }
                        catch(Exception e) {
                            //A failure to retrieve the policy should be swallowed and result in the name being unknown
                        }
                    });
                }
            });
        }
        return conditionEngineResult;
    }

    protected EnvironmentSnapshot getConditionEngineRepository(long collectionSequenceId) throws CpeException {

        EnvironmentSnapshot environmentSnapshot;

        try {
            environmentSnapshot = environmentSnapshotCache.get(collectionSequenceId);
        } catch (Exception e) {
            throw new BackEndRequestFailedCpeException(BackEndRequestFailedErrors.GeneralFailure, e);
        }
        return environmentSnapshot;
    }

    private void assignDefaultCollection(ConditionEngineResult conditionEngineResult,
                                         EnvironmentSnapshot environmentSnapshot,
                                         CollectionSequence collectionSequence) {

        DocumentCollection defaultCollection = environmentSnapshot
                .getCollection(collectionSequence.defaultCollectionId);

        conditionEngineResult.matchedCollections.add(new MatchedCollection(defaultCollection, Arrays.asList()));

        conditionEngineResult.collectionIdAssignedByDefault = defaultCollection.id;
    }

    private boolean evaluateCollection(CollectionSequence collectionSequence, DocumentUnderEvaluation document, ConditionEngineResult conditionEngineResult,
                                       EnvironmentSnapshot environmentSnapshot, Long collectionId) throws CpeException {
        boolean matchedTheCollection = false;

        DocumentCollection collection = environmentSnapshot.getCollection(collectionId);

        if (collection.condition != null) {
            Condition condition = collection.condition;

            ConditionEvaluationResult conditionEvaluationResult = evaluateCondition.evaluate(collectionSequence, document, condition, environmentSnapshot);

            if (conditionEvaluationResult.isMatch()) {
                //collection is a match
                conditionEngineResult.matchedCollections.add(new MatchedCollection(collection, conditionEvaluationResult.getMatchedConditions()));
                matchedTheCollection = true;
            }

            if (conditionEvaluationResult.getUnevaluatedConditions().size() != 0) {
                //even if we matched, we should add any missing fields
                conditionEngineResult.unevaluatedConditions.addAll(conditionEvaluationResult.getUnevaluatedConditions());
                if(!matchedTheCollection){
                    conditionEngineResult.incompleteCollections.add(collectionId);
                }
            }

            // Potentially we could optimize here, and if its full collection match, then dont add any unmatched conditions or
            // matched conditions, as it is no longer relevant once a full collection sequence is evaluated.
            // But I have left here incase a condition is in more than one, and prevents reevaluation in more
            // than one limb for majority case.
            conditionEngineResult.unmatchedConditions.addAll( conditionEvaluationResult.getUnmatchedConditions());

            // note the CER matchedConditions list is all the matched conditions regardless of collection match status.
            conditionEngineResult.matchedConditions.addAll( conditionEvaluationResult.getAllConditionMatches());
        }
        return matchedTheCollection;
    }
}
