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
package com.github.cafdataprocessing.corepolicy.common.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.cafdataprocessing.corepolicy.common.ConditionEngineMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Defines the Classification result for a document.
 */
public class ClassifyDocumentResult {

    /**
     * Due to the fact that we are now json serializing some of our internal properties in ConditionEngineResult ( not ignoring them ) we
     * have split it off as an internal class - and collatedocument now returns this.
     * <p>
     * The result of a collate document, indicates collections that were matched (and the conditions that
     * caused that match) and any conditions that were unable to be evaluated because of missing metadata fields.
     * <p>
     * Used to convert our internal ConditionEngineResult object into one which is for public consumption, and doesn't contain
     * any internal properties.  This is a public interface type, and as such shouldnt change.
     */
    public String reference;
    @JsonProperty("unevaluated_conditions")
    public Collection<UnevaluatedCondition> unevaluatedConditions;

    @JsonProperty("matched_collections")
    public Collection<MatchedCollection> matchedCollections;

    @JsonProperty("collection_id_assigned_by_default")
    public Long collectionIdAssignedByDefault;

    @JsonProperty("incomplete_collections")
    public Collection<Long> incompleteCollections;

    @JsonProperty("resolved_policies")
    public Collection<Long> resolvedPolicies;

    @JsonIgnore
    public String signature;

    private final static Logger logger = LoggerFactory.getLogger(ClassifyDocumentResult.class);

    /**
     * This constructor is just used by the WebApiBase for constructing form a json response.
     */
    @JsonCreator
    private ClassifyDocumentResult(){

    }

    private ClassifyDocumentResult( ConditionEngineResult result ) {

        unevaluatedConditions = new ArrayList<>();
        matchedCollections = new ArrayList<>();
        incompleteCollections = new ArrayList<>();

        collectionIdAssignedByDefault = result.collectionIdAssignedByDefault;
        unevaluatedConditions.addAll( result.unevaluatedConditions );
        matchedCollections.addAll( result.matchedCollections );
        incompleteCollections.addAll(result.incompleteCollections);
        reference = result.reference;
    }

    public static ClassifyDocumentResult create(ConditionEngineResult result, ConditionEngineMetadata conditionEngineMetadata) {
        ClassifyDocumentResult classifyDocumentResult = new ClassifyDocumentResult(result);
        try {
            classifyDocumentResult.signature = conditionEngineMetadata.compressAndEncode(result);
        } catch (IOException e) {
            logger.warn("Unable to compress and encode ConditionEngineResult to signature, continuing without.", e);
            return new ClassifyDocumentResult(result);
        }

        return classifyDocumentResult;
    }


    /**
     * Return a friendly json serialized representation of the result. Applications should use an object mapper
     * directly.
     *
     * @return Indented json
     */
    @Override
    public String toString() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        try {
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            logger.error("Error serializing ClassifyDocumentResult exception: ", e);
            return "Error serializing ClassifyDocumentResult";
        }
    }

}
