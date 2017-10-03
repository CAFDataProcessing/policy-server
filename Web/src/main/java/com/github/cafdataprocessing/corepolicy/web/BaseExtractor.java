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
package com.github.cafdataprocessing.corepolicy.web;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.github.cafdataprocessing.corepolicy.common.DtoDeserializer;
import com.github.cafdataprocessing.corepolicy.common.DtoSerializer;
import com.github.cafdataprocessing.corepolicy.common.dto.DtoBase;
import com.github.cafdataprocessing.corepolicy.common.shared.CorePolicyObjectMapper;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

/**
 * Base class to use for extractors
 */
public abstract class BaseExtractor<T> {
    protected static CorePolicyObjectMapper mapper;
    static {
        mapper = new CorePolicyObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(DtoBase.class, new DtoDeserializer());
        module.addSerializer(DtoBase.class, new DtoSerializer());
        mapper.registerModules(module,new JodaModule());
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);

    }

    public T extract(JsonNode jsonNode){
        return convert(convertTextNodesToJsonNode(jsonNode));
    }

    protected JsonNode convertTextNodesToJsonNode(JsonNode node) {
        ObjectNode newNode = mapper.createObjectNode();

        Iterator<Map.Entry<String, JsonNode>> fields = node.fields();

        while(fields.hasNext()){
            Map.Entry<String, JsonNode> nextNode = fields.next();

            if(nextNode.getValue() instanceof TextNode){
                try {
                    JsonNode jsonNode = mapper.readTree(nextNode.getValue().asText());
                    if(jsonNode instanceof ObjectNode) {
                        newNode.put(nextNode.getKey(), jsonNode);
                        continue;
                    }
                } catch (IOException e) {
//                    newNode.put(nextNode.getKey(), nextNode.getValue());
                }
            }
            newNode.put(nextNode.getKey(), nextNode.getValue());

        }
        return newNode;
    }

    abstract protected T convert(JsonNode node);


    public T extract(HttpServletRequest request){
        return extract((JsonNode) request.getAttribute("jsonParameters"));
    }
}
