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
package com.github.cafdataprocessing.corepolicy.environment;

import com.cedarsoftware.util.DeepEquals;
import com.github.cafdataprocessing.corepolicy.Matchers;
import com.github.cafdataprocessing.corepolicy.common.ClassificationApi;
import com.github.cafdataprocessing.corepolicy.common.EngineProperties;
import com.github.cafdataprocessing.corepolicy.common.EnvironmentSnapshot;
import com.github.cafdataprocessing.corepolicy.common.PolicyApi;
import com.github.cafdataprocessing.corepolicy.common.domainModels.EnvironmentSnapshotImpl;
import com.github.cafdataprocessing.corepolicy.common.dto.*;
import com.github.cafdataprocessing.corepolicy.common.dto.conditions.Condition;
import com.github.cafdataprocessing.corepolicy.common.dto.conditions.FragmentCondition;
import com.github.cafdataprocessing.corepolicy.common.dto.conditions.LexiconCondition;
import com.github.cafdataprocessing.corepolicy.common.dto.conditions.StringCondition;
import com.github.cafdataprocessing.corepolicy.common.exceptions.CpeException;
import com.github.cafdataprocessing.corepolicy.common.shared.CorePolicyObjectMapper;
import com.github.cafdataprocessing.corepolicy.repositories.EnvironmentSnapshotRepository;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.*;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EnvironmentSnapshotApiImplTest {

    @Mock
    EngineProperties engineProperties;
    @Mock
    ClassificationApi classificationApi;
    @Mock
    EnvironmentSnapshotRepository environmentSnapshotRepository;
    @Mock
    PolicyApi policyApi;

    EnvironmentSnapshotApiImpl environmentSnapshotApi;

    EnvironmentSnapshot environmentSnapshot;

    @Before
    public void setUp() throws Exception {
        environmentSnapshotApi = new EnvironmentSnapshotApiImpl(classificationApi, policyApi, environmentSnapshotRepository);
        environmentSnapshot = new EnvironmentSnapshotImpl();
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
      public void testExpiredResultInNewEnvironment(){
        CollectionSequence collectionSequence = new CollectionSequence();
        collectionSequence.id = 1L;
        collectionSequence.lastModified = new DateTime().plusHours(1);
        when(classificationApi.retrieveCollectionSequences(any())).thenReturn(Arrays.asList(collectionSequence));
        EnvironmentSnapshot initializedEnvironmentSnapshot = environmentSnapshotApi.get(1L, new DateTime().minusHours(1),new DateTime().minusHours(1));
        assertNotNull(initializedEnvironmentSnapshot);
    }

    @Test
    public void testUpgradedSnapshotIsRecognisedAsExpired(){
        CollectionSequence collectionSequence = new CollectionSequence();
        collectionSequence.id = 1L;
        collectionSequence.lastModified = new DateTime().plusHours(1);
        when(classificationApi.retrieveCollectionSequences(any())).thenReturn(Arrays.asList(collectionSequence));
        EnvironmentSnapshot initializedEnvironmentSnapshot = environmentSnapshotApi.get(1L, new DateTime().minusHours(1), null);
        assertNotNull(initializedEnvironmentSnapshot);
    }

    @Test
    public void testNotExpiredDoesNotResultInNewEnvironment(){
        CollectionSequence collectionSequence = new CollectionSequence();
        collectionSequence.id = 1L;
        collectionSequence.lastModified = new DateTime().minusHours(2);
        when(classificationApi.retrieveCollectionSequences(any())).thenReturn(Arrays.asList(collectionSequence));
        EnvironmentSnapshot initializedEnvironmentSnapshot = environmentSnapshotApi.get(1L, new DateTime().minusHours(1), collectionSequence.lastModified);
        assertNull(initializedEnvironmentSnapshot);
    }

    @Test
    public void testReturnCollectionSequence() throws CpeException {
        //Set up collectionSequence
        CollectionSequence collectionSequence = setupAddCollectionSequence("Test");
        environmentSnapshot = environmentSnapshotApi.get(1L, null, null);

        CollectionSequence returnedSequence = environmentSnapshot.getSequence(collectionSequence.id);
        Assert.assertTrue(DeepEquals.deepEquals(collectionSequence, returnedSequence));
    }

    @Test
    public void testReturnCollectionSequenceEntries() throws CpeException {
        //Set up collectionSequence
        CollectionSequence collectionSequence = setupAddCollectionSequence("Test");

        //Set up collectionSequenceEntries
        Collection<CollectionSequenceEntry> collectionSequenceEntries = setupAddCollectionSequenceEntries(2, collectionSequence);

//        CollectionSequenceEntry collectionSequenceEntry1 = collectionSequenceEntries.stream().findFirst().get();
//        CollectionSequenceEntry collectionSequenceEntry2 = collectionSequenceEntries.stream().skip(1).findFirst().get();

        environmentSnapshot = environmentSnapshotApi.get(1L, null, null);
        CollectionSequence returnedCollectionSequence = environmentSnapshot.getSequence(collectionSequence.id);
        Assert.assertEquals(2, returnedCollectionSequence.collectionSequenceEntries.size());
    }

    @Test
    public void testReturnCollections() throws CpeException {
        //Set up collectionSequence
        CollectionSequence collectionSequence = setupAddCollectionSequence("Test");

        //Set up collectionSequenceEntries
        Collection<CollectionSequenceEntry> collectionSequenceEntries = setupAddCollectionSequenceEntries(2, collectionSequence);

        CollectionSequenceEntry collectionSequenceEntry1 = collectionSequenceEntries.stream().findFirst().get();
        CollectionSequenceEntry collectionSequenceEntry2 = collectionSequenceEntries.stream().skip(1).findFirst().get();

        //Set up collections
        DocumentCollection documentCollection1 = setupAddDocumentCollection("DocCol1", collectionSequenceEntry1);

        DocumentCollection documentCollection2 = setupAddDocumentCollection("DocCol2", collectionSequenceEntry1);

        DocumentCollection documentCollection3 = setupAddDocumentCollection("DocCol3", collectionSequenceEntry2);

        environmentSnapshot = environmentSnapshotApi.get(1L, null, null);

        assertIsInCache(documentCollection1);
        assertIsInCache(documentCollection2);
        assertIsInCache(documentCollection3);
    }

    @Test
    public void testReturnConditions() throws CpeException {
        //Set up collectionSequence
        CollectionSequence collectionSequence = setupAddCollectionSequence("Test");

        //Set up collectionSequenceEntries
        Collection<CollectionSequenceEntry> collectionSequenceEntries = setupAddCollectionSequenceEntries(2, collectionSequence);

        CollectionSequenceEntry collectionSequenceEntry1 = collectionSequenceEntries.stream().findFirst().get();
        CollectionSequenceEntry collectionSequenceEntry2 = collectionSequenceEntries.stream().skip(1).findFirst().get();

        //Set up collections
        DocumentCollection documentCollection1 = setupAddDocumentCollection("DocCol1", collectionSequenceEntry1);

        DocumentCollection documentCollection2 = setupAddDocumentCollection("DocCol2", collectionSequenceEntry1);

        DocumentCollection documentCollection3 = setupAddDocumentCollection("DocCol3", collectionSequenceEntry2);

        //Set up conditions
        StringCondition StringCondition1 = new StringCondition();
        StringCondition1.field = ("String Field1");
        StringCondition1.name = ("String Condition1");

        setupAddSingleCondition(StringCondition1, documentCollection1);

        StringCondition StringCondition2 = new StringCondition();
        StringCondition2.field = ("String Field2");
        StringCondition2.name = ("String Condition2");

        setupAddSingleCondition(StringCondition2, documentCollection2);

        StringCondition StringCondition3 = new StringCondition();
        StringCondition3.field = ("String Field3");
        StringCondition3.name = ("String Condition3");

        setupAddSingleCondition(StringCondition3, documentCollection3);

        environmentSnapshot = environmentSnapshotApi.get(1L, null, null);

        assertIsInCache(documentCollection1);
        assertIsInCache(documentCollection2);
        assertIsInCache(documentCollection3);
    }

    @Test
    public void testReturnFragmentConditions() throws CpeException {
        //Set up collectionSequence
        CollectionSequence collectionSequence = setupAddCollectionSequence("Test");

        //Set up collectionSequenceEntries
        Collection<CollectionSequenceEntry> collectionSequenceEntries = setupAddCollectionSequenceEntries(1, collectionSequence);
        CollectionSequenceEntry collectionSequenceEntry1 = collectionSequenceEntries.stream().findFirst().get();

        //Set up collections
        DocumentCollection documentCollection1 = setupAddDocumentCollection("DocCol1", collectionSequenceEntry1);

        //Set up conditions
        StringCondition StringCondition1 = new StringCondition();
        StringCondition1.field = ("String Field1");
        StringCondition1.name = ("String Condition1");

        setupAddSingleCondition(StringCondition1);

        FragmentCondition FragmentCondition = new FragmentCondition();
        FragmentCondition.value = (StringCondition1.id);
        FragmentCondition.name = ("Fragment Condition1");

        setupAddSingleCondition(FragmentCondition, documentCollection1);

        environmentSnapshot = environmentSnapshotApi.get(1L, null, null);

        assertIsInCache(StringCondition1);
        assertIsInCache(documentCollection1);
    }

    @Test
    public void testReturnLexicons() throws CpeException {
        //Set up collectionSequence
        CollectionSequence collectionSequence = setupAddCollectionSequence("Test");

        //Set up collectionSequenceEntries
        Collection<CollectionSequenceEntry> collectionSequenceEntries = setupAddCollectionSequenceEntries(1, collectionSequence);
        CollectionSequenceEntry collectionSequenceEntry1 = collectionSequenceEntries.stream().findFirst().get();

        //Set up collections
        DocumentCollection documentCollection1 = setupAddDocumentCollection("DocCol1", collectionSequenceEntry1);

        //Set up lexicons
        Lexicon lexicon = setupAddLexicon("Lexicon1");

        //Set up lexicon expressions
        Collection<LexiconExpression> lexiconExpressions = setupAddLexiconExpressions(3, lexicon);

        //Set up conditions
        LexiconCondition lexiconCondition = new LexiconCondition();
        lexiconCondition.value = (lexicon.id);
        lexiconCondition.name = ("Lexicon Condition1");

        setupAddSingleCondition(lexiconCondition, documentCollection1);

        environmentSnapshot = environmentSnapshotApi.get(1L, null, null);

        assertIsInCache(documentCollection1);

        assertIsInCache(lexicon);
    }

    @Test
    public void testReturnPolicy() throws CpeException {
        //Set up collectionSequence
        CollectionSequence collectionSequence = setupAddCollectionSequence("Test");
        collectionSequence.id = 1L;

        //Set up collectionSequenceEntries
        Collection<CollectionSequenceEntry> collectionSequenceEntries = setupAddCollectionSequenceEntries(1, collectionSequence);
        CollectionSequenceEntry collectionSequenceEntry1 = collectionSequenceEntries.stream().findFirst().get();

        //Set up collections
        DocumentCollection documentCollection1 = setupAddDocumentCollection("DocCol1", collectionSequenceEntry1);

        //Set up policy
        PolicyType policyType = setupAddPolicyType("PolicyType1");
        Policy policy = setupAddPolicy("Policy1", policyType, documentCollection1);

        environmentSnapshot = environmentSnapshotApi.get(1L, null, null);

        assertIsInCache(policy);
    }

    @Test
    public void testReturnPolicyType() throws CpeException {
        //Set up collectionSequence
        CollectionSequence collectionSequence = setupAddCollectionSequence("Test");
        collectionSequence.id = 1L;

        //Set up collectionSequenceEntries
        Collection<CollectionSequenceEntry> collectionSequenceEntries = setupAddCollectionSequenceEntries(1, collectionSequence);
        CollectionSequenceEntry collectionSequenceEntry1 = collectionSequenceEntries.stream().findFirst().get();

        //Set up collections
        DocumentCollection documentCollection1 = setupAddDocumentCollection("DocCol1", collectionSequenceEntry1);

        //Set up policy
        PolicyType policyType = setupAddPolicyType("PolicyType1");
        Policy policy = setupAddPolicy("Policy1", policyType, documentCollection1);

        environmentSnapshot = environmentSnapshotApi.get(1L, null, null);

        assertIsInCache(policyType);
        assertIsInCache(policy);
    }

    //todo test labels and educiton

    //region DataSetup methods

    private long collectionSequenceId = 0;
    private CollectionSequence setupAddCollectionSequence(String name) throws CpeException {
        CollectionSequence collectionSequence = new CollectionSequence();
        collectionSequence.id = (++collectionSequenceId);
        collectionSequence.name = (name);
        collectionSequence.lastModified = new DateTime().minusHours(1);

        when(classificationApi.retrieveCollectionSequences(Matchers.collectionMatches(Arrays.asList(collectionSequence.id))))
                .thenReturn(Arrays.asList(collectionSequence));

        return collectionSequence;
    }

    //    private long collectionSequenceEntryId = 0;
    private Collection<CollectionSequenceEntry> setupAddCollectionSequenceEntries(int numberToAdd, CollectionSequence parentCollectionSequence) throws CpeException {
        for (int i = 0; i<numberToAdd; i++){
            CollectionSequenceEntry collectionSequenceEntry = new CollectionSequenceEntry();
//            collectionSequenceEntry.setId(++collectionSequenceEntryId);
//            collectionSequenceEntry.setSequenceId(parentCollectionSequence.getId());
            collectionSequenceEntry.order = (short)i;
            parentCollectionSequence.collectionSequenceEntries.add(collectionSequenceEntry);
        }

//        when(collectionSequenceEntryRepository.findAll(eq(parentCollectionSequence.getId()))).thenReturn(collectionSequenceEntries);

        return parentCollectionSequence.collectionSequenceEntries;
    }

    private DocumentCollection setupAddDocumentCollection(String name, CollectionSequenceEntry collectionSequenceEntry) throws CpeException {
        DocumentCollection documentCollection = setupAddDocumentCollection(name);

        collectionSequenceEntry.collectionIds.add(documentCollection.id);
        return documentCollection;
    }

    private long conditionId = 0;
    private void setupAddSingleCondition(Condition condition) throws CpeException {
        Collection<Condition> conditions = new LinkedList<>();

        condition.id = (++conditionId);
        conditions.add(condition);

        when(classificationApi.retrieveConditions(Matchers.collectionMatches(Arrays.asList(condition.id)), any())).thenReturn(conditions);
    }

    private void setupAddSingleCondition(Condition condition, DocumentCollection documentCollection) throws CpeException {
        setupAddSingleCondition(condition);

        documentCollection.condition = condition;
    }

    private long documentCollectionId = 0;
    private DocumentCollection setupAddDocumentCollection(String name) throws CpeException {
        DocumentCollection documentCollection = new DocumentCollection();
        documentCollection.id = (++documentCollectionId);
        documentCollection.name = (name);
        documentCollection.policyIds = new HashSet<>();

        when(classificationApi.retrieveCollections(Matchers.collectionMatches(Arrays.asList(documentCollection.id)))).thenReturn(Arrays.asList(documentCollection));
        when(classificationApi.retrieveCollections(Matchers.collectionMatches(Arrays.asList(documentCollection.id)), anyBoolean(), anyBoolean()))
                .thenReturn(Arrays.asList(documentCollection));
        return documentCollection;
    }

    private long lexiconId = 0;
    private Lexicon setupAddLexicon(String name) throws CpeException {
        Lexicon lexicon = new Lexicon();
        lexicon.id = (++lexiconId);
        lexicon.name = (name);
        lexicon.lexiconExpressions = new ArrayList<>();

        when(classificationApi.retrieveLexicons(Matchers.collectionMatches(Arrays.asList(lexicon.id)))).thenReturn(Arrays.asList(lexicon));

        return lexicon;
    }

    private long policyId = 0;
    private Policy setupAddPolicy(String name, PolicyType policyType, DocumentCollection collection) throws CpeException {
        Policy policy = new Policy();
        policy.id = (++policyId);
        policy.name = (name);
        policy.typeId = policyType.id;
        policy.details = (new CorePolicyObjectMapper()).createObjectNode();

        collection.policyIds.add(policy.id);

        when(policyApi.retrievePolicy(policy.id)).thenReturn(policy);

        return policy;
    }

    private long policyTypeId = 0;
    private PolicyType setupAddPolicyType(String name) throws CpeException {
        PolicyType policyType = new PolicyType();
        policyType.id = (++policyTypeId);
        policyType.name = (name);
        policyType.definition = (new CorePolicyObjectMapper()).createObjectNode();

        when(policyApi.retrievePolicyType(policyType.id)).thenReturn(policyType);

        return policyType;
    }

    private long lexiconExpressionId = 0;
    private Collection<LexiconExpression> setupAddLexiconExpressions(int numberToAdd, Lexicon parentLexicon) throws CpeException {
        for (int i = 0; i<numberToAdd; i++){
            LexiconExpression lexiconExpression = new LexiconExpression();
            lexiconExpression.id= (++lexiconExpressionId);
            lexiconExpression.lexiconId = (parentLexicon.id);
            lexiconExpression.expression = ("Lexicon Expression" + parentLexicon.id);

            parentLexicon.lexiconExpressions.add(lexiconExpression);
        }

//        when(lexiconExpressionRepository.findAll(eq(parentLexicon.getId()))).thenReturn(lexiconExpressions);

        return parentLexicon.lexiconExpressions;
    }
    //endregion

    private void assertIsInCache(DocumentCollection documentCollection){
        Assert.assertTrue(DeepEquals.deepEquals(documentCollection, environmentSnapshot.getCollection(documentCollection.id)));
    }

    private void assertIsInCache(Condition condition){
        Assert.assertTrue(DeepEquals.deepEquals(condition, environmentSnapshot.getCondition(condition.id)));
    }

    private void assertIsInCache(Lexicon lexicon){
        Assert.assertTrue(DeepEquals.deepEquals(lexicon, environmentSnapshot.getLexicon(lexicon.id)));
    }

    private void assertIsInCache(Policy policy){
        Assert.assertTrue(DeepEquals.deepEquals(policy, environmentSnapshot.getPolicy(policy.id)));
    }

    private void assertIsInCache(PolicyType policyType){
        Assert.assertTrue(DeepEquals.deepEquals(policyType, environmentSnapshot.getPolicyType(policyType.id)));
    }
}