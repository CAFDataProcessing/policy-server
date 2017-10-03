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

import com.github.cafdataprocessing.corepolicy.common.EnvironmentSnapshot;
import com.github.cafdataprocessing.corepolicy.common.domainModels.EnvironmentSnapshotImpl;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 */
public class ApiInitializer implements EnvironmentInitializer {

    private final EnvironmentSnapshotApi environmentSnapshotApi;

    @Autowired
    public ApiInitializer(EnvironmentSnapshotApi environmentSnapshotApi){
        this.environmentSnapshotApi = environmentSnapshotApi;
    }

    @Override
    public EnvironmentSnapshotImpl initialize(EnvironmentSnapshotImpl environmentSnapshot) {
        DateTime snapshotTime = null;
        DateTime csLastModifiedTime = null;
        if(environmentSnapshot.getCreateDate()!=null){
            snapshotTime = environmentSnapshot.getCreateDate();
        }

        if ( environmentSnapshot.getCollectionSequenceLastModifiedDate() != null ) {
            csLastModifiedTime = environmentSnapshot.getCollectionSequenceLastModifiedDate();
        }

        EnvironmentSnapshot retrievedEnvironmentSnapshot = environmentSnapshotApi.get(environmentSnapshot.getCollectionSequenceId(), snapshotTime, csLastModifiedTime);

        if(retrievedEnvironmentSnapshot==null){
            return environmentSnapshot;
        }

        EnvironmentSnapshotImpl newEnvironmentSnapshot = new EnvironmentSnapshotImpl();
        newEnvironmentSnapshot.setCreateDate(retrievedEnvironmentSnapshot.getCreateDate());
        newEnvironmentSnapshot.setCollectionSequenceLastModifiedDate(retrievedEnvironmentSnapshot.getCollectionSequenceLastModifiedDate());
        newEnvironmentSnapshot.setCollectionSequenceId(retrievedEnvironmentSnapshot.getCollectionSequenceId());
        newEnvironmentSnapshot.setInstanceId(retrievedEnvironmentSnapshot.getInstanceId());
        newEnvironmentSnapshot.getCollections().putAll(retrievedEnvironmentSnapshot.getCollections());
        newEnvironmentSnapshot.getCollectionSequences().putAll(retrievedEnvironmentSnapshot.getCollectionSequences());
        newEnvironmentSnapshot.getConditions().putAll(retrievedEnvironmentSnapshot.getConditions());
        newEnvironmentSnapshot.getFieldLabels().putAll(retrievedEnvironmentSnapshot.getFieldLabels());
        newEnvironmentSnapshot.getLexicons().putAll(retrievedEnvironmentSnapshot.getLexicons());
        newEnvironmentSnapshot.getPolicies().putAll(retrievedEnvironmentSnapshot.getPolicies());
        newEnvironmentSnapshot.getPolicyTypes().putAll(retrievedEnvironmentSnapshot.getPolicyTypes());

        return newEnvironmentSnapshot;
    }


    @Override
    public EnvironmentSnapshotImpl remove(EnvironmentSnapshotImpl environmentSnapshot){

        // nothing to do here.
        return environmentSnapshot;
    }
}
