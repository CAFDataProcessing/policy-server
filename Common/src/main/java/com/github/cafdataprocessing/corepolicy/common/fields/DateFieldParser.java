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
package com.github.cafdataprocessing.corepolicy.common.fields;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

/**
 * Class is used to parse the values of Date condition value into a DateTime object. Only supports Epoch Seconds right now.
 */
public class DateFieldParser {
    public static DateTime parse(String fieldValue) throws DateParsingException {
        try{
            String epochTime = fieldValue.replace("e", "");
            long epochSeconds = Long.parseLong(epochTime);
            return new DateTime(epochSeconds * 1000, DateTimeZone.UTC); //joda time requires epoch milliseconds
        }
        catch (NumberFormatException ex){
            //failed parsing as an epoch, so lets try parsing as ISO

            //DateTimeFormatter fmt = ISODateTimeFormat.dateTimeParser();
            DateTimeFormatter fmt = ISODateTimeFormat.dateTimeParser().withZoneUTC();

            try {
                DateTime time = fmt.parseDateTime(fieldValue);
                return time;
            }
            catch(IllegalArgumentException dtParseEx){
                throw new DateParsingException("Could not parse Date field: " + fieldValue, dtParseEx);
            }
        }
    }
}
