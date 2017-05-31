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
import java.util.stream.Collectors;

/**
 * Serializer for the EnvironmentSnapshot class
 */
public class EnvironmentSnapshotSerializer extends JsonSerializer<EnvironmentSnapshot> {
    @Override
    public void serialize(EnvironmentSnapshot value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
        jgen.writeStartObject();
        jgen.writeStringField("type", "collection_sequence");
        if(value.getCollectionSequenceId()!=null) {
            jgen.writeNumberField("id", value.getCollectionSequenceId());
        }
        else {
            jgen.writeNumberField("id",null);
        }
        jgen.writeObjectFieldStart("additional");
        if(value.getCollectionSequenceId()!=null) {
            jgen.writeNumberField("collection_sequence_id", value.getCollectionSequenceId());
        }
        else {
            jgen.writeNumberField("collection_sequence_id", null);
        }
        if(value.getInstanceId()!=null) {
            jgen.writeStringField("instance_id", value.getInstanceId());
        }
        else {
            jgen.writeStringField("instance_id", null);
        }
        if(value.getCreateDate()!=null) {
            jgen.writeObjectField("create_date", value.getCreateDate());
        }
        else {
            jgen.writeObjectField("create_date", null);
        }
        if(value.getCollectionSequenceLastModifiedDate()!=null) {
            jgen.writeObjectField("collection_sequence_lastmodified_date", value.getCollectionSequenceLastModifiedDate());
        }
        else {
            jgen.writeObjectField("collection_sequence_lastmodified_date", null);
        }
        if(value.getPersistedDate()!=null) {
            jgen.writeObjectField("persisted_date", value.getPersistedDate());
        }
        else {
            jgen.writeObjectField("persisted_date",null);
        }
        if(value.getFingerprint()!=null) {
            jgen.writeStringField("fingerprint", value.getFingerprint());
        }
        else {
            jgen.writeStringField("fingerprint", null);
        }
        if(value.getCollectionSequences()!=null) {
            jgen.writeObjectField("collection_sequences", value.getCollectionSequences().values());
        }
        else {
            jgen.writeStringField("fingerprint",null);
        }
        if(value.getCollections()!=null) {
            jgen.writeObjectField("collections", value.getCollections().values());
        }
        else {
            jgen.writeObjectField("collections", null);
        }
        if(value.getConditions()!=null) {
            jgen.writeObjectField("condition_fragments", value.getConditions().values().stream().filter(u -> u.isFragment).collect(Collectors.toList()));
        }
        else {
            jgen.writeObjectField("condition_fragments",null);
        }
        if(value.getFieldLabels()!=null) {
            jgen.writeObjectField("field_labels", value.getFieldLabels().values());
        }
        else {
            jgen.writeObjectField("field_labels",null);
        }
        if(value.getLexicons()!=null) {
            jgen.writeObjectField("lexicons", value.getLexicons().values());
        }
        else {
            jgen.writeObjectField("lexicons",null);
        }
        if(value.getPolicies()!=null) {
            jgen.writeObjectField("policies", value.getPolicies().values());
        }
        else {
            jgen.writeObjectField("policies",null);
        }
        if(value.getPolicyTypes()!=null) {
            jgen.writeObjectField("policy_types", value.getPolicyTypes().values());
        }
        else {
            jgen.writeObjectField("policy_types",null);
        }
        jgen.writeEndObject();
        jgen.writeEndObject();
    }
}
