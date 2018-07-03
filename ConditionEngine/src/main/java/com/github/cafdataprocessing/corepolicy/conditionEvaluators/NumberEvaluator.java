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

import com.google.common.base.Strings;
import com.github.cafdataprocessing.corepolicy.common.ApiProperties;
import com.github.cafdataprocessing.corepolicy.common.EnvironmentSnapshot;
import com.github.cafdataprocessing.corepolicy.common.dto.conditions.NumberCondition;
import com.github.cafdataprocessing.corepolicy.common.shared.MetadataValue;
import com.github.cafdataprocessing.corepolicy.document.DocumentUnderEvaluation;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.LinkedList;

/**
 *
 *
 * Evaluator for the NumberCondition
 */
@Component("NumberCondition")
class NumberEvaluator extends FieldConditionEvaluator<NumberCondition> {

    @Autowired
    public NumberEvaluator(ApiProperties apiProperties) {
        super(apiProperties);
    }

    @Override
    protected void evaluateFieldValues(ConditionEvaluationResult result, NumberCondition condition, DocumentUnderEvaluation document, EnvironmentSnapshot environmentSnapshot) {

        //convert values to numbers
        Collection<Long> documentFieldValues = new LinkedList<>();

        for(MetadataValue metadataValue : document.getValues(condition.field)){
            if ( metadataValue == null )
                continue;

            String documentVal = metadataValue.getAsString();
            if(Strings.isNullOrEmpty( documentVal ) )
                continue; //ignore empty values

            try {
                documentFieldValues.add(Long.parseLong(documentVal));
            } catch (NumberFormatException ex){
                //Failure to convert a string to a number should be treated as if the value does not exist
            }
        }

        switch (condition.operator){
            case EQ:
                result.setMatch(documentFieldValues.contains(condition.value));
                break;
            case GT:
                result.setMatch(documentFieldValues.stream().anyMatch(a -> condition.value < a));
                break;
            case LT:
                result.setMatch(documentFieldValues.stream().anyMatch(a -> condition.value > a));
                break;
            default:
                throw new NotImplementedException("Unrecognised number operator: " + condition.operator);
        }

        result.populateEvaluationResult(result.isMatch(), condition, document, false );
    }


}
