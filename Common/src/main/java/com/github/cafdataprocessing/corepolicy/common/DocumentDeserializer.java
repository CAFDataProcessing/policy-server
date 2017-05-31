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
package com.github.cafdataprocessing.corepolicy.common;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.io.IOException;
import java.util.Iterator;

/**
 *
 */
public class DocumentDeserializer extends JsonDeserializer<Document> {
    @Override
    public Document deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {

        ObjectCodec oc = jp.getCodec();
        JsonNode node = oc.readTree(jp);
        return deserialize(node);
    }

    Document deserialize(JsonNode node){
        DocumentImpl documentImpl = new DocumentImpl();
        Iterator<String> fieldNameIterator = node.fieldNames();
        while(fieldNameIterator.hasNext()){
            String fieldName = fieldNameIterator.next();
            JsonNode fieldNode = node.get(fieldName);
            if(fieldName.equalsIgnoreCase(DocumentFields.Reference)){
                if(fieldNode.isTextual()){
                    documentImpl.setReference(fieldNode.asText());
                }
            }
            else if(fieldName.equalsIgnoreCase("document")){
                if(fieldNode instanceof ArrayNode){
                    ArrayNode arrayNode = (ArrayNode)fieldNode;
                    Iterator<JsonNode> arrayNodeIterator = arrayNode.elements();
                    while(arrayNodeIterator.hasNext()){
                        JsonNode arrayNodeEntry = arrayNodeIterator.next();
                        if(arrayNodeEntry.isObject()){
                            documentImpl.getDocuments().add(deserialize(arrayNodeEntry));
                        }
                    }
                }
            }
            else {
                if(fieldNode.isValueNode()){
                    documentImpl.getMetadata().put(fieldName, fieldNode.asText());
                }
                else if(fieldNode instanceof ArrayNode){
                    ArrayNode arrayNode = (ArrayNode)fieldNode;
                    Iterator<JsonNode> arrayNodeIterator = arrayNode.elements();
                    while(arrayNodeIterator.hasNext()){
                        JsonNode arrayNodeEntry = arrayNodeIterator.next();
                        if(arrayNodeEntry.isValueNode()){
                            documentImpl.getMetadata().put(fieldName, arrayNodeEntry.asText());
                        }
                    }
                }
            }
        }

        return documentImpl;
    }
}
