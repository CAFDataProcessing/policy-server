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
package com.github.cafdataprocessing.corepolicy.api;

import com.github.cafdataprocessing.corepolicy.common.ApiProperties;
import com.github.cafdataprocessing.corepolicy.common.EnvironmentSnapshot;
import com.github.cafdataprocessing.corepolicy.common.dto.ItemType;
import com.github.cafdataprocessing.corepolicy.common.dtoweb.RetrieveAdditional;
import com.github.cafdataprocessing.corepolicy.common.dtoweb.RetrieveRequest;
import com.github.cafdataprocessing.corepolicy.environment.EnvironmentSnapshotApi;
import org.apache.http.NameValuePair;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collection;

/**
 * Base class for any web api endpoints for the EnvironmentSnapshot api.
 */
public abstract class EnvironmentSnapshotApiWebBase extends WebApiBase implements EnvironmentSnapshotApi {
    private static final Logger logger = LoggerFactory.getLogger(EnvironmentSnapshotApiWebBase.class);

    public EnvironmentSnapshotApiWebBase(ApiProperties apiProperties){
        super(apiProperties);
    }

//    Function<EnvironmentSnapshot, EnvironmentSnapshot> environmentSnapshotTranslator = snapshot -> EnvironmentSnapshotWeb.get(snapshot);


    @Override
    public EnvironmentSnapshot get(long collectionSequenceId, DateTime dateTime, DateTime csLastModifiedTime) {

        RetrieveRequest retrieveRequest = new RetrieveRequest();
        retrieveRequest.type = ItemType.COLLECTION_SEQUENCE;
        retrieveRequest.id = Arrays.asList(collectionSequenceId);

        retrieveRequest.additional = new RetrieveAdditional();
        retrieveRequest.additional.includeChildren = true;
        // we want to return snapshots which contain changes after the csLastModifiedTime,
        // not related to the createTime of our snapshot.
        retrieveRequest.additional.datetime = csLastModifiedTime;

        final Collection<NameValuePair> params = createParams(retrieveRequest);

        Collection<EnvironmentSnapshot> environmentSnapshots = makeMultipleRequest(WebApiAction.RETRIEVE, params, EnvironmentSnapshot.class);

        if(environmentSnapshots.size() > 1)
            throw new RuntimeException("Only expected a single environment snapshot!");

        // the API may return null to signify no-change if a datetime was specified
        return environmentSnapshots.isEmpty() ? null : environmentSnapshots.iterator().next();
    }

    @Override
    protected Logger getLogger() {
        return logger;
    }

}
