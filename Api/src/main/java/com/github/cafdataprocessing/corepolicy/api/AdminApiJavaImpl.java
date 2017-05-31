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

import com.github.cafdataprocessing.corepolicy.common.ApiProperties;
import com.github.cafdataprocessing.corepolicy.common.UserContext;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * Administration API which allows restricted access to create/update base data.
 */
public class AdminApiJavaImpl extends AdminApiWebBase {

    private UserContext context;

    @Autowired
    public AdminApiJavaImpl(UserContext context, ApiProperties apiProperties) {
        super(apiProperties);
        this.context = context;
    }

    @Override
    protected URIBuilder createBaseUrl(WebApiAction apiAction) {

        throw new UnsupportedOperationException("AdminApi does not have an endpoint via the webapi");
    }

    @Override
    protected Collection<NameValuePair> getApiParams() {
        Collection<NameValuePair> params = new ArrayList<>(1);
        params.add(new BasicNameValuePair("project_id", context.getProjectId()));
        return params;
    }
}
