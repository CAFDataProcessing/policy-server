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
package com.github.cafdataprocessing.corepolicy.conditionEvaluators;

import com.github.cafdataprocessing.corepolicy.document.DocumentUnderEvaluation;
import com.google.common.base.Strings;
import com.github.cafdataprocessing.corepolicy.common.ApiProperties;
import com.github.cafdataprocessing.corepolicy.common.EnvironmentSnapshot;
import com.github.cafdataprocessing.corepolicy.common.dto.CollectionSequence;
import com.github.cafdataprocessing.corepolicy.common.dto.FieldLabel;
import com.github.cafdataprocessing.corepolicy.common.dto.UnevaluatedCondition;
import com.github.cafdataprocessing.corepolicy.common.dto.conditions.Condition;
import com.github.cafdataprocessing.corepolicy.common.dto.conditions.FieldCondition;
import com.github.cafdataprocessing.corepolicy.common.exceptions.CpeException;

/**
 *
 */
abstract class FieldConditionEvaluator<T extends FieldCondition> extends ConditionEvaluatorBase<T> {
    protected final ApiProperties apiProperties;

    public FieldConditionEvaluator(ApiProperties apiProperties) {
        this.apiProperties = apiProperties;
    }

    protected FieldStatus fieldMissing(DocumentUnderEvaluation document, T condition, EnvironmentSnapshot environmentSnapshot) throws CpeException {
        if(Strings.isNullOrEmpty(condition.field)){
            throw new IllegalArgumentException("Condition must have a field.");
        }

        //CheckLabels
        FieldLabel fieldLabel = environmentSnapshot.getFieldLabel(condition.field);

        if (fieldLabel == null) {
            return checkField(document, condition.field, environmentSnapshot);
        } else {
            if (document.hasLabelValues(fieldLabel.name)) {
                //Label values already added to document
                return new FieldStatus();
            }

            for (String field : fieldLabel.fields) {
                FieldStatus fieldStatus = checkField(document, field, environmentSnapshot);
                if (fieldStatus == null || fieldStatus.reasonForMissingField == null) {
                    //Add the value for the label to the document
                    document.addLabelValues(fieldLabel.name, document.getMetadata().get(field));
                    document.addLabelValues(fieldLabel.name, document.getStreams().get(field));
                    return fieldStatus;
                }
            }
        }

        //No label or field found for this value.
        FieldStatus fieldStatus = new FieldStatus();
        fieldStatus.fieldMissing = true;
        fieldStatus.reasonForMissingField = UnevaluatedCondition.Reason.MISSING_FIELD;
        fieldStatus.missingField = condition.field;

        return fieldStatus;
    }

    @Override
    protected void evaluate(CollectionSequence collectionSequence, ConditionEvaluationResult result, DocumentUnderEvaluation document, T condition, EnvironmentSnapshot environmentSnapshot) throws CpeException {

        if (checkForCachedEvaluationResults(result, document, condition)) return;

        if (checkForUnevaluatedCondition(result, document, condition, environmentSnapshot)) return;

        evaluateFieldValues(result, condition, document, environmentSnapshot);
    }

    protected abstract void evaluateFieldValues(ConditionEvaluationResult result, T condition, DocumentUnderEvaluation document, EnvironmentSnapshot environmentSnapshot);

    private FieldStatus checkField(DocumentUnderEvaluation document, String fieldName, EnvironmentSnapshot environmentSnapshot) {
        FieldStatus fieldStatus = new FieldStatus();

        //If there are values for this field name then return
        if (document.getMetadata().containsKey(fieldName) || document.getStreams().containsKey(fieldName) ) {
            return fieldStatus;
        }
        fieldStatus.fieldMissing = true;
        fieldStatus.missingField = fieldName;
        fieldStatus.reasonForMissingField = UnevaluatedCondition.Reason.MISSING_FIELD;

        return fieldStatus;
    }

    protected boolean checkForUnevaluatedCondition(ConditionEvaluationResult result, DocumentUnderEvaluation document, T condition, EnvironmentSnapshot environmentSnapshot) {

        FieldStatus fieldStatus = fieldMissing(document, condition, environmentSnapshot);

        if (!fieldStatus.fieldMissing) {
            return false;
        }

        // record as an unevaluated condition unless we dont have any field to deal with it
        // i.e its a repository field, and its just not on the document.  All others should be
        // unevalauted e.g. Missing_service.

        // Now there is a difference on MISSING_FIELD returns status
        // - driven by the status of do we have all metadata available to us at this point in time.
        // Pre Metadata Extraction - Unevaluated as all repository fields may not be present.
        // Post Metadata Extraction - Unmatched, as we finally need to trust that all fields are just not going to be there.
        boolean unEvaluated = false;
        if (fieldStatus.reasonForMissingField == UnevaluatedCondition.Reason.MISSING_SERVICE) {
            unEvaluated = true;
        } else if (fieldStatus.reasonForMissingField == UnevaluatedCondition.Reason.MISSING_FIELD) {
            if (fieldStatus.missingField == null) {
                // we only come into here when we have hit a missing field as such its always unevaluated.
                unEvaluated = true;
            } else {
                // This is the field we asked for so its a simple repository field missing.
                // At present its unevaluated if not all info is present yet!
                unEvaluated = !document.getFullMetadata();
            }
        }

        if (unEvaluated) {
            recordUnevaluatedCondition(result, condition, fieldStatus.reasonForMissingField);
            return true;
        }

        return false;
    }

    protected void recordUnevaluatedCondition(ConditionEvaluationResult result, Condition condition, UnevaluatedCondition.Reason reason) {
        UnevaluatedCondition unevaluatedCondition = new UnevaluatedCondition(condition, reason);
        result.getUnevaluatedConditions().add(unevaluatedCondition);
    }

    protected static class FieldStatus {
        boolean fieldMissing;
        String missingField;
        UnevaluatedCondition.Reason reasonForMissingField;
    }
}
