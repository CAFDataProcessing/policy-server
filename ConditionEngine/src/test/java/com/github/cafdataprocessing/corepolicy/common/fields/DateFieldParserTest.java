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
package com.github.cafdataprocessing.corepolicy.common.fields;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the Date Field Parser
 */
public class DateFieldParserTest {

    private DateTime getFixedDate(){
        DateTime yesterday = DateTime.now().minusDays(1);
        return new DateTime(
                yesterday.year().get(),
                yesterday.monthOfYear().get(),
                yesterday.dayOfMonth().get(),
                0, 0, 0,
                DateTimeZone.UTC);
    }


    @Before
    public void setup(){

    }

    @After
    public void cleanup(){
    }

    @Test
    public void testCanParseNegativeEpochDate() throws DateParsingException {
        EpochTime fixedTime = new EpochTime(new DateTime(1900,1,1,12,00));
        String epochValue = Long.toString(fixedTime.getSeconds());
        EpochTime result = new EpochTime(DateFieldParser.parse(epochValue));
        Assert.assertTrue(fixedTime.getSeconds() == result.getSeconds());
    }

    @Test
    public void testCanParseEpochDate() throws DateParsingException {
        EpochTime fixedTime = new EpochTime(getFixedDate());
        String epochValue = Long.toString(fixedTime.getSeconds());
        EpochTime result = new EpochTime(DateFieldParser.parse(epochValue));
        Assert.assertTrue(fixedTime.getSeconds() == result.getSeconds());
    }

    @Test (expected = DateParsingException.class)
    public void testCannotParseNonDateFormat() throws DateParsingException {
        EpochTime result = new EpochTime(DateFieldParser.parse("sdafdsfv"));
        Assert.fail("Expected an DateParsingException before now.");
    }

    @Test (expected = DateParsingException.class)
    public void testCannotParseNonSupportedDateFormat() throws DateParsingException {
        //ISO 8601 requires 4 digit year, so a 2 digit year should fail to parse.
        EpochTime result = new EpochTime(DateFieldParser.parse("13/05/97"));
        Assert.fail("Expected an DateParsingException before now.");
    }

    @Test
    public void testCanParseIso8601Date() throws DateParsingException {
        org.joda.time.format.DateTimeFormatter isoFormatter = ISODateTimeFormat.dateTime();
        String isoFormattedDate = isoFormatter.print(getFixedDate());
        System.out.println("Formatted date: " + isoFormattedDate);
        DateTime result = DateFieldParser.parse(isoFormattedDate);
        System.out.println("Returned date:  " + isoFormatter.print(result));
        Assert.assertTrue(result.isEqual(getFixedDate()));
    }
}
