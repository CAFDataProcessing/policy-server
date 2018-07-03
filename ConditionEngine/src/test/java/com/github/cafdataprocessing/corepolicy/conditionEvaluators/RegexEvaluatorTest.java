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

import com.github.cafdataprocessing.corepolicy.booleanagent.BooleanAgentServices;
import com.github.cafdataprocessing.corepolicy.Helper;
import com.github.cafdataprocessing.corepolicy.common.ApiProperties;
import com.github.cafdataprocessing.corepolicy.common.ConditionEngineMetadata;
import com.github.cafdataprocessing.corepolicy.common.EngineProperties;
import com.github.cafdataprocessing.corepolicy.common.EnvironmentSnapshot;
import com.github.cafdataprocessing.corepolicy.common.dto.CollectionSequence;
import com.github.cafdataprocessing.corepolicy.common.dto.conditions.RegexCondition;
import com.github.cafdataprocessing.corepolicy.common.exceptions.CpeException;
import com.github.cafdataprocessing.corepolicy.document.DocumentUnderEvaluation;
import com.github.cafdataprocessing.corepolicy.document.DocumentUnderEvaluationImpl;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.regex.Pattern;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Tests for the RegexEvaluator
 */
@RunWith(MockitoJUnitRunner.class)
public class RegexEvaluatorTest {
    DocumentUnderEvaluation document;

    @Mock
    CollectionSequence collectionSequence;

    @Mock
    private BooleanAgentServices booleanAgentServices;

    @Mock
    private EnvironmentSnapshot environmentSnapshot;

    @Mock
    private RegexMatcherFactory regexMatcherFactory;

    @Mock
    private EngineProperties engineProperties;

    @Mock
    private ApiProperties apiProperties;

    @Mock
    private ConditionEngineMetadata conditionEngineMetadata;

    @InjectMocks
    private ContentExpressionHelper contentExpressionHelper;

    @Before
    public void setup() throws CpeException {
        DocumentUnderEvaluationImpl document = new DocumentUnderEvaluationImpl(conditionEngineMetadata, apiProperties);
        document.addMetadataString("field", "some value hello hell");
        this.document = document;
        document.setFullMetadata(false);

        when(regexMatcherFactory.getPattern(anyString())).then(invocation -> {
            Object[] args = invocation.getArguments();
            return Pattern.compile((String) args[0]);
        });

        when(engineProperties.getRegexTimeout()).thenReturn(2);
    }

    @After
    public void cleanup(){

    }

    /**
     * A utility method to create an Evaluator and evaluate the Document field against a provided condition
     * */
    private ConditionEvaluationResult evaluate(RegexCondition condition) throws CpeException {
        ConditionEvaluator<RegexCondition> evaluator = new RegexEvaluator(contentExpressionHelper, apiProperties);
        return evaluator.evaluate(this.collectionSequence, this.document, condition, environmentSnapshot);
    }

    @Test
    public void testFailure() throws CpeException {
        RegexCondition notMatchingExpression = new RegexCondition();
        notMatchingExpression.id = (Helper.getId());
        notMatchingExpression.field = "missingfield";
        notMatchingExpression.value = "xyz";

        //Note the failure will always return the condition in the missing field conditions, field only added if the
        //expression evals to true
        ConditionEvaluationResult result = evaluate(notMatchingExpression);
        Assert.assertFalse(result.isMatch());
        Assert.assertTrue(result.getUnevaluatedConditions().size() == 1);
        Assert.assertTrue(ConditionKeyHelper.containsKey(result.getUnevaluatedConditions(),notMatchingExpression).isPresent());
    }

    @Test
    public void testRegexContentExpressionCondition() throws CpeException {
        RegexCondition regexCondition = new RegexCondition();
        regexCondition.field = ("field");
        regexCondition.value = (".*(hello|value).*");

        ConditionEvaluationResult result = evaluate(regexCondition);
        Assert.assertTrue(result.isMatch());
        Assert.assertTrue(result.getUnevaluatedConditions().size() == 0);
    }

    @Test
     public void testNotFoundRegexContentExpressionCondition() throws CpeException {
        RegexCondition regexCondition = new RegexCondition();
        regexCondition.field = ("field");
        regexCondition.value = ("values");

        ConditionEvaluationResult result = evaluate(regexCondition);
        Assert.assertFalse(result.isMatch());
        Assert.assertTrue(result.getUnevaluatedConditions().size() == 0);
    }
}
