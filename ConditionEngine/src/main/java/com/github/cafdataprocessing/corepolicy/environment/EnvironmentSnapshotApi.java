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
import org.joda.time.DateTime;

/**
 *
 */
public interface EnvironmentSnapshotApi {
    /**
     *
     * @param collectionSequenceId  The id of a collection sequence to get a shapshot for.
     * @param dateTime The creation time of an EXISTING snapshot you have.
     * @param csLastModifiedTime The last modification time of the collection sequence in the snapshot ( used to know if its been modified since. )
     * @return If null dateTime or sequence has changed since dateTime, an environment snapshot.
     * If nothing has changed since dateTime, will return null
     */
    EnvironmentSnapshot get(long collectionSequenceId, DateTime dateTime, DateTime csLastModifiedTime );
}
