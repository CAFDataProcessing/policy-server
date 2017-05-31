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
package com.github.cafdataprocessing.corepolicy.booleanagent;

import com.github.cafdataprocessing.corepolicy.common.dto.LexiconExpression;
import com.github.cafdataprocessing.corepolicy.common.dto.conditions.TextCondition;
import com.github.cafdataprocessing.corepolicy.domainModels.BooleanAgentDocument;
import com.github.cafdataprocessing.corepolicy.domainModels.BooleanAgentDocuments;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

public class ConditionToBooleanAgentConverterImplTest {

    @Test
    public void testConvert() throws Exception {
        ConditionToBooleanAgentConverterImpl conditionToBooleanAgentConverter = new ConditionToBooleanAgentConverterImpl();
        Collection<TextCondition> contentConditions = new ArrayList<>();
        Collection<LexiconExpression> lexiconExpressions = new ArrayList<LexiconExpression>();

        TextCondition TextCondition = new TextCondition();
        TextCondition.id = (99L);
        TextCondition.value = ("an expression");
        contentConditions.add(TextCondition);

        LexiconExpression lexiconExpression = new LexiconExpression();
        lexiconExpression.lexiconId = (99L);
        lexiconExpression.id = (88L);
        lexiconExpression.expression = ("lexicon expression");
        lexiconExpressions.add(lexiconExpression);

        BooleanAgentDocuments addToTextIndexDocuments = conditionToBooleanAgentConverter.convert(contentConditions, lexiconExpressions);
        Assert.assertEquals(addToTextIndexDocuments.getDocuments().size(), 2);
        BooleanAgentDocument contentExpressionDocument = (BooleanAgentDocument)addToTextIndexDocuments.getDocuments().toArray()[0];

        String reference = contentExpressionDocument.getReference();
        Assert.assertEquals(reference, "99");

        Optional<String> booleanRestriction = contentExpressionDocument.getBooleanRestriction().stream().findFirst();
        Assert.assertTrue(booleanRestriction.isPresent());
        Assert.assertEquals(booleanRestriction.get(), "an expression");

        Optional<String> conditionId = contentExpressionDocument.getCondition_id().stream().findFirst();
        Assert.assertTrue(conditionId.isPresent());
        Assert.assertEquals(conditionId.get(), "99");

        BooleanAgentDocument lexiconExpressionDocument = (BooleanAgentDocument)addToTextIndexDocuments.getDocuments().toArray()[1];

        reference = lexiconExpressionDocument.getReference();
        Assert.assertEquals(reference, "99_88");

        booleanRestriction = lexiconExpressionDocument.getBooleanRestriction().stream().findFirst();
        Assert.assertTrue(booleanRestriction.isPresent());
        Assert.assertEquals(booleanRestriction.get(), "lexicon expression");

        Optional<String> lexiconId = lexiconExpressionDocument.getLexicon_id().stream().findFirst();
        Assert.assertTrue(lexiconId.isPresent());
        Assert.assertEquals(lexiconId.get(), "99");

        Optional<String> lexiconExpressionId = lexiconExpressionDocument.getLexicon_expression_id().stream().findFirst();
        Assert.assertTrue(lexiconExpressionId.isPresent());
        Assert.assertEquals(lexiconExpressionId.get(), "88");
    }
}