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
package com.github.cafdataprocessing.corepolicy.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.github.cafdataprocessing.corepolicy.common.dtoweb.RetrieveRequest;
import com.github.cafdataprocessing.corepolicy.common.exceptions.MissingRequiredParameterCpeException;
import com.github.cafdataprocessing.corepolicy.common.exceptions.excpetionErrors.MissingRequiredParameterErrors;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

/**
 * Creates a RetrieveRequest from a HttpServletRequest
 */
public class RetrieveRequestExtractor extends BaseExtractor<RetrieveRequest> {

    @Override
    protected RetrieveRequest convert(JsonNode node) {
        JsonNode typeNode = node.get("type");
        if(typeNode == null){
            throw new MissingRequiredParameterCpeException(MissingRequiredParameterErrors.TYPE_REQUIRED);
        }

        node = updateJsonNode(node);

        try {
            return mapper.treeToValue(node, RetrieveRequest.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    protected JsonNode updateJsonNode(JsonNode node) {
        ObjectNode newNode = mapper.createObjectNode();

        Iterator<Map.Entry<String, JsonNode>> fields = node.fields();

        while(fields.hasNext()){
            Map.Entry<String, JsonNode> nextNode = fields.next();

            if(nextNode.getKey().equalsIgnoreCase("additional") && nextNode.getValue() instanceof TextNode){
                try {
                    newNode.put(nextNode.getKey(), mapper.readTree(nextNode.getValue().asText()));
                } catch (IOException e) {
                    newNode.put(nextNode.getKey(), nextNode.getValue());
                }
            } else {
                newNode.put(nextNode.getKey(), nextNode.getValue());
            }
        }
        return newNode;
    }
}
