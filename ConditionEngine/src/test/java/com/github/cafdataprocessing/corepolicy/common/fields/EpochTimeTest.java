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
package com.github.cafdataprocessing.corepolicy.common.fields;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the EpochTime class.
 */
public class EpochTimeTest {
    @Before
    public void setup(){

    }

    @After
    public void cleanup(){

    }

    @Test
    public void checkSettingViaSeconds(){
        long epochSeconds = 123874;
        EpochTime epoch = new EpochTime(epochSeconds);

        Assert.assertEquals(epochSeconds, epoch.getSeconds());
    }

    @Test
    public void checkSettingViaDateTime(){
        long epochSeconds = 1232458;
        long epochMillis = epochSeconds*1000;
        DateTime dateRepresentation = new DateTime(epochMillis, DateTimeZone.UTC);

        EpochTime epoch = new EpochTime(dateRepresentation);

        Assert.assertEquals(epochSeconds, epoch.getSeconds());
    }

    @Test
    public void checkGetUtcDateTime(){
        long epochSeconds = 1232458;
        long epochMillis = epochSeconds*1000;
        DateTime dateRepresentation = new DateTime(epochMillis, DateTimeZone.UTC);

        EpochTime epoch = new EpochTime(epochSeconds);

        Assert.assertTrue(dateRepresentation.equals(epoch.getUtcDateTime()));
    }

    @Test
    public void checkGetDateTimeOtherTimeZone(){
        long epochSeconds = 1232458;
        long epochMillis = epochSeconds*1000;
        DateTime dateRepresentation = new DateTime(epochMillis, DateTimeZone.forOffsetHours(4));

        EpochTime epoch = new EpochTime(epochSeconds);

        Assert.assertTrue(dateRepresentation.equals(epoch.getDateTime(DateTimeZone.forOffsetHours(4))));
    }
}
