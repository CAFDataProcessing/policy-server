/*
 * Copyright 2015-2017 EntIT Software LLC, a Micro Focus company.
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
package com.github.cafdataprocessing.corepolicy.common;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.cafdataprocessing.corepolicy.common.dto.conditions.*;

import java.io.IOException;

/**
 *
 */
public class ConditionDeserializer extends JsonDeserializer<Condition> {
    private final ObjectMapper om;

    public ConditionDeserializer(final ObjectMapper om) {
        this.om = om;
    }

    @Override
    public Condition deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        ObjectCodec oc = jsonParser.getCodec();
        JsonNode node = oc.readTree(jsonParser);

        String type = node.get("type").textValue();

        Class conditionClass;
        switch (type){
            case "number":
            {
                conditionClass = NumberCondition.class;
                break;
            }
            case "not":
            {
                conditionClass = NotCondition.class;
                break;
            }
            case "lexicon":
            {
                conditionClass = LexiconCondition.class;
                break;
            }
            case "exists":
            {
                conditionClass = ExistsCondition.class;
                break;
            }
            case "date":
            {
                conditionClass = DateCondition.class;
                break;
            }
            case "boolean":
            {
                conditionClass = BooleanCondition.class;
                break;
            }
            case "text":
            {
                conditionClass = TextCondition.class;
                break;
            }
            case "regex":
            {
                conditionClass = RegexCondition.class;
                break;
            }
            case "string":
            {
                conditionClass = StringCondition.class;
                break;
            }
            case "fragment":
            {
                conditionClass = FragmentCondition.class;
                break;
            }
            default:{
                throw new RuntimeException("Unsupported condition type " + type);
            }
        }
        return (Condition)om.readValue(node.toString(), conditionClass);
    }
}
