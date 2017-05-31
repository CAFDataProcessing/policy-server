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
package com.github.cafdataprocessing.corepolicy.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.cafdataprocessing.corepolicy.common.UserContext;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 *
 */
@Scope(value = "request", proxyMode = ScopedProxyMode.INTERFACES)
@Component
public class HttpUserContext implements UserContext {
    private final HttpServletRequest httpServletRequest;

    @Autowired
    public HttpUserContext (HttpServletRequest httpServletRequest){
        this.httpServletRequest = httpServletRequest;
    }

    @Override
    public String getProjectId() {
        return getParameter("project_id");
    }

    @Override
    public void setProjectId(String projectId) {
        throw new NotImplementedException("setProjectId is not implemented.");
    }

    private String getParameter(String name) {
        JsonNode jsonParameters = (JsonNode)httpServletRequest.getAttribute("jsonParameters");
        return jsonParameters.get(name).asText();
    }
}
