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

import com.github.cafdataprocessing.corepolicy.common.dto.*;
import com.github.cafdataprocessing.corepolicy.common.ApiProperties;
import com.github.cafdataprocessing.corepolicy.common.ConditionEngineMetadata;
import com.github.cafdataprocessing.corepolicy.common.EnvironmentSnapshot;
import com.github.cafdataprocessing.corepolicy.common.dto.conditions.Condition;
import com.github.cafdataprocessing.corepolicy.common.dto.conditions.TestCondition;
import com.github.cafdataprocessing.corepolicy.common.exceptions.CpeException;
import com.github.cafdataprocessing.corepolicy.conditionEvaluators.ConditionEvaluationResult;
import com.github.cafdataprocessing.corepolicy.conditionEvaluators.ConditionKeyHelper;
import com.github.cafdataprocessing.corepolicy.conditionEvaluators.EvaluateCondition;
import com.github.cafdataprocessing.corepolicy.document.DocumentUnderEvaluation;
import com.github.cafdataprocessing.corepolicy.document.DocumentUnderEvaluationImpl;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.stream.Collectors;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ConditionEngineTest {

    @Mock
    ConditionEngineMetadata conditionEngineMetadata;

    DocumentUnderEvaluationImpl document;

    @Mock
    CollectionSequence collectionSequence;
    @InjectMocks
    private TestCondition matchingNumberCondition;
    @InjectMocks
    private TestCondition notMatchingNumberCondition;
    @InjectMocks
    private TestCondition matchingStringCondition;
    @InjectMocks
    private TestCondition notMatchingStringCondition;
    @InjectMocks
    private TestCondition matchingExistsCondition;
    @InjectMocks
    private TestCondition notMatchingExistsCondition1;
    @InjectMocks
    private TestCondition notMatchingExistsCondition2;

    @Mock
    private EnvironmentSnapshot environmentSnapshot;

    @Mock
    private EnvironmentSnapshotCache environmentSnapshotCache;

    @Mock
    ExcludedContentProcessor excludedContentProcessor;

    @Mock
    EvaluateCondition evaluateCondition;

    @Mock
    private ApiProperties apiProperties;

    @InjectMocks
    ConditionEngine evaluator = new ConditionEngineImpl(evaluateCondition, environmentSnapshotCache, conditionEngineMetadata);

    @Before
    public void setup() throws Exception {

        document = new DocumentUnderEvaluationImpl(conditionEngineMetadata, apiProperties);

        document.setReference("testMe");

        when(environmentSnapshotCache.get(any())).thenReturn(environmentSnapshot);

        {
            ConditionEvaluationResult conditionEvaluationResult = new ConditionEvaluationResult();
            conditionEvaluationResult.setMatch(true);

            MatchedCondition matchedCondition = new  MatchedCondition(new DocumentUnderEvaluationImpl(conditionEngineMetadata, apiProperties).getReference(),matchingNumberCondition);
            conditionEvaluationResult.getMatchedConditions().add(matchedCondition);
            conditionEvaluationResult.getAllConditionMatches().add(matchedCondition);

            when(evaluateCondition.evaluate(any(CollectionSequence.class), any(DocumentUnderEvaluation.class), eq(matchingNumberCondition), eq(environmentSnapshot))).thenReturn(conditionEvaluationResult);
        }

        {
            ConditionEvaluationResult conditionEvaluationResult = new ConditionEvaluationResult();
            conditionEvaluationResult.setMatch(false);

            when(evaluateCondition.evaluate(any(CollectionSequence.class), any(DocumentUnderEvaluation.class), eq(notMatchingNumberCondition), eq(environmentSnapshot))).thenReturn(conditionEvaluationResult);
        }

        {
            ConditionEvaluationResult conditionEvaluationResult = new ConditionEvaluationResult();
            conditionEvaluationResult.setMatch(true);
            MatchedCondition matchedCondition = new MatchedCondition(new DocumentUnderEvaluationImpl(conditionEngineMetadata, apiProperties).getReference(),matchingStringCondition);
            conditionEvaluationResult.getMatchedConditions().add(matchedCondition);
            conditionEvaluationResult.getAllConditionMatches().add(matchedCondition);

            when(evaluateCondition.evaluate(any(CollectionSequence.class), any(DocumentUnderEvaluation.class), eq(matchingStringCondition), eq(environmentSnapshot))).thenReturn(conditionEvaluationResult);
        }

        {
            ConditionEvaluationResult conditionEvaluationResult = new ConditionEvaluationResult();
            conditionEvaluationResult.setMatch(false);
            UnevaluatedCondition unevaluatedCondition = new UnevaluatedCondition(notMatchingStringCondition, UnevaluatedCondition.Reason.MISSING_FIELD);

            conditionEvaluationResult.getUnevaluatedConditions().add(unevaluatedCondition);

            when(evaluateCondition.evaluate(any(CollectionSequence.class), any(DocumentUnderEvaluation.class), eq(notMatchingStringCondition), eq(environmentSnapshot))).thenReturn(conditionEvaluationResult);
        }

        {
            ConditionEvaluationResult conditionEvaluationResult = new ConditionEvaluationResult();
            conditionEvaluationResult.setMatch(true);

            MatchedCondition matchedCondition = new MatchedCondition(new DocumentUnderEvaluationImpl(conditionEngineMetadata, apiProperties).getReference(),matchingExistsCondition);

            conditionEvaluationResult.getMatchedConditions().add(matchedCondition);
            conditionEvaluationResult.getAllConditionMatches().add(matchedCondition);

            when(evaluateCondition.evaluate(any(CollectionSequence.class), any(DocumentUnderEvaluation.class), eq(matchingExistsCondition), eq(environmentSnapshot))).thenReturn(conditionEvaluationResult);
        }

        {
            ConditionEvaluationResult conditionEvaluationResult = new ConditionEvaluationResult();
            conditionEvaluationResult.setMatch(false);
            UnevaluatedCondition unevaluatedCondition = new UnevaluatedCondition(notMatchingExistsCondition1, UnevaluatedCondition.Reason.MISSING_FIELD);
            conditionEvaluationResult.getUnevaluatedConditions().add(unevaluatedCondition);

            when(evaluateCondition.evaluate(any(CollectionSequence.class), any(DocumentUnderEvaluation.class), eq(notMatchingExistsCondition1), eq(environmentSnapshot))).thenReturn(conditionEvaluationResult);
        }

        {
            ConditionEvaluationResult conditionEvaluationResult = new ConditionEvaluationResult();
            conditionEvaluationResult.setMatch(false);
            UnevaluatedCondition unevaluatedCondition = new UnevaluatedCondition(notMatchingExistsCondition2, UnevaluatedCondition.Reason.MISSING_FIELD);
            conditionEvaluationResult.getUnevaluatedConditions().add(unevaluatedCondition);

            when(evaluateCondition.evaluate(any(CollectionSequence.class), any(DocumentUnderEvaluation.class), eq(notMatchingExistsCondition2), eq(environmentSnapshot))).thenReturn(conditionEvaluationResult);
        }
    }

    /**
     * A utility method to create a DocumentCollection for a single condition
     * */
    private DocumentCollection createDocumentCollection(Condition condition) throws CpeException {
        DocumentCollection documentCollection = new DocumentCollection();
        long id = Helper.getId();
        documentCollection.id = (id);
        documentCollection.name = (Long.toString(id));
        documentCollection.description = ("Mocked collection");
        documentCollection.condition = (condition);

        when(environmentSnapshot.getCollection(documentCollection.id)).thenReturn(documentCollection);

        return documentCollection;
    }

    @After
    public void cleanup(){

    }

    @Test
    public void testEmptySequenceHasNoMatch() throws CpeException {
        CollectionSequence sequence = addCollectionSequence();

        sequence.collectionSequenceEntries.add(new CollectionSequenceEntry());

        ConditionEngineResult result = this.evaluator.evaluate(this.document, sequence.id);

        Assert.assertTrue(result.matchedCollections.isEmpty());
    }

    @Test
    public void testSingleSequenceMatch() throws CpeException {
        CollectionSequence sequence = addCollectionSequence();
        CollectionSequenceEntry entry = new CollectionSequenceEntry();

        sequence.collectionSequenceEntries.add(entry);
        sequence.evaluationEnabled = true;

        DocumentCollection collection =
                createDocumentCollection(matchingNumberCondition);
        entry.collectionIds.add(collection.id);

        ConditionEngineResult result = this.evaluator.evaluate(this.document, sequence.id);

        Assert.assertEquals(1, result.matchedCollections.size());
        Assert.assertTrue(result.matchedCollections.stream()
                .anyMatch(c -> c.getId() == collection.id));
    }

    @Test
    public void testDisabledCollectionSequenceReturnsBlankResult() {
        ConditionEngineResult blankConditionEngineResult = new ConditionEngineResult();
        blankConditionEngineResult.reference= "testMe";

        CollectionSequence sequence = addCollectionSequence();
        CollectionSequenceEntry entry = new CollectionSequenceEntry();

        sequence.collectionSequenceEntries.add(entry);
        sequence.evaluationEnabled = false;

        DocumentCollection collection = createDocumentCollection(matchingNumberCondition);
        entry.collectionIds.add(collection.id);

        ConditionEngineResult result = this.evaluator.evaluate(this.document, sequence.id);

        Assert.assertEquals(blankConditionEngineResult.reference, result.reference);
        Assert.assertEquals(blankConditionEngineResult.unevaluatedConditions, result.unevaluatedConditions);
        Assert.assertEquals(blankConditionEngineResult.matchedCollections, result.matchedCollections);
        Assert.assertEquals(blankConditionEngineResult.unmatchedConditions, result.unmatchedConditions);
        Assert.assertEquals(blankConditionEngineResult.matchedConditions, result.matchedConditions);
        Assert.assertEquals(blankConditionEngineResult.collectionIdAssignedByDefault, result.collectionIdAssignedByDefault);
        Assert.assertEquals(blankConditionEngineResult.incompleteCollections, result.incompleteCollections);
    }

    @Test
    public void testSingleSequenceNoMatch() throws CpeException {
        CollectionSequence sequence = addCollectionSequence();
        CollectionSequenceEntry entry = new CollectionSequenceEntry();

        sequence.collectionSequenceEntries.add(entry);

        DocumentCollection collection =
                createDocumentCollection(notMatchingNumberCondition);
        entry.collectionIds.add(collection.id);

        ConditionEngineResult result = this.evaluator.evaluate(this.document, sequence.id);

        Assert.assertTrue(result.matchedCollections.isEmpty());
    }

    @Test
    public void testMultipleSequenceMatch() throws CpeException {
        CollectionSequence sequence = addCollectionSequence();

        CollectionSequenceEntry entry1 = new CollectionSequenceEntry();
        entry1.stopOnMatch = (false);
        CollectionSequenceEntry entry2 = new CollectionSequenceEntry();
        entry2.stopOnMatch = (false);

        sequence.collectionSequenceEntries.add(entry1);
        sequence.collectionSequenceEntries.add(entry2);

        DocumentCollection collection1 =
                createDocumentCollection(matchingNumberCondition);
        DocumentCollection collection2 =
                createDocumentCollection(matchingStringCondition);

        Collection<DocumentCollection> documentCollections = new LinkedList<>();
        documentCollections.add(collection1);
        documentCollections.add(collection2);
        addCollections(entry1, documentCollections);

        ConditionEngineResult result = this.evaluator.evaluate(this.document, sequence.id);

        Assert.assertEquals(2, result.matchedCollections.size());
        Assert.assertTrue(result.matchedCollections.stream()
                .anyMatch(c -> c.getId() == collection1.id));
        Assert.assertTrue(result.matchedCollections.stream()
                .anyMatch(c -> c.getId() == collection2.id));
    }

    @Test
    public void testMultipleSequenceStopOnMatch() throws CpeException {
        CollectionSequence sequence = addCollectionSequence();

        CollectionSequenceEntry entry1 = new CollectionSequenceEntry();
        entry1.stopOnMatch = (true);
        CollectionSequenceEntry entry2 = new CollectionSequenceEntry();
        entry2.stopOnMatch = (false);

        sequence.collectionSequenceEntries.add(entry1);
        sequence.collectionSequenceEntries.add(entry2);

        DocumentCollection collection1 =
                createDocumentCollection(matchingNumberCondition);
        DocumentCollection collection2 =
                createDocumentCollection(matchingStringCondition);
        DocumentCollection collection3 =
                createDocumentCollection(matchingStringCondition);

        Collection<DocumentCollection> documentCollections1 = new LinkedList<>();
        documentCollections1.add(collection1);
        documentCollections1.add(collection2);
        addCollections(entry1, documentCollections1);

        addCollections(entry2, collection3);

        ConditionEngineResult result = this.evaluator.evaluate(this.document, sequence.id);

        Assert.assertEquals(2, result.matchedCollections.size());
        Assert.assertTrue(result.matchedCollections.stream()
                .anyMatch(c -> c.getId() == collection1.id));
        Assert.assertTrue(result.matchedCollections.stream()
                .anyMatch(c -> c.getId() == collection2.id));

    }

    private void addCollections(CollectionSequenceEntry collectionSequenceEntry, DocumentCollection documentCollection) throws CpeException {
        Collection<DocumentCollection> documentCollections = new LinkedList<>();
        documentCollections.add(documentCollection);

        addCollections(collectionSequenceEntry, documentCollections);
    }

    private void addCollections(CollectionSequenceEntry collectionSequenceEntry, Collection<DocumentCollection> documentCollections) throws CpeException {
        Collection<Long> ids = documentCollections.stream().map(u->u.id).collect(Collectors.toList());

        collectionSequenceEntry.collectionIds.addAll(ids);
        for(DocumentCollection documentCollection: documentCollections) {
            when(environmentSnapshot.getCollection(eq(documentCollection.id))).thenReturn(documentCollection);
        }
    }

    @Test
    public void testSingleConditionMissingFieldReported() throws CpeException {
        CollectionSequence sequence = addCollectionSequence();
        CollectionSequenceEntry entry = new CollectionSequenceEntry();
        sequence.collectionSequenceEntries.add(entry);

        DocumentCollection collection =
                createDocumentCollection(notMatchingExistsCondition1);
        addCollections(entry, collection);

        ConditionEngineResult result = this.evaluator.evaluate(this.document, sequence.id);

        Assert.assertEquals(0, result.matchedCollections.size());
        Assert.assertEquals(1, result.unevaluatedConditions.size());
        Assert.assertTrue(ConditionKeyHelper .containsKey(result.unevaluatedConditions, notMatchingExistsCondition1).isPresent());
    }

    @Test
    public void testMultipleCollectionsMissingFieldReported() throws CpeException {
        CollectionSequence sequence = addCollectionSequence();
        CollectionSequenceEntry entry = new CollectionSequenceEntry();

        sequence.collectionSequenceEntries.add(entry);

        DocumentCollection collection =
                createDocumentCollection(notMatchingExistsCondition1);
        DocumentCollection collection2 =
                createDocumentCollection(notMatchingExistsCondition2);

        Collection<DocumentCollection> collections = new LinkedList<>();
        collections.add(collection);
        collections.add(collection2);
        addCollections(entry, collections);

        ConditionEngineResult result = this.evaluator.evaluate(this.document, sequence.id);

        Assert.assertEquals(0, result.matchedCollections.size());
        Assert.assertEquals(2, result.unevaluatedConditions.size());
        Assert.assertTrue(ConditionKeyHelper.containsKey(result.unevaluatedConditions, notMatchingExistsCondition1).isPresent());
        Assert.assertTrue(ConditionKeyHelper.containsKey(result.unevaluatedConditions, notMatchingExistsCondition2).isPresent());
    }

    @Test
    public void testMultipleCollectionSequenceEntryMissingFieldReported() throws CpeException {
        CollectionSequence sequence = addCollectionSequence();
        CollectionSequenceEntry entry1 = new CollectionSequenceEntry();
        CollectionSequenceEntry entry2 = new CollectionSequenceEntry();

        sequence.collectionSequenceEntries.add(entry1);
        sequence.collectionSequenceEntries.add(entry2);

        DocumentCollection collection =
                createDocumentCollection(notMatchingExistsCondition1);
        addCollections(entry1, collection);

        DocumentCollection collection2 =
                createDocumentCollection(notMatchingExistsCondition2);
        addCollections(entry2, collection2);

        ConditionEngineResult result = this.evaluator.evaluate(this.document, sequence.id);

        Assert.assertEquals(0, result.matchedCollections.size());
        Assert.assertEquals(2, result.unevaluatedConditions.size());
        Assert.assertTrue(ConditionKeyHelper.containsKey(result.unevaluatedConditions, notMatchingExistsCondition1).isPresent());
        Assert.assertTrue(ConditionKeyHelper.containsKey(result.unevaluatedConditions,notMatchingExistsCondition2).isPresent());
    }

    @Test
    public void testMatchingConditionReturned() throws CpeException {
        CollectionSequence sequence = addCollectionSequence();
        CollectionSequenceEntry entry = new CollectionSequenceEntry();
        sequence.collectionSequenceEntries.add(entry);

        DocumentCollection collection = createDocumentCollection(matchingNumberCondition);
        addCollections(entry, collection);

        ConditionEngineResult result = this.evaluator.evaluate(this.document, sequence.id);

        Assert.assertEquals(1, result.matchedCollections.size());
        Assert.assertTrue(result.matchedCollections.stream()
                .anyMatch(c -> c.getId() == collection.id));

        final Collection<MatchedCondition> matchedConditions = result.matchedCollections.stream().findFirst().get().getMatchedConditions();
        Assert.assertFalse(matchedConditions.isEmpty());
    }

    @Test
    public void testPolicyInformationReturned() throws CpeException {
        CollectionSequence sequence = addCollectionSequence();
        CollectionSequenceEntry entry = new CollectionSequenceEntry();
        sequence.collectionSequenceEntries.add(entry);

        DocumentCollection collection = createDocumentCollection(matchingNumberCondition);
        collection.policyIds = new HashSet<>(Arrays.asList(1L,2L));
        Policy policy1 = new Policy();
        policy1.id=1L;
        policy1.name="This is policy 1";
        when(environmentSnapshot.getPolicy(eq(1L))).thenReturn(policy1);
        when(environmentSnapshot.getPolicy(eq(2L))).thenReturn(null);

        addCollections(entry, collection);

        ConditionEngineResult result = this.evaluator.evaluate(this.document, sequence.id);

        Assert.assertEquals(1, result.matchedCollections.size());
        Assert.assertTrue(result.matchedCollections.stream()
                .anyMatch(c -> c.getId() == collection.id));
        MatchedCollection matchedCollection = result.matchedCollections.stream().findFirst().get();
        CollectionPolicy collectionPolicy1 = matchedCollection.getPolicies().stream().filter(p -> p.getId()==1L).findFirst().get();
        Assert.assertNotNull(collectionPolicy1);
        Assert.assertEquals(policy1.name, collectionPolicy1.getName());

        CollectionPolicy collectionPolicy2 = matchedCollection.getPolicies().stream().filter(p -> p.getId()==2L).findFirst().get();
        Assert.assertNotNull(collectionPolicy2);
        Assert.assertEquals(CollectionPolicy.unknownPolicyName, collectionPolicy2.getName());


        final Collection<MatchedCondition> matchedConditions = matchedCollection.getMatchedConditions();
        Assert.assertFalse(matchedConditions.isEmpty());
    }


    private CollectionSequence addCollectionSequence(){
        CollectionSequence sequence = new CollectionSequence();
        sequence.id = (Helper.getId());
        when(environmentSnapshot.getSequence(eq(sequence.id))).thenReturn(sequence);

        return sequence;
    }
}