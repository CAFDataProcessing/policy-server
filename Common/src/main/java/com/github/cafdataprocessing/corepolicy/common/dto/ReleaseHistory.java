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
package com.github.cafdataprocessing.corepolicy.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.InvalidParameterException;

/**
 * Represents release history information for a version of Policy components.
 */
public class ReleaseHistory {

    private static Logger logger = LoggerFactory.getLogger(ReleaseHistory.class);

    public ReleaseHistory(){

    }

    public ReleaseHistory(String versionString) {
        String[] items = versionString.split("\\.");

        //supporting input versions as produced by current build
        if ( items.length != 3  )
        {
            throw new InvalidParameterException("Invalid version string: " +  versionString +
                                        " is not of correct format. [Major.minor.revision]");
        }

        this.majorVersion = Short.valueOf(items[0]);
        this.minorVersion = Short.valueOf(items[1]);
        //split the revision on '-' so we can split versions as produced by build machine, e.g. '0-SNAPSHOT' and '0-12'
        String[] splitRevisionAndBuild = items[2].split("-");
        this.revision = Short.valueOf(splitRevisionAndBuild[0]);
        if(splitRevisionAndBuild.length < 2){
            //if no '-' separator then default to 1 as build specific information is not available
            this.build = "1";
        }
        else{
            this.build = splitRevisionAndBuild[1];
        }

        this.id = 0L;

        this.revisionComment = "Current Api Version";
        this.dateStamp = DateTime.now();
    }

    @JsonProperty("release_history_id")
    public Long id;

    @JsonProperty("release_comment")
    public String revisionComment;

    @JsonProperty("major_version")
    public short majorVersion;

    @JsonProperty("minor_version")
    public short minorVersion;

    public short revision;
    public String build;

    @JsonProperty("date_stamp")
    public DateTime dateStamp;

    public String version() {
        // return the current version parts, as a single entity.
        return majorVersion + "." + minorVersion + "." + revision + "-" + build;
    }

    @Override
    public String toString() {

        // schema may not contain timestamp like h2, as such just leave it out.
        if ( dateStamp == null ) {
            return String.format("Id: %d  Build: %d.%d.%d-%d  Comment: %s.",
                    id, majorVersion, minorVersion, revision, build, revisionComment );
        }

        return String.format("Id: %d  Build: %d.%d.%d-%d  Comment: %s  DateStamp: %s",
                id, majorVersion, minorVersion, revision, build, revisionComment, dateStamp.toString());
    }
}
