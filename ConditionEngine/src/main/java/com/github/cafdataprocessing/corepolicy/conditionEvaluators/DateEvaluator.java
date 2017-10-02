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

import com.github.cafdataprocessing.corepolicy.document.DocumentUnderEvaluation;
import com.google.common.base.Strings;
import com.github.cafdataprocessing.corepolicy.common.ApiProperties;
import com.github.cafdataprocessing.corepolicy.common.dto.conditions.DateCondition;
import com.github.cafdataprocessing.corepolicy.common.dto.conditions.DateOperator;
import com.github.cafdataprocessing.corepolicy.common.fields.DateFieldParser;
import com.github.cafdataprocessing.corepolicy.common.fields.DateParsingException;
import com.github.cafdataprocessing.corepolicy.common.shared.DateHelper;
import com.github.cafdataprocessing.corepolicy.common.shared.MetadataValue;
import com.github.cafdataprocessing.corepolicy.common.EnvironmentSnapshot;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalTime;
import org.joda.time.Period;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.function.BiFunction;

/**
 * Evaluator for the DateCondition.
 */
@Component("DateCondition")
public class DateEvaluator extends FieldConditionEvaluator<DateCondition> {

    @Autowired
    public DateEvaluator(ApiProperties apiProperties ) {
        super(apiProperties);
    }

    @Override
    protected void evaluateFieldValues(ConditionEvaluationResult result, DateCondition condition, DocumentUnderEvaluation document, EnvironmentSnapshot environmentSnapshot) {
        DateTime targetDate = null;

        BiFunction<DateTime, DateOperator, Boolean> dateComparer = null;
        if(DateHelper.isPeriod(condition.value)){
            final Period period = Period.parse(condition.value);
            targetDate = new DateTime(DateTimeZone.UTC);
            targetDate = targetDate.minus(period);

            dateComparer = getTwoDatesComparer(targetDate);
        } else if(DateHelper.isTime(condition.value)){

            LocalTime localTime = LocalTime.parse(condition.value, ISODateTimeFormat.localTimeParser().withZoneUTC());
            dateComparer = getTwoTimesComparer(localTime);

        } else if(DateHelper.isDay(condition.value)) {
            dateComparer = getDayComparer(condition.value);
        } else {
            try {
                targetDate =  DateFieldParser.parse(condition.value);
            } catch (DateParsingException e) {
                targetDate = null;
            }
            dateComparer = getTwoDatesComparer(targetDate);
        }

        for(MetadataValue fieldValue : document.getValues(condition.field)){

            DateTime fieldDataTimeValue;
            try{
                fieldDataTimeValue = DateFieldParser.parse(fieldValue.getAsString());
            }
            catch(DateParsingException ex){
                //ignore parsing errors for now
                continue;
            }

            if(dateComparer.apply(fieldDataTimeValue, condition.operator)){
                result.setMatch(true);
                break;
            }
        }

        result.populateEvaluationResult(result.isMatch(), condition, document, true );
    }

    private BiFunction<DateTime, DateOperator, Boolean> getDayComparer(String day) {
        return (DateTime date, DateOperator dateOperator) -> {
            if(Strings.isNullOrEmpty(day) || date == null){
                return false;
            }
            int numberedDay = DateHelper.getNumberedDay(day);
            if(numberedDay == -1){
                return false;
            }
            int result = Integer.compare(date.getDayOfWeek(), numberedDay);
            return resultMatchesOperator(result, dateOperator);
        };
    }

    private BiFunction<DateTime, DateOperator, Boolean> getTwoDatesComparer(DateTime targetDate) {
        return (DateTime date, DateOperator dateOperator) -> {
            if(targetDate == null || date == null){
                return false;
            }
            int result = date.getMillis()==targetDate.getMillis() ? 0 : date.getMillis() <  targetDate.getMillis() ? -1 : 1;
//            int result = date.compareTo(targetDate);
            return resultMatchesOperator(result, dateOperator);
        };
    }

    private BiFunction<DateTime, DateOperator, Boolean> getTwoTimesComparer(LocalTime targetDate) {
        return (DateTime date, DateOperator dateOperator) -> {
            if (targetDate == null || date == null) {
                return false;
            }

            int result = date.toLocalTime().compareTo(targetDate);
            return resultMatchesOperator(result, dateOperator);
        };
    }

    private Boolean resultMatchesOperator(int result, DateOperator dateOperator){
        switch (dateOperator) {
            case ON: {
                return result == 0;
            }
            case BEFORE: {
                return result == -1;
            }
            case AFTER: {
                return result == 1;
            }
            default:{
                return false;
            }
        }
    }

}
