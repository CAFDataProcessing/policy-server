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
package com.github.cafdataprocessing.corepolicy.policy.MetadataPolicy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.cafdataprocessing.corepolicy.common.PolicyApi;
import com.github.cafdataprocessing.corepolicy.common.dto.Policy;
import com.github.cafdataprocessing.corepolicy.common.dto.PolicyType;
import com.github.cafdataprocessing.corepolicy.common.shared.JsonValidation;
import com.github.cafdataprocessing.corepolicy.domainModels.FieldAction;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MetadataPolicyConverterImplTest {

    @Mock
    PolicyApi policyApi;

    @Before
    public void setup() throws Exception {

        // ensure to make it call the validation method, as we check for valid
        // MetadataPolicy definitions.
        Mockito.doAnswer(new Answer<Object>() {
            public Object answer(InvocationOnMock invocation)throws Exception  {
                Object[] args = invocation.getArguments();

                LoggerFactory.getLogger(MetadataPolicyConverterImplTest.class).debug("called with arguments: " + args);

                Policy policy = (Policy)args[0];
                PolicyType pt = (PolicyType)args[1];

                JsonValidation.validateJson( policy.details, pt.definition );

                return true;
            }}).when(policyApi).validate(any(), any());

    }

    @Test
    public void testConvert() throws Exception {

        when(policyApi.retrievePolicyType(anyLong())).thenReturn(getPolicyType());

        MetadataPolicyConverter sut = new MetadataPolicyConverterImpl(policyApi);

        Policy policy = new Policy();
        ObjectMapper mapper = new ObjectMapper();
        policy.details= mapper.readTree("{" +
                "\"fieldActions\": [" +
                "{\"name\":\"myfield\", \"action\": \"ADD_FIELD_VALUE\", \"value\":\"My value\"}," +
                "{\"name\":\"myfield\", \"action\": \"ADD_FIELD_VALUE\", \"value\":\"My value 2\"}" +
                "]}");


        MetadataPolicy metadataPolicy = sut.convert(policy);
        assertEquals(2, metadataPolicy.getFieldActions().size());
        FieldAction fieldAction = metadataPolicy.getFieldActions().stream().findFirst().get();
        assertEquals(FieldAction.Action.ADD_FIELD_VALUE, fieldAction.getAction());
        assertEquals("myfield", fieldAction.getFieldName());
        assertEquals("My value", fieldAction.getFieldValue());

    }

    @Test(expected = Exception.class)
    public void testInvalidFieldActionAction() throws Exception {
        String json = "{" +
                "\"fieldActions\": [" +
                "{\"name\":\"myfield\", \"action\": \"BAD\", \"value\":\"My value\"}," +
                "{\"name\":\"myfield\", \"action\": \"ADD_FIELD_VALUE\", \"value\":\"My value 2\"}" +
                "]}";
        validateXml(json);
    }

    @Test(expected = Exception.class)
    public void testMissingFieldActionAction() throws Exception {
        String json = "{" +
                "\"fieldActions\": [" +
                "{\"name\":\"myfield\", \"value\":\"My value\"}," +
                "{\"name\":\"myfield\", \"value\":\"My value 2\"}" +
                "]}";
        validateXml(json);
    }

    @Test(expected = Exception.class)
    public void testMissingFieldActionFieldName() throws Exception {
        String json = "{" +
                "\"fieldActions\": [" +
                "{\"action\": \"ADD_FIELD_VALUE\", \"value\":\"My value\"}," +
                "{\"action\": \"ADD_FIELD_VALUE\", \"value\":\"My value 2\"}" +
                "]}";
        validateXml(json);
    }

    @Test(expected = Exception.class)
    public void testEmptyFieldActionFieldName() throws Exception {
        String json = "{" +
                "\"fieldActions\": [" +
                "{\"name\":\"\", \"action\": \"ADD_FIELD_VALUE\", \"value\":\"My value\"}," +
                "{\"name\":\"\", \"action\": \"ADD_FIELD_VALUE\", \"value\":\"My value 2\"}" +
                "]}";
        validateXml(json);
    }

    @Test
    public void testNoFieldActionsIndexTypeIsValid() throws Exception {
        String json = "{" +
                "\"fieldActions\": [" +
                "]}";
        validateXml(json);
    }

    public PolicyType getPolicyType() throws Exception {
        PolicyType policyType = new PolicyType();
        String jsonSchema = IOUtils.toString(ClassLoader.class.getResourceAsStream("/metadataPolicy.json"), "UTF-8");
        ObjectMapper mapper = new ObjectMapper();
        policyType.definition = mapper.readTree(jsonSchema);

        return policyType;
    }

    private void validateXml(String xml) throws Exception {
        when(policyApi.retrievePolicyType(anyLong())).thenReturn(getPolicyType());

        MetadataPolicyConverter sut = new MetadataPolicyConverterImpl(policyApi);

        Policy policy = new Policy();
        policy.typeId = 1L;
        ObjectMapper mapper = new ObjectMapper();
        policy.details = mapper.readTree(xml);

        MetadataPolicy metadataPolicy = sut.convert(policy);
    }
}