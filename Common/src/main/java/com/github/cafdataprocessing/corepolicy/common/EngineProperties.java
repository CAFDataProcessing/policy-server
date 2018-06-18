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
package com.github.cafdataprocessing.corepolicy.common;

import com.github.cafdataprocessing.corepolicy.common.shared.StringHelper;
import com.google.common.collect.Multimap;
import org.joda.time.Period;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * Defines properties that describe behaviour of the condition engine.
 */
@Configuration
@PropertySource("classpath:engine.properties")
@PropertySource(value = "file:${CAF_COREPOLICY_CONFIG}/engine.properties", ignoreResourceNotFound = true)
public class EngineProperties extends PropertiesBase{

    private static int engineEnvironmentcacheExpiryhoursDefault = 24;

    public String getMode(){
        return environment.getProperty("engine.mode");
    }

    public Integer getRegexCacheMaxsize(){
        return Integer.parseInt(environment.getProperty("engine.regexcache.maxsize"));
    }

    public Integer getRegexCacheExpiryHours(){
        return Integer.parseInt(environment.getProperty("engine.regexcache.expiryhours"));
    }

    @Deprecated
    public Period getEnvironmentCacheMaxage(){
        return Period.parse(environment.getProperty("engine.environmentcache.maxage"));
    }

    public Integer getEnvironmentCacheMaxsize(){
        return Integer.parseInt(environment.getProperty("engine.environmentcache.maxsize"));
    }

    public Integer getEnvironmentCacheExpiryHours(){
        String engineEnvironmentcacheExpiryhours = environment.getProperty("engine.environmentcache.expiryhours");
        return engineEnvironmentcacheExpiryhours == null ? engineEnvironmentcacheExpiryhoursDefault : Integer.parseInt(engineEnvironmentcacheExpiryhours);
    }

    public Period getEnvironmentCacheExpiry(){
        String engineEnvironmentcacheExpiry = environment.getProperty("engine.environmentcache.expiry");
        if (engineEnvironmentcacheExpiry == null) {
            engineEnvironmentcacheExpiry = "PT" + getEnvironmentCacheExpiryHours() + "H";
        }
        return Period.parse(engineEnvironmentcacheExpiry);
    }

    public String getEnvironmentCacheVerifyPeriod(){
        return environment.getProperty("engine.environmentcache.verifyperiod");
    }

    public String getEnvironmentCacheLocation() {
        return environment.getProperty("engine.environmentcache.location", ".");
    }

    public String getEnvironmentCacheMode() { return environment.getProperty("engine.environmentcache.mode", "memory"); }

    public Integer getRegexTimeout(){
        return Integer.parseInt(environment.getProperty("engine.regextimeout"));
    }

    public String getHashPassword(){
        return getSetting("engine.hash.password", "{g<tg}>Gc%PbtC$uY4xx4>#H)FX}*'");
    }

    public Integer getDefaultBatchSize(){
        //Returns the number of expressions to insert per call.
        return Integer.parseInt(environment.getProperty("engine.defaultbatchsize"));
    }


    @Override
    public String toString(){

        // Get a string to represent our internal property state.
        Multimap<String, String> entries = new CaseInsensitiveKeyMultimap<>();
        entries.put("engine.mode", getMode());
        entries.put("engine.regexcache.maxsize", String.valueOf(getRegexCacheMaxsize()));
        entries.put("engine.regexcache.expiryhours", String.valueOf(getRegexCacheExpiryHours()));
        entries.put("engine.environmentcache.maxage", String.valueOf(getEnvironmentCacheMaxage()));
        entries.put("engine.environmentcache.maxsize", String.valueOf(getEnvironmentCacheMaxsize()));
        entries.put("engine.environmentcache.expiryhours", String.valueOf(getEnvironmentCacheExpiryHours()));
        entries.put("engine.environmentcache.expiry", String.valueOf(getEnvironmentCacheExpiry()));
        entries.put("engine.environmentcache.verifyperiod", getEnvironmentCacheVerifyPeriod());
        entries.put("engine.environmentcache.location", getEnvironmentCacheLocation());
        entries.put("engine.environmentcache.mode", getEnvironmentCacheMode());
        entries.put("engine.regextimeout", String.valueOf(getRegexTimeout()));
        return StringHelper.mapToHtml(entries);
    }

}

