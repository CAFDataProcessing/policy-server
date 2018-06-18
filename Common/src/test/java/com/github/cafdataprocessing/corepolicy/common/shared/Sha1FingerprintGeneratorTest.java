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
package com.github.cafdataprocessing.corepolicy.common.shared;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 */
public class Sha1FingerprintGeneratorTest {


    /**
     * Sanity check, this sha1 hash was generated externally, I'm just verifying that any changes made for e.g.
     * efficiency result in the same hash.
     */
    @Test
    public void testShaHashFunction(){
        Sha1FingerprintGenerator sut = new Sha1FingerprintGenerator();
        Assert.assertEquals("E2F67C772368ACDEEE6A2242C535C6CC28D8E0ED", sut.generate("This is a test string"));
    }
}
