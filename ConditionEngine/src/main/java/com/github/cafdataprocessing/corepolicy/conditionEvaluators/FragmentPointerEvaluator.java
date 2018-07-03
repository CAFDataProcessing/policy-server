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

import com.github.cafdataprocessing.corepolicy.common.EnvironmentSnapshot;
import com.github.cafdataprocessing.corepolicy.common.dto.CollectionSequence;
import com.github.cafdataprocessing.corepolicy.common.dto.conditions.Condition;
import com.github.cafdataprocessing.corepolicy.common.dto.conditions.FragmentCondition;
import com.github.cafdataprocessing.corepolicy.common.exceptions.BackEndRequestFailedCpeException;
import com.github.cafdataprocessing.corepolicy.common.exceptions.CpeException;
import com.github.cafdataprocessing.corepolicy.common.exceptions.excpetionErrors.BackEndRequestFailedErrors;
import com.github.cafdataprocessing.corepolicy.document.DocumentUnderEvaluation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component("FragmentCondition")
class FragmentPointerEvaluator extends ConditionEvaluatorBase<FragmentCondition> {
    private EvaluateCondition evaluateCondition;

    @Autowired
    public FragmentPointerEvaluator(EvaluateCondition evaluateCondition){
        this.evaluateCondition = evaluateCondition;
    }

    @Override
    protected void evaluate(CollectionSequence collectionSequence, ConditionEvaluationResult result, DocumentUnderEvaluation document, FragmentCondition condition, EnvironmentSnapshot environmentSnapshot) throws CpeException {
        if(condition.value == null){
            throw new BackEndRequestFailedCpeException(BackEndRequestFailedErrors.InvalidDataDetected, new Exception("Condition Fragment id " + condition.id + " has no target condition."));
        }

        final Condition subCondition = environmentSnapshot.getCondition(condition.value);
        ConditionEvaluationResult subConditionResult = evaluateCondition.evaluate(collectionSequence, document, subCondition, environmentSnapshot);

        if(subConditionResult.isMatch()) {

//Discussed with the team and agreed that we should not be including the fragment pointer as a matched condition
//Uncomment this if it should be
//            MatchedCondition matchedCondition = new MatchedCondition(condition);
//            result.getMatchedConditions().add(matchedCondition);
        }

        result.populateEvaluationResult( subConditionResult, true );
    }
}
