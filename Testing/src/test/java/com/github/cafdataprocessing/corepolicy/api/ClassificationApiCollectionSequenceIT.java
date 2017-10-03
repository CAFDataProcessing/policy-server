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

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.cafdataprocessing.corepolicy.common.ApiProperties;
import com.github.cafdataprocessing.corepolicy.common.ApiStrings;
import com.github.cafdataprocessing.corepolicy.common.dto.*;
import com.github.cafdataprocessing.corepolicy.common.dto.conditions.Condition;
import com.github.cafdataprocessing.corepolicy.common.dto.conditions.ConditionType;
import com.github.cafdataprocessing.corepolicy.common.dto.conditions.ExistsCondition;
import com.github.cafdataprocessing.corepolicy.common.dto.conditions.LexiconCondition;
import com.github.cafdataprocessing.corepolicy.common.exceptions.ConditionEngineException;
import com.github.cafdataprocessing.corepolicy.common.exceptions.InvalidFieldValueCpeException;
import com.github.cafdataprocessing.corepolicy.common.shared.ErrorCodes;
import com.github.cafdataprocessing.corepolicy.testing.Assume;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;
import java.util.stream.Collectors;

import static com.github.cafdataprocessing.corepolicy.TestHelper.getUniqueString;
import static com.github.cafdataprocessing.corepolicy.TestHelper.shouldThrow;
import com.github.cafdataprocessing.corepolicy.common.EnvironmentSnapshot;
import static org.junit.Assert.*;

/**
 *
 */
public class ClassificationApiCollectionSequenceIT extends ClassificationApiTestBase {

    private Filter nonFragmentFilter = null;

    public ClassificationApiCollectionSequenceIT() {
        super();

        // created a filter just for our own use in tests to get back all conditions, which aren't fragments!!
        ObjectNode newNode = new ObjectNode(JsonNodeFactory.instance);
        // add a string test simple string case first.
        newNode.put(ApiStrings.Conditions.Arguments.IS_FRAGMENT, false);
        nonFragmentFilter = Filter.create(newNode);
    }

    //PD-429
    @Test
    public void testCreateCollectionSequenceWithAccentedCharactersIT() throws Exception {
        CollectionSequence collectionSequence = new CollectionSequence();
        collectionSequence.name = "Citroên CS";
        collectionSequence.description = "Citroên CS Description";

        CollectionSequence created = sut.create(collectionSequence);

        assertNotNull(created.id);
        assertEquals(collectionSequence.name, created.name);
        assertEquals(collectionSequence.description, created.description);

        CollectionSequence retireved = sut.retrieveCollectionSequences(Arrays.asList(created.id)).stream().findFirst().get();

        assertEquals(collectionSequence.name, retireved.name);
        assertEquals(collectionSequence.description, retireved.description);
    }


    @Test
    public void testCreateCollectionSequenceIT() throws Exception {
        CollectionSequence collectionSequence = new CollectionSequence();
        collectionSequence.name = getUniqueString("Hello");
        collectionSequence.description = "My description";

        CollectionSequence created = sut.create(collectionSequence);

        assertNotNull(created.id);
        assertEquals(collectionSequence.name, created.name);
        assertEquals(collectionSequence.description, created.description);
        assertEquals(created.evaluationEnabled, true); // Tests if collection sequence evaluation enabled defaults true
    }

    @Test
    public void testCreateDisabledCollectionSequenceIT() throws Exception {
        Assume.assumeFalse(Assume.AssumeReason.BUG, "CAF-568", "ClassificationApiCollectionSequenceIT::testUpdateCollectionSequence", "Disabling Collection Sequences is not currently supported in MySQL",
                !testingProperties.getWebInHibernate()&&apiProperties.isInApiMode(ApiProperties.ApiMode.web), genericApplicationContext);

        CollectionSequence collectionSequence = new CollectionSequence();
        collectionSequence.name = getUniqueString("Hello");
        collectionSequence.description = "My description";
        collectionSequence.evaluationEnabled = false;

        CollectionSequence created = sut.create(collectionSequence);

        assertNotNull(created.id);
        assertEquals(collectionSequence.name, created.name);
        assertEquals(collectionSequence.description, created.description);
        assertEquals(false, created.evaluationEnabled);
    }

    @Test
    public void testCreateCollectionSequenceWithEvaluateOr() throws Exception {
        CollectionSequence collectionSequence = new CollectionSequence();
        collectionSequence.name = getUniqueString("Hello");
        collectionSequence.description = "My description";
        collectionSequence.fullConditionEvaluation = true;

        CollectionSequence created = sut.create(collectionSequence);

        assertNotNull(created.id);
        assertEquals(collectionSequence.name, created.name);
        assertEquals(collectionSequence.description, created.description);
        assertEquals(collectionSequence.fullConditionEvaluation, created.fullConditionEvaluation);
    }

    @Test
    public void testCreateCollectionSequenceWithEntries() throws Exception {
        DocumentCollection collection = new DocumentCollection();
        collection.name = getUniqueString("Collection");
        DocumentCollection createdCollection = sut.create(collection);

        CollectionSequence collectionSequence = new CollectionSequence();
        collectionSequence.name = getUniqueString("Hello");
        collectionSequence.description = "My description";
        collectionSequence.collectionSequenceEntries = new ArrayList<>();
        CollectionSequenceEntry collectionSequenceEntry = new CollectionSequenceEntry();
        collectionSequenceEntry.collectionIds = new HashSet<>(Arrays.asList(createdCollection.id));
        collectionSequenceEntry.stopOnMatch = true;
        collectionSequenceEntry.order = 400;
        collectionSequence.collectionSequenceEntries.add(collectionSequenceEntry);

        CollectionSequence created = sut.create(collectionSequence);

        assertNotNull(created.id);
        assertEquals(collectionSequence.name, created.name);
        assertEquals(collectionSequence.description, created.description);

        //Due to a quirk of the repositories the count is only generated after the creation of the sequence entries
        CollectionSequence retrieved = sut.retrieveCollectionSequences(Arrays.asList(created.id)).stream().findFirst().get();

        assertEquals((Integer) 1, retrieved.collectionCount);

        assertTrue(collectionSequence.collectionSequenceEntries.size() > 0);
        assertEquals(1, retrieved.collectionSequenceEntries.size());
    }
    
    

    @Test
    public void testCreateCollectionSequenceEntriesAreOrderedByDefault() throws Exception {
        DocumentCollection collection = new DocumentCollection();
        collection.name = getUniqueString("Collection");
        DocumentCollection createdCollection = sut.create(collection);

        CollectionSequence collectionSequence = new CollectionSequence();
        collectionSequence.name = getUniqueString("Hello");
        collectionSequence.description = "My description";
        collectionSequence.collectionSequenceEntries = new ArrayList<>();
        
        List<CollectionSequenceEntry> expectedOrder = new ArrayList<>();
        {
            CollectionSequenceEntry collectionSequenceEntry = null;
            collectionSequenceEntry = createSequenceEntry( createdCollection, collectionSequence, 400 );
            expectedOrder.add(  collectionSequenceEntry );

            collectionSequenceEntry = createSequenceEntry( createdCollection, collectionSequence, 300 );
             // add this to the beginning.
            expectedOrder.add( 0, collectionSequenceEntry );
            collectionSequenceEntry = createSequenceEntry( createdCollection, collectionSequence, 1 );
             // add this to the beginning.
            expectedOrder.add( 0, collectionSequenceEntry );
            collectionSequenceEntry = createSequenceEntry( createdCollection, collectionSequence, 2 );
             // add this to the beginning.
            expectedOrder.add( 1, collectionSequenceEntry );
        }
        CollectionSequence created = sut.create(collectionSequence);

        //Due to a quirk of the repositories the count is only generated after the creation of the sequence entries
        CollectionSequence retrieved = sut.retrieveCollectionSequences(Arrays.asList(created.id)).stream().findFirst().get();
     
      
        assertEquals("Should have 1 collection per C.S.E.", Integer.valueOf(expectedOrder.size()), retrieved.collectionCount);
        assertEquals("Must have expected num of collection sequence entries", expectedOrder.size(), retrieved.collectionSequenceEntries.size());
        
        // Check that the default ordering of the entries which are returned is correct.
        Iterator<CollectionSequenceEntry> csIter = retrieved.collectionSequenceEntries.listIterator();
        
        for ( CollectionSequenceEntry cse : expectedOrder )
        {
            // get the next element from our list 
            assertTrue("We must have the same number of elements",  csIter.hasNext());
            CollectionSequenceEntry tmpVal = csIter.next();
            
            compareObject(cse, tmpVal, true);
        }
    }

    // There was an issue where the information inside the environment snapshot is not being ordered correctly, and definately
    // not in the same order as those requested directly via the API!  As such our evaluate / classify calls can behave differently
    // to info returned say to a client like the UI!.
    @Test
    public void testCreateCollectionSequenceEntriesAreOrderedByDefault_UsingEnvSnapshot() throws Exception {
        DocumentCollection collection = new DocumentCollection();
        collection.name = getUniqueString("Collection");
        DocumentCollection createdCollection = sut.create(collection);

        CollectionSequence collectionSequence = new CollectionSequence();
        collectionSequence.name = getUniqueString("Hello");
        collectionSequence.description = "My description";
        collectionSequence.collectionSequenceEntries = new ArrayList<>();
        
        List<CollectionSequenceEntry> expectedOrder = new ArrayList<>();
        {
            CollectionSequenceEntry collectionSequenceEntry = null;
            collectionSequenceEntry = createSequenceEntry( createdCollection, collectionSequence, 400 );
            expectedOrder.add(  collectionSequenceEntry );

            collectionSequenceEntry = createSequenceEntry( createdCollection, collectionSequence, 300 );
             // add this to the beginning.
            expectedOrder.add( 0, collectionSequenceEntry );
            collectionSequenceEntry = createSequenceEntry( createdCollection, collectionSequence, 1 );
             // add this to the beginning.
            expectedOrder.add( 0, collectionSequenceEntry );
            collectionSequenceEntry = createSequenceEntry( createdCollection, collectionSequence, 2 );
             // add this to the beginning.
            expectedOrder.add( 1, collectionSequenceEntry );
        }
        
        CollectionSequence created = sut.create(collectionSequence);

        EnvironmentSnapshot snapshotInstance = envSnapshot.get( created.id, null, null );
        
        CollectionSequence retrieved = snapshotInstance.getCollectionSequences().get( created.id );
        
        //Due to a quirk of the repositories the count is only generated after the creation of the sequence entries
        CollectionSequence apiDirectRetrieved = sut.retrieveCollectionSequences(Arrays.asList(created.id)).stream().findFirst().get();
      
        assertEquals("Should have 1 collection per C.S.E.", Integer.valueOf(expectedOrder.size()), retrieved.collectionCount);
        assertEquals("Must have expected num of collection sequence entries", expectedOrder.size(), retrieved.collectionSequenceEntries.size());
        
        // Check that the default ordering of the entries which are returned is correct.
        Iterator<CollectionSequenceEntry> csIter = retrieved.collectionSequenceEntries.listIterator();
        
        for ( CollectionSequenceEntry cse : expectedOrder )
        {
            // get the next element from our list 
            assertTrue("We must have the same number of elements",  csIter.hasNext());
            CollectionSequenceEntry tmpVal = csIter.next();
            
            compareObject(cse, tmpVal, true);
        }
    }
    
    private CollectionSequenceEntry createSequenceEntry( DocumentCollection createdCollection,
                                                         CollectionSequence collectionSequence, 
                                                         int order) {
        CollectionSequenceEntry collectionSequenceEntry = new CollectionSequenceEntry();
        collectionSequenceEntry.collectionIds = new HashSet<>(Arrays.asList(createdCollection.id));
        collectionSequenceEntry.stopOnMatch = true;
        collectionSequenceEntry.order = (short)order;
        collectionSequence.collectionSequenceEntries.add(collectionSequenceEntry);
        return collectionSequenceEntry;
    }

    //PD-513
    //Ensures the collection counts are updated properly
    @Test
    public void testCreateCollectionSequencesWithUpdatingCount() throws Exception {


        CollectionSequence collectionSequence1 = new CollectionSequence();
        {
            collectionSequence1.name = getUniqueString("Hello");
            collectionSequence1.description = "My description";
            collectionSequence1.collectionSequenceEntries = new ArrayList<>();
            CollectionSequenceEntry collectionSequenceEntry = new CollectionSequenceEntry();

            DocumentCollection collection = new DocumentCollection();
            collection.name = getUniqueString("Collection");
            DocumentCollection createdCollection = sut.create(collection);

            collectionSequenceEntry.collectionIds = new HashSet<>(Arrays.asList(createdCollection.id));
            collectionSequenceEntry.stopOnMatch = true;
            collectionSequenceEntry.order = 400;
            collectionSequence1.collectionSequenceEntries.add(collectionSequenceEntry);

            collectionSequence1 = sut.create(collectionSequence1);
        }

        CollectionSequence collectionSequence2 = new CollectionSequence();
        {
            collectionSequence2.name = getUniqueString("Hello");
            collectionSequence2.description = "My description";
            collectionSequence2.collectionSequenceEntries = new ArrayList<>();
            CollectionSequenceEntry collectionSequenceEntry = new CollectionSequenceEntry();

            DocumentCollection collection = new DocumentCollection();
            collection.name = getUniqueString("Collection");
            DocumentCollection createdCollection = sut.create(collection);
            DocumentCollection collection1 = new DocumentCollection();
            collection1.name = getUniqueString("Collection");
            DocumentCollection createdCollection1 = sut.create(collection1);

            collectionSequenceEntry.collectionIds = new HashSet<>(Arrays.asList(createdCollection.id, createdCollection1.id));
            collectionSequenceEntry.stopOnMatch = true;
            collectionSequenceEntry.order = 400;
            collectionSequence2.collectionSequenceEntries.add(collectionSequenceEntry);

            collectionSequence2 = sut.create(collectionSequence2);
        }

        Collection<CollectionSequence> collectionSequences = sut.retrieveCollectionSequences(Arrays.asList(collectionSequence1.id, collectionSequence2.id));
        final CollectionSequence finalCollectionSequence = collectionSequence1;
        CollectionSequence retireved1 = collectionSequences.stream().filter(u -> u.id.equals(finalCollectionSequence.id)).findFirst().get();
        final CollectionSequence finalCollectionSequence1 = collectionSequence2;
        CollectionSequence retireved2 = collectionSequences.stream().filter(u -> u.id.equals(finalCollectionSequence1.id)).findFirst().get();

        assertEquals((Integer) 1, retireved1.collectionCount);
        assertEquals((Integer) 2, retireved2.collectionCount);
    }

    @Test
    public void testUpdateCollectionSequence() throws Exception {
        Assume.assumeFalse(Assume.AssumeReason.BUG, "CAF-568", "ClassificationApiCollectionSequenceIT::testUpdateCollectionSequence", "Disabling Collection Sequences is not currently supported in MySQL",
                !testingProperties.getWebInHibernate()&&apiProperties.isInApiMode(ApiProperties.ApiMode.web), genericApplicationContext);

        CollectionSequence collectionSequence = new CollectionSequence();
        collectionSequence.name = "Hello";
        collectionSequence.description = "My description";
        collectionSequence.evaluationEnabled = true;

        CollectionSequence created = sut.create(collectionSequence);
        created.name = "new";
        created.description = "new";
        created.evaluationEnabled = false;

        CollectionSequence updated = sut.update(created);

        assertNotNull(created.id);
        assertEquals(created.name, updated.name);
        assertEquals(created.description, updated.description);
        assertEquals(false, updated.evaluationEnabled); // Test if the evaluation was updated correctly to false
    }

    @Test
    public void testUpdateCollectionSequenceWithNewEntries() throws Exception {
        DocumentCollection collection = new DocumentCollection();
        collection.name = "Collection";
        DocumentCollection createdCollection = sut.create(collection);

        CollectionSequence collectionSequence = new CollectionSequence();
        collectionSequence.name = "Hello";
        collectionSequence.description = "My description";
        collectionSequence.collectionSequenceEntries = new ArrayList<>();
        CollectionSequenceEntry collectionSequenceEntry = new CollectionSequenceEntry();
        collectionSequenceEntry.collectionIds = new HashSet<>(Arrays.asList(createdCollection.id));
        collectionSequenceEntry.stopOnMatch = true;
        collectionSequenceEntry.order = 400;
        collectionSequence.collectionSequenceEntries.add(collectionSequenceEntry);


        CollectionSequence created = sut.create(collectionSequence);


        DocumentCollection collection1 = new DocumentCollection();
        collection1.name = "Collection 1";
        createdCollection = sut.create(collection1);

        CollectionSequenceEntry collectionSequenceEntry1 = new CollectionSequenceEntry();
        collectionSequenceEntry1.collectionIds = new HashSet<>(Arrays.asList(createdCollection.id));
        collectionSequenceEntry1.stopOnMatch = true;
        collectionSequenceEntry1.order = 1050;
        created.collectionSequenceEntries.clear();
        created.collectionSequenceEntries.add(collectionSequenceEntry1);

        CollectionSequence updated = sut.update(created);

        assertEquals(1, updated.collectionSequenceEntries.size());
        assertEquals(1, updated.collectionSequenceEntries.stream().findFirst().get().collectionIds.size());
        assertEquals(createdCollection.id, updated.collectionSequenceEntries.stream().findFirst().get().collectionIds.stream().findFirst().get());

    }

    @Test
    public void testRetrieveCollectionSequences() throws Exception {
        CollectionSequence collectionSequence = new CollectionSequence();
        collectionSequence.name = "Hello";
        collectionSequence.description = "My description";
        collectionSequence.evaluationEnabled = true;

        CollectionSequence created = sut.create(collectionSequence);

        Collection<CollectionSequence> retrieved = sut.retrieveCollectionSequences
                (Arrays.asList(created.id));
        assertTrue(retrieved.size() > 0);
        for (CollectionSequence cs : retrieved) {
            assertEquals(true, cs.evaluationEnabled);
        }
    }

    @Test
    public void testRetrieveCollectionSequencesPage() throws Exception {

        // Ok when creating we need to find out what our first ID is.
        // So we know how many we have in the system, and where to start our paging from.
        Long totalHits = 0L;
        Long startIndex = 0L;

        {
            // before we create any new collection sequences find out how many are in system!
            PageOfResults<CollectionSequence> pageOfResults = sut.retrieveCollectionSequencesPage
                    (new PageRequest(1L, 1L));

            totalHits = pageOfResults.totalhits;
            startIndex = totalHits;
        }

        // All requests should now find totalHits + 15L new results.
        // Start index is how many hits we have in the system, prior to our new items
        totalHits = totalHits + 15L;

        for (int i = 0; i < 15; i++) {
            CollectionSequence collectionSequence = new CollectionSequence();
            collectionSequence.name = "Hello";
            collectionSequence.description = "My description";
            CollectionSequence created = sut.create(collectionSequence);
        }

        {
            PageRequest pageRequest = new PageRequest();
            pageRequest.start = startIndex + 1L;
            pageRequest.max_page_results = 10L;
            PageOfResults<CollectionSequence> pageOfResults = sut.retrieveCollectionSequencesPage
                    (pageRequest);
            assertEquals(totalHits, pageOfResults.totalhits);
            assertEquals(10, pageOfResults.results.size());
        }

        {
            PageRequest pageRequest = new PageRequest();
            pageRequest.start = startIndex + 1L;
            pageRequest.max_page_results = 15L;
            PageOfResults<CollectionSequence> pageOfResults = sut.retrieveCollectionSequencesPage
                    (pageRequest);
            assertEquals(totalHits, pageOfResults.totalhits);
            assertEquals(15, pageOfResults.results.size());
        }

        {
            PageRequest pageRequest = new PageRequest();
            pageRequest.start = startIndex + 5L;
            pageRequest.max_page_results = 10L;
            PageOfResults<CollectionSequence> pageOfResults = sut.retrieveCollectionSequencesPage
                    (pageRequest);
            assertEquals(totalHits, pageOfResults.totalhits);
            assertEquals(10, pageOfResults.results.size());
        }

        {
            PageRequest pageRequest = new PageRequest();
            pageRequest.start = startIndex + 14L;
            pageRequest.max_page_results = 10L;

            PageOfResults<CollectionSequence> pageOfResults = sut.retrieveCollectionSequencesPage
                    (pageRequest);
            assertEquals(totalHits, pageOfResults.totalhits);
            assertEquals(2, pageOfResults.results.size());
        }
    }

    @Test
    public void testRetrieveCollectionSequencesByName() throws Exception {
        CollectionSequence collectionSequence = new CollectionSequence();
        collectionSequence.name = getUniqueString("Hello");
        collectionSequence.description = "My description";
        CollectionSequence created = sut.create(collectionSequence);

        Collection<CollectionSequence> retrieved = sut.retrieveCollectionSequencesByName
                (created.name);
        assertTrue(retrieved.size() > 0);
    }

    @Test(expected = RuntimeException.class)
    public void testDeleteCollectionSequence() throws Exception {

        CollectionSequence created;

        try {
            CollectionSequence collectionSequence = new CollectionSequence();
            collectionSequence.name = "Hello";
            collectionSequence.description = "My description";
            created = sut.create(collectionSequence);

            sut.deleteCollectionSequence(created.id);
        } catch (Exception ex) {
            // this should all work above here!
            Assert.assertTrue("testDeleteCollectionSequence failure: " + ex.toString(), false);
            throw new ConditionEngineException(ErrorCodes.GENERIC_ERROR, "Failed in deletecollection sequence setup", ex);
        }

        Collection<CollectionSequence> retrieved = sut.retrieveCollectionSequences
                (Arrays.asList(created.id));

        assertNotNull(retrieved);
        assertTrue(retrieved.size() == 0);

    }

    @Test
    public void testDefaultCollectionAndExcludedFragment() {

        //EXCLUDED FRAGMENTS NOT SUPPORTED BY WEB SERVICES
        if (!apiProperties.getMode().equalsIgnoreCase("direct"))
            return;

        DocumentCollection collection = new DocumentCollection();
        collection.name = "Default collection";
        collection = sut.create(collection);

        assertNotNull(collection.id);

        ExistsCondition fragment = new ExistsCondition();
        fragment.name = "My fragment";
        fragment.field = "some field";
        fragment = sut.create(fragment);

        assertNotNull(fragment.id);

        CollectionSequence collectionSequence = new CollectionSequence();
        collectionSequence.name = "Hello";
        collectionSequence.description = "My description";
        collectionSequence.defaultCollectionId = collection.id;
        collectionSequence.excludedDocumentFragmentConditionId = fragment.id;

        CollectionSequence created = sut.create(collectionSequence);

        assertEquals(created.defaultCollectionId, collection.id);
        assertEquals(created.excludedDocumentFragmentConditionId, fragment.id);

        CollectionSequence retrieved = sut.create(collectionSequence);

        assertEquals(retrieved.defaultCollectionId, collection.id);
        assertEquals(retrieved.excludedDocumentFragmentConditionId, fragment.id);

    }

    @Test
    public void testFragmentNotExists() {

        //EXCLUDED FRAGMENTS NOT SUPPORTED BY WEB SERVICES
        if (!apiProperties.getMode().equalsIgnoreCase("direct"))
            return;

        CollectionSequence collectionSequence = new CollectionSequence();
        collectionSequence.name = "Hello";
        collectionSequence.description = "My description";
        collectionSequence.excludedDocumentFragmentConditionId = (long) Integer.MAX_VALUE;

        shouldThrow(o -> {
            sut.create(collectionSequence);
        }, InvalidFieldValueCpeException.class);
    }

    @Test
    public void testDefaultCollectionNotExists() {


        CollectionSequence collectionSequence = new CollectionSequence();
        collectionSequence.name = "Hello";
        collectionSequence.description = "My description";
        collectionSequence.defaultCollectionId = (long) Integer.MAX_VALUE;

        shouldThrow(o -> {
            sut.create(collectionSequence);
        }, InvalidFieldValueCpeException.class);
    }

    @Test
    public void testReturnPageOrder() {
        Collection<CollectionSequence> newItems = new LinkedList<>();

        {
            CollectionSequence collectionSequence = new CollectionSequence();
            collectionSequence.name = "CHello";
            collectionSequence.description = "My description";
            collectionSequence = sut.create(collectionSequence);
            collectionSequence.collectionCount = null;

            newItems.add(collectionSequence);
        }
        {
            CollectionSequence collectionSequence = new CollectionSequence();
            collectionSequence.name = "bHello";
            collectionSequence.description = "My description";
            collectionSequence = sut.create(collectionSequence);
            collectionSequence.collectionCount = null;

            newItems.add(collectionSequence);
        }
        {
            CollectionSequence collectionSequence = new CollectionSequence();
            collectionSequence.name = "AHello";
            collectionSequence.description = "My description";
            collectionSequence = sut.create(collectionSequence);
            collectionSequence.collectionCount = null;

            newItems.add(collectionSequence);
        }
        {
            CollectionSequence collectionSequence = new CollectionSequence();
            collectionSequence.name = "aHello";
            collectionSequence.description = "My description";
            collectionSequence = sut.create(collectionSequence);
            collectionSequence.collectionCount = null;

            newItems.add(collectionSequence);
        }
        {
            CollectionSequence collectionSequence = new CollectionSequence();
            collectionSequence.name = "AHello";
            collectionSequence.description = "My description";
            collectionSequence = sut.create(collectionSequence);
            collectionSequence.collectionCount = null;

            newItems.add(collectionSequence);
        }

        {
            CollectionSequence collectionSequence = new CollectionSequence();
            collectionSequence.name = "BHello";
            collectionSequence.description = "My description";
            collectionSequence = sut.create(collectionSequence);
            collectionSequence.collectionCount = null;

            newItems.add(collectionSequence);
        }

        {
            CollectionSequence collectionSequence = new CollectionSequence();
            collectionSequence.name = "AHello";
            collectionSequence.description = "My description";
            collectionSequence = sut.create(collectionSequence);
            collectionSequence.collectionCount = null;

            newItems.add(collectionSequence);
        }


        checkSortedItems(newItems);
    }

    @Test
    public void testCollectionSequencePagedByFilter_Chain_Via_LexiconCondition_WithFragments() {

        Assume.assumeFalse(Assume.AssumeReason.BUG, "CAF-731", "ClassificationApiCollectionSequenceIT::testCollectionSequencePagedByFilter_Chain_Via_LexiconCondition_WithFragments", "Filtering is not currently supported in MySQL",
                !testingProperties.getWebInHibernate()&&apiProperties.isInApiMode(ApiProperties.ApiMode.web), genericApplicationContext);

        Long startFragmentCount = 0L;
        Long startNonFragmentCount = 0L;

        DocumentCollection createdCollection = createUniqueDocumentCollection("testCollectionSequencePagedByFilter_Chain_Via_LexiconCondition_WithFragments - used col");
        DocumentCollection dummyColl = createUniqueDocumentCollection("testCollectionSequencePagedByFilter_Chain_Via_LexiconCondition_WithFragments - dummy col");

        {
            // created a filter just for our own use in tests to get back all conditions, which aren't fragments!!
            ObjectNode newNode = new ObjectNode(JsonNodeFactory.instance);
            // add a string test simple string case first.
            newNode.put(ApiStrings.Conditions.Arguments.IS_FRAGMENT, false);
            nonFragmentFilter = Filter.create(newNode);

            // Get the base count of condition fragments, and conditions.
            PageOfResults<Condition> conditionresults = sut.retrieveConditionsPage(new PageRequest(1L, 1L), nonFragmentFilter);
            PageOfResults<Condition> fragmentresults = sut.retrieveConditionFragmentsPage(new PageRequest(1L, 1L));

            // store off our starting indexes - useful for non-direct modes where db isn't empty for this tenant.
            startFragmentCount = fragmentresults.totalhits;
            startNonFragmentCount = conditionresults.totalhits;
        }

        Lexicon lexiconItem = createNewLexicon("testCollectionSequencePagedByFilter_Chain_Via_LexiconCondition_WithFragments", "abc");

        Collection<LexiconCondition> expectedLexiconConditions = new ArrayList<>();
        Collection<LexiconCondition> ignoredConditions = new ArrayList<>();


        // create 2 fragment lexicon conditions.
        ignoredConditions.add(createLexiconCondition(lexiconItem, "somefieldname", "ClassificationApiCollectionSequenceIt:testCollectionSequencePagedByFilter_Chain_Via_LexiconCondition_WithFragments"));
        ignoredConditions.add(createLexiconCondition(lexiconItem, "somefieldname", "ClassificationApiCollectionSequenceIt:testCollectionSequencePagedByFilter_Chain_Via_LexiconCondition_WithFragments2"));

        LexiconCondition tobematched = createLexiconCondition(lexiconItem, "somefieldname", "ClassificationApiCollectionSequenceIt:testCollectionSequencePagedByFilter_Chain_Via_LexiconCondition_WithFragments3", true);
        expectedLexiconConditions.add(tobematched);

        LexiconCondition notMatched = createLexiconCondition(lexiconItem, "somefieldname", "ClassificationApiCollectionSequenceIt:testCollectionSequencePagedByFilter_Chain_Via_LexiconCondition_WithFragments4", true);
        ignoredConditions.add(notMatched);

        // update the collection we need with our lexicon conditions.
        createdCollection.condition = tobematched;
        createdCollection = sut.update(createdCollection);

        dummyColl.condition = notMatched;
        dummyColl = sut.update(dummyColl);


        // Now we should have 4 conditions, 2 isFragment=true and 2 new ones with isFragment=false
        // as they are parented on a collection.
        {
            PageOfResults<Condition> conditionresults = sut.retrieveConditionsPage(new PageRequest(1L, 1L), nonFragmentFilter);
            PageOfResults<Condition> fragmentresults = sut.retrieveConditionFragmentsPage(new PageRequest(1L, 1L));

            assertEquals("Fragment must have 2 new entries", startFragmentCount + 2L, fragmentresults.totalhits.longValue());
            assertEquals("Non fragment must have 2 new entries", startNonFragmentCount + 2L, conditionresults.totalhits.longValue());

            // Ensure that we can filter back using isFragment true and false.
            ObjectNode nodeIsFragmentTrue = nonFragmentFilter.deepCopy();
            nodeIsFragmentTrue.removeAll();
            nodeIsFragmentTrue.put(ApiStrings.Conditions.Arguments.IS_FRAGMENT, true);
            Filter filterOnFragmentTrue = Filter.create(nodeIsFragmentTrue);

            conditionresults = sut.retrieveConditionsPage(new PageRequest(startNonFragmentCount + 1, 10L), nonFragmentFilter);
            fragmentresults = sut.retrieveConditionsPage(new PageRequest(startFragmentCount + 1, 10L), filterOnFragmentTrue);

            assertEquals("Fragment must have 2 new entries", startNonFragmentCount + 2L, conditionresults.totalhits.longValue());
            assertTrue("All fragment results must have isFragment false: ", conditionresults.results.stream().allMatch(u -> u.isFragment == false));
            assertEquals("Non fragment must have 2 new entries", startFragmentCount + 2L, fragmentresults.totalhits.longValue());
            assertTrue("All fragment results must have isFragment true: ", fragmentresults.results.stream().allMatch(u -> u.isFragment == true));
        }


        // Setup all collection sequence / entries
        Collection<CollectionSequence> validCollectionSequences = new LinkedList<>();
        Collection<CollectionSequence> excludedItems = new LinkedList<>();

        {
            CollectionSequenceEntry collectionSequenceEntry = new CollectionSequenceEntry();
            collectionSequenceEntry.order = 100;
            collectionSequenceEntry.collectionIds.add(dummyColl.id);

            CollectionSequenceEntry collectionSequenceEntry2 = new CollectionSequenceEntry();
            collectionSequenceEntry2.order = 200;
            collectionSequenceEntry2.collectionIds.add(createdCollection.id);


            CollectionSequence collectionSequence = new CollectionSequence();
            collectionSequence.name = getUniqueString("aSequence_shouldbereturned_first");
            collectionSequence.description = "Used in testCollectionSequencePagedByFilter_Chain_Via_LexiconCondition ";
            collectionSequence.collectionSequenceEntries.add(collectionSequenceEntry);
            collectionSequence.collectionSequenceEntries.add(collectionSequenceEntry2);
            collectionSequence = sut.create(collectionSequence);

            validCollectionSequences.add(collectionSequence);
        }

        {
            CollectionSequenceEntry collectionSequenceEntry = new CollectionSequenceEntry();
            collectionSequenceEntry.order = 100;
            collectionSequenceEntry.collectionIds.add(createdCollection.id);

            CollectionSequence collectionSequence = new CollectionSequence();
            collectionSequence.name = "bSequence_shouldbereturnedsecond";
            collectionSequence.description = "Used in testCollectionSequencePagedByFilter_Chain_Via_LexiconCondition";
            collectionSequence.collectionSequenceEntries.add(collectionSequenceEntry);
            collectionSequence = sut.create(collectionSequence);

            validCollectionSequences.add(collectionSequence);
        }

        {
            CollectionSequenceEntry collectionSequenceEntry = new CollectionSequenceEntry();
            collectionSequenceEntry.order = 100;
            collectionSequenceEntry.collectionIds.add(dummyColl.id);

            CollectionSequence collectionSequence = new CollectionSequence();
            collectionSequence.name = "cSequence_shouldnotbereturned";
            collectionSequence.description = "Used in testCollectionSequencePagedByFilter_Chain_Via_LexiconCondition_WithFragments";
            collectionSequence.collectionSequenceEntries.add(collectionSequenceEntry);
            collectionSequence = sut.create(collectionSequence);

            excludedItems.add(collectionSequence);
        }


        /**************************************************
         * Now begin the retrievePage by-filter requests..!
         **************************************************/

        // first off make the request to get the Condition, that our Lexicon is part of.
        // Now we have a lexiconCondition, try to get it back by the lexicon itself.
        {
            // add a string test simple string case first.
            Filter filter = Filter.create(ApiStrings.Conditions.Arguments.TYPE, ConditionType.LEXICON.toValue());
            filter.put(ApiStrings.Conditions.Arguments.VALUE, lexiconItem.id);

            PageRequest pageRequest = new PageRequest(1L, 10L);

            PageOfResults<Condition> result = sut.retrieveConditionsPage(pageRequest, filter);
            Assert.assertEquals("Hit count should be equal to expected condition and ignored condition as both have same lexicon id.", expectedLexiconConditions.size() + ignoredConditions.size(), result.totalhits.intValue());
            Assert.assertEquals("Results should be equal expected condition and ignored condition as both have same lexicon id", expectedLexiconConditions.size() + ignoredConditions.size(), result.results.size());

            // alter the reqeust, to only return isFragment=true conditions
            filter.put(ApiStrings.Conditions.Arguments.IS_FRAGMENT, true);
            result = sut.retrieveConditionsPage(pageRequest, filter);

            Assert.assertEquals("Hit count should be equal to expected for this lexicon id.", 2L, result.totalhits.intValue());
            Assert.assertEquals("Results should be equal for lexicon id", 2L, result.results.size());
            Assert.assertTrue("All hits should contain isFragment=true", result.results.stream().allMatch(u -> u.isFragment == true));


            // do the same, but now for isFragment=false;
            filter.remove(ApiStrings.Conditions.Arguments.IS_FRAGMENT);
            filter.put(ApiStrings.Conditions.Arguments.IS_FRAGMENT, false);
            result = sut.retrieveConditionsPage(pageRequest, filter);

            Assert.assertEquals("Hit count should be equal to expected for this lexicon id.", 2L, result.totalhits.intValue());
            Assert.assertEquals("Results should be equal for lexicon id", 2L, result.results.size());
            Assert.assertTrue("All hits should contain isFragment=false", result.results.stream().allMatch(u -> u.isFragment == false));
        }

        // now a request for collections that this condition is part of.
        // first off make the request to get the Condition, that our Lexicon is part of.
        // Now we have a lexiconCondition, try to get it back by the lexicon itself.
        {
            // add a string test simple string case first.
            Filter filter = Filter.create(ApiStrings.DocumentCollections.Arguments.CONDITION + "." + ApiStrings.BaseCrud.Arguments.ID, createdCollection.condition.id);

            PageRequest pageRequest = new PageRequest(1L, 10L);

            PageOfResults<DocumentCollection> result = sut.retrieveCollectionsPage(pageRequest, filter);

            Assert.assertEquals("Hit count should be equal to valid collection size..", 1, result.totalhits.intValue());
            Assert.assertEquals("Results should be equal expected valid collection", createdCollection.id, result.results.stream().findFirst().get().id);
        }


        // finally pass the valid collection to get the collection sequences.
        String columnHeaderToFindByCollectionId = ApiStrings.CollectionSequences.Arguments.COLLECTION_SEQUENCE_ENTRIES + "." + ApiStrings.CollectionSequenceEntries.Arguments.COLLECTION_IDS;

        Filter filter = Filter.create(columnHeaderToFindByCollectionId, Arrays.asList(0L));

        // now ensure we got 0 back with the false id.
        {
            PageOfResults<CollectionSequence> relatedPolicies = sut.retrieveCollectionSequencesPage(new PageRequest(1L, 10L), filter);
            Assert.assertEquals("Should have 0 results", 0, relatedPolicies.totalhits.intValue());
            Assert.assertEquals("Should have 0 results", 0, relatedPolicies.results.size());
        }

        // clear the node and put in the real id now.
        filter = Filter.create(columnHeaderToFindByCollectionId, Arrays.asList(createdCollection.id));

        // now ensure we get all the sequences back for this collection.
        PageOfResults<CollectionSequence> sequence = sut.retrieveCollectionSequencesPage(new PageRequest(1L, 10L), filter);

        checkReturnedResultsAreSorted(validCollectionSequences, sequence, true);

        List<CollectionSequence> sortedItems = sortCollectionSequence(validCollectionSequences, true);

        // Try again now drop page size, to ensure it works.
        PageOfResults<CollectionSequence> result1 = sut.retrieveCollectionSequencesPage(new PageRequest(1L, 1L), filter);
        Assert.assertEquals("Should have both entries", Long.valueOf(validCollectionSequences.size()), result1.totalhits);
        Assert.assertEquals("Only asked for one hit.", 1, result1.results.size());

        Assert.assertEquals("Sorted items should match", sortedItems.stream().findFirst().get().id, result1.results.stream().findFirst().get().id);

        PageOfResults<CollectionSequence> result2 = sut.retrieveCollectionSequencesPage(new PageRequest(2L, 1L), filter);
        Assert.assertEquals("Should have both entries", Long.valueOf(validCollectionSequences.size()), result2.totalhits);
        Assert.assertEquals("Only asked for one hit.", 1, result2.results.size());

        PageOfResults<CollectionSequence> result3 = sut.retrieveCollectionSequencesPage(new PageRequest(3L, 1L), filter);
        Assert.assertEquals("Should have both entries", Long.valueOf(validCollectionSequences.size()), result3.totalhits);
        Assert.assertEquals("asked for one hit on page 3, which shouldn't exit.", 0, result3.results.size());
    }


    @Test
    public void testCollectionSequencePagedByFilter_Chain_Via_LexiconCondition_IsFragmentFalse() {
        Assume.assumeFalse(Assume.AssumeReason.BUG, "CAF-731", "ClassificationApiCollectionSequenceIT::testCollectionSequencePagedByFilter_Chain_Via_LexiconCondition_IsFragmentFalse", "Filtering is not currently supported in MySQL",
                !testingProperties.getWebInHibernate()&&apiProperties.isInApiMode(ApiProperties.ApiMode.web), genericApplicationContext);

        Long startFragmentCount = 0L;
        Long startNonFragmentCount = 0L;

        DocumentCollection createdCollection = createUniqueDocumentCollection("testCollectionSequencePagedByFilter_Chain_Via_LexiconCondition - used col");
        DocumentCollection dummyColl = createUniqueDocumentCollection("testCollectionSequencePagedByFilter_Chain_Via_LexiconCondition - dummy col");

        {
            // Get the base count of condition fragments, and conditions.
            PageOfResults<Condition> conditionresults = sut.retrieveConditionsPage(new PageRequest(1L, 1L), nonFragmentFilter);
            PageOfResults<Condition> fragmentresults = sut.retrieveConditionFragmentsPage(new PageRequest(1L, 1L));

            // store off our starting indexes - useful for non-direct modes where db isn't empty for this tenant.
            startFragmentCount = fragmentresults.totalhits;
            startNonFragmentCount = conditionresults.totalhits;
        }

        // Now start creating things!!!
        Lexicon lexiconItem = createNewLexicon("testCollectionSequencePagedByFilter_Condition", "abc");

        Collection<LexiconCondition> expectedLexiconConditions = new ArrayList<>();
        Collection<LexiconCondition> ignoredConditions = new ArrayList<>();

        // Ensure that we hold of creation of the lexicon, and ensure that it only happens on
        // creation of the collection, so is_fragment = false;
        LexiconCondition tobeMatchedCondition = createLexiconCondition(lexiconItem, "somefieldname", "ClassificationApiCollectionSequenceIt:testCollectionSequencePagedByFilter_Chain_Via_LexiconCondition", true);
        expectedLexiconConditions.add(tobeMatchedCondition);

        // ignored condition, isn't ignored when we request conditions by lexicon - as it has the same lexicon as above,
        // it is instead added to an ignored collection later on which should be returned!
        LexiconCondition notMatchedCondition = createLexiconCondition(lexiconItem, "somefieldname", "ClassificationApiCollectionSequenceIt:testCollectionSequencePagedByFilter_Chain_Via_LexiconCondition2", true);
        ignoredConditions.add(notMatchedCondition);

        // update the collection we need with our lexicon conditions.
        createdCollection.condition = tobeMatchedCondition;
        createdCollection = sut.update(createdCollection);

        dummyColl.condition = ignoredConditions.stream().findFirst().get();
        dummyColl = sut.update(dummyColl);

        {
            PageOfResults<Condition> conditionresults = sut.retrieveConditionsPage(new PageRequest(1L, 1L), nonFragmentFilter);
            PageOfResults<Condition> fragmentresults = sut.retrieveConditionFragmentsPage(new PageRequest(1L, 1L));

            assertEquals("Fragment must be the same as before", startFragmentCount, fragmentresults.totalhits);
            assertEquals("Non fragment must match expected", startNonFragmentCount + 2L, conditionresults.totalhits.longValue());
        }

        // Setup all collection sequence / entries
        Collection<CollectionSequence> validCollectionSequences = new LinkedList<>();
        Collection<CollectionSequence> excludedItems = new LinkedList<>();

        {
            CollectionSequenceEntry collectionSequenceEntry = new CollectionSequenceEntry();
            collectionSequenceEntry.order = 100;
            collectionSequenceEntry.collectionIds.add(dummyColl.id);

            CollectionSequenceEntry collectionSequenceEntry2 = new CollectionSequenceEntry();
            collectionSequenceEntry2.order = 200;
            collectionSequenceEntry2.collectionIds.add(createdCollection.id);

            CollectionSequence collectionSequence = new CollectionSequence();
            collectionSequence.name = getUniqueString("aSequence_shouldbereturned_first");
            collectionSequence.description = "Used in testCollectionSequencePagedByFilter_Chain_Via_LexiconCondition ";
            collectionSequence.collectionSequenceEntries.add(collectionSequenceEntry);
            collectionSequence.collectionSequenceEntries.add(collectionSequenceEntry2);
            collectionSequence = sut.create(collectionSequence);

            validCollectionSequences.add(collectionSequence);
        }

        {
            CollectionSequenceEntry collectionSequenceEntry = new CollectionSequenceEntry();
            collectionSequenceEntry.order = 100;
            collectionSequenceEntry.collectionIds.add(createdCollection.id);

            CollectionSequence collectionSequence = new CollectionSequence();
            collectionSequence.name = "bSequence_shouldbereturnedsecond";
            collectionSequence.description = "Used in testCollectionSequencePagedByFilter_Chain_Via_LexiconCondition";
            collectionSequence.collectionSequenceEntries.add(collectionSequenceEntry);
            collectionSequence = sut.create(collectionSequence);

            validCollectionSequences.add(collectionSequence);
        }

        {
            CollectionSequenceEntry collectionSequenceEntry = new CollectionSequenceEntry();
            collectionSequenceEntry.order = 100;
            collectionSequenceEntry.collectionIds.add(dummyColl.id);

            CollectionSequence collectionSequence = new CollectionSequence();
            collectionSequence.name = "cSequence_shouldnotbereturned";
            collectionSequence.description = "Used in testCollectionSequencePagedByFilter_Chain_Via_LexiconCondition";
            collectionSequence.collectionSequenceEntries.add(collectionSequenceEntry);
            collectionSequence = sut.create(collectionSequence);

            excludedItems.add(collectionSequence);
        }


        /**************************************************
         * Now begin the retrievePage by-filter requests..!
         **************************************************/

        // first off make the request to get the Condition, that our Lexicon is part of.
        // Now we have a lexiconCondition, try to get it back by the lexicon itself.
        {
            Filter filter = Filter.create(ApiStrings.Conditions.Arguments.TYPE, ConditionType.LEXICON.toValue());
            filter.put(ApiStrings.Conditions.Arguments.VALUE, lexiconItem.id);
            PageRequest pageRequest = new PageRequest(1L, 10L);

            PageOfResults<Condition> result = sut.retrieveConditionsPage(pageRequest, filter);
            Assert.assertEquals("Hit count should be equal to expected condition and ignored condition as both have same lexicon id.", expectedLexiconConditions.size() + ignoredConditions.size(), result.totalhits.intValue());
            Assert.assertEquals("Results should be equal expected condition and ignored condition as both have same lexicon id", expectedLexiconConditions.size() + ignoredConditions.size(), result.results.size());
        }

        // now a request for collections that this condition is part of.
        {
            // find collection by condition.id
            Filter filter = Filter.create(ApiStrings.DocumentCollections.Arguments.CONDITION + "." + ApiStrings.BaseCrud.Arguments.ID, createdCollection.condition.id);
            PageRequest pageRequest = new PageRequest(1L, 10L);

            PageOfResults<DocumentCollection> result = sut.retrieveCollectionsPage(pageRequest, filter);

            Assert.assertEquals("Hit count should be equal to valid collection size..", 1, result.totalhits.intValue());
            Assert.assertEquals("Results should be equal expected valid collection", createdCollection.id, result.results.stream().findFirst().get().id);
        }

        // finally pass the valid collection to get the collection sequences.
        String columnHeaderToFindByCollectionId = ApiStrings.CollectionSequences.Arguments.COLLECTION_SEQUENCE_ENTRIES + "." + ApiStrings.CollectionSequenceEntries.Arguments.COLLECTION_IDS;

        // Try with a dummy value first!
        Filter filter = Filter.create(columnHeaderToFindByCollectionId, Arrays.asList(0L));

        // now ensure we got 0 back with the false id.
        {
            PageOfResults<CollectionSequence> relatedPolicies = sut.retrieveCollectionSequencesPage(new PageRequest(1L, 10L), filter);
            Assert.assertEquals("Should have 0 results", 0, relatedPolicies.totalhits.intValue());
            Assert.assertEquals("Should have 0 results", 0, relatedPolicies.results.size());
        }

        // clear the node and put in the real id now.
        filter = Filter.create(columnHeaderToFindByCollectionId, Arrays.asList(createdCollection.id));

        // now ensure we get all the sequences back for this collection.
        PageOfResults<CollectionSequence> sequence = sut.retrieveCollectionSequencesPage(new PageRequest(1L, 10L), filter);

        checkReturnedResultsAreSorted(validCollectionSequences, sequence, true);

        List<CollectionSequence> sortedItems = sortCollectionSequence(validCollectionSequences, true);

        // Try again now drop page size, to ensure it works.
        PageOfResults<CollectionSequence> result1 = sut.retrieveCollectionSequencesPage(new PageRequest(1L, 1L), filter);
        Assert.assertEquals("Should have both entries", Long.valueOf(validCollectionSequences.size()), result1.totalhits);
        Assert.assertEquals("Only asked for one hit.", 1, result1.results.size());

        Assert.assertEquals("Sorted items should match", sortedItems.stream().findFirst().get().id, result1.results.stream().findFirst().get().id);

        PageOfResults<CollectionSequence> result2 = sut.retrieveCollectionSequencesPage(new PageRequest(2L, 1L), filter);
        Assert.assertEquals("Should have both entries", Long.valueOf(validCollectionSequences.size()), result2.totalhits);
        Assert.assertEquals("Only asked for one hit.", 1, result2.results.size());

        PageOfResults<CollectionSequence> result3 = sut.retrieveCollectionSequencesPage(new PageRequest(3L, 1L), filter);
        Assert.assertEquals("Should have both entries", Long.valueOf(validCollectionSequences.size()), result3.totalhits);
        Assert.assertEquals("asked for one hit on page 3, which shouldn't exit.", 0, result3.results.size());
    }

    @Test
    public void testCollectionSequencePagedByFilter_DocumentCollection() {

        DocumentCollection createdCollection = createUniqueDocumentCollection("testCollectionSequencePagedByFilter_DocumentCollection - used col");
        DocumentCollection dummyColl = createUniqueDocumentCollection("testCollectionSequencePagedByFilter_DocumentCollection - dummy col");

        Collection<CollectionSequence> newItems = new LinkedList<>();
        Collection<CollectionSequence> excludedItems = new LinkedList<>();

        {
            CollectionSequenceEntry collectionSequenceEntry = new CollectionSequenceEntry();
            collectionSequenceEntry.order = 100;
            collectionSequenceEntry.collectionIds.add(dummyColl.id);

            CollectionSequenceEntry collectionSequenceEntry2 = new CollectionSequenceEntry();
            collectionSequenceEntry2.order = 200;
            collectionSequenceEntry2.collectionIds.add(createdCollection.id);


            CollectionSequence collectionSequence = new CollectionSequence();
            collectionSequence.name = getUniqueString("aSequence_shouldbereturned_first");
            collectionSequence.description = "Used in testCollectionSequencePagedByFilter_DocumentCollection ";
            collectionSequence.collectionSequenceEntries.add(collectionSequenceEntry);
            collectionSequence.collectionSequenceEntries.add(collectionSequenceEntry2);
            collectionSequence = sut.create(collectionSequence);

            newItems.add(collectionSequence);
        }

        {
            CollectionSequenceEntry collectionSequenceEntry = new CollectionSequenceEntry();
            collectionSequenceEntry.order = 100;
            collectionSequenceEntry.collectionIds.add(createdCollection.id);

            CollectionSequence collectionSequence = new CollectionSequence();
            collectionSequence.name = "bSequence_shouldbereturnedsecond";
            collectionSequence.description = "Used in testCollectionSequencePagedByFilter_DocumentCollection";
            collectionSequence.collectionSequenceEntries.add(collectionSequenceEntry);
            collectionSequence = sut.create(collectionSequence);

            newItems.add(collectionSequence);
        }

        {
            CollectionSequenceEntry collectionSequenceEntry = new CollectionSequenceEntry();
            collectionSequenceEntry.order = 100;
            collectionSequenceEntry.collectionIds.add(dummyColl.id);

            CollectionSequence collectionSequence = new CollectionSequence();
            collectionSequence.name = "cSequence_shouldnotbereturned";
            collectionSequence.description = "Used in testCollectionSequencePagedByFilter_DocumentCollection";
            collectionSequence.collectionSequenceEntries.add(collectionSequenceEntry);
            collectionSequence = sut.create(collectionSequence);

            excludedItems.add(collectionSequence);
        }

        String columnHeaderToFindByCollectionId = ApiStrings.CollectionSequences.Arguments.COLLECTION_SEQUENCE_ENTRIES + "." + ApiStrings.CollectionSequenceEntries.Arguments.COLLECTION_IDS;

        Filter filter = Filter.create(columnHeaderToFindByCollectionId, Arrays.asList(0L));

        // now ensure we got 0 back with the false id.
        {
            PageOfResults<CollectionSequence> relatedPolicies = sut.retrieveCollectionSequencesPage(new PageRequest(1L, 10L), filter);
            Assert.assertEquals("Should have 0 results", 0, relatedPolicies.totalhits.intValue());
            Assert.assertEquals("Should have 0 results", 0, relatedPolicies.results.size());
        }

        // clear the node and put in the real id now.
        filter = Filter.create(columnHeaderToFindByCollectionId, Arrays.asList(createdCollection.id));

        // now ensure we get all the sequences back for this collection.
        PageOfResults<CollectionSequence> sequence = sut.retrieveCollectionSequencesPage(new PageRequest(1L, 10L), filter);

        checkReturnedResultsAreSorted(newItems, sequence, true);

        List<CollectionSequence> sortedItems = sortCollectionSequence(newItems, true);

        // Try again now drop page size, to ensure it works.
        PageOfResults<CollectionSequence> result1 = sut.retrieveCollectionSequencesPage(new PageRequest(1L, 1L), filter);
        Assert.assertEquals("Should have both entries", Long.valueOf(newItems.size()), result1.totalhits);
        Assert.assertEquals("Only asked for one hit.", 1, result1.results.size());

        Assert.assertEquals("Sorted items should match", sortedItems.stream().findFirst().get().id, result1.results.stream().findFirst().get().id);

        PageOfResults<CollectionSequence> result2 = sut.retrieveCollectionSequencesPage(new PageRequest(2L, 1L), filter);
        Assert.assertEquals("Should have both entries", Long.valueOf(newItems.size()), result2.totalhits);
        Assert.assertEquals("Only asked for one hit.", 1, result2.results.size());

        PageOfResults<CollectionSequence> result3 = sut.retrieveCollectionSequencesPage(new PageRequest(3L, 1L), filter);
        Assert.assertEquals("Should have both entries", Long.valueOf(newItems.size()), result3.totalhits);
        Assert.assertEquals("asked for one hit on page 3, which shouldn't exit.", 0, result3.results.size());

    }

    @Test
    public void testCollectionSequencePagedByFilter_DocumentCollection_ReverseCheck() {
        DocumentCollection createdCollection = null;
        {
            DocumentCollection documentCollection = new DocumentCollection();
            documentCollection.name = getUniqueString("testCollectionSequencePagedByFilter_DocumentCollection");
            documentCollection.description = "used as unique doc collection to lookup by...";
            createdCollection = sut.create(documentCollection);
        }

        DocumentCollection createdCollection2 = null;
        {
            DocumentCollection documentCollection = new DocumentCollection();
            documentCollection.name = getUniqueString("testCollectionSequencePagedByFilter_DocumentCollection");
            documentCollection.description = "used as unique doc collection to lookup by...";

            createdCollection2 = sut.create(documentCollection);
        }

        Collection<CollectionSequence> newItems = new LinkedList<>();

        {
            CollectionSequenceEntry collectionSequenceEntry = new CollectionSequenceEntry();
            collectionSequenceEntry.order = 100;
            collectionSequenceEntry.collectionIds.add(createdCollection.id);
            collectionSequenceEntry.collectionIds.add(createdCollection2.id);

            // Ensure to create the sequence with name bSequence first, then aSequence next..
            CollectionSequence collectionSequence = new CollectionSequence();
            collectionSequence.name = getUniqueString("bSequence_");
            collectionSequence.description = "Used in testCollectionSequencePagedByFilter_DocumentCollection ";
            collectionSequence.collectionSequenceEntries.add(collectionSequenceEntry);
            collectionSequence = sut.create(collectionSequence);

            newItems.add(collectionSequence);
        }

        {
            CollectionSequenceEntry collectionSequenceEntry = new CollectionSequenceEntry();
            collectionSequenceEntry.order = 100;
            collectionSequenceEntry.collectionIds.add(createdCollection.id);

            CollectionSequence collectionSequence = new CollectionSequence();
            collectionSequence.name = "aSequence_";
            collectionSequence.description = "Used in testCollectionSequencePagedByFilter_DocumentCollection";
            collectionSequence.collectionSequenceEntries.add(collectionSequenceEntry);
            collectionSequence = sut.create(collectionSequence);

            newItems.add(collectionSequence);
        }

        String columnHeaderToFindByCollectionId = ApiStrings.CollectionSequences.Arguments.COLLECTION_SEQUENCE_ENTRIES + "." + ApiStrings.CollectionSequenceEntries.Arguments.COLLECTION_IDS;

        ObjectNode newNode = new ObjectNode(JsonNodeFactory.instance);

        // Put in a value which can't be in the system to force the filter to not return any results.
        Filter filter = Filter.create(columnHeaderToFindByCollectionId, Arrays.asList(0L));

        // now ensure we got 0 back with the false id.
        {
            PageOfResults<CollectionSequence> relatedPolicies = sut.retrieveCollectionSequencesPage(new PageRequest(1L, 10L), filter);
            Assert.assertEquals("Should have 0 results", 0, relatedPolicies.totalhits.intValue());
            Assert.assertEquals("Should have 0 results", 0, relatedPolicies.results.size());
        }

        // clear the node and put in the real id now.
        filter = Filter.create(columnHeaderToFindByCollectionId, Arrays.asList(createdCollection.id));

        // now ensure we get all the sequences back for this collection.
        PageOfResults<CollectionSequence> sequence = sut.retrieveCollectionSequencesPage(new PageRequest(1L, 10L), filter);

        checkReturnedResultsAreSorted(newItems, sequence, true);
    }

    //CAF-579
    @Test
    public void checkSortByCollectionSequenceName() {
        Assume.assumeFalse(Assume.AssumeReason.BUG, "CAF-731", "ClassificationApiCollectionSequenceIT::checkSortByCollectionSequenceName", "Sorting is not currently supported in MySQL",
                !testingProperties.getWebInHibernate()&&apiProperties.isInApiMode(ApiProperties.ApiMode.web), genericApplicationContext);

        DocumentCollection createdCollection = null;
        {
            DocumentCollection documentCollection = new DocumentCollection();
            documentCollection.name = getUniqueString("testCollectionSequencePagedByFilterAndSorted_DocumentCollection");
            documentCollection.description = "used as unique doc collection to lookup by...";
            createdCollection = sut.create(documentCollection);
        }
        DocumentCollection createdCollection2 = null;
        {
            DocumentCollection documentCollection = new DocumentCollection();
            documentCollection.name = getUniqueString("testCollectionSequencePagedByFilterAndSorted_DocumentCollection");
            documentCollection.description = "used as unique doc collection to lookup by...";

            createdCollection2 = sut.create(documentCollection);
        }
        Collection<CollectionSequence> newItems = new LinkedList<>();
        {
            CollectionSequenceEntry collectionSequenceEntry = new CollectionSequenceEntry();
            collectionSequenceEntry.order = 100;
            collectionSequenceEntry.collectionIds.add(createdCollection.id);
            collectionSequenceEntry.collectionIds.add(createdCollection2.id);

            // Ensure to create the sequence with name bSequence first, then aSequence next..
            CollectionSequence collectionSequence = new CollectionSequence();
            collectionSequence.name = getUniqueString("bSequence_");
            collectionSequence.description = "Used in testCollectionSequencePagedByFilter_DocumentCollection ";
            collectionSequence.collectionSequenceEntries.add(collectionSequenceEntry);
            collectionSequence = sut.create(collectionSequence);

            newItems.add(collectionSequence);
        }
        {
            CollectionSequenceEntry collectionSequenceEntry = new CollectionSequenceEntry();
            collectionSequenceEntry.order = 100;
            collectionSequenceEntry.collectionIds.add(createdCollection.id);

            CollectionSequence collectionSequence = new CollectionSequence();
            collectionSequence.name = getUniqueString("aSequence_");
            collectionSequence.description = "Used in testCollectionSequencePagedByFilter_DocumentCollection";
            collectionSequence.collectionSequenceEntries.add(collectionSequenceEntry);
            collectionSequence = sut.create(collectionSequence);

            newItems.add(collectionSequence);
        }
        {
            CollectionSequenceEntry collectionSequenceEntry = new CollectionSequenceEntry();
            collectionSequenceEntry.order = 100;
            collectionSequenceEntry.collectionIds.add(createdCollection.id);

            CollectionSequence collectionSequence = new CollectionSequence();
            collectionSequence.name = getUniqueString("cSequence_");
            collectionSequence.description = "Used in testCollectionSequencePagedByFilter_DocumentCollection";
            collectionSequence.collectionSequenceEntries.add(collectionSequenceEntry);
            collectionSequence = sut.create(collectionSequence);

            newItems.add(collectionSequence);
        }
        String columnHeaderToFindByCollectionId = ApiStrings.CollectionSequences.Arguments.COLLECTION_SEQUENCE_ENTRIES + "." + ApiStrings.CollectionSequenceEntries.Arguments.COLLECTION_IDS;
        String fieldToSortBy = ApiStrings.CollectionSequences.Arguments.NAME;
        Filter filter = Filter.create(columnHeaderToFindByCollectionId, Arrays.asList(createdCollection.id));
        Sort sort = Sort.create(fieldToSortBy, true);
        PageOfResults<CollectionSequence> sequence = sut.retrieveCollectionSequencesPage(new PageRequest(1L, 10L), filter, sort);
        checkReturnedResultsAreSorted(newItems, sequence, true);
    }

    @Test
    public void checkSortByNameDesc() {
        Assume.assumeFalse(Assume.AssumeReason.BUG, "CAF-731", "ClassificationApiCollectionSequenceIT::checkSortByNameDesc", "Sorting is not currently supported in MySQL",
                !testingProperties.getWebInHibernate()&&apiProperties.isInApiMode(ApiProperties.ApiMode.web), genericApplicationContext);

        DocumentCollection createdCollection = null;
        {
            DocumentCollection documentCollection = new DocumentCollection();
            documentCollection.name = getUniqueString("testCollectionSequencePagedByFilterAndSorted_DocumentCollection");
            documentCollection.description = "used as unique doc collection to lookup by...";
            createdCollection = sut.create(documentCollection);
        }
        DocumentCollection createdCollection2 = null;
        {
            DocumentCollection documentCollection = new DocumentCollection();
            documentCollection.name = getUniqueString("testCollectionSequencePagedByFilterAndSorted_DocumentCollection");
            documentCollection.description = "used as unique doc collection to lookup by...";

            createdCollection2 = sut.create(documentCollection);
        }
        Collection<CollectionSequence> newItems = new LinkedList<>();
        {
            CollectionSequenceEntry collectionSequenceEntry = new CollectionSequenceEntry();
            collectionSequenceEntry.order = 100;
            collectionSequenceEntry.collectionIds.add(createdCollection.id);
            collectionSequenceEntry.collectionIds.add(createdCollection2.id);

            // Ensure to create the sequence with name bSequence first, then aSequence next..
            CollectionSequence collectionSequence = new CollectionSequence();
            collectionSequence.name = getUniqueString("bSequence_");
            collectionSequence.description = "Used in testCollectionSequencePagedByFilter_DocumentCollection ";
            collectionSequence.collectionSequenceEntries.add(collectionSequenceEntry);
            collectionSequence = sut.create(collectionSequence);

            newItems.add(collectionSequence);
        }
        {
            CollectionSequenceEntry collectionSequenceEntry = new CollectionSequenceEntry();
            collectionSequenceEntry.order = 100;
            collectionSequenceEntry.collectionIds.add(createdCollection.id);

            CollectionSequence collectionSequence = new CollectionSequence();
            collectionSequence.name = getUniqueString("aSequence_");
            collectionSequence.description = "Used in testCollectionSequencePagedByFilter_DocumentCollection";
            collectionSequence.collectionSequenceEntries.add(collectionSequenceEntry);
            collectionSequence = sut.create(collectionSequence);

            newItems.add(collectionSequence);
        }
        {
            CollectionSequenceEntry collectionSequenceEntry = new CollectionSequenceEntry();
            collectionSequenceEntry.order = 100;
            collectionSequenceEntry.collectionIds.add(createdCollection.id);

            CollectionSequence collectionSequence = new CollectionSequence();
            collectionSequence.name = getUniqueString("cSequence_");
            collectionSequence.description = "Used in testCollectionSequencePagedByFilter_DocumentCollection";
            collectionSequence.collectionSequenceEntries.add(collectionSequenceEntry);
            collectionSequence = sut.create(collectionSequence);

            newItems.add(collectionSequence);
        }
        String columnHeaderToFindByCollectionId = ApiStrings.CollectionSequences.Arguments.COLLECTION_SEQUENCE_ENTRIES + "." + ApiStrings.CollectionSequenceEntries.Arguments.COLLECTION_IDS;
        String fieldToSortBy = ApiStrings.CollectionSequences.Arguments.NAME;
        Filter filter = Filter.create(columnHeaderToFindByCollectionId, Arrays.asList(createdCollection.id));
        Sort sort = Sort.create(fieldToSortBy, false);
        PageOfResults<CollectionSequence> sequence = sut.retrieveCollectionSequencesPage(new PageRequest(1L, 10L), filter, sort);
        checkReturnedResultsAreSorted(newItems, sequence, false);
    }


    @Test
    public void checkJustSortNoFilter() {
        Assume.assumeFalse(Assume.AssumeReason.BUG, "CAF-731", "ClassificationApiCollectionSequenceIT::checkJustSortNoFilter", "Filtering is not currently supported in MySQL",
                !testingProperties.getWebInHibernate()&&apiProperties.isInApiMode(ApiProperties.ApiMode.web), genericApplicationContext);

        DocumentCollection createdCollection = null;
        {
            DocumentCollection documentCollection = new DocumentCollection();
            documentCollection.name = getUniqueString("testCollectionSequencePagedByFilterAndSorted_DocumentCollection");
            documentCollection.description = "used as unique doc collection to lookup by...";
            createdCollection = sut.create(documentCollection);
        }
        DocumentCollection createdCollection2 = null;
        {
            DocumentCollection documentCollection = new DocumentCollection();
            documentCollection.name = getUniqueString("testCollectionSequencePagedByFilterAndSorted_DocumentCollection");
            documentCollection.description = "used as unique doc collection to lookup by...";

            createdCollection2 = sut.create(documentCollection);
        }
        Collection<CollectionSequence> newItems = new LinkedList<>();
        {
            CollectionSequenceEntry collectionSequenceEntry = new CollectionSequenceEntry();
            collectionSequenceEntry.order = 100;
            collectionSequenceEntry.collectionIds.add(createdCollection.id);
            collectionSequenceEntry.collectionIds.add(createdCollection2.id);
            // Ensure to create the sequence with name bSequence first, then aSequence next..
            CollectionSequence collectionSequence = new CollectionSequence();
            collectionSequence.name = getUniqueString("bSequence_");
            collectionSequence.description = "Used in testCollectionSequencePagedByFilter_DocumentCollection ";
            collectionSequence.collectionSequenceEntries.add(collectionSequenceEntry);
            collectionSequence = sut.create(collectionSequence);
            newItems.add(collectionSequence);
        }
        {
            CollectionSequenceEntry collectionSequenceEntry = new CollectionSequenceEntry();
            collectionSequenceEntry.order = 100;
            collectionSequenceEntry.collectionIds.add(createdCollection.id);
            CollectionSequence collectionSequence = new CollectionSequence();
            collectionSequence.name = getUniqueString("aSequence_");
            collectionSequence.description = "Used in testCollectionSequencePagedByFilter_DocumentCollection";
            collectionSequence.collectionSequenceEntries.add(collectionSequenceEntry);
            collectionSequence = sut.create(collectionSequence);
            newItems.add(collectionSequence);
        }
        {
            CollectionSequenceEntry collectionSequenceEntry = new CollectionSequenceEntry();
            collectionSequenceEntry.order = 100;
            collectionSequenceEntry.collectionIds.add(createdCollection.id);
            CollectionSequence collectionSequence = new CollectionSequence();
            collectionSequence.name = getUniqueString("cSequence_");
            collectionSequence.description = "Used in testCollectionSequencePagedByFilter_DocumentCollection";
            collectionSequence.collectionSequenceEntries.add(collectionSequenceEntry);
            collectionSequence = sut.create(collectionSequence);
            newItems.add(collectionSequence);
        }
        String fieldToSortBy = ApiStrings.CollectionSequences.Arguments.NAME;
        Sort sort = Sort.create(fieldToSortBy, false);
        PageOfResults<CollectionSequence> sequence = sut.retrieveCollectionSequencesPage(new PageRequest(1L, 10L), sort);
        checkReturnedResultsAreSorted(newItems, sequence, false);

        //Now test again with ascending order
        sort = Sort.create(fieldToSortBy, true);
        sequence = sut.retrieveCollectionSequencesPage(new PageRequest(1L, 10L), sort);
        checkReturnedResultsAreSorted(newItems, sequence, true);
    }

    @Test
    public void checkSortCollectionSequenceByDescription() {
        Assume.assumeFalse(Assume.AssumeReason.BUG, "CAF-731", "ClassificationApiCollectionSequenceIT::checkSortCollectionSequenceByDescription", "Sorting is not currently supported in MySQL",
                !testingProperties.getWebInHibernate()&&apiProperties.isInApiMode(ApiProperties.ApiMode.web), genericApplicationContext);

        Collection<CollectionSequence> newItems = new LinkedList<>();
        {
            // Ensure to create the sequence with name bSequence first, then aSequence next..
            // Ensure that the name / desc values match, as our integration tests sort method uses
            // name to sort expected collection.
            CollectionSequence collectionSequence = new CollectionSequence();
            collectionSequence.name = getUniqueString("aSequence_");
            collectionSequence.description = getUniqueString("aTest_ensureSortedByThisField:");
            collectionSequence = sut.create(collectionSequence);

            newItems.add(collectionSequence);
        }
        // ensure its not just sorting by id, so put cSequence before bSequence
        {
            CollectionSequence collectionSequence = new CollectionSequence();
            collectionSequence.name = getUniqueString("cSequence_");
            collectionSequence.description = getUniqueString("cTest_ensureSortedByThisField:");
            collectionSequence = sut.create(collectionSequence);

            newItems.add(collectionSequence);
        }
        {
            CollectionSequence collectionSequence = new CollectionSequence();
            collectionSequence.name = getUniqueString("bSequence_");
            collectionSequence.description = getUniqueString("bTest_ensureSortedByThisField:");
            collectionSequence = sut.create(collectionSequence);

            newItems.add(collectionSequence);
        }

        String fieldToSortBy = ApiStrings.CollectionSequences.Arguments.DESCRIPTION;
        //sort asc
        {
            Sort sort = Sort.create(fieldToSortBy, true);
            PageOfResults<CollectionSequence> sequence = sut.retrieveCollectionSequencesPage(new PageRequest(1L, 10L), null, sort);
            checkReturnedResultsAreSorted(newItems, sequence, true);
        }

        // try again but sort desc.
        {
            Sort sort = Sort.create(fieldToSortBy, false);
            PageOfResults<CollectionSequence> sequence = sut.retrieveCollectionSequencesPage(new PageRequest(1L, 10L), null, sort);
            checkReturnedResultsAreSorted(newItems, sequence, false);
        }
    }

    @Test
    public void checkSortByInvalidField() {
        DocumentCollection createdCollection = null;
        {
            DocumentCollection documentCollection = new DocumentCollection();
            documentCollection.name = getUniqueString("testCollectionSequencePagedByFilterAndSorted_DocumentCollection");
            documentCollection.description = "used as unique doc collection to lookup by...";
            createdCollection = sut.create(documentCollection);
        }

        DocumentCollection createdCollection2 = null;
        {
            DocumentCollection documentCollection = new DocumentCollection();
            documentCollection.name = getUniqueString("testCollectionSequencePagedByFilterAndSorted_DocumentCollection");
            documentCollection.description = "used as unique doc collection to lookup by...";

            createdCollection2 = sut.create(documentCollection);
        }
        Collection<CollectionSequence> newItems = new LinkedList<>();
        {
            CollectionSequenceEntry collectionSequenceEntry = new CollectionSequenceEntry();
            collectionSequenceEntry.order = 100;
            collectionSequenceEntry.collectionIds.add(createdCollection.id);
            collectionSequenceEntry.collectionIds.add(createdCollection2.id);

            // Ensure to create the sequence with name bSequence first, then aSequence next..
            CollectionSequence collectionSequence = new CollectionSequence();
            collectionSequence.name = getUniqueString("bSequence_");
            collectionSequence.description = "Used in testCollectionSequencePagedByFilter_DocumentCollection ";
            collectionSequence.collectionSequenceEntries.add(collectionSequenceEntry);
            collectionSequence = sut.create(collectionSequence);

            newItems.add(collectionSequence);
        }
        {
            CollectionSequenceEntry collectionSequenceEntry = new CollectionSequenceEntry();
            collectionSequenceEntry.order = 100;
            collectionSequenceEntry.collectionIds.add(createdCollection.id);

            CollectionSequence collectionSequence = new CollectionSequence();
            collectionSequence.name = getUniqueString("aSequence_");
            collectionSequence.description = "Used in testCollectionSequencePagedByFilter_DocumentCollection";
            collectionSequence.collectionSequenceEntries.add(collectionSequenceEntry);
            collectionSequence = sut.create(collectionSequence);
            newItems.add(collectionSequence);
        }
        String columnHeaderToFindByCollectionId = ApiStrings.CollectionSequences.Arguments.COLLECTION_SEQUENCE_ENTRIES + "." + ApiStrings.CollectionSequenceEntries.Arguments.COLLECTION_IDS;
        //Field does not have @SortName annotation
        String fieldToSortBy = ApiStrings.CollectionSequences.Arguments.LAST_MODIFIED;
        Filter filter = Filter.create(columnHeaderToFindByCollectionId, Arrays.asList(createdCollection.id));
        Sort sort = Sort.create(fieldToSortBy, true);
        shouldThrow(o -> {
            PageOfResults<CollectionSequence> sequence = sut.retrieveCollectionSequencesPage(new PageRequest(1L, 10L), filter, sort);
        });
    }

    @Test
    public void testSortByNameWithDuplicateNames(){
        Assume.assumeFalse(Assume.AssumeReason.BUG, "CAF-731", "ClassificationApiCollectionSequenceIT::testSortByNameWithDuplicateNames", "Sorting is not currently supported in MySQL",
                !testingProperties.getWebInHibernate()&&apiProperties.isInApiMode(ApiProperties.ApiMode.web), genericApplicationContext);

        DocumentCollection createdCollection = null;
        {
            DocumentCollection documentCollection = new DocumentCollection();
            documentCollection.name = getUniqueString("testCollectionSequencePagedByFilterAndSorted_DocumentCollection");
            documentCollection.description = "used as unique doc collection to lookup by...";
            createdCollection = sut.create(documentCollection);
        }
        DocumentCollection createdCollection2 = null;
        {
            DocumentCollection documentCollection = new DocumentCollection();
            documentCollection.name = getUniqueString("testCollectionSequencePagedByFilterAndSorted_DocumentCollection");
            documentCollection.description = "used as unique doc collection to lookup by...";

            createdCollection2 = sut.create(documentCollection);
        }
        //Copy unique string to var to duplicate across sequences
        String duplicateName = getUniqueString("bSequence_");
        Collection<CollectionSequence> newItems = new LinkedList<>();
        {
            CollectionSequenceEntry collectionSequenceEntry = new CollectionSequenceEntry();
            collectionSequenceEntry.order = 100;
            collectionSequenceEntry.collectionIds.add(createdCollection.id);
            collectionSequenceEntry.collectionIds.add(createdCollection2.id);
            // Ensure to create the sequence with name bSequence first, then aSequence next..
            CollectionSequence collectionSequence = new CollectionSequence();
            collectionSequence.name = duplicateName;
            collectionSequence.description = "Used in testCollectionSequencePagedByFilter_DocumentCollection ";
            collectionSequence.collectionSequenceEntries.add(collectionSequenceEntry);
            collectionSequence = sut.create(collectionSequence);
            newItems.add(collectionSequence);
        }
        {
            CollectionSequenceEntry collectionSequenceEntry = new CollectionSequenceEntry();
            collectionSequenceEntry.order = 100;
            collectionSequenceEntry.collectionIds.add(createdCollection.id);
            CollectionSequence collectionSequence = new CollectionSequence();
            collectionSequence.name = duplicateName;
            collectionSequence.description = "Used in testCollectionSequencePagedByFilter_DocumentCollection";
            collectionSequence.collectionSequenceEntries.add(collectionSequenceEntry);
            collectionSequence = sut.create(collectionSequence);
            newItems.add(collectionSequence);
        }
        {
            CollectionSequenceEntry collectionSequenceEntry = new CollectionSequenceEntry();
            collectionSequenceEntry.order = 100;
            collectionSequenceEntry.collectionIds.add(createdCollection.id);
            CollectionSequence collectionSequence = new CollectionSequence();
            collectionSequence.name = getUniqueString("cSequence_");
            collectionSequence.description = "Used in testCollectionSequencePagedByFilter_DocumentCollection";
            collectionSequence.collectionSequenceEntries.add(collectionSequenceEntry);
            collectionSequence = sut.create(collectionSequence);
            newItems.add(collectionSequence);
        }
        String fieldToSortBy = ApiStrings.CollectionSequences.Arguments.NAME;
        Sort sort = Sort.create(fieldToSortBy, false);
        PageOfResults<CollectionSequence> sequence = sut.retrieveCollectionSequencesPage(new PageRequest(1L, 10L), sort);
        //First two sequences have duplicate name, check if secondary order is correct.
        checkReturnedResultsAreSorted(newItems, sequence, false);
    }

    //PD-857
    @Test
    public void checkEmptySequenceList() {

        // Create a collection sequence for this project Id to ensure that paging is not being called in web mode
        createCollectionSequence(null, "EmptySequence");

        ArrayList<Long> ids = new ArrayList<>();
        Collection<CollectionSequence> retrievedSequences = sut.retrieveCollectionSequences(ids);
        Assert.assertTrue("Should retrieve an empty list", retrievedSequences.isEmpty());
    }

    @Test
    public void testUpdateSequenceWithNewCollections_Additive(){
        Assume.assumeFalse(Assume.AssumeReason.BY_DESIGN, "CAF-1051", "ClassificationApiCollectionSequenceIT::testUpdateSequenceWithNewCollections_Additive", "Batch creating is not supported in MySQL.",
                !testingProperties.getWebInHibernate() && apiProperties.isInApiMode(ApiProperties.ApiMode.web), genericApplicationContext);

        DocumentCollection collection = new DocumentCollection();
        collection.name = "Collection1";
        collection = sut.create(collection);
        CollectionSequence collectionSequence = createCollectionSequence(collection, "Sequence");

        List<CollectionSequenceEntry> createdCollectionSequenceEntries = new ArrayList<>(collectionSequence.collectionSequenceEntries);

        DocumentCollection collection2 = new DocumentCollection();
        collection2.name = "Collection2";
        collection2 = sut.create(collection2);

        DocumentCollection collection3 = new DocumentCollection();
        collection3.name = "Collection3";
        collection3 = sut.create(collection3);

        collectionSequence.collectionSequenceEntries.clear();
        CollectionSequenceEntry newEntry1 = new CollectionSequenceEntry();
        newEntry1.collectionIds = new HashSet<>(Arrays.asList(collection2.id));
        CollectionSequenceEntry newEntry2 = new CollectionSequenceEntry();
        newEntry2.collectionIds = new HashSet<>(Arrays.asList(collection3.id));


        collectionSequence.collectionSequenceEntries.addAll(Arrays.asList(newEntry1, newEntry2));

        CollectionSequence updatedCollectionSequence = sut.update(collectionSequence, UpdateBehaviourType.ADD);

        Assert.assertEquals("Sequence should now have 3 collections.", 3, updatedCollectionSequence.collectionSequenceEntries.size());
        List<CollectionSequenceEntry> collectionSequenceEntries = updatedCollectionSequence.collectionSequenceEntries;
        createdCollectionSequenceEntries.addAll(Arrays.asList(newEntry1, newEntry2));

        for(int i = 0; i < collectionSequenceEntries.size(); i++){
            CollectionSequenceEntry createdEntry = createdCollectionSequenceEntries.get(i);
            CollectionSequenceEntry updatedEntry = collectionSequenceEntries.get(i);
            Assert.assertEquals("Entry Ids should match", createdEntry.collectionIds.stream().findFirst().get(), updatedEntry.collectionIds.stream().findFirst().get());
        }
    }

    private void checkReturnedResultsAreSorted(Collection<CollectionSequence> newItems, PageOfResults<CollectionSequence> sequence, boolean ascending) {
        List<CollectionSequence> sortedItems = sortCollectionSequence(newItems, ascending);

        assertEquals(sortedItems.size(), newItems.size());

        Assert.assertEquals("Total hits should match created sequences.", newItems.size(), sequence.totalhits.intValue());
        Assert.assertEquals("Results should match created sequences.", newItems.size(), sequence.results.size());

        // Default sort is asc, so ensure ascend names.
        List<CollectionSequence> returnedOrder = sequence.results.stream().collect(Collectors.toList());

        for (int i = 0; i < sortedItems.size(); i++) {
            assertEquals("Order should be asc. id not correct for index: " + String.valueOf(i), sortedItems.get(i).id, returnedOrder.get(i).id);
            assertEquals("Order should be asc. Name not correct for index: " + String.valueOf(i), sortedItems.get(i).name, returnedOrder.get(i).name);
        }
    }

    private List<CollectionSequence> sortCollectionSequence(Collection<CollectionSequence> newItems, boolean ascending) {
        List<CollectionSequence> sortedList = new ArrayList<>();
        if(ascending) {
            sortedList = newItems.stream().sorted((c1, c2) -> {
                if (c1.name == null) {
                    if (c2.name == null) {
                        return c1.id.compareTo(c2.id);
                    }
                    return -1; // null comes before any text
                } else if (c2.name == null) {
                    return 1; // source has text, dest null, so after
                } else {
                    return c1.name.compareToIgnoreCase(c2.name);
                }
            }).collect(Collectors.toList());
        } else {
            sortedList = newItems.stream().sorted((c1, c2) -> {
                if (c1.name == null) {
                    if (c2.name == null) {
                        return c1.id.compareTo(c2.id);
                    }
                    return -1; // null comes before any text
                } else if (c2.name == null) {
                    return -1; // source has text, dest null, so after
                } else {
                    return c2.name.compareToIgnoreCase(c1.name);
                }
            }).collect(Collectors.toList());
        }
        return sortedList;
    }



    private void checkSortedItems(Collection<CollectionSequence> newItems) {
        List<CollectionSequence> sortedItems;
        PageRequest pageRequest = new PageRequest(1L, 10L);
        PageOfResults<CollectionSequence> pageOfResults = sut.retrieveCollectionSequencesPage(pageRequest);

        Collection<CollectionSequence> allItems = new ArrayList<>();

        if (pageOfResults.totalhits > newItems.size()) {
            // Go through and reqeust all pages, and ensure the sorting matches that expected.
            // Now finally ensure we get all the items in one big page to compare.
            pageOfResults = sut.retrieveCollectionSequencesPage(new PageRequest(1L, pageOfResults.totalhits + 10L));
            allItems.addAll(pageOfResults.results);

            org.junit.Assert.assertEquals("Sorted Pages of result should match", Long.valueOf(allItems.size()), pageOfResults.totalhits);
        } else {
            // all items is just what we have created dont need to request it all again.
            allItems = newItems;
        }

        sortedItems = sortCollectionSequence(allItems, true);

        assertEquals(Long.valueOf(sortedItems.size()), pageOfResults.totalhits);
        List<CollectionSequence> returnedOrder = pageOfResults.results.stream().collect(Collectors.toList());

        for (int i = 0; i < sortedItems.size(); i++) {
            assertEquals("Order of id not correct for index: " + String.valueOf(i), sortedItems.get(i).id, returnedOrder.get(i).id);
            assertEquals("Order of Name not correct for index: " + String.valueOf(i), sortedItems.get(i).name, returnedOrder.get(i).name);
            assertEquals("Order of Description not correct for index: " + String.valueOf(i), sortedItems.get(i).description, returnedOrder.get(i).description);
        }
    }
}
