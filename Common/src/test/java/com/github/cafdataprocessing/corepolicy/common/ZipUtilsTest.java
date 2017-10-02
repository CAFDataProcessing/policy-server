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
package com.github.cafdataprocessing.corepolicy.common;


import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for ZipUtils class
 */
@RunWith(MockitoJUnitRunner.class)
public class ZipUtilsTest {

    private Logger logger = LoggerFactory.getLogger(ZipUtilsTest.class);

    @Test
    public void testCompressAndDecompress() throws Exception {

        String original = "Testmewith some big long string, it should really be of the length of 100 bytes or more to compress correctly." +
                "as it usually would have an overhead associated with it.  We should also repeat words to ensure compression is helped on its way!";

        String compression = ZipUtils.compressStringAndEncode( original );

        String decompressed = ZipUtils.decompressEncodedString( compression );

        byte[] test = ZipUtils.compress(original);

        String test2Decompressed = ZipUtils.decompress(test);


        Assert.assertEquals("Decompressed string should match original", original, decompressed);
        Assert.assertEquals( "Decompressed string should match original", original, test2Decompressed);


    }
}
