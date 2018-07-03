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
package com.github.cafdataprocessing.corepolicy.domainModels;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;

/**
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BooleanAgentDocument {

    @JsonProperty("reference")
    private String reference;

    @JsonProperty("summary")
    private String summary;

    @JsonProperty("condition_id")
    private Collection<String> condition_id;

    @JsonProperty("lexicon_id")
    private Collection<String> lexicon_id;

    @JsonProperty("lexicon_expression_id")
    private Collection<String> lexicon_expression_id;

    @JsonProperty("booleanrestriction")
    private Collection<String> booleanRestriction;

    @JsonProperty("alwaysmatch")
    private Collection<String> alwaysMatch;

    @JsonProperty("content")
    private String content;

    @JsonProperty("links")
    private Collection<String> links;

    public String getReference(){
        return reference;
    }

    public String getSummary() {
        return summary;
    }

    public Collection<String> getCondition_id() {
        return condition_id;
    }

    public Collection<String> getLexicon_id() {
        return lexicon_id;
    }

    public Collection<String> getLexicon_expression_id() {
        return lexicon_expression_id;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public Collection<String> getBooleanRestriction() {
        return booleanRestriction;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Collection<String> getAlwaysMatch() {
        return alwaysMatch;
    }

    public void setBooleanRestriction(Collection<String> booleanRestriction) {
        this.booleanRestriction = booleanRestriction;
    }

    public void setCondition_id(Collection<String> condition_id) {
        this.condition_id = condition_id;
    }

    public void setLexicon_id(Collection<String> lexicon_id) {
        this.lexicon_id = lexicon_id;
    }

    public void setLexicon_expression_id(Collection<String> lexicon_expression_id) {
        this.lexicon_expression_id = lexicon_expression_id;
    }

    public void setAlwaysMatch(Collection<String> alwaysMatch) {
        this.alwaysMatch = alwaysMatch;
    }

    public Collection<String> getLinks() {
        return links;
    }

    public void setLinks(Collection<String> links) {
        this.links = links;
    }
}
