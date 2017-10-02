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

import com.github.cafdataprocessing.corepolicy.booleanagent.BooleanAgentServices;
import com.github.cafdataprocessing.corepolicy.common.dto.*;
import com.github.cafdataprocessing.corepolicy.common.EngineProperties;
import com.github.cafdataprocessing.corepolicy.common.domainModels.EnvironmentSnapshotImpl;
import com.github.cafdataprocessing.corepolicy.common.dto.conditions.TextCondition;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Period;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.github.cafdataprocessing.corepolicy.Helper.getTempFolder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FilesystemPersistenceTest {

    @Mock
    EngineProperties engineProperties;

    @Mock
    BooleanAgentServices booleanAgentServices;

    @Before
    public void setUp() throws Exception {
        when(engineProperties.getEnvironmentCacheMode()).thenReturn("fs");

        String tempFolder = getTempFolder();
        when(engineProperties.getEnvironmentCacheLocation()).thenReturn(tempFolder);
        when(engineProperties.getEnvironmentCacheExpiry()).thenReturn(new Period(1,0,0,0));
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testInitialize() throws Exception {
        EnvironmentSnapshotImpl environmentSnapshot = new EnvironmentSnapshotImpl();
        environmentSnapshot.setCreateDate(DateTime.now(DateTimeZone.UTC));
        environmentSnapshot.setCollectionSequenceId(1L);
        environmentSnapshot.getCollections().put(1L, new DocumentCollection());
        environmentSnapshot.getCollectionSequences().put(1L, new CollectionSequence());
        environmentSnapshot.getConditions().put(1L, new TextCondition());
        environmentSnapshot.getFieldLabels().put("Some FieldLabel", new FieldLabel());
        environmentSnapshot.getLexicons().put(1L, new Lexicon());
        environmentSnapshot.getPolicies().put(1L, new Policy());
        environmentSnapshot.getPolicyTypes().put(1L, new PolicyType());
        environmentSnapshot.setCreateDate(DateTime.now(DateTimeZone.UTC));

        FilesystemPersistence filesystemPersistence = new FilesystemPersistence(engineProperties);
        filesystemPersistence.initialize(environmentSnapshot);

        //Save some effort by using the initializer, not a true unit test I know.
        FilesystemInitializer filesystemInitializer = new FilesystemInitializer(engineProperties, booleanAgentServices);
        EnvironmentSnapshotImpl retrieved = new EnvironmentSnapshotImpl();
        retrieved.setCollectionSequenceId(1L);
        retrieved = filesystemInitializer.initialize(retrieved);

        assertEquals(environmentSnapshot.getInstanceId(), retrieved.getInstanceId());
        assertTrue(environmentSnapshot.getCreateDate().equals(retrieved.getCreateDate()));
        assertEquals(environmentSnapshot.getCollectionSequenceLastModifiedDate(), retrieved.getCollectionSequenceLastModifiedDate());
    }
}
