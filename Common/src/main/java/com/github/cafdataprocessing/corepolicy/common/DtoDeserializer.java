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
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.github.cafdataprocessing.corepolicy.common.dto.*;
import com.github.cafdataprocessing.corepolicy.common.dto.conditions.*;
import com.github.cafdataprocessing.corepolicy.common.exceptions.MissingRequiredParameterCpeException;
import com.github.cafdataprocessing.corepolicy.common.exceptions.excpetionErrors.MissingRequiredParameterErrors;
import com.github.cafdataprocessing.corepolicy.common.exceptions.InvalidFieldValueCpeException;
import com.github.cafdataprocessing.corepolicy.common.exceptions.excpetionErrors.InvalidFieldValueErrors;

import java.io.IOException;
import java.util.Iterator;


/**
 * Deserialiser for the base dto class
 */
public class DtoDeserializer extends JsonDeserializer<DtoBase> {
    final SimpleModule module = new SimpleModule();
    final ObjectMapper mapper;

    public DtoDeserializer(){
        mapper = new ObjectMapper();
//        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.registerModules(module,new JodaModule());
    }

    @Override
    public DtoBase deserialize(JsonParser jp, DeserializationContext ctext) throws IOException, JsonProcessingException {
        ObjectCodec oc = jp.getCodec();
        JsonNode node = oc.readTree(jp);
        DtoBase value = new DtoBase();
        String type = node.path("type").asText();
        module.addDeserializer(DtoBase.class, new DtoDeserializer());
        mapper.registerModules(module, new JodaModule());
        //mapper.configure(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE,false);
        if(node.isMissingNode() || type.isEmpty()){
            throw new MissingRequiredParameterCpeException(MissingRequiredParameterErrors.TYPE_REQUIRED);
        }
        ItemType itemType = ItemType.forValue(type);
        switch (itemType){ //POLICY, POLICY_TYPE, COLLECTION_SEQUENCE, COLLECTION, CONDITION, FIELD_LABEL, LEXICON, LEXICON_EXPRESSION;
            case POLICY:
                node = removeAdditional(node);
                value = mapper.readValue(mapper.writeValueAsString(node),Policy.class);
                break;
            case POLICY_TYPE:
                node = removeAdditional(node);
                value = mapper.readValue(mapper.writeValueAsString(node), PolicyType.class);
                break;
            case COLLECTION_SEQUENCE:
                node = removeAdditional(node);
                value = mapper.readValue(mapper.writeValueAsString(node), CollectionSequence.class);
                break;
            case COLLECTION:
                node = removeAdditional(node);
                value = mapper.readValue(mapper.writeValueAsString(node), DocumentCollection.class);
                break;
            case CONDITION:
                value = getConditionClass(node);
                break;
            case FIELD_LABEL:
                node = removeAdditional(node);
                value = mapper.readValue(mapper.writeValueAsString(node), FieldLabel.class);
                break;
            case LEXICON:
                node = removeAdditional(node);
                value = mapper.readValue(mapper.writeValueAsString(node), Lexicon.class);
                break;
            case LEXICON_EXPRESSION:
                node = removeAdditional(node);
                value = mapper.readValue(mapper.writeValueAsString(node), LexiconExpression.class);
                break;
            case SEQUENCE_WORKFLOW:
                node = removeAdditional(node);
                value = mapper.readValue(mapper.writeValueAsString(node), SequenceWorkflow.class);
                break;
            case SEQUENCE_WORKFLOW_ENTRY:
                node = removeAdditional(node);
                node = checkAttachedCollectionSequence(node);
                value = mapper.readValue(mapper.writeValueAsString(node), SequenceWorkflowEntry.class);
                break;
            default:
                throw new InvalidFieldValueCpeException(InvalidFieldValueErrors.TYPE_IS_INVALID);
        }

        return value;
    }

    JsonNode removeAdditional(JsonNode root){
        ObjectNode returnNode = new ObjectNode(JsonNodeFactory.instance);
        JsonNode currentNode = root;
        Iterator<String> it = root.fieldNames();
        while (it.hasNext()){
            String fieldName = it.next();
            currentNode = currentNode.get(fieldName);
            if(fieldName.equalsIgnoreCase("additional")){
                Iterator<String> iter = currentNode.fieldNames();
                while (iter.hasNext()){
                    fieldName = iter.next();
                    returnNode.put(fieldName,currentNode.get(fieldName));
                }
            }
            else if(!fieldName.equalsIgnoreCase("project_id") && !fieldName.equalsIgnoreCase("type")) {
                returnNode.put(fieldName, currentNode);
            }
            currentNode = root;
        }
        return returnNode;
    }

    private Condition getConditionClass(JsonNode node) throws IOException {
        if(node !=null) {
            ConditionType conType = ConditionType.forValue(node.path("additional").path("type").textValue());
            switch (conType) {
                case NUMBER:
                    node = removeAdditional(node);
                    NumberCondition numberCondition = mapper.readValue(mapper.writeValueAsString(node),NumberCondition.class);
                    return numberCondition;
                case NOT:
                    node = removeAdditional(node);
                    NotCondition notCondition = mapper.readValue(mapper.writeValueAsString(node),NotCondition.class);
                    return notCondition;
                case LEXICON:
                    node = removeAdditional(node);
                    LexiconCondition lexicon = mapper.readValue(mapper.writeValueAsString(node),LexiconCondition.class);
                    return lexicon;
                case EXISTS:
                    node = removeAdditional(node);
                    ExistsCondition existsCondition = mapper.readValue(mapper.writeValueAsString(node),ExistsCondition.class);
                    return existsCondition;
                case DATE:
                    node = removeAdditional(node);
                    DateCondition dateCondition = mapper.readValue(mapper.writeValueAsString(node),DateCondition.class);
                    return dateCondition;
                case BOOLEAN:
                    node = removeAdditional(node);
                    BooleanCondition booleanCondition = mapper.readValue(mapper.writeValueAsString(node),BooleanCondition.class);
                    return booleanCondition;
                case TEXT:
                    node = removeAdditional(node);
                    TextCondition textCondition = mapper.readValue(mapper.writeValueAsString(node),TextCondition.class);
                    return textCondition;
                case REGEX:
                    node = removeAdditional(node);
                    RegexCondition regexCondition = mapper.readValue(mapper.writeValueAsString(node),RegexCondition.class);
                    return regexCondition;
                case STRING:
                    node = removeAdditional(node);
                    StringCondition stringCondition = mapper.readValue(mapper.writeValueAsString(node),StringCondition.class);
                    return stringCondition;
                case FRAGMENT:
                    node = removeAdditional(node);
                    FragmentCondition fragmentCondition = mapper.readValue(mapper.writeValueAsString(node),FragmentCondition.class);
                    return fragmentCondition;
            }
        }
        return null;
    }

    private JsonNode checkAttachedCollectionSequence(JsonNode node){
        JsonNode collectionSequenceNode =  node.get(ApiStrings.SequenceWorkflowEntry.Arguments.SEQUENCE);
        //if entry has attached collection sequence
        if(collectionSequenceNode!=null && !collectionSequenceNode.isNull()) {
            //remove additional and replace on entry
            collectionSequenceNode = removeAdditional(collectionSequenceNode);
            ((ObjectNode)node).replace(ApiStrings.SequenceWorkflowEntry.Arguments.SEQUENCE,collectionSequenceNode);
        }
        return node;
    }

}
