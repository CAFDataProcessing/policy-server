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
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.github.cafdataprocessing.corepolicy.common.dto.*;
import com.github.cafdataprocessing.corepolicy.common.dto.conditions.*;
import com.github.cafdataprocessing.corepolicy.testing.IntegrationTestBase;
import com.github.cafdataprocessing.corepolicy.common.ApiProperties;
import com.github.cafdataprocessing.corepolicy.common.ClassificationApi;
import com.github.cafdataprocessing.corepolicy.common.EnvironmentSnapshot;
import com.github.cafdataprocessing.corepolicy.common.PolicyApi;
import com.github.cafdataprocessing.corepolicy.common.domainModels.EnvironmentSnapshotImpl;
import com.github.cafdataprocessing.corepolicy.common.dtoweb.RetrieveAdditional;
import com.github.cafdataprocessing.corepolicy.common.dtoweb.RetrieveRequest;
import com.github.cafdataprocessing.corepolicy.common.shared.CorePolicyObjectMapper;
import com.github.cafdataprocessing.corepolicy.environment.EnvironmentSnapshotApi;
import com.github.cafdataprocessing.corepolicy.repositories.RepositoryConnectionProvider;
import com.github.cafdataprocessing.corepolicy.repositories.RepositoryType;
import org.apache.commons.lang3.Validate;
import org.apache.http.NameValuePair;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.sql.Connection;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Tests the environment snapshot api
 */
public class EnvironmentSnapshotApiIT extends IntegrationTestBase {

    protected EnvironmentSnapshotApi sut;
    protected ClassificationApi classificationApi;
    protected PolicyApi policyApi;
    protected ApiProperties apiProperties;
    private ObjectMapper mapper = new CorePolicyObjectMapper();

    private CollectionSequence collectionSequenceInstance;
    private Collection<DocumentCollection> collectionsInstance = new LinkedList<>();

    public EnvironmentSnapshotApiIT(){
        sut = genericApplicationContext.getBean(EnvironmentSnapshotApi.class);
        classificationApi = genericApplicationContext.getBean(ClassificationApi.class);
        policyApi = genericApplicationContext.getBean(PolicyApi.class);
        apiProperties = genericApplicationContext.getBean(ApiProperties.class);
    }


    /**
     * IMPORTANT: This test will NOT test the accuracy of the returned objects, just that the number are what we are
     * expecting. The Individual calls on the Classification API tests should manage the individual properties.
     * @throws Exception
     */
    @Test
    public void testCorrectEnvironmentReturned() throws Exception{
        /*need to check the following are all correctly returned from the snapshot call:
            collectionSequenceId
            instanceId -> do we? Not null?
            createDate
            persistedDate
            
            collectionSequences
            collections
            conditions
            fieldLabels
            lexicons
            policies
            policyTypes

            CollectionSequenceA
                CollectionA
                    ConditionA (field exists INTERNET)
                CollectionB
                    ConditionB (Lexicon 1, MYFIELD)
                CollectionC
                    ConditionC (fragment to FragmentConditionA)

            FragmentConditionA (field exists MYFIELD)

            FieldLabel: there are none in the db :(

            LexiconA
                ExpressionA
                ExpressionB

            MetadataPolicyA
            Metadata Policy Type
        */



        initializeTest();

        EnvironmentSnapshot environmentSnapshot = sut.get(this.collectionSequenceInstance.id, null, null);

        Assert.assertNotNull("Have a snapshot", environmentSnapshot);

        Assert.assertEquals("Sequence Id", this.collectionSequenceInstance.id, environmentSnapshot.getCollectionSequenceId());
        Assert.assertTrue("Created today", environmentSnapshot.getCreateDate().dayOfYear().get() == DateTime.now().dayOfYear().get());

        Assert.assertEquals("CollectionSequence modified today", collectionSequenceInstance.lastModified, environmentSnapshot.getCollectionSequenceLastModifiedDate().toDateTime());

        Assert.assertEquals("Single collection sequence", 1, environmentSnapshot.getCollectionSequences().size());
        Assert.assertEquals("3 collections", 3, environmentSnapshot.getCollections().size());
        Assert.assertEquals("4, conditions", 4, environmentSnapshot.getConditions().size());

        Assert.assertEquals("1 lexicon", 1, environmentSnapshot.getLexicons().size());
        Assert.assertEquals("No field labels", 0, environmentSnapshot.getFieldLabels().size());
    }

    /**
     * This regression test will make sure that the snapshot fingerprint is set - it was not being sent over the web dto.
     */
    @Test
    public void testSnapshotFingerprintSet() throws Exception{

        initializeTest();

        EnvironmentSnapshot environmentSnapshot = sut.get(this.collectionSequenceInstance.id, null, null);

        Assert.assertNotNull("Have a snapshot", environmentSnapshot);

        Assert.assertNotNull("Snapshot fingerprint not null", environmentSnapshot.getFingerprint());
        Assert.assertEquals("Snapshot fingerprint is sequence fingerprint", environmentSnapshot.getFingerprint(), environmentSnapshot.getCollectionSequences().get(environmentSnapshot.getCollectionSequenceId()).fingerprint);
    }

    /**
     * Method to test that the json object's conditions only include fragment conditions! Not so much an integration
     * test as we are not testing the interface but the specific implementation, but because it requires a running
     * system I think it should be here at least for now.
     * @throws Exception
     */
    @Test
    public void testWebOnlyContainsFragments() throws Exception{
        String apiMode = this.apiProperties.getMode();
        Assume.assumeTrue("Only running this test against web interfaces", "web".equalsIgnoreCase(apiMode));

        initializeTest();

        WebApiBase sutBase = (WebApiBase)sut;

        RetrieveRequest retrieveRequest = new RetrieveRequest();
        retrieveRequest.type = ItemType.COLLECTION_SEQUENCE;
        retrieveRequest.id = Arrays.asList(collectionSequenceInstance.id);

        retrieveRequest.additional = new RetrieveAdditional();
        retrieveRequest.additional.includeChildren = true;
        retrieveRequest.additional.datetime = null;

        final Collection<NameValuePair> params = sutBase.createParams(retrieveRequest);

        String resp = sutBase.makeRequest(WebApiAction.RETRIEVE, params);
        TypeFactory typeFactory = TypeFactory.defaultInstance();
        final PageOfResults<EnvironmentSnapshotImpl> deserializedResult = mapper.readValue(resp,
                typeFactory.constructParametricType(PageOfResults.class, EnvironmentSnapshot.class));

        Assert.assertEquals("Got a snapshot", 1, (long)deserializedResult.totalhits);

        EnvironmentSnapshotImpl environmentSnapshot = deserializedResult.results.stream().findFirst().get();

        Assert.assertNotNull("Have a snapshot", environmentSnapshot);

        Assert.assertEquals("Sequence Id", this.collectionSequenceInstance.id, environmentSnapshot.getCollectionSequenceId());

        Assert.assertEquals("expected 4 conditions", 4, environmentSnapshot.getConditions().size());
    }

    /**
     * There was an issue with archives involving date conditions direct to the db (was ok via web service). This test
     * is a regression test to ensure it won't happen again.
     */
    @Test
    public void testDateCondition() throws Exception{
        DateCondition dateCondition = new DateCondition();
        dateCondition.operator = DateOperator.AFTER;
        dateCondition.value = "1412891688e";
        dateCondition.field = "somefield";
        dateCondition.name = "Testing archive date condition format as date_value";

        DocumentCollection documentCollection = new DocumentCollection();
        documentCollection.name = "Testing archive date condition format as date_value";
        documentCollection.condition = dateCondition;

        documentCollection = this.classificationApi.create(documentCollection);

        CollectionSequence sequence = new CollectionSequence();
        sequence.name = "Testing archive date condition format as date_value";
        sequence.collectionSequenceEntries = new ArrayList<>(1);

        CollectionSequenceEntry entry = new CollectionSequenceEntry();
        entry.collectionIds = new HashSet<>(1);
        entry.collectionIds.add(documentCollection.id);
        entry.order = (short) 100;

        sequence.collectionSequenceEntries.add(entry);

        sequence = this.classificationApi.create(sequence);

        this.sut.get(sequence.id, null, null);
    }


    /**
     * There was an issue with archives involving date conditions direct to the db (was ok via web service). This test
     * is a regression test to ensure it won't happen again - this time for value -918893385e as it was failing despite
     * fixing the above test.
     */
    @Test
    public void testDateCondition2() throws Exception{
        DateCondition dateCondition = new DateCondition();
        dateCondition.operator = DateOperator.AFTER;
        dateCondition.value = "-918893385e";
        dateCondition.field = "somefield";
        dateCondition.name = "testDateCondition2";

        DocumentCollection documentCollection = new DocumentCollection();
        documentCollection.name = "testDateCondition2";
        documentCollection.condition = dateCondition;

        documentCollection = this.classificationApi.create(documentCollection);

        CollectionSequence sequence = new CollectionSequence();
        sequence.name = "testDateCondition2";
        sequence.collectionSequenceEntries = new ArrayList<>(1);

        CollectionSequenceEntry entry = new CollectionSequenceEntry();
        entry.collectionIds = new HashSet<>(1);
        entry.collectionIds.add(documentCollection.id);
        entry.order = (short) 100;

        sequence.collectionSequenceEntries.add(entry);

        sequence = this.classificationApi.create(sequence);

        this.sut.get(sequence.id, null, null);
    }

    /**
     * PD-586 regression - if there was a catch all collection, it's conditions were not being fingerprinted and threw
     * an error when they were added to the archive table.
     */
    @Test
    public void testOnlyCatchAllRetrievedSuccessfully(){
        ExistsCondition condition = new ExistsCondition();
        condition.field = "field";
        condition.name = "testOnlyCatchAllRetrievedSuccessfully";

        DocumentCollection collection = new DocumentCollection();
        collection.name ="testOnlyCatchAllRetrievedSuccessfully";
        collection.condition = condition;

        collection = classificationApi.create(collection);

        CollectionSequence sequence = new CollectionSequence();
        sequence.name = "testOnlyCatchAllRetrievedSuccessfully";
        sequence.collectionSequenceEntries = new ArrayList<>(1);
        sequence.defaultCollectionId = collection.id;

        sequence = this.classificationApi.create(sequence);

        this.sut.get(sequence.id, null, null);
    }


    /**
     * Method creates a full environment that is needed to test the retrieval method
     * @throws IOException
     */
    private void initializeTest() throws IOException {
        PolicyType metadataPolicyType = policyApi.retrievePolicyTypeByName("MetadataPolicy");
        Condition fragmentConditionInstance;
        Lexicon lexiconInstance;

        //create lexicon expressions
        {
            LexiconExpression expressionA = new LexiconExpression();
            expressionA.expression = "text expression A";
            expressionA.type = LexiconExpressionType.TEXT;

            LexiconExpression expressionB = new LexiconExpression();
            expressionB.expression = ".*abc.*";
            expressionB.type = LexiconExpressionType.REGEX;

            Lexicon newLexicon = new Lexicon();
            newLexicon.name = "LexiconA";
            newLexicon.description = "Testing environment snapshot.";
            newLexicon.lexiconExpressions = new LinkedList<>();
            newLexicon.lexiconExpressions.add(expressionA);
            newLexicon.lexiconExpressions.add(expressionB);

            lexiconInstance = classificationApi.create(newLexicon);
        }

        //create metadata policy
        {
            Policy metadataPolicyA = new Policy();
            metadataPolicyA.typeId = metadataPolicyType.id;
            metadataPolicyA.name = "MetadataPolicyA";
            metadataPolicyA.description = "Testing environment snapshot.";
            metadataPolicyA.details = mapper.readTree("{\n" +
                    "\"title\" : \"Test Metadata Policy\"," +
                    "\"description\" : \"Testing environment snapshot.\""+
                    "}");

            metadataPolicyA.priority = 10;
        }

        //CollectionA
        {
            DocumentCollection collectionA = new DocumentCollection();
            collectionA.name = "CollectionA";
            collectionA.description = "Testing environment snapshot.";
            collectionA.condition = new ExistsCondition();
            collectionA.condition.name = "ConditionA";
            ((ExistsCondition)collectionA.condition).field = "INTERNET";

            collectionsInstance.add(classificationApi.create(collectionA));
        }

        //CollectionB
        {
            DocumentCollection collectionB = new DocumentCollection();
            collectionB.name = "collectionB";
            collectionB.description = "Testing environment snapshot.";
            collectionB.condition = new LexiconCondition();
            collectionB.condition.name = "ConditionB";
            ((LexiconCondition)collectionB.condition).field = "MYFIELD";
            ((LexiconCondition)collectionB.condition).value = lexiconInstance.id;

            collectionsInstance.add(classificationApi.create(collectionB));
        }

        //FragmentConditionA

        {
            ExistsCondition fragmentConditionA = new ExistsCondition();
            fragmentConditionA.name = "FragmentConditionA";
            fragmentConditionA.field = "MYFIELD";

            fragmentConditionInstance = this.classificationApi.create(fragmentConditionA);
        }

        //CollectionC
        {
            DocumentCollection collectionC = new DocumentCollection();
            collectionC.name = "collectionC";
            collectionC.description = "Testing environment snapshot.";
            collectionC.condition = new FragmentCondition();
            collectionC.condition.name = "ConditionC";
            ((FragmentCondition)collectionC.condition).value = fragmentConditionInstance.id;

            collectionsInstance.add(classificationApi.create(collectionC));
        }

        //collection sequence
        {
            CollectionSequence collectionSequenceA = new CollectionSequence();
            collectionSequenceA.name = "CollectionSequenceA";
            collectionSequenceA.description = "Testing environment snapshot.";
            collectionSequenceA.fullConditionEvaluation = true;

            CollectionSequenceEntry entry = new CollectionSequenceEntry();
            entry.order = 100;
            entry.collectionIds.addAll(this.collectionsInstance.stream().map(c -> c.id).collect(Collectors.toList()));
            collectionSequenceA.collectionSequenceEntries.add(entry);

            this.collectionSequenceInstance = classificationApi.create(collectionSequenceA);

            // Get the real values in the collection sequence, some items such as the last modification time,
            // are only correct after retrieve.
            Collection<CollectionSequence> result = classificationApi.retrieveCollectionSequences(Arrays.asList(collectionSequenceInstance.id));
            Validate.notEmpty(result);
            Validate.isTrue(result.size() == 1);

            collectionSequenceInstance = result.stream().findFirst().get();
        }
    }



    @Override
    protected Connection getConnectionToClearDown() {
        if(apiProperties.getMode().equalsIgnoreCase("direct")) {
            RepositoryConnectionProvider repositoryConnectionProvider = genericApplicationContext.getBean(RepositoryConnectionProvider.class);
            return repositoryConnectionProvider.getConnection(RepositoryType.POLICY);
        }
        throw new InvalidParameterException(apiProperties.getMode());
    }
}
