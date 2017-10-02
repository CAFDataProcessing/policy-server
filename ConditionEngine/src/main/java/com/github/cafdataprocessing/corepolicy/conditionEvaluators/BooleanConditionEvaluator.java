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
import com.github.cafdataprocessing.corepolicy.common.dto.UnevaluatedCondition;
import com.github.cafdataprocessing.corepolicy.common.dto.UnmatchedCondition;
import com.github.cafdataprocessing.corepolicy.common.exceptions.CpeException;
import com.github.cafdataprocessing.corepolicy.document.DocumentUnderEvaluation;
import com.github.cafdataprocessing.corepolicy.common.dto.MatchedCondition;
import com.github.cafdataprocessing.corepolicy.common.dto.conditions.BooleanCondition;
import com.github.cafdataprocessing.corepolicy.common.dto.conditions.BooleanOperator;
import com.github.cafdataprocessing.corepolicy.common.dto.conditions.Condition;
import com.github.cafdataprocessing.corepolicy.common.EnvironmentSnapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 *
 * Evaluator for ConditionSets which are multiple conditions grouped under a boolean operator.
 */
@Component("BooleanCondition")
class BooleanConditionEvaluator extends ConditionEvaluatorBase<BooleanCondition> {
    private final static Logger logger = LoggerFactory.getLogger(BooleanConditionEvaluator.class);

    private EvaluateCondition evaluateCondition;

    @Autowired
    public BooleanConditionEvaluator(EvaluateCondition evaluateCondition){
        this.evaluateCondition = evaluateCondition;
    }

    @Override
    protected void evaluate(CollectionSequence collectionSequence, ConditionEvaluationResult result, DocumentUnderEvaluation document, BooleanCondition condition, EnvironmentSnapshot environmentSnapshot) throws CpeException {

        final Collection<Condition> conditions = condition.children;
        final BooleanOperator operator = condition.operator;

        if(conditions.size() == 0){

            MatchedCondition matchedCondition = new MatchedCondition(document.getReference(), condition);

            result.populateEvaluationResult(true, Arrays.asList(matchedCondition), null, null, Arrays.asList(matchedCondition), false);

            //If the boolean is empty then return true, but don't count this condition as one of the matched ones
            //David and I(Andy) have different opinions on this behaviour.
            return;
        }

        // we keep 2 lists of matched conditions, a flat list of all matched conditions regardless of whether
        // they cause the final match or not ( used for pre-evaluation ) and a list which only contains
        // matches which cause the final top level match ( only recorded e.g. if boolean result is a match. )
        Collection<MatchedCondition> matchedConditions = new ArrayList<>();
        Collection<UnmatchedCondition> unmatchedConditions = new ArrayList<>();
        Collection<UnevaluatedCondition> unevaluatedConditions = new ArrayList<>();
        Collection<MatchedCondition> allConditionMatches = new ArrayList<>();

        boolean foundMatch = false;

        //loop through each condition and evaluate
        for(Condition containedCondition : conditions){
            ConditionEvaluationResult containedConditionResult = evaluateCondition.evaluate(collectionSequence, document, containedCondition, environmentSnapshot);

			// Regardless of whether its a match or not, record any unevaluated / unmatched conditions
            unmatchedConditions.addAll(containedConditionResult.getUnmatchedConditions());
            unevaluatedConditions.addAll(containedConditionResult.getUnevaluatedConditions());
            allConditionMatches.addAll(containedConditionResult.getAllConditionMatches());

            if(operator == BooleanOperator.AND && !containedConditionResult.isMatch()){
                // we dont need to evaluate any more but dont return we need to assign evaluated results.
                // ensure to set it to false, incase previous hit was true.
                foundMatch = false;
                break;
            }

            if (containedConditionResult.isMatch()){
                //Indicate that at least 1 match was found
                foundMatch = true;
                matchedConditions.addAll(containedConditionResult.getMatchedConditions());
            }

            //Collection Sequence requires all OR'd conditions to be evaluated for reporting purposes
            if(operator == BooleanOperator.OR && foundMatch && !collectionSequence.fullConditionEvaluation){
                break;
            }
        }

        /**
         * At this point, we have had to check all the conditions. So either:
         * a: all true and operator was AND
         *  OR
         * b: this is an OR operator and we have stored any matches already
         *
         * In the case of OR, we don't need to do anything as all matches have already been added
         * In the case of AND, we need to add all conditions to the result now (we would have broken out if a condition
         * that didn't match was found.
         */

        // Set the final boolean condition status onto our list in one of 3 states.
        if(foundMatch){

            // we need to change this - the flat list getMatchedConditions.
            // needs to get all matched conditions.
            // but only on a collection match does the list getMatchedConditionsOnMatch get
            // populated, as this is used by the ConditionEngineResult matchedCollections tree.
            // Also the top booleanCondition is only added when result isMatch=true
            MatchedCondition matchedCondition=  new MatchedCondition(document.getReference(), condition);

            matchedConditions.add(matchedCondition);
            allConditionMatches.add(matchedCondition);
        } else {
            // PD-302 Now the parent boolean records all of its children conditions states, we
            // need to record the boolean itself but only it has no unevaluated limbs.
            // It cannot truely be unmatched if it has any unevaluated children.
            // I would love to put the boolean on as unevaluated, but we need a reason e.g.
            // children-unevaluated but this should equally be done for the NOT condition.
            if ( unevaluatedConditions.size() == 0 ) {
                unmatchedConditions.add(new UnmatchedCondition(document.getReference(), condition));
            }
        }

        result.populateEvaluationResult(foundMatch, matchedConditions, unmatchedConditions, unevaluatedConditions, allConditionMatches, false );
    }

}
