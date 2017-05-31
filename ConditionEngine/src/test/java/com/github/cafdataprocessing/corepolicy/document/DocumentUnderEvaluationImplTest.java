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
package com.github.cafdataprocessing.corepolicy.document;

import com.github.cafdataprocessing.corepolicy.common.shared.MetadataValue;
import com.google.common.collect.Multimap;
import com.github.cafdataprocessing.corepolicy.common.ApiProperties;
import com.github.cafdataprocessing.corepolicy.common.ConditionEngineMetadata;
import com.github.cafdataprocessing.corepolicy.common.DocumentImpl;
import com.github.cafdataprocessing.corepolicy.common.dto.ConditionEngineResult;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

/**
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class DocumentUnderEvaluationImplTest {

    @Mock
    private ConditionEngineMetadata conditionEngineMetadata;

    @Mock
    private ApiProperties apiProperties;

    @Test
    public void metadataClonedTest(){
        DocumentImpl originalDocument = new DocumentImpl();
        originalDocument.getMetadata().put("f","a");

        when(conditionEngineMetadata.createResult(any(Multimap.class))).thenReturn(new ConditionEngineResult());

        DocumentUnderEvaluationImpl documentUnderEvaluation = new DocumentUnderEvaluationImpl(originalDocument,
                conditionEngineMetadata, apiProperties);
        documentUnderEvaluation.addMetadataString("f","b");

        assertEquals(1, originalDocument.getMetadata().get("f").size());
    }

    // Test that when there is no match that an empty collection is returned
    @Test
    public void getValuesNoMatchTest(){
        DocumentImpl originalDocument = new DocumentImpl();
        DocumentUnderEvaluationImpl documentUnderEvaluation = new DocumentUnderEvaluationImpl(originalDocument,
                conditionEngineMetadata, apiProperties);
        Collection<MetadataValue> values = documentUnderEvaluation.getValues("test");
        Assert.assertNotNull(values);
        Assert.assertTrue(values.isEmpty());
    }
}