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

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.github.cafdataprocessing.corepolicy.common.exceptions.BackEndRequestFailedCpeException;
import com.github.cafdataprocessing.corepolicy.common.exceptions.CpeException;
import com.github.cafdataprocessing.corepolicy.common.EngineProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
* Compiler/cache of regular expressions.
*/
@Component
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class RegexMatcherFactoryImpl implements RegexMatcherFactory {
    private final static Logger logger = LoggerFactory.getLogger(RegexMatcherFactoryImpl.class);

    private LoadingCache<String, Pattern> patternCache;

    @Autowired
    public RegexMatcherFactoryImpl(EngineProperties engineProperties){

        Integer maxPatternCacheSize = 100000;
        Integer patternCacheExpiryHours = 24;

        try {
            maxPatternCacheSize = engineProperties.getRegexCacheMaxsize();
            patternCacheExpiryHours = engineProperties.getRegexCacheExpiryHours();
        } catch (Exception e) {
            logger.warn("Problem loading pattern cache settings from config, using default.", e);
        }

        patternCache = CacheBuilder
                .newBuilder()
                .maximumSize(maxPatternCacheSize)
                .expireAfterAccess(patternCacheExpiryHours, TimeUnit.HOURS)
                .build(
                    new CacheLoader<String, Pattern>() {
                       public Pattern load(String regexString) throws Exception{
                           return Pattern.compile(regexString);
                       }
                    }
                );
    }

    public Pattern getPattern(String regexString) throws CpeException {
        try {
            return patternCache.get(regexString);
        }
        catch (Exception ex){
            throw new BackEndRequestFailedCpeException(ex);
        }
    }
}
