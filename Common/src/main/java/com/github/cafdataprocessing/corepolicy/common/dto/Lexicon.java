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
package com.github.cafdataprocessing.corepolicy.common.dto;import com.fasterxml.jackson.annotation.JsonProperty;import com.fasterxml.jackson.databind.annotation.JsonDeserialize;import com.github.cafdataprocessing.corepolicy.common.ApiStrings;import com.github.cafdataprocessing.corepolicy.common.DtoDeserializer;import java.util.Collection;import java.util.LinkedHashSet;import java.util.Set;/** * */public class Lexicon extends DtoBase {    public String name;    public String description;    public String fingerprint;    @JsonProperty(ApiStrings.Lexicons.Arguments.LEXICON_EXPRESSIONS)    @JsonDeserialize(contentUsing = DtoDeserializer.class)    public Collection<LexiconExpression> lexiconExpressions;    private Set<LexiconExpression> getLexiconExpressions() {        if(lexiconExpressions == null){            return null;        }        return new LinkedHashSet<>(lexiconExpressions);    }    private void setLexiconExpressions(Set<LexiconExpression> lexiconExpressions) {        this.lexiconExpressions = lexiconExpressions;    }}