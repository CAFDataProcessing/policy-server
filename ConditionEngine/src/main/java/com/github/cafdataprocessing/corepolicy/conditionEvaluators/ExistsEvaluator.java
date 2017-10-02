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
package com.github.cafdataprocessing.corepolicy.conditionEvaluators;

import com.github.cafdataprocessing.corepolicy.common.ApiProperties;
import com.github.cafdataprocessing.corepolicy.common.EnvironmentSnapshot;
import com.github.cafdataprocessing.corepolicy.common.dto.conditions.ExistsCondition;
import com.github.cafdataprocessing.corepolicy.document.DocumentUnderEvaluation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * Evaluator for the FieldExists Condition
 */
@Component("ExistsCondition")
class ExistsEvaluator extends FieldConditionEvaluator<ExistsCondition> {

    @Autowired
    public ExistsEvaluator(ApiProperties apiProperties) {
        super(apiProperties);
    }

    @Override
    protected void evaluateFieldValues(ConditionEvaluationResult result, ExistsCondition condition, DocumentUnderEvaluation document, EnvironmentSnapshot environmentSnapshot) {

        boolean isMatch = document.getValues(condition.field) != null && !document.getValues(condition.field).isEmpty();

        result.populateEvaluationResult(isMatch, condition, document, false );
    }
}
