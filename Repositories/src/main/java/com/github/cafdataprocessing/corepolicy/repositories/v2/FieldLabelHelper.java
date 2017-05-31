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
package com.github.cafdataprocessing.corepolicy.repositories.v2;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Helper methods for field labels
 */
public class FieldLabelHelper {

    public static String fieldsToJson(Collection<String> fields) {
        if(fields == null || fields.isEmpty()){
            throw new RuntimeException("Fields is required");
        }

        Fields fieldObject = new Fields(fields);

        ObjectMapper objectMapper = new ObjectMapper();

        try {
            return objectMapper.writeValueAsString(fieldObject);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static Collection<String> fieldsFromJson(String fields) {

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        try {
            return objectMapper.readValue(fields, Fields.class).fields;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static class Fields{
        public Fields(){}
        public Fields(Collection<String> fields){
            this.fields = fields.stream().collect(Collectors.toList());
        }

        public List<String> fields;
    }
}
