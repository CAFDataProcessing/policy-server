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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.cafdataprocessing.corepolicy.common.ApiStrings;
import com.github.cafdataprocessing.corepolicy.common.FilterName;
import com.github.cafdataprocessing.corepolicy.common.SortName;
import org.joda.time.DateTime;

import java.util.LinkedList;
import java.util.List;

/**
 *
 */
public class CollectionSequence extends DtoBase {
    @FilterName(ApiStrings.CollectionSequences.Arguments.NAME)
    @SortName(ApiStrings.CollectionSequences.Arguments.NAME)
    @JsonProperty(ApiStrings.CollectionSequences.Arguments.NAME)
    public String name;

    @FilterName(ApiStrings.CollectionSequences.Arguments.DESCRIPTION)
    @SortName(ApiStrings.CollectionSequences.Arguments.DESCRIPTION)
    @JsonProperty(ApiStrings.CollectionSequences.Arguments.DESCRIPTION)
    public String description;

    @JsonProperty(ApiStrings.CollectionSequences.Arguments.DEFAULT_COLLECTION_ID)
    public Long defaultCollectionId;

    @JsonProperty(ApiStrings.CollectionSequences.Arguments.EXCLUDED_DOCUMENT_CONDITION_ID)
    public Long excludedDocumentFragmentConditionId;

    @JsonProperty(ApiStrings.CollectionSequences.Arguments.FULL_CONDITION_EVALUATION)
    public Boolean fullConditionEvaluation = false;

    @FilterName(ApiStrings.CollectionSequences.Arguments.COLLECTION_SEQUENCE_ENTRIES)
    //Mapping is not currently described so cannot be used to sort.
//    @SortName(ApiStrings.CollectionSequences.Arguments.COLLECTION_SEQUENCE_ENTRIES)
    @JsonProperty(ApiStrings.CollectionSequences.Arguments.COLLECTION_SEQUENCE_ENTRIES)
    public List<CollectionSequenceEntry> collectionSequenceEntries = new LinkedList<>();
//
//    private List<CollectionSequenceEntry> getCollectionSequenceEntries() {
//        if(collectionSequenceEntries==null) {
//            return null;
//        }
//        return new LinkedList<>(collectionSequenceEntries);
//    }
//
//    private void setCollectionSequenceEntries(List<CollectionSequenceEntry> collectionSequenceEntries) {
//        this.collectionSequenceEntries = collectionSequenceEntries;
//    }

    @JsonProperty(ApiStrings.CollectionSequences.Arguments.COLLECTION_COUNT)
    public Integer collectionCount;

    @JsonProperty(ApiStrings.CollectionSequences.Arguments.LAST_MODIFIED)
    public DateTime lastModified;

    @FilterName(ApiStrings.CollectionSequences.Arguments.EVALUATION_ENABLED)
    @JsonProperty(ApiStrings.CollectionSequences.Arguments.EVALUATION_ENABLED)
    public boolean evaluationEnabled = true; //CAF-544 says the default should be true

    public String fingerprint;
}
