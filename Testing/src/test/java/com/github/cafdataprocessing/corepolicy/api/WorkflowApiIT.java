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

import com.github.cafdataprocessing.corepolicy.common.*;
import com.github.cafdataprocessing.corepolicy.common.dto.*;
import com.google.common.base.Strings;
import com.github.cafdataprocessing.corepolicy.repositories.RepositoryConnectionProvider;
import com.github.cafdataprocessing.corepolicy.repositories.RepositoryType;
import com.github.cafdataprocessing.corepolicy.testing.Assume;
import com.github.cafdataprocessing.corepolicy.testing.IntegrationTestBase;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.security.InvalidParameterException;
import java.sql.Connection;
import java.util.*;
import java.util.stream.Collectors;

import static com.github.cafdataprocessing.corepolicy.TestHelper.getUniqueString;
import static com.github.cafdataprocessing.corepolicy.TestHelper.shouldThrow;
import static org.junit.Assert.*;

/**
 * Integration tests for the Workflow API
 */
public class WorkflowApiIT extends IntegrationTestBase {

    protected WorkflowApi sut;
    protected ClassificationApi classificationApi;
    protected PolicyApi policyApi;

    private static CollectionSequence collectionSequence = null;
    private static CollectionSequence collectionSequence2 = null;
    private static CollectionSequence collectionSequence3 = null;

    public WorkflowApiIT() {
        sut = genericApplicationContext.getBean(WorkflowApi.class);
        classificationApi = genericApplicationContext.getBean(ClassificationApi.class);
        policyApi = genericApplicationContext.getBean(PolicyApi.class);

    }

    @BeforeClass
    public static void beforeClass()
    {
        CreateOrGetApplicationContext();
    }

    @Before
    public void before() {
        // we can only run in direct-hibernate or web-hibernate for now!
        Assume.assumeTrue(Assume.AssumeReason.DEBT, "WorkflowApiIT",
                "Workflow Api is only within hibernate repository at present.",
                (apiProperties.isInApiMode(
                        ApiProperties.ApiMode.direct) && apiProperties.isInRepository(
                        ApiProperties.ApiDirectRepository.hibernate)) || (apiProperties.isInApiMode(
                        ApiProperties.ApiMode.web) && testingProperties.getWebInHibernate()),
                genericApplicationContext);

        // create sequences which are reused, only once.
        if (collectionSequence == null) {
            collectionSequence = createCollectionSequenceLocal((String) getUniqueString("aColSeqName_"), getUniqueString("aColSeqDesc_"), null, true, true);
        }
        if (collectionSequence2 == null) {
            collectionSequence2 = createCollectionSequenceLocal((String) getUniqueString("cColSeqName_"), getUniqueString("cColSeqDesc_"), null, true, true);
        }
        if (collectionSequence3 == null) {
            collectionSequence3 = createCollectionSequenceLocal((String) getUniqueString("bColSeqName_"), getUniqueString("bColSeqDesc_"), null, true, true);
        }
    }

    @Override
    protected Connection getConnectionToClearDown() {
        if(apiProperties.getMode().equalsIgnoreCase("direct")) {
            RepositoryConnectionProvider repositoryConnectionProvider = genericApplicationContext.getBean(RepositoryConnectionProvider.class);
            return repositoryConnectionProvider.getConnection(RepositoryType.CONDITION_ENGINE);
        }
        throw new InvalidParameterException(apiProperties.getMode());
    }

    @Test
    public void testSequenceWorkflowCreation() {

        // try with only name - should work fine!, build working test first,
        // as failing tests, may just fail because of something else :)
        {
            SequenceWorkflow sequenceWorkflow = new SequenceWorkflow();
            sequenceWorkflow.name = "tryme";
            // can we create without any sequence ids - we should be able to...
            SequenceWorkflow returned = sut.create(sequenceWorkflow);

            Assert.assertNotNull(returned);
            Assert.assertTrue(returned.id != 0);
        }

        // try without name
        {
            SequenceWorkflow sequenceWorkflow = new SequenceWorkflow();
            sequenceWorkflow.description = "test my description makes no diff";
            sequenceWorkflow.notes = "test notes makes no difference";
            shouldThrow((o)-> sut.create(sequenceWorkflow));
        }

        // try with all out fields, and no ids match fine!
        {
            SequenceWorkflow sequenceWorkflow = new SequenceWorkflow();
            sequenceWorkflow.name = "tryme";
            sequenceWorkflow.description = "test my description";
            sequenceWorkflow.notes = "and any notes left?";

            // can we create without any sequence ids - we should be able to...
            SequenceWorkflow returned = sut.create(sequenceWorkflow);

            checkSequenceWorkflow(sequenceWorkflow, returned);
        }

        // try with all fields, now add some collection sequence ids!
        {
            SequenceWorkflow sequenceWorkflow = new SequenceWorkflow();
            sequenceWorkflow.name = "tryme";
            sequenceWorkflow.description = "test my description";
            sequenceWorkflow.notes = "and any notes left?";

            SequenceWorkflowEntry entry = new SequenceWorkflowEntry();
            entry.collectionSequenceId = 1000000L;

            sequenceWorkflow.sequenceWorkflowEntries = Arrays.asList( entry );

            // can we create without any sequence ids - we should be able to...
            shouldThrow((o) -> sut.create(sequenceWorkflow));
        }

        // try with all fields, and some real collection sequence ids!
        {
            SequenceWorkflow sequenceWorkflow = new SequenceWorkflow();
            sequenceWorkflow.name = "tryme";
            sequenceWorkflow.description = "test my description";
            sequenceWorkflow.notes = "and any notes left?";

            SequenceWorkflowEntry entry = new SequenceWorkflowEntry();
            entry.collectionSequenceId = collectionSequence.id;

            sequenceWorkflow.sequenceWorkflowEntries = Arrays.asList( entry );

            // can we create with out matching collection sequence ids - we should be able to...
            SequenceWorkflow returned = sut.create(sequenceWorkflow);

            checkSequenceWorkflow(sequenceWorkflow, returned);
        }

        // try with all fields, and some real collection sequence ids and order them....
        {
            SequenceWorkflow sequenceWorkflow = new SequenceWorkflow();
            sequenceWorkflow.name = "tryme";
            sequenceWorkflow.description = "test my description";
            sequenceWorkflow.notes = "and any notes left?";

            SequenceWorkflowEntry entry = new SequenceWorkflowEntry();
            entry.order = 100;
            entry.collectionSequenceId = collectionSequence.id;

            SequenceWorkflowEntry entry2 = new SequenceWorkflowEntry();
            entry2.order = 200;
            entry2.collectionSequenceId = collectionSequence2.id;

            sequenceWorkflow.sequenceWorkflowEntries = Arrays.asList( entry, entry2);

            // can we create with out matching collection sequence ids - we should be able to...
            SequenceWorkflow returned = sut.create(sequenceWorkflow);

            checkSequenceWorkflow(sequenceWorkflow, returned);
        }

        // try with all fields, check we support mixture of some ordered fields and some not.
        {
            SequenceWorkflow sequenceWorkflow = new SequenceWorkflow();
            sequenceWorkflow.name = "tryme";
            sequenceWorkflow.description = "test my description";
            sequenceWorkflow.notes = "and any notes left?";

            SequenceWorkflowEntry entry = new SequenceWorkflowEntry();
            entry.collectionSequenceId = collectionSequence.id;

            SequenceWorkflowEntry entry2 = new SequenceWorkflowEntry();
            entry2.order = 200;
            entry2.collectionSequenceId = collectionSequence2.id;

            sequenceWorkflow.sequenceWorkflowEntries = Arrays.asList( entry, entry2);

            // can we create with out matching collection sequence ids - we should be able to...
            SequenceWorkflow returned = sut.create(sequenceWorkflow);

            checkSequenceWorkflow(sequenceWorkflow, returned);

            // check returned field matching the first entry has a new order of 300
            SequenceWorkflowEntry returnedEntry =
                    returned.sequenceWorkflowEntries.stream().filter(u->u.collectionSequenceId.equals(entry.collectionSequenceId)).findFirst().get();

            assertEquals("Null source entry order, must be set to highest specified order, plus 100", 300L, Short.toUnsignedLong( returnedEntry.order));
        }
    }

    @Test
    public void testSequenceWorkflowUpdate(){

            // try with only name - should work fine!, build working test first,
            // as failing tests, may just fail because of something else :)
            {
                SequenceWorkflow sequenceWorkflow = new SequenceWorkflow();
                sequenceWorkflow.name = "tryme";
                // can we create without any sequence ids - we should be able to...
                SequenceWorkflow returned = sut.create(sequenceWorkflow);

                Assert.assertNotNull(returned);
                Assert.assertTrue(returned.id != 0);

                // now it exists, check that we can update it
                Long checkId = returned.id;

                returned.description = "add some description to it?";

                returned = sut.update(returned);

                Assert.assertNotNull(returned);
                Assert.assertTrue(returned.id != 0);
                Assert.assertEquals("Id must stay the same, ", checkId, returned.id);

                // go on and add the entries to the same item, and check.
                SequenceWorkflowEntry entry = new SequenceWorkflowEntry();
                entry.collectionSequenceId = collectionSequence.id;

                SequenceWorkflowEntry entry2 = new SequenceWorkflowEntry();
                entry2.order = 200;
                entry2.collectionSequenceId = collectionSequence2.id;

                returned.sequenceWorkflowEntries = Arrays.asList(entry, entry2);
                SequenceWorkflow workflowResult = sut.update(returned);

                checkSequenceWorkflow( returned, workflowResult );
            }
    }


    @Test
    public void testSequenceWorkflowDeletion() {

        // try with only name - should work fine!, build working test first,
        // as failing tests, may just fail because of something else :)
        SequenceWorkflow sequenceWorkflow = new SequenceWorkflow();
        sequenceWorkflow.name = "tryme";
        sequenceWorkflow.description = "any description";

        // go on and add the entries to the same item, and check.
        SequenceWorkflowEntry entry = new SequenceWorkflowEntry();
        entry.order = 400;
        entry.collectionSequenceId = collectionSequence.id;

        SequenceWorkflowEntry entry2 = new SequenceWorkflowEntry();
        entry2.order = 200;
        entry2.collectionSequenceId = collectionSequence2.id;

        sequenceWorkflow.sequenceWorkflowEntries = Arrays.asList(entry, entry2);

        SequenceWorkflow returned = sut.create(sequenceWorkflow);
        checkSequenceWorkflow(sequenceWorkflow, returned);

        // Delete the item
        Long idToCheck = returned.id;

        // check we can return it
        SequenceWorkflow gotIt = sut.retrieveSequenceWorkflow(idToCheck);

        Assert.assertNotNull(gotIt);
        Assert.assertTrue(gotIt.id != 0);

        // delete it.
        sut.deleteSequenceWorkflow(idToCheck);

        // check we cant get it now.
        // check we can return it
        shouldThrow((o) -> sut.retrieveSequenceWorkflow(idToCheck));
    }

    @Test
    public void testSequenceWorkflowPreventsCollectionSequenceDeletion(){

        // try with only name - should work fine!, build working test first,
        // as failing tests, may just fail because of something else :)
        SequenceWorkflow sequenceWorkflow = new SequenceWorkflow();
        sequenceWorkflow.name = "tryme";
        sequenceWorkflow.description = "any description";

        CollectionSequence cs = createCollectionSequenceLocal((String) null, null, null);

        // go on and add the entries to the same item, and check.
        SequenceWorkflowEntry entry = new SequenceWorkflowEntry();
        entry.collectionSequenceId = cs.id;

        sequenceWorkflow.sequenceWorkflowEntries = Arrays.asList(entry);

        SequenceWorkflow returned = sut.create(sequenceWorkflow);
        checkSequenceWorkflow(sequenceWorkflow, returned);

        // Try to delete the collection sequence.
        shouldThrow((o) -> classificationApi.deleteCollectionSequence(cs.id));
    }

    @Test
    public void testSequenceWorkflow_Paging() {

        PageOfResults<SequenceWorkflow> results = sut.retrieveSequenceWorkflowsPage(new PageRequest(1L, 1L));

        // store off our starting indexes - useful for non-direct modes where db isn't empty for this tenant.
        Long startCount = results.totalhits;

        // try with only name - should work fine!, build working test first,
        // as failing tests, may just fail because of something else :)
        SequenceWorkflow sequenceWorkflow = new SequenceWorkflow();
        sequenceWorkflow.name = "tryme";
        sequenceWorkflow.description = "any description";

        // Check that we can get this item back in a page of results!
        sut.create(sequenceWorkflow);

        // Confirm that our paging goes up by one.
        {
            results = sut.retrieveSequenceWorkflowsPage(new PageRequest(1L, 1L));

            // we should be up by one
            assertEquals("Fragment must be the same as before", startCount + 1L, results.totalhits.longValue());
        }

    }

    @Test
    public void testSequenceWorkflow_FilterPaging() {

        Long startCount = 0L;

        {
            PageOfResults<SequenceWorkflow> results = sut.retrieveSequenceWorkflowsPage(new PageRequest(1L, 1L));

            // store off our starting indexes - useful for non-direct modes where db isn't empty for this tenant.
            startCount = results.totalhits;
        }

        // try with only name - should work fine!, build working test first,
        // as failing tests, may just fail because of something else :)
        SequenceWorkflow sequenceWorkflow = new SequenceWorkflow();
        sequenceWorkflow.name = "tryme";
        sequenceWorkflow.description = "any description";

        // go on and add the entries to the same item, and check.
        SequenceWorkflowEntry entry = new SequenceWorkflowEntry();
        entry.order = 400;
        entry.collectionSequenceId = collectionSequence.id;

        SequenceWorkflowEntry entry2 = new SequenceWorkflowEntry();
        entry2.order = 200;
        entry2.collectionSequenceId = collectionSequence2.id;

        sequenceWorkflow.sequenceWorkflowEntries = Arrays.asList(entry, entry2);

        // Check that we can get this item back in a page of results!
        sequenceWorkflow = sut.create(sequenceWorkflow);

        // Confirm that our paging goes up by one.
        {
            PageOfResults<SequenceWorkflow> results = sut.retrieveSequenceWorkflowsPage(new PageRequest(1L, 1L));

            // we should be up by one
            assertEquals("Fragment must be the same as before", startCount + 1L, results.totalhits.longValue());
        }

        // now try using Filter Paging
        {
            Filter filter = Filter.create(ApiStrings.SequenceWorkflow.Arguments.SEQUENCE_ENTRIES + "." + ApiStrings.SequenceWorkflowEntry.Arguments.SEQUENCE_ID, collectionSequence.id);
                PageOfResults<SequenceWorkflow> results = sut.retrieveSequenceWorkflowsPage(new PageRequest(1L, 1L), filter);

            assertEquals("must return our item", 1L, results.totalhits.longValue());
            assertEquals("must return our item", sequenceWorkflow.id, results.results.stream().findFirst().get().id);
        }


        // also using second collection should work!
        {
            Filter filter = Filter.create(ApiStrings.SequenceWorkflow.Arguments.SEQUENCE_ENTRIES + "." + ApiStrings.SequenceWorkflowEntry.Arguments.SEQUENCE_ID, collectionSequence2.id);
            PageOfResults<SequenceWorkflow> results = sut.retrieveSequenceWorkflowsPage(new PageRequest(1L, 1L), filter);

            assertEquals("must return our item", 1L, results.totalhits.longValue());
            assertEquals("must return our item", sequenceWorkflow.id, results.results.stream().findFirst().get().id);
        }

        // duff id should return nothing.
        {
            Filter filter = Filter.create(ApiStrings.SequenceWorkflow.Arguments.SEQUENCE_ENTRIES + "." + ApiStrings.SequenceWorkflowEntry.Arguments.SEQUENCE_ID, 100000L);
            PageOfResults<SequenceWorkflow> results = sut.retrieveSequenceWorkflowsPage(new PageRequest(1L, 1L), filter);

            assertEquals("must return our item", 0L, results.totalhits.longValue());
        }

        // check if you can filter by name.
        {
            Filter filter = Filter.create(ApiStrings.SequenceWorkflow.Arguments.NAME, sequenceWorkflow.name);
            PageOfResults<SequenceWorkflow> results = sut.retrieveSequenceWorkflowsPage(new PageRequest(1L,1L),filter);
            assertEquals("must return our item", 1L, results.totalhits.longValue());
            assertEquals("must return our item", sequenceWorkflow.id, results.results.stream().findFirst().get().id);
        }
    }

    @Test
    public void testSequenceWorkflowByCollectionSequenceName_FilterPaging() {

        // try with only name - should work fine!, build working test first,
        // as failing tests, may just fail because of something else :)
        SequenceWorkflow sequenceWorkflow = new SequenceWorkflow();
        sequenceWorkflow.name = "tryme";
        sequenceWorkflow.description = "any description";

        // ensure to create the name not in id / alpha ordering, incase we fool ourselves its ordered correctly!
        CollectionSequence colSequence1 = createCollectionSequenceLocal(getUniqueString("aTestSequence"), null, null);
        CollectionSequence colSequence2 = createCollectionSequenceLocal(getUniqueString("cTestSequence"), null, null);
        CollectionSequence colSequence3 = createCollectionSequenceLocal(getUniqueString("bTestSequence"), null, null);

        // go on and add the entries to the same item, and check.
        SequenceWorkflowEntry entry = new SequenceWorkflowEntry();
        entry.order = 400;
        entry.collectionSequenceId = colSequence1.id;

        SequenceWorkflowEntry entry2 = new SequenceWorkflowEntry();
        entry2.order = 200;
        entry2.collectionSequenceId = colSequence2.id;

        SequenceWorkflowEntry entry3 = new SequenceWorkflowEntry();
        entry3.order = 200;
        entry3.collectionSequenceId = colSequence3.id;
        
        sequenceWorkflow.sequenceWorkflowEntries = Arrays.asList(entry, entry2, entry3);

        // Check that we can get this item back in a page of results!
        sequenceWorkflow = sut.create(sequenceWorkflow);

        // now try using Filter Paging
        {
            Filter filter = Filter.create(ApiStrings.SequenceWorkflow.Arguments.SEQUENCE_ENTRIES + "." + ApiStrings.SequenceWorkflowEntry.Arguments.SEQUENCE_ID, colSequence1.id);
            PageOfResults<SequenceWorkflow> results = sut.retrieveSequenceWorkflowsPage(new PageRequest(1L, 1L), filter);

            assertEquals("must return our item", 1L, results.totalhits.longValue());
            assertEquals("must return our item", sequenceWorkflow.id, results.results.stream().findFirst().get().id);
        }

        // now try to use the name field to filter down to the correct entry!
        // now try using Filter Paging
        {
            Filter filter = Filter.create(ApiStrings.SequenceWorkflow.Arguments.SEQUENCE_ENTRIES + "." + ApiStrings.SequenceWorkflowEntry.Arguments.SEQUENCE + "." + ApiStrings.CollectionSequences.Arguments.NAME, colSequence1.name);
            PageOfResults<SequenceWorkflow> results = sut.retrieveSequenceWorkflowsPage(new PageRequest(1L, 6L), filter);

            assertEquals("must return our item", 1L, results.totalhits.longValue());
            assertEquals("must return our item", sequenceWorkflow.id, results.results.stream().findFirst().get().id);
        }
        
        // now try to use the name again but for the last entry...
        {
            Filter filter = Filter.create(ApiStrings.SequenceWorkflow.Arguments.SEQUENCE_ENTRIES + "." + ApiStrings.SequenceWorkflowEntry.Arguments.SEQUENCE + "." + ApiStrings.CollectionSequences.Arguments.NAME, colSequence3.name);
            PageOfResults<SequenceWorkflow> results = sut.retrieveSequenceWorkflowsPage(new PageRequest(1L, 6L), filter);

            assertEquals("must return our item", 1L, results.totalhits.longValue());
            assertEquals("must return our item", sequenceWorkflow.id, results.results.stream().findFirst().get().id);
        }
        
        // duff name should return nothing.
        {
            Filter filter = Filter.create(ApiStrings.SequenceWorkflow.Arguments.SEQUENCE_ENTRIES + "." + ApiStrings.SequenceWorkflowEntry.Arguments.SEQUENCE + "." + ApiStrings.CollectionSequences.Arguments.NAME, "AnyOldName");
            PageOfResults<SequenceWorkflow> results = sut.retrieveSequenceWorkflowsPage(new PageRequest(1L, 1L), filter);

            assertEquals("must return our item", 0L, results.totalhits.longValue());
        }
    }
    
    @Test
    public void testSequenceWorkflowEntriesByCollectionSequenceName_FilterPaging() {

        // try with only name - should work fine!, build working test first,
        // as failing tests, may just fail because of something else :)
        SequenceWorkflow sequenceWorkflow = new SequenceWorkflow();
        sequenceWorkflow.name = "tryme";
        sequenceWorkflow.description = "any description";

        // ensure to create the name not in id / alpha ordering, incase we fool ourselves its ordered correctly!
        CollectionSequence colSequence1 = createCollectionSequenceLocal(getUniqueString("aTestSequence"), null, null);
        CollectionSequence colSequence2 = createCollectionSequenceLocal(getUniqueString("cTestSequence"), null, null);
        CollectionSequence colSequence3 = createCollectionSequenceLocal(getUniqueString("bTestSequence"), null, null);

        // go on and add the entries to the same item, and check.
        SequenceWorkflowEntry entry = new SequenceWorkflowEntry();
        entry.order = 400;
        entry.collectionSequenceId = colSequence1.id;

        SequenceWorkflowEntry entry2 = new SequenceWorkflowEntry();
        entry2.order = 200;
        entry2.collectionSequenceId = colSequence2.id;

        SequenceWorkflowEntry entry3 = new SequenceWorkflowEntry();
        entry3.order = 200;
        entry3.collectionSequenceId = colSequence3.id;
        
        sequenceWorkflow.sequenceWorkflowEntries = Arrays.asList(entry, entry2, entry3);

        // Check that we can get this item back in a page of results!
        sequenceWorkflow = sut.create(sequenceWorkflow);

        // now try to use the name field to filter down to the correct entry!
        // now try using Filter Paging
        {
            Filter filter = Filter.create(ApiStrings.SequenceWorkflowEntry.Arguments.SEQUENCE + "." + ApiStrings.CollectionSequences.Arguments.NAME, colSequence1.name);
            PageOfResults<SequenceWorkflowEntry> results = sut.retrieveSequenceWorkflowEntriesPage(new PageRequest(1L, 6L), filter);

            assertEquals("must return our item", 1L, results.totalhits.longValue());
            checkSequenceWorkflowEntry( results.results.stream().findFirst().get(), entry);
        }
        
        // now try to use the name again but for the second entry...
        {
            Filter filter = Filter.create(ApiStrings.SequenceWorkflowEntry.Arguments.SEQUENCE + "." + ApiStrings.CollectionSequences.Arguments.NAME, colSequence2.name);
            PageOfResults<SequenceWorkflowEntry> results = sut.retrieveSequenceWorkflowEntriesPage(new PageRequest(1L, 6L), filter);

            assertEquals("must return our item", 1L, results.totalhits.longValue());
            checkSequenceWorkflowEntry(results.results.stream().findFirst().get(), entry2);
        }
        
        // now try to use the name again but for the last entry...
        {
            Filter filter = Filter.create(ApiStrings.SequenceWorkflowEntry.Arguments.SEQUENCE + "." + ApiStrings.CollectionSequences.Arguments.NAME, colSequence3.name);
            PageOfResults<SequenceWorkflowEntry> results = sut.retrieveSequenceWorkflowEntriesPage(new PageRequest(1L, 6L), filter);

            assertEquals("must return our item", 1L, results.totalhits.longValue());
            checkSequenceWorkflowEntry(results.results.stream().findFirst().get(), entry3);
        }
        
        // duff name should return nothing.
        {
            Filter filter = Filter.create(ApiStrings.SequenceWorkflowEntry.Arguments.SEQUENCE + "." + ApiStrings.CollectionSequences.Arguments.NAME, "AnyOldName");
            PageOfResults<SequenceWorkflowEntry> results = sut.retrieveSequenceWorkflowEntriesPage(new PageRequest(1L, 6L), filter);

            assertEquals("must return our item", 0L, results.totalhits.longValue());
        }
    }
    
    @Test
    public void testSequenceWorkflowEntriesPaged() {

        SequenceWorkflow sequenceWorkflow = new SequenceWorkflow();
        sequenceWorkflow.name = getUniqueString("tryme");
        sequenceWorkflow.description = "any description";

        // go on and add the entries to the same item, and check.
        SequenceWorkflowEntry entry = new SequenceWorkflowEntry();
        entry.order = 400;
        entry.collectionSequenceId = collectionSequence.id;

        SequenceWorkflowEntry entry2 = new SequenceWorkflowEntry();
        entry2.order = 200;
        entry2.collectionSequenceId = collectionSequence2.id;

        SequenceWorkflowEntry entry3 = new SequenceWorkflowEntry();
        entry3.order = 300;
        entry3.collectionSequenceId = collectionSequence3.id;

        sequenceWorkflow.sequenceWorkflowEntries = Arrays.asList(entry, entry2, entry3);

        // Check that we can get this item back in a page of results!
        sequenceWorkflow = sut.create(sequenceWorkflow);


        {
            Sort sort = Sort.create( ApiStrings.SequenceWorkflowEntry.Arguments.ORDER, true);
            Filter filter = Filter.create(ApiStrings.SequenceWorkflowEntry.Arguments.SEQUENCE_WORKFLOW_ID,sequenceWorkflow.id);
            PageOfResults<SequenceWorkflowEntry> entryResults = sut.retrieveSequenceWorkflowEntriesPage(new PageRequest(1L, 1L), filter, sort);
            assertEquals("Should only return 1 entry", 1, entryResults.results.size());
        }
        {
            Sort sort = Sort.create( ApiStrings.SequenceWorkflowEntry.Arguments.ORDER, false);
            Filter filter = Filter.create(ApiStrings.SequenceWorkflowEntry.Arguments.SEQUENCE_WORKFLOW_ID,sequenceWorkflow.id);
            PageOfResults<SequenceWorkflowEntry> entryResults = sut.retrieveSequenceWorkflowEntriesPage(new PageRequest(1L, 3L), filter, sort);
            assertEquals("Should return all 3 entry", 3, entryResults.results.size());
            checkSequenceEntryOrder(sequenceWorkflow.sequenceWorkflowEntries, entryResults, false);
        }
    }

    @Test
    public void testSequenceWorkflowEntries_MultiplePaging() {
        List<CollectionSequence> collectionSequences = new ArrayList<>();
        SequenceWorkflow sequenceWorkflow = new SequenceWorkflow();
        sequenceWorkflow.name = "tryme";
        sequenceWorkflow.description = "any description";
        sequenceWorkflow.sequenceWorkflowEntries = new ArrayList<>();
        CollectionSequence collectionSequence;
        for (int i = 0; i < 10; i++) {
            collectionSequence = createCollectionSequenceLocal((String) null, null, null);
            collectionSequences.add(collectionSequence);
            SequenceWorkflowEntry entry = new SequenceWorkflowEntry();
            entry.collectionSequenceId = collectionSequence.id;
            entry.order = (short)i;
            sequenceWorkflow.sequenceWorkflowEntries.add(entry);
        }
        sequenceWorkflow = sut.create(sequenceWorkflow);
        {
            Sort sort = Sort.create( ApiStrings.SequenceWorkflowEntry.Arguments.ORDER, false);
            Filter filter = Filter.create(ApiStrings.SequenceWorkflowEntry.Arguments.SEQUENCE_WORKFLOW_ID, sequenceWorkflow.id);
            PageOfResults<SequenceWorkflowEntry> entryResults = sut.retrieveSequenceWorkflowEntriesPage(new PageRequest(1L, 5L), filter, sort);
            assertEquals("Should return first 5 entries in descending order", 5, entryResults.results.size());
            checkSequenceEntryOrder(sequenceWorkflow.sequenceWorkflowEntries, entryResults, false);
            //Now retrieve the other half of the list
            entryResults = sut.retrieveSequenceWorkflowEntriesPage(new PageRequest(6L, 10L), filter, sort);
            assertEquals("Should return last 5 entries in descending order", 5, entryResults.results.size());
            checkSequenceEntryOrder(sequenceWorkflow.sequenceWorkflowEntries, entryResults, false, 5);
        }
    }

    @Test
    public void testSortEntriesBy_CollectionSequence_Name(){

        SequenceWorkflow sequenceWorkflow = new SequenceWorkflow();
        sequenceWorkflow.name = getUniqueString("tryme");
        sequenceWorkflow.description = "any description";

        // go on and add the entries to the same item, and check.
        SequenceWorkflowEntry entry = new SequenceWorkflowEntry();
        entry.order = 400;
        entry.collectionSequenceId = collectionSequence.id;

        SequenceWorkflowEntry entry2 = new SequenceWorkflowEntry();
        entry2.order = 200;
        entry2.collectionSequenceId = collectionSequence2.id;

        SequenceWorkflowEntry entry3 = new SequenceWorkflowEntry();
        entry3.order = 300;
        entry3.collectionSequenceId = collectionSequence3.id;

        sequenceWorkflow.sequenceWorkflowEntries = Arrays.asList(entry, entry2, entry3);

        // Check that we can get this item back in a page of results!
        sequenceWorkflow = sut.create(sequenceWorkflow);

        {
            final Boolean includeCollectionSequences = true;
            //Sort by collection sequence name
            Sort sort = Sort.create(ApiStrings.SequenceWorkflowEntry.Arguments.SEQUENCE + "." + ApiStrings.CollectionSequences.Arguments.NAME, true);
            //Filter by Workflow Id
            Filter filter = Filter.create(ApiStrings.SequenceWorkflowEntry.Arguments.SEQUENCE_WORKFLOW_ID,sequenceWorkflow.id);
            PageOfResults<SequenceWorkflowEntry> entryResults = sut.retrieveSequenceWorkflowEntriesPage(new PageRequest(1L, 6L), filter, sort, includeCollectionSequences);
            assertEquals("Should only return all 3 entries", 3, entryResults.results.size());
            Collection<CollectionSequence> collectionSequences = Arrays.asList(collectionSequence, collectionSequence2, collectionSequence3);
            //Check the attached collection sequences are the ones we're expecting.
            for (SequenceWorkflowEntry swe:entryResults.results){
                assertEquals("Attached collection sequence's id should match the entries expected",swe.collectionSequenceId,swe.collectionSequence.id);
                assertEquals("One of the attached collection sequence's id should match the created sequences",1,collectionSequences.stream().filter(e -> e.id.equals(swe.collectionSequence.id)).count());
            }
            //Sort the collection sequences by name
            List<CollectionSequence> sortedOrder = sortCollectionSequences(collectionSequences, true);
            List<SequenceWorkflowEntry> returnedOrder = entryResults.results.stream().collect(Collectors.toList());
            //Check the returned workflow entries are in the same order as the collection sequences
            for(int i=0; i<returnedOrder.size(); i++){
                assertEquals("Returned entry "+i+" has is ordered incorrectly",sortedOrder.get(i).id,returnedOrder.get(i).collectionSequenceId);
            }

        }

        // do the same but in desc order.
        {
            final Boolean includeCollectionSequences = true;
            //Sort by collection sequence name
            Sort sort = Sort.create(ApiStrings.SequenceWorkflowEntry.Arguments.SEQUENCE + "." + ApiStrings.CollectionSequences.Arguments.NAME, false);
            //Filter by Workflow Id
            Filter filter = Filter.create(ApiStrings.SequenceWorkflowEntry.Arguments.SEQUENCE_WORKFLOW_ID,sequenceWorkflow.id);
            PageOfResults<SequenceWorkflowEntry> entryResults = sut.retrieveSequenceWorkflowEntriesPage(new PageRequest(1L, 6L), filter, sort, includeCollectionSequences);
            assertEquals("Should only return all 3 entries", 3, entryResults.results.size());
            Collection<CollectionSequence> collectionSequences = Arrays.asList(collectionSequence, collectionSequence2, collectionSequence3);
            //Check the attached collection sequences are the ones we're expecting.
            for (SequenceWorkflowEntry swe:entryResults.results){
                assertEquals("Attached collection sequence's id should match the entries expected",swe.collectionSequenceId,swe.collectionSequence.id);
                assertEquals("One of the attached collection sequence's id should match the created sequences",1,collectionSequences.stream().filter(e -> e.id.equals(swe.collectionSequence.id)).count());
            }
            //Sort the collection sequences by name
            List<CollectionSequence> sortedOrder = sortCollectionSequences(collectionSequences, false);
            List<SequenceWorkflowEntry> returnedOrder = entryResults.results.stream().collect(Collectors.toList());
            //Check the returned workflow entries are in the same order as the collection sequences
            for(int i=0; i<returnedOrder.size(); i++){
                assertEquals("Returned entry "+i+" has is ordered incorrectly",sortedOrder.get(i).id,returnedOrder.get(i).collectionSequenceId);
            }

        }
    }


    @Test
    public void testFilterEntriesBy_CollectionSequence_EnabledStatus(){

        CollectionSequence collectionSequenceLocal1 = createCollectionSequenceLocal(null, null, getUniqueString("aTest_WorkflowByCollectionSequenceEnabled"), true, true);
        CollectionSequence collectionSequenceLocal2 = createCollectionSequenceLocal(null, null, getUniqueString("bTest_WorkflowByCollectionSequenceEnabled"), true, false);
        CollectionSequence collectionSequenceLocal3 = createCollectionSequenceLocal(null, null, getUniqueString("cTest_WorkflowByCollectionSequenceEnabled"), true, false);


        SequenceWorkflow sequenceWorkflow1 = createSequenceWorkflow(collectionSequenceLocal1, getUniqueString("aName:sequenceByEnabledStatus_"), "whatever!");
        SequenceWorkflow sequenceWorkflow2 = createSequenceWorkflow(collectionSequenceLocal2, getUniqueString("bName:sequenceByEnabledStatus_"), "whatever!");
        SequenceWorkflow sequenceWorkflow3 = createSequenceWorkflow(collectionSequenceLocal3, getUniqueString("cName:sequenceByEnabledStatus_"), "whatever!");


        {
            //Filter by Workflow enabled....
            Filter filter = Filter.create(ApiStrings.SequenceWorkflow.Arguments.SEQUENCE_ENTRIES + "." + ApiStrings.SequenceWorkflowEntry.Arguments.SEQUENCE + "." + ApiStrings.CollectionSequences.Arguments.EVALUATION_ENABLED, true);
            PageOfResults<SequenceWorkflow> entryResults = sut.retrieveSequenceWorkflowsPage(new PageRequest(1L, 6L), filter);
            assertEquals("Should only return all sequence workflows", 1, entryResults.results.size());

            SequenceWorkflow resultItem = entryResults.results.stream().findFirst().get();
            compareSequenceWorkflowItem(sequenceWorkflow1, resultItem);
        }

        {
            //Filter by Workflow disabled....
            Filter filter = Filter.create(ApiStrings.SequenceWorkflow.Arguments.SEQUENCE_ENTRIES + "." + ApiStrings.SequenceWorkflowEntry.Arguments.SEQUENCE + "." + ApiStrings.CollectionSequences.Arguments.EVALUATION_ENABLED, false);
            PageOfResults<SequenceWorkflow> entryResults = sut.retrieveSequenceWorkflowsPage(new PageRequest(1L, 6L), filter);

            assertEquals("Should only return all sequence workflows", 2, entryResults.results.size());
            compareSequenceWorkflowItem(sequenceWorkflow2, (SequenceWorkflow) entryResults.results.toArray()[0]);
            compareSequenceWorkflowItem(sequenceWorkflow3, (SequenceWorkflow) entryResults.results.toArray()[1]);
        }
    }


    @Test
    public void testFilterEntriesBy_CollectionSequence_Name(){

        CollectionSequence collectionSequenceLocal1 = createCollectionSequence(null, getUniqueString("aTest_WorkflowByCollectionSequenceName") );
        CollectionSequence collectionSequenceLocal2 = createCollectionSequence(null, getUniqueString("bTest_WorkflowByCollectionSequenceName") );
        CollectionSequence collectionSequenceLocal3 = createCollectionSequence(null, getUniqueString("cTest_WorkflowByCollectionSequenceName") );

        SequenceWorkflow sequenceWorkflow1 = createSequenceWorkflow(collectionSequenceLocal1, getUniqueString("aName:sequenceByName_"), "doesn't matter");
        SequenceWorkflow sequenceWorkflow2 = createSequenceWorkflow(collectionSequenceLocal2, getUniqueString("bName:sequenceByName_"), "doesn't matter");
        SequenceWorkflow sequenceWorkflow3 = createSequenceWorkflow(collectionSequenceLocal3, getUniqueString("cName:sequenceByName_"), "doesn't matter");


        {
            //Filter by Workflow name....
            Filter filter = Filter.create(ApiStrings.SequenceWorkflow.Arguments.SEQUENCE_ENTRIES + "." + ApiStrings.SequenceWorkflowEntry.Arguments.SEQUENCE + "." + ApiStrings.CollectionSequences.Arguments.NAME, collectionSequenceLocal1.name);
            PageOfResults<SequenceWorkflow> entryResults = sut.retrieveSequenceWorkflowsPage(new PageRequest(1L, 6L), filter);
            assertEquals("Should only return all sequence workflows", 1, entryResults.results.size());

            SequenceWorkflow resultItem = entryResults.results.stream().findFirst().get();
            compareSequenceWorkflowItem(sequenceWorkflow1, resultItem);
        }

        {
            //Filter by Workflow name2....
            Filter filter = Filter.create(ApiStrings.SequenceWorkflow.Arguments.SEQUENCE_ENTRIES + "." + ApiStrings.SequenceWorkflowEntry.Arguments.SEQUENCE + "." + ApiStrings.CollectionSequences.Arguments.NAME, collectionSequenceLocal2.name);
            PageOfResults<SequenceWorkflow> entryResults = sut.retrieveSequenceWorkflowsPage(new PageRequest(1L, 6L), filter);

            assertEquals("Should only return all sequence workflows", 1, entryResults.results.size());
            compareSequenceWorkflowItem(sequenceWorkflow2, (SequenceWorkflow) entryResults.results.toArray()[0]);
        }
    }

    @Test
    public void testFilterEntriesBy_CollectionSequence_Description(){

        String uniqueDescToMatch = getUniqueString("aTest_WorkflowByCollectionSequenceDescMatch_");
        CollectionSequence collectionSequenceLocal1 = createCollectionSequence((DocumentCollection)null, getUniqueString("aTest"), uniqueDescToMatch, true );
        CollectionSequence collectionSequenceLocal2 = createCollectionSequence((DocumentCollection)null, getUniqueString("bTest_"), uniqueDescToMatch, true );
        CollectionSequence collectionSequenceLocal3 = createCollectionSequence((DocumentCollection) null, getUniqueString("cTest"), getUniqueString("AnyOldValue_"), true);

        SequenceWorkflow sequenceWorkflow1 = createSequenceWorkflow(collectionSequenceLocal1, getUniqueString("aName:sequenceByName_"), "anything");
        SequenceWorkflow sequenceWorkflow2 = createSequenceWorkflow(collectionSequenceLocal2, getUniqueString("bName:sequenceByName_"), "anything");
        SequenceWorkflow sequenceWorkflow3 = createSequenceWorkflow(collectionSequenceLocal3, getUniqueString("cName:sequenceByName_"), "anything");


        {
            //Filter by Workflow desc....
            Filter filter = Filter.create( ApiStrings.SequenceWorkflowEntry.Arguments.SEQUENCE + "." + ApiStrings.CollectionSequences.Arguments.DESCRIPTION, uniqueDescToMatch);

            PageOfResults<SequenceWorkflowEntry> entryResults = sut.retrieveSequenceWorkflowEntriesPage(new PageRequest(1L, 6L), filter, null, true);
            assertEquals("Should only return all sequence workflows", 2, entryResults.results.size());

            SequenceWorkflowEntry resultItem = (SequenceWorkflowEntry)entryResults.results.toArray()[0];
            compareSequenceWorkflowEntryItem((SequenceWorkflowEntry)sequenceWorkflow1.sequenceWorkflowEntries.toArray()[0], resultItem);
            resultItem = (SequenceWorkflowEntry)entryResults.results.toArray()[1];
            compareSequenceWorkflowEntryItem((SequenceWorkflowEntry) sequenceWorkflow2.sequenceWorkflowEntries.toArray()[0], resultItem);
        }

        {
            //Filter by Workflow desc and parent sequenceWorkflowId, should restrict the 2 desc matches, to only 1.
            Filter filter = Filter.create(ApiStrings.SequenceWorkflowEntry.Arguments.SEQUENCE + "." + ApiStrings.CollectionSequences.Arguments.DESCRIPTION, uniqueDescToMatch);
            filter.put(ApiStrings.SequenceWorkflowEntry.Arguments.SEQUENCE_WORKFLOW_ID, sequenceWorkflow1.id);

            PageOfResults<SequenceWorkflowEntry> entryResults = sut.retrieveSequenceWorkflowEntriesPage(new PageRequest(1L, 6L), filter, null, true);
            assertEquals("Should only return all sequence workflows", 1, entryResults.results.size());

            SequenceWorkflowEntry resultItem = (SequenceWorkflowEntry)entryResults.results.toArray()[0];
            compareSequenceWorkflowEntryItem((SequenceWorkflowEntry) sequenceWorkflow1.sequenceWorkflowEntries.toArray()[0], resultItem);
        }

        {
            //Filter by Workflow desc and parent sequenceWorkflowId, should restrict the 2 desc matches, to only 1.
            Filter filter = Filter.create(ApiStrings.SequenceWorkflowEntry.Arguments.SEQUENCE + "." + ApiStrings.CollectionSequences.Arguments.DESCRIPTION, uniqueDescToMatch);
            filter.put(ApiStrings.SequenceWorkflowEntry.Arguments.SEQUENCE_WORKFLOW_ID, sequenceWorkflow1.id);

            PageOfResults<SequenceWorkflowEntry> entryResults = sut.retrieveSequenceWorkflowEntriesPage(new PageRequest(1L, 6L), filter, null, false);
            assertEquals("Should only return all sequence workflows", 1, entryResults.results.size());

            SequenceWorkflowEntry resultItem = (SequenceWorkflowEntry)entryResults.results.toArray()[0];
            assertEquals("Collection sequenceId on the entry must match", resultItem.collectionSequenceId, collectionSequenceLocal1.id );
        }
    }

    private void compareSequenceWorkflowItem(SequenceWorkflow sequenceWorkflow1, SequenceWorkflow resultItem) {
        assertNotNull(resultItem);
        assertEquals("Sequence workflow should match - id: ", sequenceWorkflow1.id, resultItem.id);
        assertEquals("Sequence workflow should match - name: ", sequenceWorkflow1.name, resultItem.name);
    }

    private void compareSequenceWorkflowEntryItem(SequenceWorkflowEntry sequenceWorkflowEntry, SequenceWorkflowEntry resultItem) {
        assertNotNull(resultItem);
        assertNotNull(sequenceWorkflowEntry);
        assertEquals("Sequence workflow should match - collectionSequenceId: ", sequenceWorkflowEntry.collectionSequenceId, resultItem.collectionSequenceId);
        assertEquals("Sequence workflow should match - sequenceWorkflowId: ", sequenceWorkflowEntry.sequenceWorkflowId, resultItem.sequenceWorkflowId);
        assertEquals("Sequence workflow should match - order: ", sequenceWorkflowEntry.order, resultItem.order);
    }
    private SequenceWorkflow createSequenceWorkflow(CollectionSequence collectionSequenceLocal, String seqName, String seqDesc ) {
        SequenceWorkflow sequenceWorkflow = new SequenceWorkflow();
        sequenceWorkflow.name = seqName;
        sequenceWorkflow.description = seqDesc;

        // go on and add the entries to the same item, and check.
        SequenceWorkflowEntry entry = new SequenceWorkflowEntry();
        entry.order = 400;
        entry.collectionSequenceId = collectionSequenceLocal.id;
        sequenceWorkflow.sequenceWorkflowEntries = Arrays.asList(entry);

        // Check that we can get this item back in a page of results!
        sequenceWorkflow = sut.create(sequenceWorkflow);
        Assert.assertNotNull(sequenceWorkflow);
        Assert.assertNotNull(sequenceWorkflow.id);
        Assert.assertTrue(sequenceWorkflow.id != 0);
        return sequenceWorkflow;
    }

    @Test
    public void testSortEntriesBy_CollectionSequence_Desc(){

        SequenceWorkflow sequenceWorkflow = new SequenceWorkflow();
        sequenceWorkflow.name = getUniqueString("testSortEntriesBy_CollectionSequence_Desc");
        sequenceWorkflow.description = "any description";

        // go on and add the entries to the same item, and check.
        SequenceWorkflowEntry entry = new SequenceWorkflowEntry();
        entry.order = 400;
        entry.collectionSequenceId = collectionSequence.id;

        SequenceWorkflowEntry entry2 = new SequenceWorkflowEntry();
        entry2.order = 200;
        entry2.collectionSequenceId = collectionSequence2.id;

        SequenceWorkflowEntry entry3 = new SequenceWorkflowEntry();
        entry3.order = 300;
        entry3.collectionSequenceId = collectionSequence3.id;

        sequenceWorkflow.sequenceWorkflowEntries = Arrays.asList(entry, entry2, entry3);

        // Check that we can get this item back in a page of results!
        sequenceWorkflow = sut.create(sequenceWorkflow);

        {
            final Boolean includeCollectionSequences = true;
            //Sort by collection sequence name
            Sort sort = Sort.create(ApiStrings.SequenceWorkflowEntry.Arguments.SEQUENCE + "." + ApiStrings.CollectionSequences.Arguments.DESCRIPTION, true);
            //Filter by Workflow Id
            Filter filter = Filter.create(ApiStrings.SequenceWorkflowEntry.Arguments.SEQUENCE_WORKFLOW_ID,sequenceWorkflow.id);
            PageOfResults<SequenceWorkflowEntry> entryResults = sut.retrieveSequenceWorkflowEntriesPage(new PageRequest(1L, 6L), filter, sort, includeCollectionSequences);
            assertEquals("Should only return all 3 entries", 3, entryResults.results.size());
            Collection<CollectionSequence> collectionSequences = Arrays.asList(collectionSequence, collectionSequence2, collectionSequence3);
            //Check the attached collection sequences are the ones we're expecting.
            for (SequenceWorkflowEntry swe:entryResults.results){
                assertEquals("Attached collection sequence's id should match the entries expected",swe.collectionSequenceId,swe.collectionSequence.id);
                assertEquals("One of the attached collection sequence's id should match the created sequences", 1, collectionSequences.stream().filter(e -> e.id.equals(swe.collectionSequence.id)).count());
            }
            //Sort the collection sequences by desc
            List<CollectionSequence> sortedOrder = sortCollectionSequences(collectionSequences, true);
            List<SequenceWorkflowEntry> returnedOrder = entryResults.results.stream().collect(Collectors.toList());
            //Check the returned workflow entries are in the same order as the collection sequences
            for(int i=0; i<returnedOrder.size(); i++){
                assertEquals("Returned entry "+i+" has is ordered incorrectly",sortedOrder.get(i).id,returnedOrder.get(i).collectionSequenceId);
            }

        }

        // do the same but in desc order.
        {
            final Boolean includeCollectionSequences = true;
            //Sort by collection sequence name
            Sort sort = Sort.create(ApiStrings.SequenceWorkflowEntry.Arguments.SEQUENCE + "." + ApiStrings.CollectionSequences.Arguments.DESCRIPTION, false);
            //Filter by Workflow Id
            Filter filter = Filter.create(ApiStrings.SequenceWorkflowEntry.Arguments.SEQUENCE_WORKFLOW_ID,sequenceWorkflow.id);
            PageOfResults<SequenceWorkflowEntry> entryResults = sut.retrieveSequenceWorkflowEntriesPage(new PageRequest(1L, 6L), filter, sort, includeCollectionSequences);
            assertEquals("Should only return all 3 entries", 3, entryResults.results.size());
            Collection<CollectionSequence> collectionSequences = Arrays.asList(collectionSequence, collectionSequence2, collectionSequence3);
            //Check the attached collection sequences are the ones we're expecting.
            for (SequenceWorkflowEntry swe:entryResults.results){
                assertEquals("Attached collection sequence's id should match the entries expected",swe.collectionSequenceId,swe.collectionSequence.id);
                assertEquals("One of the attached collection sequence's id should match the created sequences", 1, collectionSequences.stream().filter(e -> e.id.equals(swe.collectionSequence.id)).count());
            }
            //Sort the collection sequences by desc
            List<CollectionSequence> sortedOrder = sortCollectionSequences(collectionSequences, false);
            List<SequenceWorkflowEntry> returnedOrder = entryResults.results.stream().collect(Collectors.toList());
            //Check the returned workflow entries are in the same order as the collection sequences
            for(int i=0; i<returnedOrder.size(); i++){
                assertEquals("Returned entry "+i+" has is ordered incorrectly",sortedOrder.get(i).id,returnedOrder.get(i).collectionSequenceId);
            }
        }
    }

    @Test
    public void testSortEntriesByCollectionSequenceName_noAttachedColSeq(){
        Long startCount = 0L;
        {
            PageOfResults<SequenceWorkflow> results = sut.retrieveSequenceWorkflowsPage(new PageRequest(1L, 1L));
            // store off our starting indexes - useful for non-direct modes where db isn't empty for this tenant.
            startCount = results.totalhits;
        }
        // try with only name - should work fine!, build working test first,
        // as failing tests, may just fail because of something else :)
        SequenceWorkflow sequenceWorkflow = new SequenceWorkflow();
        sequenceWorkflow.name = "tryme";
        sequenceWorkflow.description = "any description";

        // go on and add the entries to the same item, and check.
        SequenceWorkflowEntry entry = new SequenceWorkflowEntry();
        entry.order = 400;
        entry.collectionSequenceId = collectionSequence.id;

        SequenceWorkflowEntry entry2 = new SequenceWorkflowEntry();
        entry2.order = 200;
        entry2.collectionSequenceId = collectionSequence2.id;

        SequenceWorkflowEntry entry3 = new SequenceWorkflowEntry();
        entry3.order = 300;
        entry3.collectionSequenceId = collectionSequence3.id;

        sequenceWorkflow.sequenceWorkflowEntries = Arrays.asList(entry, entry2, entry3);

        // Check that we can get this item back in a page of results!
        sequenceWorkflow = sut.create(sequenceWorkflow);

        // Confirm that our paging goes up by one.
        {
            PageOfResults<SequenceWorkflow> results = sut.retrieveSequenceWorkflowsPage(new PageRequest(1L, 1L));

            // we should be up by one
            assertEquals("Fragment must be the same as before", startCount + 1L, results.totalhits.longValue());
        }

        {
            final Boolean includeCollectionSequences = false;
            //Sort by collection sequence name
            Sort sort = Sort.create(ApiStrings.SequenceWorkflowEntry.Arguments.SEQUENCE + "." + ApiStrings.CollectionSequences.Arguments.NAME, false);
            //Filter by Workflow Id
            Filter filter = Filter.create(ApiStrings.SequenceWorkflowEntry.Arguments.SEQUENCE_WORKFLOW_ID, sequenceWorkflow.id);
            PageOfResults<SequenceWorkflowEntry> entryResults = sut.retrieveSequenceWorkflowEntriesPage(new PageRequest(1L, 6L), filter, sort, includeCollectionSequences);
            assertEquals("Should only return all 3 entries", 3, entryResults.results.size());
            //Check there are no attached collection sequences
            for (SequenceWorkflowEntry swe:entryResults.results){
                assertNull("Should be no attached collection sequences", swe.collectionSequence);
            }
            //Sort the collection sequences by name
            Collection<CollectionSequence> collectionSequences = Arrays.asList(collectionSequence,collectionSequence2,collectionSequence3);
            List<CollectionSequence> sortedOrder = sortCollectionSequences(collectionSequences, false);
            List<SequenceWorkflowEntry> returnedOrder = entryResults.results.stream().collect(Collectors.toList());
            //Check the returned workflow entries are in the same order as the collection sequences
            for(int i=0; i<returnedOrder.size(); i++){
                assertEquals("Returned entry "+i+" has is ordered incorrectly",sortedOrder.get(i).id,returnedOrder.get(i).collectionSequenceId);
            }
        }
        {
            //Now check that collection sequences are not returned by default.
            Sort sort = Sort.create(ApiStrings.SequenceWorkflowEntry.Arguments.SEQUENCE + "." + ApiStrings.CollectionSequences.Arguments.NAME, false);
            //Filter by Workflow Id
            Filter filter = Filter.create(ApiStrings.SequenceWorkflowEntry.Arguments.SEQUENCE_WORKFLOW_ID, sequenceWorkflow.id);
            //Do not specify whether to include collection sequences
            PageOfResults<SequenceWorkflowEntry> entryResults = sut.retrieveSequenceWorkflowEntriesPage(new PageRequest(1L, 6L), filter, sort);
            assertEquals("Should only return all 3 entries", 3, entryResults.results.size());
            for (SequenceWorkflowEntry swe:entryResults.results){
                assertNull("Should be no attached collection sequences", swe.collectionSequence);
            }
            Collection<CollectionSequence> collectionSequences = Arrays.asList(collectionSequence,collectionSequence2,collectionSequence3);
            List<CollectionSequence> sortedOrder = sortCollectionSequences(collectionSequences, false);
            List<SequenceWorkflowEntry> returnedOrder = entryResults.results.stream().collect(Collectors.toList());
            //Check the returned workflow entries are in the same order as the collection sequences
            for(int i=0; i<returnedOrder.size(); i++){
                assertEquals("Returned entry "+i+" has is ordered incorrectly",sortedOrder.get(i).id,returnedOrder.get(i).collectionSequenceId);
            }
        }
    }

    @Test
    public void testSequenceWorkflowEntriesSortByOrder(){
        Long startCount = 0L;
        {
            PageOfResults<SequenceWorkflow> results = sut.retrieveSequenceWorkflowsPage(new PageRequest(1L, 1L));
            // store off our starting indexes - useful for non-direct modes where db isn't empty for this tenant.
            startCount = results.totalhits;
        }
        // try with only name - should work fine!, build working test first,
        // as failing tests, may just fail because of something else :)
        SequenceWorkflow sequenceWorkflow = new SequenceWorkflow();
        sequenceWorkflow.name = "tryme";
        sequenceWorkflow.description = "any description";

        // go on and add the entries to the same item, and check.
        SequenceWorkflowEntry entry = new SequenceWorkflowEntry();
        entry.order = 400;
        entry.collectionSequenceId = collectionSequence.id;

        SequenceWorkflowEntry entry2 = new SequenceWorkflowEntry();
        entry2.order = 200;
        entry2.collectionSequenceId = collectionSequence2.id;

        SequenceWorkflowEntry entry3 = new SequenceWorkflowEntry();
        entry3.order = 300;
        entry3.collectionSequenceId = collectionSequence3.id;

        sequenceWorkflow.sequenceWorkflowEntries = Arrays.asList(entry, entry2, entry3);

        // Check that we can get this item back in a page of results!
        sequenceWorkflow = sut.create(sequenceWorkflow);

        // Confirm that our paging goes up by one.
        {
            PageOfResults<SequenceWorkflow> results = sut.retrieveSequenceWorkflowsPage(new PageRequest(1L, 1L));

            // we should be up by one
            assertEquals("Fragment must be the same as before", startCount + 1L, results.totalhits.longValue());
        }

        // Test that when specying the suborder field on workflowentry.order that it works.
        {
            Boolean includeCollectionSequences = true;
            //Sort by collection sequence name
            Sort sort = Sort.create( ApiStrings.SequenceWorkflowEntry.Arguments.ORDER, false);
            //Filter by Workflow Id
            Filter filter = Filter.create(ApiStrings.SequenceWorkflowEntry.Arguments.SEQUENCE_WORKFLOW_ID, sequenceWorkflow.id);
            PageOfResults<SequenceWorkflowEntry> entryResults = sut.retrieveSequenceWorkflowEntriesPage(new PageRequest(1L, 6L), filter, sort, includeCollectionSequences);
            assertEquals("Should only return all 3 entries", 3, entryResults.results.size());
            List<CollectionSequence> collectionSequences = Arrays.asList(collectionSequence, collectionSequence3, collectionSequence2);

            //Check the attached collection sequences are the ones we're expecting.
            for (SequenceWorkflowEntry swe:entryResults.results){
                assertEquals("Attached collection sequence's id should match the entries expected",swe.collectionSequenceId,swe.collectionSequence.id);
                assertEquals("One of the attached collection sequence's id should match the created sequences", 1, collectionSequences.stream().filter(e -> e.id.equals(swe.collectionSequenceId)).count());
            }
            checkSequenceEntryOrder(sequenceWorkflow.sequenceWorkflowEntries, entryResults, false);
            includeCollectionSequences = false;
            entryResults = sut.retrieveSequenceWorkflowEntriesPage(new PageRequest(1L, 6L), filter, sort, includeCollectionSequences);
            assertEquals("Should only return all 3 entries", 3, entryResults.results.size());
            for (SequenceWorkflowEntry swe:entryResults.results){
                assertNull("Should be no attached collection sequences", swe.collectionSequence);
            }
        }
    }

    // CAF-1309 / CAF-1365 
    @Test
    public void testSequenceWorkflowEntriesSortByOrder_SequenceWorkflowByID(){


        Long startCount = 0L;
        {
            PageOfResults<SequenceWorkflow> results = sut.retrieveSequenceWorkflowsPage(new PageRequest(1L, 1L));
            // store off our starting indexes - useful for non-direct modes where db isn't empty for this tenant.
            startCount = results.totalhits;
        }
        // try with only name - should work fine!, build working test first,
        // as failing tests, may just fail because of something else :)
        SequenceWorkflow sequenceWorkflow = new SequenceWorkflow();
        sequenceWorkflow.name = "testSequenceWorkflowEntriesSortByOrder_SequenceWorkflowByID";
        sequenceWorkflow.description = "any description";

        // go on and add the entries to the same item, and check.
        SequenceWorkflowEntry entry = new SequenceWorkflowEntry();
        entry.order = 400;
        entry.collectionSequenceId = collectionSequence.id;

        SequenceWorkflowEntry entry2 = new SequenceWorkflowEntry();
        entry2.order = 200;
        entry2.collectionSequenceId = collectionSequence2.id;

        SequenceWorkflowEntry entry3 = new SequenceWorkflowEntry();
        entry3.order = 300;
        entry3.collectionSequenceId = collectionSequence3.id;

        sequenceWorkflow.sequenceWorkflowEntries = Arrays.asList(entry, entry2, entry3);

        // Check that we can get this item back in a page of results!
        sequenceWorkflow = sut.create(sequenceWorkflow);

        // test if default ordering of entries is correct
        {
            //Filter by Workflow Id
            SequenceWorkflow workflowResp = sut.retrieveSequenceWorkflow(sequenceWorkflow.id);

            assertNotNull("Workflow response must not be null", workflowResp);
            assertEquals("Should only return all 3 entries", 3, workflowResp.sequenceWorkflowEntries.size());

            List<SequenceWorkflowEntry> sortedItems = sortEntries(sequenceWorkflow.sequenceWorkflowEntries, true);
            List<SequenceWorkflowEntry> returnedOrder = workflowResp.sequenceWorkflowEntries.stream().collect(Collectors.toList());
            for (int i = 0; i < returnedOrder.size(); i++) {
                assertEquals("Order of id not correct for index: " + String.valueOf(i), sortedItems.get(i).collectionSequenceId, returnedOrder.get(i).collectionSequenceId);
            }
        }
    }

    @Test
    public void testSequenceWorkflowEntriesNoSort(){

        // try with only name - should work fine!, build working test first,
        // as failing tests, may just fail because of something else :)
        SequenceWorkflow sequenceWorkflow = new SequenceWorkflow();
        sequenceWorkflow.name = "tryme";
        sequenceWorkflow.description = "any description";

        // go on and add the entries to the same item, and check.
        SequenceWorkflowEntry entry = new SequenceWorkflowEntry();
        entry.order = 400;
        entry.collectionSequenceId = collectionSequence.id;

        SequenceWorkflowEntry entry2 = new SequenceWorkflowEntry();
        entry2.order = 200;
        entry2.collectionSequenceId = collectionSequence2.id;

        SequenceWorkflowEntry entry3 = new SequenceWorkflowEntry();
        entry3.order = 300;
        entry3.collectionSequenceId = collectionSequence3.id;

        sequenceWorkflow.sequenceWorkflowEntries = Arrays.asList(entry, entry2, entry3);

        // Check that we can get this item back in a page of results!
        sequenceWorkflow = sut.create(sequenceWorkflow);

        {
            Boolean includeCollectionSequences = true;
            //Filter by Workflow Id
            Filter filter = Filter.create(ApiStrings.SequenceWorkflowEntry.Arguments.SEQUENCE_WORKFLOW_ID, sequenceWorkflow.id);
            PageOfResults<SequenceWorkflowEntry> entryResults = sut.retrieveSequenceWorkflowEntriesPage(new PageRequest(1L, 6L), filter, includeCollectionSequences);
            assertEquals("Should only return all 3 entries", 3, entryResults.results.size());
            List<CollectionSequence> collectionSequences = Arrays.asList(collectionSequence, collectionSequence2, collectionSequence3);
            //Check the attached collection sequences are the ones we're expecting.
            for (SequenceWorkflowEntry swe:entryResults.results){
                assertEquals("Attached collection sequence's id should match the entries expected",swe.collectionSequenceId,swe.collectionSequence.id);
                assertEquals("One of the attached collection sequence's id should match the created sequences", 1, collectionSequences.stream().filter(e -> e.id.equals(swe.collectionSequenceId)).count());
            }
            checkSequenceEntryOrder(sequenceWorkflow.sequenceWorkflowEntries, entryResults, true);

            //Repeat test specifying to not include collection sequences
            includeCollectionSequences = false;
            entryResults = sut.retrieveSequenceWorkflowEntriesPage(new PageRequest(1L, 6L), filter, includeCollectionSequences);
            assertEquals("Should only return all 3 entries", 3, entryResults.results.size());
            for (SequenceWorkflowEntry swe:entryResults.results){
                assertNull("Should be no attached collection sequences", swe.collectionSequence);
            }
        }
    }

    @Test
    public void testSequenceWorkflowEntriesNoFilterOrSort(){
        Long startCount = 0L;
        {
            PageOfResults<SequenceWorkflow> results = sut.retrieveSequenceWorkflowsPage(new PageRequest(1L, 1L));
            // store off our starting indexes - useful for non-direct modes where db isn't empty for this tenant.
            startCount = results.totalhits;
        }
        // try with only name - should work fine!, build working test first,
        // as failing tests, may just fail because of something else :)
        SequenceWorkflow sequenceWorkflow = new SequenceWorkflow();
        sequenceWorkflow.name = getUniqueString("tryme");
        sequenceWorkflow.description = "any description";

        // go on and add the entries to the same item, and check.
        SequenceWorkflowEntry entry = new SequenceWorkflowEntry();
        entry.order = 400;
        entry.collectionSequenceId = collectionSequence.id;

        SequenceWorkflowEntry entry2 = new SequenceWorkflowEntry();
        entry2.order = 200;
        entry2.collectionSequenceId = collectionSequence2.id;

        SequenceWorkflowEntry entry3 = new SequenceWorkflowEntry();
        entry3.order = 300;
        entry3.collectionSequenceId = collectionSequence3.id;

        sequenceWorkflow.sequenceWorkflowEntries = Arrays.asList(entry, entry2, entry3);

        // Check that we can get this item back in a page of results!
        sequenceWorkflow = sut.create(sequenceWorkflow);

        // Confirm that our paging goes up by one.
        {
            PageOfResults<SequenceWorkflow> results = sut.retrieveSequenceWorkflowsPage(new PageRequest(1L, 1L));

            // we should be up by one
            assertEquals("Should have 1 more sequence workflow", startCount + 1L, results.totalhits.longValue());
        }

        PageOfResults<SequenceWorkflowEntry> entryResults = sut.retrieveSequenceWorkflowEntriesPage(new PageRequest(startCount + 1L, 6L));
        assertEquals("Should only return 3 entries", 3, entryResults.results.size());
        for(SequenceWorkflowEntry returnedEntry:entryResults.results){
            assertEquals("Returned entry should be on created workflow", 1, sequenceWorkflow.sequenceWorkflowEntries.stream().filter(e -> e.collectionSequenceId.equals(returnedEntry.collectionSequenceId)).count());
        }

    }

    @Test
    public void testSequenceWorkflowEntries_InvalidSort(){
        // try with only name - should work fine!, build working test first,
        // as failing tests, may just fail because of something else :)
        SequenceWorkflow sequenceWorkflow = new SequenceWorkflow();
        sequenceWorkflow.name = getUniqueString("tryme");
        sequenceWorkflow.description = "any description";

        // go on and add the entries to the same item, and check.
        SequenceWorkflowEntry entry = new SequenceWorkflowEntry();
        entry.order = 400;
        entry.collectionSequenceId = collectionSequence.id;

        SequenceWorkflowEntry entry2 = new SequenceWorkflowEntry();
        entry2.order = 200;
        entry2.collectionSequenceId = collectionSequence2.id;

        SequenceWorkflowEntry entry3 = new SequenceWorkflowEntry();
        entry3.order = 300;
        entry3.collectionSequenceId = collectionSequence3.id;

        sequenceWorkflow.sequenceWorkflowEntries = Arrays.asList(entry, entry2, entry3);

        // Check that we can get this item back in a page of results!
        sequenceWorkflow = sut.create(sequenceWorkflow);

        {
            //Create sort using property without sort annotation
            Sort sort = Sort.create(ApiStrings.SequenceWorkflowEntry.Arguments.SEQUENCE+"."+ApiStrings.CollectionSequences.Arguments.LAST_MODIFIED, true);
            Filter filter = Filter.create(ApiStrings.SequenceWorkflowEntry.Arguments.SEQUENCE_WORKFLOW_ID,sequenceWorkflow.id);
            shouldThrow(o -> {
                sut.retrieveSequenceWorkflowEntriesPage(new PageRequest(1L, 1L), filter, sort);
            });

        }
    }


    private void checkSequenceEntryOrder(List<SequenceWorkflowEntry> newItems, PageOfResults<SequenceWorkflowEntry> pageOfResults, boolean ascending) {
        List<SequenceWorkflowEntry> sortedItems = sortEntries(newItems, ascending);
        List<SequenceWorkflowEntry> returnedOrder = pageOfResults.results.stream().collect(Collectors.toList());
        for (int i = 0; i < returnedOrder.size(); i++) {
            assertEquals("Order of id not correct for index: " + String.valueOf(i), sortedItems.get(i).collectionSequenceId, returnedOrder.get(i).collectionSequenceId);
        }
    }

    private void checkSequenceEntryOrder(List<SequenceWorkflowEntry> newItems, PageOfResults<SequenceWorkflowEntry> pageOfResults, boolean ascending, int startingIndex) {
        List<SequenceWorkflowEntry> sortedItems = sortEntries(newItems, ascending);
        List<SequenceWorkflowEntry> returnedOrder = pageOfResults.results.stream().collect(Collectors.toList());
        for (int i = 0; i < returnedOrder.size(); i++) {
            assertEquals("Order of id not correct for index: " + String.valueOf(i), sortedItems.get(i+startingIndex).collectionSequenceId, returnedOrder.get(i).collectionSequenceId);
        }
    }

    private List<SequenceWorkflowEntry> sortEntries(Collection<SequenceWorkflowEntry> entries, boolean ascending) {
        List<SequenceWorkflowEntry> sortedList = entries.stream().sorted((c1, c2) -> {
            if (c1.order == null) {
                if (c2.order == null) {
                    return c1.id.compareTo(c2.id);
                }
                return -1; // null comes before any text
            } else if (c2.order == null) {
                return 1; // source has text, dest null, so after
            } else {
                return c1.order.compareTo(c2.order);
            }
        }).collect(Collectors.toList());
        if (!ascending) {
            Collections.reverse(sortedList);
        }
        return sortedList;
    }

    private List<CollectionSequence> sortCollectionSequences(Collection<CollectionSequence> sequences, boolean ascending){
        List<CollectionSequence> sortedList = sequences.stream().sorted((c1, c2) -> {
            if (c1.name == null) {
                if (c2.name == null) {
                    return c1.id.compareTo(c2.id);
                }
                return -1; // null comes before any text
            } else if (c2.name == null) {
                return 1; // source has text, dest null, so after
            } else {
                return c1.name.compareTo(c2.name);
            }
        }).collect(Collectors.toList());
        if (!ascending) {
            Collections.reverse(sortedList);
        }
        return sortedList;
    }

    private CollectionSequence createCollectionSequenceLocal( String colSequenceName, String colSequenceDesc, String docColName ){
        return createCollectionSequenceLocal(colSequenceName, docColName, colSequenceDesc, true, true );
    }

    private CollectionSequence createCollectionSequenceLocal( String colSequenceName, String colSequenceDesc, String docColName, boolean createDocCollection, boolean evaluationEnabled ) {
        DocumentCollection createdCollection = null;

        if ( createDocCollection )
        {
            createdCollection = createUniqueDocumentCollection(Strings.isNullOrEmpty( docColName ) ? getUniqueString("testCollectionName") : docColName );
        }

        if ( Strings.isNullOrEmpty(colSequenceName)){
            colSequenceName = getUniqueString("createCollectionSequenceLocal_");
        }
        if ( Strings.isNullOrEmpty(colSequenceDesc)){
            colSequenceDesc = getUniqueString("createCollectionSequenceLocal_");
        }

        return createCollectionSequence( createdCollection, colSequenceName, colSequenceDesc, evaluationEnabled );
    }


    private void checkSequenceWorkflow(SequenceWorkflow sequenceWorkflow, SequenceWorkflow returned) {
        Assert.assertNotNull(returned);
        Assert.assertTrue(returned.id != 0);
        Assert.assertEquals("Workflow item name should match", sequenceWorkflow.name, returned.name);
        Assert.assertEquals("Workflow item description should match", sequenceWorkflow.description, returned.description);
        Assert.assertEquals("Workflow item notes should match", sequenceWorkflow.notes, returned.notes);

        if ( sequenceWorkflow.sequenceWorkflowEntries != null )
        {
            assertNotNull(returned.sequenceWorkflowEntries);
            assertEquals("entries size should match", sequenceWorkflow.sequenceWorkflowEntries.size(),  returned.sequenceWorkflowEntries.size());

            for ( SequenceWorkflowEntry sourceEntry : sequenceWorkflow.sequenceWorkflowEntries )
            {
                // find each match and check it.
                SequenceWorkflowEntry returnedEntry = returned.sequenceWorkflowEntries.stream().filter(u->u.collectionSequenceId.equals(sourceEntry.collectionSequenceId)).findFirst().get();
                checkSequenceWorkflowEntry(returnedEntry , sourceEntry);
            }
        }
    }

    private void checkSequenceWorkflowEntry( SequenceWorkflowEntry returnedEntry,
                                             SequenceWorkflowEntry sourceEntry )
    {
        assertNotNull("Must have matching SequenceWorkflowEntry returned", returnedEntry );
        
        // all entries need order, and if source has an order, it must match
        assertNotNull("SequenceWorkflowEntry must have order", returnedEntry.order);
        if ( sourceEntry.order!=null)
        {
            assertEquals("SequenceWorkflowEntry Order must match", sourceEntry.order, returnedEntry.order);
        }
    }
}
