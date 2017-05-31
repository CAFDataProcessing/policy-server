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
import org.joda.time.DateTime;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.fail;

/**
 * Tests for CreateUpdateResponseValidator
 */
public class CreateUpdateResponseValidatorTest extends ApiSchemaValidatorTestBase {
    //"collection_sequence", "collection", "condition", "lexicon", "lexicon_expression", "field_label
    final ApiSchemaValidator sut = new ClassificationUpdateApiSchemaValidator();

    @Test
    public void testValidCollectionSequenceResponse() throws Exception{
        CollectionSequence collectionSequence = new CollectionSequence();
        collectionSequence.id = 2L;
        collectionSequence.name = "name";
        collectionSequence.description = "response from Api create";
        collectionSequence.collectionSequenceEntries = new ArrayList<>();
        collectionSequence.collectionCount = 0;
        collectionSequence.fullConditionEvaluation = false;
        collectionSequence.lastModified = DateTime.now();
        JsonNode jsonNode = mapper.readTree(mapper.writeValueAsString(collectionSequence));
        try{
            sut.validateResponse(jsonNode);
        }
        catch (InvalidSchemaException e){
            fail("Schema invalid");
        }
        catch (Exception e){
            fail("Json failed to validate "+e);
        }
    }

    @Test
    public void testInvalidCollectionSequenceResponse() throws Exception{
        //Missing Id
        CollectionSequence collectionSequence = new CollectionSequence();
        collectionSequence.name = "name";
        collectionSequence.description = "response from Api create";
        collectionSequence.collectionSequenceEntries = new ArrayList<>();
        collectionSequence.collectionCount = 0;
        collectionSequence.fullConditionEvaluation = false;
        collectionSequence.lastModified = DateTime.now();
        JsonNode jsonNode = mapper.readTree(mapper.writeValueAsString(collectionSequence));
        try{
            sut.validateResponse(jsonNode);
            fail("Json did not fail to validate. Should have thrown exception");
        }
        catch (InvalidSchemaException e){
            fail("Schema invalid");
        }
        catch (Exception e){
            System.out.print("Correct exception thrown \n"+ e);
        }

        //Missing lastModified
        collectionSequence.id = 2L;
        collectionSequence.lastModified = null;
        jsonNode = mapper.readTree(mapper.writeValueAsString(collectionSequence));
        try{
            sut.validateResponse(jsonNode);
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
    public void testConditionValidResponse() throws Exception{
        NumberCondition numberCondition = new NumberCondition();
        numberCondition.id = 4L;
        numberCondition.operator = NumberOperatorType.EQ;
        numberCondition.field = "field";
        numberCondition.value = Long.MAX_VALUE;
        JsonNode jsonNode = mapper.readTree(mapper.writeValueAsString(numberCondition));
        try{
            sut.validateResponse(jsonNode);
        }
        catch (InvalidSchemaException e){
            fail("Schema invalid");
        }
        catch (Exception e){
            fail("Json failed to validate " + e);
        }
    }

    @Test
    public void testConditionInvalidResponse() throws Exception{
        NumberCondition numberCondition = new NumberCondition();
        numberCondition.operator = NumberOperatorType.EQ;
        numberCondition.field = "field";
        JsonNode jsonNode = mapper.readTree(mapper.writeValueAsString(numberCondition));
        try{
            sut.validateResponse(jsonNode);
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
    public void testNotConditionValidResponse() throws Exception{
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
            sut.validateResponse(jsonNode);
        }
        catch (InvalidSchemaException e){
            fail("Schema invalid");
        }
        catch (Exception e){
            fail("Json failed to validate " + e);
        }
    }

    @Test
    public void testBooleanConditionValidResponse() throws Exception {
        BooleanCondition booleanCondition = new BooleanCondition();
        booleanCondition.id = 4L;
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
        JsonNode jsonNode = mapper.readTree(mapper.writeValueAsString(booleanCondition));
        try{
            sut.validateResponse(jsonNode);
        }
        catch (InvalidSchemaException e){
            fail("Schema invalid");
        }
        catch (Exception e){
            fail("Json failed to validate " + e);
        }
    }

    @Test
    public void testCollectionValidResponseNoCondition() throws Exception {
        DocumentCollection collection = new DocumentCollection();
        collection.id = 2L;
        collection.name = "name";
        collection.description = "description Citroên";
        collection.policyIds = Collections.emptySet();
        JsonNode jsonNode = mapper.readTree(mapper.writeValueAsString(collection));
        try{
            sut.validateResponse(jsonNode);
        }
        catch (InvalidSchemaException e){
            fail("Schema invalid");
        }
        catch (Exception e){
            fail("Json failed to validate " + e);
        }
    }
    @Test
    public void testCollectionValidResponseValidCondition() throws Exception {
        DocumentCollection collection = new DocumentCollection();
        collection.id = 2L;
        collection.name = "name";
        collection.description = "description Citroên";
        collection.policyIds = new HashSet<>();
        Condition condition = new StringCondition();
        condition.id = 3L;
        condition.name = "Condition name";
        ((StringCondition)condition).operator = StringOperatorType.IS;
        ((StringCondition)condition).field = "field";
        ((StringCondition)condition).value = "test value";
        condition.isFragment = false;
        condition.order = 0;
        condition.notes = "notes";
        condition.target = ConditionTarget.CONTAINER;
        condition.includeDescendants = false;
        collection.condition = condition;
        JsonNode jsonNode = mapper.readTree(mapper.writeValueAsString(collection));
        try{
            sut.validateResponse(jsonNode);
        }
        catch (InvalidSchemaException e){
            fail("Schema invalid");
        }
        catch (Exception e){
            fail("Json failed to validate "+e);
        }
        //Test with policy Ids and minimun condition info
        collection.policyIds.add(721L);
        collection.policyIds.add(724L);
        condition = new ExistsCondition();
        ((ExistsCondition)condition).field = "field";
        collection.condition = condition;
        try{
            sut.validateResponse(jsonNode);
        }
        catch (InvalidSchemaException e){
            fail("Schema invalid");
        }
        catch (Exception e){
            fail("Json failed to validate "+e);
        }
    }

    @Test
    public void testCollectionValidResponseInvalidCondition() throws Exception{
        DocumentCollection collection = new DocumentCollection();
        collection.id = 2L;
        collection.name = "name";
        collection.description = "description Citroên";
        collection.policyIds = new HashSet<>();
        Condition condition = new StringCondition();
        condition.id = 3L;
        condition.name = "Condition name";
        collection.condition = condition;
        JsonNode jsonNode = mapper.readTree(mapper.writeValueAsString(collection));
        try{
            sut.validateResponse(jsonNode);
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
    public void testLexiconValidResponseNoExpression() throws Exception{
        Lexicon lexicon = new Lexicon();
        lexicon.id = 1502L;
        lexicon.name = "Lexicon with no entries";
        JsonNode jsonNode = mapper.readTree(mapper.writeValueAsString(lexicon));
        try{
            sut.validateResponse(jsonNode);
        }
        catch (InvalidSchemaException e){
            fail("Schema invalid");
        }
        catch (Exception e){
            fail("Json failed to validate " + e);
        }
    }

    @Test
    public void testLexiconInvalidResponseNoExpression() throws Exception{
        //Should fail without Id from response
        Lexicon lexicon = new Lexicon();
        lexicon.name = "Lexicon with no entries";
        JsonNode jsonNode = mapper.readTree(mapper.writeValueAsString(lexicon));
        try{
            sut.validateResponse(jsonNode);
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
    public void testLexiconValidResponseValidExpression() throws Exception{
        Lexicon lexicon = new Lexicon();
        lexicon.id  =4L;
        lexicon.name = "My lexicon Citroên";
        lexicon.description = "My description Citroên";
        lexicon.lexiconExpressions = new ArrayList<>();

        LexiconExpression lexiconExpression = new LexiconExpression();
        lexiconExpression.type = LexiconExpressionType.REGEX;
        lexiconExpression.expression = "ABD Citroên";
        lexiconExpression.lexiconId = lexicon.id;
        lexicon.lexiconExpressions = new LinkedList<>();
        lexicon.lexiconExpressions.add(lexiconExpression);
        JsonNode jsonNode = mapper.readTree(mapper.writeValueAsString(lexicon));
        try{
            sut.validateResponse(jsonNode);
        }
        catch (InvalidSchemaException e){
            fail("Schema invalid");
        }
        catch (Exception e){
            fail("Json failed to validate " + e);
        }
    }

    @Test
    public void testLexiconValidResponseInvalidExpression() throws Exception{
        Lexicon lexicon = new Lexicon();
        lexicon.name = "My lexicon Citroên";
        lexicon.description = "My description Citroên";
        lexicon.lexiconExpressions = new ArrayList<>();

        LexiconExpression lexiconExpression = new LexiconExpression();
        lexiconExpression.type = LexiconExpressionType.REGEX;
        JsonNode jsonNode = mapper.readTree(mapper.writeValueAsString(lexicon));
        try{
            sut.validateResponse(jsonNode);
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
    public void testFieldLabelValidResponse() throws Exception{
        FieldLabel fieldLabel = new FieldLabel();
        fieldLabel.id = 135L;
        fieldLabel.name = "name";
        fieldLabel.fieldType = FieldLabelType.DATE;
        fieldLabel.fields = Arrays.asList("Test1", "Test2", "Test3");
        JsonNode jsonNode = mapper.readTree(mapper.writeValueAsString(fieldLabel));
        try{
            sut.validateResponse(jsonNode);
        }
        catch (InvalidSchemaException e){
            fail("Schema invalid");
        }
        catch (Exception e){
            fail("Json failed to validate " + e);
        }
    }

    @Test
    public void testFieldLabelInvalidResponse() throws Exception{
        FieldLabel fieldLabel = new FieldLabel();
        fieldLabel.name = "name";
        fieldLabel.fieldType = FieldLabelType.DATE;
        fieldLabel.fields = Arrays.asList("Test1", "Test2", "Test3");
        //FieldLabel missing Id
        JsonNode jsonNode = mapper.readTree(mapper.writeValueAsString(fieldLabel));
        try{
            sut.validateResponse(jsonNode);
            fail("Json did not fail to validate. Should have thrown exception");
        }
        catch (InvalidSchemaException e){
            fail("Schema invalid");
        }
        catch (Exception e){
            System.out.print("Correct exception thrown \n" + e);
        }
    }

}
