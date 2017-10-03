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

import com.github.cafdataprocessing.corepolicy.common.dto.ReleaseHistory;
import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.io.Resources;

/**
 * Methods for handling of version information
 */
public class VersionNumber {

    public static String getCurrentVersion() {
        String buildVersionTag = com.github.cafdataprocessing.corepolicy.common.Version.getCurrentVersion();
        if(Strings.isNullOrEmpty(buildVersionTag) || buildVersionTag.equalsIgnoreCase("${ver.buildnum}"))
        {
            throw new RuntimeException("Invalid build number");
        }

        return buildVersionTag;
    }

    public static void isSupportedVersion(ReleaseHistory releaseHistory) throws Exception {

        // Now check that this release history item, is the same as our current one.
        ReleaseHistory currentRelease = new ReleaseHistory( VersionNumber.getCurrentVersion() );

        String supportedDatabaseVersionStr =
            Resources.toString(VersionNumber.class.getResource("/supported-database-version"), Charsets.UTF_8);
        ReleaseHistory supportedDatabaseVersion = new ReleaseHistory(supportedDatabaseVersionStr);
        if(supportedDatabaseVersion.majorVersion == releaseHistory.majorVersion &&
                supportedDatabaseVersion.minorVersion == releaseHistory.minorVersion &&
                supportedDatabaseVersion.revision == releaseHistory.revision){
            return;
        }

        // check based on first 3 parts of our release entry, that we support this DB.
        // It must match!
        if (!(( currentRelease.majorVersion == releaseHistory.majorVersion ) &&
                ( currentRelease.minorVersion == releaseHistory.minorVersion ) &&
                ( currentRelease.revision == releaseHistory.revision ) ))
        {
            throw new Exception("Current DB Version: '" + releaseHistory.version() + "' Revision: '" + releaseHistory.revisionComment +
                    "', isn't supported by api version: '" + currentRelease.version() + "'.");
        }
    }
}
