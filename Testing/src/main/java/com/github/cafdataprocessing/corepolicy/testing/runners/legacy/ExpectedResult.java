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
package com.github.cafdataprocessing.corepolicy.testing.runners.legacy;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collection;

/**
 *
 */
public class ExpectedResult {
    @JsonProperty("reference")
    public String reference;

    @JsonProperty("matched_collections")
    public Collection<ExpectedMatchedCollection> matchedCollections;

    @JsonProperty("collection_id_assigned_by_default")
    public Long collectionIdAssignedByDefault;
}
