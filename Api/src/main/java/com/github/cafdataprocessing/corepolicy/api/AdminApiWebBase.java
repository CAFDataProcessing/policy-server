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
package com.github.cafdataprocessing.corepolicy.api;

import com.github.cafdataprocessing.corepolicy.common.AdminApi;
import com.github.cafdataprocessing.corepolicy.common.ApiProperties;
import com.github.cafdataprocessing.corepolicy.common.dto.PolicyType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for web implementations of AdminApi
 */
public abstract class AdminApiWebBase extends WebApiBase implements AdminApi {

    private static final Logger logger = LoggerFactory.getLogger(AdminApiWebBase.class);

    public AdminApiWebBase(ApiProperties apiProperties) {
        super(apiProperties);
    }

    protected Logger getLogger() {
        return logger;
    }

    @Override
    public PolicyType create(PolicyType policy) {
        throw new UnsupportedOperationException("AdminApi is unavailable via the webapi");
    }

    @Override
    public PolicyType update(PolicyType policy) {
        throw new UnsupportedOperationException("AdminApi is unavailable via the webapi");
    }

    @Override
    public void deletePolicyType(Long id) {
        throw new UnsupportedOperationException("AdminApi is unavailable via the webapi");
    }
}
