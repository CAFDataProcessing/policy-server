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
import com.github.cafdataprocessing.corepolicy.common.DocumentFields;
import com.github.cafdataprocessing.corepolicy.common.EnvironmentSnapshot;
import com.github.cafdataprocessing.corepolicy.common.dto.CollectionSequence;
import com.github.cafdataprocessing.corepolicy.common.dto.MatchedCondition;
import com.github.cafdataprocessing.corepolicy.common.dto.conditions.*;
import com.github.cafdataprocessing.corepolicy.common.exceptions.CpeException;
import com.github.cafdataprocessing.corepolicy.document.DocumentUnderEvaluation;
import com.github.cafdataprocessing.corepolicy.document.DocumentUnderEvaluationImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ConditionEvaluatorBaseTest {

    @Mock
    CollectionSequence collectionSequence;

    @Mock
    EnvironmentSnapshot environmentSnapshot;

    @Mock
    Condition condition;

    @Mock
    ApiProperties apiProperties;

    @Mock
    private ConditionEngineMetadata conditionEngineMetadata;

    DocumentUnderEvaluation rootDocument;
    DocumentUnderEvaluation firstLevelFirstDocument;
    DocumentUnderEvaluation firstLevelSecondDocument;
    DocumentUnderEvaluation secondLevelFirstDocument;

    ConditionEvaluatorBase conditionEvaluator;

    @Before
    public void before(){
        resetSut();
        resetDocuments();
        collectionSequence.fullConditionEvaluation = false;
    }

    void resetDocuments(){
        rootDocument = new DocumentUnderEvaluationImpl(conditionEngineMetadata, apiProperties);
        rootDocument.addMetadataString( DocumentFields.ChildDocumentDepth, "0");
        rootDocument.setReference("root");

        firstLevelFirstDocument = new DocumentUnderEvaluationImpl(conditionEngineMetadata, apiProperties);
        firstLevelFirstDocument.addMetadataString(DocumentFields.ChildDocumentDepth, "1");
        firstLevelFirstDocument.setReference("firstLevelFirstDocument");

        firstLevelSecondDocument = new DocumentUnderEvaluationImpl(conditionEngineMetadata, apiProperties);
        firstLevelSecondDocument.addMetadataString(DocumentFields.ChildDocumentDepth, "1");
        firstLevelSecondDocument.setReference("firstLevelSecondDocument");

        secondLevelFirstDocument = new DocumentUnderEvaluationImpl(conditionEngineMetadata, apiProperties);
        secondLevelFirstDocument.addMetadataString(DocumentFields.ChildDocumentDepth, "2");
        secondLevelFirstDocument.setReference("secondLevelFirstDocument");

        rootDocument.getDocuments().add(firstLevelFirstDocument);
        rootDocument.getDocuments().add(firstLevelSecondDocument);

        firstLevelFirstDocument.getDocuments().add(secondLevelFirstDocument);
    }

    void resetSut(){
        conditionEvaluator = mock(ConditionEvaluatorBase.class, Mockito.CALLS_REAL_METHODS);
    }

    @Test
    public void testContainerOnlyEvaluate() throws Exception {
        condition.target = (ConditionTarget.CONTAINER);
        condition.includeDescendants = (true);

        configureResultForDocument(rootDocument, true);
        configureResultForDocument(firstLevelFirstDocument, false);

        ConditionEvaluationResult conditionEvaluationResult = conditionEvaluator.evaluate(this.collectionSequence, rootDocument,
                condition, environmentSnapshot);

        assertTrue(conditionEvaluationResult.isMatch());
        assertEquals(1, conditionEvaluationResult.getMatchedConditions().size());
    }

    @Test
    public void testFalseContainerOnlyEvaluate() throws Exception {
        condition.target = (ConditionTarget.CONTAINER);
        condition.includeDescendants = (true);

        configureResultForDocument(rootDocument, false);
        configureResultForDocument(firstLevelFirstDocument, true);

        ConditionEvaluationResult conditionEvaluationResult = conditionEvaluator.evaluate(this.collectionSequence, rootDocument,
                condition, environmentSnapshot);

        assertFalse(conditionEvaluationResult.isMatch());
        assertEquals(0, conditionEvaluationResult.getMatchedConditions().size());
    }

    @Test
    public void testChildrenOnlyEvaluate() throws Exception {

        condition.target = (ConditionTarget.CHILDREN);
        condition.includeDescendants = (true);

        configureResultForDocument(rootDocument, true);
        configureResultForDocument(firstLevelFirstDocument, false);
        configureResultForDocument(firstLevelSecondDocument, false);
        configureResultForDocument(secondLevelFirstDocument, false);

        ConditionEvaluationResult conditionEvaluationResult = conditionEvaluator.evaluate(this.collectionSequence, rootDocument,
                condition, environmentSnapshot);

        assertFalse(conditionEvaluationResult.isMatch());
        assertEquals(0, conditionEvaluationResult.getMatchedConditions().size());
    }

    @Test
    public void testAllEvaluate() throws Exception {

        condition.target = (ConditionTarget.ALL);
        condition.includeDescendants = (true);

        resetSut();
        resetDocuments();

        configureResultForDocument(rootDocument, true);
        configureResultForDocument(firstLevelFirstDocument, true);
        configureResultForDocument(firstLevelSecondDocument, true);
        configureResultForDocument(secondLevelFirstDocument, true);

        ConditionEvaluationResult conditionEvaluationResult = conditionEvaluator.evaluate(this.collectionSequence, rootDocument,
                condition, environmentSnapshot);

        assertTrue(conditionEvaluationResult.isMatch());
        assertEquals(4, conditionEvaluationResult.getMatchedConditions().size());
        assertEquals(4, conditionEvaluationResult.getAllConditionMatches().size());
    }

    @Test
    public void testNotDescendantsEvaluate() throws Exception {

        condition.target = (ConditionTarget.ALL);
        condition.includeDescendants = (false);

        configureResultForDocument(rootDocument, true);
        configureResultForDocument(firstLevelFirstDocument, true);
        configureResultForDocument(firstLevelSecondDocument, true);
        configureResultForDocument(secondLevelFirstDocument, false);

        ConditionEvaluationResult conditionEvaluationResult = conditionEvaluator.evaluate(this.collectionSequence, rootDocument,
                condition, environmentSnapshot);

        assertTrue(conditionEvaluationResult.isMatch());
        assertEquals(3, conditionEvaluationResult.getMatchedConditions().size());

    }

    @Test
    public void testAllMatchEvaluate() throws Exception {

        condition.target = (ConditionTarget.ALL);
        condition.includeDescendants = (true);

        configureResultForDocument(rootDocument, true);
        configureResultForDocument(firstLevelFirstDocument, true);
        configureResultForDocument(firstLevelSecondDocument, true);
        configureResultForDocument(secondLevelFirstDocument, true);

        ConditionEvaluationResult conditionEvaluationResult = conditionEvaluator.evaluate(this.collectionSequence, rootDocument,
                condition, environmentSnapshot);
        assertTrue(conditionEvaluationResult.isMatch());
        assertEquals(4, conditionEvaluationResult.getMatchedConditions().size());
        assertEquals(4, conditionEvaluationResult.getAllConditionMatches().size());

    }

    @Test
    public void testExcludedRootEvaluate() throws Exception {

        condition.target = (ConditionTarget.ALL);
        condition.includeDescendants = (false);

        rootDocument.setIsExcluded(true);
        configureResultForDocument(rootDocument, false);
        configureResultForDocument(firstLevelFirstDocument, false);
        configureResultForDocument(firstLevelSecondDocument, false);
        configureResultForDocument(secondLevelFirstDocument, false);

        ConditionEvaluationResult conditionEvaluationResult = conditionEvaluator.evaluate(this.collectionSequence, rootDocument,
                condition, environmentSnapshot);

        assertFalse(conditionEvaluationResult.isMatch());
        assertEquals(0, conditionEvaluationResult.getMatchedConditions().size());
        assertEquals(0, conditionEvaluationResult.getAllConditionMatches().size());
    }

    @Test
    public void testExcludedFirstLevelEvaluate() throws Exception {

        condition.target = (ConditionTarget.ALL);
        condition.includeDescendants = (false);

        firstLevelFirstDocument.setIsExcluded(true);

        configureResultForDocument(rootDocument, true);
        configureResultForDocument(firstLevelFirstDocument, true);
        configureResultForDocument(firstLevelSecondDocument, true);
        configureResultForDocument(secondLevelFirstDocument, false);

        ConditionEvaluationResult conditionEvaluationResult = conditionEvaluator.evaluate(this.collectionSequence, rootDocument,
                condition, environmentSnapshot);

        assertTrue(conditionEvaluationResult.isMatch());
        assertEquals(2, conditionEvaluationResult.getMatchedConditions().size());
        assertEquals(2, conditionEvaluationResult.getAllConditionMatches().size());
    }

    @Test
    public void testFalseExcludedFirstLevelEvaluate() throws Exception {

        condition.target = (ConditionTarget.ALL);
        condition.includeDescendants = (false);

        firstLevelFirstDocument.setIsExcluded(true);

        configureResultForDocument(rootDocument, true);
        configureResultForDocument(firstLevelFirstDocument, true);
        configureResultForDocument(firstLevelSecondDocument, false);
        configureResultForDocument(secondLevelFirstDocument, false);

        ConditionEvaluationResult conditionEvaluationResult = conditionEvaluator.evaluate(this.collectionSequence, rootDocument,
                condition, environmentSnapshot);

        assertTrue(conditionEvaluationResult.isMatch());
        assertEquals(1, conditionEvaluationResult.getMatchedConditions().size());
        assertEquals(1, conditionEvaluationResult.getAllConditionMatches().size());
    }

    @Test
    public void testNestedConditionForChildren() throws CpeException {

        DocumentUnderEvaluation rootDocument = new DocumentUnderEvaluationImpl(conditionEngineMetadata, apiProperties);
        rootDocument.setReference("root");

        DocumentUnderEvaluation firstChildDocument = new DocumentUnderEvaluationImpl(conditionEngineMetadata, apiProperties);
        firstChildDocument.setReference("first child");

        rootDocument.getDocuments().add(firstChildDocument);

        DocumentUnderEvaluation secondChildDocument = new DocumentUnderEvaluationImpl(conditionEngineMetadata, apiProperties);
        secondChildDocument.setReference("second child");
        secondChildDocument.addMetadataString("FIELDNAME", "Hello");

        firstChildDocument.getDocuments().add(secondChildDocument);

        StringCondition stringFieldCondition = mock(StringCondition.class);
        stringFieldCondition.id = (2L);
        stringFieldCondition.target = ConditionTarget.CHILDREN;
        stringFieldCondition.field = ("FIELDNAME");
        stringFieldCondition.includeDescendants = false;
        stringFieldCondition.operator= (StringOperatorType.CONTAINS);
        stringFieldCondition.value = ("Hello");

        when(environmentSnapshot.getCondition(eq(2L))).thenReturn(stringFieldCondition);

        BooleanCondition booleanCondition = mock(BooleanCondition.class);

        booleanCondition.id = (1L);
        booleanCondition.target = (ConditionTarget.CHILDREN);
        booleanCondition.operator = (BooleanOperator.OR);
        booleanCondition.includeDescendants = false;
        booleanCondition.children = (Arrays.asList((Condition)stringFieldCondition));

        EvaluateCondition evaluateCondition = mock(EvaluateCondition.class);
        BooleanConditionEvaluator booleanEvaluator = new BooleanConditionEvaluator(evaluateCondition);
        StringEvaluator stringEvaluator = new StringEvaluator(mock(ApiProperties.class));

        when(evaluateCondition.evaluate(any(CollectionSequence.class), any(DocumentUnderEvaluation.class), eq(booleanCondition), any(EnvironmentSnapshot.class))).thenAnswer(a ->
                {
                    Object[] arguments = a.getArguments();
                    return booleanEvaluator.evaluate(this.collectionSequence, (DocumentUnderEvaluation)arguments[0], (BooleanCondition)arguments[1], environmentSnapshot);
                }
        );

        when(evaluateCondition.evaluate(any(CollectionSequence.class), any(DocumentUnderEvaluation.class), eq(stringFieldCondition), any(EnvironmentSnapshot.class))).thenAnswer(a ->
                {
                    Object[] arguments = a.getArguments();
                    return stringEvaluator.evaluate(this.collectionSequence, (DocumentUnderEvaluation)arguments[1], (StringCondition)arguments[2], environmentSnapshot);
                }
        );

        ConditionEvaluationResult resultForCondition = booleanEvaluator.evaluate(this.collectionSequence, rootDocument, booleanCondition, environmentSnapshot);

        Assert.assertTrue(resultForCondition.isMatch());
        Assert.assertEquals(2, resultForCondition.getMatchedConditions().size());
        assertEquals(2, resultForCondition.getAllConditionMatches().size());
    }

    private void configureResultForDocument(DocumentUnderEvaluation documentUnderEvaluation, Boolean result) throws CpeException {
        doAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            ConditionEvaluationResult conditionEvaluationResult = (ConditionEvaluationResult) args[1];
            conditionEvaluationResult.setMatch(conditionEvaluationResult.isMatch() || result);
            MatchedCondition matchedCondition = new MatchedCondition(documentUnderEvaluation.getReference(), condition);

            if(result) {
                conditionEvaluationResult.getAllConditionMatches().add(matchedCondition);
                conditionEvaluationResult.getMatchedConditions().add(matchedCondition);
            }

            return null;
        }).when(conditionEvaluator).evaluate(any(CollectionSequence.class), any(ConditionEvaluationResult.class),
                eq(documentUnderEvaluation), eq(condition), any(EnvironmentSnapshot.class));

    }
}
