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

import com.github.cafdataprocessing.corepolicy.booleanagent.BooleanAgentServices;
import com.google.common.base.Stopwatch;
import com.github.cafdataprocessing.corepolicy.booleanagent.BooleanAgentQueryResult;
import com.github.cafdataprocessing.corepolicy.common.ApiProperties;
import com.github.cafdataprocessing.corepolicy.common.EnvironmentSnapshot;
import com.github.cafdataprocessing.corepolicy.common.dto.UnevaluatedCondition;
import com.github.cafdataprocessing.corepolicy.common.dto.conditions.TextCondition;
import com.github.cafdataprocessing.corepolicy.document.DocumentUnderEvaluation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Evaluator for ContentExpression condition. Expect that a field agentid (case sensitive) containing the conditions
 * id will be applied to the document in the event of a success.
 */
@Component("TextCondition")
class TextEvaluator extends FieldConditionEvaluator<TextCondition> {
    private final BooleanAgentServices booleanAgentServices;
    private ContentExpressionHelper contentExpressionHelper;

    @Autowired
    public TextEvaluator(BooleanAgentServices booleanAgentServices,ContentExpressionHelper contentExpressionHelper,
                         ApiProperties apiProperties) {
        super(apiProperties);
        this.booleanAgentServices = booleanAgentServices;
        this.contentExpressionHelper = contentExpressionHelper;
    }

    @Override
    protected void evaluateFieldValues(ConditionEvaluationResult result, TextCondition condition, DocumentUnderEvaluation document, EnvironmentSnapshot environmentSnapshot) {
        if (!booleanAgentServices.getAvailable()) {
            recordUnevaluatedCondition(result, condition, UnevaluatedCondition.Reason.MISSING_SERVICE);
            return;
        }

        Stopwatch stopwatch = Stopwatch.createStarted();
        BooleanAgentQueryResult booleanAgentQueryResult = contentExpressionHelper
                .handleBooleanAgentExpression(environmentSnapshot.getInstanceId(), booleanAgentServices,
                        document, condition.field, condition.language);
        document.logTime("Evaluate-TextCondition-Query", stopwatch);

        boolean isMatch = booleanAgentQueryResult != null && booleanAgentQueryResult.getConditionIdTerms().containsKey(condition.id);

        result.populateEvaluationResult(isMatch, condition, document, isMatch ? booleanAgentQueryResult.getConditionIdTerms().get(condition.id) : null, true );
    }

}
