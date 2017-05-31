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
import com.github.cafdataprocessing.corepolicy.common.dto.UnevaluatedCondition;
import com.github.cafdataprocessing.corepolicy.common.dto.conditions.Condition;
import com.github.cafdataprocessing.corepolicy.common.dto.conditions.NotCondition;
import com.github.cafdataprocessing.corepolicy.common.exceptions.CpeException;
import com.github.cafdataprocessing.corepolicy.document.DocumentUnderEvaluation;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for Not Evaluator
 */
@RunWith(MockitoJUnitRunner.class)
public class NotEvaluatorTest {
    long currentConditionId = 1;

    @Mock
    CollectionSequence collectionSequence;

    @Mock
    private DocumentUnderEvaluation document;

    @Mock
    private EvaluateCondition evaluateCondition;

    @Mock
    private EnvironmentSnapshot environmentSnapshot;

    @InjectMocks
    private NotEvaluator sut = new NotEvaluator(evaluateCondition);

    @Before
    public void before() throws CpeException {
        when(document.getConditionEvaluationResult(any())).thenReturn(null);
    }

    @After
    public void cleanup(){

    }

    /**
     * A utility method to create an Evaluator and evaluate the Document field against a provided condition
     * */
    private ConditionEvaluationResult evaluate(NotCondition condition) throws CpeException {
        return sut.evaluate(this.collectionSequence, this.document, condition, environmentSnapshot);
    }

    private NotCondition getNotConditionForCondition(boolean result, Collection<Condition> missingFieldConditions) throws CpeException {
        NotCondition notCondition = new NotCondition();
        Condition targetCondition = mock(Condition.class);

        final long id = currentConditionId++;
        targetCondition.id = id;
        notCondition.condition = (targetCondition);

        when(environmentSnapshot.getCondition(eq(targetCondition.id))).thenReturn(targetCondition);

        final ConditionEvaluationResult conditionEvaluationResult = new ConditionEvaluationResult();
        conditionEvaluationResult.setMatch(result);

        if(missingFieldConditions!=null){
            Collection<UnevaluatedCondition> unevaluatedConditions = missingFieldConditions.stream().map(c -> {
                UnevaluatedCondition unevaluatedCondition = new UnevaluatedCondition(c, UnevaluatedCondition.Reason.MISSING_FIELD);

                return unevaluatedCondition;

            }).collect(Collectors.toList());

            conditionEvaluationResult.getUnevaluatedConditions().addAll(unevaluatedConditions);
        }

        when(evaluateCondition.evaluate(any(CollectionSequence.class), any(DocumentUnderEvaluation.class),
                eq(targetCondition),
                eq(environmentSnapshot))).thenReturn(conditionEvaluationResult);

        return notCondition;
    }

    @Test
    public void testMatches() throws Exception {

        NotCondition notCondition = getNotConditionForCondition(false, null);
        ConditionEvaluationResult evaluationResult = evaluate(notCondition);

        Assert.assertTrue(evaluationResult.isMatch());
    }

    @Test
    public void testDoesNotMatchAndContainsMissingFields() throws Exception {
        Condition missingFieldCondition = mock(Condition.class);
        NotCondition notCondition = getNotConditionForCondition(false, Arrays.asList(missingFieldCondition));

        ConditionEvaluationResult evaluationResult = evaluate(notCondition);

        Assert.assertFalse(evaluationResult.isMatch());
        Assert.assertTrue(ConditionKeyHelper.containsKey(evaluationResult.getUnevaluatedConditions(), missingFieldCondition).isPresent());
    }

    @Test
    public void testIsNotMatches() throws Exception {
        NotCondition notCondition = getNotConditionForCondition(true, null);
        ConditionEvaluationResult evaluationResult = evaluate(notCondition);

        Assert.assertFalse(evaluationResult.isMatch());
    }

    @Test
    public void testSubConditionMatchesNotAddedToList() throws CpeException {
        NotCondition notCondition = getNotConditionForCondition(true, null);
        ConditionEvaluationResult result = evaluate(notCondition);

        Assert.assertEquals(0, result.getMatchedConditions().size());
        Assert.assertEquals(0, result.getAllConditionMatches().size());
    }

    @Test
    public void testSubConditionNotMatchAddedToList() throws CpeException {
        NotCondition notCondition = getNotConditionForCondition(false, null);

        ConditionEvaluationResult result = evaluate(notCondition);
        Assert.assertEquals(1, result.getMatchedConditions().size());
        Assert.assertEquals(1, result.getAllConditionMatches().size());
    }


}
