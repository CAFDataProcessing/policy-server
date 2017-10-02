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
package com.github.cafdataprocessing.corepolicy.common.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.cafdataprocessing.corepolicy.common.shared.CorePolicyObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.LinkedHashSet;

/**
 * The result of our internal evaluation calls in the Condition Engine.
 * Indicates collections that were matched (and the conditions that caused that match)
 * This is serialized to and from json, and as such can't contain ignored properties, to remain equal across this operation.
 *
 *
 * N.B. A decision was taken to serialize this information onto the documents as a compressed property.  As this information isn't
 * persisted and is transitory we can use this class directly to serialize it.
 * If for some reason we cause an upgrade to this class which makes it incompatible, and there are some docs left in a CFS queue somewhere, we fail
 * to recognise the information, just as if the HASH was invalid.
 */
public class ConditionEngineResult {

    public String reference;
    public Collection<UnevaluatedCondition> unevaluatedConditions;
    public Collection<MatchedCollection> matchedCollections;
    public Collection<UnmatchedCondition> unmatchedConditions;

    // Matched conditions may be here, and in matched collections, but not vice versa, as it will only get added to matchedCollections->matchedConditions
    // if the collection also is a match.   Therefore this is kept around to prevent re-evaluation even in non-matched collections.
    public Collection<MatchedCondition> matchedConditions;

    public Long collectionIdAssignedByDefault;
    public Collection<Long> incompleteCollections;

    private final static Logger logger = LoggerFactory.getLogger(ConditionEngineResult.class);

    public ConditionEngineResult(){

        // to ensure uniqueness while keeping order we are using a linkedhashset
        this.matchedConditions = new LinkedHashSet<>();
        this.unmatchedConditions = new LinkedHashSet<>();
        this.unevaluatedConditions = new LinkedHashSet<>();
        this.matchedCollections = new LinkedHashSet<>();
        this.incompleteCollections = new LinkedHashSet<>();
    }

    /**
     * Return an indented json serialized representation of the result.
     */
    @Override
    public String toString(){
        ObjectMapper mapper = new CorePolicyObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        try {
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            logger.error("Error serializing condition engine exception", e);
            return "Error serializing ConditionEngineResult";
        }
    }
}