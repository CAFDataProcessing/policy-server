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
package com.github.cafdataprocessing.corepolicy.booleanagent;

import com.github.cafdataprocessing.corepolicy.common.dto.LexiconExpression;
import com.github.cafdataprocessing.corepolicy.common.dto.conditions.TextCondition;
import com.github.cafdataprocessing.corepolicy.domainModels.BooleanAgentDocument;
import com.github.cafdataprocessing.corepolicy.domainModels.BooleanAgentDocuments;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 *
 */
@Component
public class ConditionToBooleanAgentConverterImpl implements ConditionToBooleanAgentConverter {
    @Override
    public BooleanAgentDocuments convert(Collection<TextCondition> contentExpressionConditions, Collection<LexiconExpression> lexiconExpressions) {
        BooleanAgentDocuments addToTextIndexDocuments = new BooleanAgentDocuments();
        Collection<BooleanAgentDocument> documents = new ArrayList<>();
        addToTextIndexDocuments.setDocuments(documents);

        if(contentExpressionConditions!=null){
            for(TextCondition contentExpressionCondition:contentExpressionConditions){
                BooleanAgentDocument document = new BooleanAgentDocument();
                document.setReference(String.valueOf(contentExpressionCondition.id));
                document.setBooleanRestriction(Arrays.asList(String.valueOf(contentExpressionCondition.value)));
                document.setCondition_id(Arrays.asList(String.valueOf(contentExpressionCondition.id)));
                documents.add(document);
            }
        }

        if(lexiconExpressions!=null){
            for(LexiconExpression lexiconExpression:lexiconExpressions){
                BooleanAgentDocument document = new BooleanAgentDocument();
                document.setReference(String.valueOf(lexiconExpression.lexiconId) + "_" + lexiconExpression.id);
                document.setBooleanRestriction(Arrays.asList(String.valueOf(lexiconExpression.expression)));
                document.setLexicon_id(Arrays.asList(String.valueOf(lexiconExpression.lexiconId)));
                document.setLexicon_expression_id(Arrays.asList(String.valueOf(lexiconExpression.id)));
                documents.add(document);
            }
        }

        return addToTextIndexDocuments;
    }
}
