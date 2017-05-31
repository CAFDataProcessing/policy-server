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
package com.github.cafdataprocessing.corepolicy.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.cafdataprocessing.corepolicy.common.*;
import com.github.cafdataprocessing.corepolicy.common.dto.*;
import com.github.cafdataprocessing.corepolicy.common.dto.conditions.*;
import com.github.cafdataprocessing.corepolicy.TestHelper;
import com.github.cafdataprocessing.corepolicy.domainModels.FieldAction;
import com.github.cafdataprocessing.corepolicy.policy.MetadataPolicy.MetadataPolicy;
import com.github.cafdataprocessing.corepolicy.testing.IntegrationTestBase;
import com.github.cafdataprocessing.corepolicy.common.ApiProperties.ApiMode;
import com.github.cafdataprocessing.corepolicy.testing.Assume;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.util.*;

import static com.github.cafdataprocessing.corepolicy.TestHelper.getUniqueString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 *
 */
public class ClassifyDocumentApiIT extends IntegrationTestBase {

    protected ClassifyDocumentApi sut;
    protected ClassificationApi classificationApi;
    protected PolicyApi policyApi;
    protected Policy policy1;
    protected String expectedFieldName;
    protected String expectedFieldValue;
    protected ConditionEngineMetadata conditionEngineMetadata;

    CollectionSequence collSequence_MatchedConditionRecording;
    CollectionSequence collectionSequence;
    Document document;

    public ClassifyDocumentApiIT() {
        sut = genericApplicationContext.getBean(ClassifyDocumentApi.class);
        classificationApi = genericApplicationContext.getBean(ClassificationApi.class);
        policyApi = genericApplicationContext.getBean(PolicyApi.class);
        conditionEngineMetadata = genericApplicationContext.getBean(ConditionEngineMetadata.class);
    }

    @Before
    public void before() {
        {
            MetadataPolicy metadataPolicy = new MetadataPolicy();
            PolicyType policyType = policyApi.retrievePolicyTypeByName("MetadataPolicy");
            expectedFieldName = UUID.randomUUID().toString();
            expectedFieldValue = UUID.randomUUID().toString();
            FieldAction expectedFieldAction = new FieldAction();
            expectedFieldAction.setAction(FieldAction.Action.ADD_FIELD_VALUE);
            expectedFieldAction.setFieldName(expectedFieldName);
            expectedFieldAction.setFieldValue(expectedFieldValue);
            Collection<FieldAction> fieldActions = new ArrayList<>();
            fieldActions.add(expectedFieldAction);

            metadataPolicy.setFieldActions(fieldActions);

            ObjectMapper objectMapper = new ObjectMapper();
            policy1 = new Policy();
            policy1.name = "Policy";
            policy1.details = objectMapper.valueToTree(metadataPolicy);
            policy1.typeId = policyType.id;
            policy1.priority = 100;

            policy1 = policyApi.create(policy1);

            Policy policy2 = new Policy();
            policy2.name = "Policy";
            policy2.details = objectMapper.valueToTree(metadataPolicy);
            policy2.typeId = policyType.id;

            policy2 = policyApi.create(policy2);

            NumberCondition numberCondition = new NumberCondition();
            numberCondition.name = "afield condition 1";
            numberCondition.field = "afield";
            numberCondition.operator = NumberOperatorType.EQ;
            numberCondition.value = 1L;

            DocumentCollection collection1 = new DocumentCollection();
            collection1.name = "Collection 1";

            collection1.policyIds = new HashSet<>();
            collection1.policyIds.add(policy1.id);
            collection1.condition = numberCondition;

            collection1 = classificationApi.create(collection1);

            DocumentCollection collection2 = new DocumentCollection();
            collection2.name = "Collection 2";

            collection2.policyIds = new HashSet<>();
            collection2.policyIds.add(policy2.id);
            collection2.condition = numberCondition;

            collection2 = classificationApi.create(collection2);

            DocumentCollection incompleteMatchCollection = new DocumentCollection();
            incompleteMatchCollection.name = "Collection Incomplete missing field";
            incompleteMatchCollection.description = "Used in ClassifyDocumentApiIT tests.";

            // This is used by later tests which need to setup incomplete/unmatched conditions.
            numberCondition = new NumberCondition();
            numberCondition.name = "MissingNumberfield condition 1";
            numberCondition.field = "MissingNumberField";
            numberCondition.value = 123L;
            numberCondition.operator = NumberOperatorType.EQ;
            incompleteMatchCollection.condition = numberCondition;
            incompleteMatchCollection = classificationApi.create(incompleteMatchCollection);

            collectionSequence = new CollectionSequence();
            collectionSequence.name = getUniqueString("ClassifyDocumentApiIT::setup_");
            collectionSequence.description = "Used in ClassifyDocumentApiIT tests.";
            collectionSequence.collectionSequenceEntries = new ArrayList<>();
            CollectionSequenceEntry collectionSequenceEntry = new CollectionSequenceEntry();
            collectionSequenceEntry.collectionIds = new HashSet<>(Arrays.asList(collection1.id, collection2.id, incompleteMatchCollection.id));
            collectionSequenceEntry.stopOnMatch = false;
            collectionSequenceEntry.order = 400;
            collectionSequence.collectionSequenceEntries.add(collectionSequenceEntry);

            collectionSequence = classificationApi.create(collectionSequence);
        }

        // For matched conditions during unevaluated / incomplete collection tests.
        {
            DocumentCollection incompleteMatchCollection = new DocumentCollection();
            incompleteMatchCollection.name = getUniqueString("ClassifyDocumentApiIT Collection_");
            incompleteMatchCollection.description = "Used in ClassifyDocumentApiIT::testMatchedConditionsAreReturnedEvenOnUnmatchedCollection.";

            // This is used by later tests which need to setup incomplete/unmatched conditions.
            NumberCondition numberCondition = new NumberCondition();
            numberCondition.name = "MissingNumberField condition";
            numberCondition.field = "MissingNumberField";
            numberCondition.value = 123L;
            numberCondition.operator = NumberOperatorType.EQ;

            NumberCondition numberConditionAField = new NumberCondition();
            numberConditionAField.name = "afield condition";
            numberConditionAField.field = "afield";
            numberConditionAField.value = 1L;
            numberConditionAField.operator = NumberOperatorType.EQ;

            BooleanCondition booleanCondition = new BooleanCondition();
            booleanCondition.name = "numberConditionAField and numberCondition";
            booleanCondition.operator = BooleanOperator.AND;
            booleanCondition.children = new ArrayList<>();
            booleanCondition.children.add(numberConditionAField);
            booleanCondition.children.add(numberCondition);

            incompleteMatchCollection.condition = booleanCondition;
            incompleteMatchCollection = classificationApi.create(incompleteMatchCollection);

            CollectionSequence matchedConditionRecordingCollection = new CollectionSequence();
            matchedConditionRecordingCollection.name = getUniqueString("ClassifyDocumentApiIT::tests");
            matchedConditionRecordingCollection.description = "Used in ClassifyDocumentApiIT testMatchedConditionsAreReturnedEvenOnUnmatchedCollection.";
            matchedConditionRecordingCollection.collectionSequenceEntries = new ArrayList<>();

            CollectionSequenceEntry collectionSequenceEntry = new CollectionSequenceEntry();
            collectionSequenceEntry.collectionIds = new HashSet<>(Arrays.asList(incompleteMatchCollection.id));
            collectionSequenceEntry.stopOnMatch = true;
            collectionSequenceEntry.order = 400;
            matchedConditionRecordingCollection.collectionSequenceEntries.add(collectionSequenceEntry);

            // record final collectionsequence
            collSequence_MatchedConditionRecording = classificationApi.create(matchedConditionRecordingCollection);
        }

        document = new DocumentImpl();
        document.setReference(UUID.randomUUID().toString());
        document.getMetadata().put("afield", String.valueOf(1L));
        document.getMetadata().put("MissingNumberField", String.valueOf(123L));
        document.setFullMetadata(true);
    }

    @Test
    public void classifyTest() {

        Collection<ClassifyDocumentResult> classifyDocumentResults = sut.
                classify(collectionSequence.id, Collections.singletonList(document));

        assertEquals(classifyDocumentResults.size(), 1);
        assertEquals(3, classifyDocumentResults.stream().findFirst().get().matchedCollections.size());
    }


    @Test
    public void classifyTestOnStream() {

        Assume.assumeTrue(Assume.AssumeReason.BUG, "classifyTestOnStream", "PD-896", "unable to pass streams over the web interface for now", apiProperties.isInApiMode(ApiMode.direct), genericApplicationContext);

        // Perform the exact match of the classify test above, except using a stream to represent
        // each of our source metadata field for evaluation!
        Document streamDocument = new DocumentImpl();
        streamDocument.setReference(UUID.randomUUID().toString());
        // replace normal doc props with streams.
        // document.getMetadata().put("afield", String.valueOf(1L));
        // document.getMetadata().put("MissingNumberField", String.valueOf(123L));

        streamDocument.getStreams().put("afield", TestHelper.getInputStream(String.valueOf(1L)));
        streamDocument.getStreams().put("MissingNumberField", TestHelper.getInputStream(String.valueOf(123L)));
        streamDocument.setFullMetadata(true);

        Collection<ClassifyDocumentResult> classifyDocumentResults = sut.
                classify(collectionSequence.id, Collections.singletonList(streamDocument));

        assertEquals(classifyDocumentResults.size(), 1);
        assertEquals(3, classifyDocumentResults.stream().findFirst().get().matchedCollections.size());
    }


    @Test
    public void classifyTestNullReference() {

        // Perform the exact match of the classify test above, except using a stream to represent
        // each of our source metadata field for evaluation!
        Document streamDocument = new DocumentImpl();
        streamDocument.setReference(null);

        streamDocument.getMetadata().put("afield", String.valueOf(1L));
        streamDocument.getMetadata().put("MissingNumberField", String.valueOf(123L));
        streamDocument.setFullMetadata(true);

        Collection<ClassifyDocumentResult> classifyDocumentResults = sut.
                classify(collectionSequence.id, Collections.singletonList(streamDocument));

        assertEquals(classifyDocumentResults.size(), 1);
        assertEquals(3, classifyDocumentResults.stream().findFirst().get().matchedCollections.size());
    }


    @Test
    public void classifyTestEmptyReference() {

        // Perform the exact match of the classify test above, except using a stream to represent
        // each of our source metadata field for evaluation!
        Document streamDocument = new DocumentImpl();
        streamDocument.setReference("");

        streamDocument.getMetadata().put("afield", String.valueOf(1L));
        streamDocument.getMetadata().put("MissingNumberField", String.valueOf(123L));
        streamDocument.setFullMetadata(true);

        Collection<ClassifyDocumentResult> classifyDocumentResults = sut.
                classify(collectionSequence.id, Collections.singletonList(streamDocument));

        assertEquals(classifyDocumentResults.size(), 1);
        assertEquals(3, classifyDocumentResults.stream().findFirst().get().matchedCollections.size());
    }

    @Test
    public void testClassifyEvaluationOfSameStreamMultipleTimesReadsAllOfStream() {

        Assume.assumeTrue(Assume.AssumeReason.BUG, "classifyTestOnStream", "PD-896", "unable to pass streams over the web interface for now", apiProperties.isInApiMode(ApiMode.direct), genericApplicationContext);

        // Ensure english works and doesn't throw any longer - PD-431.
        StringCondition condition = new StringCondition();
        condition.name = "test a field for starting.";
        condition.field = "afield";
        condition.value = "later on in the conditions.";
        condition.operator = StringOperatorType.ENDS_WITH;

        StringCondition condition2 = new StringCondition();
        condition2.name = "test afield for ending.";
        condition2.field = "afield";
        condition2.value = "Add some piece of";
        condition2.operator = StringOperatorType.STARTS_WITH;

        BooleanCondition rootCondition = new BooleanCondition();
        rootCondition.name = "test AND strings conditions on afield";
        rootCondition.operator = BooleanOperator.AND;
        rootCondition.children = Arrays.asList(condition, condition2);

        DocumentCollection collection1 = new DocumentCollection();
        collection1.name = "Collection For Test on stream";
        collection1.condition = rootCondition;
        collection1 = classificationApi.create(collection1);

        DocumentCollection collection2 = new DocumentCollection();
        collection2.name = "Collection For Test on streams again!";
        collection2.condition = rootCondition;
        collection2 = classificationApi.create(collection2);

        CollectionSequence collectionSequence = new CollectionSequence();
        collectionSequence.name = "Hello";
        collectionSequence.description = "My description";
        collectionSequence.collectionSequenceEntries = new ArrayList<>();
        CollectionSequenceEntry collectionSequenceEntry = new CollectionSequenceEntry();
        collectionSequenceEntry.collectionIds = new HashSet<>(Arrays.asList(collection1.id, collection2.id));
        collectionSequenceEntry.stopOnMatch = false;
        collectionSequenceEntry.order = 400;
        collectionSequence.collectionSequenceEntries.add(collectionSequenceEntry);
        collectionSequence = classificationApi.create(collectionSequence);

        // Perform the exact match of the classify test above, except using a stream to represent
        // each of our source metadata field for evaluation!
        Document streamDocument = new DocumentImpl();
        streamDocument.setReference(UUID.randomUUID().toString());

        // We want to setup a piece of text, that we can evaluate multiple times, to ensure that it
        // reads the whole stream correctly each time.
        // Test both string conditions
        streamDocument.getStreams().put("afield", TestHelper.getInputStream(String.valueOf("Add some piece of text that I will search for later on in the conditions.")));
        streamDocument.setFullMetadata(true);


        Collection<ClassifyDocumentResult> classifyDocumentResults = sut.
                classify(collectionSequence.id, Collections.singletonList(streamDocument));

        assertEquals(classifyDocumentResults.size(), 1);
        assertEquals(2, classifyDocumentResults.stream().findFirst().get().matchedCollections.size());

        ConditionEngineResult classifyCER = conditionEngineMetadata.createResult(classifyDocumentResults.stream().findFirst().get());
        assertNotNull(classifyCER);

        // ensure the actual matched condition count contains the 2 strings, and the boolean parent. * 2.
        assertEquals("Ensure all our conditions are matched, across both collections.", 6, classifyCER.matchedConditions.size());
    }


    @Test
    public void classifyOnEnglishLangTest() {
        // Ensure english works and doesn't throw any longer - PD-431.
        RegexCondition condition = new RegexCondition();
        condition.field = "afield";
        condition.value = "Some english";

        DocumentCollection collection1 = new DocumentCollection();
        collection1.name = "Collection For English Test";

        collection1.policyIds = new HashSet<>();
        collection1.policyIds.add(policy1.id);
        collection1.condition = condition;

        collection1 = classificationApi.create(collection1);

        collectionSequence = new CollectionSequence();
        collectionSequence.name = "Hello";
        collectionSequence.description = "My description";
        collectionSequence.collectionSequenceEntries = new ArrayList<>();
        CollectionSequenceEntry collectionSequenceEntry = new CollectionSequenceEntry();
        collectionSequenceEntry.collectionIds = new HashSet<>(Arrays.asList(collection1.id));
        collectionSequenceEntry.stopOnMatch = true;
        collectionSequenceEntry.order = 400;
        collectionSequence.collectionSequenceEntries.add(collectionSequenceEntry);
        collectionSequence = classificationApi.create(collectionSequence);

        document = new DocumentImpl();
        document.setReference(UUID.randomUUID().toString());
        document.getMetadata().put("afield", String.valueOf("Some english condition that we need to match on."));
        document.setFullMetadata(true);

        Collection<ClassifyDocumentResult> classifyDocumentResults = sut.
                classify(collectionSequence.id, Collections.singletonList(document));

        assertEquals(classifyDocumentResults.size(), 1);

    }

    @Test
    public void executeTest() {

        Collection<ClassifyDocumentResult> classifyDocumentResults = sut.
                classify(collectionSequence.id, Collections.singletonList(document));

        ClassifyDocumentResult classifyDocumentResult = classifyDocumentResults.stream().
                filter(c -> c.reference.equals(document.getReference())).findFirst().get();

        DocumentToExecutePolicyOn documentToExecutePolicyOn = new DocumentToExecutePolicyOn();
        documentToExecutePolicyOn.document = document;
        documentToExecutePolicyOn.policyIds = classifyDocumentResult.resolvedPolicies;

        Collection<Document> documents = sut.execute(collectionSequence.id, Arrays.asList(documentToExecutePolicyOn));

        assertEquals(documents.size(), 1);
        Document firstDocument = documents.stream().findFirst().get();

        /**
         * This is because in direct mode we operate on the actual document ref, but on web mode this is impossible.
         */
        if ("direct".equalsIgnoreCase(apiProperties.getMode())) {
            assertEquals(document.getMetadata().size(), firstDocument.getMetadata().size());
        } else {
            assertTrue(document.getMetadata().size() < (firstDocument.getMetadata().size()));
        }

        assertTrue(firstDocument.getMetadata().containsKey(expectedFieldName));
        Collection<String> addedMetadata = firstDocument.getMetadata().get(expectedFieldName);
        assertTrue(addedMetadata.contains(expectedFieldValue));
    }

    @Test
    public void testClassifyReturnsExpectedResult_CompleteCollections() {

        Collection<ClassifyDocumentResult> classifyDocumentResults = sut.
                classify(collectionSequence.id, Collections.singletonList(document));

        ClassifyDocumentResult classifyDocumentResult = classifyDocumentResults.stream().
                filter(c -> c.reference.equals(document.getReference())).findFirst().get();

        // we have 3 collections, it should match all 3.
        assertEquals("Classify Document should have all matched collections.", 3, classifyDocumentResult.matchedCollections.size());
        assertEquals("Classify Document should have no incomplete collections.", 0, classifyDocumentResult.incompleteCollections.size());
        assertEquals("Classify Document should have no unevaluated conditions.", 0, classifyDocumentResult.unevaluatedConditions.size());
    }

    @Test
    public void testClassifyReturnsExpectedResult_IncompleteCollections() {

        // use a new document which doesn't have all the fields present.

        Document document = new DocumentImpl();
        document.setReference(UUID.randomUUID().toString());
        document.getMetadata().put("afield", String.valueOf(1L));
        document.setFullMetadata(false);

        Collection<ClassifyDocumentResult> classifyDocumentResults = sut.
                classify(collectionSequence.id, Collections.singletonList(document));

        ClassifyDocumentResult classifyDocumentResult = classifyDocumentResults.stream().
                filter(c -> c.reference.equals(document.getReference())).findFirst().get();

        // we have 3 collections, it should match all 3.
        assertEquals("Classify Document should have all matched collections.", 2, classifyDocumentResult.matchedCollections.size());
        assertEquals("Classify Document should have incomplete collections.", 1, classifyDocumentResult.incompleteCollections.size());
        assertEquals("Classify Document should have unevaluated conditions.", 1, classifyDocumentResult.unevaluatedConditions.size());
    }

    @Test
    public void testClassifyReturnsExpectedResult_UnevalatedConditions() {

        // use a new document which doesn't have all the fields present.
        Document document = new DocumentImpl();
        document.setReference(UUID.randomUUID().toString());
        document.getMetadata().put("afield", String.valueOf(1L));

        // set metadata to true, to ensure it knows difference in incomplete/unevaluated.
        document.setFullMetadata(true);

        Collection<ClassifyDocumentResult> classifyDocumentResults = sut.
                classify(collectionSequence.id, Collections.singletonList(document));

        ClassifyDocumentResult classifyDocumentResult = classifyDocumentResults.stream().
                filter(c -> c.reference.equals(document.getReference())).findFirst().get();

        // we have 3 collections, it should match only the first 2.
        assertEquals("Classify Document should have all matched collections.", 2, classifyDocumentResult.matchedCollections.size());
        assertEquals("Classify Document should have no incomplete collections.", 0, classifyDocumentResult.incompleteCollections.size());
        assertEquals("Classify Document should have no unevaluated conditions.", 0, classifyDocumentResult.unevaluatedConditions.size());
    }

    @Test
    public void testClassifyDocumentReturns_ValidConditionEngineMetadata() {

        // use a new document which doesn't have all the fields present.
        Document document = new DocumentImpl();
        document.setReference(UUID.randomUUID().toString());
        document.getMetadata().put("afield", String.valueOf(1L));

        // set metadata to true, to ensure it knows difference in incomplete/unevaluated.
        document.setFullMetadata(true);

        Collection<ClassifyDocumentResult> classifyDocumentResults = sut.
                classify(collectionSequence.id, Collections.singletonList(document));

        ClassifyDocumentResult classifyDocumentResult = classifyDocumentResults.stream().
                filter(c -> c.reference.equals(document.getReference())).findFirst().get();

        // obtain the metadata from the classifyresult.
        ConditionEngineResult result = conditionEngineMetadata.createResult(classifyDocumentResult);

        assertEquals("Classify Document should have all matched collections.", 2, classifyDocumentResult.matchedCollections.size());
        assertEquals("Classify Document should have no incomplete collections.", 0, classifyDocumentResult.incompleteCollections.size());
        assertEquals("Classify Document should have no unevaluated conditions.", 0, classifyDocumentResult.unevaluatedConditions.size());

        // in web mode we have choosen not to serialize the signature back to the user - this is becuase the pre-evaluation
        // info is only for internal use, and we give no access to create / read this to the consumer so its of no real use
        if (apiProperties.isInApiMode(Arrays.asList(ApiMode.web))) {
            return;
        }

        // Validate the signature / conditionengineresult fields.

        assertEquals("ConditionEngineResult should have all matched collections.", 2, result.matchedCollections.size());
        assertEquals("ConditionEngineResult should have no incomplete collections.", 0, result.incompleteCollections.size());
        assertEquals("ConditionEngineResult should have no unevaluated conditions.", 0, result.unevaluatedConditions.size());
        assertEquals("ConditionEngineResult should have 1 unmatched conditions.", 1, result.unmatchedConditions.size());
    }

    @Test
    public void testConditionEngineMetadataPreventsReevaluation() {

        // in web mode we have chosen not to serialize the signature back to the user - this is because the pre-evaluation
        // info is only for internal use, and we give no access to create / read this to the consumer so its of no real use
        Assume.assumeFalse(Assume.AssumeReason.BY_DESIGN, "testConditionEngineMetadataPreventsReevaluation", null,
                "conditionengineresult cant be used in web mode", apiProperties.isInApiMode(Arrays.asList(ApiMode.web)),
                genericApplicationContext);

        Document newDocument = new DocumentImpl();
        newDocument.setReference(UUID.randomUUID().toString());
        newDocument.getMetadata().put("afield", String.valueOf(1L));
        newDocument.setFullMetadata(false);

        {
            Collection<ClassifyDocumentResult> classifyDocumentResults = sut.
                    classify(collectionSequence.id, Collections.singletonList(newDocument));

            ClassifyDocumentResult classifyDocumentResult = classifyDocumentResults.stream().
                    filter(c -> c.reference.equals(newDocument.getReference())).findFirst().get();

            // we have 3 collections, it should match only the first 2.
            assertEquals("Classify Document should have matched collections.", 2, classifyDocumentResult.matchedCollections.size());
            assertEquals("Classify Document should have incomplete collections.", 1, classifyDocumentResult.incompleteCollections.size());
            assertEquals("Classify Document should have unevaluated conditions.", 1, classifyDocumentResult.unevaluatedConditions.size());

            ConditionEngineResult result = conditionEngineMetadata.createResult(classifyDocumentResult);

            // apply this metadata to the document, regardless of what collections matched status is.
            conditionEngineMetadata.applyTemporaryMetadataToDocument(result, newDocument);

            // Before we remove the fields, ensure we have the correct info present.
            ConditionEngineResult resultOnFirstDoc = conditionEngineMetadata.createResult(classifyDocumentResult);

            assertEquals("ConditionEngineResult should have matched collections.", 2, resultOnFirstDoc.matchedCollections.size());
            assertEquals("ConditionEngineResult should have incomplete collections.", 1, resultOnFirstDoc.incompleteCollections.size());
            assertEquals("ConditionEngineResult should have unevaluated conditions.", 1, resultOnFirstDoc.unevaluatedConditions.size());
            assertEquals("ConditionEngineResult should have no unmatched conditions.", 0, resultOnFirstDoc.unmatchedConditions.size());
        }


        // now we have some metadata ( pre-evaluation info ) on the document,
        // remove the previous field, that was a match.
        Document firstDocument = new DocumentImpl();
        firstDocument.getMetadata().putAll(newDocument.getMetadata());
        firstDocument.getMetadata().removeAll("afield");

        // Also put on a new field so that we match the last condition/collection now.
        firstDocument.getMetadata().put("MissingNumberField", String.valueOf(123L));

        // change indication of all metadata to true!
        firstDocument.setFullMetadata(true);

        // now reclassify this item, with the previous evaluation info present on it.
        Collection<ClassifyDocumentResult> reclassifyResult = sut.
                classify(collectionSequence.id, Collections.singletonList(firstDocument));

        ClassifyDocumentResult reclassifyDocumentResult = reclassifyResult.stream().
                filter(c -> c.reference.equals(firstDocument.getReference())).findFirst().get();

        // Validate that we now have all 3 collections being matched, even though the field was
        // no longer on the document!
        assertEquals("Classify Document should have matched collections.", 3, reclassifyDocumentResult.matchedCollections.size());
        assertEquals("Classify Document should have no incomplete collections.", 0, reclassifyDocumentResult.incompleteCollections.size());
        assertEquals("Classify Document should have no unevaluated conditions.", 0, reclassifyDocumentResult.unevaluatedConditions.size());

        // Get the condition engine result!
        // Before we remove the fields, ensure we have the correct info present.
        ConditionEngineResult reclassifyCER = conditionEngineMetadata.createResult(reclassifyDocumentResult);

        assertEquals("ConditionEngineResult should have matched collections.", 3, reclassifyCER.matchedCollections.size());
        assertEquals("ConditionEngineResult should have no incomplete collections.", 0, reclassifyCER.incompleteCollections.size());
        assertEquals("ConditionEngineResult should have no unevaluated conditions.", 0, reclassifyCER.unevaluatedConditions.size());
        assertEquals("ConditionEngineResult should have no unmatched conditions.", 0, reclassifyCER.unmatchedConditions.size());
    }


    @Test
    public void testMatchedConditionsAreReturnedEvenInIncompleteCollection() {

        // in web mode we have choosen not to serialize the signature back to the user - this is becuase the pre-evaluation
        // info is only for internal use, and we give no access to create / read this to the consumer so its of no real use
        Assume.assumeFalse(Assume.AssumeReason.BY_DESIGN, "testConditionEngineMetadataPreventsReevaluation", null, "conditionengineresult cant be used in web mode", apiProperties.isInApiMode(Arrays.asList(ApiMode.web)), genericApplicationContext);

        // Introduced to cover bug PD302
        // Where in a Boolean Condition we may have evaluated some limbs but then stopped due to
        // unevalated / unmatched.   Unfortunately it is possible for some limbs only to be possible
        // at certain stages in the chain
        // we want them never to be re-evaluated regardless of whether they are in a matched collection or
        // not.

        // create a document which has the first condition satisfied but not the second, leaving it
        // incomplete first time around.
        Document newDocument = new DocumentImpl();
        newDocument.setReference(UUID.randomUUID().toString());
        newDocument.getMetadata().put("afield", String.valueOf(1L));
        // newDocument.getMetadata().put("MissingField", String.valueOf("123L"));
        newDocument.setFullMetadata(false);

        Collection<ClassifyDocumentResult> classifyDocumentResults = sut.
                classify(collSequence_MatchedConditionRecording.id, Collections.singletonList(newDocument));

        ClassifyDocumentResult classifyDocumentResult = classifyDocumentResults.stream().
                filter(c -> c.reference.equals(newDocument.getReference())).findFirst().get();

        // we have 3 collections, it should match only the first 2.
        assertEquals("Classify Document should have matched collections.", 0, classifyDocumentResult.matchedCollections.size());
        assertEquals("Classify Document should have incomplete collections.", 1, classifyDocumentResult.incompleteCollections.size());
        assertEquals("Classify Document should have unevaluated conditions.", 1, classifyDocumentResult.unevaluatedConditions.size());

        ConditionEngineResult cer = conditionEngineMetadata.createResult(classifyDocumentResult);


        // Before we remove the fields, ensure we have the correct info present.
        assertEquals("ConditionEngineResult should have matched collections.", 0, cer.matchedCollections.size());

        assertEquals("ConditionEngineResult should have matched conditions.", 1, cer.matchedConditions.size());
        assertTrue("ConditionEngineResult should have matched condition.", cer.matchedConditions.stream().findFirst().get().getFieldName().equalsIgnoreCase("afield"));

        assertEquals("ConditionEngineResult should have incomplete collections.", 1, cer.incompleteCollections.size());

        assertEquals("ConditionEngineResult should have unevaluated conditions.", 1, cer.unevaluatedConditions.size());
        assertTrue("ConditionEngineResult should have unevaluated condition.", cer.unevaluatedConditions.stream().findFirst().get().reason.equals(UnevaluatedCondition.Reason.MISSING_FIELD));

        assertEquals("ConditionEngineResult should have no unmatched conditions.", 0, cer.unmatchedConditions.size());
    }


    @Test
    public void testMatchedConditionsAreReturnedEvenInUnmatchedCollection() {

        // in web mode we have chosen not to serialize the signature back to the user - this is because the pre-evaluation
        // info is only for internal use, and we give no access to create / read this to the consumer so its of no real use
        Assume.assumeFalse(Assume.AssumeReason.BY_DESIGN, "testConditionEngineMetadataPreventsReevaluation", null,
                "conditionengineresult cant be used in web mode", apiProperties.isInApiMode(Arrays.asList(ApiMode.web)), genericApplicationContext);

        // Introduced to cover bug PD302
        // Where in a Boolean Condition we may have evaluated some limbs but then stopped due to
        // unevalated / unmatched.   Unfortunately it is possible for some limbs only to be possible
        // at certain stages in the chain
        // we want them never to be re-evaluated regardless of whether they are in a matched collection or
        // not.

        // create a document which has the first condition satisfied but not the second, leaving it
        // incomplete first time around.
        Document newDocument = new DocumentImpl();
        newDocument.setReference(UUID.randomUUID().toString());
        newDocument.getMetadata().put("afield", String.valueOf(1L));
        // newDocument.getMetadata().put("MissingField", String.valueOf("123L"));
        newDocument.setFullMetadata(true);

        Collection<ClassifyDocumentResult> classifyDocumentResults = sut.
                classify(collSequence_MatchedConditionRecording.id, Collections.singletonList(newDocument));

        ClassifyDocumentResult classifyDocumentResult = classifyDocumentResults.stream().
                filter(c -> c.reference.equals(newDocument.getReference())).findFirst().get();

        // we have 3 collections, it should match only the first 2.
        assertEquals("Classify Document should have matched collections.", 0, classifyDocumentResult.matchedCollections.size());
        assertEquals("Classify Document should have incomplete collections.", 0, classifyDocumentResult.incompleteCollections.size());
        assertEquals("Classify Document should have unevaluated conditions.", 0, classifyDocumentResult.unevaluatedConditions.size());

        ConditionEngineResult cer = conditionEngineMetadata.createResult(classifyDocumentResult);

        // Before we remove the fields, ensure we have the correct info present.

        assertEquals("ConditionEngineResult should have matched collections.", 0, cer.matchedCollections.size());

        assertEquals("ConditionEngineResult should have matched conditions.", 1, cer.matchedConditions.size());
        assertTrue("ConditionEngineResult should have matched condition.", cer.matchedConditions.stream().findFirst().get().getFieldName().equalsIgnoreCase("afield"));

        assertEquals("ConditionEngineResult should have incomplete collections.", 0, cer.incompleteCollections.size());
        assertEquals("ConditionEngineResult should have unevaluated conditions.", 0, cer.unevaluatedConditions.size());


        assertEquals("ConditionEngineResult should have no unmatched conditions.", 2, cer.unmatchedConditions.size());
    }

    @Test
    public void testClassifyWithKeyviewFieldName() {
        // in web mode we have chosen not to serialize the signature back to the user - this is because the pre-evaluation
        // info is only for internal use, and we give no access to create / read this to the consumer so its of no real use
        Assume.assumeFalse(Assume.AssumeReason.BY_DESIGN, "testConditionEngineMetadataPreventsReevaluation", null,
                "conditionengineresult cant be used in web mode",
                apiProperties.isInApiMode(Arrays.asList(ApiMode.web)), genericApplicationContext);

        Document newDocument = new DocumentImpl();
        newDocument.setReference(UUID.randomUUID().toString());
        newDocument.getMetadata().put("Doc_Class_Code", "20");

        StringCondition condition = new StringCondition();
        condition.field = "Doc_Class_Code";
        condition.value = "20";
        condition.operator = StringOperatorType.IS;
        condition.name = "Keyview field test condition";

        DocumentCollection collection = new DocumentCollection();
        collection.condition = condition;
        collection.name = "Keyview field Test Collection";
        collection = classificationApi.create(collection);

        CollectionSequenceEntry sequenceEntry = new CollectionSequenceEntry();
        sequenceEntry.collectionIds = new HashSet<>();
        sequenceEntry.collectionIds.add(collection.id);
        sequenceEntry.order = 400;

        CollectionSequence sequence = new CollectionSequence();
        sequence.name = "Keyview field Test Collection Sequence";
        sequence.collectionSequenceEntries = new ArrayList<>();
        sequence.collectionSequenceEntries.add(sequenceEntry);
        sequence = classificationApi.create(sequence);
//        DocumentCollection retrievedCollection = classificationApi.retrieveCollections(sequence.collectionSequenceEntries.stream().findFirst().get().collectionIds).stream().findFirst().get();
        ClassifyDocumentResult result = sut.classify(sequence.id, newDocument);
        MatchedCollection matchedCollection = result.matchedCollections.stream().findFirst().get();
        Assert.assertEquals("Should return 1 matched collection", 1, result.matchedCollections.size());
        Assert.assertEquals("Collection returned should match", collection.id, matchedCollection.getId());
        Assert.assertEquals("Should have only matched 1 condition",1, matchedCollection.getMatchedConditions().size());
        Assert.assertEquals("condition should match",collection.condition.id,matchedCollection.getMatchedConditions().stream().findFirst().get().id);

    }

    @Override
    protected Connection getConnectionToClearDown() {
        return null;
    }
}
