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
package com.github.cafdataprocessing.corepolicy.api;

import com.github.cafdataprocessing.corepolicy.common.ApiProperties;
import com.github.cafdataprocessing.corepolicy.common.ApiStrings;
import com.github.cafdataprocessing.corepolicy.common.PolicyApi;
import com.github.cafdataprocessing.corepolicy.common.dto.*;
import com.github.cafdataprocessing.corepolicy.common.dto.conditions.*;

import com.github.cafdataprocessing.corepolicy.testing.Assume;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static com.github.cafdataprocessing.corepolicy.TestHelper.shouldThrow;
import static org.junit.Assert.*;

public class ClassificationApiCollectionIT extends ClassificationApiTestBase {

    @Test
    public void testMoveToNewCollection(){
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

        DocumentCollection saved = sut.create(collection);

        DocumentCollection collection2 = new DocumentCollection();
        collection2.name = "to move 2.";
        collection2.description = "test update to move";
        collection2= sut.create(collection2);

        collection2.condition = existsCondition;

        Collection<DocumentCollection> originalUpdateQuery = sut.retrieveCollections(Arrays.asList(saved.id), true, true);

        DocumentCollection saved2 = sut.update(collection2);

        Collection<DocumentCollection> originalUpdateQuery2 = sut.retrieveCollections(Arrays.asList(saved.id), true, true);

        getLogger().info("what...");

    }


    @Test
    public void testAddCollection() throws Exception {
        DocumentCollection collection = new DocumentCollection();
        collection.name = "name Citroên";
        collection.description = "description Citroên";
        collection.policyIds = new HashSet<>(Arrays.asList(10000000L, 20000000L));

        ExistsCondition existsCondition = new ExistsCondition();
        existsCondition.field = "field Citroên";
        collection.condition = existsCondition;

        //Policies should not exist
        shouldThrow(u -> sut.create(collection));

        PolicyApi policyApi = genericApplicationContext.getBean(PolicyApi.class);
        Policy newUniquePolicy1 = policyApi.create(PolicyApiIT.newUniqueIndexPolicy());
        Policy newUniquePolicy2 = policyApi.create(PolicyApiIT.newUniqueExternalPolicy());

        collection.policyIds = new HashSet<>(Arrays.asList(newUniquePolicy1.id, newUniquePolicy2.id));

        DocumentCollection created = sut.create(collection);

        assertEquals(collection.name, created.name);
        assertEquals(collection.description, created.description);
        assertArrayEquals(collection.policyIds.toArray(), created.policyIds.toArray());
    }

    //PD-571 - Collection name gets truncated when name begins with digits
    @Test
    public void testAddCollectionWithDigitInName() throws Exception {
        DocumentCollection collection = new DocumentCollection();
        collection.name = "123 name Citroên";
        collection.description = "description Citroên";

        DocumentCollection created = sut.create(collection);

        assertEquals(collection.name, created.name);
        assertEquals(collection.description, created.description);
    }

   @Test
    public void testAddCollectionWithObjectInName() throws Exception {
        DocumentCollection collection = new DocumentCollection();
        collection.name = "{'json':true}";
        collection.description = "{'json':true}";

        DocumentCollection created = sut.create(collection);

        assertEquals(collection.name, created.name);
        assertEquals(collection.description, created.description);
    }

    @Test
    public void testAddDeletedPolicyCollection() throws Exception {
        PolicyApi policyApi = genericApplicationContext.getBean(PolicyApi.class);


        Policy newUniquePolicy = policyApi.create(PolicyApiIT.newUniqueIndexPolicy());
        policyApi.deletePolicy(newUniquePolicy.id);

        DocumentCollection collection = new DocumentCollection();
        collection.name = "name";

        collection.description = "description";
        collection.policyIds = new HashSet<>(Arrays.asList(newUniquePolicy.id));

        ExistsCondition existsCondition = new ExistsCondition();
        existsCondition.field = "field";
        collection.condition = existsCondition;

        //Policy should not exist
        shouldThrow(u -> sut.create(collection));
    }

    @Test
    public void testUpdatePolicyCollection() throws Exception {
        DocumentCollection collection = new DocumentCollection();
        collection.name = "name";
        collection.description = "description";

        PolicyApi policyApi = genericApplicationContext.getBean(PolicyApi.class);
        Policy newUniquePolicy1 = policyApi.create(PolicyApiIT.newUniqueIndexPolicy());
        Policy newUniquePolicy2 = policyApi.create(PolicyApiIT.newUniqueExternalPolicy());

        collection.policyIds = new HashSet<>(Arrays.asList(newUniquePolicy1.id));

        DocumentCollection created = sut.create(collection);

        //Add policy
        created.policyIds = new HashSet<>(Arrays.asList(newUniquePolicy1.id, newUniquePolicy2.id));

        DocumentCollection updated = sut.update(created);

        assertArrayEquals(created.policyIds.toArray(), updated.policyIds.toArray());

        //Remove policy
        updated.policyIds = new HashSet<>(Arrays.asList(newUniquePolicy2.id));

        updated = sut.update(updated);

        assertArrayEquals(Arrays.asList(newUniquePolicy2.id).toArray(), updated.policyIds.toArray());
    }

    //PD-512
    @Test
    public void testUpdateNewPolicyCollection() throws Exception {
        DocumentCollection collection = new DocumentCollection();
        collection.name = "name";
        collection.description = "description";

        DocumentCollection created = sut.create(collection);

        PolicyApi policyApi = genericApplicationContext.getBean(PolicyApi.class);
        Policy newUniquePolicy1 = PolicyApiIT.newUniqueIndexPolicy();
        newUniquePolicy1 = policyApi.create(newUniquePolicy1);
        //Add policy
        created.policyIds = new HashSet<>(Arrays.asList(newUniquePolicy1.id));

        DocumentCollection updated = sut.update(created);

        DocumentCollection retrieved= sut.retrieveCollections(Arrays.asList(updated.id)).stream().findFirst().get();
        assertArrayEquals(Arrays.asList(newUniquePolicy1.id).toArray(), retrieved.policyIds.toArray());
    }

    @Test
    public void testUpdateCollection() throws Exception {
        DocumentCollection collection = new DocumentCollection();
        collection.name = "name";
        collection.description = "description";

        DocumentCollection created = sut.create(collection);
        created.name = "new name Citroên";
        created.description = "new description Citroên";

        DocumentCollection updated = sut.update(created);
        assertEquals(created.name, updated.name);
        assertEquals(created.description, updated.description);
    }

    @Test
    public void testUpdateCollectionCondition() throws Exception {
        DocumentCollection collection = new DocumentCollection();
        collection.name = "name";
        collection.description = "description";
        collection.policyIds = new HashSet<>(Arrays.asList(10000000L, 20000000L));

        ExistsCondition existsCondition = new ExistsCondition();
        existsCondition.field = "field";
        collection.condition = existsCondition;

        shouldThrow(u->sut.create(collection));

        PolicyApi policyApi = genericApplicationContext.getBean(PolicyApi.class);
        Policy newUniquePolicy1 = policyApi.create(PolicyApiIT.newUniqueIndexPolicy());
        Policy newUniquePolicy2 = policyApi.create(PolicyApiIT.newUniqueExternalPolicy());

        collection.policyIds = new HashSet<>(Arrays.asList(newUniquePolicy1.id, newUniquePolicy2.id));

        DocumentCollection created = sut.create(collection);

        Long createdConditionId = created.condition.id;


        ((ExistsCondition) created.condition).field = "Test update";

        DocumentCollection update = sut.update(created);
        Long updatedConditionId = update.condition.id;

        assertEquals("Test update", ((ExistsCondition) update.condition).field);
        assertNotEquals(updatedConditionId, createdConditionId);
    }

    @Test
    public void testCreateCollectionWithSamePolicyTypesCondition() throws Exception {
        DocumentCollection collection = new DocumentCollection();
        collection.name = "name";
        collection.description = "description";

        ExistsCondition existsCondition = new ExistsCondition();
        existsCondition.field = "field";
        collection.condition = existsCondition;


        PolicyApi policyApi = genericApplicationContext.getBean(PolicyApi.class);

        // policies must be of same type to throw!
        Policy newUniquePolicy1 = policyApi.create(PolicyApiIT.newUniqueIndexPolicy());
        Policy newUniquePolicy2 = policyApi.create(PolicyApiIT.newUniqueIndexPolicy());

        collection.policyIds = new HashSet<>(Arrays.asList(newUniquePolicy1.id, newUniquePolicy2.id));

        shouldThrow(u -> sut.create(collection));


    }

    @Test
    public void testUpdateCollectionWithSamePolicyTypesCondition() throws Exception {
        DocumentCollection collection = new DocumentCollection();
        collection.name = "name";
        collection.description = "description";

        ExistsCondition existsCondition = new ExistsCondition();
        existsCondition.field = "field";
        collection.condition = existsCondition;

        PolicyApi policyApi = genericApplicationContext.getBean(PolicyApi.class);
        Policy newUniquePolicy1 = policyApi.create(PolicyApiIT.newUniqueIndexPolicy());
        Policy newUniquePolicy2 = policyApi.create(PolicyApiIT.newUniqueIndexPolicy());

        collection = sut.create(collection);


        collection.policyIds = new HashSet<>(Arrays.asList(newUniquePolicy1.id, newUniquePolicy2.id));

        final DocumentCollection updateCollection = collection;

        shouldThrow(u -> sut.update(updateCollection));
    }

    @Test
    public void testRetrieveCollectionsBy_FilterNotCondition() {
        Assume.assumeFalse(Assume.AssumeReason.BUG, "CAF-731", "ClassificationApiCollectionIT::testRetrieveCollectionsBy_FilterNotCondition", "Filtering is not currently supported in MySQL",
                !testingProperties.getWebInHibernate()&&apiProperties.isInApiMode(ApiProperties.ApiMode.web), genericApplicationContext);

        DocumentCollection createdCollection = createUniqueDocumentCollection("testRetrieveCollectionsBy_FilterNotCondition");

        ExistsCondition existsCondition = new ExistsCondition();
        existsCondition.field = "my field1";
        existsCondition.order = 100;

        NotCondition condition = new NotCondition();
        condition.name = "testRetrieveCollectionsBy_FilterNotCondition";
        condition.condition = existsCondition;

        createdCollection.condition = condition;

        DocumentCollection savedCollection = sut.update(createdCollection);

        NotCondition resultCondition = (NotCondition) sut.retrieveConditions(Arrays.asList(savedCollection.condition.id), true).stream().findFirst().get();

        // now try to get the collection from each condition - first off the boolean.
        PageOfResults<DocumentCollection> result = sut.retrieveCollectionsPage(new PageRequest(), Filter.create("condition.id", resultCondition.id));

        Assert.assertEquals("Must be 1 collection", 1L, result.totalhits.longValue());
        Assert.assertEquals("Must be 1 collection", savedCollection.id, result.results.stream().findFirst().get().id);

        // try again except with child condition of the NOT.
        result = sut.retrieveCollectionsPage(new PageRequest(), Filter.create("condition.id", resultCondition.condition.id));

        Assert.assertEquals("Must be 1 collection", 1L, result.totalhits.longValue());
        Assert.assertEquals("Must be 1 collection", savedCollection.id, result.results.stream().findFirst().get().id);
    }

    @Test
    public void testRetrieveCollectionsBy_Filter_FragmentCondition() {
        Assume.assumeFalse(Assume.AssumeReason.BUG, "CAF-731", "ClassificationApiCollectionIT::testRetrieveCollectionsBy_FilterFragmentCondition", "Filtering is not currently supported in MySQL",
                !testingProperties.getWebInHibernate()&&apiProperties.isInApiMode(ApiProperties.ApiMode.web), genericApplicationContext);

        DocumentCollection createdCollection = createUniqueDocumentCollection("testRetrieveCollectionsBy_Filter_FragmentCondition Collection 1");
        DocumentCollection createdCollection2 = createUniqueDocumentCollection("testRetrieveCollectionsBy_Filter_FragmentCondition Collection 2");

        ExistsCondition existsCondition = new ExistsCondition();
        existsCondition.field = "my field1";
        existsCondition.order = 100;
        existsCondition.name = "my field1 - this is the actual fragment....";

        existsCondition = sut.create(existsCondition);

        FragmentCondition fragmentCondition = new FragmentCondition();
        fragmentCondition.value = existsCondition.id;
        fragmentCondition.name = "Fragment pointer - lives outside collection";

        // Create this on its own so it becomes a fragment it its own right - this doesn't ever belong to
        // a collection - ensure lookup shows nothing!
        fragmentCondition = sut.create(fragmentCondition);

        // Attach another to each collection, and let it create it, in this way we get a new condition
        // attached to the collection.
        FragmentCondition fragmentCondition1 = new FragmentCondition();
        fragmentCondition1.value = existsCondition.id;
        fragmentCondition1.name = "Fragment pointer - lives in collection1";

        FragmentCondition fragmentCondition2 = new FragmentCondition();
        fragmentCondition2.value = existsCondition.id;
        fragmentCondition2.name = "Fragment pointer - lives in collection2";

        createdCollection.condition = fragmentCondition1;
        DocumentCollection savedCollection = sut.update(createdCollection);

        createdCollection2.condition = fragmentCondition2;
        DocumentCollection savedCollection2 = sut.update(createdCollection2);

        // update our conditions with the real info from the DB which has been saved!
        fragmentCondition1 = (FragmentCondition) sut.retrieveConditions(Arrays.asList(savedCollection.condition.id), true).stream().findFirst().get();
        fragmentCondition2 = (FragmentCondition) sut.retrieveConditions(Arrays.asList(savedCollection2.condition.id), true).stream().findFirst().get();

        {
            // now try to get the collection from each condition - first off try with
            // fragment which isn't part of any collection.
            PageOfResults<DocumentCollection> result = sut.retrieveCollectionsPage(new PageRequest(), Filter.create("condition.id", fragmentCondition.id));

            Assert.assertEquals("Must be 0 collections", 0L, result.totalhits.longValue());
            Assert.assertEquals("Must be 0 collections", 0, result.results.size());
        }

        {
            // now try to get the collection from each condition - first off the boolean.
            PageOfResults<DocumentCollection> result = sut.retrieveCollectionsPage(new PageRequest(), Filter.create("condition.id", fragmentCondition1.id));

            Assert.assertEquals("Must be 1 collection", 1L, result.totalhits.longValue());
            Assert.assertEquals("Must be 1 collection", savedCollection.id, result.results.stream().findFirst().get().id);
        }

        {
            // try again except with child condition.
            PageOfResults<DocumentCollection> result = sut.retrieveCollectionsPage(new PageRequest(), Filter.create("condition.id", fragmentCondition2.id));

            Assert.assertEquals("Must be 1 collection", 1L, result.totalhits.longValue());
            Assert.assertEquals("Must be 1 collection", savedCollection2.id, result.results.stream().findFirst().get().id);
        }

        {
            // try again except with the actual value of the fragment conditions, this should bring back both the collections, not just
            // 1!!!!
            PageOfResults<DocumentCollection> result = sut.retrieveCollectionsPage(new PageRequest(), Filter.create("condition.id", fragmentCondition2.value));

            Assert.assertEquals("Must match collections", 2L, result.totalhits.longValue());
            Assert.assertNotNull(result.results.stream().findFirst().filter(u->u.id==createdCollection.id));
            Assert.assertNotNull(result.results.stream().findFirst().filter(u->u.id==createdCollection2.id));
        }
    }

    @Test
    public void testRetrieveCollectionsBy_Filter_PolicyIds() throws Exception {
        Assume.assumeFalse(Assume.AssumeReason.BUG, "CAF-731", "ClassificationApiCollectionIT::testRetrieveCollectionsBy_Filter_PolicyIds", "Filtering is not currently supported in MySQL",
                !testingProperties.getWebInHibernate()&&apiProperties.isInApiMode(ApiProperties.ApiMode.web), genericApplicationContext);

        DocumentCollection collection = new DocumentCollection();
        collection.name = "testRetreiveCollectionsBy_Filter_PolicyIds";
        collection.description = "test usage....";

        ExistsCondition existsCondition = new ExistsCondition();
        existsCondition.field = "field-blah";
        existsCondition.name = "testRetreiveCollectionsBy_Filter_PolicyIds";
        collection.condition = existsCondition;

        PolicyApi policyApi = genericApplicationContext.getBean(PolicyApi.class);
        Policy newUniquePolicy1 = policyApi.create(PolicyApiIT.newUniqueIndexPolicy());
        Policy newUniquePolicy2 = policyApi.create(PolicyApiIT.newUniqueExternalPolicy());

        collection.policyIds = new HashSet<>(Arrays.asList(newUniquePolicy1.id, newUniquePolicy2.id));

        DocumentCollection created = sut.create(collection);

        assertArrayEquals(collection.policyIds.toArray(), created.policyIds.toArray());

        // Now check we can return the same collection using the policy ids as a filter!
        {
            PageOfResults<DocumentCollection> result = sut.retrieveCollectionsPage(new PageRequest(), Filter.create(ApiStrings.DocumentCollections.Arguments.POLICY_IDS, newUniquePolicy1.id));

            Assert.assertEquals("Must match collections", 1L, result.totalhits.longValue());
            Assert.assertNotNull(result.results.stream().findFirst().filter(u -> u.id == collection.id));
        }

        // do same again for the other policy id.
        {
            PageOfResults<DocumentCollection> result = sut.retrieveCollectionsPage(new PageRequest(), Filter.create(ApiStrings.DocumentCollections.Arguments.POLICY_IDS, newUniquePolicy2.id));

            Assert.assertEquals("Must match collections", 1L, result.totalhits.longValue());
            Assert.assertNotNull(result.results.stream().findFirst().filter(u -> u.id == collection.id));
        }

    }
    @Test
    public void testRetrieveCollectionsBy_FilterBooleanCondition() {
        Assume.assumeFalse(Assume.AssumeReason.BUG, "CAF-731", "ClassificationApiCollectionIT::testRetrieveCollectionsBy_FilterBooleanCondition", "Filtering is not currently supported in MySQL",
                !testingProperties.getWebInHibernate()&&apiProperties.isInApiMode(ApiProperties.ApiMode.web), genericApplicationContext);

        DocumentCollection createdCollection = createUniqueDocumentCollection("testRetrieveCollectionsBy_FilterBooleanCondition");

        BooleanCondition topBooleanCondition = new BooleanCondition();
        topBooleanCondition.name = "testRetrieveCollectionsBy_FilterBooleanCondition bool1";
        topBooleanCondition.operator = BooleanOperator.AND;
        topBooleanCondition.children = new ArrayList<>();

        BooleanCondition booleanCondition1 = new BooleanCondition();
        booleanCondition1.name = "testRetrieveCollectionsBy_FilterBooleanCondition bool2";
        booleanCondition1.operator = BooleanOperator.AND;
        booleanCondition1.children = new ArrayList<>();

        BooleanCondition booleanCondition2 = new BooleanCondition();
        booleanCondition2.name = "testRetrieveCollectionsBy_FilterBooleanCondition bool3";
        booleanCondition2.operator = BooleanOperator.AND;
        booleanCondition2.children = new ArrayList<>();

        {
            ExistsCondition existsCondition = new ExistsCondition();
            existsCondition.field = "my field1";
            existsCondition.order = 100;
            booleanCondition1.children.add(existsCondition);
        }
        {
            ExistsCondition existsCondition = new ExistsCondition();
            existsCondition.field = "my field2";
            existsCondition.order = 200;
            booleanCondition1.children.add(existsCondition);
        }
        {
            ExistsCondition existsCondition = new ExistsCondition();
            existsCondition.field = "my field3";
            existsCondition.order = 100;
            booleanCondition2.children.add(existsCondition);
        }
        {
            ExistsCondition existsCondition = new ExistsCondition();
            existsCondition.field = "my field4";
            existsCondition.order = 200;
            booleanCondition2.children.add(existsCondition);
        }

        topBooleanCondition.children.add(booleanCondition1);
        topBooleanCondition.children.add(booleanCondition2);

        createdCollection.condition = topBooleanCondition;

        DocumentCollection savedCollection = sut.update(createdCollection);

        BooleanCondition resultCondition = (BooleanCondition) sut.retrieveConditions(Arrays.asList(savedCollection.condition.id), true).stream().findFirst().get();

        // now try to get the collection from each condition - first off the boolean.
        PageOfResults<DocumentCollection> result = sut.retrieveCollectionsPage(new PageRequest(), Filter.create("condition.id", resultCondition.id));

        Assert.assertEquals("Must be 1 collection", 1L, result.totalhits.longValue());
        Assert.assertEquals("Must be 1 collection", savedCollection.id, result.results.stream().findFirst().get().id);

        // try to get the parent collection for each child condition we have
        checkParentCollectionMatchForEachChildCondition(savedCollection, resultCondition);
    }

    private void checkParentCollectionMatchForEachChildCondition(DocumentCollection savedCollection, BooleanCondition resultCondition) {
        PageOfResults<DocumentCollection> result;
        for( Condition conditionItem : resultCondition.children)
        {
            result = sut.retrieveCollectionsPage( new PageRequest(), Filter.create("condition.id", conditionItem.id));

            Assert.assertEquals("Must be 1 collection", 1L, result.totalhits.longValue());
            Assert.assertEquals("Must be 1 collection", savedCollection.id, result.results.stream().findFirst().get().id );

            // Check each of its children if appropriate.
            if ( conditionItem instanceof BooleanCondition) {
                checkParentCollectionMatchForEachChildCondition(savedCollection, (BooleanCondition)conditionItem);
            }
        }
    }

    @Test
    public void testRetrieveCollections() throws Exception {
        DocumentCollection collection = new DocumentCollection();
        collection.name = "name";
        collection.description = "description";

        DocumentCollection created = sut.create(collection);

        Collection<DocumentCollection> documentCollections = sut.retrieveCollections(Arrays.asList(created.id));
        assertEquals(1, documentCollections.size());
    }

    @Test
    public void testRetrieveCollectionWithChildren() throws Exception {
        DocumentCollection collection = new DocumentCollection();
        collection.name = "name";
        collection.description = "description";

        ExistsCondition existsCondition = new ExistsCondition();
        existsCondition.field = "field1";

        collection.condition = existsCondition;

        DocumentCollection created = sut.create(collection);

        Collection<DocumentCollection> documentCollections = sut.retrieveCollections(Arrays.asList(created.id), true, true);
        assertTrue(documentCollections.stream().findFirst().get().condition instanceof ExistsCondition);
    }

    @Test
    public void testReturnPageOrder() {
        Collection<DocumentCollection> newItems = new LinkedList<>();

        {
            DocumentCollection item = new DocumentCollection();
            item.name = "Cname";
            item.description = "description";

            item = sut.create(item);

            newItems.add(item);
        }
        {
            DocumentCollection item = new DocumentCollection();
            item.name = "Aname";
            item.description = "description";

            item = sut.create(item);

            newItems.add(item);
        }
        {
            DocumentCollection item = new DocumentCollection();
            item.name = "aname";
            item.description = "description";

            item = sut.create(item);

            newItems.add(item);
        }
        {
            DocumentCollection item = new DocumentCollection();
            item.name = "Bname";
            item.description = "description";

            item = sut.create(item);

            newItems.add(item);
        }

        checkSortedItems(newItems);
    }

    @Test
    public void testUpdateCollection_AddPolicies() throws IOException {
        Assume.assumeFalse(Assume.AssumeReason.BY_DESIGN, "CAF-1051", "ClassificationApiCollectionIT::testUpdateCollection_AddPolicies", "Batch creating is not supported in MySQL.",
                !testingProperties.getWebInHibernate() && apiProperties.isInApiMode(ApiProperties.ApiMode.web), genericApplicationContext);

        DocumentCollection collection = new DocumentCollection();
        collection.name = "doc1";
        collection.description = "description";
        PolicyApi policyApi = genericApplicationContext.getBean(PolicyApi.class);
        Policy newUniquePolicy1 = policyApi.create(PolicyApiIT.newUniqueIndexPolicy());
        Policy newUniquePolicy2 = policyApi.create(PolicyApiIT.newUniqueExternalPolicy());

        collection.policyIds = new HashSet<>(Arrays.asList(newUniquePolicy1.id, newUniquePolicy2.id));

        collection = sut.create(collection);

        //Now update with another Policy ID.
        Policy newUniquePolicy3 = policyApi.create(PolicyApiIT.newUniqueIndexPolicy());
        collection.policyIds.clear();
        collection.policyIds = new HashSet<>(Arrays.asList(newUniquePolicy3.id));

        DocumentCollection updatedCollection = sut.update(collection, UpdateBehaviourType.ADD);
        Assert.assertEquals("Updated collection should have 3 policies", 3, updatedCollection.policyIds.size());
        List<Long> idList = new ArrayList<>();
        idList.add(newUniquePolicy1.id);
        idList.add(newUniquePolicy2.id);
        idList.add(newUniquePolicy3.id);

        //Check the Policy Ids on the updated collection are what we expect.
        updatedCollection.policyIds.forEach( e -> Assert.assertTrue("Returned Policy Ids should match", idList.contains(e)));
    }

    private void checkSortedItems(Collection<DocumentCollection> newItems) {
        List<DocumentCollection> sortedItems;
        PageRequest pageRequest = new PageRequest(1L, 10L);
        PageOfResults<DocumentCollection> pageOfResults = sut.retrieveCollectionsPage(pageRequest);
        Collection<DocumentCollection> allItems = new ArrayList<>();

        if ( pageOfResults.totalhits > newItems.size() ) {
            // Go through and reqeust all pages, and ensure the sorting matches that expected.
            // Now finally ensure we get all the items in one big page to compare.
            pageOfResults = sut.retrieveCollectionsPage(new PageRequest(1L, pageOfResults.totalhits + 10L));
            allItems.addAll(pageOfResults.results);

            Assert.assertEquals("Sorted Pages of result should match", Long.valueOf(allItems.size()), pageOfResults.totalhits);
        }
        else {
            // all items is just what we have created dont need to request it all again.
            allItems = newItems;
        }

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

        assertEquals(Long.valueOf(sortedItems.size()), pageOfResults.totalhits);
        List<DocumentCollection> returnedOrder = pageOfResults.results.stream().collect(Collectors.toList());

        for (int i = 0; i < sortedItems.size(); i++) {
            assertEquals("Order of id not correct for index: " + String.valueOf(i), sortedItems.get(i).id, returnedOrder.get(i).id);
            assertEquals("Order of Name not correct for index: " + String.valueOf(i), sortedItems.get(i).name, returnedOrder.get(i).name);
        }
    }
}