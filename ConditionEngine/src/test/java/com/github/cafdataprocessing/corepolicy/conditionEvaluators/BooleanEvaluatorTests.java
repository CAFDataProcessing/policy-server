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
import com.github.cafdataprocessing.corepolicy.document.DocumentUnderEvaluationImpl;
import com.github.cafdataprocessing.corepolicy.Helper;
import com.github.cafdataprocessing.corepolicy.common.ApiProperties;
import com.github.cafdataprocessing.corepolicy.common.ConditionEngineMetadata;
import com.github.cafdataprocessing.corepolicy.common.EnvironmentSnapshot;
import com.github.cafdataprocessing.corepolicy.common.dto.CollectionSequence;
import com.github.cafdataprocessing.corepolicy.common.dto.MatchedCondition;
import com.github.cafdataprocessing.corepolicy.common.dto.conditions.BooleanCondition;
import com.github.cafdataprocessing.corepolicy.common.dto.conditions.BooleanOperator;
import com.github.cafdataprocessing.corepolicy.common.dto.conditions.Condition;
import com.github.cafdataprocessing.corepolicy.common.dto.conditions.TestCondition;
import com.github.cafdataprocessing.corepolicy.common.exceptions.CpeException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

/**
 *
 * Tests for the ConditionSetEvaluator
 */
@RunWith(MockitoJUnitRunner.class)
public class BooleanEvaluatorTests {
    @Mock
    CollectionSequence collectionSequence;

    @Mock
    DocumentUnderEvaluation document;

    @Mock
    private ConditionEngineMetadata conditionEngineMetadata;

//    @Mock
//    ConditionRepository conditionRepository;

    @Mock
    EvaluateCondition evaluateCondition;

    @Mock
    private EnvironmentSnapshot environmentSnapshot;

    @Mock
    private ApiProperties apiProperties;

//    @Mock
//    ConditionEvaluator mockEvaluator;

    @InjectMocks
    TestCondition matchingNumberCondition;
    @InjectMocks
    TestCondition notMatchingNumberCondition;
    @InjectMocks
    TestCondition matchingStringCondition;
    @InjectMocks
    TestCondition notMatchingStringCondition;
    @InjectMocks
    TestCondition notMatchConditionFieldExists;
    @InjectMocks
    TestCondition notMatchNoFieldCondition;

    @Before
    public void setup()  {
        collectionSequence.fullConditionEvaluation = false;
        when(document.getConditionEvaluationResult(any())).thenReturn(null);
    }

    @After
    public void cleanup(){

    }

    /**
     * A utility method to create a DocumentCollection by joining provided Conditions in a ConditionSet
     * */
    private BooleanCondition createBooleanCondition(BooleanOperator conditionCollectionMatchType, Condition... conditions)  {
        BooleanCondition conditionSet = new BooleanCondition();
        conditionSet.id = (Helper.getId());
        conditionSet.operator = (conditionCollectionMatchType);

//        when(conditionEngineRepository.getCondition(conditionSet.id)).thenReturn(conditionSet);
        conditionSet.children = new ArrayList<>();

        for (Condition cond : conditions) {
            conditionSet.children.add(cond);
        }
        return conditionSet;
    }

    /**
     * A utility method to create a ConditionSetEvaluator and evaluate the Document field against a provided condition
     * */
    private ConditionEvaluationResult evaluate(BooleanCondition condition)  {
        ConditionEvaluator<BooleanCondition> evaluator = new BooleanConditionEvaluator(evaluateCondition);
        return evaluator.evaluate(this.collectionSequence, this.document, condition, environmentSnapshot);
    }

    @Test
    public void checkSingleAndMatch()  {
        BooleanCondition set = createBooleanCondition(BooleanOperator.AND, matchingNumberCondition);

        Mockito.reset(evaluateCondition);
        registerEvaluationResult(true, matchingNumberCondition, Arrays.asList(matchingNumberCondition));

        ConditionEvaluationResult conditionEvaluationResult = evaluate(set);
        Assert.assertTrue(conditionEvaluationResult.isMatch());
        Assert.assertEquals(2, conditionEvaluationResult.getMatchedConditions().size());
        Assert.assertEquals(2, conditionEvaluationResult.getAllConditionMatches().size());
    }

    @Test
    public void checkSingleAndNotMatch()  {
        BooleanCondition set = createBooleanCondition(BooleanOperator.AND, notMatchingNumberCondition);

        Mockito.reset(evaluateCondition);
        registerEvaluationResult(false, notMatchingNumberCondition, null);

        Assert.assertFalse(evaluate(set).isMatch());
    }

    @Test
    public void checkSingleOrMatch()  {
        BooleanCondition set = createBooleanCondition(BooleanOperator.OR, matchingNumberCondition);

        Mockito.reset(evaluateCondition);
        registerEvaluationResult(true, matchingNumberCondition, Arrays.asList(matchingNumberCondition));

        ConditionEvaluationResult conditionEvaluationResult = evaluate(set);

        Assert.assertTrue(conditionEvaluationResult.isMatch());
        Assert.assertEquals(2, conditionEvaluationResult.getMatchedConditions().size());
        Assert.assertEquals(2, conditionEvaluationResult.getAllConditionMatches().size());
    }

    @Test
    public void checkSingleOrNotMatch()  {
        BooleanCondition set = createBooleanCondition(BooleanOperator.OR, notMatchingNumberCondition);

        Mockito.reset(evaluateCondition);
        registerEvaluationResult(false, notMatchingNumberCondition, null);

        Assert.assertFalse(evaluate(set).isMatch());
    }

    @Test
     public void checkAndSuccess()  {
        BooleanCondition set = createBooleanCondition(BooleanOperator.AND, matchingNumberCondition, matchingStringCondition);

        Mockito.reset(evaluateCondition);
        registerEvaluationResult(true, matchingNumberCondition, Arrays.asList(matchingNumberCondition));
        registerEvaluationResult(true, matchingStringCondition, Arrays.asList(matchingStringCondition));

        ConditionEvaluationResult conditionEvaluationResult = evaluate(set);

        Assert.assertTrue(conditionEvaluationResult.isMatch());
        Assert.assertEquals(3, conditionEvaluationResult.getMatchedConditions().size());
        Assert.assertEquals(3, conditionEvaluationResult.getAllConditionMatches().size());
    }

    @Test
    public void checkOrMultipleSuccess()  {
        collectionSequence.fullConditionEvaluation = true;
        BooleanCondition set = createBooleanCondition(BooleanOperator.OR, matchingNumberCondition, matchingStringCondition);

        Mockito.reset(evaluateCondition);
        registerEvaluationResult(true, matchingNumberCondition, Arrays.asList(matchingNumberCondition));
        registerEvaluationResult(true, matchingStringCondition, Arrays.asList(matchingStringCondition));

        ConditionEvaluationResult evaluationResult = evaluate(set);

        Assert.assertTrue(evaluationResult.isMatch());
        Assert.assertEquals(3, evaluationResult.getMatchedConditions().size());
    }

    @Test
    public void checkOrMultipleOneMatches()  {
        BooleanCondition set = createBooleanCondition(BooleanOperator.OR, notMatchConditionFieldExists, matchingStringCondition);

        Mockito.reset(evaluateCondition);
        registerEvaluationResult(false, notMatchConditionFieldExists, Arrays.asList());
        registerEvaluationResult(true, matchingStringCondition, Arrays.asList(matchingStringCondition));

        ConditionEvaluationResult evaluationResult = evaluate(set);
        Assert.assertTrue(evaluationResult.isMatch());
        Assert.assertEquals(2, evaluationResult.getMatchedConditions().size());
        Assert.assertEquals(2, evaluationResult.getAllConditionMatches().size());
    }

    @Test
    public void checkAndFailure()  {
        BooleanCondition set = createBooleanCondition(BooleanOperator.AND, matchingNumberCondition, notMatchingNumberCondition);

        Mockito.reset(evaluateCondition);
        registerEvaluationResult(true, matchingNumberCondition, Arrays.asList(matchingNumberCondition));
        registerEvaluationResult(false, notMatchingNumberCondition, Arrays.asList());

        ConditionEvaluationResult evaluationResult = evaluate(set);

        Assert.assertFalse(evaluationResult.isMatch());
    }

    @Test
    public void checkOrAllSuccess()  {
        BooleanCondition set = createBooleanCondition(BooleanOperator.OR, matchingNumberCondition, matchingStringCondition);

        Mockito.reset(evaluateCondition);
        registerEvaluationResult(true, matchingNumberCondition, Arrays.asList(matchingNumberCondition));
        registerEvaluationResult(true, matchingStringCondition, Arrays.asList(matchingStringCondition));

        Assert.assertTrue(evaluate(set).isMatch());
    }

    @Test
    public void checkOrSomeSuccess()  {
        BooleanCondition set = createBooleanCondition(BooleanOperator.OR, notMatchingNumberCondition, matchingStringCondition);

        Mockito.reset(evaluateCondition);
        registerEvaluationResult(false, notMatchingNumberCondition, Arrays.asList());
        registerEvaluationResult(true, matchingStringCondition, Arrays.asList(matchingStringCondition));

        ConditionEvaluationResult evaluationResult = evaluate(set);

        Assert.assertTrue(evaluationResult.isMatch());
    }

    @Test
    public void checkOrFailure()  {
        BooleanCondition set = createBooleanCondition(BooleanOperator.OR, notMatchingNumberCondition, notMatchingStringCondition);

        Mockito.reset(evaluateCondition);
        registerEvaluationResult(false, notMatchingNumberCondition, Arrays.asList());
        registerEvaluationResult(false, notMatchingStringCondition, Arrays.asList());

        ConditionEvaluationResult evaluationResult = evaluate(set);

        Assert.assertFalse(evaluationResult.isMatch());
        Assert.assertEquals(0, evaluationResult.getMatchedConditions().size());
        Assert.assertEquals(0, evaluationResult.getAllConditionMatches().size());
    }

    @Test
    public void checkNoMatchExistingFieldReturnsCondition()  {
        BooleanCondition set = createBooleanCondition(BooleanOperator.AND, notMatchConditionFieldExists);

        Mockito.reset(evaluateCondition);
        registerEvaluationResult(false, notMatchConditionFieldExists, Arrays.asList());

        ConditionEvaluationResult evaluationResult = evaluate(set);

        Assert.assertEquals(0, evaluationResult.getUnevaluatedConditions().size());
    }

    @Test
    public void multipleAndConditionsListedForMatch()  {
        BooleanCondition set = createBooleanCondition(BooleanOperator.AND, matchingNumberCondition, matchingStringCondition);

        Mockito.reset(evaluateCondition);
        registerEvaluationResult(true, matchingNumberCondition, Arrays.asList(matchingNumberCondition));
        registerEvaluationResult(true, matchingStringCondition, Arrays.asList(matchingStringCondition));

        ConditionEvaluationResult evaluationResult = evaluate(set);

        Assert.assertTrue(evaluationResult.isMatch());
        Assert.assertEquals(3, evaluationResult.getMatchedConditions().size());
        Assert.assertEquals(3, evaluationResult.getAllConditionMatches().size());
    }

    @Test
    public void multipleAndConditionsNoneListedForMatch()  {
        //important for this test that the matching condition is FIRST in the list
        BooleanCondition set = createBooleanCondition(BooleanOperator.AND, matchingNumberCondition, notMatchConditionFieldExists, matchingStringCondition);

        Mockito.reset(evaluateCondition);
        registerEvaluationResult(true, matchingNumberCondition, Arrays.asList(matchingNumberCondition));
        registerEvaluationResult(false, notMatchConditionFieldExists, Arrays.asList());
        registerEvaluationResult(true, matchingStringCondition, Arrays.asList(matchingStringCondition));

        ConditionEvaluationResult evaluationResult = evaluate(set);

        Assert.assertFalse(evaluationResult.isMatch());
        Assert.assertEquals(0, evaluationResult.getMatchedConditions().size());

        // we should get a hit on the matching number condition as its the first limb - in the all condition matches.
        Assert.assertEquals(1, evaluationResult.getAllConditionMatches().size());
        Assert.assertEquals("AllConditionMatches should contain first condition limb ID: ", matchingNumberCondition.id, evaluationResult.getAllConditionMatches().stream().findFirst().get().id );
    }

    @Test
    public void firstOrConditionListedForMatch()  {
        BooleanCondition set = createBooleanCondition(BooleanOperator.OR, notMatchConditionFieldExists, matchingStringCondition);

        Mockito.reset(evaluateCondition);
        registerEvaluationResult(false, notMatchConditionFieldExists, Arrays.asList());
        registerEvaluationResult(true, matchingStringCondition, Arrays.asList(matchingStringCondition));

        ConditionEvaluationResult evaluationResult = evaluate(set);

        Assert.assertTrue(evaluationResult.isMatch());
        Assert.assertEquals(2, evaluationResult.getMatchedConditions().size());
    }

    private void registerEvaluationResult(Boolean isMatch, Condition condition, Collection<Condition> matchingConditions){
        ConditionEvaluationResult mockConditionEvaluationResult = new ConditionEvaluationResult();

        mockConditionEvaluationResult.setMatch(isMatch);
        if(matchingConditions!=null){
            mockConditionEvaluationResult.getMatchedConditions().addAll(matchingConditions.stream()
                    .map(c -> new MatchedCondition(new DocumentUnderEvaluationImpl(conditionEngineMetadata, apiProperties).getReference(), c)).collect(Collectors.toList()));

            mockConditionEvaluationResult.getAllConditionMatches().addAll(matchingConditions.stream()
                    .map(c -> new MatchedCondition(new DocumentUnderEvaluationImpl(conditionEngineMetadata, apiProperties).getReference(), c)).collect(Collectors.toList()));
        }

        try {
            when(evaluateCondition.evaluate(any(CollectionSequence.class), any(DocumentUnderEvaluation.class), eq(condition), any(EnvironmentSnapshot.class)))
                    .thenReturn(mockConditionEvaluationResult);
        } catch (CpeException e) {
            e.printStackTrace();
        }
    }
}
