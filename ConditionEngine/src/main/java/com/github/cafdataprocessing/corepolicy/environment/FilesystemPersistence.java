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
package com.github.cafdataprocessing.corepolicy.environment;

import com.github.cafdataprocessing.corepolicy.common.EngineProperties;
import com.github.cafdataprocessing.corepolicy.common.domainModels.EnvironmentSnapshotImpl;
import com.github.cafdataprocessing.corepolicy.common.shared.CorePolicyLogger;
import com.github.cafdataprocessing.corepolicy.common.shared.CorePolicyObjectMapper;
import com.github.cafdataprocessing.corepolicy.common.shared.Level;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 *
 */
public class FilesystemPersistence implements EnvironmentInitializer {
    private final EngineProperties engineProperties;
    CorePolicyObjectMapper objectMapper = new CorePolicyObjectMapper();

    @Autowired
    public FilesystemPersistence(EngineProperties engineProperties) {
        this.engineProperties = engineProperties;
    }

    @Override
    public EnvironmentSnapshotImpl initialize(EnvironmentSnapshotImpl environmentSnapshot) {
        try(CorePolicyLogger traceLogger = new CorePolicyLogger("FileSystemPersisence:initialize", Level.DEBUG)) {
            if (environmentSnapshot.getPersistedDate() != null) {
                traceLogger.log("snapshot has already been persisted - nothing to do.");
                return environmentSnapshot;
            }

            environmentSnapshot.setPersistedDate(DateTime.now(DateTimeZone.UTC));

            String filename = String.valueOf(environmentSnapshot.getCollectionSequenceId()) + "-" + environmentSnapshot.getInstanceId() + ".json";
            Path cacheFilePath = Paths.get(engineProperties.getEnvironmentCacheLocation(), filename);
            File file = cacheFilePath.toFile();
            traceLogger.log("Persisting as: " + cacheFilePath);

            try {
                objectMapper.writeValue(file, environmentSnapshot);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            return environmentSnapshot;
        }
    }

    @Override
    public EnvironmentSnapshotImpl remove(EnvironmentSnapshotImpl environmentSnapshot){

        try(CorePolicyLogger traceLogger = new CorePolicyLogger("FileSystemPersisence:remove", Level.DEBUG)) {
            // If this snapshot has been persisted, then remove it, otherwise
            // nothing to do here
            if (environmentSnapshot.getPersistedDate() == null) {
                traceLogger.log("No persisted date - nothing to do.");
                return environmentSnapshot;
            }

            // if cache is already invalidated return now.
            if (environmentSnapshot.getInvalidatedCache()) {
                traceLogger.log("Cache already invalidated - nothing to do.");
                return environmentSnapshot;
            }

            String filename = String.valueOf(environmentSnapshot.getCollectionSequenceId()) + "-" + environmentSnapshot.getInstanceId() + ".json";
            Path cacheFilePath = Paths.get(engineProperties.getEnvironmentCacheLocation(), filename);
            File file = cacheFilePath.toFile();

            environmentSnapshot.setInvalidatedCache(true);

            traceLogger.log("Updating persisted item with invalidated marker - location: " + cacheFilePath);

            try {
                // we could delete the file, but my issue with that is nothing would then tidy up the booleanagents
                // until they finally expire themselves.  Better to mark something in the file
                // and leave it there to be used by removeReallyOldFiles ( our expiry code ).
                objectMapper.writeValue(file, environmentSnapshot);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }


            return environmentSnapshot;
        }
    }
}
