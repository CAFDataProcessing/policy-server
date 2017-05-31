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

import com.github.cafdataprocessing.corepolicy.common.EnvironmentSnapshot;
import com.github.cafdataprocessing.corepolicy.common.dto.CollectionSequence;
import com.github.cafdataprocessing.corepolicy.common.dto.conditions.Condition;
import com.github.cafdataprocessing.corepolicy.common.dto.conditions.NotCondition;
import com.github.cafdataprocessing.corepolicy.common.exceptions.BackEndRequestFailedCpeException;
import com.github.cafdataprocessing.corepolicy.common.exceptions.CpeException;
import com.github.cafdataprocessing.corepolicy.common.exceptions.excpetionErrors.BackEndRequestFailedErrors;
import com.github.cafdataprocessing.corepolicy.document.DocumentUnderEvaluation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Evaluator for the NotCondition
 */
@Component("NotCondition")
class NotEvaluator extends ConditionEvaluatorBase<NotCondition> {

    private EvaluateCondition evaluateCondition;

    @Autowired
    public NotEvaluator(EvaluateCondition evaluateCondition){
        this.evaluateCondition = evaluateCondition;
    }

    @Override
    protected void evaluate(CollectionSequence collectionSequence, ConditionEvaluationResult result, DocumentUnderEvaluation document, NotCondition condition, EnvironmentSnapshot environmentSnapshot) throws CpeException {
        if(condition.condition == null){
            throw new BackEndRequestFailedCpeException(BackEndRequestFailedErrors.InvalidDataDetected, new Exception("Not Condition id " + condition.id + " has no target condition."));
        }

        if (checkForCachedEvaluationResults(result, document, condition)) return;

        final Condition subCondition = condition.condition;
        ConditionEvaluationResult subConditionResult = evaluateCondition.evaluate(collectionSequence, document, subCondition, environmentSnapshot);

        if(!subConditionResult.getUnevaluatedConditions().isEmpty()){
            result.setMatch(false); //the missing field condition is preserved
            result.getUnevaluatedConditions().addAll(subConditionResult.getUnevaluatedConditions());
            return;
        }

        //the missing field condition is preserved
        result.populateEvaluationResult(!subConditionResult.isMatch(), condition, document, false);
    }
}
