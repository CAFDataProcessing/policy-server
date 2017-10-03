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
package com.github.cafdataprocessing.corepolicy.web;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 *
 */
public class ErrorResponseTest {
    @Before
    public void setup(){

    }

    @After
    public void cleanup(){

    }

    /**
     * This method will test that exception types are stripped from an error message as expected, or not if not
     * required.
     */
    @Test
    public void testSanitizeWorks(){

        Map<String, String> errors = new HashMap<>();
        errors.put("There was a problem", "There was a problem");
        errors.put("There was a problem  ", "There was a problem");
        errors.put("  There was a problem  ", "There was a problem");

        errors.put("Exception: There was a problem", "There was a problem");
        errors.put("There was an Exception: oops", "There was an Exception: oops");

        errors.put("java.xxx.Exception: There was a problem", "There was a problem");
        errors.put("java.xxx.Exception: Java.yyy.Exception: There was a problem", "There was a problem");
        errors.put("java.lang.RuntimeException: java.sql.SQLException: One or more collection sequences in the ID list do not exist", "One or more collection sequences in the ID list do not exist");


        Collection<String> failedMessages = new LinkedList<>();

        for (Map.Entry<String, String> testMessage : errors.entrySet()){
            String sanitized = ErrorResponse.sanitizeReason(testMessage.getKey());

            if(!sanitized.equals(testMessage.getValue())) {
                failedMessages.add(String.format("Message '%s'\terror: expected | actual:\t'%s'\t|\t'%s'"
                        , testMessage.getKey()
                        , testMessage.getValue()
                        , sanitized
                ));
            }
        }

        if(failedMessages.size() != 0){
            Assert.fail("Failure due to the following messages: \n" + String.join("\n", failedMessages));
        }
    }
}