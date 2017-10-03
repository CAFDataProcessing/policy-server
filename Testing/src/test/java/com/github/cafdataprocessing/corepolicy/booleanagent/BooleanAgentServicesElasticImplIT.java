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
package com.github.cafdataprocessing.corepolicy.booleanagent;

import com.github.cafdataprocessing.corepolicy.testing.IntegrationTestBase;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

@RunWith(MockitoJUnitRunner.class)
public class BooleanAgentServicesElasticImplIT extends BooleanAgentServicesBaseIT {
    private static Logger logger = LoggerFactory.getLogger(BooleanAgentServicesElasticImplIT.class);

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    public BooleanAgentServicesElasticImplIT() {
        if (!elasticsearchProperties.isElasticsearchDisabled()) {
            booleanAgentServices = IntegrationTestBase.genericApplicationContext.getBean(BooleanAgentServicesElasticImpl.class);
        }
    }

    @Override
    public String getNearExpression(String lhs, String rhs) {
        return getBinaryExpression(lhs, rhs, "/5");
    }

    @Override
    public String getAndExpression(String lhs, String rhs) {
        return getBinaryExpression(getParenthesizedTerm(lhs), getParenthesizedTerm(rhs), "and");
    }

    @Override
    public String getOrExpression(String lhs, String rhs) {
        return getBinaryExpression(getParenthesizedTerm(lhs), getParenthesizedTerm(rhs), "or");
    }

    @Override
    public String getSimilarDocumentExpression(String originalExpression, Collection<Term> distinctTerms) {
        //Actually, Elasticsearch has a separate query type "more_like_this" for similar-document queries
        //but there's no nice way to include this in the BooleanAgentServices interface model.
        //For integration test purposes, it's enough simply to query with the original content here,
        //though not very satisfying :(
        return originalExpression;
    }

    @Override
    public void waitForIndexRefresh() throws Exception {
        // Ensure the index reflects the deletion before retesting.
        //
        // Elasticsearch is near-realtime and there can be a default one-second delay between issuing a deletion request
        // and the deletion being apparent in index queries. This delay has been allowed for in the integration test
        // rather than in the delete() implementation itself, as the delete method may be called multiple times.
        TimeUnit.MILLISECONDS.sleep(1100);
    }

    @Test
    public void testCanConnect() {
        Assert.assertTrue("Elasticsearch BooleanAgentServices failed to connect", booleanAgentServices.canConnect());
    }

    @Test
    public void testICU() throws Exception {
        testExpression(2001L,
                "This สวัสดี ผมมาจากกรุงเทพฯ says 'Hello. I am from Bangkok' in Thai.",
                getNearExpression("มา", "จาก"),
                Arrays.asList("มา", "จาก"));
    }

    private String getParenthesizedTerm(String term) {
        return new StringBuilder("(").append(term).append(")").toString();
    }

    private String getBinaryExpression(String lhs, String rhs, String op) {
        return new StringBuilder(lhs).append(" ").append(op).append(" ").append(rhs).toString();
    }
}