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
package com.github.cafdataprocessing.corepolicy.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.cafdataprocessing.corepolicy.common.dto.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;

/**
 *
 */
@RunWith(Parameterized.class)
public class PolicyApiRetrieveIT extends PolicyApiTestBase {
    private final ItemType itemType;
    Consumer<Integer> create;
    Function<PageRequest, PageOfResults<?>> retrieve;

    public PolicyApiRetrieveIT(ItemType itemType){

        ObjectMapper mapper = new ObjectMapper();

        this.itemType = itemType;
        switch(itemType){
            case POLICY:{
                this.create = (index) ->{
                    Policy policy = new Policy();
                    policy.name = String.valueOf(index);
                    policy.typeId = 1L;
                    try {
                        policy.details = mapper.readTree(PolicyApiIT.metadataPolicyJson);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    sut.create(policy);
                };
                this.retrieve = sut::retrievePoliciesPage;
                break;
            }
            case POLICY_TYPE:{
                this.create = (index) ->{
                    PolicyType policyType = new PolicyType();
                    policyType.name = String.valueOf(index);
                    try {
                        policyType.definition = mapper.readTree(PolicyApiIT.policyTypeJson);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    policyType.shortName = "UniqueName:" + UUID.randomUUID().toString() + ":" + String.valueOf(index);
                    sut.create(policyType);
                };
                this.retrieve = sut::retrievePolicyTypesPage;
                break;
            }
        }
    }

    @Parameterized.Parameters
    public static Collection<Object[]> getParameters(){
        return Arrays.asList(new Object[][]{
                {ItemType.POLICY}, {ItemType.POLICY_TYPE}
        });
    }

    @Test
    public void testRetrieve() throws Exception {
        System.out.println(itemType.toValue());

        int targetNumber = 16;
        if(itemType==ItemType.POLICY_TYPE){
            targetNumber=14; //As there is a global policy type
        }

        // Ok before we create any policies get the hit count now!
        long initialHitCount = 0;
        {
            PageOfResults pageOfResults = retrieve.apply(new PageRequest());
            initialHitCount = pageOfResults.totalhits;
        }

        for (int i = 0; i < targetNumber; i++) {
            create.accept(i);
        }

        {
            PageRequest pageRequest = new PageRequest();
            pageRequest.start = initialHitCount + 1L;
            pageRequest.max_page_results = 10L;
            PageOfResults pageOfResults = retrieve.apply(pageRequest);
            assertEquals(initialHitCount + targetNumber, pageOfResults.totalhits.longValue());
            assertEquals(10, pageOfResults.results.size());
        }

        {
            PageRequest pageRequest = new PageRequest();
            pageRequest.start = initialHitCount + 1L;
            pageRequest.max_page_results = 12L;
            PageOfResults pageOfResults = retrieve.apply(pageRequest);
            assertEquals(initialHitCount + targetNumber, pageOfResults.totalhits.longValue());
            assertEquals(12, pageOfResults.results.size());
        }

        {
            PageRequest pageRequest = new PageRequest();
            pageRequest.start = initialHitCount + 5L;
            pageRequest.max_page_results = 10L;
            PageOfResults pageOfResults = retrieve.apply(pageRequest);
            assertEquals(initialHitCount + targetNumber, pageOfResults.totalhits.longValue());
            assertEquals(10, pageOfResults.results.size());
        }

        {
            // try to request last element created
            PageRequest pageRequest = new PageRequest();
            pageRequest.start = initialHitCount + targetNumber;
            pageRequest.max_page_results = 10L;
            PageOfResults pageOfResults = retrieve.apply(pageRequest);
            assertEquals(initialHitCount + targetNumber, pageOfResults.totalhits.longValue());
            assertEquals(1, pageOfResults.results.size());
        }
        {
            PageRequest pageRequest = new PageRequest();
            pageRequest.start = initialHitCount + 30L;
            pageRequest.max_page_results = 10L;
            PageOfResults pageOfResults = retrieve.apply(pageRequest);
            assertEquals(initialHitCount + targetNumber, pageOfResults.totalhits.longValue());
            assertEquals(0, pageOfResults.results.size());
        }
    }
}
