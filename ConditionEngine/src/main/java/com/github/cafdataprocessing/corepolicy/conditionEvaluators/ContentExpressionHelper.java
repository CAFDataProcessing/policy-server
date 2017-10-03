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

import com.github.cafdataprocessing.corepolicy.booleanagent.BooleanAgentServices;
import com.github.cafdataprocessing.corepolicy.document.DocumentUnderEvaluation;
import com.github.cafdataprocessing.corepolicy.booleanagent.BooleanAgentQueryResult;
import com.github.cafdataprocessing.corepolicy.common.EngineProperties;
import com.github.cafdataprocessing.corepolicy.common.IsoToLanguageEnumConverter;
import com.github.cafdataprocessing.corepolicy.common.LanguagesEnum;
import com.github.cafdataprocessing.corepolicy.common.MatcherWithTimeout;
import com.github.cafdataprocessing.corepolicy.common.exceptions.BackEndRequestFailedCpeException;
import com.github.cafdataprocessing.corepolicy.common.exceptions.CpeException;
import com.github.cafdataprocessing.corepolicy.common.shared.MetadataValue;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 *
 */
@Component
public class ContentExpressionHelper {
    private RegexMatcherFactory matcherFactory;
    private final EngineProperties engineProperties;

    @Autowired
    public ContentExpressionHelper(RegexMatcherFactory matcherFactory,
                                   EngineProperties engineProperties){
        this.matcherFactory = matcherFactory;
        this.engineProperties = engineProperties;
    }

    public BooleanAgentQueryResult handleBooleanAgentExpression(String instanceId, BooleanAgentServices booleanAgentServices,
                                                                DocumentUnderEvaluation document, String fieldName,
                                                                String languageCode) throws CpeException {

        LanguagesEnum languagesEnum = null;
        if(languageCode!=null){
            languagesEnum = IsoToLanguageEnumConverter.convert(languageCode.toLowerCase());
        }
        final LanguagesEnum finalLanguagesEnum = languagesEnum;

        final Collection<MetadataValue> returnedFieldValues = getFieldValues(document, fieldName).stream().
                filter(fv -> isValidForLanguage(finalLanguagesEnum, fv)).collect(Collectors.toList());

        BooleanAgentQueryResult booleanAgentQueryResult;
        if(languagesEnum == null){
            booleanAgentQueryResult = document.getBooleanAgentQueryResult(fieldName);
        } else {
            booleanAgentQueryResult = document.getBooleanAgentQueryResult(fieldName, languagesEnum);
        }

        if(booleanAgentQueryResult == null){
            try {
                booleanAgentQueryResult = booleanAgentServices.query(instanceId, returnedFieldValues);
            }
            catch (Exception e) {
                throw new BackEndRequestFailedCpeException(e);
            }
            if(languagesEnum == null){
                document.addBooleanAgentQueryResult(fieldName, booleanAgentQueryResult);
            } else {
                document.addBooleanAgentQueryResult(fieldName, languagesEnum, booleanAgentQueryResult);
            }
        }

        return booleanAgentQueryResult;
    }

    public Collection<String> handleRegexExpression(DocumentUnderEvaluation document, String fieldName, String expression)
            throws CpeException {
        Pattern pattern = matcherFactory.getPattern(expression);
        HashSet<String> matchedStrings = new HashSet<>();

        final Collection<MetadataValue> returnedfieldValues = getFieldValues(document, fieldName);

        getMatches(pattern, returnedfieldValues, matchedStrings);

        return matchedStrings;
    }

    private Collection<MetadataValue> getFieldValues(DocumentUnderEvaluation document, String fieldName) {
        final Collection<MetadataValue> fieldValues = new ArrayList<>();
        fieldValues.addAll(document.getValues(fieldName));
        return fieldValues;
    }

    private void getMatches(Pattern pattern, Collection<MetadataValue> fieldValues, HashSet<String> matchedStrings) {

        for(MetadataValue fieldValue: fieldValues){
            MatcherWithTimeout matcher = new MatcherWithTimeout(pattern.matcher(fieldValue.getStringValue()),
                    engineProperties.getRegexTimeout()) ;
            while(matcher.find()){
                matchedStrings.add(matcher.group());
            }
        }
    }

    //https://docs.oracle.com/javase/8/docs/api/java/lang/Character.UnicodeScript.html
    //http://en.wikipedia.org/wiki/Script_%28Unicode%29
    //http://www.unicode.org/notes/tn26/
    private static Pattern chinesePattern = Pattern.compile("\\p{IsHan}");
    private static Pattern thaiPattern = Pattern.compile("\\p{IsThai}");
    private static Pattern koreanPattern = Pattern.compile("\\p{IsHangul}|\\p{IsHan}");
    private static Pattern japanesePattern = Pattern.compile("\\p{IsHan}|\\p{IsHiragana}|\\p{IsKatakana}");

    private boolean isValidForLanguage(LanguagesEnum language, MetadataValue metadataValue){
        String value = metadataValue.getAsString();

        if(StringUtils.isEmpty(value)){return false;}

        if(language==null){
            return true;
        }

        switch (language){
            case Chinese:{
                return chinesePattern.matcher(value).find();
            }
            case Korean:{
                return koreanPattern.matcher(value).find();
            }
            case Japanese:{
                return japanesePattern.matcher(value).find();
            }
            case Thai:{
                return thaiPattern.matcher(value).find();
            }
            default: {
                return true;
            }
        }
    }
}
