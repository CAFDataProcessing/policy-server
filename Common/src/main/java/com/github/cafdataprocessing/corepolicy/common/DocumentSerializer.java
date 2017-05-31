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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.Collection;

/**
 *
 */
public class DocumentSerializer extends JsonSerializer<Document> {
    @Override
    public void serialize(Document value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
        jgen.writeStartObject();
        if(value.getMetadata()!=null){
            for(String key:value.getMetadata().keySet()){
                if(key.equalsIgnoreCase(DocumentFields.Reference)){
                    jgen.writeFieldName(DocumentFields.Reference);
                    jgen.writeString(value.getReference());
                }
                else {
                    Collection<String> values = value.getMetadata().get(key);
                    if(values.size() == 1) {
                        jgen.writeFieldName(key);
                        jgen.writeString(values.iterator().next());
                    }
                    else {
                        jgen.writeArrayFieldStart(key);
                        for (String fieldValue : values) {
                            jgen.writeString(fieldValue);
                        }
                        jgen.writeEndArray();
                    }
                }
            }
        }
        if(value.getDocuments()!=null){
            jgen.writeArrayFieldStart("document");
            for(Document childDocument:value.getDocuments()){
                serialize(childDocument, jgen, provider);
            }
            jgen.writeEndArray();
        }
        jgen.writeEndObject();
    }
}
