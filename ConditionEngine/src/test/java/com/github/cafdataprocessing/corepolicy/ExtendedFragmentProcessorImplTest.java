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
package com.github.cafdataprocessing.corepolicy;

import com.github.cafdataprocessing.corepolicy.common.ApiProperties;
import com.github.cafdataprocessing.corepolicy.common.ConditionEngineMetadata;
import com.github.cafdataprocessing.corepolicy.common.EnvironmentSnapshot;
import com.github.cafdataprocessing.corepolicy.common.dto.CollectionSequence;
import com.github.cafdataprocessing.corepolicy.common.dto.ExcludedFragment;
import com.github.cafdataprocessing.corepolicy.common.dto.MatchedCondition;
import com.github.cafdataprocessing.corepolicy.common.dto.conditions.Condition;
import com.github.cafdataprocessing.corepolicy.common.dto.conditions.ConditionTarget;
import com.github.cafdataprocessing.corepolicy.common.exceptions.CpeException;
import com.github.cafdataprocessing.corepolicy.conditionEvaluators.ConditionEvaluationResult;
import com.github.cafdataprocessing.corepolicy.conditionEvaluators.EvaluateCondition;
import com.github.cafdataprocessing.corepolicy.document.DocumentUnderEvaluation;
import com.github.cafdataprocessing.corepolicy.document.DocumentUnderEvaluationImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.UUID;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for the ExtendedFragmentProcessorImpl class
 */
@RunWith(MockitoJUnitRunner.class)
public class ExtendedFragmentProcessorImplTest {

    DocumentUnderEvaluationImpl document;
    DocumentUnderEvaluationImpl subDocument1;
    DocumentUnderEvaluationImpl subDocument2;

    @Mock
    CollectionSequence collectionSequence;

    @Mock
    EvaluateCondition evaluateCondition;

    @Mock
    EnvironmentSnapshot environmentSnapshot;

    @Mock
    Condition condition;

    @Mock
    private ConditionEngineMetadata conditionEngineMetadata;

    @Mock
    private ApiProperties apiProperties;

    @InjectMocks
    ExcludedContentProcessorImpl excludedContentProcessor;

    @Before
    public void setup() {
        document = new DocumentUnderEvaluationImpl(conditionEngineMetadata, apiProperties);
        document.setReference(UUID.randomUUID().toString());

        {
            document.addMetadataString("FieldA", "AValue");
            document.addMetadataString("FieldA", "BValue");
            document.addMetadataString("FieldB", "BValue");
            document.addMetadataString("FieldB", "this is a SSN 22-563-2347");
        }

        {
            subDocument1 = new DocumentUnderEvaluationImpl(conditionEngineMetadata, apiProperties);
            subDocument1.setReference(UUID.randomUUID().toString());

            subDocument1.addMetadataString("SubFieldA", "AValue");
            subDocument1.addMetadataString("SubFieldA", "BValue");

            document.getDocuments().add(subDocument1);
        }

        {
            subDocument2 = new DocumentUnderEvaluationImpl(conditionEngineMetadata, apiProperties);
            subDocument2.setReference(UUID.randomUUID().toString());

            subDocument2.addMetadataString("SubFieldA", "AValue");
            subDocument2.addMetadataString("SubFieldA", "BValue");
            subDocument2.addMetadataString("SubFieldC", "this is a SSN 22-563-2347");

            document.getDocuments().add(subDocument2);
        }

        Condition condition = mock(Condition.class);
        condition.target = (ConditionTarget.ALL);
        when(environmentSnapshot.getCondition(any())).thenReturn(condition);
    }

    //region Excluded Fragment
    @Test
    public void replaceACommonStringInMultiValueFieldTest(){
        Collection<ExcludedFragment> excludedFragments = new LinkedList<>();

        excludedFragments.add(createExcludedFragment("Value", "FieldA"));

        excludedContentProcessor.removeExcludedFragments(document, excludedFragments);

        Assert.assertTrue(document.metadataContains("FieldA", "A"));
        Assert.assertTrue(document.metadataContains("FieldA", "B"));
        Assert.assertFalse(document.metadataContains("FieldA", "AValue"));
    }

    @Test
    public void replaceAStringInMultiValueFieldTest(){
        Collection<ExcludedFragment> excludedFragments = new LinkedList<>();

        excludedFragments.add(createExcludedFragment("A", "FieldA"));

        excludedContentProcessor.removeExcludedFragments(document, excludedFragments);

        Assert.assertTrue(document.metadataContains("FieldA", "Value"));
        Assert.assertTrue(document.metadataContains("FieldA", "BValue"));
        Assert.assertFalse(document.metadataContains("FieldA", "AValue"));
    }

    @Test
    public void replaceAStringSubDocumentsTest(){
        Collection<ExcludedFragment> excludedFragments = new LinkedList<>();

        excludedFragments.add(createExcludedFragment("A", "SubFieldA"));

        excludedContentProcessor.removeExcludedFragments(document, excludedFragments);

        Assert.assertTrue(document.getDocuments().stream().allMatch(u -> u.metadataContains("SubFieldA", "Value")));
        Assert.assertTrue(document.getDocuments().stream().allMatch(u -> u.metadataContains("SubFieldA", "BValue")));

        Assert.assertTrue(document.metadataContains("FieldA", "AValue"));
    }

    @Test
    public void replaceAStringInMultipleFieldsTest(){
        Collection<ExcludedFragment> excludedFragments = new LinkedList<>();

        excludedFragments.add(createExcludedFragment("B", "FieldA", "FieldB"));

        excludedContentProcessor.removeExcludedFragments(document, excludedFragments);

        Assert.assertTrue(document.metadataContains("FieldA", "Value"));
        Assert.assertTrue(document.metadataContains("FieldB", "Value"));

        Assert.assertFalse(document.metadataContains("FieldA", "BValue"));
        Assert.assertFalse(document.metadataContains("FieldB", "BValue"));
    }

    @Test
    public void removeAStringRegexTest(){
        Collection<ExcludedFragment> excludedFragments = new LinkedList<>();

        excludedFragments.add(createExcludedFragment("\\d{2}-\\d{3}-\\d{4}", "FieldB"));

        excludedContentProcessor.removeExcludedFragments(document, excludedFragments);

        Assert.assertTrue(document.metadataContains("FieldB","this is a SSN "));
        Assert.assertFalse(document.metadataContains("FieldB","this is a SSN 22-563-2347"));
    }

    private ExcludedFragment createExcludedFragment(String pattern, String... fieldNames){
        Collection<String> list = Arrays.asList(fieldNames);

        ExcludedFragment excludedFragment = new ExcludedFragment();
        excludedFragment.fieldNames = list;
        excludedFragment.pattern = pattern;

        return excludedFragment;
    }
    //endregion

    //region Excluded Content
    @Test
    public void excludedContentTest() throws CpeException {
        ConditionEvaluationResult result = new ConditionEvaluationResult();
        result.setMatch(true);
        result.getMatchedConditions().addAll(
                Arrays.asList(new MatchedCondition(document.getReference(), condition)));
        when(evaluateCondition.evaluate(any(), any(), any(), any())).thenReturn(result);

        Collection<DocumentUnderEvaluation> excludedDocuments =  excludedContentProcessor.setExcludedDocuments(this.collectionSequence, document, 1L, environmentSnapshot);

        Assert.assertTrue(document.getIsExcluded());
        Assert.assertEquals(1, excludedDocuments.size());
    }

    @Test
    public void notExcludedContentTest() throws CpeException {
        ConditionEvaluationResult result = new ConditionEvaluationResult();
        result.setMatch(false);
        when(evaluateCondition.evaluate(any(), any(), any(), any())).thenReturn(result);

        Collection<DocumentUnderEvaluation> excludedDocuments =  excludedContentProcessor.setExcludedDocuments(this.collectionSequence, document, 1L, environmentSnapshot);

        Assert.assertFalse(document.getIsExcluded());
        Assert.assertEquals(0, excludedDocuments.size());
    }

    @Test
    public void childExcludedContentTest() throws CpeException {
        {
            ConditionEvaluationResult conditionEvaluationResult = new ConditionEvaluationResult();
            conditionEvaluationResult.setMatch(true);

            Collection<MatchedCondition> matchedConditionArrayList =
                    Arrays.asList(new MatchedCondition(subDocument2.getReference(), condition));

            conditionEvaluationResult.getMatchedConditions().addAll( matchedConditionArrayList );
            conditionEvaluationResult.getAllConditionMatches().addAll( matchedConditionArrayList );

            when(evaluateCondition.evaluate(any(), eq(document), any(), any())).thenReturn(conditionEvaluationResult);
        }
        Collection<DocumentUnderEvaluation> excludedDocuments =  excludedContentProcessor.setExcludedDocuments(this.collectionSequence, document, 1L, environmentSnapshot);

        Assert.assertFalse(document.getIsExcluded());

        Assert.assertFalse(subDocument1.getIsExcluded());

        Assert.assertTrue(subDocument2.getIsExcluded());

        Assert.assertEquals(1, excludedDocuments.size());
    }

    @Test
    public void rootAndChildExcludedContentTest() throws CpeException {
        {
            ConditionEvaluationResult conditionEvaluationResult = new ConditionEvaluationResult();
            conditionEvaluationResult.setMatch(true);
            conditionEvaluationResult.getMatchedConditions().addAll(
                    Arrays.asList(new MatchedCondition(document.getReference(), condition), new MatchedCondition(subDocument2.getReference(), condition)));
            when(evaluateCondition.evaluate(any(), eq(document), any(), any())).thenReturn(conditionEvaluationResult);
        }
        Collection<DocumentUnderEvaluation> excludedDocuments = excludedContentProcessor.setExcludedDocuments(this.collectionSequence, document, 1L, environmentSnapshot);

        Assert.assertTrue(document.getIsExcluded());

        Assert.assertFalse(subDocument1.getIsExcluded());

        Assert.assertTrue(subDocument2.getIsExcluded());

        Assert.assertEquals(2, excludedDocuments.size());
    }
    //endregion
}
