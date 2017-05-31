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
package com.github.cafdataprocessing.corepolicy.common.shared;

import com.github.cafdataprocessing.corepolicy.common.fields.EpochTime;
import com.google.common.base.Strings;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Collection;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Helper class for dealing with dates.
 */
public class DateHelper {

    private static final Pattern periodPattern = Pattern.compile("^[P|T][0-9].*$", Pattern.DOTALL);

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormat.forPattern("yyyyMMdd");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormat.forPattern("yyyyMMddHH");

    public static boolean isValidString(String value){
        return DateHelper.isPeriod(value) || DateHelper.isTime(value) || DateHelper.isDay(value);
    }

    public static boolean isDateTime(String value) {
        try {
            DateTime dateTime = getDateTime(value);

            return dateTime != null;
        } catch (Exception e) {
            return false;
        }
    }

    public static DateTime getDateTime(String value)
    {
        DateTime dateTime;
        if(value.endsWith("e")){
            dateTime = new EpochTime(Long.parseLong(value.substring(0, value.length() - 1))).getUtcDateTime();
        }
        else {
            dateTime = DateTime.parse(value);
        }
        return dateTime;
    }

    /**
     * Util function to represent a date as a long with hourly resolution as yyyyMMddHH.
     * e.g.11:41AM 10th March 2015 is 2015031011. Interprets null as 0.
     * @param dateTime DateTime to convert
     * @return long representation of the date
     */
    public static long dateToLongHours(DateTime dateTime){
        if(dateTime == null)
            return 0;

        return Long.parseLong(dateTime.toString(DATE_TIME_FORMATTER));
    }

    /**
     * Util function to represent a date as a long with daily resolution as yyyyMMddHH.
     * e.g.11:41AM 10th March 2015 is 20150310. Interprets null as 0.
     * @param dateTime DateTime to convert
     * @return long representation of the date
     */
    public static long dateToLongDays(DateTime dateTime){
        if(dateTime == null)
            return 0;

        return Long.parseLong(dateTime.toString(DATE_FORMATTER));
    }

    /**
     * Check if a string is a time (24 hour clock)
     * @param value
     * @return true if its a valid time, false otherwise
     */
    public static boolean isTime(String value){
        String pattern = "^([0-9]|0[0-9]|1[0-9]|2[0-3])(:[0-5][0-9]){1,2}?$";
        return value.matches(pattern);
    }

    /**
     * Check if a string a days
     * @param value
     * @return true if its a valid days, false otherwise
     */
    public static boolean isDay(String value){
        return getNumberedDay(value) > 0;
    }

    /**
     * Check if it is a period
     * @param value
     * @return true if its a valid days, false otherwise
     */
    public static boolean isPeriod(String value){
        Matcher matcher = periodPattern.matcher(value);

        return matcher.matches();
    }

    /**
     * Gets the corresponding number representing a day
     * @param value
     * @return mon = 1, tue = 2, wed = 3, thu = 4, fri = 5, sat = 6, sun = 7 if no matches then -1
     */
    public static int getNumberedDay(String value){
        if(Strings.isNullOrEmpty(value) || !value.matches("^[A-z]+$")){
            return -1;
        }
        LinkedList<String> days = (LinkedList<String>)getDayList();
        String normalisedValue = value.toLowerCase().substring(0, 3);

        int indexOf = days.indexOf(normalisedValue);

        return indexOf == -1 ? -1 : indexOf + 1;
    }


    private static Collection<String> getDayList(){
        Collection<String> dayList = new LinkedList<>();
        dayList.add("mon");
        dayList.add("tue");
        dayList.add("wed");
        dayList.add("thu");
        dayList.add("fri");
        dayList.add("sat");
        dayList.add("sun");

        return dayList;
    }
}
