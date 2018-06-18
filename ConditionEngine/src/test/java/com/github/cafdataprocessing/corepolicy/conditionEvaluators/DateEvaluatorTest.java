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
import com.github.cafdataprocessing.corepolicy.document.DocumentUnderEvaluation;
import com.github.cafdataprocessing.corepolicy.document.DocumentUnderEvaluationImpl;
import com.github.cafdataprocessing.corepolicy.common.ApiProperties;
import com.github.cafdataprocessing.corepolicy.common.ConditionEngineMetadata;
import com.github.cafdataprocessing.corepolicy.common.EnvironmentSnapshot;
import com.github.cafdataprocessing.corepolicy.common.dto.CollectionSequence;
import com.github.cafdataprocessing.corepolicy.common.dto.conditions.DateCondition;
import com.github.cafdataprocessing.corepolicy.common.dto.conditions.DateOperator;
import com.github.cafdataprocessing.corepolicy.common.exceptions.CpeException;
import com.github.cafdataprocessing.corepolicy.common.fields.EpochTime;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeZone;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.Period;

/**
 *
 * Tests for the DateEvaluator.
 */
@RunWith(MockitoJUnitRunner.class)
public class DateEvaluatorTest {
    @Mock
    CollectionSequence collectionSequence;

    @Mock
    private ApiProperties apiProperties;

    @Mock
    private BooleanAgentServices booleanAgentServices;

    @Mock
    EnvironmentSnapshot environmentSnapshot;

    @Mock
    ConditionEngineMetadata conditionEngineMetadata;

    private static DateTime getFixedDate() {
        return new DateTime(1999, 12, 31, 22, 59, 59, DateTimeZone.UTC);
    }

    private static String toString(DateTime dateTime) {
        return dateTime.toString();
    }

    @Before
    public void setup() {


    }

    @After
    public void cleanup() {

    }

    private DocumentUnderEvaluation getDocument()
    {
        //Set up document with dates relative to 1999-12-31T22:59:59.000Z
        // 1 fields epoch format
        // 1 field representing 10 years earlier in epoch format
        // 1 field representing 10 years later in epoch format

        DateTime fixedTime = getFixedDate();
        DocumentUnderEvaluationImpl doc = new DocumentUnderEvaluationImpl(conditionEngineMetadata, apiProperties);
        new EpochTime(fixedTime).getSeconds();

        doc.addMetadataString("EpochField", Long.toString(new EpochTime(fixedTime).getSeconds()));

        doc.addMetadataString("EpochFieldLater", Long.toString(
                new EpochTime(fixedTime.plusYears(10)).getSeconds()));

        doc.addMetadataString("EpochFieldEarlier", Long.toString(
                new EpochTime(fixedTime.minusYears(10)).getSeconds()));

        doc.addMetadataString("Field10YearsAgo", DateTime.now().minusYears(10).toString());


        doc.addMetadataString("Monday", DateTime.now(DateTimeZone.UTC).withDayOfWeek(DateTimeConstants.MONDAY).toString());


        doc.addMetadataString("Friday", DateTime.now(DateTimeZone.UTC).withDayOfWeek(DateTimeConstants.FRIDAY).toString());

        return doc;
    }

    /**
     * A utility method to create an Evaluator and evaluate the Document field against a provided condition
     */
    private ConditionEvaluationResult evaluate(DateCondition condition) throws CpeException {
        ConditionEvaluator<DateCondition> evaluator = new DateEvaluator(apiProperties);

        DocumentUnderEvaluation documentUnderEvaluation = getDocument();

        return evaluator.evaluate(this.collectionSequence, documentUnderEvaluation, condition, environmentSnapshot);
    }

    @Test
    public void equalsOperator() throws CpeException {
        DateCondition condition = new DateCondition();
        condition.field = ("EpochField");
        condition.operator = (DateOperator.ON);
        condition.value = (toString(getFixedDate()));

        Assert.assertTrue(evaluate(condition).isMatch());
    }

    @Test
    public void equalsOperatorFailBefore() throws CpeException {
        DateCondition condition = new DateCondition();
        condition.field = ("EpochField");
        condition.operator = (DateOperator.ON);
        condition.value = (toString(getFixedDate().minusYears(1)));

        Assert.assertFalse(evaluate(condition).isMatch());
    }

    @Test
    public void equalsOperatorFailAfter() throws CpeException {
        DateCondition condition = new DateCondition();
        condition.field = ("EpochField");
        condition.operator = (DateOperator.ON);
        condition.value = (toString(getFixedDate().plusYears(1)));

        Assert.assertFalse(evaluate(condition).isMatch());
    }

    @Test
    public void beforeOperatorPass() throws CpeException {
        DateCondition condition = new DateCondition();
        condition.field = ("EpochFieldEarlier");
        condition.operator = (DateOperator.BEFORE);
        condition.value = (toString(getFixedDate()));

        Assert.assertTrue(evaluate(condition).isMatch());
    }

    @Test
    public void beforeOperatorFailEqual() throws CpeException {
        DateCondition condition = new DateCondition();
        condition.field = ("EpochField");
        condition.operator = (DateOperator.BEFORE);
        condition.value = (toString(getFixedDate()));

        Assert.assertFalse(evaluate(condition).isMatch());
    }

    @Test
    public void beforeOperatorFailAfter() throws CpeException {
        DateCondition condition = new DateCondition();
        condition.field = ("EpochFieldLater");
        condition.operator = (DateOperator.BEFORE);
        condition.value = (toString(getFixedDate()));

        Assert.assertFalse(evaluate(condition).isMatch());
    }

    @Test
    public void afterOperatorPass() throws CpeException {
        DateCondition condition = new DateCondition();
        condition.field = ("EpochFieldLater");
        condition.operator = (DateOperator.AFTER);
        condition.value = (toString(getFixedDate()));

        ConditionEvaluationResult result = evaluate(condition);
        Assert.assertTrue("Match should be true", result.isMatch());
        Assert.assertEquals("Should contain no unmatched conditions.", result.getUnmatchedConditions().size(), 0);
        Assert.assertEquals("Should contain no unevaluated conditions.", result.getUnevaluatedConditions().size(), 0);
    }

    @Test
    public void afterOperatorFailEqual() throws CpeException {
        DateCondition condition = new DateCondition();
        condition.field = ("EpochField");
        condition.operator = (DateOperator.AFTER);
        condition.value = (toString(getFixedDate()));

        ConditionEvaluationResult result = evaluate(condition);
        Assert.assertFalse("Match should be FALSE", result.isMatch());
        Assert.assertTrue("Should contain this as unmatched condition.", ConditionKeyHelper.containsKey(result.getUnmatchedConditions(),condition).isPresent());
        Assert.assertEquals("Should contain no unevaluated conditions.", result.getUnevaluatedConditions().size(), 0);
    }

    @Test
    public void afterOperatorFailBefore() throws CpeException {
        DateCondition condition = new DateCondition();
        condition.field = ("EpochFieldEarlier");
        condition.operator = (DateOperator.AFTER);
        condition.value = (toString(getFixedDate()));


        ConditionEvaluationResult result = evaluate(condition);
        Assert.assertFalse("Match should be FALSE", result.isMatch());
        Assert.assertTrue("Should contain this as unmatched condition.", ConditionKeyHelper.containsKey(result.getUnmatchedConditions(),condition).isPresent());
        Assert.assertEquals("Should contain no unevaluated conditions.", result.getUnevaluatedConditions().size(), 0);
    }

    @Test
    public void afterOperatorFailFieldBeforeFieldNotExists() throws CpeException {
        DateCondition condition = new DateCondition();
        condition.field = ("MissingField");
        condition.operator = (DateOperator.AFTER);
        condition.value = (toString(getFixedDate()));

        ConditionEvaluationResult result = evaluate(condition);
        Assert.assertFalse("Match should be FALSE", result.isMatch());
        Assert.assertTrue("Should contain this as unmatched condition.", ConditionKeyHelper.containsKey(result.getUnmatchedConditions(),condition).isPresent());
        Assert.assertEquals("Should contain no unevaluated conditions.", result.getUnevaluatedConditions().size(), 0);
    }

    @Test
    public void afterOperatorFailBeforeFieldExists() throws CpeException {
        DateCondition condition = new DateCondition();
        condition.field = ("EpochFieldEarlier");
        condition.operator = (DateOperator.AFTER);
        condition.value = (toString(getFixedDate()));

        ConditionEvaluationResult result = evaluate(condition);
        Assert.assertFalse("Match should be FALSE", result.isMatch());
        Assert.assertTrue("Should contain this as unmatched condition.", ConditionKeyHelper.containsKey(result.getUnmatchedConditions(),condition).isPresent());
        Assert.assertEquals("Should contain no unevaluated conditions.", result.getUnevaluatedConditions().size(), 0);
    }

    @Test
    public void TestPeriodMatch() throws CpeException {
        {
            DateCondition condition = new DateCondition();
            condition.field = ("EpochFieldEarlier");
            condition.operator = (DateOperator.AFTER);
            condition.value = (Period.of(1, 0, 0).toString());

            Assert.assertFalse(evaluate(condition).isMatch());
        }
        {
            DateCondition condition = new DateCondition();
            condition.field = ("EpochFieldEarlier");
            condition.operator = (DateOperator.BEFORE);
            condition.value = (Period.of(1, 0, 0).toString());

            Assert.assertTrue(evaluate(condition).isMatch());
        }
    }

    @Test
    public void beforeOperatorTestPeriodMatchOld() throws CpeException {
        {
            //Check that 10 years ago was before 9 years ago
            DateCondition condition = new DateCondition();
            condition.field = ("Field10YearsAgo");
            condition.operator = (DateOperator.BEFORE);
            condition.value = (Period.of(9, 0, 0).toString());

            Assert.assertTrue(evaluate(condition).isMatch());
        }
        {
            //Check that 10 years ago was not before 11 years ago
            DateCondition condition = new DateCondition();
            condition.field = ("Field10YearsAgo");
            condition.operator = (DateOperator.BEFORE);
            condition.value = (Period.of(11, 0, 0).toString());

            Assert.assertFalse(evaluate(condition).isMatch());
        }
    }

    @Test
    public void afterOperatorTestPeriodMatchOld() throws CpeException {
        {
            //Check that 10 years ago was not after 9 years ago
            DateCondition condition = new DateCondition();
            condition.field = ("Field10YearsAgo");
            condition.operator = (DateOperator.AFTER);
            condition.value = (Period.of(9, 0, 0).toString());

            Assert.assertFalse(evaluate(condition).isMatch());
        }
        {
            //Check that 10 years ago was after 11 years ago
            DateCondition condition = new DateCondition();
            condition.field = ("Field10YearsAgo");
            condition.operator = (DateOperator.AFTER);
            condition.value = (Period.of(11, 0, 0).toString());

            Assert.assertTrue(evaluate(condition).isMatch());
        }
    }

    @Test
    public void testTimeAfter() throws CpeException {
        {
            DateCondition condition = new DateCondition();
            condition.field = ("EpochField");
            condition.operator = (DateOperator.AFTER);
            condition.value = ("22:00");

            Assert.assertTrue(evaluate(condition).isMatch());
        }
    }

    @Test
    public void testTimeBefore() throws CpeException {
        {
            DateCondition condition = new DateCondition();
            condition.field = ("EpochField");
            condition.operator = (DateOperator.BEFORE);
            condition.value = ("23:00");

            Assert.assertTrue(evaluate(condition).isMatch());
        }
    }

    @Test
    public void testTimeOn() throws CpeException {
        {
            DateCondition condition = new DateCondition();
            condition.field = ("EpochField");
            condition.operator = (DateOperator.ON);
            condition.value = ("22:59:59");

            Assert.assertTrue(evaluate(condition).isMatch());
        }

        {
            DateCondition condition = new DateCondition();
            condition.field = ("EpochField");
            condition.operator = (DateOperator.ON);
            condition.value = ("22:59:58");

            Assert.assertFalse(evaluate(condition).isMatch());
        }
    }

    @Test
    public void testDayTimeOn() throws CpeException {
        {

            DateCondition condition = new DateCondition();
            condition.field = ("Monday");
            condition.operator = (DateOperator.ON);
            condition.value = ("Mon");

            Assert.assertTrue(evaluate(condition).isMatch());
        }

        {

            DateCondition condition = new DateCondition();
            condition.field = ("Monday");
            condition.operator = (DateOperator.AFTER);
            condition.value = ("Mon");

            Assert.assertFalse(evaluate(condition).isMatch());
        }

        {

            DateCondition condition = new DateCondition();
            condition.field = ("Monday");
            condition.operator = (DateOperator.BEFORE);
            condition.value = ("Mon");

            Assert.assertFalse(evaluate(condition).isMatch());
        }
    }

    @Test
    public void testDayTimeAfter() throws CpeException {
        //Assert that friday is after tuesday
        DateCondition condition = new DateCondition();
        condition.field = ("Friday");
        condition.operator = (DateOperator.AFTER);
        condition.value = ("Tuesday");

        Assert.assertTrue(evaluate(condition).isMatch());
    }

    @Test
    public void testDayTimeBefore() throws CpeException {
        //Assert that friday is after tuesday
        DateCondition condition = new DateCondition();
        condition.field = ("Monday");
        condition.operator = (DateOperator.BEFORE);
        condition.value = ("Tuesday");

        Assert.assertTrue(evaluate(condition).isMatch());
    }

    @Test
    public void testInvalidDay() throws CpeException {
        //Assert that friday is after tuesday
        DateCondition condition = new DateCondition();
        condition.field = ("Monday");
        condition.operator = (DateOperator.BEFORE);
        condition.value = ("Tuesday,T");

        Assert.assertFalse(evaluate(condition).isMatch());
    }

    @Test
    public void testTimezone(){
        DateTime dt1 = new DateTime();
        DateTime dt2 = new DateTime(dt1.getMillis(), DateTimeZone.forOffsetHours(-8));

        Assert.assertEquals(dt1.getMillis(), dt2.getMillis());
    }

}
