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
package com.github.cafdataprocessing.corepolicy.environment;

import com.github.cafdataprocessing.corepolicy.common.domainModels.EnvironmentSnapshotImpl;
import com.github.cafdataprocessing.corepolicy.common.shared.CorePolicyLogger;
import com.github.cafdataprocessing.corepolicy.common.shared.Level;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.Collection;

/**
 *
 */
public class InitializationPipelineImpl implements EnvironmentInitializer{
    private Collection<EnvironmentInitializer> stages;

    @Autowired
    public InitializationPipelineImpl(FilesystemInitializer filesystemInitializer,
                                      ApiInitializer apiInitializer,
                                      FilesystemPersistence filesystemPersistence,
                                      BooleanAgentInitializer booleanAgentInitializer){
        stages = Arrays.asList(filesystemInitializer, apiInitializer, filesystemPersistence, booleanAgentInitializer);
    }

    @Override
    public EnvironmentSnapshotImpl initialize(EnvironmentSnapshotImpl environmentSnapshot) {
        try(CorePolicyLogger traceInfo = new CorePolicyLogger("InitializationPipelineImpl:initialize", Level.DEBUG)) {
             for (EnvironmentInitializer environmentInitializer : stages) {

                environmentSnapshot = environmentInitializer.initialize(environmentSnapshot);
            }
            return environmentSnapshot;
        }
    }

    @Override
    public EnvironmentSnapshotImpl remove(EnvironmentSnapshotImpl environmentSnapshot) {

        try(CorePolicyLogger traceInfo = new CorePolicyLogger("InitializationPipelineImpl:remove", Level.DEBUG)) {
            // Call each initializer type, and let them do whatever work they need to.
            for (EnvironmentInitializer environmentInitializer : stages) {
                environmentSnapshot = environmentInitializer.remove(environmentSnapshot);
            }

            return environmentSnapshot;
        }
    }
}
