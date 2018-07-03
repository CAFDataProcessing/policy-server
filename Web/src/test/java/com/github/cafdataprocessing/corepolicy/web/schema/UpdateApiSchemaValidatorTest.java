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
package com.github.cafdataprocessing.corepolicy.web.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.cafdataprocessing.corepolicy.common.dto.*;
import com.github.cafdataprocessing.corepolicy.common.dto.conditions.*;
import com.github.fge.jsonschema.core.exceptions.InvalidSchemaException;
import org.joda.time.DateTime;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.fail;

public class UpdateApiSchemaValidatorTest extends ApiSchemaValidatorTestBase {
    final ApiSchemaValidator sut = new ClassificationUpdateApiSchemaValidator();
    final ApiSchemaValidator policyValidator = new PolicyUpdateApiSchemaValidator();
    //CollectionSequence, Collection, Condition, Lexicon, FieldLabel, Policy, Policy Type

    @Test
    public void testUpdateEmptyCollectionSequence() throws Exception {
        CollectionSequence collectionSequence = new CollectionSequence();
        JsonNode jsonNode = mapper.readTree(mapper.writeValueAsString(collectionSequence));
        try{
            sut.validateRequest(jsonNode);
            fail("Json did not fail to validate. Should have thrown exception");
        }
        catch (InvalidSchemaException e){
            fail("Schema invalid");
        }
        catch (Exception e){
            System.out.print("Correct exception thrown \n"+ e);
        }
    }

    @Test
    public void testUpdateCollectionSequenceNoId() throws Exception {
        CollectionSequence collectionSequence = new CollectionSequence();
        collectionSequence.name = "Hello";
        collectionSequence.description = "My description";
        JsonNode jsonNode = mapper.readTree(mapper.writeValueAsString(collectionSequence));
        try{
            sut.validateRequest(jsonNode);
            fail("Json did not fail to validate. Should have thrown exception");
        }
        catch (InvalidSchemaException e){
            fail("Schema invalid");
        }
        catch (Exception e){
            System.out.print("Correct exception thrown \n"+ e);
        }
    }

    @Test
    public void testUpdateCollectionSequenceNoName() throws Exception {
        CollectionSequence collectionSequence = new CollectionSequence();
        collectionSequence.id = 2L;
        collectionSequence.description = "My description";
        JsonNode jsonNode = mapper.readTree(mapper.writeValueAsString(collectionSequence));
        try{
            sut.validateRequest(jsonNode);
            fail("Json did not fail to validate. Should have thrown exception");
        }
        catch (InvalidSchemaException e){
            fail("Schema invalid");
        }
        catch (Exception e){
            System.out.print("Correct exception thrown \n" + e);
        }
    }

    @Test
    public void testUpdateCollectionSequenceValid() throws Exception {
        CollectionSequence collectionSequence = new CollectionSequence();
        collectionSequence.id = 2L;
        collectionSequence.name = "name";
        collectionSequence.collectionSequenceEntries = new LinkedList<>();
        collectionSequence.collectionCount = 0;
        collectionSequence.lastModified = DateTime.now();
        collectionSequence.fullConditionEvaluation = false;
        JsonNode jsonNode = mapper.readTree(mapper.writeValueAsString(collectionSequence));
        try{
            sut.validateRequest(jsonNode);
        }
        catch (InvalidSchemaException e){
            fail("Schema invalid");
        }
        catch (Exception e){
            fail("Json failed to validate " + e);
        }
    }

    @Test
    public void testUpdateEmptyCollection() throws Exception{
        DocumentCollection documentCollection = new DocumentCollection();
        JsonNode jsonNode = mapper.readTree(mapper.writeValueAsString(documentCollection));
        try{
            sut.validateRequest(jsonNode);
            fail("Json did not fail to validate. Should have thrown exception");
        }
        catch (InvalidSchemaException e){
            fail("Schema invalid");
        }
        catch (Exception e){
            System.out.print("Correct exception thrown \n" + e);
        }
    }

    @Test
    public void testUpdateCollectionNoId() throws Exception{
        DocumentCollection collection = new DocumentCollection();
        collection.name = "name";
        collection.description = "description";
        collection.policyIds = new HashSet<>(Arrays.asList(753L, 754L));
        ExistsCondition existsCondition = new ExistsCondition();
        existsCondition.field = "field";
        existsCondition.isFragment = false;
        existsCondition.includeDescendants = false;
        existsCondition.target = ConditionTarget.CONTAINER;
        collection.condition = existsCondition;
        JsonNode jsonNode = mapper.readTree(mapper.writeValueAsString(collection));
        try{
            sut.validateRequest(jsonNode);
            fail("Json did not fail to validate. Should have thrown exception");
        }
        catch (InvalidSchemaException e){
            fail("Schema invalid");
        }
        catch (Exception e){
            System.out.print("Correct exception thrown \n" + e);
        }
    }

    @Test
    public void testUpdateCollectionNoName() throws Exception{
        DocumentCollection collection = new DocumentCollection();
        collection.id = 5L;
        collection.policyIds = new HashSet<>(Arrays.asList(753L, 754L));
        ExistsCondition existsCondition = new ExistsCondition();
        existsCondition.field = "field";
        existsCondition.isFragment = false;
        existsCondition.includeDescendants = false;
        existsCondition.target = ConditionTarget.CONTAINER;
        collection.condition = existsCondition;
        JsonNode jsonNode = mapper.readTree(mapper.writeValueAsString(collection));
        try{
            sut.validateRequest(jsonNode);
            fail("Json did not fail to validate. Should have thrown exception");
        }
        catch (InvalidSchemaException e){
            fail("Schema invalid");
        }
        catch (Exception e){
            System.out.print("Correct exception thrown \n" + e);
        }
    }

    @Test
    public void testUpdateValidCollectionNoCondition() throws Exception{
        DocumentCollection collection = new DocumentCollection();
        collection.id = 5L;
        collection.name = "name";
        collection.policyIds = new HashSet<>();
        JsonNode jsonNode = mapper.readTree(mapper.writeValueAsString(collection));
        try{
            sut.validateRequest(jsonNode);
        }
        catch (InvalidSchemaException e){
            fail("Schema invalid");
        }
        catch (Exception e){
            fail("Json failed to validate " + e);
        }
    }

    @Test
    public void testUpdateValidCollectionValidCondition() throws Exception{
        DocumentCollection collection = new DocumentCollection();
        collection.id = 5L;
        collection.name = "name";
        collection.policyIds = new HashSet<>();
        ExistsCondition existsCondition = new ExistsCondition();
        existsCondition.field = "field";
        existsCondition.isFragment = false;
        existsCondition.includeDescendants = false;
        existsCondition.target = ConditionTarget.CONTAINER;
        collection.condition = existsCondition;
        JsonNode jsonNode = mapper.readTree(mapper.writeValueAsString(collection));
        try{
            sut.validateRequest(jsonNode);
        }
        catch (InvalidSchemaException e){
            fail("Schema invalid");
        }
        catch (Exception e){
            fail("Json failed to validate " + e);
        }
    }

    @Test
    public void testUpdateValidCollectionInvalidCondition() throws Exception{
        DocumentCollection collection = new DocumentCollection();
        collection.id = 5L;
        collection.name = "name";
        collection.policyIds = new HashSet<>();
        ExistsCondition existsCondition = new ExistsCondition();
       //Condition missing field
        existsCondition.includeDescendants = false;
        existsCondition.target = ConditionTarget.CONTAINER;
        collection.condition = existsCondition;
        JsonNode jsonNode = mapper.readTree(mapper.writeValueAsString(collection));
        try{
            sut.validateRequest(jsonNode);
            fail("Json did not fail to validate. Should have thrown exception");
        }
        catch (InvalidSchemaException e){
            fail("Schema invalid");
        }
        catch (Exception e){
            System.out.print("Correct exception thrown \n" + e);
        }
    }

    @Test
    public void testUpdateEmptyCondition() throws Exception {
        ExistsCondition condition = new ExistsCondition();
        JsonNode jsonNode = mapper.readTree(mapper.writeValueAsString(condition));
        try{
            sut.validateRequest(jsonNode);
            fail("Json did not fail to validate. Should have thrown exception");
        }
        catch (InvalidSchemaException e){
            fail("Schema invalid");
        }
        catch (Exception e){
            System.out.print("Correct exception thrown \n" + e);
        }
    }

    @Test
    public void testUpdateConditionNoId() throws Exception{
        ExistsCondition condition = new ExistsCondition();
        condition.field = "field1";
        condition.name = "name";
        condition.target = ConditionTarget.CONTAINER;
        JsonNode jsonNode = mapper.readTree(mapper.writeValueAsString(condition));
        try{
            sut.validateRequest(jsonNode);
            fail("Json did not fail to validate. Should have thrown exception");
        }
        catch (InvalidSchemaException e){
            fail("Schema invalid");
        }
        catch (Exception e){
            System.out.print("Correct exception thrown \n" + e);
        }
    }

    @Test
    public void testUpdateConditionNoName() throws Exception{
        ExistsCondition condition = new ExistsCondition();
        condition.field = "field1";
        condition.id = 2L;
        condition.target = ConditionTarget.CONTAINER;
        JsonNode jsonNode = mapper.readTree(mapper.writeValueAsString(condition));
        try{
            sut.validateRequest(jsonNode);
        }
        catch (InvalidSchemaException e){
            fail("Schema invalid");
        }
        catch (Exception e){
            fail("Json failed to validate " + e);
        }
    }

    @Test
    public void testUpdateValidCondition() throws Exception{
        TextCondition textCondition = new TextCondition();
        textCondition.id = 54L;
        textCondition.field = "field";
        textCondition.language = "eng";
        textCondition.value = "value";
        JsonNode jsonNode = mapper.readTree(mapper.writeValueAsString(textCondition));
        try{
            sut.validateRequest(jsonNode);
        }
        catch (InvalidSchemaException e){
            fail("Schema invalid");
        }
        catch (Exception e){
            fail("Json failed to validate " + e);
        }
    }

    @Test
    public void testUpdateValidNotCondition() throws Exception{
        NotCondition condition = new NotCondition();
        ExistsCondition existsCondition = new ExistsCondition();
        existsCondition.field = "Field1";
        condition.condition = existsCondition;
        condition.id = 500L;
        condition.isFragment = true;
        condition.includeDescendants = false;
        condition.target = ConditionTarget.CONTAINER;
        JsonNode jsonNode = mapper.readTree(mapper.writeValueAsString(condition));
        try{
            sut.validateRequest(jsonNode);
        }
        catch (InvalidSchemaException e){
            fail("Schema invalid");
        }
        catch (Exception e){
            fail("Json failed to validate " + e);
        }
    }

    @Test
    public void testUpdateInvalidNotCondition() throws Exception{
        NotCondition condition = new NotCondition();
        ExistsCondition existsCondition = new ExistsCondition();
        condition.condition = existsCondition;
        condition.id = 500L;
        condition.isFragment = true;
        condition.includeDescendants = false;
        condition.target = ConditionTarget.CONTAINER;
        JsonNode jsonNode = mapper.readTree(mapper.writeValueAsString(condition));
        try{
            sut.validateRequest(jsonNode);
            fail("Json did not fail to validate. Should have thrown exception");
        }
        catch (InvalidSchemaException e){
            fail("Schema invalid");
        }
        catch (Exception e){
            System.out.print("Correct exception thrown \n" + e);
        }
    }

    @Test
    public void testUpdateValidBooleanCondition() throws Exception{
        BooleanCondition booleanCondition = new BooleanCondition();
        booleanCondition.id = 4L;
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
        JsonNode jsonNode = mapper.readTree(mapper.writeValueAsString(booleanCondition));
        try{
            sut.validateRequest(jsonNode);
        }
        catch (InvalidSchemaException e){
            fail("Schema invalid");
        }
        catch (Exception e){
            fail("Json failed to validate " + e);
        }
    }

    @Test
    public void testUpdateInvalidBooleanCondition() throws Exception{
        BooleanCondition booleanCondition = new BooleanCondition();
        booleanCondition.id = 4L;
        booleanCondition.operator = BooleanOperator.AND;
        booleanCondition.children = new ArrayList<>();
        {
            ExistsCondition existsCondition = new ExistsCondition();
            booleanCondition.children.add(existsCondition);
        }
        {
            ExistsCondition existsCondition = new ExistsCondition();
            existsCondition.field = "my field1";
            booleanCondition.children.add(existsCondition);
        }
        JsonNode jsonNode = mapper.readTree(mapper.writeValueAsString(booleanCondition));
        try{
            sut.validateRequest(jsonNode);
            fail("Json did not fail to validate. Should have thrown exception");
        }
        catch (InvalidSchemaException e){
            fail("Schema invalid");
        }
        catch (Exception e){
            System.out.print("Correct exception thrown \n" + e);
        }
    }

    @Test
    public void testUpdateEmptyLexicon() throws Exception {
        Lexicon lexicon = new Lexicon();
        JsonNode jsonNode = mapper.readTree(mapper.writeValueAsString(lexicon));
        try{
            sut.validateRequest(jsonNode);
            fail("Json did not fail to validate. Should have thrown exception");
        }
        catch (InvalidSchemaException e){
            fail("Schema invalid");
        }
        catch (Exception e){
            System.out.print("Correct exception thrown \n" + e);
        }
    }

    @Test
    public void testUpdateLexiconNoId() throws Exception {
        Lexicon lexicon = new Lexicon();
        lexicon.name = "name";
        lexicon.lexiconExpressions = new ArrayList<>();
        JsonNode jsonNode = mapper.readTree(mapper.writeValueAsString(lexicon));
        try{
            sut.validateRequest(jsonNode);
            fail("Json did not fail to validate. Should have thrown exception");
        }
        catch (InvalidSchemaException e){
            fail("Schema invalid");
        }
        catch (Exception e){
            System.out.print("Correct exception thrown \n" + e);
        }
    }

    @Test
    public void testUpdateLexiconNoName() throws Exception {
        Lexicon lexicon = new Lexicon();
        lexicon.id = 5L;
        lexicon.lexiconExpressions = new ArrayList<>();
        JsonNode jsonNode = mapper.readTree(mapper.writeValueAsString(lexicon));
        try{
            sut.validateRequest(jsonNode);
            fail("Json did not fail to validate. Should have thrown exception");
        }
        catch (InvalidSchemaException e){
            fail("Schema invalid");
        }
        catch (Exception e){
            System.out.print("Correct exception thrown \n" + e);
        }
    }

    @Test
    public void testUpdateLexiconValidNoExpression() throws Exception{
        Lexicon lexicon = new Lexicon();
        lexicon.id = 5L;
        lexicon.name = "name";
        lexicon.lexiconExpressions = new ArrayList<>();
        JsonNode jsonNode = mapper.readTree(mapper.writeValueAsString(lexicon));
        try{
            sut.validateRequest(jsonNode);
        }
        catch (InvalidSchemaException e){
            fail("Schema invalid");
        }
        catch (Exception e){
            fail("Json failed to validate " + e);
        }
    }

    @Test
    public void testUpdateLexiconValidWithExpressions() throws Exception{
        Lexicon lexicon = new Lexicon();
        lexicon.id = 5L;
        lexicon.name = "name";
        lexicon.lexiconExpressions = new ArrayList<>();
        LexiconExpression lexiconExpression = new LexiconExpression();
        lexiconExpression.type = LexiconExpressionType.REGEX;
        lexiconExpression.expression = "ABD Citroên";
        lexiconExpression.id = 12L;
        lexiconExpression.lexiconId = lexicon.id;
        lexicon.lexiconExpressions = new LinkedList<>();
        lexicon.lexiconExpressions.add(lexiconExpression);
        JsonNode jsonNode = mapper.readTree(mapper.writeValueAsString(lexicon));
        try{
            sut.validateRequest(jsonNode);
        }
        catch (InvalidSchemaException e){
            fail("Schema invalid");
        }
        catch (Exception e){
            fail("Json failed to validate " + e);
        }
    }

    @Test
    public void testUpdateValidLexiconInvalidExpressions() throws Exception{
        Lexicon lexicon = new Lexicon();
        lexicon.id = 5L;
        lexicon.name = "name";
        lexicon.lexiconExpressions = new ArrayList<>();
        LexiconExpression lexiconExpression = new LexiconExpression();
        lexiconExpression.type = LexiconExpressionType.REGEX;
        lexiconExpression.lexiconId = lexicon.id;
        lexicon.lexiconExpressions = new LinkedList<>();
        lexicon.lexiconExpressions.add(lexiconExpression);
        JsonNode jsonNode = mapper.readTree(mapper.writeValueAsString(lexicon));
        try{
            sut.validateRequest(jsonNode);
            fail("Json did not fail to validate. Should have thrown exception");
        }
        catch (InvalidSchemaException e){
            fail("Schema invalid");
        }
        catch (Exception e){
            System.out.print("Correct exception thrown \n" + e);
        }
    }

    @Test
    public void testUpdateEmptyPolicy() throws Exception {
        Policy policy = new Policy();
        JsonNode jsonNode = mapper.readTree(mapper.writeValueAsString(policy));
        try{
            policyValidator.validateRequest(jsonNode);
            fail("Json did not fail to validate. Should have thrown exception");
        }
        catch (InvalidSchemaException e){
            fail("Schema invalid");
        }
        catch (Exception e){
            System.out.print("Correct exception thrown \n" + e);
        }
    }

    @Test
    public void testUpdatePolicyNoId() throws Exception{
        Policy policy = new Policy();
        policy.name = "name";
        policy.details = mapper.readTree("{\"externalReference\":\"Test\"}");
        policy.typeId = 2L;
        JsonNode jsonNode = mapper.readTree(mapper.writeValueAsString(policy));
        try{
            policyValidator.validateRequest(jsonNode);
            fail("Json did not fail to validate. Should have thrown exception");
        }
        catch (InvalidSchemaException e){
            fail("Schema invalid");
        }
        catch (Exception e){
            System.out.print("Correct exception thrown \n" + e);
        }
    }

    @Test
    public void testUpdatePolicyNoName() throws Exception{
        Policy policy = new Policy();
        policy.id = 3L;
        policy.details = mapper.readTree("{\"externalReference\":\"Test\"}");
        policy.typeId = 2L;
        JsonNode jsonNode = mapper.readTree(mapper.writeValueAsString(policy));
        try{
            policyValidator.validateRequest(jsonNode);
            fail("Json did not fail to validate. Should have thrown exception");
        }
        catch (InvalidSchemaException e){
            fail("Schema invalid");
        }
        catch (Exception e){
            System.out.print("Correct exception thrown \n" + e);
        }
    }

    @Test
    public void testUpdatePolicyValid() throws Exception{
        Policy policy = new Policy();
        policy.id = 3L;
        policy.name = "name";
        policy.description = "des";
        policy.details = mapper.readTree("{\"externalReference\":\"Test\"}");
        policy.typeId = 2L;
        JsonNode jsonNode = mapper.readTree(mapper.writeValueAsString(policy));
        try{
            policyValidator.validateRequest(jsonNode);
        }
        catch (InvalidSchemaException e){
            fail("Schema invalid");
        }
        catch (Exception e){
            fail("Json failed to validate " + e);
        }
    }

    @Test
    public void testUpdateEmptyPolicyType() throws Exception{
        PolicyType policyType = new PolicyType();
        JsonNode jsonNode = mapper.readTree(mapper.writeValueAsString(policyType));
        try{
            policyValidator.validateRequest(jsonNode);
            fail("Json did not fail to validate. Should have thrown exception");
        }
        catch (InvalidSchemaException e){
            fail("Schema invalid");
        }
        catch (Exception e){
            System.out.print("Correct exception thrown \n" + e);
        }
    }

    @Test
    public void testUpdatePolicyTypeNoId() throws Exception{
        PolicyType policyType = new PolicyType();
        policyType.name="name";
        policyType.description = "des";
        policyType.shortName = "short_name";
        policyType.definition = mapper.readTree(policyTypeJson);
        JsonNode jsonNode = mapper.readTree(mapper.writeValueAsString(policyType));
        try{
            policyValidator.validateRequest(jsonNode);
            fail("Json did not fail to validate. Should have thrown exception");
        }
        catch (InvalidSchemaException e){
            fail("Schema invalid");
        }
        catch (Exception e){
            System.out.print("Correct exception thrown \n" + e);
        }
    }

    @Test
    public void testUpdatePolicyTypeValid() throws Exception{
        PolicyType policyType = new PolicyType();
        policyType.id = 5L;
        policyType.name="name";
        policyType.description = "des";
        policyType.shortName = "short_name";
        policyType.definition = mapper.readTree(policyTypeJson);
        JsonNode jsonNode = mapper.readTree(mapper.writeValueAsString(policyType));
        try{
            policyValidator.validateRequest(jsonNode);
        }
        catch (InvalidSchemaException e){
            fail("Schema invalid");
        }
        catch (Exception e){
            fail("Json failed to validate " + e);
        }
    }


    public static String policyTypeJson = "{\n" +
            "    \"title\": \"Metadata Policy Type\",\n" +
            "    \"description\": \"A metadata policy.\",\n" +
            "    \"type\": \"object\",\n" +
            "    \"properties\": {\n" +
            "        \"fieldActions\": {\n" +
            "            \"type\": \"array\",\n" +
            "            \"items\": {\n" +
            "                \"title\": \"Field Action\",\n" +
            "                \"type\": \"object\",\n" +
            "                \"properties\": {\n" +
            "                    \"name\": {\n" +
            "                        \"description\": \"The name of the field to perform the action on.\",\n" +
            "                        \"type\": \"string\",\n" +
            "                        \"minLength\": 1\n" +
            "                    },\n" +
            "                    \"action\": {\n" +
            "                        \"description\": \"The type of action to perform on the field.\",\n" +
            "                        \"type\": \"string\",\n" +
            "                        \"enum\": [\n" +
            "                            \"ADD_FIELD_VALUE\"\n" +
            "                        ]\n" +
            "                    },\n" +
            "                    \"value\": {\n" +
            "                        \"description\": \"The value to use for the field action.\",\n" +
            "                        \"type\": \"string\"\n" +
            "                    }\n" +
            "                },\n" +
            "                \"required\": [\"name\", \"action\"]\n" +
            "            }\n" +
            "        }\n" +
            "    }\n" +
            "}";
}