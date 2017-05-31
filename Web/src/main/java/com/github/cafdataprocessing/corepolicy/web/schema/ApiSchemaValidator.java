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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import org.apache.commons.io.IOUtils;

import java.io.InputStream;

/**
 *
 *
 */
public abstract class ApiSchemaValidator {
    private final String requestSchemaResource;
    private final String responseSchemaResource;
    private JsonSchema requestJsonSchema;
    private JsonSchema responseJsonSchema;
    final private JsonSchemaFactory jsonSchemaFactory = JsonSchemaFactory.byDefault();

    public ApiSchemaValidator(String requestSchemaResource, String responseSchemaResource){

        this.requestSchemaResource = requestSchemaResource;
        this.responseSchemaResource = responseSchemaResource;
    }

    public void validateRequest(String json) throws Exception{
        final ObjectMapper objectMapper = new ObjectMapper();
        final JsonNode jsonNode = objectMapper.reader().readTree(json);

        validateRequest(jsonNode);
    }

    public void validateRequest(JsonNode jsonNode) throws Exception{
        if(requestJsonSchema==null){
            try(InputStream requestStream = ClassLoader.class.getResourceAsStream(requestSchemaResource)) {
                String schemaJsonString = IOUtils.toString(requestStream, "UTF-8");
                final ObjectMapper objectMapper = new ObjectMapper();
                final JsonNode jsonSchemaNode = objectMapper.reader().readTree(schemaJsonString);

                requestJsonSchema = jsonSchemaFactory.getJsonSchema(jsonSchemaNode);
            }
        }
        validate(requestJsonSchema, jsonNode);
    }

    public void validateResponse(JsonNode jsonNode) throws Exception{
        if(responseJsonSchema==null){
            try (InputStream resourceStream = ClassLoader.class.getResourceAsStream(responseSchemaResource)) {
                String schemaJsonString = IOUtils.toString(resourceStream, "UTF-8");
                final ObjectMapper objectMapper = new ObjectMapper();
                final JsonNode jsonSchemaNode = objectMapper.reader().readTree(schemaJsonString);
                responseJsonSchema = jsonSchemaFactory.getJsonSchema(jsonSchemaNode);
            }
        }
        validate(responseJsonSchema, jsonNode);
    }

    static void validate(JsonSchema jsonSchema, JsonNode jsonNode) throws Exception{
        ProcessingReport report;
        report = jsonSchema.validate(jsonNode);
        if(!report.isSuccess()){
            throw new Exception(report.toString());
        }
    }
}
