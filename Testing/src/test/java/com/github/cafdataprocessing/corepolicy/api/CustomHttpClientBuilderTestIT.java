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

import com.github.cafdataprocessing.corepolicy.common.CorePolicyApplicationContext;
import com.github.cafdataprocessing.corepolicy.common.PolicyApi;
import com.github.cafdataprocessing.corepolicy.common.shared.TemporaryEnvChanger;
import com.github.cafdataprocessing.corepolicy.testing.Assume;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

/**
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class CustomHttpClientBuilderTestIT extends PolicyApiTestBase {

    @Before
    public void setup() throws Exception {

        //these tests will only actually be run in web mode.
        //they will show as ignored otherwise ( this is a valid skipped test by design ).
        Assume.assumeTrue(Assume.AssumeReason.BY_DESIGN,
                "CustomHttpClientBuilderTestIT::setup",
                "Only running CustomHttpClientBuilderTestIT tests in web mode.",
                apiProperties.getMode().equalsIgnoreCase("web"),
                genericApplicationContext );
    }


    @Test
    public void testCustomHttpClientBuilderPolicy() throws Exception {

        String className = MyHttpClientBuilder.class.getName();
        try(TemporaryEnvChanger temporaryEnvChanger = new TemporaryEnvChanger("api.web.httpclientbuilderclass", className)){
            CorePolicyApplicationContext corePolicyApplicationContext = new CorePolicyApplicationContext();
            corePolicyApplicationContext.refresh();
            PolicyApi policyApi = corePolicyApplicationContext.getBean(PolicyApi.class);

            try{
                policyApi.retrievePolicyTypeByName("test");
            }
            catch(ExceptionToTest ignored){
                // An ExceptionTest should have been thrown and caught here.
                // We do not need to assume that we are in web mode, because the @Before method will guarantee us that
                // If another exception was thrown then we probably tried to use the Default HttpClient Builder
            }
        }
    }

    static class MyHttpClientBuilder extends HttpClientBuilder{

        public MyHttpClientBuilder(){
            super();
        }

        @Override
        public CloseableHttpClient build(){
            throw new ExceptionToTest();
        }
    }

    static class ExceptionToTest extends RuntimeException{
    }
}
