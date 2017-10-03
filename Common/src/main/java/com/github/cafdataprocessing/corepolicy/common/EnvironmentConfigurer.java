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
package com.github.cafdataprocessing.corepolicy.common;

import com.github.cafdataprocessing.corepolicy.common.security.CorePolicyEncryptorProviderImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.Arrays;

/**
 *
 */
public class EnvironmentConfigurer {

    private static Logger logger = LoggerFactory.getLogger(EnvironmentConfigurer.class);

    public static void configure(ConfigurableEnvironment configurableEnvironment){
        logger.info(String.format("CAF_COREPOLICY_CONFIG: %s",
                configurableEnvironment.getProperty("CAF_COREPOLICY_CONFIG")));

        AnnotationConfigApplicationContext propertiesApplicationContext = new AnnotationConfigApplicationContext ();
        propertiesApplicationContext.register(ConversionConfiguration.class);
        propertiesApplicationContext.register(PropertySourcesPlaceholderConfigurer.class);
        propertiesApplicationContext.register(CorePolicyEncryptorProviderImpl.class);
        propertiesApplicationContext.register(ApiProperties.class,
                EngineProperties.class,
                ElasticsearchProperties.class);
        propertiesApplicationContext.refresh();

        ApiProperties apiProperties = propertiesApplicationContext.getBean(ApiProperties.class);
        ElasticsearchProperties elasticsearchProperties = propertiesApplicationContext.getBean(ElasticsearchProperties.class);

        for(String active : configurableEnvironment.getActiveProfiles()){
            logger.info("PRE PROFILE: " + active);
        }

        if(apiProperties.getMode()!=null){
            if(!apiProperties.getMode().equalsIgnoreCase("none")){
                configurableEnvironment.addActiveProfile("api");
                // document stats may be removed - temporary code.
                configurableEnvironment.addActiveProfile("document-stats-api");
                if(Arrays.asList(configurableEnvironment.getActiveProfiles()).stream().noneMatch(p -> p.equalsIgnoreCase("webserver"))){
                    configurableEnvironment.addActiveProfile("threadusercontext");
                    configurableEnvironment.addActiveProfile("application-scope");
                }
                else
                {
                    configurableEnvironment.addActiveProfile("web-application-scope");
                }
            }

            if(apiProperties.getMode().equalsIgnoreCase("web")){
                configurableEnvironment.addActiveProfile("apiweb");
            }

            if(apiProperties.getMode().equalsIgnoreCase("direct")){
                configurableEnvironment.addActiveProfile("apidirect");

                // Only if we are in direct mode should we be using the api.direct.repository
                if(apiProperties.getRepository()!=null){
                    if(apiProperties.getRepository().equalsIgnoreCase("h2")) {
                        configurableEnvironment.addActiveProfile("h2");
                    }
                    if(apiProperties.getRepository().equalsIgnoreCase("hibernate")) {
                        configurableEnvironment.addActiveProfile("apihibernate");
                        configurableEnvironment.addActiveProfile("administrationusercontext");
                    }
                }
                configurableEnvironment.addActiveProfile("apidirect-environment-snapshot-repository-none");
            }
        }

        if (configurableEnvironment.acceptsProfiles("api")) {
            configurableEnvironment.addActiveProfile("environmentcache-fs");
        }

        if(elasticsearchProperties.isElasticsearchDisabled()){
            configurableEnvironment.addActiveProfile("noelastic");
        }
        else{
            configurableEnvironment.addActiveProfile("elastic");
        }

        for(String active : configurableEnvironment.getActiveProfiles()){
            logger.info("POST PROFILE: " + active);
        }
    }
}
