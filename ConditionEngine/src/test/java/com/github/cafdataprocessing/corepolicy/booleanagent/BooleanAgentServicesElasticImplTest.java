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
package com.github.cafdataprocessing.corepolicy.booleanagent;

import com.github.cafdataprocessing.corepolicy.common.ElasticsearchProperties;
import com.github.cafdataprocessing.corepolicy.common.UserContext;
import com.github.cafdataprocessing.corepolicy.common.exceptions.BackEndRequestFailedCpeException;
import com.github.cafdataprocessing.corepolicy.common.shared.MetadataValue;
import com.github.cafdataprocessing.corepolicy.domainModels.BooleanAgentDocument;
import com.github.cafdataprocessing.corepolicy.domainModels.BooleanAgentDocuments;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import java.util.Collection;
import java.util.Collections;


public class BooleanAgentServicesElasticImplTest {
    static BooleanAgentServicesElasticImpl elasticBAS;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @BeforeClass
    public static void setup() {
        elasticBAS = getElasticsearchBooleanAgentServices(createMockElasticsearchProperties());
    }

    @Test
    public void testGetAvailable() {
        Assert.assertTrue("Elasticsearch BooleanAgentServices should be available", elasticBAS.getAvailable());

        BooleanAgentServices unavailableElasticBAS = getElasticsearchBooleanAgentServices(createMockUnavailableProperties());
        Assert.assertFalse("Elasticsearch BooleanAgentServices should not be available", unavailableElasticBAS.getAvailable());
    }

    @Test
    public void testCanConnect() {
        Assert.assertFalse("Without a running Elasticsearch implementation, Elasticsearch BooleanAgentServices should disallow connection", elasticBAS.canConnect());
    }

    @Test
    public void testCreate() {
        //With no test documents, create() should do nothing.
        elasticBAS.create("nodocs-instance-id", Mockito.mock(BooleanAgentDocuments.class));

        //Without a running Elasticsearch implementation, Elasticsearch BooleanAgentServices should throw ex.
        BooleanAgentDocuments booleanAgentDocs = new BooleanAgentDocuments();
        booleanAgentDocs.setDocuments(Collections.singletonList(Mockito.mock(BooleanAgentDocument.class)));
        thrown.expect(BackEndRequestFailedCpeException.class);
        elasticBAS.create("dummy-instance-id", booleanAgentDocs);
    }

    @Test
    public void testQuery() throws Exception {
        //With no field values, query() should do nothing.
        elasticBAS.query("nofieldvalues-instance-id", Collections.emptyList());

        //Without a running Elasticsearch implementation, Elasticsearch BooleanAgentServices should throw ex.
        thrown.expect(BackEndRequestFailedCpeException.class);
        BooleanAgentQueryResult result = elasticBAS.query("dummy-instance-id", Collections.singletonList(Mockito.mock(MetadataValue.class)));
    }

    @Test
    public void testIsValidExpression() throws Exception {
        //Expect null to be valid
        elasticBAS.isValidExpression(null);

        /* TODO: Further validation requires mocking of org.elasticsearch.client.Client
        //Valid expression
        elasticBAS.isValidExpression("cat /5 dog");

        //Invalid expression
        elasticBAS.isValidExpression("cat) /5 dog");
        */
    }

    @Test
    public void testDelete() throws Exception {
        //TODO: Requires mocking of org.elasticsearch.client.Client
        //Without a running Elasticsearch implementation, Elasticsearch BooleanAgentServices should throw ex.
        thrown.expect(BackEndRequestFailedCpeException.class);
        elasticBAS.delete("dummy-instance-id");
    }

    @Test
    public void testExistForInstanceId() throws Exception {
        //TODO: Requires mocking of org.elasticsearch.client.Client
        //Without a running Elasticsearch implementation, Elasticsearch BooleanAgentServices should throw ex.
        thrown.expect(BackEndRequestFailedCpeException.class);
        boolean exists = elasticBAS.existForInstanceId("dummy-instance-id");
    }

    @Test
    public void testDoTermGetInfo() throws Exception {
        //TODO: Requires mocking of org.elasticsearch.client.Client
        //Without a running Elasticsearch implementation, Elasticsearch BooleanAgentServices should throw ex.
        thrown.expect(BackEndRequestFailedCpeException.class);
        Collection<Term> terms = elasticBAS.doTermGetInfo("dummy terms text");
    }

    private static BooleanAgentServicesElasticImpl getElasticsearchBooleanAgentServices(final ElasticsearchProperties properties) {
        UserContext mockUserContext = Mockito.mock(UserContext.class);
        return new BooleanAgentServicesElasticImpl(properties, mockUserContext);
    }

    private static ElasticsearchProperties createMockElasticsearchProperties() {
        ElasticsearchProperties properties = Mockito.mock(ElasticsearchProperties.class);
        Mockito.when(properties.isElasticsearchDisabled()).thenReturn(false);
        Mockito.when(properties.getElasticsearchHost()).thenReturn("nonexistent-elasticsearch");
        Mockito.when(properties.getElasticsearchPort()).thenReturn(9999);
        Mockito.when(properties.getElasticsearchClusterName()).thenReturn("policyserver");
        Mockito.when(properties.getElasticsearchPolicyIndexName()).thenReturn("policy");
        Mockito.when(properties.getElasticsearchTransportPingTimeout()).thenReturn("10s");
        Mockito.when(properties.getElasticsearchMasterNodeTimeout()).thenReturn("10s");
        Mockito.when(properties.getElasticsearchIndexStatusTimeout()).thenReturn(2);
        return properties;
    }

    private ElasticsearchProperties createMockUnavailableProperties() {
        ElasticsearchProperties properties = Mockito.mock(ElasticsearchProperties.class);
        Mockito.when(properties.isElasticsearchDisabled()).thenReturn(true);
        return properties;
    }
}
