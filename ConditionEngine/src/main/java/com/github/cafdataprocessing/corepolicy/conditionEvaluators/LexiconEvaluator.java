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
package com.github.cafdataprocessing.corepolicy.conditionEvaluators;

import com.github.cafdataprocessing.corepolicy.booleanagent.BooleanAgentServices;
import com.github.cafdataprocessing.corepolicy.common.dto.*;
import com.google.common.base.Stopwatch;
import com.github.cafdataprocessing.corepolicy.booleanagent.BooleanAgentQueryResult;
import com.github.cafdataprocessing.corepolicy.common.ApiProperties;
import com.github.cafdataprocessing.corepolicy.common.EnvironmentSnapshot;
import com.github.cafdataprocessing.corepolicy.common.dto.conditions.LexiconCondition;
import com.github.cafdataprocessing.corepolicy.common.exceptions.BackEndRequestFailedCpeException;
import com.github.cafdataprocessing.corepolicy.common.exceptions.CpeException;
import com.github.cafdataprocessing.corepolicy.common.exceptions.excpetionErrors.BackEndRequestFailedErrors;
import com.github.cafdataprocessing.corepolicy.document.DocumentUnderEvaluation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Evaluator for the Lexicon Condition
 */
@Component("LexiconCondition")
class LexiconEvaluator extends FieldConditionEvaluator<LexiconCondition>{
    private final BooleanAgentServices booleanAgentServices;
    private ContentExpressionHelper contentExpressionHelper;

    @Autowired
    public LexiconEvaluator(BooleanAgentServices booleanAgentServices,
                            ContentExpressionHelper contentExpressionHelper,
                            ApiProperties apiProperties){
        super(apiProperties);
        this.booleanAgentServices = booleanAgentServices;
        this.contentExpressionHelper = contentExpressionHelper;
    }

    @Override
    protected void evaluateFieldValues(ConditionEvaluationResult result, LexiconCondition condition, DocumentUnderEvaluation document, EnvironmentSnapshot environmentSnapshot) {
        if(condition.value == null){
            throw new BackEndRequestFailedCpeException(BackEndRequestFailedErrors.InvalidDataDetected, new Exception("Lexicon Condition id " + condition.id + " has no target lexicon."));
        }

        Lexicon lexicon = environmentSnapshot.getLexicon(condition.value);
        Collection<LexiconExpression> lexiconExpressions = lexicon.lexiconExpressions;
        boolean containsBooleanAgentExpressions = false;
        MatchedCondition matchedCondition = new MatchedCondition(document.getReference(), condition);

        Collection<LexiconExpression> regexLexiconExpressions = new ArrayList<>();
        for(LexiconExpression lexiconExpression:lexiconExpressions){
            if(lexiconExpression.type == LexiconExpressionType.REGEX) {
                regexLexiconExpressions.add(lexiconExpression);
            }
            else{
                if(!booleanAgentServices.getAvailable()){
                    recordUnevaluatedCondition(result, condition, UnevaluatedCondition.Reason.MISSING_SERVICE);
                }
                containsBooleanAgentExpressions = true;
            }
        }

        if(!regexLexiconExpressions.isEmpty()){
            regexLexiconExpressions.parallelStream().forEach(le -> {
                try {
                    evaluateRegex(document, condition, matchedCondition, le);
                } catch (Exception e) {
                    throw new BackEndRequestFailedCpeException(BackEndRequestFailedErrors.GeneralFailure, e);
                }
            });
        }
        if(containsBooleanAgentExpressions){
            evaluateBooleanAgent(environmentSnapshot, document, condition, matchedCondition);
        }

        if ( !matchedCondition.getMatchedLexiconExpressions().isEmpty() )
        {
            result.populateEvaluationResult( matchedCondition );
        }
        else {
            result.populateEvaluationResult( false, condition, document, false );
        }
    }

    private void evaluateBooleanAgent(EnvironmentSnapshot environmentSnapshot, DocumentUnderEvaluation document,
                                      LexiconCondition condition, MatchedCondition matchedCondition) throws CpeException {

        Stopwatch stopwatch = Stopwatch.createStarted();
        BooleanAgentQueryResult booleanAgentQueryResult = contentExpressionHelper
                .handleBooleanAgentExpression(environmentSnapshot.getInstanceId(),
                        booleanAgentServices, document, condition.field, condition.language);
        document.logTime("Evaluate-LexiconCondition-Query", stopwatch);

        List<LexiconExpressionId> lexiconExpressionIdList =  booleanAgentQueryResult
                .getLexiconExpressionIdTerms()
                .asMap()
                .keySet()
                .stream()
                .filter(lei -> lei.lexiconId.equals(condition.value)).collect(Collectors.toList());

        if(!lexiconExpressionIdList.isEmpty()){
            for(LexiconExpressionId lexiconExpressionId:lexiconExpressionIdList){
                MatchedLexiconExpression matchedLexiconExpression = new MatchedLexiconExpression();
                matchedLexiconExpression.setLexiconExpressionId(lexiconExpressionId.lexiconExpressionId);
                for(String match:booleanAgentQueryResult.getLexiconExpressionIdTerms().get(lexiconExpressionId)){
                    matchedLexiconExpression.getTerms().add(match);
                }
                matchedCondition.getMatchedLexiconExpressions().add(matchedLexiconExpression);
            }
        }
    }

    private void evaluateRegex(DocumentUnderEvaluation document, LexiconCondition condition, MatchedCondition matchedCondition, LexiconExpression lexiconExpression) throws CpeException {
        Stopwatch stopwatch = Stopwatch.createStarted();
        Collection<String> matches = contentExpressionHelper
                .handleRegexExpression(document, condition.field, lexiconExpression.expression);
        document.logTime("Evaluate-LexiconCondition-Regex", stopwatch);

        if(!matches.isEmpty()){
            MatchedLexiconExpression matchedLexiconExpression = new MatchedLexiconExpression();
            matchedLexiconExpression.setLexiconExpressionId(lexiconExpression.id);
            for(String match:matches){
                matchedLexiconExpression.getTerms().add(match);
            }
            matchedCondition.getMatchedLexiconExpressions().add(matchedLexiconExpression);
        }
    }
}
