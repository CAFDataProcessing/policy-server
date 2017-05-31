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


import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

/**
 *
 */
public class StringHelperTests {

    @Test
    public void testCanJoinStrings(){
        Assert.assertEquals("a,b,c", StringHelper.toCSV(Arrays.asList("a", "b", "c")));
    }

    @Test
    public void testCanJoinLongs(){
        Assert.assertEquals("1,2,3", StringHelper.toCSV(Arrays.asList(1L, 2L, 3L)));
    }

    @Test
    public void testCanJoinVarargs(){
        Assert.assertEquals("a,b,c", StringHelper.toCSV("a", "b", "c"));
    }
}
