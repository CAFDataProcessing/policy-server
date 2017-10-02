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

/**
 *
 * This class will have a resolution of seconds. When passing a DateTime with milliseconds, they will be truncated.
 */
public class EpochTime {

    private long epochSeconds;

    public EpochTime(long epochTimeSeconds){
        this.epochSeconds = epochTimeSeconds;
    }

    public EpochTime(DateTime dateTime){
        this.epochSeconds = dateTime.toDateTime(DateTimeZone.UTC).getMillis() / 1000;
    }

    public long getSeconds(){
        return this.epochSeconds;
    }

    public DateTime getUtcDateTime(){
        return new DateTime(this.epochSeconds * 1000, DateTimeZone.UTC);
    }

    public DateTime getDateTime(DateTimeZone dateTimeZone){
        return new DateTime(this.epochSeconds * 1000, dateTimeZone);
    }

    @Override
    public String toString(){
        return Long.toString(getSeconds());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EpochTime epochTime = (EpochTime) o;

        if (epochSeconds != epochTime.epochSeconds) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return (int) (epochSeconds ^ (epochSeconds >>> 32));
    }
}
