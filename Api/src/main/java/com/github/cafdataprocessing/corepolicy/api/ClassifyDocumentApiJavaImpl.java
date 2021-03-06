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
package com.github.cafdataprocessing.corepolicy.api;

import com.github.cafdataprocessing.corepolicy.common.ApiProperties;
import com.github.cafdataprocessing.corepolicy.common.UserContext;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Web implementation of the ClassifyDocumentApi that goes to Java WS.
 */
public class ClassifyDocumentApiJavaImpl extends ClassifyDocumentApiWebBase {

    private UserContext context;

    @Autowired
    ClassifyDocumentApiJavaImpl(UserContext userContext, ApiProperties apiProperties){
        super(apiProperties);
        this.context = userContext;
    }

    @Override
    protected URIBuilder createBaseUrl(WebApiAction apiAction) {
        StringBuilder sb = new StringBuilder(getApiProperties().getWebServiceUrl());
        if(!sb.toString().endsWith("/"))
            sb.append("/");

        //since we are in policy api, then obviously this is a policy endpoint!
        sb.append(apiAction.toValue());

        String uri = sb.toString();
        try {
            return new URIBuilder(uri);
        } catch (URISyntaxException e) {
            getLogger().error("Problem with URI - syntax was invalid: " + uri, e);
            throw new RuntimeException("Problem with URI - syntax was invalid: " + uri, e);
        }
    }

    @Override
    protected Collection<NameValuePair> getApiParams() {
        Collection<NameValuePair> params = new ArrayList<>(1);
        params.add(new BasicNameValuePair("project_id", context.getProjectId()));
        return params;
    }
}
