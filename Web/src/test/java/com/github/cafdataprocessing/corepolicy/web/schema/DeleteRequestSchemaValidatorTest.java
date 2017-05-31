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
import com.github.fge.jsonschema.core.exceptions.InvalidSchemaException;
import org.junit.Test;

import static org.junit.Assert.fail;

/**
 * Tests for DeleteRequestSchemaValidator
 */
public class DeleteRequestSchemaValidatorTest extends ApiSchemaValidatorTestBase {
    final ApiSchemaValidator sut = new ClassificationDeleteApiSchemaValidator();
    final ApiSchemaValidator policyValidator = new PolicyDeleteApiSchemaValidator();

    @Test
    public void testDeleteValidClassification() throws Exception{
        DtoBase dto = new CollectionSequence();
        dto.id = 2L;
        JsonNode jsonNode = mapper.readTree(mapper.writeValueAsString(dto));
        try{
            sut.validateRequest(jsonNode);
        }
        catch (InvalidSchemaException e){
            fail("Schema invalid");
        }
        catch (Exception e){
            fail("Json failed to validate " + e);
        }
        dto = new DocumentCollection();
        dto.id = 2L;
        jsonNode = mapper.readTree(mapper.writeValueAsString(dto));
        try{
            sut.validateRequest(jsonNode);
        }
        catch (InvalidSchemaException e){
            fail("Schema invalid");
        }
        catch (Exception e){
            fail("Json failed to validate " + e);
        }
        dto = new Lexicon();
        dto.id = 2L;
        jsonNode = mapper.readTree(mapper.writeValueAsString(dto));
        try{
            sut.validateRequest(jsonNode);
        }
        catch (InvalidSchemaException e){
            fail("Schema invalid");
        }
        catch (Exception e){
            fail("Json failed to validate " + e);
        }
        dto = new LexiconExpression();
        dto.id = 2L;
        jsonNode = mapper.readTree(mapper.writeValueAsString(dto));
        try{
            sut.validateRequest(jsonNode);
        }
        catch (InvalidSchemaException e){
            fail("Schema invalid");
        }
        catch (Exception e){
            fail("Json failed to validate " + e);
        }
        dto = new FieldLabel();
        dto.id = 2L;
        jsonNode = mapper.readTree(mapper.writeValueAsString(dto));
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
    public void testDeleteInvalidClassification() throws Exception{
        DtoBase dto = new CollectionSequence();
        JsonNode jsonNode = mapper.readTree(mapper.writeValueAsString(dto));
        dto = new DocumentCollection();
        jsonNode = mapper.readTree(mapper.writeValueAsString(dto));
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
        dto = new Lexicon();
        jsonNode = mapper.readTree(mapper.writeValueAsString(dto));
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
        dto = new LexiconExpression();
        jsonNode = mapper.readTree(mapper.writeValueAsString(dto));
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
        dto = new FieldLabel();
        jsonNode = mapper.readTree(mapper.writeValueAsString(dto));
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
    public void testDeletePolicyValid() throws Exception{
        DtoBase dto = new Policy();
        dto.id = 2L;
        JsonNode jsonNode = mapper.readTree(mapper.writeValueAsString(dto));
        try{
            policyValidator.validateRequest(jsonNode);
        }
        catch (InvalidSchemaException e){
            fail("Schema invalid");
        }
        catch (Exception e){
            fail("Json failed to validate " + e);
        }
        dto = new PolicyType();
        dto.id = 3L;
        jsonNode = mapper.readTree(mapper.writeValueAsString(dto));
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
    public void testDeletePolicyInvalid() throws Exception{
        DtoBase dto = new Policy();
        JsonNode jsonNode = mapper.readTree(mapper.writeValueAsString(dto));
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
        dto = new PolicyType();
        jsonNode = mapper.readTree(mapper.writeValueAsString(dto));
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



}
