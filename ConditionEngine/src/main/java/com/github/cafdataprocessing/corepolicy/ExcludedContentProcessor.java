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
package com.github.cafdataprocessing.corepolicy;

import com.github.cafdataprocessing.corepolicy.common.EnvironmentSnapshot;
import com.github.cafdataprocessing.corepolicy.common.dto.CollectionSequence;
import com.github.cafdataprocessing.corepolicy.common.dto.ExcludedFragment;
import com.github.cafdataprocessing.corepolicy.common.exceptions.CpeException;
import com.github.cafdataprocessing.corepolicy.document.DocumentUnderEvaluation;

import java.util.Collection;

/**
 * Defines the methods an ExcludedContentProcessor should implement.
 */
public interface ExcludedContentProcessor {
    /**
     * Removes content from the document or its sub documents when they match the excludingFragments
     * @param document the document to modify
     * @param excludedFragments the excludedFragments to run against the document
     */
    void removeExcludedFragments(DocumentUnderEvaluation document, Collection<ExcludedFragment> excludedFragments);

    /**
     * Marks the document and its sub-documents with the pre-approved property
     * @param document the document to be checked
     * @param conditionId the id of the excluded Document Condition
     * @param environmentSnapshot the cache of needed conditions
     */
    Collection<DocumentUnderEvaluation> setExcludedDocuments(CollectionSequence collectionSequence, DocumentUnderEvaluation document, Long conditionId, EnvironmentSnapshot environmentSnapshot) throws CpeException;
}
