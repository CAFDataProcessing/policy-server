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
package com.github.cafdataprocessing.corepolicy.booleanagent;

import com.github.cafdataprocessing.corepolicy.common.ElasticsearchProperties;
import com.github.cafdataprocessing.corepolicy.common.shared.MetadataValue;
import com.github.cafdataprocessing.corepolicy.document.DocumentUnderEvaluationImpl;
import com.github.cafdataprocessing.corepolicy.domainModels.BooleanAgentDocument;
import com.github.cafdataprocessing.corepolicy.testing.IntegrationTestBase;
import com.github.cafdataprocessing.corepolicy.TestHelper;
import com.github.cafdataprocessing.corepolicy.common.ConditionEngineMetadata;
import com.github.cafdataprocessing.corepolicy.common.dto.conditions.TextCondition;
import com.github.cafdataprocessing.corepolicy.document.DocumentUnderEvaluation;
import com.github.cafdataprocessing.corepolicy.domainModels.BooleanAgentDocuments;
import com.github.cafdataprocessing.corepolicy.testing.Assume;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.util.*;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;

/**
 *
 */
public abstract class BooleanAgentServicesBaseIT extends IntegrationTestBase {
    private static Logger logger = LoggerFactory.getLogger(BooleanAgentServicesBaseIT.class);

    private final String instanceId = UUID.randomUUID().toString();

    protected BooleanAgentServices booleanAgentServices;
    protected ElasticsearchProperties elasticsearchProperties;
    protected ConditionEngineMetadata conditionEngineMetadata;

    public BooleanAgentServicesBaseIT()
    {
        elasticsearchProperties = genericApplicationContext.getBean(ElasticsearchProperties.class);
        conditionEngineMetadata = genericApplicationContext.getBean(ConditionEngineMetadata.class);
    }

    public abstract String getNearExpression(String lhs, String rhs);
    public abstract String getAndExpression(String lhs, String rhs);
    public abstract String getOrExpression(String lhs, String rhs);
    public abstract String getSimilarDocumentExpression(String originalExpression, Collection<Term> distinctTerms);

    public void waitForIndexRefresh() throws Exception {
        return;
    }

    @Before
    public void checkTestIsSupported(){

        // we can either take a decision to create a new application context for these tests, in a preset mode,
        // or ignore the tests at runtime if the mode of execution is not what is required.
        // but its easily changed to match others that use the TemporaryEnvChanger
        Assume.assumeTrue(Assume.AssumeReason.BY_DESIGN,
                "BooleanAgentServicesBaseIT::checkTestIsSupported", String.format("Tests will only run when Elasticsearch" +
                        " is enabled."),
                !elasticsearchProperties.isElasticsearchDisabled(),
                genericApplicationContext);
    }

    @Test
    public void testQuery() throws Exception {

        testCreate();
        DocumentUnderEvaluation document = new DocumentUnderEvaluationImpl(conditionEngineMetadata,apiProperties);
        document.addMetadataString("content", "There is beer test here. This is no beer. Beer here. Beer no none.");

        BooleanAgentQueryResult booleanAgentQueryResult = booleanAgentServices.query(instanceId, document.getMetadata().get("content"));
        Assert.assertEquals(1, booleanAgentQueryResult.getConditionIdTerms().keys().stream().distinct().count());
    }

    @Test
    public void testQueryOnFieldWithNoValueReturnsBlankResult() throws Exception {
        testCreate();

        Collection<String> values = new ArrayList<>();

        BooleanAgentQueryResult booleanAgentQueryResult = booleanAgentServices.query(instanceId,
                MetadataValue.getMetadataValues( values, apiProperties));
        Assert.assertNotNull("booleanAgentQueryResult must not be null, with no values supplied", booleanAgentQueryResult);
    }

    @Test
    public void testCreate() throws Exception {
        Collection<BooleanAgentDocument> documents = new ArrayList<>();

        BooleanAgentDocument document = new BooleanAgentDocument();
        document.setReference("99");
        document.setCondition_id(Arrays.asList("99"));
        document.setBooleanRestriction(Arrays.asList(getNearExpression("beer", "test")));
        document.setContent("beer test");
        documents.add(document);

        BooleanAgentDocuments addToTextIndexDocuments = new BooleanAgentDocuments();
        addToTextIndexDocuments.setDocuments(documents);
        booleanAgentServices.create(instanceId, addToTextIndexDocuments);
    }

    @Test
    public void testExistsAndDelete() throws Exception {
        String newInstanceId = UUID.randomUUID().toString();

        Collection<BooleanAgentDocument> documents = new ArrayList<>();

        BooleanAgentDocument document = new BooleanAgentDocument();
        document.setReference("99");
        document.setCondition_id(Arrays.asList("99"));
        document.setBooleanRestriction(Arrays.asList(getNearExpression("beer", "test")));
        document.setContent("beer test");
        documents.add(document);

        BooleanAgentDocuments addToTextIndexDocuments = new BooleanAgentDocuments();
        addToTextIndexDocuments.setDocuments(documents);
        booleanAgentServices.create(newInstanceId, addToTextIndexDocuments);

        assertTrue(booleanAgentServices.existForInstanceId(newInstanceId));

        booleanAgentServices.delete(newInstanceId);

        waitForIndexRefresh();

        assertFalse(booleanAgentServices.existForInstanceId(newInstanceId));
    }

    @Test
    public void testHHJones() throws Exception {
        List<String> expectation = Arrays.asList("H.H.Jones");
        testExpression(1001L, "This is H.H.Jones", "H.H.Jones", expectation);
    }

    @Test
    public void testHHJonesNearLondon() throws Exception {
        List<String> expectation = Arrays.asList("H.H.Jones", "London");
        testExpression(1002L, "This is H.H.Jones he is near to London. J.J.Jones is near to Belfast.",
                getNearExpression("H.H.Jones", "London"), expectation);
    }

    @Test
    public void testUNearI() throws Exception {
        testExpression(1003L, "Hey John, this is just between u and I, sell your stock tomorrow!",
                getNearExpression("U", "I"),
                Arrays.asList("u", "i"));
    }

    @Test
    public void testInvalidExpression(){
        TestHelper.shouldThrow((o) -> {
            booleanAgentServices.isValidExpression(getAndExpression(getNearExpression("cat", "dog"), getNearExpression("(you", "work")));
        });
    }

    @Test
    public void testValidExpression(){
        booleanAgentServices.isValidExpression(getNearExpression("cat", "dog"));
    }

//    @Test
    public void test100kConditions() throws Exception {
        Collection<BooleanAgentDocument> documents = new ArrayList<>();

        for(int index=0; index <100000; index++){
            BooleanAgentDocument document = new BooleanAgentDocument();
            document.setReference(String.valueOf(index));
            document.setCondition_id(Arrays.asList(String.valueOf(index)));
            String expression = getOrExpression(getNearExpression(UUID.randomUUID().toString(), UUID.randomUUID().toString()), getNearExpression("APPLE", "CART"));
            document.setBooleanRestriction(Arrays.asList(expression));
            documents.add(document);
        }

        BooleanAgentDocuments addToTextIndexDocuments = new BooleanAgentDocuments();
        addToTextIndexDocuments.setDocuments(documents);

        long time = System.currentTimeMillis();
        booleanAgentServices.create(instanceId, addToTextIndexDocuments);
        long completed = System.currentTimeMillis() - time;
        logger.debug("created agents in {}", DurationFormatUtils.formatDuration(completed, "HH:mm:ss:SS"));

        DocumentUnderEvaluation document = new DocumentUnderEvaluationImpl(conditionEngineMetadata, apiProperties);
        document.addMetadataString("content", "There is beer test here. This is no beer. Don't tip the apple cart. Beer here. Beer no none.");

        time = System.currentTimeMillis();
        BooleanAgentQueryResult booleanAgentQueryResult = booleanAgentServices.query(instanceId, document.getMetadata().get("content"));
        completed = System.currentTimeMillis() - time;
        logger.debug("queried agents in {}", DurationFormatUtils.formatDuration(completed, "HH:mm:ss:SS"));

        Assert.assertNotEquals(0, booleanAgentQueryResult.getConditionIdTerms().keys().stream().distinct().count());
    }

    protected void testExpression(Long conditionId, String input, String expression, Collection<String> expectedTerms) throws Exception {
        Collection<BooleanAgentDocument> documents = new ArrayList<>();
        BooleanAgentDocument document = new BooleanAgentDocument();
        document.setReference(conditionId.toString());
        document.setCondition_id(Arrays.asList(conditionId.toString()));
        document.setBooleanRestriction(Arrays.asList(expression));
        documents.add(document);

        BooleanAgentDocuments addToTextIndexDocuments = new BooleanAgentDocuments();
        addToTextIndexDocuments.setDocuments(documents);
        booleanAgentServices.create(instanceId, addToTextIndexDocuments);

        DocumentUnderEvaluation documentUnderEvaluation = new DocumentUnderEvaluationImpl(conditionEngineMetadata, apiProperties);
        documentUnderEvaluation.addMetadataString("content", input);

        BooleanAgentQueryResult booleanAgentQueryResult = booleanAgentServices.query(instanceId, documentUnderEvaluation.getMetadata().get("content"));

        Assert.assertTrue(booleanAgentQueryResult.getConditionIdTerms().keys().contains(conditionId));
        Collection<String> retrievedTerms = booleanAgentQueryResult.getConditionIdTerms().get(conditionId);
        Assert.assertEquals(expectedTerms.size(), retrievedTerms.size());
        for(String expectedTerm: expectedTerms){
            Assert.assertTrue("Returned expectedTerms does not contain '" + expectedTerm + "'", retrievedTerms.stream().filter(t -> t.equalsIgnoreCase(expectedTerm)).count()>0);
        }
    }

    @Test
    public void testConditionFindSimilarDocuments()  throws Exception{

        TextCondition textCondition = new TextCondition();

        String input = "a lazy dog jumped over the fox.";
        Collection<Term> terms = booleanAgentServices.doTermGetInfo(input);

        if ( terms.size() == 0 )
        {
            Assert.fail("testConditionFindSimilarDocuments: Failed due to having no valid term info.");
        }

        textCondition.field = "content";
        textCondition.value = getSimilarDocumentExpression(input, terms);
        textCondition.notes = "TEST testConditionFindSimilarDocuments: This is used to find similar documents on DRECONTENT";
        textCondition.id = 10010L;

        Collection<BooleanAgentDocument> documents = new ArrayList<>();
        BooleanAgentDocument document = new BooleanAgentDocument();
        document.setReference(textCondition.id.toString());
        document.setCondition_id(Arrays.asList(textCondition.id.toString()));
        document.setBooleanRestriction(Arrays.asList(textCondition.value));
        documents.add(document);

        BooleanAgentDocuments addToTextIndexDocuments = new BooleanAgentDocuments();
        addToTextIndexDocuments.setDocuments(documents);
        booleanAgentServices.create(instanceId, addToTextIndexDocuments);

        DocumentUnderEvaluation documentUnderEvaluation = new DocumentUnderEvaluationImpl(conditionEngineMetadata, apiProperties);
        documentUnderEvaluation.addMetadataString("content", input);

        BooleanAgentQueryResult booleanAgentQueryResult = booleanAgentServices.query(instanceId, documentUnderEvaluation.getMetadata().get("content"));

        Assert.assertTrue(booleanAgentQueryResult.getConditionIdTerms().keys().contains(textCondition.id));

        // how do we verify this?
        Collection<String> retrievedTerms = booleanAgentQueryResult.getConditionIdTerms().get(textCondition.id);

        Collection<Term> termsInStoredQuery = booleanAgentServices.doTermGetInfo(input);
        Assert.assertFalse(termsInStoredQuery.stream().anyMatch(t -> t.getDocumentOccurrences() == 0));
    }

    @Override
    protected Connection getConnectionToClearDown() {
        return null;
    }

}