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
package com.github.cafdataprocessing.corepolicy.testing;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

/**
 *
 * Please note this file is only for use within the testing project, and is not released with our common configuration
 * files.
 */


@Configuration
@PropertySource("classpath:testing.properties")
@PropertySource(value = "file:${CAF_COREPOLICY_CONFIG}/testing.properties", ignoreResourceNotFound = true)
public class TestingProperties {

    @Autowired
    private Environment environment;

    public String getProjectId()
    {
        return environment.getProperty("testing.projectid");
    }

    public boolean getInDocker() {
        return Boolean.valueOf(environment.getProperty("testing.indocker","false"));
    }

    public boolean getWebInHibernate() {
        return Boolean.valueOf(environment.getProperty("testing.webinhibernate","false"));
    }

}
