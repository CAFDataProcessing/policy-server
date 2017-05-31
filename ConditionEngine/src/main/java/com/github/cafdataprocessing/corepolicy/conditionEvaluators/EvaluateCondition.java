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
import com.github.cafdataprocessing.corepolicy.common.exceptions.CpeException;
import com.github.cafdataprocessing.corepolicy.document.DocumentUnderEvaluation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Basic condition evaluator implementation
 */
@Component
public class EvaluateCondition implements ConditionEvaluator {
    private ApplicationContext applicationContext;

    @Autowired
    public EvaluateCondition(ApplicationContext applicationContext){
        this.applicationContext = applicationContext;
    }

    public ConditionEvaluationResult evaluate(CollectionSequence collectionSequence, DocumentUnderEvaluation document, Condition condition,
                                              EnvironmentSnapshot environmentSnapshot) throws CpeException {
        final ConditionEvaluator conditionEvaluator =
                applicationContext.getBean(condition.getClass().getSimpleName(), ConditionEvaluator.class);

        return conditionEvaluator.evaluate(collectionSequence, document, condition, environmentSnapshot);
    }
}
