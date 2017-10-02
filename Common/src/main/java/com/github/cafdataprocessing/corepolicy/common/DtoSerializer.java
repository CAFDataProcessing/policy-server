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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.github.cafdataprocessing.corepolicy.common.dto.*;
import com.github.cafdataprocessing.corepolicy.common.dto.conditions.*;
import com.github.cafdataprocessing.corepolicy.common.exceptions.ConditionEngineException;

import java.io.IOException;

/**
 * Serializer for base dto class
 */
public class DtoSerializer extends JsonSerializer<DtoBase>{
    final ObjectMapper mapper = new ObjectMapper();
    final SimpleModule module = new SimpleModule();

    @Override
    public void serialize(DtoBase dtoBase, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        module.addSerializer(DtoBase.class, new DtoSerializer());
        mapper.registerModule(module);
        if(dtoBase.getClass() == PolicyType.class){
            serialize((PolicyType)dtoBase,jgen,provider);
        }
        else if (dtoBase.getClass() == Policy.class){
            serialize((Policy)dtoBase,jgen,provider);
        }
        else if (dtoBase.getClass() == CollectionSequence.class){
            serialize((CollectionSequence)dtoBase,jgen,provider);
        }
        else if (dtoBase.getClass() == DocumentCollection.class){
            serialize((DocumentCollection)dtoBase,jgen,provider);
        }
        else if (dtoBase instanceof  Condition) {
            serialize((Condition)dtoBase,jgen,provider);
        }
        else if (dtoBase.getClass() == FieldLabel.class){
            serialize((FieldLabel)dtoBase,jgen,provider);
        }
        else if(dtoBase.getClass() == Lexicon.class){
            serialize((Lexicon)dtoBase,jgen,provider);
        }
        else if(dtoBase.getClass() == LexiconExpression.class){
            serialize((LexiconExpression)dtoBase,jgen,provider);
        }
        else if(dtoBase instanceof UnevaluatedCondition){
            serialize((UnevaluatedCondition) dtoBase,jgen,provider);
        }
        else if(dtoBase instanceof UnmatchedCondition){
            serialize((UnmatchedCondition)dtoBase,jgen,provider);
        }
        else if ( dtoBase.getClass() == SequenceWorkflow.class ) {
            serialize((SequenceWorkflow)dtoBase, jgen, provider);
        }
        else if ( dtoBase.getClass() == SequenceWorkflowEntry.class ) {
            serialize((SequenceWorkflowEntry)dtoBase, jgen, provider);
        }
        else{
            throw new ConditionEngineException(new RuntimeException("Unsupported Dto Type"+dtoBase.getClass().getTypeName()));
        }
    }

    public void serialize(LexiconExpression lexiconExpression, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        jgen.writeStartObject(); //Begin LexiconExpression
        jgen.writeStringField(ApiStrings.JsonProperties.TYPE, "lexicon_expression");
        if(lexiconExpression.id!=null)
            jgen.writeNumberField(ApiStrings.BaseCrud.Arguments.ID, lexiconExpression.id);
        else{
            jgen.writeNumberField(ApiStrings.BaseCrud.Arguments.ID, null);
        }
        jgen.writeFieldName(ApiStrings.JsonProperties.ADDITIONAL);
        {
            jgen.writeStartObject(); //Begin additional
            if(lexiconExpression.lexiconId!=null) {
                jgen.writeNumberField(ApiStrings.LexiconExpressions.Arguments.LEXICON_ID, lexiconExpression.lexiconId);
            }
            else{
                jgen.writeNumberField(ApiStrings.LexiconExpressions.Arguments.LEXICON_ID,null);
            }
            if(lexiconExpression.type!=null) {
                jgen.writeObjectField(ApiStrings.LexiconExpressions.Arguments.TYPE, lexiconExpression.type);
            }
            else{
                jgen.writeObjectField(ApiStrings.LexiconExpressions.Arguments.TYPE,null);
            }
            if(lexiconExpression.expression!=null) {
                jgen.writeStringField(ApiStrings.LexiconExpressions.Arguments.EXPRESSION, lexiconExpression.expression);
            }
            else{
                jgen.writeStringField(ApiStrings.LexiconExpressions.Arguments.EXPRESSION,null);
            }
            jgen.writeEndObject(); //End additional
        }
        jgen.writeEndObject(); //end LexiconExpression
    }

    public void serialize(Lexicon lexicon, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        jgen.writeStartObject(); //begin Lexicon
        jgen.writeStringField(ApiStrings.JsonProperties.TYPE, "lexicon");
        if(lexicon.id != null){
            jgen.writeNumberField(ApiStrings.BaseCrud.Arguments.ID, lexicon.id);
        }
        else {
            jgen.writeNumberField(ApiStrings.BaseCrud.Arguments.ID, null);
        }
        if(lexicon.name != null) {
            jgen.writeStringField(ApiStrings.Lexicons.Arguments.NAME, lexicon.name);
        } else {
            jgen.writeStringField(ApiStrings.Lexicons.Arguments.NAME,null);
        }
        if(lexicon.description!=null) {
            jgen.writeStringField(ApiStrings.Lexicons.Arguments.DESCRIPTION, lexicon.description);
        }
        else {
            jgen.writeStringField(ApiStrings.Lexicons.Arguments.DESCRIPTION,null);
        }
        jgen.writeFieldName(ApiStrings.JsonProperties.ADDITIONAL); //begin additional
        jgen.writeStartObject();
        if(lexicon.lexiconExpressions!=null){
            if(lexicon.lexiconExpressions.size() <= 0){
                jgen.writeFieldName(ApiStrings.Lexicons.Arguments.LEXICON_EXPRESSIONS);
                jgen.writeStartArray();
                jgen.writeEndArray();
            }
            else {
                jgen.writeFieldName(ApiStrings.Lexicons.Arguments.LEXICON_EXPRESSIONS);
                {
                    jgen.writeStartArray(); //begin expressions array
                    for (LexiconExpression it : lexicon.lexiconExpressions) {
                        jgen.writeObject(it);
                    }
                    jgen.writeEndArray(); //end expressions array
                }
            }
        }
        else {
           jgen.writeNullField(ApiStrings.Lexicons.Arguments.LEXICON_EXPRESSIONS);
        }
        jgen.writeEndObject(); //end additional
        jgen.writeEndObject(); //end Lexicon
    }

    public void serialize(PolicyType value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        jgen.writeStartObject(); //Begin PolicyType
        jgen.writeStringField(ApiStrings.JsonProperties.TYPE, "policy_type");
        if(value.id != null ) {
            jgen.writeNumberField(ApiStrings.BaseCrud.Arguments.ID, value.id);
        }
        else {
            jgen.writeNumberField(ApiStrings.BaseCrud.Arguments.ID,null);
        }
        if(value.name != null) {
            jgen.writeStringField(ApiStrings.PolicyType.Arguments.POLICY_TYPE_NAME, value.name);
        }
        else {
            jgen.writeStringField(ApiStrings.PolicyType.Arguments.POLICY_TYPE_NAME, null);
        }
        if(value.description != null) {
            jgen.writeStringField(ApiStrings.PolicyType.Arguments.POLICY_TYPE_DESC, value.description);
        }
        else {
            jgen.writeStringField(ApiStrings.PolicyType.Arguments.POLICY_TYPE_DESC, null);
        }
        jgen.writeFieldName(ApiStrings.JsonProperties.ADDITIONAL);
        {
            jgen.writeStartObject(); //Begin additional
            if(value.shortName != null) {
                jgen.writeStringField(ApiStrings.PolicyType.Arguments.POLICY_TYPE_INTERNAL_NAME, value.shortName);
            }
            else {
                jgen.writeStringField(ApiStrings.PolicyType.Arguments.POLICY_TYPE_INTERNAL_NAME, null);
            }
            if(value.definition!=null) {
                jgen.writeFieldName(ApiStrings.PolicyType.Arguments.POLICY_TYPE_DEFINITION);
                jgen.writeObject(value.definition);
            }
            else{
                jgen.writeStringField(ApiStrings.PolicyType.Arguments.POLICY_TYPE_DEFINITION, null);
            }
            if(value.conflictResolutionMode!=null){
                jgen.writeStringField(ApiStrings.PolicyType.Arguments.POLICY_CONFLICT_RESOLUTION_MODE, value.conflictResolutionMode.toValue());
            }
            else {
                jgen.writeStringField(ApiStrings.PolicyType.Arguments.POLICY_CONFLICT_RESOLUTION_MODE, null);
            }
            jgen.writeEndObject(); //end additional
        }
        jgen.writeEndObject(); //end PolicyType
    }

    public  void serialize(Policy value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        jgen.writeStartObject(); //Begin Policy
        jgen.writeStringField(ApiStrings.JsonProperties.TYPE, "policy");
        if(value.id != null) {
            jgen.writeNumberField(ApiStrings.BaseCrud.Arguments.ID, value.id);
        }
        else {
            jgen.writeNumberField(ApiStrings.BaseCrud.Arguments.ID,null);
        }
        if(value.name != null) {
            jgen.writeStringField(ApiStrings.Policy.Arguments.NAME, value.name);
        }
        else {
            jgen.writeStringField(ApiStrings.Policy.Arguments.NAME, null);
        }
        if(value.description != null) {
            jgen.writeStringField(ApiStrings.Policy.Arguments.DESCRIPTION, value.description);
        }
        else {
            jgen.writeStringField(ApiStrings.Policy.Arguments.DESCRIPTION, null);
        }
        jgen.writeFieldName(ApiStrings.JsonProperties.ADDITIONAL);
        {
            jgen.writeStartObject(); //begin additional
            if(value.priority != null) {
                jgen.writeNumberField(ApiStrings.Policy.Arguments.PRIORITY, value.priority);
            }
            else {
                jgen.writeNumberField(ApiStrings.Policy.Arguments.PRIORITY, null);
            }
            if(value.details != null) {
                jgen.writeFieldName(ApiStrings.Policy.Arguments.DETAILS);
                jgen.writeObject(value.details);
            }
            else {
                jgen.writeObjectField(ApiStrings.Policy.Arguments.DETAILS, null);
            }
            if(value.typeId!=null) {
                jgen.writeNumberField(ApiStrings.Policy.Arguments.POLICY_TYPE, value.typeId);
            }
            else {
                jgen.writeNumberField(ApiStrings.Policy.Arguments.POLICY_TYPE, null);
            }
            jgen.writeEndObject(); //end additional
        }
        jgen.writeEndObject(); //end Policy
    }

    public void serialize(FieldLabel value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        jgen.writeStartObject(); // Begin fieldLabel
        jgen.writeStringField(ApiStrings.JsonProperties.TYPE, "field_label");
        if(value.id != null) {
            jgen.writeNumberField(ApiStrings.BaseCrud.Arguments.ID, value.id);
        }
        else {
            jgen.writeNumberField(ApiStrings.BaseCrud.Arguments.ID, null);
        }
        if(value.name!=null) {
            jgen.writeStringField(ApiStrings.FieldLabels.Arguments.NAME, value.name);
        }
        else {
            jgen.writeStringField(ApiStrings.FieldLabels.Arguments.NAME,null);
        }
        jgen.writeFieldName(ApiStrings.JsonProperties.ADDITIONAL);
        {
            jgen.writeStartObject(); //begin additional
            if(value.fieldType!=null) {
                jgen.writeStringField(ApiStrings.FieldLabels.Arguments.FIELD_TYPE, value.fieldType.toValue());
            }
            else{
                jgen.writeStringField(ApiStrings.FieldLabels.Arguments.FIELD_TYPE, null);
            }
            if(value.fields!=null) {
                jgen.writeFieldName(ApiStrings.FieldLabels.Arguments.FIELDS);
                jgen.writeStartArray(); //begin fields array
                for (String it : value.fields) {
                    jgen.writeString(it);
                }
                jgen.writeEndArray(); //end fields array
            }
            else{
                jgen.writeNullField(ApiStrings.FieldLabels.Arguments.FIELDS);
            }
            jgen.writeEndObject(); //end additional
        }
        jgen.writeEndObject(); //end Fieldlabel
    }



    public void serialize(CollectionSequence value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        jgen.writeStartObject(); //Begin CollectionSequence
        jgen.writeStringField(ApiStrings.JsonProperties.TYPE, "collection_sequence");
        if(value.id!=null){
            jgen.writeNumberField(ApiStrings.BaseCrud.Arguments.ID,value.id);
        }
        else {
            jgen.writeNumberField(ApiStrings.BaseCrud.Arguments.ID, null);
        }
        if(value.name!=null) {
            jgen.writeStringField(ApiStrings.CollectionSequences.Arguments.NAME, value.name);
        }
        else {
            jgen.writeStringField(ApiStrings.CollectionSequences.Arguments.NAME, null);
        }
        if(value.description!=null) {
            jgen.writeStringField(ApiStrings.CollectionSequences.Arguments.DESCRIPTION, value.description);
        }
        else {
            jgen.writeStringField(ApiStrings.CollectionSequences.Arguments.DESCRIPTION, null);
        }
        jgen.writeFieldName(ApiStrings.JsonProperties.ADDITIONAL);
        {
            jgen.writeStartObject(); //begin additional
            if(value.collectionSequenceEntries!=null){
                jgen.writeFieldName(ApiStrings.CollectionSequences.Arguments.COLLECTION_SEQUENCE_ENTRIES);
                jgen.writeStartArray(); //begin entries array
                for (CollectionSequenceEntry it : value.collectionSequenceEntries) {
                    jgen.writeStartObject(); //begin entry
                    {
                        jgen.writeArrayFieldStart(ApiStrings.CollectionSequenceEntries.Arguments.COLLECTION_IDS); //begin entry's collection Id array
                        {
                            for (Long collectionIdIter : it.collectionIds) {
                                jgen.writeNumber(collectionIdIter);
                            }
                        }
                        jgen.writeEndArray(); //end collection Id array
                        if(it.order!=null) {
                            jgen.writeNumberField(ApiStrings.CollectionSequenceEntries.Arguments.ORDER, it.order);
                        }
                        else{
                            jgen.writeNumberField(ApiStrings.CollectionSequenceEntries.Arguments.ORDER,null);
                        }
                        jgen.writeBooleanField(ApiStrings.CollectionSequenceEntries.Arguments.STOP_ON_MATCH, it.stopOnMatch);
                    }
                    jgen.writeEndObject(); //end entry
                }
                jgen.writeEndArray(); //end entries array

            }
            else {
                jgen.writeObjectField(ApiStrings.CollectionSequences.Arguments.COLLECTION_SEQUENCE_ENTRIES,null);
            }
            if(value.defaultCollectionId!=null) {
                jgen.writeNumberField(ApiStrings.CollectionSequences.Arguments.DEFAULT_COLLECTION_ID, value.defaultCollectionId);
            }
            else {
                jgen.writeNumberField(ApiStrings.CollectionSequences.Arguments.DEFAULT_COLLECTION_ID,null);
            }
            if(value.collectionCount!=null) {
                jgen.writeNumberField(ApiStrings.CollectionSequences.Arguments.COLLECTION_COUNT, value.collectionCount);
            }
            else {
                jgen.writeNumberField(ApiStrings.CollectionSequences.Arguments.COLLECTION_COUNT, null);
            }
            if(value.lastModified!=null) {
                jgen.writeStringField(ApiStrings.CollectionSequences.Arguments.LAST_MODIFIED, value.lastModified.toString());
            }
            else {
                jgen.writeStringField(ApiStrings.CollectionSequences.Arguments.LAST_MODIFIED,null);
            }
            jgen.writeBooleanField(ApiStrings.CollectionSequences.Arguments.FULL_CONDITION_EVALUATION, value.fullConditionEvaluation);
            jgen.writeBooleanField(ApiStrings.CollectionSequences.Arguments.EVALUATION_ENABLED, value.evaluationEnabled);
            if(value.excludedDocumentFragmentConditionId!=null) {
                jgen.writeNumberField(ApiStrings.CollectionSequences.Arguments.EXCLUDED_DOCUMENT_CONDITION_ID, value.excludedDocumentFragmentConditionId);
            }
            else {
                jgen.writeNumberField(ApiStrings.CollectionSequences.Arguments.EXCLUDED_DOCUMENT_CONDITION_ID, null);
            }
            if(value.fingerprint!=null){
                jgen.writeStringField(ApiStrings.CollectionSequences.Arguments.FINGERPRINT, value.fingerprint);
            }
            else {
                jgen.writeStringField(ApiStrings.CollectionSequences.Arguments.FINGERPRINT, null);
            }
        }
        jgen.writeEndObject(); //end additional
        jgen.writeEndObject(); //end CollectionSequence
    }


    public void serialize(DocumentCollection value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        jgen.writeStartObject(); //Begin Collection
        jgen.writeStringField(ApiStrings.JsonProperties.TYPE, "collection");
        if(value.id!=null) {
            jgen.writeNumberField(ApiStrings.BaseCrud.Arguments.ID, value.id);
        }
        else {
            jgen.writeNumberField(ApiStrings.BaseCrud.Arguments.ID, null);
        }
        if(value.name!=null) {
            jgen.writeStringField(ApiStrings.DocumentCollections.Arguments.NAME, value.name);
        }
        else {
            jgen.writeStringField(ApiStrings.DocumentCollections.Arguments.NAME, null);
        }
        if(value.description!=null) {
            jgen.writeStringField(ApiStrings.DocumentCollections.Arguments.DESCRIPTION, value.description);
        }
        else {
            jgen.writeStringField(ApiStrings.DocumentCollections.Arguments.DESCRIPTION, null);
        }
        jgen.writeFieldName(ApiStrings.JsonProperties.ADDITIONAL);
        {
            jgen.writeStartObject(); //begin additional
            jgen.writeArrayFieldStart(ApiStrings.DocumentCollections.Arguments.POLICY_IDS); //begin policy ids array
            {
                if(value.policyIds!=null) {
                    for (Long it : value.policyIds) {
                        jgen.writeNumber(it);
                    }
                }
            }
            jgen.writeEndArray(); //end policy ids array
            jgen.writeObjectField(ApiStrings.DocumentCollections.Arguments.CONDITION, value.condition);
            jgen.writeEndObject();//end additional
        }
        jgen.writeEndObject();//end collection
    }

    public void serialize(Condition value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        jgen.writeStartObject(); //begin Condition
        jgen.writeStringField(ApiStrings.JsonProperties.TYPE, "condition");
        if(value.id!=null) {
            jgen.writeNumberField(ApiStrings.BaseCrud.Arguments.ID, value.id);
        }
        else {
            jgen.writeNumberField(ApiStrings.BaseCrud.Arguments.ID, null);
        }
        if(value.name!=null) {
            jgen.writeStringField(ApiStrings.Conditions.Arguments.NAME, value.name);
        }
        else {
            jgen.writeStringField(ApiStrings.Conditions.Arguments.NAME, null);
        }
        jgen.writeFieldName(ApiStrings.JsonProperties.ADDITIONAL);
        {
            jgen.writeStartObject(); //begin additional
            if(value.conditionType!=null) {
                jgen.writeStringField(ApiStrings.Conditions.Arguments.CONDITION_TYPE, value.conditionType.toValue());
            }
            else {
                jgen.writeStringField(ApiStrings.Conditions.Arguments.CONDITION_TYPE,null);
            }
            jgen.writeBooleanField(ApiStrings.Conditions.Arguments.IS_FRAGMENT, value.isFragment);
            if(value.order!=null) {
                jgen.writeNumberField(ApiStrings.Conditions.Arguments.ORDER, value.order);
            }
            else {
                jgen.writeNumberField(ApiStrings.Conditions.Arguments.ORDER,null);
            }
            if(value.notes!=null) {
                jgen.writeStringField(ApiStrings.Conditions.Arguments.NOTES, value.notes);
            }
            else {
                jgen.writeStringField(ApiStrings.Conditions.Arguments.NOTES,null);
            }
            jgen.writeBooleanField(ApiStrings.Conditions.Arguments.INCLUDE_DESCENDANTS, value.includeDescendants);
            if(value.parentConditionId!=null) {
                jgen.writeNumberField(ApiStrings.Conditions.Arguments.PARENT_CONDITION_ID, value.parentConditionId);
            }
            else {
                jgen.writeNumberField(ApiStrings.Conditions.Arguments.PARENT_CONDITION_ID, null);
            }
            if(value.target!=null) {
                jgen.writeObjectField(ApiStrings.Conditions.Arguments.TARGET, value.target);
            }
            else jgen.writeObjectField(ApiStrings.Conditions.Arguments.TARGET,null);
            switch (value.conditionType){ //check condition subtype to serialize type specific info
                case NUMBER:
                    if(((NumberCondition) value).operator!=null) {
                        jgen.writeStringField(ApiStrings.Conditions.Arguments.OPERATOR, ((NumberCondition) value).operator.toValue());
                    }
                    else {
                        jgen.writeStringField(ApiStrings.Conditions.Arguments.OPERATOR,null);
                    }
                    if(((NumberCondition) value).value!=null) {
                        jgen.writeStringField(ApiStrings.Conditions.Arguments.VALUE, ((NumberCondition) value).value.toString());
                    }
                    else {
                        jgen.writeNumberField(ApiStrings.Conditions.Arguments.VALUE, null);
                    }
                    if(((NumberCondition) value).field!=null) {
                        jgen.writeStringField(ApiStrings.Conditions.Arguments.FIELD, ((NumberCondition) value).field);
                    }
                    else {
                        jgen.writeStringField(ApiStrings.Conditions.Arguments.FIELD,null);
                    }
                    break;
                case NOT:
                    if(((NotCondition) value).condition!=null) {
                        jgen.writeObjectField(ApiStrings.Conditions.Arguments.CONDITION,((NotCondition) value).condition);
                    }
                    else {
                        jgen.writeObjectField(ApiStrings.Conditions.Arguments.CONDITION, null);
                    }

                    break;
                case LEXICON:
                    if(((LexiconCondition)value).value!=null) {
                        jgen.writeNumberField(ApiStrings.Conditions.Arguments.VALUE, ((LexiconCondition) value).value);
                    }
                    else {
                        jgen.writeNumberField(ApiStrings.Conditions.Arguments.VALUE,null);
                    }
                    if(((LexiconCondition) value).language!=null) {
                        jgen.writeStringField(ApiStrings.Conditions.Arguments.LANGUAGE, ((LexiconCondition) value).language);
                    }
                    else {
                        jgen.writeStringField(ApiStrings.Conditions.Arguments.LANGUAGE, null);
                    }
                    if(((LexiconCondition) value).field!=null) {
                        jgen.writeStringField(ApiStrings.Conditions.Arguments.FIELD, ((LexiconCondition) value).field);
                    }
                    else {
                        jgen.writeStringField(ApiStrings.Conditions.Arguments.FIELD,null);
                    }
                    break;
                case EXISTS:
                    if(((ExistsCondition)value).field!=null){
                        jgen.writeStringField(ApiStrings.Conditions.Arguments.FIELD,((ExistsCondition)value).field);
                    }
                    else {
                        jgen.writeStringField(ApiStrings.Conditions.Arguments.FIELD,null);
                    }
                    break;
                case DATE:
                    if(((DateCondition)value).value!=null) {
                        jgen.writeStringField(ApiStrings.Conditions.Arguments.VALUE, ((DateCondition) value).value);
                    }
                    else {
                        jgen.writeStringField(ApiStrings.Conditions.Arguments.VALUE,null);
                    }
                    if(((DateCondition) value).operator!=null) {
                        jgen.writeStringField(ApiStrings.Conditions.Arguments.OPERATOR, ((DateCondition) value).operator.toValue());
                    }
                    else {
                        jgen.writeStringField(ApiStrings.Conditions.Arguments.OPERATOR,null);
                    }
                    if(((DateCondition) value).field!=null) {
                        jgen.writeStringField(ApiStrings.Conditions.Arguments.FIELD, ((DateCondition) value).field);
                    }
                    else {
                        jgen.writeStringField(ApiStrings.Conditions.Arguments.FIELD,null);
                    }
                    break;
                case BOOLEAN:
                    if(((BooleanCondition) value).operator!=null) {
                        jgen.writeStringField(ApiStrings.Conditions.Arguments.OPERATOR, ((BooleanCondition) value).operator.toValue());
                    }
                    else jgen.writeStringField(ApiStrings.Conditions.Arguments.OPERATOR, null);
                    if(((BooleanCondition)value).children!=null) {
                        jgen.writeArrayFieldStart(ApiStrings.Conditions.Arguments.CHILDREN);
                        for (Condition it : ((BooleanCondition) value).children) {
                            jgen.writeObject(it);
                        }
                        jgen.writeEndArray();
                    }
                    else {
                        jgen.writeFieldName(ApiStrings.Conditions.Arguments.CHILDREN);
                        jgen.writeNull();
                    }
                    break;
                case TEXT:
                    if(((TextCondition)value).value!=null) {
                        jgen.writeStringField(ApiStrings.Conditions.Arguments.VALUE, ((TextCondition) value).value);
                    }
                    else {
                        jgen.writeStringField(ApiStrings.Conditions.Arguments.VALUE,null);
                    }
                    if(((TextCondition)value).language!=null) {
                        jgen.writeStringField(ApiStrings.Conditions.Arguments.LANGUAGE, ((TextCondition) value).language);
                    }
                    else {
                        jgen.writeStringField(ApiStrings.Conditions.Arguments.LANGUAGE,null);
                    }
                    if(((TextCondition) value).field!=null) {
                        jgen.writeStringField(ApiStrings.Conditions.Arguments.FIELD, ((TextCondition) value).field);
                    }
                    else {
                        jgen.writeStringField(ApiStrings.Conditions.Arguments.FIELD,null);
                    }
                    break;
                case REGEX:
                    if(((RegexCondition)value).value!=null) {
                        jgen.writeStringField(ApiStrings.Conditions.Arguments.VALUE, ((RegexCondition) value).value);
                    }
                    else {
                        jgen.writeStringField(ApiStrings.Conditions.Arguments.VALUE,null);
                    }
                    if(((RegexCondition) value).field!=null) {
                        jgen.writeStringField(ApiStrings.Conditions.Arguments.FIELD, ((RegexCondition) value).field);
                    }
                    else {
                        jgen.writeStringField(ApiStrings.Conditions.Arguments.FIELD,null);
                    }
                    break;
                case STRING:
                    if(((StringCondition)value).value!=null) {
                        jgen.writeStringField(ApiStrings.Conditions.Arguments.VALUE, ((StringCondition) value).value);
                    }
                    else {
                        jgen.writeStringField(ApiStrings.Conditions.Arguments.VALUE,null);
                    }
                    if(((StringCondition)value).operator!=null) {
                        jgen.writeStringField(ApiStrings.Conditions.Arguments.OPERATOR, ((StringCondition) value).operator.toValue());
                    }
                    else {
                        jgen.writeStringField(ApiStrings.Conditions.Arguments.OPERATOR,null);
                    }
                    if(((StringCondition) value).field!=null) {
                        jgen.writeStringField(ApiStrings.Conditions.Arguments.FIELD, ((StringCondition) value).field);
                    }
                    else{
                        jgen.writeStringField(ApiStrings.Conditions.Arguments.FIELD,null);
                    }
                    break;
                case FRAGMENT:
                    if(((FragmentCondition)value).value!=null) {
                        jgen.writeNumberField(ApiStrings.Conditions.Arguments.VALUE, ((FragmentCondition) value).value);
                    }
                    else {
                        jgen.writeNumberField(ApiStrings.Conditions.Arguments.VALUE,null);
                    }
                    break;
                default:
                    throw new ConditionEngineException(new RuntimeException("Unsupported condition type" + value.conditionType.toValue()));
            }
            jgen.writeEndObject(); //end additional
        }
        jgen.writeEndObject(); //end Condition
    }

    public void serialize(UnevaluatedCondition value,JsonGenerator jgen,SerializerProvider provider) throws IOException {
        jgen.writeStartObject();
        if(value.id!=null) {
            jgen.writeNumberField(ApiStrings.BaseCrud.Arguments.ID, value.id);
        }
        else{
            jgen.writeNumberField(ApiStrings.BaseCrud.Arguments.ID,null);
        }
        if(value.reason!=null) {
            jgen.writeStringField(ApiStrings.UnevaluatedConditions.Arguments.REASON, value.reason.toValue());
        }
        else{
            jgen.writeStringField(ApiStrings.UnevaluatedConditions.Arguments.REASON, null);
        }
        if(value.conditionType!=null) {
            jgen.writeStringField(ApiStrings.UnevaluatedConditions.Arguments.CONDITION_TYPE, value.conditionType.toValue());
        }
        else {
            jgen.writeStringField(ApiStrings.UnevaluatedConditions.Arguments.CONDITION_TYPE,null);
        }
        if(value.name!=null) {
            jgen.writeStringField(ApiStrings.UnevaluatedConditions.Arguments.NAME, value.name);
        }
        else{
            jgen.writeStringField(ApiStrings.UnevaluatedConditions.Arguments.NAME,null);
        }
        if(value.fingerprint!=null){
            jgen.writeStringField(ApiStrings.UnevaluatedConditions.Arguments.FINGERPRINT,value.fingerprint);
        }
        else{
            jgen.writeStringField(ApiStrings.UnevaluatedConditions.Arguments.FINGERPRINT,null);
        }
        jgen.writeEndObject();
    }

    public void serialize(UnmatchedCondition value,JsonGenerator jgen,SerializerProvider provider ) throws IOException {
        jgen.writeStartObject();
        if(value.id!=null) {
            jgen.writeNumberField(ApiStrings.BaseCrud.Arguments.ID, value.id);
        }
        else{
            jgen.writeNumberField(ApiStrings.BaseCrud.Arguments.ID,null);
        }
        if(value.getReference()!=null) {
            jgen.writeStringField(ApiStrings.UnmatchedConditions.Arguments.REFERENCE, value.getReference());
        }
        else{
            jgen.writeStringField(ApiStrings.UnmatchedConditions.Arguments.REFERENCE, null);
        }
        if(value.conditionType!=null) {
            jgen.writeStringField(ApiStrings.UnmatchedConditions.Arguments.CONDITION_TYPE, value.conditionType.toValue());
        }
        else {
            jgen.writeStringField(ApiStrings.UnmatchedConditions.Arguments.CONDITION_TYPE,null);
        }
        if(value.name!=null) {
            jgen.writeStringField(ApiStrings.UnmatchedConditions.Arguments.NAME, value.name);
        }
        else{
            jgen.writeStringField(ApiStrings.UnmatchedConditions.Arguments.NAME,null);
        }
        if(value.fingerprint!=null){
            jgen.writeStringField(ApiStrings.UnmatchedConditions.Arguments.FINGERPRINT,value.fingerprint);
        }
        else{
            jgen.writeStringField(ApiStrings.UnmatchedConditions.Arguments.FINGERPRINT,null);
        }
        jgen.writeEndObject();
    }

    public void serialize(SequenceWorkflow value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        jgen.writeStartObject(); //begin Condition
        jgen.writeStringField(ApiStrings.JsonProperties.TYPE, "sequence_workflow");
        if(value.id!=null) {
            jgen.writeNumberField(ApiStrings.BaseCrud.Arguments.ID, value.id);
        }
        else {
            jgen.writeNumberField(ApiStrings.BaseCrud.Arguments.ID, null);
        }
        if(value.name!=null) {
            jgen.writeStringField(ApiStrings.SequenceWorkflow.Arguments.NAME, value.name);
        }
        else {
            jgen.writeStringField(ApiStrings.SequenceWorkflow.Arguments.NAME, null);
        }
        if(value.description!=null) {
            jgen.writeStringField(ApiStrings.SequenceWorkflow.Arguments.DESCRIPTION, value.description);
        }
        else {
            jgen.writeStringField(ApiStrings.SequenceWorkflow.Arguments.DESCRIPTION, null);
        }
        jgen.writeFieldName(ApiStrings.JsonProperties.ADDITIONAL);
        {
            jgen.writeStartObject(); //begin additional

            jgen.writeArrayFieldStart(ApiStrings.SequenceWorkflow.Arguments.SEQUENCE_ENTRIES); //begin sequence ids array
            {
                if(value.sequenceWorkflowEntries!=null) {
                    for (SequenceWorkflowEntry it : value.sequenceWorkflowEntries) {
                        jgen.writeStartObject(); //begin entry
                        {
                            if (it.collectionSequenceId != null) {
                                jgen.writeNumberField(ApiStrings.SequenceWorkflowEntry.Arguments.SEQUENCE_ID, it.collectionSequenceId);
                            } else {
                                jgen.writeNumberField(ApiStrings.SequenceWorkflowEntry.Arguments.SEQUENCE_ID, null);
                            }
                            if (it.order != null) {
                                jgen.writeNumberField(ApiStrings.SequenceWorkflowEntry.Arguments.ORDER, it.order);
                            } else {
                                jgen.writeNumberField(ApiStrings.SequenceWorkflowEntry.Arguments.ORDER, null);
                            }
                            if (it.sequenceWorkflowId != null) {
                                jgen.writeNumberField(ApiStrings.SequenceWorkflowEntry.Arguments.SEQUENCE_WORKFLOW_ID, it.sequenceWorkflowId);
                            } else {
                                jgen.writeNullField(ApiStrings.SequenceWorkflowEntry.Arguments.SEQUENCE_WORKFLOW_ID);
                            }
                            if (it.collectionSequence != null){
                                jgen.writeObjectField(ApiStrings.SequenceWorkflowEntry.Arguments.SEQUENCE,it.collectionSequence);
                            } else {
                                jgen.writeNullField(ApiStrings.SequenceWorkflowEntry.Arguments.SEQUENCE);
                            }
                        }
                        jgen.writeEndObject(); //end entry
                    }
                }
            }
            jgen.writeEndArray(); //end sequence ids array

            if(value.notes!=null) {
                jgen.writeStringField(ApiStrings.Conditions.Arguments.NOTES, value.notes);
            }
            else {
                jgen.writeStringField(ApiStrings.Conditions.Arguments.NOTES, null);
            }

            jgen.writeEndObject(); //end additional
        }
        jgen.writeEndObject(); //end Condition
    }

    public void serialize(SequenceWorkflowEntry value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        jgen.writeStartObject(); //begin Condition
        jgen.writeStringField(ApiStrings.JsonProperties.TYPE, "sequence_workflow_entry");
        if(value.id!=null) {
            jgen.writeNumberField(ApiStrings.BaseCrud.Arguments.ID, value.id);
        }
        else {
            jgen.writeNumberField(ApiStrings.BaseCrud.Arguments.ID, null);
        }
        jgen.writeFieldName(ApiStrings.JsonProperties.ADDITIONAL);
        {
            jgen.writeStartObject(); //begin additional

            if (value.collectionSequenceId != null) {
                jgen.writeNumberField(ApiStrings.SequenceWorkflowEntry.Arguments.SEQUENCE_ID, value.collectionSequenceId);
            } else {
                jgen.writeNumberField(ApiStrings.SequenceWorkflowEntry.Arguments.SEQUENCE_ID, null);
            }
            if (value.order != null) {
                jgen.writeNumberField(ApiStrings.SequenceWorkflowEntry.Arguments.ORDER, value.order);
            } else {
                jgen.writeNumberField(ApiStrings.SequenceWorkflowEntry.Arguments.ORDER, null);
            }
            if(value.collectionSequence !=null){
                jgen.writeObjectField(ApiStrings.SequenceWorkflowEntry.Arguments.SEQUENCE,value.collectionSequence);
            } else {
                jgen.writeObjectField(ApiStrings.SequenceWorkflowEntry.Arguments.SEQUENCE,null);
            }
            if (value.sequenceWorkflowId != null) {
                jgen.writeNumberField(ApiStrings.SequenceWorkflowEntry.Arguments.SEQUENCE_WORKFLOW_ID, value.sequenceWorkflowId);
            } else {
                jgen.writeNumberField(ApiStrings.SequenceWorkflowEntry.Arguments.SEQUENCE_WORKFLOW_ID, null);
            }

            jgen.writeEndObject(); //end additional
        }
        jgen.writeEndObject(); //end Condition
    }

}
