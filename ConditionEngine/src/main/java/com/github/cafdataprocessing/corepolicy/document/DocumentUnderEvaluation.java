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
package com.github.cafdataprocessing.corepolicy.document;

import com.github.cafdataprocessing.corepolicy.conditionEvaluators.CachedConditionEvaluationResult;
import com.github.cafdataprocessing.corepolicy.conditionEvaluators.ConditionEvaluationResult;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Multimap;
import com.github.cafdataprocessing.corepolicy.booleanagent.BooleanAgentQueryResult;
import com.github.cafdataprocessing.corepolicy.common.LanguagesEnum;
import com.github.cafdataprocessing.corepolicy.common.dto.MatchedCondition;
import com.github.cafdataprocessing.corepolicy.common.dto.UnmatchedCondition;
import com.github.cafdataprocessing.corepolicy.common.shared.MetadataValue;
import org.slf4j.Logger;

import java.io.InputStream;
import java.util.Collection;

/**
 *
 */
public interface DocumentUnderEvaluation {

    String getReference();
    void setReference(String reference);
    boolean getFullMetadata();
    void setFullMetadata(boolean fullMetadata);

    Multimap<String, MetadataValue> getMetadata();   //Internally we use a case insensitive multimap.
    Multimap<String, MetadataValue> getStreams();  //Internally we use a case insensitive multimap.
    Collection<DocumentUnderEvaluation> getDocuments();

    /**
     * Utility methods, to add to the metadata / streams lists.
     */
    void addMetadataString(String key, String value);
    void addMetadataStream(String key, InputStream value);
    boolean metadataContains(String key, String value);

    void addBooleanAgentQueryResult(String fieldName, LanguagesEnum language, BooleanAgentQueryResult booleanAgentQueryResult);
    void addBooleanAgentQueryResult(String fieldName, BooleanAgentQueryResult booleanAgentQueryResult);
    BooleanAgentQueryResult getBooleanAgentQueryResult(String fieldName, LanguagesEnum language);
    BooleanAgentQueryResult getBooleanAgentQueryResult(String fieldName);

    Boolean getIsExcluded();
    void setIsExcluded(Boolean isExcluded);
    Integer getDepth();

    /**
     * Adds values to a label
     * @param labelName
     * @param values
     */
    void addLabelValues(String labelName, Collection<MetadataValue> values);

    /**
     * Gets the values for a label
     * @param labelName
     * @return
     */
    Collection<MetadataValue> getLabelValues(String labelName);

    /**
     * Checks if there is a label value
     * @param labelName
     * @return
     */
    boolean hasLabelValues(String labelName);

    void addConditionEvaluationResult( ConditionEvaluationResult conditionEvaluationResult, Long conditionId );
    void addConditionEvaluationResult(MatchedCondition mc);
    void addConditionEvaluationResult(UnmatchedCondition umc);
    CachedConditionEvaluationResult getConditionEvaluationResult(Long conditionId);


    void setConditionHasBeenEvaluatedThisRun( Long conditionId );
    Boolean hasConditionBeenEvaluatedThisRun( Long conditionId );

    void logTime(String category, Stopwatch stopwatch);
    void log(Logger logger);

    /**
     * Returns the value for the labelName if it exists, then the document fields if there is no label
     * @param name
     * @return
     */
    Collection<MetadataValue> getValues(String name);

    /**
     *
     * @param fieldName
     * @param language if language is null we return null
     * @param value
     */
    void addLanguageValue(String fieldName, LanguagesEnum language, MetadataValue value);

    Collection<MetadataValue> getLanguageValue(String fieldName, LanguagesEnum language);
}
