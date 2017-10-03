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
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.type.CollectionType;

import java.io.IOException;
import java.util.Set;

/**
 * Custom deserializer for Set of any time
 */
public class SetDeserializer extends JsonDeserializer<Set<?>> implements ContextualDeserializer {
    private JavaType targetType;

    SetDeserializer(){}

    SetDeserializer(JavaType targetType)
    {
        this.targetType = targetType;
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) throws JsonMappingException {
        return new SetDeserializer(property.getType());
    }

    @Override
    public Set<?> deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        ObjectCodec oc = jp.getCodec();
        JsonNode node = jp.readValueAsTree();

        if(!(node instanceof ArrayNode)){
            ArrayNode arrayNode = (ArrayNode)oc.createArrayNode();
            arrayNode.add(node);

            node = arrayNode;
        }
        else if (targetType.containedType(0) instanceof CollectionType){
            //See if we can read the node as the inner type of the object
            boolean canRead = false;
            try {
                oc.readValue(oc.treeAsTokens(node), targetType.containedType(0));
                canRead = true;
            } catch (Exception ignored) {}

            //If we could read it should be an array of arrays
            if(canRead) {
                ArrayNode arrayNode = (ArrayNode)oc.createArrayNode();
                arrayNode.add(node);

                node = arrayNode;
            }
        }

        return oc.readValue(oc.treeAsTokens(node), targetType);
    }
}