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
package com.github.cafdataprocessing.corepolicy;

import com.github.cafdataprocessing.corepolicy.common.exceptions.CpeException;
import com.github.cafdataprocessing.corepolicy.common.EnvironmentSnapshot;
import com.github.cafdataprocessing.corepolicy.common.dto.CollectionSequence;
import com.github.cafdataprocessing.corepolicy.common.dto.ExcludedFragment;
import com.github.cafdataprocessing.corepolicy.common.dto.conditions.Condition;
import com.github.cafdataprocessing.corepolicy.common.shared.ChangedValue;
import com.github.cafdataprocessing.corepolicy.common.shared.MetadataValue;
import com.github.cafdataprocessing.corepolicy.conditionEvaluators.ConditionEvaluationResult;
import com.github.cafdataprocessing.corepolicy.conditionEvaluators.EvaluateCondition;
import com.github.cafdataprocessing.corepolicy.document.DocumentUnderEvaluation;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Used to remove the excluded fragments from a document
 */
public class ExcludedContentProcessorImpl implements ExcludedContentProcessor {


    private EvaluateCondition evaluateCondition;

    @Autowired
    public ExcludedContentProcessorImpl(EvaluateCondition evaluateCondition){
        this.evaluateCondition = evaluateCondition;
    }

    /**
     * Sort the excluded fragments by order
     */
    private Comparator<ExcludedFragment> byOrder = (e1, e2) -> Integer.compare(
            e1.order, e2.order);

    @Override
    public void removeExcludedFragments(DocumentUnderEvaluation document, Collection<ExcludedFragment> excludedFragments) {
        for(ExcludedFragment excludedFragment : excludedFragments.stream().sorted(byOrder).collect(Collectors.toList())) {
            for (String fieldName : excludedFragment.fieldNames) {
                Collection<MetadataValue> fieldValues = document.getMetadata().get(fieldName);

                //Gets a list of the old value along with the new value
                List<ChangedValue<MetadataValue>> updatedFieldValues =
                        fieldValues.stream().map(value -> new ChangedValue<>(value, new MetadataValue(value.getApiProperties(), (String)value.getAsString().replaceAll(excludedFragment.pattern, "")))).collect(Collectors.toList());

                updatedFieldValues.forEach(u -> {
                    if (u.hasChanged()) {
                        //There is no replace method so we remove the old value and then add the new one
                        document.getMetadata().remove(fieldName, u.getOldValue());
                        document.getMetadata().put(fieldName, u.getNewValue());
                    }
                });
            }
        }

        if(document.getDocuments() != null) {
            document.getDocuments().forEach(u->removeExcludedFragments(u, excludedFragments));
        }
    }

    @Override
    public Collection<DocumentUnderEvaluation> setExcludedDocuments(CollectionSequence collectionSequence, DocumentUnderEvaluation document, Long excludedConditionId, EnvironmentSnapshot environmentSnapshot) throws CpeException {
        if(excludedConditionId == null){
            return Arrays.asList();
        }

        Condition condition = environmentSnapshot.getCondition(excludedConditionId);

        setExcludedDocuments(collectionSequence, document, condition, environmentSnapshot);
        Collection<DocumentUnderEvaluation> excludedDocuments = new LinkedList<>();
        populateExcludedDocuments(excludedDocuments, document);

        return excludedDocuments;
    }

    private void populateExcludedDocuments(Collection<DocumentUnderEvaluation> excludedDocuments, DocumentUnderEvaluation document) {
        if(document.getIsExcluded()){
            excludedDocuments.add(document);
        }
        for(DocumentUnderEvaluation childDocument: document.getDocuments()){
            populateExcludedDocuments(excludedDocuments, childDocument);
        }
    }

    private void setExcludedDocuments(CollectionSequence collectionSequence, DocumentUnderEvaluation document, Condition excludedCondition, EnvironmentSnapshot environmentSnapshot) throws CpeException {
        ConditionEvaluationResult conditionEvaluationResult = evaluateCondition.evaluate(collectionSequence, document, excludedCondition, environmentSnapshot);

        if(conditionEvaluationResult.isMatch()){
            conditionEvaluationResult.getMatchedConditions().stream().forEach(mc -> markDocumentAsExcluded(document, mc.getReference()));
        }
    }

    private void markDocumentAsExcluded(DocumentUnderEvaluation documentUnderEvaluation, String reference){
        if(documentUnderEvaluation.getReference()!=null && documentUnderEvaluation.getReference().equals(reference)){
            documentUnderEvaluation.setIsExcluded(true);
            return;
        }
        for(DocumentUnderEvaluation childDocumentUnderEvaluation:documentUnderEvaluation.getDocuments()){
            markDocumentAsExcluded(childDocumentUnderEvaluation,reference);
        }
    }
}
