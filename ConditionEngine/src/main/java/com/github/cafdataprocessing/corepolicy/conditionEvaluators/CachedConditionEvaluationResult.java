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

import com.github.cafdataprocessing.corepolicy.common.dto.MatchedCondition;
import com.github.cafdataprocessing.corepolicy.common.dto.UnmatchedCondition;

/**
 * Represents a cached version of a condition evaluation result.
 */
public class CachedConditionEvaluationResult {

    private boolean isMatch = false;
    private MatchedCondition matchedCondition = null;
    private UnmatchedCondition unmatchedCondition = null;

    public boolean isMatch() {
        return isMatch;
    }

    public void setIsMatch(boolean isMatch) {
        this.isMatch = isMatch;
    }

    public MatchedCondition getMatchedCondition() {
        return matchedCondition;
    }

    public void setMatchedCondition(MatchedCondition matchedCondition) {
        this.matchedCondition = matchedCondition;
    }

    public UnmatchedCondition getUnmatchedCondition() {
        return unmatchedCondition;
    }

    public void setUnmatchedCondition(UnmatchedCondition unmatchedCondition) {
        this.unmatchedCondition = unmatchedCondition;
    }
}
