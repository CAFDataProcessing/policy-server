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
package com.github.cafdataprocessing.corepolicy.web;

import com.github.cafdataprocessing.corepolicy.booleanagent.BooleanAgentServices;
import com.github.cafdataprocessing.corepolicy.common.ElasticsearchProperties;
import com.github.cafdataprocessing.corepolicy.common.exceptions.BackEndRequestFailedCpeException;
import com.github.cafdataprocessing.corepolicy.common.exceptions.excpetionErrors.BackEndRequestFailedErrors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Provides method to determine if service is in a healthy state for use.
 */

@RestController
@RequestMapping(value = "/healthcheck", method = {RequestMethod.GET, RequestMethod.POST})
public class PolicyHealthCheckController extends BaseErrorHandlingController {

    private BooleanAgentServices booleanAgentServices;
    private ElasticsearchProperties elasticsearchProperties;

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    public PolicyHealthCheckController(BooleanAgentServices booleanAgentServices, ElasticsearchProperties elasticsearchProperties){
        this.booleanAgentServices = booleanAgentServices;
        this.elasticsearchProperties = elasticsearchProperties;
    }

    @RequestMapping()
    public Boolean checkHealth() {

        // if elasticsearch is disabled, then return true
        if (elasticsearchProperties.isElasticsearchDisabled())
            return true;

        // elasticsearch is configured on, use it as main health indicator.
        if (booleanAgentServices.canConnect()) {
            return true;
        }

        throw new BackEndRequestFailedCpeException(BackEndRequestFailedErrors.ElasticsearchConnectionFailed);
    }
}
