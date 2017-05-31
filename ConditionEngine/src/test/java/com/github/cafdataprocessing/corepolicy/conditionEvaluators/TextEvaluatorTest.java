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

import com.github.cafdataprocessing.corepolicy.booleanagent.BooleanAgentQueryResultImpl;
import com.github.cafdataprocessing.corepolicy.booleanagent.BooleanAgentServices;
import com.github.cafdataprocessing.corepolicy.document.DocumentUnderEvaluation;
import com.github.cafdataprocessing.corepolicy.document.DocumentUnderEvaluationImpl;
import com.github.cafdataprocessing.corepolicy.Helper;
import com.github.cafdataprocessing.corepolicy.booleanagent.BooleanAgentQueryResult;
import com.github.cafdataprocessing.corepolicy.common.ApiProperties;
import com.github.cafdataprocessing.corepolicy.common.ConditionEngineMetadata;
import com.github.cafdataprocessing.corepolicy.common.EnvironmentSnapshot;
import com.github.cafdataprocessing.corepolicy.common.dto.CollectionSequence;
import com.github.cafdataprocessing.corepolicy.common.dto.conditions.TextCondition;
import com.github.cafdataprocessing.corepolicy.common.exceptions.CpeException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collection;
import java.util.regex.Pattern;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

/**
 * The tests here should be the same as the field exists expression, because that's what it's doing under the covers.
 * Because failed content expressions are evaluated externally, we can't actually check the eval here.
 */
@RunWith(MockitoJUnitRunner.class)
public class TextEvaluatorTest {
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
    private ConditionEngineMetadata conditionEngineMetadata;

    @Mock
    private ApiProperties apiProperties;

    @InjectMocks
    private ContentExpressionHelper contentExpressionHelper;

    @Before
    public void setup() throws CpeException {
        DocumentUnderEvaluationImpl document = new DocumentUnderEvaluationImpl(conditionEngineMetadata, apiProperties);
        document.addMetadataString("field", "some value hello hell");
        document.setFullMetadata(false);
        this.document = document;

        when(regexMatcherFactory.getPattern(anyString())).then(invocation -> {
            Object[] args = invocation.getArguments();
            return Pattern.compile((String) args[0]);
        });

        when(booleanAgentServices.getAvailable()).thenReturn(true);
    }

    @After
    public void cleanup(){

    }

    /**
     * A utility method to create an Evaluator and evaluate the Document field against a provided condition
     * */
    private ConditionEvaluationResult evaluate(TextCondition condition) throws CpeException {
        ConditionEvaluator<TextCondition> evaluator = new TextEvaluator(booleanAgentServices,
                contentExpressionHelper, apiProperties);
        return evaluator.evaluate(this.collectionSequence, this.document, condition, environmentSnapshot);
    }

    @Test
    public void testSuccess() throws CpeException {
        TextCondition matchingExpression = new TextCondition();
        matchingExpression.field = ("field");
        matchingExpression.id = (Helper.getId());
        matchingExpression.value = ("some expression");

        try {
            BooleanAgentQueryResult booleanAgentQueryResult = new BooleanAgentQueryResultImpl();
            booleanAgentQueryResult.getConditionIdTerms().put(matchingExpression.id,"SOME");
            booleanAgentQueryResult.getConditionIdTerms().put(matchingExpression.id,"EXPRESSION");

            when(booleanAgentServices.query(anyString(), any(Collection.class))).thenReturn(booleanAgentQueryResult);
        } catch (Exception e) {
            e.printStackTrace();
        }

        ConditionEvaluationResult result = evaluate(matchingExpression);
        Assert.assertTrue(result.isMatch());
        Assert.assertEquals(0, result.getUnevaluatedConditions().size());
    }

    @Test
    public void testUnevaluatedCondition() throws CpeException {
        TextCondition matchingExpression = new TextCondition();
        matchingExpression.field = ("missingfield");
        matchingExpression.id = (Helper.getId());
        matchingExpression.value = ("some expression");

        when(booleanAgentServices.getAvailable()).thenReturn(false);

        ConditionEvaluationResult result = evaluate(matchingExpression);
        Assert.assertFalse(result.isMatch());
        Assert.assertEquals(1, result.getUnevaluatedConditions().size());
    }

    @Test
    public void testFailure() throws CpeException {
        TextCondition notMatchingExpression = new TextCondition();
        notMatchingExpression.id = (Helper.getId());
        notMatchingExpression.field = "missingfield";

        //Note the failure will always return the condition in the missing field conditions, field only added if the
        //expression evals to true
        ConditionEvaluationResult result = evaluate(notMatchingExpression);
        Assert.assertFalse(result.isMatch());
        Assert.assertTrue(result.getUnevaluatedConditions().size() == 1);
        Assert.assertTrue(ConditionKeyHelper.containsKey(result.getUnevaluatedConditions(), notMatchingExpression).isPresent());
    }
}
