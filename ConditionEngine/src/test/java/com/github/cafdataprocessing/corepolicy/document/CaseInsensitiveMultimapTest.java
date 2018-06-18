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
package com.github.cafdataprocessing.corepolicy.document;

import com.github.cafdataprocessing.corepolicy.multimap.utils.CaseInsensitiveMultimap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the case insensitive multimap.
 */
public class CaseInsensitiveMultimapTest {
    CaseInsensitiveMultimap<String> caseInsensitiveMultimap;

    @Before
    public void setup(){
        caseInsensitiveMultimap = new CaseInsensitiveMultimap<>();
    }

    @Test
    public void addKeyTest(){
        caseInsensitiveMultimap.put("Name", "Value");
        caseInsensitiveMultimap.put("Name1", "Value");

        Assert.assertEquals(1, caseInsensitiveMultimap.get("name").size());
        Assert.assertEquals(1, caseInsensitiveMultimap.get("name1").size());
    }

    /*
    Test names with different spellings
     */
    @Test
    public void addMultipleKeyTest(){
        caseInsensitiveMultimap.put("Name", "Value");
        caseInsensitiveMultimap.put("name", "Value1");
        caseInsensitiveMultimap.put("NAME", "Value2");

        Assert.assertEquals(3, caseInsensitiveMultimap.get("name").size());
    }

    /*
    Should add one value as all values are the same
     */
    @Test
    public void addMultipleValueTest(){
        caseInsensitiveMultimap.put("Name", "Value");
        caseInsensitiveMultimap.put("Name", "Value");
        caseInsensitiveMultimap.put("Name", "Value");

        Assert.assertEquals(1, caseInsensitiveMultimap.get("name").size());
    }

    @Test
    public void putNullValueTest(){
        caseInsensitiveMultimap.put("Name", "Value");
        caseInsensitiveMultimap.put("Name", null);


        Assert.assertEquals(2, caseInsensitiveMultimap.get("name").size());
    }

    @Test(expected = NullPointerException.class)
    public void putNullNameThrowsTest(){
        caseInsensitiveMultimap.put(null, "Value");
    }

    @Test(expected = NullPointerException.class)
    public void getNullNameThrowsTest(){
        caseInsensitiveMultimap.get(null);
    }
}
