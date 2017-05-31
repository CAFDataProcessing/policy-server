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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.github.cafdataprocessing.corepolicy.common.dto.ReleaseHistory;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Tests for VersionNumber class
 */
@RunWith(MockitoJUnitRunner.class)
public class VersionNumberTest {

    @Test(expected = Exception.class)
    public void testHigherMajorVersionThrows() throws Exception
    {
        ReleaseHistory version = new ReleaseHistory(VersionNumber.getCurrentVersion());

        // Now bump up the major version by 1.
        version.majorVersion+=1;

        VersionNumber.isSupportedVersion(version);
    }

    @Test(expected = Exception.class)
    public void testHigherMinorVersionThrows() throws Exception
    {
        ReleaseHistory version = new ReleaseHistory(VersionNumber.getCurrentVersion());

        // Now bump up the major version by 1.
        version.minorVersion+=1;

        VersionNumber.isSupportedVersion(version);
    }

    @Test(expected = Exception.class)
    public void testHigherPatchRevisionThrows() throws Exception
    {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new GuavaModule());
        String supportedDatabaseVersionStr =
                Resources.toString(VersionNumber.class.getResource("/supported-database-version"), Charsets.UTF_8);
        ReleaseHistory version = new ReleaseHistory(supportedDatabaseVersionStr);

        // Now bump up the revision version by 1.
        version.revision+=1;

        VersionNumber.isSupportedVersion(version);
    }

    @Test(expected = Exception.class)
    public void testLowerMajorVersionThrows() throws Exception
    {
        ReleaseHistory version = new ReleaseHistory(VersionNumber.getCurrentVersion());

        // Now bump up the major version by 1.
        version.majorVersion-=1;

        VersionNumber.isSupportedVersion(version);
    }

    @Test(expected = Exception.class)
    public void testLowerMinorVersionThrows() throws Exception
    {
        ReleaseHistory version = new ReleaseHistory(VersionNumber.getCurrentVersion());

        // Now bump up the major version by 1.
        version.minorVersion-=1;

        VersionNumber.isSupportedVersion(version);
    }

    @Test(expected = Exception.class)
    public void testLowerPatchRevisionThrows() throws Exception
    {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new GuavaModule());
        String supportedDatabaseVersionStr =
                Resources.toString(VersionNumber.class.getResource("/supported-database-version"), Charsets.UTF_8);
        ReleaseHistory version = new ReleaseHistory(supportedDatabaseVersionStr);

        // Now lower the revision version by 1.
        version.revision-=1;

        VersionNumber.isSupportedVersion(version);
    }

    @Test
    public void testCurrentVersionDefaultsToSupported() throws Exception
    {
        ReleaseHistory version = new ReleaseHistory(VersionNumber.getCurrentVersion());

        // default version should be supported.
        VersionNumber.isSupportedVersion(version);
    }

    @Test
    public void testBuildNumberIsIgnored() throws Exception
    {
        ReleaseHistory version = new ReleaseHistory(VersionNumber.getCurrentVersion());

        // default version should be supported.
        // Now change the build version and verify it is ignored.
        version.build += "100";

        VersionNumber.isSupportedVersion(version);
    }
}
