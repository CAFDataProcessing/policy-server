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
package com.github.cafdataprocessing.corepolicy.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.github.cafdataprocessing.corepolicy.common.shared.StringHelper;
import com.google.common.base.Strings;
import com.google.common.collect.Multimap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

import java.util.Collection;

/**
 *
 */
@Configuration
@PropertySource("classpath:api.properties")
@PropertySource(value = "file:${CAF_COREPOLICY_CONFIG}/api.properties", ignoreResourceNotFound = true)
public class ApiProperties {

    @Autowired
    private Environment environment;

    private final static Logger logger = LoggerFactory.getLogger(ApiProperties.class);
    private final static long DEFAULT_CACHE_MAX_AGE_MS = 600000; //10 minute cache default
    private final static long DEFAULT_CACHE_MAX_SIZE = 1000;

    public enum ApiMode {
        none,
        direct,
        web;

        @JsonCreator
        public static ApiMode forValue(String value) {
            return ApiMode.valueOf(value.toLowerCase());
        }

        @JsonValue
        public String toValue() {
            return this.name().toLowerCase();
        }
    };

    public enum ApiDirectRepository {
        none,
        mysql,
        hibernate;

        @JsonCreator
        public static ApiDirectRepository forValue(String value) {
            return ApiDirectRepository.valueOf(value.toLowerCase());
        }

        @JsonValue
        public String toValue() {
            return this.name().toLowerCase();
        }
    };

    public String getMode(){
        return environment.getProperty("api.mode");
    }

    public String getWebServiceUrl(){
        return environment.getProperty("api.webservice.url");
    }

    public String getRepository(){
        return environment.getProperty("api.direct.repository");
    }

    public Boolean getUseHttpGet() {
        return Boolean.parseBoolean(environment.getProperty("api.web.useget"));
    }

    public String getHttpClientBuilderClass() {
        return environment.getProperty("api.web.httpclientbuilderclass");
    }

    public boolean getStreamsCacheEnabled() {
        return Boolean.parseBoolean(environment.getProperty("api.streams.cache", "true"));
    }

    public boolean getAdminBaseDataEnabled(){
        // defaults to FALSE if not present.  I didn't put this value in the default api.properties file
        // as it should be a hidden property.
        return Boolean.parseBoolean(environment.getProperty("api.admin.basedata", "false"));
    }

    private long getLongWithDefaultAndMin(String propName, long minValue, long defaultValue){
        String propValue = environment.getProperty(propName);

        if(Strings.isNullOrEmpty(propValue))
            return defaultValue; //10 minutes default

        try {
            long parsedVal = Long.valueOf(propValue);
            if(parsedVal < minValue){
                logger.warn("Minimum value for " + propName +" is: " + minValue + ", using default.");
                return defaultValue;
            }
            return parsedVal;
        } catch(NumberFormatException nfe){
            logger.warn("Error parsing " + propName + " as long, using default. Value was = " + propValue);
        }

        return defaultValue;
    }

    /***
     * This is an ORd result, so if we are in any of the modes supplied we return true.
     */
    public boolean isInApiMode( Collection<ApiMode> modes )
    {
        for ( ApiProperties.ApiMode mode : modes )
        {
            if (isInApiMode(mode)) return true;
        }

        return false;
    }

    /***
     * Helper which takes a single ApiMode, and checks if we are running in that mode.
     * @param mode
     * @return
     */
    public boolean isInApiMode(ApiMode mode) {
        final ApiMode currentMode = ApiMode.forValue(getMode());
        return (currentMode == mode);
    }

    public boolean isInRepository( ApiDirectRepository repository )
    {
        final ApiDirectRepository currentMode = ApiDirectRepository.forValue(getRepository());
        return (currentMode == repository);
    }

    @Override
    public String toString(){

        // Get a string to represent our internal property state.
        Multimap<String, String> entries = new CaseInsensitiveKeyMultimap<>();
        entries.put("api.mode", getMode());
        entries.put("api.webservice.url", getWebServiceUrl());
        entries.put("api.direct.repository", getRepository());
        entries.put("api.web.useget", String.valueOf(getUseHttpGet()));
        entries.put("api.web.httpclientbuilderclass", getHttpClientBuilderClass());
        entries.put("api.streams.cache", String.valueOf(getStreamsCacheEnabled()));
        entries.put("api.admin.basedata", String.valueOf(getAdminBaseDataEnabled()));

        return StringHelper.mapToHtml(entries);
    }

}
