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

import com.github.cafdataprocessing.corepolicy.common.ApiProperties;
import com.github.cafdataprocessing.corepolicy.common.ConditionEngineMetadata;
import com.github.cafdataprocessing.corepolicy.common.EnvironmentSnapshot;
import com.github.cafdataprocessing.corepolicy.common.dto.CollectionSequence;
import com.github.cafdataprocessing.corepolicy.common.dto.conditions.NumberCondition;
import com.github.cafdataprocessing.corepolicy.common.dto.conditions.NumberOperatorType;
import com.github.cafdataprocessing.corepolicy.common.exceptions.CpeException;
import com.github.cafdataprocessing.corepolicy.document.DocumentUnderEvaluationImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class NumberEvaluatorTest {

    @Mock
    CollectionSequence collectionSequence;

    @Mock
    private EnvironmentSnapshot environmentSnapshot;

    @Mock
    private ConditionEngineMetadata conditionEngineMetadata;

    @Mock
    private ApiProperties apiProperties;

    @Before
    public void setUp(){

    }

    private DocumentUnderEvaluationImpl getDocumentUnderEvaluation() {
        DocumentUnderEvaluationImpl doc = new DocumentUnderEvaluationImpl(conditionEngineMetadata, apiProperties);
        doc.addMetadataString("FieldA", "1");
        doc.addMetadataString("FieldA", "5");
        doc.addMetadataString("FieldB", "-432000000");
        return doc;
    }

    /**
     * A utility method to create an Evaluator and evaluate the Document field against a provided condition
     * */
    private ConditionEvaluationResult evaluate(NumberCondition condition) throws CpeException {
        ConditionEvaluator<NumberCondition> evaluator = new NumberEvaluator(apiProperties);
        return evaluator.evaluate(this.collectionSequence, getDocumentUnderEvaluation(), condition, environmentSnapshot);
    }

    @Test
    public void testSingleValueEqual() throws CpeException {
        NumberCondition numberCondition = new NumberCondition();
        numberCondition.operator = (NumberOperatorType.EQ);
        numberCondition.field = ("FieldA");
        numberCondition.value = ((long) 1);

        Assert.assertTrue(evaluate(numberCondition).isMatch());
    }

    @Test
    public void testSingleValueNotEqual() throws CpeException {
        NumberCondition numberCondition = new NumberCondition();
        numberCondition.operator = (NumberOperatorType.EQ);
        numberCondition.field = ("FieldA");
        numberCondition.value = ((long) 3);

        Assert.assertFalse(evaluate(numberCondition).isMatch());
    }

    @Test
    public void testSingleValueLessThan() throws CpeException {
        NumberCondition numberCondition = new NumberCondition();
        numberCondition.operator = (NumberOperatorType.GT);
        numberCondition.field = ("FieldA");
        numberCondition.value = ((long) 3);

        Assert.assertTrue(evaluate(numberCondition).isMatch());
    }

    @Test
    public void testSingleValueNotLessThan() throws CpeException {
        NumberCondition numberCondition = new NumberCondition();
        numberCondition.operator = (NumberOperatorType.GT);
        numberCondition.field = ("FieldA");
        numberCondition.value = ((long) 5);

        Assert.assertFalse(evaluate(numberCondition).isMatch());
    }

    @Test
    public void testSingleValueLessThanNegative() throws CpeException {
        NumberCondition numberCondition = new NumberCondition();
        numberCondition.operator = (NumberOperatorType.GT);
        numberCondition.field = ("FieldA");
        numberCondition.value = ((long) -1);

        Assert.assertTrue(evaluate(numberCondition).isMatch());
    }

    @Test
    public void testSingleValueGreaterThan() throws CpeException {
        NumberCondition numberCondition = new NumberCondition();
        numberCondition.operator = (NumberOperatorType.LT);
        numberCondition.field = ("FieldA");
        numberCondition.value = ((long) 6);

        Assert.assertTrue(evaluate(numberCondition).isMatch());
    }

    @Test
    public void testSingleValueNotGreaterThan() throws CpeException {
        NumberCondition numberCondition = new NumberCondition();
        numberCondition.operator = (NumberOperatorType.LT);
        numberCondition.field = ("FieldA");
        numberCondition.value = ((long) 1);

        Assert.assertFalse(evaluate(numberCondition).isMatch());
    }

    @Test
    public void testSingleNegativeValueEq() throws CpeException {
        NumberCondition numberCondition = new NumberCondition();
        numberCondition.operator = (NumberOperatorType.EQ);
        numberCondition.field = ("FieldB");
        numberCondition.value = (-432000000L);

        ConditionEvaluationResult conditionEvaluationResult = evaluate(numberCondition);

        Assert.assertTrue(conditionEvaluationResult.isMatch());
    }

}
