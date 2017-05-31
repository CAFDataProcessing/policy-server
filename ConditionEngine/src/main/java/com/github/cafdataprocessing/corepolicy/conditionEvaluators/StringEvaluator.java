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
package com.github.cafdataprocessing.corepolicy.conditionEvaluators;

import com.github.cafdataprocessing.corepolicy.common.ApiProperties;
import com.github.cafdataprocessing.corepolicy.common.EnvironmentSnapshot;
import com.github.cafdataprocessing.corepolicy.common.dto.conditions.StringCondition;
import com.github.cafdataprocessing.corepolicy.common.exceptions.BackEndRequestFailedCpeException;
import com.github.cafdataprocessing.corepolicy.common.exceptions.excpetionErrors.BackEndRequestFailedErrors;
import com.github.cafdataprocessing.corepolicy.common.shared.MetadataValue;
import com.github.cafdataprocessing.corepolicy.document.DocumentUnderEvaluation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.Locale;

/**
 * Evaluator for String Condition
 */
@Component("StringCondition")
class StringEvaluator extends FieldConditionEvaluator<StringCondition> {

    @Autowired
    public StringEvaluator(ApiProperties apiProperties) {
        super(apiProperties);
    }

    @Override
    protected void evaluateFieldValues(ConditionEvaluationResult result, StringCondition condition, DocumentUnderEvaluation document, EnvironmentSnapshot environmentSnapshot) {

        if(condition.field == null){
            throw new BackEndRequestFailedCpeException(BackEndRequestFailedErrors.InvalidDataDetected, new IllegalArgumentException("A value is required for condition " + condition.id));
        }
        final String fieldValue = condition.value.toUpperCase(Locale.getDefault());

        final Collection<MetadataValue> fieldValues = document.getValues(condition.field);
        result.setMatch(isMatch(condition, fieldValue, fieldValues));

        result.populateEvaluationResult( result.isMatch(), condition, document, Collections.singletonList(condition.value), false );
    }

    private boolean isMatch(StringCondition condition, String fieldValue, Collection<MetadataValue> metadataValues ) {
        boolean match = false;

        // TODO - make stream aware.
        Collection<String> fieldValues = MetadataValue.getStringValues(metadataValues);

        switch(condition.operator){
            case CONTAINS:
            {
                match = fieldValues.stream()
                        .anyMatch(value -> value.toUpperCase(Locale.getDefault()).contains(fieldValue));
                break;
            }
            case ENDS_WITH:
            {
                match = fieldValues.stream()
                        .anyMatch(value -> value.toUpperCase(Locale.getDefault()).endsWith(fieldValue));
                break;
            }
            case IS:
            {
                match = fieldValues.stream()
                        .anyMatch(value -> value.equalsIgnoreCase(fieldValue));
                break;
            }
            case STARTS_WITH:{
                match = fieldValues.stream()
                        .anyMatch(value -> value.toUpperCase(Locale.getDefault()).startsWith(fieldValue));
                break;
            }
        }
        return match;
    }
}
