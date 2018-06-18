/*
 * Copyright 2015-2018 Micro Focus or one of its affiliates.
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
import com.github.cafdataprocessing.corepolicy.common.dto.*;
import com.github.cafdataprocessing.corepolicy.common.domainModels.EnvironmentSnapshotImpl;
import com.github.cafdataprocessing.corepolicy.common.dto.conditions.BooleanCondition;
import com.github.cafdataprocessing.corepolicy.common.dto.conditions.Condition;
import com.github.cafdataprocessing.corepolicy.common.dto.conditions.NotCondition;
import com.github.cafdataprocessing.corepolicy.common.shared.CorePolicyObjectMapper;
import org.joda.time.DateTime;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * Deserializer for the EnvironmentSnapshot class
 */
public class EnvironmentSnapshotDeserializer extends JsonDeserializer<EnvironmentSnapshot> {

    @Override
    public EnvironmentSnapshot deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        CorePolicyObjectMapper mapper = new CorePolicyObjectMapper();
        ObjectCodec oc = jp.getCodec();
        JsonNode root = oc.readTree(jp);
        EnvironmentSnapshotImpl value = new EnvironmentSnapshotImpl();
        Iterator<String> it = root.fieldNames();
        JsonNode node = root;
        while (it.hasNext()){
            String fieldName = it.next();
            node = node.get(fieldName);
            if (node != null) {
                switch (fieldName){
                    case "id":
                        value.setCollectionSequenceId(getNodeLongValue(node));
                        break;
                    case "type":
                        break;
                    case "additional":
                        value.setCollectionSequenceId(getNodeLongValue(node.get("collection_sequence_id")));
                        value.setInstanceId(getNodeTextValue(node.get("instance_id")));
                        value.setCreateDate(mapper.readValue(node.get("create_date").toString(), DateTime.class));
                        value.setPersistedDate(mapper.readValue(node.get("persisted_date").toString(), DateTime.class));
                        value.setCollectionSequenceLastModifiedDate(mapper.readValue(node.get("collection_sequence_lastmodified_date").toString(), DateTime.class));
                        value.fingerprint = node.get("fingerprint").textValue();

                        JsonNode hashmapNode = node.get("collection_sequences");
                        for(JsonNode currentNode:hashmapNode){
                            CollectionSequence temp = (CollectionSequence)mapper.readValue(currentNode.toString(), DtoBase.class);
                            value.collectionSequences.put(temp.id,temp);
                        }
                        hashmapNode = node.get("collections");
                        for(JsonNode curretnNode:hashmapNode){
                            DocumentCollection temp = (DocumentCollection)mapper.readValue(curretnNode.toString(),DtoBase.class);
                            value.collections.put(temp.id,temp);
                            if(temp.condition!=null){
                                addConditions(temp.condition, value);
                            }
                        }
                        hashmapNode = node.get("condition_fragments");
                        for(JsonNode currentNode:hashmapNode){
                            Condition temp = (Condition)mapper.readValue(currentNode.toString(),DtoBase.class);
                            addConditions(temp, value);
                        }
                        hashmapNode = node.get("field_labels");
                        for(JsonNode currentNode:hashmapNode){
                            FieldLabel temp = (FieldLabel)mapper.readValue(currentNode.toString(),DtoBase.class);
                            value.fieldLabels.put(temp.name,temp);
                        }
                        hashmapNode = node.get("lexicons");
                        for(JsonNode currentNode:hashmapNode){
                            Lexicon temp  = (Lexicon)mapper.readValue(currentNode.toString(),DtoBase.class);
                            value.lexicons.put(temp.id,temp);
                        }
                        hashmapNode = node.get("policies");
                        for(JsonNode currentNode:hashmapNode){
                            Policy temp  = (Policy)mapper.readValue(currentNode.toString(),DtoBase.class);
                            value.policies.put(temp.id, temp);
                        }
                        hashmapNode = node.get("policy_types");
                        for(JsonNode currentNode:hashmapNode){
                            PolicyType temp  = (PolicyType)mapper.readValue(currentNode.toString(),DtoBase.class);
                            value.policyTypes.put(temp.id, temp);
                        }
                        break;

                }
            }
            node = root;
        }

        return value;
    }

    private void addConditions(Condition condition, EnvironmentSnapshotImpl environmentSnapshot){
        if(condition==null){
            return;
        }
        environmentSnapshot.conditions.put(condition.id,condition);
        if(condition instanceof BooleanCondition) {
            List<Condition> children = ((BooleanCondition) condition).children;
            if(children != null) {
                for (Condition child: children) {
                    addConditions(child, environmentSnapshot);
                }
            }
        } else if(condition instanceof NotCondition) {
            Condition child = ((NotCondition) condition).condition;
            addConditions(child, environmentSnapshot);
        }
    }

    private Long getNodeLongValue(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        return node.asLong();
    }

    private String getNodeTextValue(JsonNode node) {
        if (node == null || node.isNull() || node.textValue().equalsIgnoreCase("null")||node.asText().isEmpty()) {
            return null;
        }
        return node.textValue();
    }
}
