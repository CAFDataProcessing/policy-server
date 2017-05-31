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
import com.github.fge.jsonschema.core.exceptions.InvalidSchemaException;
import org.junit.Test;

import static org.junit.Assert.fail;

/**
 * Tests for RetrieveSchemaValidator
 */
public class RetrieveSchemaValidatorTest extends ApiSchemaValidatorTestBase {
    final ApiSchemaValidator sut = new ClassificationRetrieveApiSchemaValidator();
    final ApiSchemaValidator policyValidator = new PolicyRetrieveApiSchemaValidator();

    @Test
    public void testClassificationRetrieveByName() throws Exception{
        JsonNode jsonNode = mapper.readTree(ClassificationRetrieveCollectionSequenceByNameValid);
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
    public void testClassificationRetrieveConditionById() throws Exception{
        JsonNode jsonNode = mapper.readTree(ClassificationRetrieveConditionByIdValid);
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
    public void testClassificationRetrieveById() throws Exception{
        JsonNode jsonNode = mapper.readTree(ClassificationRetrieveCollectionSequenceByIdValid);
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
    public void testClassificationRetrieveCollection() throws Exception{
        JsonNode jsonNode = mapper.readTree(ClassificationRetrieveCollectionValid);
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
    public void testClassificationRetrieveInvalid() throws Exception{
        JsonNode jsonNode = mapper.readTree(ClassificationRetrieveInvalidMissingType);
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
        jsonNode = mapper.readTree(ClassificationRetrieveInvalidMissingAdditional);
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
    public void testPolicyRetrieveById() throws Exception{
        JsonNode jsonNode = mapper.readTree(PolicyRetrieveByIdValid);
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
    public void testPolicyTypeRetrieveById() throws Exception{
        JsonNode jsonNode = mapper.readTree(PolicyTypeRetrieveByIdValid);
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
    public void testPolicyRetrieveByType() throws Exception{
        JsonNode jsonNode = mapper.readTree(PolicyRetrieveByTypeValid);
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
    public void testPolicyRetrieveInvalid() throws Exception{
        JsonNode jsonNode = mapper.readTree(PolicyRetrieveInvalidNoType);
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
    public void testClassificationResponseValid() throws Exception{
        JsonNode jsonNode = mapper.readTree(ClassificationResponseValid);
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
    public void testClassificationResponseInvalid() throws Exception{
        JsonNode jsonNode = mapper.readTree(ClassificiationResponseInvalidMissingTotalHits);
        try{
            sut.validateResponse(jsonNode);
            fail("Json did not fail to validate. Should have thrown exception");
        }
        catch (InvalidSchemaException e){
            fail("Schema invalid");
        }
        catch (Exception e) {
            System.out.print("Correct exception thrown \n" + e);
        }
        jsonNode = mapper.readTree(ClassificationResponseInvalid);
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
    public void testEmptyResponse() throws Exception{
        JsonNode jsonNode = mapper.readTree(EmptyResponse);
        try{
            sut.validateResponse(jsonNode);
        }
        catch (InvalidSchemaException e){
            fail("Schema invalid");
        }
        catch (Exception e){
            fail("Json failed to validate " + e);
        }
        try{
            policyValidator.validateResponse(jsonNode);
        }
        catch (InvalidSchemaException e){
            fail("Schema invalid");
        }
        catch (Exception e){
            fail("Json failed to validate " + e);
        }
    }

    @Test
    public void testPolicyResponseValid() throws Exception{
        JsonNode jsonNode = mapper.readTree(PolicyResponseValid);
        try{
            policyValidator.validateResponse(jsonNode);
        }
        catch (InvalidSchemaException e){
            fail("Schema invalid");
        }
        catch (Exception e){
            fail("Json failed to validate " + e);
        }
    }

    @Test
    public void testPolicyTypeResponseValid() throws Exception{
        JsonNode jsonNode = mapper.readTree(PolicyTypeResponeValid);
        try{
            policyValidator.validateResponse(jsonNode);
        }
        catch (InvalidSchemaException e){
            fail("Schema invalid");
        }
        catch (Exception e){
            fail("Json failed to validate " + e);
        }
    }

    @Test
    public void testPolicyResponseInvalid() throws Exception{
        JsonNode jsonNode = mapper.readTree(PolicyResponseInvalid);
        try{
            policyValidator.validateResponse(jsonNode);
            fail("Json did not fail to validate. Should have thrown exception");
        }
        catch (InvalidSchemaException e){
            fail("Schema invalid");
        }
        catch (Exception e) {
            System.out.print("Correct exception thrown \n" + e);
        }
    }

    static String ClassificationRetrieveCollectionSequenceByNameValid = "{\"project_id\":\"f7b9ac62-e7be-4010-b558-4a0f08c40378\"," +
            "\"type\":\"collection_sequence\",\"max_page_results\":6,\"start\":1,\"additional\":" +
            "{\"name\":\"Hello0bfe4b02-4764-4cba-b14b-6f2497f96741\",\"include_children\":null,\"include_condition\":null," +
            "\"datetime\":null}}";

    static String ClassificationRetrieveCollectionSequenceByIdValid = "{\"project_id\":\"f5b236e9-e719-4c20-88eb-9a4cc5e0c167\"," +
            "\"type\":\"collection_sequence\",\"max_page_results\":6,\"start\":1,\"id\":2108,\"additional\":{\"name\":null," +
            "\"include_children\":null,\"include_condition\":null,\"datetime\":null}}";

    static String ClassificationRetrieveConditionByIdValid = "{\"project_id\":\"ace849f3-cf71-4e29-860d-c87015a4f6e8\"," +
            "\"type\":\"condition\",\"max_page_results\":6,\"start\":1,\"id\":5493,\"additional\":" +
            "{\"name\":null,\"include_children\":false,\"include_condition\":null,\"datetime\":null}}";

    static String ClassificationRetrieveCollectionValid = "{\"project_id\":\"0ad9f141-5c75-4891-8f2e-9928f6cfd698\"," +
            "\"type\":\"collection\",\"max_page_results\":6,\"start\":1,\"additional\":{\"name\":null," +
            "\"include_children\":null,\"include_condition\":null,\"datetime\":null,\"filter\":{\"condition.id\":5495}}}";

    static String ClassificationRetrieveInvalidMissingType = "{\"project_id\":\"ace849f3-cf71-4e29-860d-c87015a4f6e8\"," +
            "\"max_page_results\":6,\"start\":1,\"id\":5493,\"additional\":{\"name\":null,\"include_children\":false," +
            "\"include_condition\":null,\"datetime\":null}}";

    static String ClassificationRetrieveInvalidMissingAdditional = "{\"project_id\":\"ace849f3-cf71-4e29-860d-c87015a4f6e8\"," +
            "\"type\":\"condition\",\"max_page_results\":6,\"start\":1,\"id\":5493}";

    static String PolicyRetrieveByIdValid = "{\"project_id\":\"a1f171f6-87cf-4460-a5c5-cd6f17c69418\",\"type\":\"policy\"," +
            "\"max_page_results\":6,\"start\":1,\"id\":757,\"additional\":" +
            "{\"name\":null,\"include_children\":null,\"include_condition\":null,\"datetime\":null}}";

    static String PolicyTypeRetrieveByIdValid = "{\"project_id\":\"77b77903-d620-488c-9f5c-3cf846338396\",\"id\":1,\"type\":\"policy_type\"}";

    static String PolicyRetrieveByTypeValid = "{\"project_id\":\"9d1bc401-5e03-4824-bc39-8dd46e9b7088\",\"type\":\"policy\"," +
            "\"max_page_results\":100,\"start\":1,\"additional\":{\"name\":null,\"include_children\":null," +
            "\"include_condition\":null,\"datetime\":null,\"filter\":{\"policy_type\":1226}}}";

    static String PolicyRetrieveInvalidNoType = "{\"project_id\":\"77b77903-d620-488c-9f5c-3cf846338396\",\"id\":1}";

    static String ClassificationResponseValid = "{\"results\":[{\"type\":\"collection_sequence\",\"id\":2109,\"name\":" +
            "\"Hello\",\"description\":\"My description\",\"additional\":" +
            "{\"collection_sequence_entries\":[],\"default_collection_id\":null,\"collection_count\":0,\"last_modified\":" +
            "\"2015-07-31T10:34:22.000+01:00\",\"full_condition_evaluation\":false," +
            "\"excluded_document_condition_id\":null,\"fingerprint\":null}}],\"totalhits\":1}";

    static String ClassificiationResponseInvalidMissingTotalHits = "{\"results\":[{\"type\":\"collection_sequence\"," +
            "\"id\":2109,\"name\":\"Hello\",\"description\":\"My description\",\"additional\":" +
            "{\"collection_sequence_entries\":[],\"default_collection_id\":null,\"collection_count\":0,\"last_modified\":" +
            "\"2015-07-31T10:34:22.000+01:00\",\"full_condition_evaluation\":false," +
            "\"excluded_document_condition_id\":null,\"fingerprint\":null}}]}";

    static String ClassificationResponseInvalid = "{\"results\":[{\"type\":\"collection_sequence\"," +
            "\"id\":2109,\"description\":\"My description\",\"additional\":" +
            "{\"collection_sequence_entries\":[],\"default_collection_id\":null,\"last_modified\":" +
            "\"2015-07-31T10:34:22.000+01:00\",\"full_condition_evaluation\":false," +
            "\"excluded_document_condition_id\":null,\"fingerprint\":null}}],\"totalhits\":1}";

    static String PolicyResponseValid = "{\"results\":[{\"type\":\"policy\",\"id\":769,\"name\":" +
            "\"updatedf29ca54b-52ff-4638-a6c1-3dc56a44961b\",\"description\":\"description\"," +
            "\"additional\":{\"priority\":0,\"details\":{\"title\":\"Test Metadata Policy\",\"description\":" +
            "\"Example Metadata Policy Details for unit tests\"},\"policy_type_id\":1}}]," +
            "\"totalhits\":1}";

    static String PolicyTypeResponeValid = "{\"results\":[{\"type\":\"policy_type\",\"id\":1,\"name\":\"Metadata\"," +
            "\"description\":\"Policies that determine how content should be indexed.\",\"additional\":{\"short_name\":" +
            "\"MetadataPolicy\",\"definition\":{\"title\":\"Metadata Policy Type\",\"description\":\"A metadata policy.\"," +
            "\"type\":\"object\",\"properties\":{" +
            "\"fieldActions\":{\"type\":\"array\",\"items\":{\"title\":\"Field Action\",\"type\":\"object\"," +
            "\"properties\":{\"name\":{\"description\":\"The name of the field to perform the action on.\",\"type\":\"string\"," +
            "\"minLength\":1},\"action\":{\"description\":\"The type of action to perform on the field.\",\"type\":\"string\"," +
            "\"enum\":[\"ADD_FIELD_VALUE\"]},\"value\":{\"description\":\"The value to use for the field action.\"," +
            "\"type\":\"string\"}},\"required\":[\"name\",\"action\"]}}}}," +
            "\"conflict_resolution_mode\":\"custom\"}}],\"totalhits\":1}";

    static String PolicyResponseInvalid = "{\"results\":[{\"type\":\"policy\",\"id\":769," +
            "\"description\":\"description\"," +
            "\"additional\":{\"priority\":0,\"details\":{\"title\":\"Test Metadata Policy\",\"description\":" +
            "\"Example Metadata Policy Details for unit tests\"},\"policy_type_id\":1}}]," +
            "\"totalhits\":1}";

    static String EmptyResponse = "{\"results\":[],\"totalhits\":0}";
}
