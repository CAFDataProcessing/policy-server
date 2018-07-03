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
import com.github.fge.jsonschema.core.exceptions.InvalidSchemaException;
import org.junit.Test;

import static org.junit.Assert.fail;

/**
 * Tests for the ClassifyDocumentSchemaValidator
 */
public class ClassifyDocumentSchemaValidatorTest extends ApiSchemaValidatorTestBase {
    final ApiSchemaValidator sut = new ClassifyDocumentApiSchemaValidator();

    @Test
    public void testValidClassifyRequest() throws Exception{
        JsonNode node = mapper.readTree(ClassifyDocumentValidRequestString);
        try{
            sut.validateRequest(node);
        }
        catch (InvalidSchemaException e){
            fail("Schema invalid");
        }
        catch (Exception e){
            fail("Json failed to validate " + e);
        }
    }

    @Test
    public void testInvalidClassifyRequest() throws Exception{
        //Missing collection sequence id
        JsonNode node = mapper.readTree(ClassifyDocumentMissingSequenceIdRequestString);
        try{
            sut.validateRequest(node);
            fail("Json did not fail to validate. Should have thrown exception");
        }
        catch (InvalidSchemaException e){
            fail("Schema invalid");
        }
        catch (Exception e){
            System.out.print("Correct exception thrown \n" + e);
        }
        node = mapper.readTree(ClassifyDocumentMissingJsonRequestString);
        try{
            sut.validateRequest(node);
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
    public void testValidClassifyResponse() throws Exception{
        JsonNode node = mapper.readTree(ClassifyDocumentValidResponseString);
        try{
            sut.validateResponse(node);
        }
        catch (InvalidSchemaException e){
            fail("Schema invalid");
        }
        catch (Exception e){
            fail("Json failed to validate " + e);
        }
    }

    @Test
    public void testInvalidClassifyResponse() throws Exception{
        JsonNode node = mapper.readTree(ClassifyDocumentInvalidResponseString);
        try{
            sut.validateResponse(node);
            fail("Json did not fail to validate. Should have thrown exception");
        }
        catch (InvalidSchemaException e){
            fail("Schema invalid");
        }
        catch (Exception e){
            System.out.print("Correct exception thrown \n" + e);
        }
    }

    static String ClassifyDocumentValidRequestString  = "{\"project_id:\":\"27ae9ece-ade1-4c89-a3db-83a854a8210a\"," +
            "\"collection_sequence\":2105,\"json\":{\"document\":[{\"afield\":\"1\",\"DREREFERENCE\":" +
            "\"81a2c47f-b31f-4da1-8871-f18843cfa510\",\"MissingNumberField\":\"123\",\"POLICY_KV_METADATA_PRESENT\":" +
            "\"true\",\"reference\":\"81a2c47f-b31f-4da1-8871-f18843cfa510\",\"document\":[]}]}}";

    static String ClassifyDocumentMissingSequenceIdRequestString = "{\"project_id\":\"27ae9ece-ade1-4c89-a3db-83a854a8210a\",\"collection_sequence\":null,\"json\":{\"document\":[{\"afield\":\"1\",\"DREREFERENCE\":\"81a2c47f-b31f-4da1-8871-f18843cfa510\",\"MissingNumberField\":\"123\",\"POLICY_KV_METADATA_PRESENT\":\"true\",\"reference\":\"81a2c47f-b31f-4da1-8871-f18843cfa510\",\"document\":[]}]}}";

    static String ClassifyDocumentMissingJsonRequestString = "{\"project_id\":\"27ae9ece-ade1-4c89-a3db-83a854a8210a\",\"collection_sequence\":2105,\"json\":{}}";

    static String ClassifyDocumentValidResponseString = "{\"result\":[{\"reference\":\"81a2c47f-b31f-4da1-8871-f18843cfa510\"" +
            ",\"unevaluated_conditions\":[],\"matched_collections\":[{\"id\":2576,\"name\":\"Collection 2\"," +
            "\"matched_conditions\":[{\"name\":\"afield condition 1\",\"type\":\"number\",\"reference\":" +
            "\"81a2c47f-b31f-4da1-8871-f18843cfa510\",\"terms\":[],\"id\":5487,\"matched_lexicon_expressions\":[]," +
            "\"field_name\":\"afield\"}],\"policies\":[{\"id\":756,\"name\":\"Policy\"}]},{\"id\":2577,\"name\":" +
            "\"Collection Incomplete missing field\",\"matched_conditions\":[{\"name\":" +
            "\"MissingNumberfield condition 1\",\"type\":\"number\",\"reference\":\"81a2c47f-b31f-4da1-8871-f18843cfa510\"," +
            "\"terms\":[],\"id\":5488,\"matched_lexicon_expressions\":[],\"field_name\":\"MissingNumberField\"}]," +
            "\"policies\":[]},{\"id\":2575,\"name\":\"Collection 1\",\"matched_conditions\":[{\"name\":\"afield condition 1\"," +
            "\"type\":\"number\",\"reference\":\"81a2c47f-b31f-4da1-8871-f18843cfa510\",\"terms\":[],\"id\":5486," +
            "\"matched_lexicon_expressions\":[],\"field_name\":\"afield\"}],\"policies\":[{\"id\":755,\"name\":\"Policy\"}]}]," +
            "\"collection_id_assigned_by_default\":null,\"incomplete_collections\":[],\"resolved_policies\":[755]}]}";

    static String ClassifyDocumentInvalidResponseString = "{\"result\":[{\"reference\":\"81a2c47f-b31f-4da1-8871-f18843cfa510\"" +
            ",\"unevaluated_conditions\":[],\"incomplete_collections\":[],\"resolved_policies\":[755]}]}";

}
