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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.github.cafdataprocessing.corepolicy.common.DtoDeserializer;
import com.github.cafdataprocessing.corepolicy.common.dto.conditions.Condition;

import java.io.IOException;

/**
 * Custom deserializer for abstract class Condition
 */
public class AbstractConditionDeserializer extends JsonDeserializer<Condition> {
    @Override
    public Condition deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        DtoDeserializer dtoDeserializer = new DtoDeserializer();

        return (Condition)dtoDeserializer.deserialize(jp, ctxt);
    }
}
