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

import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;

/**
 *
 */
public class CorePolicyApplicationContext extends GenericApplicationContext implements AutoCloseable {
    public CorePolicyApplicationContext(){
        EnvironmentConfigurer.configure(this.getEnvironment());
        XmlBeanDefinitionReader xmlReader = new XmlBeanDefinitionReader(this);
        xmlReader.loadBeanDefinitions(new ClassPathResource("corepolicy-beans.xml"));

        //this is required in non web contexts to make sure our DisposableBean implementations have their destroy method called
        this.registerShutdownHook();
    }


}
