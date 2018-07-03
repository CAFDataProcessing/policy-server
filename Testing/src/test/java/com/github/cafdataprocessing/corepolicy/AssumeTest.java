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
package com.github.cafdataprocessing.corepolicy;

import com.github.cafdataprocessing.corepolicy.testing.Assume;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for Assume class
 */
@RunWith(MockitoJUnitRunner.class)
public class AssumeTest {
    protected static Logger logger = LoggerFactory.getLogger(AssumeTest.class);

    @Test
    public void TestUnclassified()
    {
        logger.error("Test put any debug message out here as it shouldn't go into our report!");

        Assume.assumeTrue(Assume.AssumeReason.UNCLASSIFIED, "TestUnclassified", "PD-999", "My description - TestUnclassified", false, null);
    }

    @Test
    public void TestBug() {

        logger.debug( "Test put any debug message out here as it shouldn't go into our report!");

        Assume.assumeTrue(Assume.AssumeReason.BUG, "TestBug", "PD-999", "My description- TestBug", false, null);
    }

    @Test
    public void TestDebt() {

        logger.info ("Test put any info message out, and ensure its not in the report");
        Assume.assumeTrue(Assume.AssumeReason.DEBT, "TestDebt", "PD-999", "My description - TestDebt", false, null);
    }

    @Test
    public void Test_ByDesign() {

        logger.debug("Test put any info message out, and ensure its not in the report");

        Assume.assumeTrue(Assume.AssumeReason.BY_DESIGN, "Test_ByDesign", "PD-999", "My description - Test-ByDesign", false, null);
    }

}
