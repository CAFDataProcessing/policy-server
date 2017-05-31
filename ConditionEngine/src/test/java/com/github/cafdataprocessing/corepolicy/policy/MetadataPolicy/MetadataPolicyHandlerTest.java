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
package com.github.cafdataprocessing.corepolicy.policy.MetadataPolicy;

import com.cedarsoftware.util.DeepEquals;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.cafdataprocessing.corepolicy.common.Document;
import com.github.cafdataprocessing.corepolicy.common.DocumentImpl;
import com.github.cafdataprocessing.corepolicy.common.dto.Policy;
import com.github.cafdataprocessing.corepolicy.common.exceptions.CpeException;
import com.github.cafdataprocessing.corepolicy.domainModels.FieldAction;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.util.Assert.isTrue;

/**
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class MetadataPolicyHandlerTest {
    String hasActions = "{\"fieldActions\":[{\"name\": \"test name\", \"value\": \"test value\", \"action\": \"ADD_FIELD_VALUE\"}]}";
    String noActions = "{\"fieldActions\":[]}";
    Long dummyCollectionSequenceId = 1L;
    MetadataPolicyHandler sut;

    Document document;

    @Before
    public void before(){
        sut = new MetadataPolicyHandler(1L);
        document = new DocumentImpl();
    }


    @Test
    public void testGetPolicyTypeId() throws Exception {
        assertEquals(1L, sut.getPolicyTypeId());
    }

    @Test
    public void testWithOneItem() throws Exception {
        Collection<Policy> policies = new HashSet<>();
        Collection<Policy> expectedOutput = new HashSet<>();

        {
            Policy policyPriority1 = new Policy();
            policyPriority1.priority = 1;
            policyPriority1.typeId = 1L;
            ObjectMapper mapper = new ObjectMapper();
            policyPriority1.details = mapper.readTree(hasActions);
            policies.add(policyPriority1);
            expectedOutput.add(policyPriority1);
        }

        Collection<Policy> returnedPolicies = sut.resolve(document, policies);

        assertEquals(1, returnedPolicies.size());
        assertTrue(DeepEquals.deepEquals(expectedOutput, returnedPolicies));
    }

    @Test
    public void testWithTwoItemsSamePriorityAndType() throws Exception {
        Collection<Policy> policies = new HashSet<>();
        Collection<Policy> expectedOutput = new HashSet<>();
        ObjectMapper mapper = new ObjectMapper();

        {
            Policy policyPriority1 = new Policy();
            policyPriority1.priority = 1;
            policyPriority1.typeId = 1L;
            policyPriority1.details = mapper.readTree(hasActions);
            policies.add(policyPriority1);
            expectedOutput.add(policyPriority1);
        }
        {
            Policy policyPriority1 = new Policy();
            policyPriority1.priority = 1;
            policyPriority1.typeId = 1L;
            policyPriority1.details = mapper.readTree(hasActions);
            policies.add(policyPriority1);
            expectedOutput.add(policyPriority1);
        }

        Collection<Policy> returnedPolicies = sut.resolve(document, policies);

        assertEquals(2, returnedPolicies.size());
        assertTrue(DeepEquals.deepEquals(expectedOutput, returnedPolicies));
    }

    @Test
    public void testWithTwoItemsSamePriorityAndDifferentActions() throws Exception {
        Collection<Policy> policies = new HashSet<>();
        Collection<Policy> expectedOutput = new HashSet<>();
        ObjectMapper mapper = new ObjectMapper();

        {
            Policy policyPriority1 = new Policy();
            policyPriority1.priority = 1;
            policyPriority1.typeId = 1L;
            policyPriority1.details = mapper.readTree(hasActions);
            policies.add(policyPriority1);
            expectedOutput.add(policyPriority1);
        }
        {
            Policy policyPriority1 = new Policy();
            policyPriority1.priority = 1;
            policyPriority1.typeId = 1L;
            policyPriority1.details = mapper.readTree(noActions);
            policies.add(policyPriority1);
            expectedOutput.add(policyPriority1);
        }

        Collection<Policy> returnedPolicies = sut.resolve(document, policies);

        assertEquals(2, returnedPolicies.size());
        assertTrue(DeepEquals.deepEquals(expectedOutput, returnedPolicies));
    }

    @Test
    public void testWithMultiplePriority() throws Exception {
        Collection<Policy> policies = new HashSet<>();
        Collection<Policy> expectedOutput = new HashSet<>();
        ObjectMapper mapper = new ObjectMapper();

        {
            Policy policyPriority1 = new Policy();
            policyPriority1.priority = 1;
            policyPriority1.typeId = 1L;
            policyPriority1.details = mapper.readTree(hasActions);
            policies.add(policyPriority1);
        }
        {
            Policy policyPriority1 = new Policy();
            policyPriority1.priority = 2;
            policyPriority1.typeId = 1L;
            policyPriority1.details = mapper.readTree(noActions);
            policies.add(policyPriority1);
            expectedOutput.add(policyPriority1);
        }

        Collection<Policy> returnedPolicies = sut.resolve(document, policies);

        assertEquals(1, returnedPolicies.size());
        assertTrue(DeepEquals.deepEquals(expectedOutput, returnedPolicies));
    }

    @Test
    public void testWithMultipleSomeWithDifferentPriority() throws Exception {
        Collection<Policy> policies = new HashSet<>();
        Collection<Policy> expectedOutput = new HashSet<>();
        ObjectMapper mapper = new ObjectMapper();

        {
            Policy policyPriority1 = new Policy();
            policyPriority1.priority = 1;
            policyPriority1.typeId = 1L;
            policyPriority1.details = mapper.readTree(hasActions);
            policies.add(policyPriority1);
        }
        {
            Policy policyPriority1 = new Policy();
            policyPriority1.priority = 1;
            policyPriority1.typeId = 1L;
            policyPriority1.details = mapper.readTree(noActions);
            policies.add(policyPriority1);
        }
        {
            Policy policyPriority1 = new Policy();
            policyPriority1.priority = 2;
            policyPriority1.typeId = 1L;
            policyPriority1.details = mapper.readTree(hasActions);
            policies.add(policyPriority1);
            expectedOutput.add(policyPriority1);
        }
        {
            Policy policyPriority1 = new Policy();
            policyPriority1.priority = 2;
            policyPriority1.typeId = 1L;
            policyPriority1.details = mapper.readTree(hasActions);
            policies.add(policyPriority1);
            expectedOutput.add(policyPriority1);
        }

        Collection<Policy> returnedPolicies = sut.resolve(document, policies);

        assertEquals(2, returnedPolicies.size());
        assertTrue(DeepEquals.deepEquals(expectedOutput, returnedPolicies));
    }

    @Test
    public void testWithMultipleSomeWithDifferentPriorityDifferentActions() throws Exception {
        Collection<Policy> policies = new HashSet<>();
        Collection<Policy> expectedOutput = new HashSet<>();
        ObjectMapper mapper = new ObjectMapper();

        {
            Policy policyPriority1 = new Policy();
            policyPriority1.priority = 1;
            policyPriority1.typeId = 1L;
            policyPriority1.details = mapper.readTree(hasActions);
            policies.add(policyPriority1);
        }
        {
            Policy policyPriority1 = new Policy();
            policyPriority1.priority = 1;
            policyPriority1.typeId = 1L;
            policyPriority1.details = mapper.readTree(noActions);
            policies.add(policyPriority1);
        }
        {
            Policy policyPriority1 = new Policy();
            policyPriority1.priority = 2;
            policyPriority1.typeId = 1L;
            policyPriority1.details = mapper.readTree(hasActions);
            policies.add(policyPriority1);
            expectedOutput.add(policyPriority1);
        }
        {
            Policy policyPriority1 = new Policy();
            policyPriority1.priority = 2;
            policyPriority1.typeId = 1L;
            policyPriority1.details = mapper.readTree(hasActions);
            policies.add(policyPriority1);
            expectedOutput.add(policyPriority1);
        }
        {
            Policy policyPriority1 = new Policy();
            policyPriority1.priority = 2;
            policyPriority1.typeId = 1L;
            policyPriority1.details = mapper.readTree(noActions);
            policies.add(policyPriority1);
            expectedOutput.add(policyPriority1);
        }

        Collection<Policy> returnedPolicies = sut.resolve(document, policies);

        assertEquals(3, returnedPolicies.size());
        assertTrue(DeepEquals.deepEquals(expectedOutput, returnedPolicies));
    }

    @Test
    public void testEmptyPolicies() {
        Collection<Policy> policies = new HashSet<>();

        Collection<Policy> returnedPolicies = sut.resolve(document, policies);

        assertEquals(0, returnedPolicies.size());
    }

    @Test
    public void testNullPolicies() {
        Collection<Policy> policies = null;

        Collection<Policy> returnedPolicies = sut.resolve(document, policies);

        assertEquals(0, returnedPolicies.size());
    }

    @Test(expected = CpeException.class)
    public void testPoliciesDifferentType() throws Exception {
        Collection<Policy> policies = new HashSet<>();
        ObjectMapper mapper = new ObjectMapper();
        {
            Policy policyPriority1 = new Policy();
            policyPriority1.priority = 1;
            policyPriority1.typeId = 1L;
            policyPriority1.details = mapper.readTree(hasActions);
            policies.add(policyPriority1);
        }
        {
            Policy policyPriority1 = new Policy();
            policyPriority1.priority = 1;
            policyPriority1.typeId = 2L;
            policyPriority1.details = mapper.readTree(hasActions);
            policies.add(policyPriority1);
        }

        Collection<Policy> returnedPolicies = sut.resolve(document, policies);
    }

    @Test
    public void testSetFieldAction() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        MetadataPolicy metadataPolicy = new MetadataPolicy();
        Collection<FieldAction> fieldActions = new ArrayList<>();
        FieldAction setFieldAction = new FieldAction();
        String expectedFieldName = "test_Field_"+ UUID.randomUUID().toString();
        String expectedFieldValue_1 = "test_field_value_"+ UUID.randomUUID().toString();
        setFieldAction.setFieldName(expectedFieldName);
        setFieldAction.setFieldValue(expectedFieldValue_1);
        setFieldAction.setAction(FieldAction.Action.SET_FIELD_VALUE);

        fieldActions.add(setFieldAction);
        metadataPolicy.setFieldActions(fieldActions);

        Policy policy = new Policy();
        policy.details = objectMapper.valueToTree(metadataPolicy);

        sut.handle(document, policy, dummyCollectionSequenceId);
        Collection<String> returnedFieldValues = document.getMetadata().get(expectedFieldName);
        Assert.assertEquals
                ("Expecting set field to be returned with single value.", 1,
                        returnedFieldValues.size());
        String returnedValue = returnedFieldValues.iterator().next();
        Assert.assertEquals("Returned value on set field should be as expected.", expectedFieldValue_1, returnedValue);

        //verify that calling handle again removes the first value that was added for the field and adds the new value
        String expectedFieldValue_2 = "test_field_value_"+ UUID.randomUUID().toString();
        setFieldAction.setFieldValue(expectedFieldValue_2);
        policy = new Policy();
        policy.details = objectMapper.valueToTree(metadataPolicy);

        sut.handle(document, policy, dummyCollectionSequenceId);
        returnedFieldValues = document.getMetadata().get(expectedFieldName);
        Assert.assertEquals
                ("Expecting set field to be returned with single value.", 1,
                        returnedFieldValues.size());
        returnedValue = returnedFieldValues.iterator().next();
        Assert.assertEquals("Returned value on set field should be as expected.", expectedFieldValue_2, returnedValue);
    }

    @Test
    public void testAddFieldAction() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        MetadataPolicy metadataPolicy = new MetadataPolicy();
        Collection<FieldAction> fieldActions = new ArrayList<>();
        FieldAction addFieldAction = new FieldAction();
        String expectedFieldName = "test_Field_"+ UUID.randomUUID().toString();
        String expectedFieldValue_1 = "test_field_value_"+ UUID.randomUUID().toString();
        addFieldAction.setFieldName(expectedFieldName);
        addFieldAction.setFieldValue(expectedFieldValue_1);
        addFieldAction.setAction(FieldAction.Action.ADD_FIELD_VALUE);

        fieldActions.add(addFieldAction);
        metadataPolicy.setFieldActions(fieldActions);

        Policy policy = new Policy();
        policy.details = objectMapper.valueToTree(metadataPolicy);

        sut.handle(document, policy, dummyCollectionSequenceId);
        Collection<String> returnedFieldValues = document.getMetadata().get(expectedFieldName);
        Assert.assertEquals
                ("Expecting added field to be returned with single value.", 1,
                        returnedFieldValues.size());
        String returnedValue = returnedFieldValues.iterator().next();
        Assert.assertEquals("Returned value on added field should be as expected.", expectedFieldValue_1, returnedValue);

        //add another value and verify it the previous value remains
        String expectedFieldValue_2 = "test_field_value_"+ UUID.randomUUID().toString();
        addFieldAction.setFieldValue(expectedFieldValue_2);
        policy = new Policy();
        policy.details = objectMapper.valueToTree(metadataPolicy);

        sut.handle(document, policy, dummyCollectionSequenceId);
        returnedFieldValues = document.getMetadata().get(expectedFieldName);
        Assert.assertEquals
                ("Expecting added field to be returned with multiple values.", 2,
                        returnedFieldValues.size());
        Assert.assertTrue("Expecting first value to be in the returned values.",
                returnedFieldValues.contains(expectedFieldValue_1));
        Assert.assertTrue("Expecting second value to be in the returned values.",
                returnedFieldValues.contains(expectedFieldValue_2));
    }
}
