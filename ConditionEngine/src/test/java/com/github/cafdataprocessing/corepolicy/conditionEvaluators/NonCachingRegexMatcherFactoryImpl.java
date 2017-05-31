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
package com.github.cafdataprocessing.corepolicy.conditionEvaluators;

import com.github.cafdataprocessing.corepolicy.common.exceptions.CpeException;

import java.util.regex.Pattern;

/**
 * Non caching implementation to use in tests, just compiles a Pattern and returns.
 */
public class NonCachingRegexMatcherFactoryImpl implements RegexMatcherFactory {
    @Override
    public Pattern getPattern(String regexString) throws CpeException {
        return Pattern.compile(regexString);
    }
}
