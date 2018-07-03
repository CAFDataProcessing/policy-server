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

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Result object for a matched collection, holds the collection description and conditions that caused the match.
 */
public class MatchedCollection {
    private Long id;
    private String name;
    @JsonProperty("matched_conditions")
    private Collection<MatchedCondition> matchedConditions;

    @JsonProperty("policies")
    private Collection<CollectionPolicy> policies;

    // default constructor used for serialization of this object from json.
    public MatchedCollection(){
        this.policies = new ArrayList<>();
    }

    public MatchedCollection(DocumentCollection collection, Collection<MatchedCondition> matchedConditions){
        this();
        this.id = collection.id;
        this.name = collection.name;
        this.matchedConditions = matchedConditions;
        if(collection.policyIds!=null){
            collection.policyIds.forEach(p -> {
                CollectionPolicy collectionPolicy = new CollectionPolicy();
                collectionPolicy.setId(p);
                this.policies.add(collectionPolicy);
            });
        }
    }

    /**
     * The matched conditions defined for the collection.
     * @return Collection of matched conditions
     */
    public Collection<MatchedCondition> getMatchedConditions() {
        return matchedConditions;
    }

    /**
     * The id of the collection that the document matched.
     * @return
     */
    public Long getId() {
        return id;
    }

    /**
     * The id of the collection that the document matched.
     * @param id
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * The name of the collection that the document matched.
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * The id of the collection that the document matched.
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    public Collection<CollectionPolicy> getPolicies() {
        return policies;
    }

    public void setPolicies(Collection<CollectionPolicy> policies) {
        this.policies = policies;
    }
}
