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
package com.github.cafdataprocessing.corepolicy;

import com.github.cafdataprocessing.corepolicy.common.*;
import com.github.cafdataprocessing.corepolicy.common.dto.*;
import com.github.cafdataprocessing.corepolicy.document.DocumentUnderEvaluation;
import com.google.common.collect.Multimap;
import com.github.cafdataprocessing.corepolicy.common.exceptions.BackEndRequestFailedCpeException;
import org.apache.commons.lang3.NotImplementedException;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PolicyEngineImplTest {
    PolicyEngineImpl sut;

    @Mock
    ConditionEngine conditionEngine;

    @Mock
    EnvironmentSnapshotCache environmentSnapshotCache;

    @Mock
    PolicyApi policyApi;

    @Mock
    ConditionEngineMetadata conditionEngineMetadata;

    @Mock
    private ApiProperties apiProperties;

    @Before
    public void before(){
        when(conditionEngineMetadata.createResult(any(Multimap.class))).thenReturn(new ConditionEngineResult());
        sut = new PolicyEngineImpl(policyApi, environmentSnapshotCache, conditionEngine, conditionEngineMetadata, apiProperties);
    }

    @Test
    public void testGetEnvironmentSnapshot() throws Exception{
        EnvironmentSnapshot mockEnvironmentSnapshot = mock(EnvironmentSnapshot.class);
        when(mockEnvironmentSnapshot.getCollectionSequenceId()).thenReturn(1L);
        when(environmentSnapshotCache.get(1L)).thenReturn(mockEnvironmentSnapshot);

        EnvironmentSnapshot environmentSnapshot = sut.getEnvironmentSnapshot(1L);

        assertEquals((Long) 1L, environmentSnapshot.getCollectionSequenceId());
    }

    @Test
    public void testInvalidateCache() throws Exception{
        EnvironmentSnapshot mockEnvironmentSnapshot = mock(EnvironmentSnapshot.class);
        when(mockEnvironmentSnapshot.getCollectionSequenceId()).thenReturn(1L);
        when(mockEnvironmentSnapshot.getCreateDate()).thenReturn(new DateTime());
        when(mockEnvironmentSnapshot.getCollectionSequenceLastModifiedDate()).thenReturn(new DateTime());

        EnvironmentSnapshot mockEnvironmentSnapshotRebuilt = mock(EnvironmentSnapshot.class);
        when(mockEnvironmentSnapshotRebuilt.getCollectionSequenceId()).thenReturn(1L);
        when(mockEnvironmentSnapshotRebuilt.getCreateDate()).thenReturn(new DateTime().plusHours(1));
        when(mockEnvironmentSnapshotRebuilt.getCollectionSequenceLastModifiedDate()).thenReturn(new DateTime().plusHours(1));

        when(environmentSnapshotCache.get(1L)).thenReturn(mockEnvironmentSnapshot);

        EnvironmentSnapshot environmentSnapshot = sut.getEnvironmentSnapshot(1L);
        DateTime firstDateTime = environmentSnapshot.getCreateDate();

        when(environmentSnapshotCache.get(1L)).thenReturn(mockEnvironmentSnapshotRebuilt);
        sut.invalidateCache(1L);
        environmentSnapshot = sut.getEnvironmentSnapshot(1L);

        assertNotEquals(firstDateTime, environmentSnapshot.getCreateDate());
    }

    @Test
    public void testRegisterPolicyHandler(){
        PolicyHandler policyHandler = mock(PolicyHandler.class);
        sut.registerPolicyHandler(policyHandler);
    }

    @Test
    public void testEvaluateResolvePriorityBased(){
        Policy policy1 = new Policy();
        policy1.id = 1L;
        policy1.typeId = 1L;
        policy1.priority = 100;

        Policy policy2 = new Policy();
        policy2.id = 2L;
        policy2.typeId = 1L;
        policy2.priority = 200;

        PolicyType policyType = new PolicyType();
        policyType.id = 1L;
        policyType.conflictResolutionMode = ConflictResolutionMode.PRIORITY;

        PolicyHandler policyHandler = mock(PolicyHandler.class);
        when(policyHandler.resolve(any(), anyCollection())).thenThrow(new NotImplementedException("not implemented"));
        sut.registerPolicyHandler(policyHandler);

        ConditionEngineResult conditionEngineResult = new ConditionEngineResult();
        MatchedCollection matchedCollection = new MatchedCollection();
        CollectionPolicy collectionPolicy = new CollectionPolicy();
        collectionPolicy.setId(1L);
        matchedCollection.getPolicies().add(collectionPolicy);
        collectionPolicy = new CollectionPolicy();
        collectionPolicy.setId(2L);
        matchedCollection.getPolicies().add(collectionPolicy);
        conditionEngineResult.matchedCollections.add(matchedCollection);

        when(conditionEngine.evaluate(any(DocumentUnderEvaluation.class), eq(1L), any(EnvironmentSnapshot.class))).thenReturn(conditionEngineResult);

        EnvironmentSnapshot environmentSnapshot = mock(EnvironmentSnapshot.class);
        when(environmentSnapshot.getPolicy(1L)).thenReturn(policy1);
        when(environmentSnapshot.getPolicy(2L)).thenReturn(policy2);
        when(environmentSnapshot.getPolicyType(1L)).thenReturn(policyType);
        try {
            when(environmentSnapshotCache.get(anyLong())).thenReturn(environmentSnapshot);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        DocumentImpl doc = new DocumentImpl();
        doc.setFullMetadata(true);
        ClassifyDocumentResult classifyResult = sut.classify(1L, new DocumentImpl());

        assertNotNull(classifyResult);
        assertNotNull(classifyResult.resolvedPolicies);
        assertEquals(1, classifyResult.resolvedPolicies.size());
        Long firstPolicyId = classifyResult.resolvedPolicies.stream().findFirst().get();
        assertEquals((Long)2L, firstPolicyId);
    }

    @Test
    public void testEvaluateResolveHandlerBased(){
        Policy policy1 = new Policy();
        policy1.id = 1L;
        policy1.typeId = 1L;
        policy1.priority = 100;

        Policy policy2 = new Policy();
        policy2.id = 2L;
        policy2.typeId = 1L;
        policy2.priority = 200;

        PolicyType policyType = new PolicyType();
        policyType.id = 1L;
        policyType.conflictResolutionMode = ConflictResolutionMode.CUSTOM;

        PolicyHandler policyHandler = mock(PolicyHandler.class);
        when(policyHandler.getPolicyTypeId()).thenReturn(1L);
        when(policyHandler.resolve(any(), anyCollection())).thenReturn(Arrays.asList(policy1));
        sut.registerPolicyHandler(policyHandler);

        ConditionEngineResult conditionEngineResult = new ConditionEngineResult();
        MatchedCollection matchedCollection = new MatchedCollection();
        CollectionPolicy collectionPolicy = new CollectionPolicy();
        collectionPolicy.setId(1L);
        matchedCollection.getPolicies().add(collectionPolicy);
        collectionPolicy = new CollectionPolicy();
        collectionPolicy.setId(2L);
        matchedCollection.getPolicies().add(collectionPolicy);
        conditionEngineResult.matchedCollections.add(matchedCollection);

        when(conditionEngine.evaluate(any(DocumentUnderEvaluation.class), eq(1L), any(EnvironmentSnapshot.class))).thenReturn(conditionEngineResult);

        EnvironmentSnapshot environmentSnapshot = mock(EnvironmentSnapshot.class);
        when(environmentSnapshot.getPolicy(1L)).thenReturn(policy1);
        when(environmentSnapshot.getPolicy(2L)).thenReturn(policy2);
        when(environmentSnapshot.getPolicyType(1L)).thenReturn(policyType);
        try {
            when(environmentSnapshotCache.get(anyLong())).thenReturn(environmentSnapshot);
        } catch (Exception e) {
            throw new BackEndRequestFailedCpeException(e);
        }

        DocumentImpl doc = new DocumentImpl();
        doc.setFullMetadata(true);
        ClassifyDocumentResult classifyResult = sut.classify(1L, new DocumentImpl());

        assertNotNull(classifyResult);
        assertNotNull(classifyResult.resolvedPolicies);
        assertEquals(1, classifyResult.resolvedPolicies.size());
        Long firstPolicyId = classifyResult.resolvedPolicies.stream().findFirst().get();
        assertEquals((Long)1L, firstPolicyId);
    }
}