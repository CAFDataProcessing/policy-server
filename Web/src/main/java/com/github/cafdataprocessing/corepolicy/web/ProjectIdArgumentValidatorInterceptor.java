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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.cafdataprocessing.corepolicy.common.exceptions.BackEndRequestFailedCpeException;
import com.github.cafdataprocessing.corepolicy.common.exceptions.MissingRequiredParameterCpeException;
import com.github.cafdataprocessing.corepolicy.common.exceptions.excpetionErrors.MissingRequiredParameterErrors;
import com.github.cafdataprocessing.corepolicy.common.shared.CorePolicyObjectMapper;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 *
 */
public class ProjectIdArgumentValidatorInterceptor extends HandlerInterceptorAdapter {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        if (request.getRequestURI().equals("/favicon.ico"))
            return true;

        JsonNode jsonNode;
        switch (request.getMethod()) {
            case "POST": {
                if (request.getContentType().equalsIgnoreCase("application/json")) {
                    jsonNode = readJsonFromBody(request);
                    break;
                }
            }
            default: {
                jsonNode = readJsonFromRequest(request);
            }
        }

        JsonNode projectNode = jsonNode.get("project_id");
        if(projectNode == null || projectNode.asText().isEmpty()){
            MissingRequiredParameterCpeException missingRequiredParameterCpeException = new MissingRequiredParameterCpeException(MissingRequiredParameterErrors.PROJECT_ID_REQUIRED);
            ErrorResponse errorResponse = new ErrorResponse(missingRequiredParameterCpeException.getErrorCode(), missingRequiredParameterCpeException.getLocalizedMessage(), missingRequiredParameterCpeException.getError().getLocalisedMessage(), missingRequiredParameterCpeException.getCorrelationCode());
            CorePolicyObjectMapper corePolicyObjectMapper = new CorePolicyObjectMapper();
            corePolicyObjectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            String errorMessage = corePolicyObjectMapper.writeValueAsString(errorResponse);

            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().print(errorMessage);
            return false;
        }

        request.setAttribute("jsonParameters", jsonNode);

        return true;
    }

    private JsonNode readJsonFromBody(HttpServletRequest request) {

        ObjectMapper mapper = new ObjectMapper();

        try {
            return mapper.readTree(request.getReader());
        } catch (IOException e) {
            throw new BackEndRequestFailedCpeException(e);
        }
    }

    private JsonNode readJsonFromRequest(HttpServletRequest request) {
        Map<String, String[]> parameterMap = request.getParameterMap();

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode objectNode = mapper.createObjectNode();

        for (Map.Entry<String, String[]> entry: parameterMap.entrySet()) {

            String name = entry.getKey();

            if(name.endsWith("[]") && name.length() > 2) {
                name = name.substring(0, name.length() - 2);
            }

            switch (entry.getValue().length){
                case 0 :{
                    break;
                }
                case 1: {
                    String item = entry.getValue()[0];

                    objectNode.put(name, item);
                    break;
                }
                default : {
                    ArrayNode jsonNodes = objectNode.putArray(name);
                    for (String item: entry.getValue()) {
                        jsonNodes.add(item);
                    }
                    break;
                }
            }
        }

        return objectNode;
    }
}
