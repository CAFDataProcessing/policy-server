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
package com.github.cafdataprocessing.corepolicy.web.schema;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.cafdataprocessing.corepolicy.common.dto.*;
import com.github.cafdataprocessing.corepolicy.common.dto.conditions.*;
import com.github.fge.jsonschema.core.exceptions.InvalidSchemaException;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.fail;

public class CreateApiSchemaValidatorTest extends ApiSchemaValidatorTestBase {
    final ApiSchemaValidator sut = new ClassificationCreateApiSchemaValidator();
    final ApiSchemaValidator policyValidator = new PolicyCreateApiSchemaValidator();

    @Test
    public void testCreateEmptyCollectionSequence() throws Exception {
        CollectionSequence emptyCollection =  new CollectionSequence();
        JsonNode jsonData = mapper.readTree(mapper.writeValueAsString(emptyCollection));
        try {
            sut.validateRequest(jsonData);
            fail("Json did not fail to validate. Should have thrown exception");
        }
        catch (InvalidSchemaException e){
            fail("Schema invalid " + e);
        }
        catch (Exception et){
            System.out.print("Empty collection sequence test passed");
        }
    }

    @Test
    public void testCreateCollectionSequenceWithName() throws Exception{
        CollectionSequence collectionSequence =  new CollectionSequence();
        //Test Create with a collection sequence missing Id
        collectionSequence.name = "Hello";
        collectionSequence.description = "My description";
        JsonNode jsonData = mapper.readTree(mapper.writeValueAsString(collectionSequence));
        try{
            sut.validateRequest(jsonData);
        }
        catch (InvalidSchemaException e){
            fail("Schema invalid");
        }
        catch (Exception e){
            fail("Json failed to validate "+e);
        }
        //Test create with a collection sequence missing name. Should fail to validate
        collectionSequence.name = null;
        jsonData = mapper.readTree(mapper.writeValueAsString(collectionSequence));
        try{
            sut.validateRequest(jsonData);
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
    public void testCreateCollectionSequenceWithEntries() throws Exception{
        CollectionSequence collectionSequence =  new CollectionSequence();
        //CollectionSequence with valid entries
        collectionSequence.name = "Hello";
        collectionSequence.description = "My description";
        CollectionSequenceEntry entry = new CollectionSequenceEntry();
        entry.order = 100;
        collectionSequence.collectionSequenceEntries.add(entry);
        JsonNode jsonData = mapper.readTree(mapper.writeValueAsString(collectionSequence));
        try{
            sut.validateRequest(jsonData);
        }
        catch (InvalidSchemaException e){
            fail("Schema invalid");
        }
        catch (Exception e){
            fail("Json failed to validate "+e);
        }
        //CollectionSequence with invalid entries, should not validate.
        collectionSequence.collectionSequenceEntries.add(new CollectionSequenceEntry());
        jsonData = mapper.readTree(mapper.writeValueAsString(collectionSequence));
        try{
            sut.validateRequest(jsonData);
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
    public void testCreateEmptyCollection() throws Exception{
        DocumentCollection collection = new DocumentCollection();
        JsonNode jsonData = mapper.readTree(mapper.writeValueAsString(collection));
        try {
            sut.validateRequest(jsonData);
            fail("Json did not fail to validate. Should have thrown exception");
        }
        catch (InvalidSchemaException e){
            fail("Schema invalid " + e);
        }
        catch (Exception et){
            System.out.print("Empty collection sequence test passed");
        }
    }

    @Test
    public void  testCreateCollectionValidName() throws Exception {
        DocumentCollection collection = new DocumentCollection();
        //Test with valid name
        collection.name = "name";
        JsonNode jsonData = mapper.readTree(mapper.writeValueAsString(collection));
        try{
            sut.validateRequest(jsonData);
        }
        catch (InvalidSchemaException e){
            fail("Schema invalid");
        }
        catch (Exception e){
            fail("Json failed to validate " + e);
        }
    }

    @Test
    public void  testCreateCollectionValidCondition() throws Exception {
        DocumentCollection collection = new DocumentCollection();
        //Test with valid condition
        StringCondition condition = new StringCondition();
        condition.operator = StringOperatorType.IS;
        condition.name = "conditionName";
        condition.field = "test field";
        condition.value = "test value";
        collection.condition = condition;
        collection.name = "name";
        JsonNode jsonData = mapper.readTree(mapper.writeValueAsString(collection));
        try{
            sut.validateRequest(jsonData);
        }
        catch (InvalidSchemaException e){
            fail("Schema invalid");
        }
        catch (Exception e){
            fail("Json failed to validate "+e);
        }
    }

    @Test
    public void  testCreateCollectionInvalidCondition() throws Exception{
        DocumentCollection collection = new DocumentCollection();
        //Test with invalid condition, should fail to validate.
        collection.condition = new ExistsCondition();
        JsonNode jsonData = mapper.readTree(mapper.writeValueAsString(collection));
        try{
            sut.validateRequest(jsonData);
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
    public void testCreateEmptyCondition() throws Exception{
        NumberCondition condition = new NumberCondition();
        JsonNode jsonData = mapper.readTree(mapper.writeValueAsString(condition));
        try{
            sut.validateRequest(jsonData);
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
    public void testCreateValidNumberCondition() throws Exception {
        //Test valid number condition
        Condition condition = new NumberCondition();
        condition.name = "name";
        ((NumberCondition) condition).value = Long.valueOf(50);
        ((NumberCondition) condition).operator = NumberOperatorType.LT;
        ((NumberCondition) condition).field = "field";
        JsonNode jsonData = mapper.readTree(mapper.writeValueAsString(condition));
        try{
            sut.validateRequest(jsonData);
        }
        catch (InvalidSchemaException e){
            fail("Schema invalid");
        }
        catch (Exception e){
            fail("Json failed to validate \n" + e);
        }
    }

    @Test
    public void testCreateValidBooleanCondition() throws Exception{
        //Test valid boolean condition
        Condition condition = new BooleanCondition();
        ((BooleanCondition) condition).operator = BooleanOperator.AND;
        ((BooleanCondition) condition).name = "Bool1";
        ((BooleanCondition) condition).children = new ArrayList<>();

        ExistsCondition existsCondition = new ExistsCondition();
        existsCondition.field = "my field1";
        existsCondition.order = 100;
        ((BooleanCondition) condition).children.add(existsCondition);

        ExistsCondition existsCondition2 = new ExistsCondition();
        existsCondition2.field = "my field2";
        existsCondition2.order = 200;
        ((BooleanCondition) condition).children.add(existsCondition2);
        JsonNode jsonData = mapper.readTree(mapper.writeValueAsString(condition));
        try{
            sut.validateRequest(jsonData);
        }
        catch (InvalidSchemaException e){
            fail("Schema invalid");
        }
        catch (Exception e){
            fail("Json failed to validate \n"+e);
        }
    }

    @Test
    public void testCreateValidNotCondition() throws Exception{
        ExistsCondition existsCondition = new ExistsCondition();
        existsCondition.field = "Field1";
        existsCondition.name = "existsName";
        NotCondition notCondition = new NotCondition();
        notCondition.condition = existsCondition;
        notCondition.name = "conditionName";
        JsonNode jsonData = mapper.readTree(mapper.writeValueAsString(notCondition));
        try{
            sut.validateRequest(jsonData);
        }
        catch (InvalidSchemaException e){
            fail("Schema invalid");
        }
        catch (Exception e){
            fail("Json failed to validate \n"+e);
        }
    }

    @Test
    public void testCreateEmptyLexicon() throws Exception{
        Lexicon lexicon = new Lexicon();
        JsonNode jsonData = mapper.readTree(mapper.writeValueAsString(lexicon));
        try{
            sut.validateRequest(jsonData);
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
    public void testCreateValidLexiconNoExpression() throws Exception{
        Lexicon lexicon = new Lexicon();
        lexicon.name = "lex";
        JsonNode jsonData = mapper.readTree(mapper.writeValueAsString(lexicon));
        try{
            sut.validateRequest(jsonData);
        }
        catch (InvalidSchemaException e){
            fail("Schema invalid");
        }
        catch (Exception e){
            fail("Json failed to validate \n" + e);
        }
    }

    @Test
    public void testCreateValidLexiconValidExpression() throws Exception{
        Lexicon lexicon = new Lexicon();
        lexicon.name = "lex";
        lexicon.id = Long.valueOf(2);
        lexicon.lexiconExpressions = new ArrayList<>();
        LexiconExpression lexiconExpression = new LexiconExpression();
        lexiconExpression.expression = "expression";
        lexiconExpression.lexiconId = lexicon.id;
        lexiconExpression.type = LexiconExpressionType.TEXT;
        lexicon.lexiconExpressions.add(lexiconExpression);
        JsonNode jsonData = mapper.readTree(mapper.writeValueAsString(lexicon));
        try{
            sut.validateRequest(jsonData);
        }
        catch (InvalidSchemaException e){
            fail("Schema invalid");
        }
        catch (Exception e){
            fail("Json failed to validate \n" + e);
        }
    }

    @Test
    public void testCreateValidLexiconInvalidExpression() throws Exception{
        //Test valid lexicon with empty expression
        Lexicon lexicon = new Lexicon();
        lexicon.name = "name";
        lexicon.lexiconExpressions = new ArrayList<>();
        lexicon.lexiconExpressions.add(new LexiconExpression());
        JsonNode jsonData = mapper.readTree(mapper.writeValueAsString(lexicon));
        try{
            sut.validateRequest(jsonData);
            fail("Json did not fail to validate. Should have thrown exception");
        }
        catch (InvalidSchemaException e){
            fail("Schema invalid");
        }
        catch (Exception e){
            System.out.print("Correct exception thrown \n" + e);
        }
        //Now check with invalid, rather than empty expression
        lexicon.lexiconExpressions = new ArrayList<>();
        LexiconExpression lexiconExpression = new LexiconExpression();
        lexiconExpression.type = LexiconExpressionType.TEXT;
        lexiconExpression.expression = "expression"; //Expression missing lexicon Id
        try{
            sut.validateRequest(jsonData);
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
    public void testCreateEmptyPolicy() throws Exception {
        Policy policy = new Policy();
        JsonNode jsonData = mapper.readTree(mapper.writeValueAsString(policy));
        try{
            policyValidator.validateRequest(jsonData);
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
    public void testCreateValidPolicy() throws Exception {
        Policy policy = new Policy();
        policy.name = "name";
        policy.details = mapper.readTree("{\"externalReference\":\"Test\"}");
        policy.typeId = 2L;
        JsonNode jsonData = mapper.readTree(mapper.writeValueAsString(policy));
        try{
            policyValidator.validateRequest(jsonData);
        }
        catch (InvalidSchemaException e){
            fail("Schema invalid");
        }
        catch (Exception e){
            fail("Json failed to validate \n" + e);
        }

    }

    @Test
    public void testCreateEmptyPolicyType() throws Exception {
        PolicyType policyType = new PolicyType();
        JsonNode jsonData = mapper.readTree(mapper.writeValueAsString(policyType));
        try{
            policyValidator.validateRequest(jsonData);
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
    public void testCreateValidPolicyType() throws Exception{
        PolicyType policyType = new PolicyType();
        policyType.name = "name";
        policyType.shortName = "short_name";
        policyType.description = "Test policy type";
        policyType.definition = mapper.readTree(policyTypeJson);
        policyType.conflictResolutionMode = ConflictResolutionMode.PRIORITY;
        JsonNode jsonData = mapper.readTree(mapper.writeValueAsString(policyType));
        try{
            policyValidator.validateRequest(jsonData);
        }
        catch (InvalidSchemaException e){
            fail("Schema invalid");
        }
        catch (Exception e){
            fail("Json failed to validate \n" + e);
        }
    }

    @Test
    public void testCreateInvalidPolicyType() throws Exception {
        //Test PolicyType missing name, should fail.
        PolicyType policyType = new PolicyType();
        policyType.shortName = "short_name";
        policyType.description = "Test policy type";
        policyType.definition = mapper.readTree(policyTypeJson);
        policyType.conflictResolutionMode = ConflictResolutionMode.PRIORITY;
        JsonNode jsonData = mapper.readTree(mapper.writeValueAsString(policyType));
        try{
            policyValidator.validateRequest(jsonData);
            fail("Json did not fail to validate. Should have thrown exception");
        }
        catch (InvalidSchemaException e){
            fail("Schema invalid");
        }
        catch (Exception e){
            System.out.print("Correct exception thrown \n" + e);
        }
        //Test policy missing internal short name
        policyType = new PolicyType();
        policyType.name = "name";
        policyType.description = "missing short name";
        policyType.definition = mapper.readTree(policyTypeJson);
        policyType.conflictResolutionMode = ConflictResolutionMode.PRIORITY;
        jsonData = mapper.readTree(mapper.writeValueAsString(policyType));
        try{
            policyValidator.validateRequest(jsonData);
            fail("Json did not fail to validate. Should have thrown exception");
        }
        catch (InvalidSchemaException e){
            fail("Schema invalid");
        }
        catch (Exception e){
            System.out.print("Correct exception thrown \n" + e);
        }
        //Test policy missing definition
        policyType = new PolicyType();
        policyType.name = "name";
        policyType.shortName = "short_name";
        policyType.description = "missing short name";
        policyType.conflictResolutionMode = ConflictResolutionMode.PRIORITY;
        jsonData = mapper.readTree(mapper.writeValueAsString(policyType));
        try{
            policyValidator.validateRequest(jsonData);
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
    public void testCreateInvalidPolicy() throws Exception {
        //Test policy missing name
        Policy policy = new Policy();
        policy.typeId = 2L;
        policy.priority = 0;
        policy.details = mapper.readTree("{\"externalReference\":\"Test\"}");
        JsonNode jsonData = mapper.readTree(mapper.writeValueAsString(policy));
        try{
            policyValidator.validateRequest(jsonData);
            fail("Json did not fail to validate. Should have thrown exception");
        }
        catch (InvalidSchemaException e){
            fail("Schema invalid");
        }
        catch (Exception e){
            System.out.print("Correct exception thrown \n" + e);
        }
        //Test Policy missing details
        policy = new Policy();
        policy.name = "name";
        policy.priority = 0;
        policy.typeId = 2L;
        jsonData = mapper.readTree(mapper.writeValueAsString(policy));
        try{
            policyValidator.validateRequest(jsonData);
            fail("Json did not fail to validate. Should have thrown exception");
        }
        catch (InvalidSchemaException e){
            fail("Schema invalid");
        }
        catch (Exception e){
            System.out.print("Correct exception thrown \n" + e);
        }
        //Test policy missing typeId
        policy = new Policy();
        policy.name = "name";
        policy.priority = 0;
        policy.details = mapper.readTree("{\"externalReference\":\"Test\"}");
        jsonData = mapper.readTree(mapper.writeValueAsString(policy));
        try{
            policyValidator.validateRequest(jsonData);
            fail("Json did not fail to validate. Should have thrown exception");
        }
        catch (InvalidSchemaException e){
            fail("Schema invalid");
        }
        catch (Exception e){
            System.out.print("Correct exception thrown \n" + e);
        }
    }
//    @Test
//    public void testPolicy() throws Exception {
//        Policy policy = new Policy();
//
//    }

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