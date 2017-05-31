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
package com.github.cafdataprocessing.corepolicy.common.shared;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchemaFactory;

/**
 * Custom validation logic for JSON
 */
public class JsonValidation {

    static public void validateJson(JsonNode json, JsonNode schemaDefinition) throws Exception {
        final JsonSchemaFactory factory = JsonSchemaFactory.byDefault();
        final JsonNode policyJson = json;
        final JsonNode policyTypeJson = schemaDefinition;

        final com.github.fge.jsonschema.main.JsonSchema schema =
                factory.getJsonSchema(policyTypeJson);

        ProcessingReport report = schema.validate(policyJson);
        if(!report.isSuccess()){
            throw new Exception(report.toString());
        }
    }
}
