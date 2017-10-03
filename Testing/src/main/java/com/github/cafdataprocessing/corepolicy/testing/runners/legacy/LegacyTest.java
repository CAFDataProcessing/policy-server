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
import com.github.cafdataprocessing.corepolicy.common.Document;

/**
 *
 */
public class LegacyTest {
    @JsonProperty("match_terms")
    public Boolean matchTerms;

    @JsonProperty("test_name")
    public Boolean test_name;

    @JsonProperty("test_description")
    public Boolean testDescription;

    @JsonProperty("input_document")
    public Document document;

    @JsonProperty("collection_sequence_name")
    public String collectionSequenceName;

    @JsonProperty("expected_result")
    public ExpectedResult expectedResult;
}
