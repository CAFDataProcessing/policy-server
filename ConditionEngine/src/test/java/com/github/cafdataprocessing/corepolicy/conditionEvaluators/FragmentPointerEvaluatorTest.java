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
import com.github.cafdataprocessing.corepolicy.common.dto.MatchedCondition;
import com.github.cafdataprocessing.corepolicy.common.dto.conditions.Condition;
import com.github.cafdataprocessing.corepolicy.common.dto.conditions.FragmentCondition;
import com.github.cafdataprocessing.corepolicy.common.dto.conditions.TestCondition;
import com.github.cafdataprocessing.corepolicy.common.exceptions.CpeException;
import com.github.cafdataprocessing.corepolicy.document.DocumentUnderEvaluation;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

/**
 * Tests for the FragmentPointerEvaluator
 */
@RunWith(MockitoJUnitRunner.class)
public class FragmentPointerEvaluatorTest {
    @Mock
    CollectionSequence collectionSequence;

    @Mock
    private DocumentUnderEvaluation document;

    @InjectMocks
    private TestCondition matchingCondition;

    @InjectMocks
    private TestCondition  notMatchingCondition;

    @Mock
    private EvaluateCondition evaluateCondition;

    @Mock
    private EnvironmentSnapshot environmentSnapshot;

    @InjectMocks
    private FragmentPointerEvaluator evaluator = new FragmentPointerEvaluator(evaluateCondition);

    @Before
    public void setup() {
        when(environmentSnapshot.getCondition(eq(matchingCondition.id))).thenReturn(matchingCondition);
        when(environmentSnapshot.getCondition(eq(notMatchingCondition.id))).thenReturn(notMatchingCondition);
        when(document.getConditionEvaluationResult(any())).thenReturn(null);
    }

    @After
    public void cleanup(){
    }

    private static FragmentCondition getFragmentPointerCondition(Condition targetCondition){
        FragmentCondition fragmentPointerCondition = new FragmentCondition();
        fragmentPointerCondition.value = (targetCondition.id);
        return fragmentPointerCondition;
    }

    /**
     * A utility method to create an Evaluator and evaluate the Document field against a provided condition
     * */
    private ConditionEvaluationResult evaluate(FragmentCondition condition) {
        return evaluator.evaluate(this.collectionSequence, this.document, condition, environmentSnapshot);
    }

    @Test
    public void testMatches() throws Exception {
        FragmentCondition fragmentPointerCondition = getFragmentPointerCondition(matchingCondition);

        Mockito.reset(evaluateCondition);
        registerEvaluationResult(true, matchingCondition, Arrays.asList(matchingCondition));

        ConditionEvaluationResult evaluationResult = evaluate(fragmentPointerCondition);

        Assert.assertTrue(evaluationResult.isMatch());
        Assert.assertEquals(1, evaluationResult.getMatchedConditions().size());
        Assert.assertEquals(1, evaluationResult.getAllConditionMatches().size());
    }

    @Test
    public void testMatchesAndNotContainsMissingFields() throws Exception {
        FragmentCondition fragmentPointerCondition = getFragmentPointerCondition(matchingCondition);

        Mockito.reset(evaluateCondition);
        registerEvaluationResult(true, matchingCondition, Arrays.asList(matchingCondition));

        ConditionEvaluationResult evaluationResult = evaluate(fragmentPointerCondition);

        Assert.assertTrue(evaluationResult.isMatch());
        Assert.assertEquals(0, evaluationResult.getUnevaluatedConditions().size());
        Assert.assertEquals(1, evaluationResult.getMatchedConditions().size());
        Assert.assertEquals(1, evaluationResult.getAllConditionMatches().size());
    }

    @Test
    public void testIsNotMatches() throws Exception {
        FragmentCondition fragmentPointerCondition = getFragmentPointerCondition(notMatchingCondition);

        Mockito.reset(evaluateCondition);
        registerEvaluationResult(false, notMatchingCondition, Arrays.asList());

        ConditionEvaluationResult evaluationResult = evaluate(fragmentPointerCondition);

        Assert.assertFalse(evaluationResult.isMatch());
    }

    @Test
    public void testSubConditionNotMatches() {
        FragmentCondition fragmentPointerCondition = getFragmentPointerCondition(this.notMatchingCondition);

        Mockito.reset(evaluateCondition);
        registerEvaluationResult(false, notMatchingCondition, Arrays.asList());

        ConditionEvaluationResult evaluationResult = evaluate(fragmentPointerCondition);

        Assert.assertEquals(0, evaluationResult.getMatchedConditions().size());
        Assert.assertEquals(0, evaluationResult.getAllConditionMatches().size());
    }

    private void registerEvaluationResult(Boolean isMatch, Condition condition, Collection<Condition> matchingConditions){
        ConditionEvaluationResult mockConditionEvaluationResult = new ConditionEvaluationResult();

        mockConditionEvaluationResult.setMatch(isMatch);
        if(matchingConditions!=null){

            String reference = UUID.randomUUID().toString();
            mockConditionEvaluationResult.getMatchedConditions().addAll(matchingConditions.stream()
                    .map(c -> new MatchedCondition(reference, c)).collect(Collectors.toList()));

            mockConditionEvaluationResult.getAllConditionMatches().addAll(matchingConditions.stream()
                    .map(c -> new MatchedCondition(reference, c)).collect(Collectors.toList()));
        }

        try {
            when(evaluateCondition.evaluate(any(CollectionSequence.class), any(DocumentUnderEvaluation.class), eq(condition), any(EnvironmentSnapshot.class)))
                    .thenReturn(mockConditionEvaluationResult);
        } catch (CpeException e) {
            e.printStackTrace();
        }
    }
}
