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

import com.github.cafdataprocessing.corepolicy.common.ClassificationApi;
import com.github.cafdataprocessing.corepolicy.common.ClassifyDocumentApi;
import com.github.cafdataprocessing.corepolicy.common.PolicyApi;
import com.github.cafdataprocessing.corepolicy.common.WorkflowApi;
import org.springframework.context.ApplicationContext;

/**
 *
 * Provides helper methods, to get our Api interface classes.
 */
public class ApiProvider {
    private ApplicationContext applicationContext;

    public ApiProvider( ApplicationContext applicationContext ){
        // do not allow creation of the application context via this helper class - this prevents correct closure.
        this.applicationContext = applicationContext;
    }

    public ClassificationApi getClassificationApi(){
        return applicationContext.getBean(ClassificationApi.class);
    }

    public PolicyApi getPolicyApi(){
        return applicationContext.getBean(PolicyApi.class);
    }

    public ClassifyDocumentApi getClassifyDocumentApi() { return applicationContext.getBean(ClassifyDocumentApi.class ); }

    public WorkflowApi getWorkflowApi() { return applicationContext.getBean(WorkflowApi.class); }
}
