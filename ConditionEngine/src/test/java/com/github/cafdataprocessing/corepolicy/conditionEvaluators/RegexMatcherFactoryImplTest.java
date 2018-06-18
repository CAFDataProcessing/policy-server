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
package com.github.cafdataprocessing.corepolicy.conditionEvaluators;

import com.github.cafdataprocessing.corepolicy.common.EngineProperties;
import com.github.cafdataprocessing.corepolicy.common.exceptions.CpeException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class RegexMatcherFactoryImplTest {

    @Mock
    EngineProperties engineProperties;

    @Before
    public void before(){
        when(engineProperties.getRegexCacheMaxsize()).thenReturn(100000);
        when(engineProperties.getRegexCacheExpiryHours()).thenReturn(24);
    }

    private RegexMatcherFactoryImpl getFactory() {
        return new RegexMatcherFactoryImpl(engineProperties);
    }

    @Test
    public void testGetPattern() throws Exception {
        RegexMatcherFactoryImpl factory = getFactory();
        final Pattern pattern = factory.getPattern("abc");
        Matcher m = pattern.matcher("anshabcderj");

        Assert.assertTrue(m.find());

    }

    @Test(expected = CpeException.class)
    public void testGetPatternBadThrows() throws CpeException{
        RegexMatcherFactoryImpl factory = getFactory();

        factory.getPattern("[");
        Assert.fail("Expected exception for invalid match.");
    }

    /**
     * Get two patterns for the same string, check they are reference equals.
      * @throws CpeException
     */
    @Test
    public void testSamePatternSameObject() throws CpeException {
        RegexMatcherFactoryImpl factory = getFactory();
        final Pattern p1 = factory.getPattern("abc");
        final Pattern p2 = factory.getPattern("abc");

        Assert.assertTrue(p1 == p2);
    }
}
