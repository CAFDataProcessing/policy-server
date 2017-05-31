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

import com.github.cafdataprocessing.corepolicy.common.dto.ReleaseHistory;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests for the Release History class.
 */
public class ReleaseHistoryTest {

    /**
     * Test that provided snapshot version is successfully parsed by ReleaseHistory into separate parts and returned as expected.
     */
    @Test
    public void testSnapshotVersion(){
        String testVersion = "1.0.0-SNAPSHOT";
        ReleaseHistory releaseHistory = new ReleaseHistory(testVersion);
        String returnedVersion = releaseHistory.version();
        Assert.assertEquals("Major version of version should be as expected.", 1, releaseHistory.majorVersion);
        Assert.assertEquals("Minor version of version should be as expected.", 0, releaseHistory.minorVersion);
        Assert.assertEquals("Revision of version should be as expected.", 0, releaseHistory.revision);
        Assert.assertEquals("Build should be as expected.", "SNAPSHOT", releaseHistory.build);
        Assert.assertEquals("String representation of version should be as expected.", testVersion, returnedVersion);
    }

    /**
     * Test that provided release build version is successfully parsed by ReleaseHistory into separate parts and returned as expected.
     */
    @Test
    public void testReleaseVersion(){
        String testVersion = "1.0.0-12";
        ReleaseHistory releaseHistory = new ReleaseHistory(testVersion);
        String returnedVersion = releaseHistory.version();
        Assert.assertEquals("Major version of version should be as expected.", 1, releaseHistory.majorVersion);
        Assert.assertEquals("Minor version of version should be as expected.", 0, releaseHistory.minorVersion);
        Assert.assertEquals("Revision of version should be as expected.", 0, releaseHistory.revision);
        Assert.assertEquals("Build should be as expected.", "12", releaseHistory.build);
        Assert.assertEquals("String representation of version should be as expected.", testVersion, returnedVersion);
    }

    /**
     * Test that a version that lacks specific build info is still parsed by ReleaseHistory into separate parts,
     * defaulting where applicable and returned as expected.
     */
    @Test
    public void testDefaultedBuildVersion(){
        String testVersion = "1.0.0";
        String expectedReturnedVersion = "1.0.0-1";
        ReleaseHistory releaseHistory = new ReleaseHistory(testVersion);
        String returnedVersion = releaseHistory.version();
        Assert.assertEquals("Major version of version should be as expected.", 1, releaseHistory.majorVersion);
        Assert.assertEquals("Minor version of version should be as expected.", 0, releaseHistory.minorVersion);
        Assert.assertEquals("Revision of version should be as expected.", 0, releaseHistory.revision);
        Assert.assertEquals("Build should be defaulted to 1 as expected.", "1", releaseHistory.build);
        Assert.assertEquals("String representation of version should be as expected.", expectedReturnedVersion, returnedVersion);
    }
}
