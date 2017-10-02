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

import com.github.cafdataprocessing.corepolicy.common.dto.CollectionSequence;
import com.github.cafdataprocessing.corepolicy.common.dto.conditions.Condition;
import com.github.cafdataprocessing.corepolicy.common.dto.conditions.ConditionTarget;
import com.github.cafdataprocessing.corepolicy.common.dto.conditions.FragmentCondition;
import com.github.cafdataprocessing.corepolicy.common.exceptions.CpeException;
import com.github.cafdataprocessing.corepolicy.document.DocumentUnderEvaluation;
import com.github.cafdataprocessing.corepolicy.common.EnvironmentSnapshot;
import com.github.cafdataprocessing.corepolicy.common.dto.MatchedCondition;
import com.github.cafdataprocessing.corepolicy.common.dto.UnmatchedCondition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.LinkedList;
import java.util.stream.Collectors;

/**
 *
 */
public abstract class ConditionEvaluatorBase<T extends Condition> implements ConditionEvaluator<T> {
    private final static Logger logger = LoggerFactory.getLogger(BooleanConditionEvaluator.class);

    @Override
    public ConditionEvaluationResult evaluate(CollectionSequence collectionSequence, DocumentUnderEvaluation documentUnderEvaluation,
                                              T condition,
                                              EnvironmentSnapshot environmentSnapshot) throws CpeException {

        ConditionEvaluationResult conditionEvaluationResult = new ConditionEvaluationResult();

        // If we have evaluated this limb for this document before - we can return early without build up our tree of results again.
        // Either way these results are already in the matched_conditions list, and we dont want duplicates being added.
        if ( documentUnderEvaluation.hasConditionBeenEvaluatedThisRun( condition.id )) {

            // we dont need to add any results, as we have already evaluated this limb for this document already, and assigned the results
            // to the ConditionEngineResult.
            CachedConditionEvaluationResult conditionEvaluationResultMatch = documentUnderEvaluation.getConditionEvaluationResult(condition.id);

            if (conditionEvaluationResultMatch != null) {

                // all the conditions for this match should also be on the document already!
                conditionEvaluationResult.setMatch(conditionEvaluationResultMatch.isMatch());

                if ( conditionEvaluationResultMatch.isMatch() ) {
                    conditionEvaluationResult.getAllConditionMatches().add(conditionEvaluationResultMatch.getMatchedCondition());
                    conditionEvaluationResult.getMatchedConditions().add(conditionEvaluationResultMatch.getMatchedCondition());
                }
                else {
                    conditionEvaluationResult.getUnmatchedConditions().add(conditionEvaluationResultMatch.getUnmatchedCondition());
                }
            }

            return conditionEvaluationResult;
        }


        Collection<DocumentUnderEvaluation> itemsToCheck = getItemsToCheck(documentUnderEvaluation, condition);
        for (DocumentUnderEvaluation itemToCheck: itemsToCheck) {
            ConditionEvaluationResult tempConditionEvaluationResult = new ConditionEvaluationResult();
            evaluate(collectionSequence, tempConditionEvaluationResult, itemToCheck, (T) condition, environmentSnapshot);

            //Merge the results
            conditionEvaluationResult.populateEvaluationResult(tempConditionEvaluationResult, true );

            //Add the result to the cached condition result
            if ( ((T) condition ) instanceof FragmentCondition) {
                // we skip fragment conditions, they dont the top level fragment returned for a match.
                logger.debug("Skipping cache of FragmentCondition ID: " + ((T) condition).id );
            }
            else
            {
                itemToCheck.addConditionEvaluationResult(tempConditionEvaluationResult, ((T) condition).id);
            }

            itemToCheck.setConditionHasBeenEvaluatedThisRun( condition.id );
        }

        return conditionEvaluationResult;
    }

    /**childFile.isFile()
     * Gets all the documents the item should be ran against
     * @param documentUnderEvaluation
     * @param condition
     * @return
     */
    private Collection<DocumentUnderEvaluation> getItemsToCheck(DocumentUnderEvaluation documentUnderEvaluation, Condition condition) {
        Collection<DocumentUnderEvaluation> documents = new LinkedList<>();

        if(condition.target == null || condition.target == ConditionTarget.ALL || condition.target == ConditionTarget.CONTAINER && !documentUnderEvaluation.getIsExcluded()){
            documents.add(documentUnderEvaluation);
        }

        if(condition.target == ConditionTarget.ALL || condition.target == ConditionTarget.CHILDREN) {
            documents.addAll(getChildren(documentUnderEvaluation, condition.includeDescendants));
        }

        return documents;
    }

    /**
     * Gets the children of document passed, can also get all descendants
     * @param documentUnderEvaluation
     * @param includeDescendiants indicated if the descendants should be included rather than just the direct children
     * @return the descendants
     */
    private Collection<DocumentUnderEvaluation> getChildren(DocumentUnderEvaluation documentUnderEvaluation, boolean includeDescendiants){

        if(documentUnderEvaluation.getDocuments().isEmpty()){
            return new LinkedList<>();
        }

        Collection<DocumentUnderEvaluation> documents = documentUnderEvaluation.getDocuments().stream().filter(u-> !u.getIsExcluded()).collect(Collectors.toList());

        if(includeDescendiants){
            Collection<DocumentUnderEvaluation> childDocuments = new LinkedList<>();
            for (DocumentUnderEvaluation descendiant: documents){
                childDocuments.addAll(getChildren(descendiant, true));
            }
            documents.addAll(childDocuments);
        }
        return documents;
    }

    protected abstract void evaluate(CollectionSequence collectionSequence,
                                     ConditionEvaluationResult result,
                                     DocumentUnderEvaluation document,
                                     T condition,
                                     EnvironmentSnapshot environmentSnapshot) throws CpeException;


    protected boolean checkForCachedEvaluationResults(ConditionEvaluationResult result, DocumentUnderEvaluation document, Condition condition) {
        CachedConditionEvaluationResult conditionEvaluationResultMatch = document.getConditionEvaluationResult(condition.id);

        // Have we got this result cached already, if so obtain and return it now instead of re-evaluating
        if(conditionEvaluationResultMatch != null) {
            result.setMatch(conditionEvaluationResultMatch.isMatch());

            if (conditionEvaluationResultMatch.isMatch()) {
                // CP3551 - if we need to add terms for re-evaluation optimization change here!
                // we are now obtaining the previous match information from the cached previous evaluation.
                MatchedCondition matchedCondition = conditionEvaluationResultMatch.getMatchedCondition();

                result.getMatchedConditions().add(matchedCondition);
                result.getAllConditionMatches().add(matchedCondition);
            } else {

                UnmatchedCondition umc = conditionEvaluationResultMatch.getUnmatchedCondition();

                if ( umc == null )
                {
                    // its actually unevaluated or missing, not unmatched, as such return false.
                    return false;
                }

                result.getUnmatchedConditions().add(umc);
            }

            return true;
        }

        return false;
    }
}
