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
package com.github.cafdataprocessing.corepolicy.environment;

import com.github.cafdataprocessing.corepolicy.booleanagent.BooleanAgentServices;
import com.github.cafdataprocessing.corepolicy.common.EngineProperties;
import com.github.cafdataprocessing.corepolicy.common.domainModels.EnvironmentSnapshotImpl;
import com.github.cafdataprocessing.corepolicy.common.exceptions.CpeException;
import com.github.cafdataprocessing.corepolicy.common.shared.CorePolicyLogger;
import com.github.cafdataprocessing.corepolicy.common.shared.CorePolicyObjectMapper;
import com.github.cafdataprocessing.corepolicy.common.shared.Level;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 */
public class FilesystemInitializer implements EnvironmentInitializer {
    private final static Logger logger = LoggerFactory.getLogger(FilesystemInitializer.class);

    private final BooleanAgentServices booleanAgentServices;
    private final CorePolicyObjectMapper objectMapper = new CorePolicyObjectMapper();
    private final Path environmentSnapshotCacheLocation;
    private final Period environmentCacheExpiry;

    @Autowired
    public FilesystemInitializer(EngineProperties engineProperties, BooleanAgentServices booleanAgentServices){
        this.booleanAgentServices = booleanAgentServices;
        environmentSnapshotCacheLocation = Paths.get(engineProperties.getEnvironmentCacheLocation());
        environmentCacheExpiry = engineProperties.getEnvironmentCacheExpiry();
    }

    @Override
    public EnvironmentSnapshotImpl initialize(EnvironmentSnapshotImpl environmentSnapshot) {

        try(CorePolicyLogger traceInfo = new CorePolicyLogger("FilesystemInitializer:initialize", Level.DEBUG)) {
            removeReallyOldFiles();

            List<File> files = getFiles(environmentSnapshot.getCollectionSequenceId() + "-*.json");
            files = files.stream().sorted((p, o) -> ((Long) o.lastModified()).compareTo(((Long) p.lastModified()))).collect(Collectors.toList());
            if (files.isEmpty()) {
                traceInfo.log("No valid snapshot files left.");
                return environmentSnapshot;
            }

            EnvironmentSnapshotImpl tmpSnapshot = null;
            File mostRecentFile = files.stream().findFirst().get();
            for (File file : files) {
                if (file.equals(mostRecentFile)) {
                    // Get the snapshot, but check it hasn't been invalidated before we use it
                    // as a return value. This is only validated internally by tests for now.
                    tmpSnapshot = readFromFile(file);
                    if (!tmpSnapshot.getInvalidatedCache()) {
                        continue;
                    }
                    // it is in invalid let it be cleaned, and dont use it.
                }
                checkExpireFile(file);
            }

            // return the one from disk, only if it hasn't been invalidated.
            if ( tmpSnapshot.getInvalidatedCache() )
            {
                traceInfo.log( "Using new snapshot as tmpSnapshot has been invalidated.");
                return environmentSnapshot;
            }

            traceInfo.log( "Using existing snapshot from disk: " + tmpSnapshot.getInstanceId() + " Persisted: " + tmpSnapshot.getPersistedDate() );
            return  tmpSnapshot;
        }
    }

    @Override
    public EnvironmentSnapshotImpl remove(EnvironmentSnapshotImpl environmentSnapshot){
        // nothing to do here at present, FilesystemPersistance remove single file instance,
        // this already removes old files in init method.
        return environmentSnapshot;
    }

    private List<File> getFiles(String pattern) {
        List<File> files = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(environmentSnapshotCacheLocation, pattern)) {
            for (Path entry: stream) {
                files.add(entry.toFile());
            }
        } catch (IOException x) {
            try(CorePolicyLogger traceInfo = new CorePolicyLogger("FilesystemInitializer:initialize", Level.DEBUG)) {
                traceInfo.log(String.format("error creating folder %s: %s", environmentSnapshotCacheLocation, x.getMessage()));

                // Check whether the folder specified for the environmentSnapshotCacheLocation actually exists.
                if (!Files.exists(environmentSnapshotCacheLocation)) {
                    // The directory does not exist - log a message and attempt to create it
                    traceInfo.log(String.format("Environment snapshot cache location (%s) does not exist.", environmentSnapshotCacheLocation));

                    try {
                        // Attempt to create the environment snapshot cache location folder
                        Files.createDirectories(environmentSnapshotCacheLocation.toAbsolutePath());

                        // Return an empty list of files
                        return files;
                    }
                    catch (IOException ex) {
                        traceInfo.log(String.format("error creating folder %s: %s",
                                environmentSnapshotCacheLocation,
                                ex.getMessage()));
                    }
                }
            }

            throw new RuntimeException(String.format("error reading folder %s: %s",
                    environmentSnapshotCacheLocation,
                    x.getMessage()),
                    x);
        }
        return files;
    }

    private void removeReallyOldFiles(){
        List<File> files = getFiles("*-*.json");
        for (File file: files) {
            checkExpireFile(file);
        }
    }

    private void checkExpireFile(File file) {
        try {
            BasicFileAttributes basicAttr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
            if (basicAttr.creationTime().toMillis() < new DateTime().minus(environmentCacheExpiry).getMillis()) {
                cleanupOldFile(file);
            }
        } catch (IOException e) {
            logger.warn(String.format("Could not access %s to evaluate for expiry.", file.toURI()));
        }
    }

    private void cleanupOldFile(File file) {
        String filename = file.getName();
        String instanceId = filename.substring(filename.indexOf("-") + 1, filename.indexOf("."));
        if(booleanAgentServices.getAvailable()) {

            try {
                booleanAgentServices.delete(instanceId);
            } catch (CpeException e) {
                // just log this erorr, we dont want our ability not to delete
                // to stop further evaluations
                logger.warn("Unable to delete BooleanAgentServices for instance: " + instanceId);
            }
        }
        boolean deleted = file.delete();
        if(!deleted){
            logger.warn("Could not expire " + file.getAbsolutePath());
        }
    }

    EnvironmentSnapshotImpl readFromFile(File file){
        try {
            EnvironmentSnapshotImpl environmentSnapshotFromDisk = objectMapper.readValue(file, EnvironmentSnapshotImpl.class);
            return environmentSnapshotFromDisk;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
