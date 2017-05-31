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
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.github.cafdataprocessing.corepolicy.common.dto.DtoBase;
import com.github.cafdataprocessing.corepolicy.common.shared.CorePolicyObjectMapper;

import java.io.IOException;

/**
 * Custom deserializer for PageOfResults
 */
public class PageOfResultsDeserializer extends JsonDeserializer<Object> implements ContextualDeserializer {

    private JavaType targetType;

    PageOfResultsDeserializer(){}

    PageOfResultsDeserializer(JavaType targetType)
    {
        this.targetType = targetType;
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) throws JsonMappingException {
        return new PageOfResultsDeserializer(property.getType());
    }

    @Override
    public Object deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        ObjectCodec oc = jp.getCodec();
        JsonNode root = oc.readTree(jp);
        try {
            Class type = targetType.containedType(0).getRawClass();
            Object o;
            if(DtoBase.class.isAssignableFrom(type)){
                CorePolicyObjectMapper corePolicyObjectMapper = new CorePolicyObjectMapper();
                 o = corePolicyObjectMapper.readValue(oc.treeAsTokens(root), DtoBase.class);
            } else {
                o = oc.treeToValue(root, type);
            }

            return o;

//            return oc.readValue(oc.treeAsTokens(root), targetType.containedType(0));
        }
        catch(NullPointerException e){
            return null;
        }
    }
}


