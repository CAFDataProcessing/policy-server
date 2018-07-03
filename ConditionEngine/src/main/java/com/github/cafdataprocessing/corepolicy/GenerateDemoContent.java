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
package com.github.cafdataprocessing.corepolicy;

import com.github.cafdataprocessing.corepolicy.common.ClassificationApi;
import com.github.cafdataprocessing.corepolicy.common.PolicyApi;
import com.github.cafdataprocessing.corepolicy.common.dto.CollectionSequence;
import com.github.cafdataprocessing.corepolicy.common.dto.CollectionSequenceEntry;
import com.github.cafdataprocessing.corepolicy.common.dto.DocumentCollection;
import com.github.cafdataprocessing.corepolicy.common.dto.conditions.ExistsCondition;
import com.github.cafdataprocessing.corepolicy.common.exceptions.CpeException;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.HashSet;

/**
 *
 */
public class GenerateDemoContent {

    ClassificationApi classificationApi;
    PolicyApi policyApi;

    @Autowired
    public GenerateDemoContent(ClassificationApi classificationApi, PolicyApi policyApi) {
        this.classificationApi = classificationApi;
        this.policyApi = policyApi;
    }

    public Long createSimpleConditionSequence() throws CpeException {
        CollectionSequence simpleSequence = new CollectionSequence();
        simpleSequence.name = "Simple sequence";
        simpleSequence.description = "Contains a single Entry, with a single collection.";

        DocumentCollection simpleCollection = new DocumentCollection();
        simpleCollection.name = "Simple Collection";
        simpleCollection.description = "This collection has a single condition that will check for a field called \"content\"";

        ExistsCondition simpleCondition = new ExistsCondition();
        simpleCondition.name = "Content is present";
        simpleCondition.field = "content";

        simpleCollection.condition = simpleCondition;

        simpleCollection = classificationApi.create(simpleCollection);

        CollectionSequenceEntry simpleEntry = new CollectionSequenceEntry();
        simpleEntry.order = (short) 100;
        simpleEntry.stopOnMatch = false;
        simpleEntry.collectionIds = new HashSet<>(Arrays.asList(simpleCollection.id));

        simpleSequence.collectionSequenceEntries = Arrays.asList(simpleEntry);

        simpleSequence = classificationApi.create(simpleSequence);

        return simpleSequence.id;
    }
}
