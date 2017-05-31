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
package com.github.cafdataprocessing.corepolicy.web.schema;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonschema.core.exceptions.InvalidSchemaException;
import com.github.cafdataprocessing.corepolicy.common.shared.CorePolicyObjectMapper;
import org.apache.commons.io.IOUtils;

import java.io.StringReader;

/**
 *
 */
public class ApiSchemaValidatorTestBase {


    final CorePolicyObjectMapper mapper = new CorePolicyObjectMapper();

    public void validateRequest(ApiSchemaValidator apiSchemaValidator, String resourceName) throws Exception {
        ApiSchemaValidator sut = apiSchemaValidator;

        String jsonTestData = IOUtils.toString(ClassLoader.class.getResourceAsStream(resourceName), "UTF-8");
        final ObjectMapper objectMapper = new ObjectMapper();
        final ApiTestData apiTestData = objectMapper.reader(ApiTestData.class).readValue(new StringReader(jsonTestData));
        for(ApiTest apiTest:apiTestData.apiTests){
            String jsonString = apiTest.json.toJSONString();
            if(apiTest.valid){
                try {
                    sut.validateRequest(jsonString);
                    System.out.println("Test passed '" + apiTest.description + "'");
                }
                catch (Exception e){
                    throw new ApiTestException("Test failed - '" + apiTest.description + "'" + e.toString());
                }
            }
            else {
                try{
                    sut.validateRequest(jsonString);
                    throw new ApiTestException("Test should have failed - '" + apiTest.description + "'");
                }
                catch(InvalidSchemaException e){
                    System.out.println("Test failed '" + apiTest.description + "'");
                    throw e;
                }
                catch (ApiTestException e){
                    System.out.println("Test failed '" + apiTest.description + "'");
                    throw e;
                }
                catch(Exception ex){
                    System.out.println("Test passed correctly reported exception '" + apiTest.description + "'");
                }
            }
        }
    }

}
