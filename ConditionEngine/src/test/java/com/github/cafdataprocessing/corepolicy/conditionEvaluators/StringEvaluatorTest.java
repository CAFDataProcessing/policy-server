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
import com.github.cafdataprocessing.corepolicy.common.ConditionEngineMetadata;
import com.github.cafdataprocessing.corepolicy.common.EnvironmentSnapshot;
import com.github.cafdataprocessing.corepolicy.common.dto.CollectionSequence;
import com.github.cafdataprocessing.corepolicy.common.dto.conditions.StringCondition;
import com.github.cafdataprocessing.corepolicy.common.dto.conditions.StringOperatorType;
import com.github.cafdataprocessing.corepolicy.common.exceptions.CpeException;
import com.github.cafdataprocessing.corepolicy.document.DocumentUnderEvaluation;
import com.github.cafdataprocessing.corepolicy.document.DocumentUnderEvaluationImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

@RunWith(MockitoJUnitRunner.class)
public class StringEvaluatorTest {

    @Mock
    CollectionSequence collectionSequence;

    @Mock
    private EnvironmentSnapshot environmentSnapshot;

    @Mock
    private ConditionEngineMetadata conditionEngineMetadata;

    @Mock
    private ApiProperties apiProperties;

    @Before
    public void setUp() throws Exception {

    }

    private DocumentUnderEvaluationImpl getDocumentUnderEvaluation() {
        DocumentUnderEvaluationImpl doc = new DocumentUnderEvaluationImpl(conditionEngineMetadata, apiProperties);

        doc.addMetadataString("FieldA", "AValue");
        doc.addMetadataString("FieldA", "BValue");

        doc.addMetadataStream("FieldB", getInputStream("AValue"));
        doc.addMetadataStream("FieldB", getInputStream("BValue"));

        return doc;
    }

    private static InputStream getInputStream(String testString) {
        return new ByteArrayInputStream(testString.getBytes());
    }

    /**
     * A utility method to create an Evaluator and evaluate the Document field against a provided condition
     */
    private ConditionEvaluationResult evaluate(StringCondition condition) throws CpeException {
        return evaluate(condition, null);
    }

    private ConditionEvaluationResult evaluate(StringCondition condition, DocumentUnderEvaluation document) throws CpeException {

        if (document == null) {
            document = getDocumentUnderEvaluation();
        }

        ConditionEvaluator<StringCondition> evaluator = new StringEvaluator(apiProperties);
        return evaluator.evaluate(this.collectionSequence, document, condition, environmentSnapshot);
    }

    @Test
    public void testIsExact() throws Exception {
        {
            StringCondition stringCondition = new StringCondition();
            stringCondition.operator = (StringOperatorType.IS);
            stringCondition.field = ("FieldA");
            stringCondition.value = ("AValue");

            Assert.assertTrue(evaluate(stringCondition).isMatch());
        }

        // Test on stream value.
        {
            StringCondition stringCondition = new StringCondition();
            stringCondition.operator = (StringOperatorType.IS);
            stringCondition.field = ("FieldB");
            stringCondition.value = ("AValue");

            Assert.assertTrue(evaluate(stringCondition).isMatch());
        }
    }

    @Test
    public void testIsExactCheckTerms() throws Exception {
        {
            StringCondition stringCondition = new StringCondition();
            stringCondition.operator = (StringOperatorType.IS);
            stringCondition.field = ("FieldA");
            stringCondition.value = ("AValue");

            ConditionEvaluationResult evaluateResult = evaluate(stringCondition);
            Assert.assertTrue(evaluateResult.isMatch());
            Assert.assertEquals(1, evaluateResult.getMatchedConditions().size());
            Assert.assertArrayEquals(new String[]{"AValue"}, evaluateResult.getMatchedConditions().stream().findFirst().get().getTerms().toArray());
        }
        // Test on stream value.
        {
            StringCondition stringCondition = new StringCondition();
            stringCondition.operator = (StringOperatorType.IS);
            stringCondition.field = ("FieldB");
            stringCondition.value = ("AValue");

            ConditionEvaluationResult evaluateResult = evaluate(stringCondition);
            Assert.assertTrue(evaluateResult.isMatch());
            Assert.assertEquals(1, evaluateResult.getMatchedConditions().size());
            Assert.assertArrayEquals(new String[]{"AValue"}, evaluateResult.getMatchedConditions().stream().findFirst().get().getTerms().toArray());
        }
    }

    @Test
    public void testIsNotTrueExact() throws Exception {
        {
            StringCondition stringCondition = new StringCondition();
            stringCondition.operator = (StringOperatorType.IS);
            stringCondition.field = ("FieldA");
            stringCondition.value = ("CValue");

            Assert.assertFalse(evaluate(stringCondition).isMatch());
        }

        // Test on stream value.
        {
            StringCondition stringCondition = new StringCondition();
            stringCondition.operator = (StringOperatorType.IS);
            stringCondition.field = ("FieldB");
            stringCondition.value = ("CValue");

            Assert.assertFalse(evaluate(stringCondition).isMatch());
        }
    }

    @Test
    public void testIsStartsWith() throws Exception {
        {
            StringCondition stringCondition = new StringCondition();

            stringCondition.operator = (StringOperatorType.STARTS_WITH);
            stringCondition.field = ("FieldA");
            stringCondition.value = ("AVa");

            ConditionEvaluationResult evaluate = evaluate(stringCondition);
            Assert.assertTrue(evaluate.isMatch());
            Assert.assertEquals(1, evaluate.getMatchedConditions().size());
            Assert.assertArrayEquals(new String[]{"AVa"}, evaluate.getMatchedConditions().stream().findFirst().get().getTerms().toArray());
        }

        // Test on stream value.
        {
            StringCondition stringCondition = new StringCondition();
            stringCondition.operator = (StringOperatorType.STARTS_WITH);
            stringCondition.field = ("FieldB");
            stringCondition.value = ("AVa");

            ConditionEvaluationResult evaluate = evaluate(stringCondition);
            Assert.assertTrue(evaluate.isMatch());
            Assert.assertEquals(1, evaluate.getMatchedConditions().size());
            Assert.assertArrayEquals(new String[]{"AVa"}, evaluate.getMatchedConditions().stream().findFirst().get().getTerms().toArray());
        }
    }

    @Test
    public void testIsStartsWithFails() throws Exception {
        {
            StringCondition stringCondition = new StringCondition();
            stringCondition.operator = (StringOperatorType.STARTS_WITH);
            stringCondition.field = ("FieldA");
            stringCondition.value = ("CVa");

            Assert.assertFalse(evaluate(stringCondition).isMatch());
        }

        // Test on stream value.
        {
            StringCondition stringCondition = new StringCondition();
            stringCondition.operator = (StringOperatorType.STARTS_WITH);
            stringCondition.field = ("FieldB");
            stringCondition.value = ("CVa");

            Assert.assertFalse(evaluate(stringCondition).isMatch());
        }
    }

    @Test
    public void testIsEndsWith() throws Exception {
        {
            StringCondition stringCondition = new StringCondition();

            stringCondition.operator = (StringOperatorType.ENDS_WITH);
            stringCondition.field = ("FieldA");
            stringCondition.value = ("lue");

            Assert.assertTrue(evaluate(stringCondition).isMatch());
        }

        // Test on stream value.
        {
            StringCondition stringCondition = new StringCondition();

            stringCondition.operator = (StringOperatorType.ENDS_WITH);
            stringCondition.field = ("FieldB");
            stringCondition.value = ("lue");

            Assert.assertTrue(evaluate(stringCondition).isMatch());
        }
    }

    @Test
    public void testIsEndsWithFails() throws Exception {
        {
            StringCondition stringCondition = new StringCondition();
            stringCondition.operator = (StringOperatorType.ENDS_WITH);
            stringCondition.field = ("FieldA");
            stringCondition.value = ("CVa");

            Assert.assertFalse(evaluate(stringCondition).isMatch());
        }
        // Test on stream value.
        {
            StringCondition stringCondition = new StringCondition();
            stringCondition.operator = (StringOperatorType.ENDS_WITH);
            stringCondition.field = ("FieldB");
            stringCondition.value = ("CVa");

            Assert.assertFalse(evaluate(stringCondition).isMatch());
        }
    }

    @Test
    public void testIsAny() throws Exception {

        {
            StringCondition stringCondition = new StringCondition();
            stringCondition.operator = (StringOperatorType.CONTAINS);
            stringCondition.field = ("FieldA");
            stringCondition.value = ("alu");

            Assert.assertTrue(evaluate(stringCondition).isMatch());
        }
        // Test on stream value.
        {
            StringCondition stringCondition = new StringCondition();
            stringCondition.operator = (StringOperatorType.CONTAINS);
            stringCondition.field = ("FieldB");
            stringCondition.value = ("alu");

            Assert.assertTrue(evaluate(stringCondition).isMatch());
        }
    }

    @Test
    public void testIsAnyFails() throws Exception {

        {
            StringCondition stringCondition = new StringCondition();
            stringCondition.operator = (StringOperatorType.CONTAINS);
            stringCondition.field = ("FieldA");
            stringCondition.value = ("CiVa");

            Assert.assertFalse(evaluate(stringCondition).isMatch());
        }
        // Test on stream value.
        {
            StringCondition stringCondition = new StringCondition();
            stringCondition.operator = (StringOperatorType.CONTAINS);
            stringCondition.field = ("FieldA");
            stringCondition.value = ("CiVa");

            Assert.assertFalse(evaluate(stringCondition).isMatch());
        }
    }

    @Test
    public void testIsAnyCaseInsensitive() throws Exception {
        {
            StringCondition stringCondition = new StringCondition();
            stringCondition.operator = (StringOperatorType.CONTAINS);
            stringCondition.field = ("FieldA");
            stringCondition.value = ("ALU");

            Assert.assertTrue(evaluate(stringCondition).isMatch());
        }
        // Test on stream value.
        {
            StringCondition stringCondition = new StringCondition();
            stringCondition.operator = (StringOperatorType.CONTAINS);
            stringCondition.field = ("FieldB");
            stringCondition.value = ("ALU");

            Assert.assertTrue(evaluate(stringCondition).isMatch());
        }
    }

    @Test
    public void testFailFieldExistsConditionNotReported() throws Exception {
        {
            StringCondition stringCondition = new StringCondition();
            stringCondition.operator = (StringOperatorType.CONTAINS);
            stringCondition.field = ("FieldA");
            stringCondition.value = ("CiVa");

            Assert.assertFalse(evaluate(stringCondition).isMatch());
            Assert.assertEquals(0, evaluate(stringCondition).getUnevaluatedConditions().size());
        }
        // Test on stream value.
        {
            StringCondition stringCondition = new StringCondition();
            stringCondition.operator = (StringOperatorType.CONTAINS);
            stringCondition.field = ("FieldB");
            stringCondition.value = ("CiVa");

            Assert.assertFalse(evaluate(stringCondition).isMatch());
            Assert.assertEquals(0, evaluate(stringCondition).getUnevaluatedConditions().size());
        }

    }

    @Test
    public void testFailNoFieldConditionReportedAsUnevaluatedPreKV() throws Exception {

        StringCondition stringCondition = new StringCondition();
        stringCondition.operator = (StringOperatorType.CONTAINS);
        stringCondition.field = ("MissingField");
        stringCondition.value = ("CiVa");

        ConditionEvaluationResult result;
        DocumentUnderEvaluation document = getDocumentUnderEvaluation();
        document.setFullMetadata(false);

        result = evaluate(stringCondition, document);

        Assert.assertFalse(result.isMatch());

        Assert.assertTrue(ConditionKeyHelper.containsKey(result.getUnevaluatedConditions(), stringCondition).isPresent());

    }


    @Test
    public void testFailNoFieldConditionReportedUnmatched() throws Exception {
        StringCondition stringCondition = new StringCondition();
        stringCondition.operator = (StringOperatorType.CONTAINS);
        stringCondition.field = ("MissingField");
        stringCondition.value = ("CiVa");

        Assert.assertFalse(evaluate(stringCondition).isMatch());

        Assert.assertTrue(ConditionKeyHelper.containsKey(evaluate(stringCondition).getUnmatchedConditions(), stringCondition).isPresent());
    }

}