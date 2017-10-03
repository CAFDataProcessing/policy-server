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
package com.github.cafdataprocessing.corepolicy.api;

import com.cedarsoftware.util.DeepEquals;
import com.github.cafdataprocessing.corepolicy.common.*;
import com.github.cafdataprocessing.corepolicy.common.dto.*;
import com.github.cafdataprocessing.corepolicy.common.dto.conditions.*;
import com.github.cafdataprocessing.corepolicy.TestHelper;
import com.github.cafdataprocessing.corepolicy.testing.Assume;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;
import java.util.stream.Collectors;

import static com.github.cafdataprocessing.corepolicy.TestHelper.getUniqueString;
import static com.github.cafdataprocessing.corepolicy.TestHelper.shouldThrow;
import static org.junit.Assert.*;

/**
 * Tests for the condition API that are related to conditions, other classes may still test conditions.
 */
public class ClassificationApiConditionIT extends ClassificationApiTestBase {
    @Test
    public void testAddConditionToNot() throws Exception {
        NotCondition notCondition = new NotCondition();
        notCondition.name = "My not condition fragment";

        notCondition = sut.create(notCondition);

        ExistsCondition existsCondition = new ExistsCondition();
        existsCondition.field = "my field";
        existsCondition.parentConditionId = notCondition.id;

        existsCondition = sut.create(existsCondition);

        notCondition = (NotCondition) sut.retrieveConditions(Arrays.asList(notCondition.id), true).stream().findFirst().get();
        assertNotNull(notCondition.condition);
        assertEquals(existsCondition.id, notCondition.condition.id);
    }

    @Test
    public void testAddConditionToNotTogether() throws Exception {
        NotCondition notCondition = new NotCondition();
        notCondition.name = "My not condition fragment";


        ExistsCondition existsCondition = new ExistsCondition();
        existsCondition.field = "my field";
        notCondition.condition = existsCondition;


        notCondition = sut.create(notCondition);

        notCondition = (NotCondition) sut.retrieveConditions(Arrays.asList(notCondition.id), true).stream().findFirst().get();
        assertNotNull(notCondition.condition);
    }

    @Test
    public void testRetrieveNotWithoutChildren() throws Exception {
        NotCondition notCondition = new NotCondition();
        notCondition.name = "My not condition fragment";


        ExistsCondition existsCondition = new ExistsCondition();
        existsCondition.field = "my field";
        notCondition.condition = existsCondition;


        notCondition = sut.create(notCondition);

        notCondition = (NotCondition) sut.retrieveConditions(Arrays.asList(notCondition.id), false).stream().findFirst().get();
        assertNull(notCondition.condition);
    }

    @Test
    public void testRetrieveBooleanWithoutChildren() throws Exception {
        BooleanCondition booleanCondition1 = new BooleanCondition();
        booleanCondition1.operator = BooleanOperator.AND;
        booleanCondition1.name = "Bool1";
        booleanCondition1.children = new LinkedList<>();

        BooleanCondition booleanCondition2 = new BooleanCondition();
        booleanCondition2.operator = BooleanOperator.AND;
        booleanCondition2.name = "Bool2";
        booleanCondition2.children = new LinkedList<>();
        booleanCondition1.children.add(booleanCondition2);

        ExistsCondition existsCondition = new ExistsCondition();
        existsCondition.field = "my field";
        booleanCondition2.children.add(existsCondition);


        booleanCondition1 = sut.create(booleanCondition1);

        BooleanCondition resultCondition = (BooleanCondition) sut.retrieveConditions(Arrays.asList(booleanCondition1.id), false).stream().findFirst().get();
        assertNull(resultCondition.children);
    }

    @Test
    public void testCreateBooleanWithoutChildrenMatches() throws Exception {
        BooleanCondition booleanCondition1 = new BooleanCondition();
        booleanCondition1.operator = BooleanOperator.AND;
        booleanCondition1.name = "Bool1";
        booleanCondition1.children = new LinkedList<>();

        ClassifyDocumentApi cApi = genericApplicationContext.getBean(ClassifyDocumentApi.class);
        Document newDocument = new DocumentImpl();
        newDocument.setReference(UUID.randomUUID().toString());
        newDocument.getMetadata().put("afield", String.valueOf(1L));
        newDocument.setFullMetadata(false);

        DocumentCollection collection1 = new DocumentCollection();
        collection1.name = "Collection 1";

        collection1.policyIds = new HashSet<>();
        collection1.condition = booleanCondition1;
        collection1 = sut.create(collection1);

        CollectionSequence collectionSequence = new CollectionSequence();
        collectionSequence.name = getUniqueString("ClassificationApiCondition::testCreateBooleanWithoutChildrenMatches");
        collectionSequence.description = "Used in ClassificationApiConditionIT tests.";
        collectionSequence.collectionSequenceEntries = new ArrayList<>();
        CollectionSequenceEntry collectionSequenceEntry = new CollectionSequenceEntry();
        collectionSequenceEntry.collectionIds = new HashSet<>(Arrays.asList(collection1.id));
        collectionSequenceEntry.stopOnMatch = false;
        collectionSequenceEntry.order = 400;
        collectionSequence.collectionSequenceEntries.add(collectionSequenceEntry);

        collectionSequence = sut.create(collectionSequence);

        Collection<ClassifyDocumentResult> classifyDocumentResults = cApi.classify(collectionSequence.id, Collections.singletonList(newDocument));
        Assert.assertTrue("Should match collection with no child conditions", classifyDocumentResults.stream().findFirst().get().matchedCollections.size() == 1);
    }

    @Test
    public void testRetrieveBooleanWithChildren() throws Exception {
        BooleanCondition booleanCondition1 = new BooleanCondition();
        booleanCondition1.operator = BooleanOperator.AND;
        booleanCondition1.name = "Bool1";
        booleanCondition1.children = new LinkedList<>();

        BooleanCondition booleanCondition2 = new BooleanCondition();
        booleanCondition2.operator = BooleanOperator.AND;
        booleanCondition2.name = "Bool2";
        booleanCondition2.children = new LinkedList<>();
        booleanCondition1.children.add(booleanCondition2);

        ExistsCondition existsCondition = new ExistsCondition();
        existsCondition.field = "my field";
        booleanCondition2.children.add(existsCondition);

        booleanCondition1 = sut.create(booleanCondition1);

        BooleanCondition resultCondition = (BooleanCondition) sut.retrieveConditions(Arrays.asList(booleanCondition1.id), true).stream().findFirst().get();
        assertEquals(1, resultCondition.children.size());
        assertEquals(1, ((BooleanCondition) resultCondition.children.stream().findFirst().get()).children.size());
    }

    @Test
    public void testRetrieveBooleanWithChildrenOrderedCorrectly() {
        BooleanCondition booleanCondition1 = new BooleanCondition();
        booleanCondition1.operator = BooleanOperator.AND;
        booleanCondition1.name = "Bool1";
        booleanCondition1.children = new ArrayList<>();

        ExistsCondition existsCondition = new ExistsCondition();
        existsCondition.field = "my field1";
        booleanCondition1.children.add(existsCondition);

        ExistsCondition existsCondition2 = new ExistsCondition();
        existsCondition2.field = "my field2";
        booleanCondition1.children.add(existsCondition2);

        BooleanCondition savedCondition = sut.create(booleanCondition1);

        BooleanCondition resultCondition = (BooleanCondition) sut.retrieveConditions(Arrays.asList(savedCondition.id), true).stream().findFirst().get();

        assertEquals(booleanCondition1.children.size(), resultCondition.children.size());
        checkChildOrdering(booleanCondition1.children, resultCondition.children);
    }

    @Test
    public void testRetrieveBooleanWithChildrenDefaultOrdering() {
        DocumentCollection incompleteMatchCollection = new DocumentCollection();
        List<Condition> expected = new ArrayList<>();

        // construct the expected results, list.
        {
            NumberCondition numberCondition = new NumberCondition();
            numberCondition.field = "MissingNumberField";
            numberCondition.value = 123L;
            numberCondition.order = 200;
            numberCondition.operator = NumberOperatorType.EQ;

            NumberCondition numberConditionAField = new NumberCondition();
            numberConditionAField.field = "afield";
            numberConditionAField.value = 1L;
            numberConditionAField.order = 100;
            numberConditionAField.operator = NumberOperatorType.EQ;

            expected.add(numberConditionAField);
            expected.add(numberCondition);
        }

        // now build the real items, without any order field!
        {
            incompleteMatchCollection.name = getUniqueString("ClassificationAPIConditionIT Collection_");
            incompleteMatchCollection.description = "Used in ClassificationAPIConditionIT::testOrdering.";

            // This is used by later tests which need to setup incomplete/unmatched conditions.
            NumberCondition numberCondition = new NumberCondition();
            numberCondition.field = "MissingNumberField";
            numberCondition.value = 123L;
            numberCondition.operator = NumberOperatorType.EQ;

            NumberCondition numberConditionAField = new NumberCondition();
            numberConditionAField.field = "afield";
            numberConditionAField.value = 1L;
            numberConditionAField.operator = NumberOperatorType.EQ;

            BooleanCondition booleanCondition = new BooleanCondition();
            booleanCondition.operator = BooleanOperator.AND;
            booleanCondition.children = new ArrayList<>();
            booleanCondition.children.add(numberConditionAField);
            booleanCondition.children.add(numberCondition);

            incompleteMatchCollection.condition = booleanCondition;
        }

        DocumentCollection saved = sut.create(incompleteMatchCollection);
        DocumentCollection returned = sut.retrieveCollections(Arrays.asList(saved.id), true, true).stream().findFirst().get();

        checkChildOrdering(expected, ((BooleanCondition) returned.condition).children);
    }

    @Test
    public void testDefaultOrderingLotsOfTimes(){
        TestHelper.runTestMultipleTimes(r -> testRetrieveBooleanWithChildrenDefaultOrdering(), 20);
    }

    @Test
    public void testOrderLotsOfTimes(){
        TestHelper.runTestMultipleTimes(r -> testRetrieveBooleanWithChildrenOrderedCorrectly(), 20);
    }

    @Test
    public void testOrderOnChildrenLotsOfTimes(){
        TestHelper.runTestMultipleTimes(r -> testRetrieveBooleanWithChildrenOrderedCorrectlyWithOrderField(), 20);
    }

    public void testRetrieveBooleanWithChildrenOrderedCorrectlyWithOrderField() {

        {
            BooleanCondition booleanCondition1 = new BooleanCondition();
            booleanCondition1.operator = BooleanOperator.AND;
            booleanCondition1.name = "Bool1";
            booleanCondition1.children = new ArrayList<>();

            ExistsCondition existsCondition = new ExistsCondition();
            existsCondition.field = "my field1";
            existsCondition.order = 100;
            booleanCondition1.children.add(existsCondition);

            ExistsCondition existsCondition2 = new ExistsCondition();
            existsCondition2.field = "my field2";
            existsCondition2.order = 200;
            booleanCondition1.children.add(existsCondition2);

            List<Condition> expectedResults = new ArrayList<>();
            expectedResults.add(existsCondition);
            expectedResults.add(existsCondition2);

            BooleanCondition savedCondition = sut.create(booleanCondition1);

            BooleanCondition resultCondition = (BooleanCondition) sut.retrieveConditions(Arrays.asList(savedCondition.id), true).stream().findFirst().get();

            assertEquals(booleanCondition1.children.size(), resultCondition.children.size());
            checkChildOrdering(expectedResults, resultCondition.children);
        }

        // do the same, but now place the children onto the list in reverse order, and ensure
        // that they actually get returned in the correct order!
        BooleanCondition booleanCondition1 = new BooleanCondition();
        booleanCondition1.operator = BooleanOperator.AND;
        booleanCondition1.name = "Bool1";
        booleanCondition1.children = new ArrayList<>();

        ExistsCondition existsCondition = new ExistsCondition();
        existsCondition.field = "my field1";
        existsCondition.order = 400;


        ExistsCondition existsCondition2 = new ExistsCondition();
        existsCondition2.field = "my field2";
        existsCondition2.order = 300;

        booleanCondition1.children.add(existsCondition);
        booleanCondition1.children.add(existsCondition2);

        List<Condition> expectedResults = new ArrayList<>();
        expectedResults.add(existsCondition2);
        expectedResults.add(existsCondition);

        Condition savedCollection = sut.create(booleanCondition1);

        BooleanCondition resultCondition = (BooleanCondition) sut.retrieveConditions(Arrays.asList(savedCollection.id), true).stream().findFirst().get();

        assertEquals(booleanCondition1.children.size(), resultCondition.children.size());
        checkChildOrdering(expectedResults, resultCondition.children);
    }

    private void checkChildOrdering(List<Condition> expected, List<Condition> resultCondition) {
        for ( int index=0; index<expected.size(); index++) {
            Condition original = expected.get(index);
            Condition returned = resultCondition.get(index);

            if (original instanceof FieldCondition) {
                assertEquals("Original name must equal returned field: ", ((FieldCondition)original).field,  ((FieldCondition) returned).field);
            }
            else {
                // order by name.
                assertEquals("Original name must equal returned name: ", original.name, returned.name);
            }

            // ensure that the order field if present in both matches.
            if ( original.order != null )
            {
                assertEquals("Original order must equal returned order: ", original.order, returned.order);
            }
        }
    }

    @Test
    public void testAddConditionToBooleanTogether() throws Exception {
        BooleanCondition booleanCondition = new BooleanCondition();
        booleanCondition.name = "My not condition fragment";
        booleanCondition.operator = BooleanOperator.AND;
        booleanCondition.children = new ArrayList<>();
        {
            ExistsCondition existsCondition = new ExistsCondition();
            existsCondition.field = "my field";
            booleanCondition.children.add(existsCondition);
        }
        {
            ExistsCondition existsCondition = new ExistsCondition();
            existsCondition.field = "my field1";
            booleanCondition.children.add(existsCondition);
        }

        booleanCondition = sut.create(booleanCondition);

        booleanCondition = (BooleanCondition) sut.retrieveConditions(Arrays.asList(booleanCondition.id), true).stream().findFirst().get();
        assertNotNull(booleanCondition.children);
        assertEquals(2, booleanCondition.children.size());

        {
            ExistsCondition existsCondition = new ExistsCondition();
            existsCondition.field = "my field2";
            existsCondition.parentConditionId = booleanCondition.id;

            sut.create(existsCondition);
        }

        booleanCondition = (BooleanCondition) sut.retrieveConditions(Arrays.asList(booleanCondition.id), true).stream().findFirst().get();
        assertNotNull(booleanCondition.children);
        assertEquals(3, booleanCondition.children.size());
    }


    @Test
    public void testUpdateNotCondition() {
        ExistsCondition existsCondition = new ExistsCondition();
        existsCondition.field = "Field1";

        NotCondition notCondition = new NotCondition();
        notCondition.condition = existsCondition;

        NotCondition createdNotCondition = sut.create(notCondition);

        Condition retrievedExistsCondition = sut.retrieveConditions(Arrays.asList(createdNotCondition.condition.id), false).stream().findFirst().get();

        ExistsCondition existsCondition2 = new ExistsCondition();
        existsCondition2.field = "Field2";

        createdNotCondition.condition = existsCondition2;

        NotCondition updatedNotCondition = sut.update(createdNotCondition);

        assertNotNull(updatedNotCondition.condition);
        assertTrue(updatedNotCondition.condition instanceof ExistsCondition);
        assertEquals(existsCondition2.field, ((ExistsCondition) updatedNotCondition.condition).field);

        boolean threw = false;
        try {
            //Old condition is deleted
            Collection<Condition> conditions = sut.retrieveConditions(Arrays.asList(retrievedExistsCondition.id), false);
            assertEquals(0, conditions.size());
        } catch (RuntimeException e) {
            threw = true;
        }
        assertTrue(threw);
    }

    @Test
    public void testUpdateCollectionWithConditions() {
        ExistsCondition existsCondition = new ExistsCondition();
        existsCondition.field = "Field1";

        NotCondition notCondition = new NotCondition();
        notCondition.condition = existsCondition;

        DocumentCollection documentCollection = new DocumentCollection();
        documentCollection.name = "Collection";
        documentCollection.condition = notCondition;

        DocumentCollection createdDocumentCollection = sut.create(documentCollection);
        assertNotNull(createdDocumentCollection.condition);

        Long createdNotConditionId = createdDocumentCollection.condition.id;
        Long createdExistsCondition = ((NotCondition) createdDocumentCollection.condition).condition.id;

        sut.retrieveConditions(Arrays.asList(createdNotConditionId), false).stream().findFirst().get();

        //Update with a new condition
        ExistsCondition existsCondition2 = new ExistsCondition();
        existsCondition2.field = "Field2";
        createdDocumentCollection.condition = existsCondition2;

        {
            DocumentCollection retrievedCollection = sut.retrieveCollections(Arrays.asList(createdDocumentCollection.id), true, true).stream().findFirst().get();
        }

        sut.update(createdDocumentCollection);

        DocumentCollection retrievedCollection = sut.retrieveCollections(Arrays.asList(createdDocumentCollection.id), true, true).stream().findFirst().get();

        assertNotNull(retrievedCollection.condition);
        assertEquals("Field2", ((ExistsCondition) retrievedCollection.condition).field);

        //Make sure the initial conditions have been removed
        boolean threw = false;
        try {
            sut.retrieveConditions(Arrays.asList(createdNotConditionId), false);
            sut.retrieveConditions(Arrays.asList(createdExistsCondition), false);
        } catch (RuntimeException e) {
            threw = true;
        }
        assertTrue(threw);
    }

    @Test
    public void testCreateWithTarget() throws Exception {
        StringCondition stringCondition = new StringCondition();
        stringCondition.name = "My string condition fragment";
        stringCondition.operator = StringOperatorType.CONTAINS;
        stringCondition.value = "this";
        stringCondition.field = "field";
        stringCondition.target = ConditionTarget.CHILDREN;

        StringCondition created = sut.create(stringCondition);
        assertEqualsCondition(stringCondition, created);
    }

    @Test
    public void testUpdateFragment() throws Exception {
        StringCondition stringCondition = new StringCondition();
        stringCondition.name = "My string condition fragment";
        stringCondition.operator = StringOperatorType.CONTAINS;
        stringCondition.value = "this";
        stringCondition.field = "field";

        StringCondition created = sut.create(stringCondition);
        assertEqualsCondition(stringCondition, created);
        created.name = "New name";
        created.operator = StringOperatorType.ENDS_WITH;
        created.value = "new value";
        created.field = "different field";
        StringCondition updated = sut.update(created);
        assertEqualsCondition(created, updated);
    }


    // PD-710 / PD-588
    @Test
    public void testReParentBooleanCondition(){
        DocumentCollection collection = new DocumentCollection();
        collection.name = "name Citroên";
        collection.description = "description Citroên";

        ExistsCondition existsCondition = new ExistsCondition();
        existsCondition.field = "field Citroên";

        BooleanCondition booleanCondition = new BooleanCondition();
        booleanCondition.name = "Test move parent";
        booleanCondition.operator = BooleanOperator.OR;

        BooleanCondition topmostCondition = new BooleanCondition();
        topmostCondition.name = "test move on topmost parent";
        topmostCondition.operator = BooleanOperator.OR;


        topmostCondition.children = new ArrayList<>();
        booleanCondition.children = new ArrayList<>();

        topmostCondition.children.add(booleanCondition);
        booleanCondition.children.add(existsCondition);

        topmostCondition = sut.create(topmostCondition);

        BooleanCondition booleanConditionDest = new BooleanCondition();
        booleanConditionDest.name = "Test move to this boolean parent";
        booleanConditionDest.operator = BooleanOperator.OR;
        booleanConditionDest.children = new ArrayList<>();
        ExistsCondition existsCondition2 = new ExistsCondition();
        existsCondition2.field = "simple exists - to check for addition to boolean";
        booleanConditionDest.children.add(existsCondition);

        booleanConditionDest = sut.create(booleanConditionDest);

        Assert.assertTrue(booleanConditionDest.id > 0);
        Assert.assertTrue(topmostCondition.conditionType == ConditionType.BOOLEAN);

        booleanCondition = (BooleanCondition) topmostCondition.children.stream().findFirst().get();
        Condition booleanToChange = booleanCondition;
        //Condition booleanToChange = booleanCondition.children.stream().findFirst().get();

        // Bring back whole tree to make sure we have static version of the details and check it is what we expect.
        {

            Collection<Condition> results = sut.retrieveConditions(Arrays.asList(topmostCondition.id), true);

            Assert.assertEquals("Must return top most condition", 1, results.size());
            Condition result = results.stream().findFirst().get();
            Assert.assertEquals("Must return next boolean condition", 1, ((BooleanCondition) result).children.size());
            Assert.assertEquals("Must return next boolean condition id", booleanCondition.id, ((BooleanCondition) result).children.stream().findFirst().get().id);
        }

        // check the destination boolean has only 1 exists child at present.
        {

            Collection<Condition> results = sut.retrieveConditions(Arrays.asList(booleanConditionDest.id), true);

            Assert.assertEquals("Must return destination condition", 1, results.size());
            Condition result = results.stream().findFirst().get();
            Assert.assertEquals("Must return children already on dest condition", 1, ((BooleanCondition) result).children.size());
            Assert.assertEquals("Must return next boolean condition id", existsCondition2.name, ((BooleanCondition) result).children.stream().findFirst().get().name);
        }

        // Reparent the saved boolean child condition, onto this item
        Long childOrigId = booleanToChange.id;

        booleanToChange.parentConditionId = booleanConditionDest.id;
        booleanToChange.order = null;

        Condition updated = sut.update(booleanToChange);

        Assert.assertEquals("ID after update should remain unchanged.", updated.id, childOrigId);
        {
            Collection<Condition> results = sut.retrieveConditions(Arrays.asList(topmostCondition.id), true);

            Assert.assertEquals("Must return top most condition", 1, results.size());
            Condition result = results.stream().findFirst().get();
            Assert.assertEquals("Must return no boolean condition", 0, ((BooleanCondition) result).children.size());
        }


        // check the updated boolean child which has been moved, still has its own child
        {
            Collection<Condition> results = sut.retrieveConditions(Arrays.asList(updated.id), true);
            Assert.assertEquals("Must have 1 condition result", 1, results.size());
        }


        // check destination parent now has both children present on it.
        {
            Collection<Condition> results = sut.retrieveConditions(Arrays.asList(booleanConditionDest.id), true);
            Assert.assertEquals("Must have 1 condition result", 1, results.size());

            BooleanCondition rootCondition = ((BooleanCondition)results.stream().findFirst().get());

            Assert.assertEquals("Must return boolean on new parent", 2, rootCondition.children.size());
            Assert.assertEquals("Must return boolean reparented condition on new parent item", 1, rootCondition.children.stream().filter(u -> u.id.equals(childOrigId)).count());
        }

    }

    @Test
    public void testReParentBooleanConditionOntoADirectDescendantFails(){

        Assume.assumeFalse(Assume.AssumeReason.BUG, "CAF-901", "ClassificationApiConditionIT::testReParentBooleanConditionOntoADirectDescendantFails", "Always skip for now to enable successful builds and releases.", true, genericApplicationContext);

        Assume.assumeFalse(Assume.AssumeReason.BUG, "CAF-901", "ClassificationApiConditionIT::testReParentBooleanConditionOntoADirectDescendantFails", "Will sometimes causes a stack overflow.",
                testingProperties.getInDocker(),genericApplicationContext);

        // PD711 - We end up creating a recursive condition.
        Assume.assumeTrue(Assume.AssumeReason.BUG, "testReParentBooleanConditionOntoADirectDescendantFails", "PD-711 - Recursive conditions when reparent to a child condition in your own tree.",
                !(apiProperties.isInApiMode(ApiProperties.ApiMode.direct) && apiProperties.isInRepository(ApiProperties.ApiDirectRepository.hibernate)),genericApplicationContext);

        DocumentCollection collection = new DocumentCollection();
        collection.name = "name Citroên";
        collection.description = "description Citroên";

        ExistsCondition existsCondition = new ExistsCondition();
        existsCondition.field = "field Citroên";

        BooleanCondition booleanCondition = new BooleanCondition();
        booleanCondition.name = "Test move parent";
        booleanCondition.operator = BooleanOperator.OR;

        BooleanCondition topmostCondition = new BooleanCondition();
        topmostCondition.name = "test move on topmost parent";
        topmostCondition.operator = BooleanOperator.OR;


        topmostCondition.children = new ArrayList<>();
        booleanCondition.children = new ArrayList<>();

        topmostCondition.children.add(booleanCondition);
        booleanCondition.children.add(existsCondition);

        collection.condition = topmostCondition;

        DocumentCollection createdCollection = sut.create(collection);
        topmostCondition = (BooleanCondition)createdCollection.condition;

        booleanCondition = (BooleanCondition) topmostCondition.children.stream().findFirst().get();

        // Bring back whole tree to make sure we have static version of the details and check it is what we expect.
        {

            Collection<Condition> results = sut.retrieveConditions(Arrays.asList(topmostCondition.id), true);

            Assert.assertEquals("Must return top most condition", 1, results.size());
            Condition result = results.stream().findFirst().get();
            Assert.assertEquals("Must return next boolean condition", 1, ((BooleanCondition) result).children.size());
            Assert.assertEquals("Must return next boolean condition id", booleanCondition.id, ((BooleanCondition) result).children.stream().findFirst().get().id);
        }

        // Store off the useful ids, and check they return now as expected.
        Long originalTopCondition = topmostCondition.id;
        Long childBooleanCondition = booleanCondition.id;

        BooleanCondition booleanToChange = topmostCondition;

        // Reparent the top condition, onto its child condition
        booleanToChange.parentConditionId = booleanCondition.id;

        // ensure we dont have any children on us during update, or it
        // will try to delete  / update them.
        booleanToChange.children = null;

        // Expect it to throw, and the original conditions to be intact!
        shouldThrow(u -> { sut.update(booleanToChange); });

        {

            Collection<Condition> results = sut.retrieveConditions(Arrays.asList(topmostCondition.id), true);

            Assert.assertEquals("Must return top most condition", 1, results.size());
            Condition result = results.stream().findFirst().get();
            Assert.assertEquals("Must return next boolean condition", 1, ((BooleanCondition) result).children.size());
            Assert.assertEquals("Must return next boolean condition id", booleanCondition.id, ((BooleanCondition) result).children.stream().findFirst().get().id);
        }
    }

    @Test
    public void testCantPromoteToFragment() throws Exception {
        NotCondition notConditionFragment = new NotCondition();
        notConditionFragment.name = "notConditionFragment";
        NotCondition targetNotCondition = new NotCondition();
        targetNotCondition.name = "targetNotCondition";
        notConditionFragment.condition = targetNotCondition;

        NotCondition created = sut.create(notConditionFragment);
        assertNotNull(created.condition);
        assertTrue(created.condition instanceof NotCondition);
        assertFalse(created.condition.isFragment);

        NotCondition createdTargetNotCondition = (NotCondition) created.condition;

//        Condition returned = sut.retrieveConditions(Arrays.asList(created.id), true).stream().findFirst().get();
        createdTargetNotCondition.isFragment = true;
        Condition updatedTargetNotCondition = sut.update(createdTargetNotCondition);



        assertFalse(updatedTargetNotCondition.isFragment);

//        NotCondition originalNotCondition = (NotCondition)sut.retrieveConditions(Arrays.asList(created.id), true).stream().findFirst().get();
//        assertTrue(originalNotCondition.condition instanceof FragmentCondition);

    }

    @Test
    public void testUpdateBooleanChildren() throws Exception {
        BooleanCondition booleanCondition = new BooleanCondition();
        booleanCondition.name = "My boolean condition fragment";
        booleanCondition.operator = BooleanOperator.OR;

        booleanCondition = sut.create(booleanCondition);

        ExistsCondition existsCondition = new ExistsCondition();
        existsCondition.field = "my field";
        existsCondition.parentConditionId = booleanCondition.id;

        existsCondition = sut.create(existsCondition);

        booleanCondition = (BooleanCondition) sut.retrieveConditions(Arrays.asList(booleanCondition.id), true).stream().findFirst().get();
        assertEquals(1, booleanCondition.children.size());
        assertArrayEquals(new Long[]{existsCondition.id}, booleanCondition.children.stream().map(c -> c.id).collect(Collectors.toList()).toArray());

        booleanCondition.children = new ArrayList<>();
        booleanCondition = sut.update(booleanCondition);

        booleanCondition = (BooleanCondition) sut.retrieveConditions(Arrays.asList(booleanCondition.id), true).stream().findFirst().get();
        assertEquals(0, booleanCondition.children.size());
    }

    @Test
    public void testUpdateBoolean_Additive(){

        Assume.assumeFalse(Assume.AssumeReason.BY_DESIGN, "CAF-1051", "ClassificationApiConditionIT::testUpdateBoolean_Additive", "Batch creating is not supported in MySQL.",
                !testingProperties.getWebInHibernate() && apiProperties.isInApiMode(ApiProperties.ApiMode.web), genericApplicationContext);

        ExistsCondition existsCondition1 = new ExistsCondition();
        existsCondition1.field = "a field";
        existsCondition1.name = "existsCondition1";

        BooleanCondition booleanCondition = new BooleanCondition();
        booleanCondition.name = "My boolean condition fragment";
        booleanCondition.operator = BooleanOperator.OR;
        booleanCondition.children = new ArrayList<>();
        booleanCondition.children.add(existsCondition1);

        booleanCondition = sut.create(booleanCondition);

        booleanCondition.children.clear();

        ExistsCondition existsCondition2 = new ExistsCondition();
        existsCondition2.field = "a field2";
        existsCondition2.name = "existsCondition2";

        ExistsCondition existsCondition3 = new ExistsCondition();
        existsCondition3.field = "a field3";
        existsCondition3.name = "existsCondition3";

        booleanCondition.children.add(existsCondition2);
        booleanCondition.children.add(existsCondition3);

        BooleanCondition updatedBooleanCondition = sut.update(booleanCondition, UpdateBehaviourType.ADD);

        Assert.assertEquals("Boolean condition should now have 3 children.", 3, updatedBooleanCondition.children.size());
        checkChildOrdering(Arrays.asList(existsCondition1,existsCondition2,existsCondition3),updatedBooleanCondition.children);
    }

    @Test
    public void booleanConditionTest() {
        BooleanCondition booleanCondition = new BooleanCondition();

        TestHelper.shouldThrow((o) -> {
            sut.create(booleanCondition);
        });
        booleanCondition.name = "a name";
        TestHelper.shouldThrow((o) -> {
            sut.create(booleanCondition);
        });
        booleanCondition.operator = BooleanOperator.OR;

        BooleanCondition created = sut.create(booleanCondition);
        assertEquals(booleanCondition.operator, created.operator);
    }

    @Test
    public void numberConditionTest() {
        NumberCondition numberCondition = new NumberCondition();

        TestHelper.shouldThrow((o) -> {
            sut.create(numberCondition);
        });
        numberCondition.operator = NumberOperatorType.EQ;
        TestHelper.shouldThrow((o) -> {
            sut.create(numberCondition);
        });
        numberCondition.field = "field";
        TestHelper.shouldThrow((o) -> {
            sut.create(numberCondition);
        });
        numberCondition.value = Long.MAX_VALUE;
        sut.create(numberCondition);
    }

    @Test
    public void lexiconConditionTest() {
        Lexicon lexicon = new Lexicon();
        lexicon.name = "lexicon";
        lexicon = sut.create(lexicon);

        LexiconCondition lexiconCondition = new LexiconCondition();

        TestHelper.shouldThrow((o) -> {
            sut.create(lexiconCondition);
        });
        lexiconCondition.field = "field";
        TestHelper.shouldThrow((o) -> {
            sut.create(lexiconCondition);
        });
        lexiconCondition.language = "ABC";
        TestHelper.shouldThrow((o) -> {
            sut.create(lexiconCondition);
        });
        lexiconCondition.value = Long.MAX_VALUE;
        TestHelper.shouldThrow((o) -> {
            sut.create(lexiconCondition);
        });

        lexiconCondition.language = "eng";
        lexiconCondition.value = lexicon.id;

        sut.create(lexiconCondition);
    }

    @Test
    public void lexiconConditionDeleteLexiconTest() {
        Lexicon lexicon = new Lexicon();
        lexicon.name = "lexicon";
        lexicon = sut.create(lexicon);

        LexiconCondition lexiconCondition = new LexiconCondition();
        lexiconCondition.field = "field";

        lexiconCondition.language = "eng";
        lexiconCondition.value = lexicon.id;

        sut.create(lexiconCondition);


        final Lexicon finalLexicon = lexicon;
        TestHelper.shouldThrow((o)-> {
            sut.deleteLexicon(finalLexicon.id);
        });
    }

    @Test
    public void existsConditionTest() {
        ExistsCondition existsCondition = new ExistsCondition();

        TestHelper.shouldThrow((o) -> {
            sut.create(existsCondition);
        });
        existsCondition.field = "field";

        sut.create(existsCondition);
    }

    @Test
    public void dateConditionTest() {
        DateCondition dateCondition = new DateCondition();

        TestHelper.shouldThrow((o) -> {
            sut.create(dateCondition);
        });
        dateCondition.field = "field";
        TestHelper.shouldThrow((o) -> {
            sut.create(dateCondition);
        });
        dateCondition.operator = DateOperator.AFTER;
        TestHelper.shouldThrow((o) -> {
            sut.create(dateCondition);
        });
        dateCondition.value = "some wonky datetim";
        TestHelper.shouldThrow((o) -> {
            sut.create(dateCondition);
        });

        DateCondition created;
        //ISO8601
        DateTime now = new DateTime();
        now = new DateTime(now.withZone(DateTimeZone.UTC).toString(ISODateTimeFormat.dateHourMinuteSecond()));//Strip ms

        dateCondition.value = now.toString();
        created = sut.create(dateCondition);
        assertEquals(now, new DateTime(created.value));

        dateCondition.value = now.withZone(DateTimeZone.UTC).toString();
        created = sut.create(dateCondition);
        assertEquals(now, new DateTime(created.value));

        // Try using Zulu time to transmit this information.
        DateTime retryWithZulu = now.toDateTime(DateTimeZone.UTC);
        dateCondition.value = retryWithZulu.toString();
        created =  sut.create(dateCondition);

        assertEquals(now, new DateTime(created.value));

        dateCondition.value = "1994-11-05T08:15:30-05:00";
        created = sut.create(dateCondition);
        assertEquals(new DateTime(dateCondition.value).getMillis(), new DateTime(created.value).getMillis());
    }

    @Test
    public void retrieveSingleItemInPageTest() {
        TextCondition textCondition = new TextCondition();
        textCondition.field = "field";
        textCondition.value = "value";
        textCondition.isFragment = true;

        sut.create(textCondition);

        PageOfResults<Condition> conditionPageOfResults = sut.retrieveConditionFragmentsPage(new PageRequest(1L, 1L));

        assertEquals(new Long(1), conditionPageOfResults.totalhits);
        assertEquals(1, conditionPageOfResults.results.size());
        assertEquals(true, conditionPageOfResults.results.stream().findFirst().get().isFragment);
    }


    @Test
    public void testConditionField_Notes() {

        TextCondition textCondition = new TextCondition();

        // Should throw with missing fields.
        TestHelper.shouldThrow((o) -> {
            sut.create(textCondition);
        });

        // add fields we require.
        textCondition.field = "field";
        textCondition.value = "value";

        TextCondition created = sut.create(textCondition);
        created.notes = "add some notes so we can prove it holds lots of info.";

        TextCondition updated = sut.update(created);

        Assert.assertEquals("ID field should match ", updated.id, created.id);
        Assert.assertEquals("Notes field should match ", updated.notes, created.notes);

        // Test retrieve.
        Collection<Condition> retrieved = sut.retrieveConditions(Arrays.asList(created.id), false);

        Assert.assertEquals("Should only return a single condition.", retrieved.size(), 1);

        Condition condition = retrieved.stream().findFirst().get();

        Assert.assertEquals("ID field should match ", condition.id, created.id);
        Assert.assertEquals("Notes field should match ", condition.notes, created.notes);


        // Finally retrieve with children option enabled.
        retrieved = sut.retrieveConditions(Arrays.asList(created.id), true);

        Assert.assertEquals("Should only return a single condition.", retrieved.size(), 1);

        condition = retrieved.stream().findFirst().get();

        Assert.assertEquals("ID field should match ", condition.id, created.id);
        Assert.assertEquals("Notes field should match ", condition.notes, created.notes);
    }

    @Test
    public void textConditionTest() {
        TextCondition textCondition = new TextCondition();

        TestHelper.shouldThrow((o) -> {
            sut.create(textCondition);
        });

        textCondition.field = "field";
        TestHelper.shouldThrow((o) -> {
            sut.create(textCondition);
        });

        textCondition.value = "value";

        TextCondition created = sut.create(textCondition);
    }

    //PD-600
    @Test
    public void textConditionValidationTest() {
        TextCondition textCondition = new TextCondition();
        textCondition.field = "field";
        textCondition.value = "(value";
        TestHelper.shouldThrow((o) -> {
            sut.create(textCondition);
        });
    }

    @Test
    public void regexCondition() {
        RegexCondition regexCondition = new RegexCondition();

        TestHelper.shouldThrow((o) -> {
            sut.create(regexCondition);
        });

        regexCondition.field = "field";
        TestHelper.shouldThrow((o) -> {
            sut.create(regexCondition);
        });

        regexCondition.value = "value";

        RegexCondition created = sut.create(regexCondition);
    }

    @Test
    public void stringCondition() {
        StringCondition stringCondition = new StringCondition();

        TestHelper.shouldThrow((o) -> {
            sut.create(stringCondition);
        });

        stringCondition.field = "field";
        TestHelper.shouldThrow((o) -> {
            sut.create(stringCondition);
        });

        stringCondition.value = "value";
        TestHelper.shouldThrow((o) -> {
            sut.create(stringCondition);
        });

        stringCondition.operator = StringOperatorType.CONTAINS;

        StringCondition created = sut.create(stringCondition);
        stringCondition.id = created.id;
        stringCondition.isFragment = true;

        assertTrue(DeepEquals.deepEquals(stringCondition, created));

        created.value = "value1";

        StringCondition updated = sut.update(created);

        assertTrue(DeepEquals.deepEquals(created, updated));
    }

    @Test
    public void updateConditionKeepsIsFragment() {
        StringCondition stringCondition = new StringCondition();

        stringCondition.field = "field";
        stringCondition.value = "value";

        stringCondition.operator = StringOperatorType.CONTAINS;

        StringCondition created = sut.create(stringCondition);

        assertTrue(created.isFragment);

        StringCondition updated = sut.update(created);

        assertTrue(updated.isFragment);
    }

    private void assertEqualsCondition(StringCondition expected, StringCondition actual) {
        assertNotNull(actual.id);
        if (expected.id != null) {
            assertEquals(expected.id, actual.id);
        }
        assertEquals(expected.name, actual.name);
        assertEquals(expected.operator, actual.operator);
        assertEquals(expected.value, actual.value);
        assertEquals(expected.field, actual.field);
        assertEquals(expected.target, actual.target);
    }

    @Test
    public void testReturnPageOrder() {
        Collection<Condition> newItems = new LinkedList<>();

        {
            StringCondition item = new StringCondition();
            item.name = "C";
            item.field = "field";
            item.value = "value";
            item.operator = StringOperatorType.CONTAINS;

            item = sut.create(item);

            newItems.add(item);
        }
        {
            StringCondition item = new StringCondition();
            item.name = "A";
            item.field = "field";
            item.value = "value";
            item.operator = StringOperatorType.CONTAINS;

            item = sut.create(item);

            newItems.add(item);
        }
        {
            StringCondition item = new StringCondition();
            item.name = "B";
            item.field = "field";
            item.value = "value";
            item.operator = StringOperatorType.CONTAINS;

            item = sut.create(item);

            newItems.add(item);
        }

        checkSortedItems(newItems);
    }

    //PD-503
    @Test
    public void updateTypeTest() {
        RegexCondition regexCondition = new RegexCondition();
        regexCondition.field = "field";
        regexCondition.value = "value";

        RegexCondition created = sut.create(regexCondition);

        StringCondition stringCondition = new StringCondition();
        stringCondition.id = created.id;
        stringCondition.operator = StringOperatorType.CONTAINS;
        stringCondition.value = "T";
        stringCondition.field = "f";

        StringCondition update = sut.update(stringCondition);

        assertEquals(ConditionType.STRING, update.conditionType);
    }
    @Test
    public void testLexiconCondition_FilterPage() {
        Assume.assumeFalse(Assume.AssumeReason.BUG, "CAF-731", "ClassificationApiConditionIT::testLexiconCondition_FilterPage", "Filtering is not currently supported in MySQL",
                !testingProperties.getWebInHibernate()&&apiProperties.isInApiMode(ApiProperties.ApiMode.web), genericApplicationContext);

        Lexicon lexicon = new Lexicon();
        lexicon.name = "testLexiconCondition_FilterPage";

        {
            LexiconExpression expression = new LexiconExpression();
            expression.type = LexiconExpressionType.REGEX;
            expression.expression = "abc";

            lexicon.lexiconExpressions = new LinkedList<>();
            lexicon.lexiconExpressions.add(expression);
        }

        Lexicon created = sut.create(lexicon);
        Collection<LexiconCondition> expectedLexiconConditions = new ArrayList<>();

        expectedLexiconConditions.add(createLexiconCondition(created, "somefieldname", "ClassificationApiConditionIT:createLexicon"));
        expectedLexiconConditions.add(createLexiconCondition(created, "somefieldname", "ClassificationApiConditionIT:createLexicon"));

        // Now we have a lexiconCondition, try to get it back by the lexicon itself.
        // So type=lexicon and value=idx
        Filter filter = Filter.create(ApiStrings.Conditions.Arguments.TYPE, ConditionType.LEXICON.toValue() );
        filter.put( ApiStrings.Conditions.Arguments.VALUE, created.id);

        {
            PageRequest pageRequest = new PageRequest(1L, 10L);

            PageOfResults<Condition> result = sut.retrieveConditionsPage(pageRequest, filter);
            Assert.assertEquals("Hit count should be equal", expectedLexiconConditions.size(), result.totalhits.intValue());
            Assert.assertEquals("Results should be equal", expectedLexiconConditions.size(), result.results.size());
        }

        // Page 1 at a time over both nodes, and check they are in correct order.
        PageOfResults<Condition> result = sut.retrieveConditionsPage(new PageRequest(1L, 1L), filter);
        PageOfResults<Condition> result2 = sut.retrieveConditionsPage(new PageRequest(2L, 1L), filter);

        Assert.assertEquals("Hit count should be equal", expectedLexiconConditions.size(), result.totalhits.intValue() );

        Assert.assertEquals("Results should have 1 entry.", 1L, result.results.size());
        Assert.assertEquals("Results2 should have 1 entry.", 1L, result2.results.size());


        Assert.assertNotSame("Results should different ids.", result.results.stream().findFirst().get().id, result2.results.stream().findFirst().get().id);

        // ids should be ascending also.
        Assert.assertNotSame("Results 1 should have lower id than result 2 as they should be asc.", result.results.stream().findFirst().get().id < result2.results.stream().findFirst().get().id);
    }

    @Test
    public void testCondition_FilterBy_Notes() {
        Assume.assumeFalse(Assume.AssumeReason.BUG, "CAF-731", "ClassificationApiConditionIT::testCondition_FilterBy_Notes", "Filtering is not currently supported in MySQL",
                !testingProperties.getWebInHibernate()&&apiProperties.isInApiMode(ApiProperties.ApiMode.web), genericApplicationContext);


        TextCondition created1 = null;
        {
            TextCondition textCondition = new TextCondition();

            // add fields we require.
            textCondition.field = "testCondition_FilterBy_Notes1";
            textCondition.value = "value";
            textCondition.notes = getUniqueString("a unique piece of test to filter by.");

            created1 = sut.create(textCondition);
        }
        TextCondition created2 = null;
        {
            TextCondition textCondition = new TextCondition();

            // add fields we require.
            textCondition.field = "testCondition_FilterBy_Notes2";
            textCondition.value = "value";
            textCondition.notes = getUniqueString("b unique piece of test to filter by.");

            created2 = sut.create(textCondition);
        }

        // Now we have a 2 conditions, create a filter, which restricts the page, just to a single notes field,
        // which is on the first condition.
        Filter filter = Filter.create(ApiStrings.Conditions.Arguments.NOTES, created1.notes);

        PageOfResults<Condition> result = sut.retrieveConditionsPage(new PageRequest(1L, 1L), filter);


        Assert.assertEquals("Hit count should be equal", 1, result.totalhits.intValue() );
        Assert.assertEquals("Results should have 1 entry.", 1L, result.results.size());

        Condition resultItem = result.results.stream().findFirst().get();
        Assert.assertEquals("ID of the condition should match filter.", created1.id, resultItem.id);
        Assert.assertEquals("ID of the condition should match filter.", created1.notes, resultItem.notes);
    }

    @Test
    public void testCondition_SortBy_Notes(){
        Assume.assumeFalse(Assume.AssumeReason.BUG, "CAF-731", "ClassificationApiConditionIT::testCondition_SortBy_Notes", "Sorting is not currently supported in MySQL",
                !testingProperties.getWebInHibernate()&&apiProperties.isInApiMode(ApiProperties.ApiMode.web), genericApplicationContext);

        TextCondition created1 = null;
        {
            TextCondition textCondition = new TextCondition();

            // add fields we require.
            textCondition.field = "testCondition_FilterBy_Notes1";
            textCondition.value = "value";
            textCondition.notes = getUniqueString("c unique note");

            created1 = sut.create(textCondition);
        }
        TextCondition created2 = null;
        {
            TextCondition textCondition = new TextCondition();

            // add fields we require.
            textCondition.field = "testCondition_FilterBy_Notes2";
            textCondition.language = "eng";
            textCondition.value = "value";
            textCondition.notes = getUniqueString("a unique note");

            created2 = sut.create(textCondition);
        }
        TextCondition created3 = null;
        {
            TextCondition textCondition = new TextCondition();

            // add fields we require.
            textCondition.field = "testCondition_FilterBy_Notes2";
            textCondition.language = "eng";
            textCondition.value = "value";
            textCondition.notes = getUniqueString("b unique note");

            created3 = sut.create(textCondition);
        }
        Sort sort = Sort.create(ApiStrings.Conditions.Arguments.NOTES,true);
        PageOfResults<Condition> result = sut.retrieveConditionsPage(new PageRequest(1L, 5L), sort);
        Assert.assertEquals("Hit count should be equal", 3, result.totalhits.intValue());
        Assert.assertEquals("Results should have 3 entries.", 3L, result.results.size());
        List<Condition> createdConditions = Arrays.asList(created1, created2, created3);
        createdConditions.sort((c1, c2) -> {
            if (c1.notes == null) {
                if (c2.notes == null) {
                    return c1.id.compareTo(c2.id);
                }
                return -1; // null comes before any text
            } else if (c2.notes == null) {
                return 1; // source has text, dest null, so after
            } else {
                return c1.notes.compareToIgnoreCase(c2.notes);
            }
        });
        List<Condition> returnedConditions = new ArrayList<>(result.results);
        for(int i=0; i< result.results.size(); i++){
            Assert.assertEquals(createdConditions.get(i).notes, returnedConditions.get(i).notes);
        }
        sort = Sort.create(ApiStrings.Conditions.Arguments.NOTES,false);
        result = sut.retrieveConditionsPage(new PageRequest(1L, 3L), sort);
        createdConditions.sort((c1, c2) -> {
            if (c1.notes == null) {
                if (c2.notes == null) {
                    return c1.id.compareTo(c2.id);
                }
                return -1; // null comes before any text
            } else if (c2.notes == null) {
                return 1; // source has text, dest null, so after
            } else {
                return c2.notes.compareToIgnoreCase(c1.notes);
            }
        });
        returnedConditions = new ArrayList<>(result.results);
        for(int i=0; i< result.results.size(); i++){
            Assert.assertEquals(createdConditions.get(i).notes, returnedConditions.get(i).notes);
        }
    }

    @Test
    public void testCreate_Update_Collection_CreatesNewNonFragmentConditions() {

        Assume.assumeFalse(Assume.AssumeReason.BUG, "CAF-731", "ClassificationApiConditionIT::testCreate_Update_Collection_CreatesNewNonFragmentConditions", "Filtering is not currently supported in MySQL",
                !testingProperties.getWebInHibernate()&&apiProperties.isInApiMode(ApiProperties.ApiMode.web), genericApplicationContext);

        Lexicon lexiconItem = createNewLexicon("testCollectionSequencePagedByFilter_Condition", "abc");

        // Get the base count of condition fragments, and conditions.
        PageOfResults < Condition > fragmentresults = sut.retrieveConditionFragmentsPage(new PageRequest(1L, 1L));


        // created a filter just for our own use in tests to get back all conditions, which aren't fragments!!
        Filter filter = Filter.create(ApiStrings.Conditions.Arguments.IS_FRAGMENT, false );

        PageOfResults<Condition> conditionresults = sut.retrieveConditionsPage(new PageRequest(1L, 1L), filter);

        // store off our starting indexes - useful for non-direct modes where db isn't empty for this tenant.
        Long startFragmentCount = fragmentresults.totalhits;
        Long startNonFragmentCount = conditionresults.totalhits;

        LexiconCondition newCondition = createLexiconCondition(lexiconItem, "somefieldname", "testCreate_Update_Collection_CreatesNewNonFragmentConditions");

        // Confirm lexicon condition has been added as a fragment, and not returned in non-fragment list.
        {
            fragmentresults = sut.retrieveConditionFragmentsPage(new PageRequest(1L, 1L));
            conditionresults = sut.retrieveConditionsPage(new PageRequest(1L, 1L), filter);

            // we should be up by one condition - and none on fragment count.
            assertEquals("Fragment must be the same as before", startFragmentCount + 1L, fragmentresults.totalhits.longValue());
            assertEquals("Non fragment must be the same", startNonFragmentCount, conditionresults.totalhits);
        }

        // create a new document collection with our lexicon condition on it. It should now create a new condition isFragment=false.
        {
            DocumentCollection documentCollection = new DocumentCollection();
            documentCollection.name = "testCreate_Update_Collection_CreatesNewNonFragmentConditions";
            documentCollection.description = "used as unique doc collection to lookup by...";
            documentCollection.condition = newCondition;

            // watch we should actually validate that we dont supply the condition with an ID
            // if that happens it will throw here! and we need to blank out the ID below on the condition,
            // prior to calling create / update!!!
            documentCollection = sut.create(documentCollection);

            fragmentresults = sut.retrieveConditionFragmentsPage(new PageRequest(1L, 1L));
            conditionresults = sut.retrieveConditionsPage(new PageRequest(1L, 1L), filter);

            // we should be up by one condition - and none on fragment count.
            assertEquals("Fragment must be the same as before", startFragmentCount + 1L, fragmentresults.totalhits.longValue());
            assertEquals("Non fragment must be the same", (startNonFragmentCount + 1L), conditionresults.totalhits.longValue());
        }

        {
            // update the collection we need with our lexicon conditions, it should now create a new condition isFragment=false.
            DocumentCollection createdCollection = createUniqueDocumentCollection("testCreate_Update_Collection_CreatesNewNonFragmentConditions - used col");
            createdCollection.condition = newCondition;
            createdCollection = sut.update(createdCollection);

            fragmentresults = sut.retrieveConditionFragmentsPage(new PageRequest(1L, 1L));
            conditionresults = sut.retrieveConditionsPage(new PageRequest(1L, 1L), filter);

            // we should be up by 2 conditions - and none on fragment count.
            assertEquals("Fragment must be the same as before", startFragmentCount + 1L, fragmentresults.totalhits.longValue());
            assertEquals("Non fragment must be the same", (startNonFragmentCount + 2L), conditionresults.totalhits.longValue());
        }

    }

    private void checkSortedItems(Collection<Condition> newItems) {
        List<Condition> sortedItems;
        PageRequest pageRequest = new PageRequest(1L, 10L);
        PageOfResults<Condition> pageOfResults = sut.retrieveConditionFragmentsPage(pageRequest);
        List<Condition> allItems = new ArrayList<>();


        // Go through and reqeust all pages, and ensure the sorting matches that expected.
        // Now finally ensure we get all the items in one big page to compare.

        // as the amount of data increases the length of time
        // to get all results increases. As such batch this in smaller amounts.
        long maxPageSize = 50;
        long startIndex = 1;
        while ( allItems.size() < pageOfResults.totalhits )
        {
            pageOfResults = sut.retrieveConditionFragmentsPage(new PageRequest(startIndex, maxPageSize));

            if ( pageOfResults.results.size() == 0 ) {
                break;
            }

            allItems.addAll(pageOfResults.results);
            startIndex += maxPageSize;
        }


        assertEquals("Sorted Pages of result should match", Long.valueOf(allItems.size()), pageOfResults.totalhits);

        sortedItems = allItems.stream().sorted((c1, c2) -> {
            if (c1.name == null ) {
                if(c2.name == null) {
                    return c1.id.compareTo(c2.id);
                }
                return -1; // null comes before any text
            } else if (c2.name == null) {
                return 1; // source has text, dest null, so after
            }
            else{
                return c1.name.compareToIgnoreCase(c2.name);
            }
        }).collect(Collectors.toList());

        assertEquals("Sorted Items size, should match total hits", Long.valueOf(sortedItems.size()), pageOfResults.totalhits);

        // Now go through the default order we got the results back in ( allItems ) and our sorted list. ( sortedItems )
        for (int i = 0; i < sortedItems.size(); i++) {
            assertEquals("Order of id not correct for index: " + String.valueOf(i), sortedItems.get(i).id, allItems.get(i).id);
            assertEquals("Order of Name not correct for index: " + String.valueOf(i), sortedItems.get(i).name, allItems.get(i).name);
        }
    }
}
