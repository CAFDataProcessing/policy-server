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
import com.github.cafdataprocessing.corepolicy.common.dto.*;
import com.github.cafdataprocessing.corepolicy.common.dto.conditions.*;
import com.github.cafdataprocessing.corepolicy.common.EngineProperties;
import com.github.cafdataprocessing.corepolicy.common.domainModels.EnvironmentSnapshotImpl;
import org.joda.time.Period;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;

import static com.github.cafdataprocessing.corepolicy.Helper.getTempFolder;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FilesystemInitializerTest {

    @Mock
    EngineProperties engineProperties;

    @Mock
    BooleanAgentServices booleanAgentServices;

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testInitialize() throws Exception {
        String tempFolder = getTempFolder();
        when(engineProperties.getEnvironmentCacheLocation()).thenReturn(tempFolder);

//This breaks, a great mystery!!
//        when(engineProperties.getEnvironmentCacheLocation()).thenReturn(getTempFolder());

        when(engineProperties.getEnvironmentCacheExpiry()).thenReturn(new Period(1,0,0,0));


        EnvironmentSnapshotImpl environmentSnapshot = new EnvironmentSnapshotImpl();
        environmentSnapshot.setCollectionSequenceId(1L);
        environmentSnapshot.getCollections().put(1L, new DocumentCollection());
        CollectionSequence collectionSequence = new CollectionSequence();
        collectionSequence.collectionSequenceEntries.add(new CollectionSequenceEntry());
        collectionSequence.collectionSequenceEntries.add(new CollectionSequenceEntry());
        environmentSnapshot.getCollectionSequences().put(1L, collectionSequence);
        environmentSnapshot.getConditions().put(1L, new NumberCondition());
        environmentSnapshot.getConditions().put(2L, new NotCondition());
        environmentSnapshot.getConditions().put(3L, new LexiconCondition());
        environmentSnapshot.getConditions().put(4L, new ExistsCondition());
        environmentSnapshot.getConditions().put(5L, new DateCondition());
        BooleanCondition booleanCondition = new BooleanCondition();
        booleanCondition.children = Arrays.asList(new TextCondition(), new ExistsCondition());
        environmentSnapshot.getConditions().put(6L, booleanCondition);
        environmentSnapshot.getConditions().put(7L, new TextCondition());
        environmentSnapshot.getConditions().put(8L, new RegexCondition());
        environmentSnapshot.getConditions().put(9L, new StringCondition());
        environmentSnapshot.getConditions().put(11L, new FragmentCondition());
        environmentSnapshot.getLexicons().put(1L, new Lexicon());
        environmentSnapshot.getPolicies().put(1L, new Policy());
        environmentSnapshot.getPolicyTypes().put(1L, new PolicyType());
        //todo add field labels

        FilesystemPersistence filesystemPersistence = new FilesystemPersistence(engineProperties);
        filesystemPersistence.initialize(environmentSnapshot);

        FilesystemInitializer filesystemInitializer = new FilesystemInitializer(engineProperties, booleanAgentServices);

        EnvironmentSnapshotImpl newEnvironmentSnapshot = new EnvironmentSnapshotImpl();
        newEnvironmentSnapshot.setCollectionSequenceId(environmentSnapshot.getCollectionSequenceId());
        newEnvironmentSnapshot= filesystemInitializer.initialize(newEnvironmentSnapshot);

        assertEquals(environmentSnapshot.getCollectionSequenceId(), newEnvironmentSnapshot.getCollectionSequenceId());
    }

}