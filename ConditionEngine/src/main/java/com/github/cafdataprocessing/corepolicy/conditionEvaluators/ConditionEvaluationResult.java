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

import com.github.cafdataprocessing.corepolicy.common.dto.UnevaluatedCondition;
import com.github.cafdataprocessing.corepolicy.common.dto.UnmatchedCondition;
import com.github.cafdataprocessing.corepolicy.common.dto.MatchedCondition;
import com.github.cafdataprocessing.corepolicy.common.dto.conditions.Condition;
import com.github.cafdataprocessing.corepolicy.document.DocumentUnderEvaluation;

import java.util.Collection;
import java.util.HashSet;

/**
 * Object that encapsulates the result of a condition evaluation. Have collections of missing fields/matched conditions
 * because it could be a boolean or a not condition and we need to track the tree of conditions that caused a match.
 */
public class ConditionEvaluationResult {
    private boolean match;
    private Collection<UnevaluatedCondition> unevaluatedConditions;
    // N.B. this only contains conditions that finally cause the match.
    private Collection<MatchedCondition> matchedConditions;
    private Collection<UnmatchedCondition> unmatchedConditions;

    // Flat list of all conditions which are evaluated and match regardless of whether
    // they are part of a final match or not.
    private Collection <MatchedCondition> allConditionMatches;

    /**
     * Default constructor initializes the missingFields and sets the result to false.
     * */
    public ConditionEvaluationResult(){
        this.unevaluatedConditions = new HashSet<>();
        this.matchedConditions = new HashSet<>();
        this.allConditionMatches = new HashSet<>();
        this.unmatchedConditions = new HashSet<>();
        this.match = false;
    }

    public Collection<UnevaluatedCondition> getUnevaluatedConditions() {
        return unevaluatedConditions;
    }

    public boolean isMatch() {
        return match;
    }

    public void setMatch(boolean match) {
        this.match = match;
    }

    public Collection<MatchedCondition> getMatchedConditions() {
        return matchedConditions;
    }

    public Collection<UnmatchedCondition> getUnmatchedConditions() {
        return unmatchedConditions;
    }

    // only has matched conditions inside, if the overal result is a match!
    public Collection<MatchedCondition> getAllConditionMatches() {
        return allConditionMatches;
    }


    /**
     * Overload to remove necessity to pass term matches.
     * @param isMatch
     * @param condition
     * @param document
     */
    void populateEvaluationResult( boolean isMatch, Condition condition, DocumentUnderEvaluation document, boolean mergeResult ) {
        populateEvaluationResult( isMatch, condition, document, null, mergeResult );
    }

    /***
     * Used to populate the Matched Condition / Unmatched Condition list with a single condition.
     * @param isMatch
     * @param condition
     * @param document
     * @param termMatches // optional matched terms.
     */
    void populateEvaluationResult( boolean isMatch, Condition condition, DocumentUnderEvaluation document, Collection<String> termMatches, boolean mergeResult ) {

        if ( !mergeResult ) {
            setMatch(isMatch);
        }
        else if ( isMatch ) {
            // if merging only setMatch=true never to false.
            setMatch(isMatch);
        }

        if(isMatch){
            MatchedCondition matchedCondition = new MatchedCondition(document.getReference(), condition);

            if ( termMatches != null && termMatches.size() > 0 ) {
                matchedCondition.getTerms().addAll(termMatches);
            }

            getMatchedConditions().add(matchedCondition);
            getAllConditionMatches().add(matchedCondition);

        } else {
            getUnmatchedConditions().add(new UnmatchedCondition(document.getReference(), condition ));
        }

    }
    /***
     * Used to populate the Matched Condition list with a single condition,
     * but one which has already been constructed e.g. usually with lexiconexpression / term info
     * @param condition
     */
    void populateEvaluationResult( MatchedCondition condition ) {

        setMatch(true);

        getMatchedConditions().add(condition);
        getAllConditionMatches().add(condition);
    }

    /***
     * Overload the populate a conditionEvaluationResult from a child conditionEvaluationResult.
     * @param tempConditionEvaluationResult
     * @param mergeResult  // indicate whether we set the match result to a value, or merge it.
     */
    void populateEvaluationResult( ConditionEvaluationResult tempConditionEvaluationResult, boolean mergeResult )
    {
        populateEvaluationResult(tempConditionEvaluationResult.isMatch(),
                tempConditionEvaluationResult.getMatchedConditions(),
                tempConditionEvaluationResult.getUnmatchedConditions(),
                tempConditionEvaluationResult.getUnevaluatedConditions(),
                tempConditionEvaluationResult.getAllConditionMatches(),
                mergeResult);
    }

    void populateEvaluationResult( ConditionEvaluationResult tempConditionEvaluationResult ) {

        populateEvaluationResult(tempConditionEvaluationResult, true );
    }

    /**
     * Used to populate a conditionEvaluationResult from lists which are used during child evaluation ( usualy booleanconditions )
     * @param isMatch
     * @param matchedConditions
     * @param unmatchedConditions
     * @param unevaluatedConditions
     * @param allConditionMatches
     * @param mergeResult // merge the result ( only merges true ) or force to incoming isMatch value.
     */
    void populateEvaluationResult(boolean isMatch, Collection<MatchedCondition> matchedConditions, Collection<UnmatchedCondition> unmatchedConditions, Collection<UnevaluatedCondition> unevaluatedConditions, Collection<MatchedCondition> allConditionMatches, boolean mergeResult) {

        if ( !mergeResult ) {
            setMatch(isMatch);
        }
        else if ( isMatch ) {
            // if merging only setMatch=true never to false.
            setMatch(isMatch);
        }

        // only add to matchconditions if the result is going to be a match!
        if (isMatch && (matchedConditions != null)) {
            getMatchedConditions().addAll(matchedConditions);
        }

        if (allConditionMatches != null) {
            getAllConditionMatches().addAll(allConditionMatches);
        }
        if (unmatchedConditions != null) {
            getUnmatchedConditions().addAll(unmatchedConditions);
        }
        if (unevaluatedConditions != null) {
            getUnevaluatedConditions().addAll(unevaluatedConditions);
        }
    }

}
