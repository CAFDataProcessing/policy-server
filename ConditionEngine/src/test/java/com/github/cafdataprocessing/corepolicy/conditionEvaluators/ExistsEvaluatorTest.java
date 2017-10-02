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
import com.github.cafdataprocessing.corepolicy.common.dto.conditions.ExistsCondition;
import com.github.cafdataprocessing.corepolicy.common.exceptions.CpeException;
import com.github.cafdataprocessing.corepolicy.document.DocumentUnderEvaluation;
import com.github.cafdataprocessing.corepolicy.document.DocumentUnderEvaluationImpl;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Tests for the ExistsEvaluator
 */
@RunWith(MockitoJUnitRunner.class)
public class ExistsEvaluatorTest {
    @Mock
    CollectionSequence collectionSequence;

    @Mock
    private EnvironmentSnapshot environmentSnapshot;

    @Mock
    private ConditionEngineMetadata conditionEngineMetadata;

    @Mock
    private ApiProperties apiProperties;

    @Before
    public void setup() {

    }

    private DocumentUnderEvaluationImpl getDocumentUnderEvaluation() {
        DocumentUnderEvaluationImpl document = new DocumentUnderEvaluationImpl(conditionEngineMetadata, apiProperties);
        document.addMetadataString("FieldA", "AValue");
        document.addMetadataString("FieldA", "BValue");
        return document;
    }

    @After
    public void cleanup() {

    }

    /**
     * A utility method to create an Evaluator and evaluate the Document field against a provided condition
     */
    private ConditionEvaluationResult evaluate(ExistsCondition condition) throws CpeException {
        return evaluate( condition, null );
    }

    private ConditionEvaluationResult evaluate(ExistsCondition condition, DocumentUnderEvaluation document) throws CpeException {
        ConditionEvaluator<ExistsCondition> evaluator = new ExistsEvaluator(apiProperties);

        DocumentUnderEvaluation doc = document;

        if ( document == null ) {
            doc = getDocumentUnderEvaluation();
        }

        return evaluator.evaluate(this.collectionSequence, doc, condition, environmentSnapshot);
    }

    @Test
    public void testMatches() throws Exception {
        ExistsCondition fieldExistsCondition = new ExistsCondition();
        fieldExistsCondition.field = "FieldA";

        Assert.assertTrue(evaluate(fieldExistsCondition).isMatch());
    }

    @Test
    public void testIsNotMatches() throws Exception {
        ExistsCondition fieldExistsCondition = new ExistsCondition();
        fieldExistsCondition.field = "FieldB";

        Assert.assertFalse(evaluate(fieldExistsCondition).isMatch());
    }

    @Test
    public void testMissingFieldNotReported() throws Exception {
        ExistsCondition fieldExistsCondition = new ExistsCondition();
        fieldExistsCondition.field = "NonExistantField";

        ConditionEvaluationResult result = evaluate(fieldExistsCondition);

        Assert.assertFalse(result.isMatch());
        Assert.assertTrue(result.getUnevaluatedConditions().size() == 0);
    }
}
