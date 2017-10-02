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

import com.github.cafdataprocessing.corepolicy.document.DocumentUnderEvaluation;
import com.google.common.base.Stopwatch;
import com.github.cafdataprocessing.corepolicy.common.ApiProperties;
import com.github.cafdataprocessing.corepolicy.common.EnvironmentSnapshot;
import com.github.cafdataprocessing.corepolicy.common.dto.conditions.RegexCondition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;

/**
 * Defines a condition that evaluates against a regular expression.
 */
@Component("RegexCondition")
public class RegexEvaluator extends FieldConditionEvaluator<RegexCondition> {
    private ContentExpressionHelper contentExpressionHelper;

    @Autowired
    public RegexEvaluator(ContentExpressionHelper contentExpressionHelper, ApiProperties apiProperties){
        super(apiProperties);
        this.contentExpressionHelper = contentExpressionHelper;
    }

    @Override
    protected void evaluateFieldValues(ConditionEvaluationResult result, RegexCondition condition, DocumentUnderEvaluation document, EnvironmentSnapshot environmentSnapshot) {
        String expression = condition.value;

        Stopwatch stopwatch = Stopwatch.createStarted();
        //todo check this still works with labels
        Collection<String> matches = contentExpressionHelper
                .handleRegexExpression(document, condition.field, expression);
        document.logTime("Evaluate-RegexCondition-Regex", stopwatch);

        result.populateEvaluationResult(!matches.isEmpty(), condition, document, matches, true);
    }
}
