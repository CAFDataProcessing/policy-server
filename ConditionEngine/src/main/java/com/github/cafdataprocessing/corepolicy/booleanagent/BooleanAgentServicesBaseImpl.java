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

import com.github.cafdataprocessing.corepolicy.common.exceptions.CpeException;
import com.github.cafdataprocessing.corepolicy.common.dto.LexiconExpressionId;
import com.github.cafdataprocessing.corepolicy.domainModels.BooleanAgentDocument;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

/**
 *
 */
public class BooleanAgentServicesBaseImpl {

    private final String startTagGuid;
    private final String endTagGuid;

    public BooleanAgentServicesBaseImpl() {
        this.startTagGuid = UUID.randomUUID().toString();
        this.endTagGuid = UUID.randomUUID().toString();
    }

    public String getStartTagGuid() {
        return startTagGuid;
    }

    public String getEndTagGuid() {
        return endTagGuid;
    }

    protected ArrayList<String> extractLinksFromHighlightedText(String highlightedText) {
        ArrayList<String> matchedStrings = new ArrayList<>();
        int startIndex = 0;
        while(true){
            startIndex = highlightedText.indexOf(getStartTagGuid(), startIndex);
            if(startIndex==-1){
                break;
            }
            int endIndex = highlightedText.indexOf(getEndTagGuid(), startIndex);
            matchedStrings.add(highlightedText.substring(startIndex + getStartTagGuid().length(), endIndex));
            startIndex = endIndex;
        }
        return matchedStrings;
    }

    protected void extractTermsFromBooleanAgentDocument(String text, BooleanAgentQueryResult result,
                                                        BooleanAgentDocument booleanAgentDocument) throws CpeException {
        if(booleanAgentDocument.getCondition_id()!=null && booleanAgentDocument.getCondition_id().size()>0){
            Optional<String> conditionId = booleanAgentDocument.getCondition_id().stream().findFirst();
            if(conditionId.isPresent()){
                Collection<String> links = booleanAgentDocument.getLinks();
                if(links!=null){
                    for(String link:links){
                        result.getConditionIdTerms().put(Long.valueOf(conditionId.get()), link);
                    }
                }
            }
        } else if(booleanAgentDocument.getLexicon_id()!=null && booleanAgentDocument.getLexicon_id().size()>0){
            LexiconExpressionId lexiconExpressionId = new LexiconExpressionId();
            Optional<String> lexiconId = booleanAgentDocument.getLexicon_id().stream().findFirst();
            if(lexiconId.isPresent()){
                lexiconExpressionId.lexiconId = Long.valueOf(lexiconId.get());
            }
            Optional<String> lexiconExpressionIdString = booleanAgentDocument.getLexicon_expression_id().stream().findFirst();
            if(lexiconExpressionIdString.isPresent()){
                lexiconExpressionId.lexiconExpressionId = Long.valueOf(lexiconExpressionIdString.get());
            }

            Collection<String> links = booleanAgentDocument.getLinks();
            if(links!=null) {
                for (String link : links) {
                    result.getLexiconExpressionIdTerms().put(lexiconExpressionId, link);
                }
            }
        }
    }
}
