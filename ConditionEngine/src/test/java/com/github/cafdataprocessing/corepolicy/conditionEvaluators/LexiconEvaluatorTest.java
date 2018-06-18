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
import com.github.cafdataprocessing.corepolicy.common.EngineProperties;
import com.github.cafdataprocessing.corepolicy.common.EnvironmentSnapshot;
import com.github.cafdataprocessing.corepolicy.common.dto.CollectionSequence;
import com.github.cafdataprocessing.corepolicy.common.dto.Lexicon;
import com.github.cafdataprocessing.corepolicy.common.dto.conditions.LexiconCondition;
import com.github.cafdataprocessing.corepolicy.common.exceptions.CpeException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;

import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.when;

/**
* Tests for the LexiconEvaluator class
*/
@RunWith(MockitoJUnitRunner.class)
public class LexiconEvaluatorTest {

    @Mock
    CollectionSequence collectionSequence;

    @Mock
    private BooleanAgentServices booleanAgentServices;

    @Mock
    private EngineProperties engineProperties;

    @Mock
    private EnvironmentSnapshot environmentSnapshot;

    @Mock
    private ConditionEngineMetadata conditionEngineMetadata;

    @Mock
    private ApiProperties apiProperties;

    private ContentExpressionHelper contentExpressionHelper = new ContentExpressionHelper(new NonCachingRegexMatcherFactoryImpl(),
            engineProperties);

    DocumentUnderEvaluation document;

    @Before
    public void setup(){
        when(engineProperties.getRegexTimeout()).thenReturn(2);
        Lexicon lexicon = new Lexicon();
        lexicon.lexiconExpressions = new ArrayList<>();
        when(environmentSnapshot.getLexicon(anyLong())).thenReturn(lexicon);
        DocumentUnderEvaluationImpl document = new DocumentUnderEvaluationImpl(conditionEngineMetadata, apiProperties);
        document.addMetadataString("FieldA", "AValue");
        document.addMetadataString("FieldA", "BValue");

        this.document = document;
    }

    @After
    public void cleanup(){

    }

    /**
     * A utility method to create an Evaluator and evaluate the Document field against a provided condition
     * */
    private ConditionEvaluationResult evaluate(LexiconCondition condition) throws CpeException {
        ConditionEvaluator<LexiconCondition> evaluator = new LexiconEvaluator(booleanAgentServices, contentExpressionHelper, apiProperties);
        return evaluator.evaluate(this.collectionSequence, this.document, condition, environmentSnapshot);
    }

    @Test
    public void writeWhenImplemented() throws CpeException {
        LexiconCondition condition = new LexiconCondition();
        condition.field = "somefield";
        condition.value = 1L;
        evaluate(condition);
    }
}
