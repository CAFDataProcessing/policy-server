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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.github.cafdataprocessing.corepolicy.common.dto.*;
import com.github.cafdataprocessing.corepolicy.common.dtoweb.*;
import com.google.common.base.Strings;
import com.github.cafdataprocessing.corepolicy.common.ApiProperties;
import com.github.cafdataprocessing.corepolicy.common.exceptions.BackEndRequestFailedCpeException;
import com.github.cafdataprocessing.corepolicy.common.exceptions.CpeException;
import com.github.cafdataprocessing.corepolicy.common.exceptions.InvalidFieldValueCpeException;
import com.github.cafdataprocessing.corepolicy.common.exceptions.MissingRequiredParameterCpeException;
import com.github.cafdataprocessing.corepolicy.common.exceptions.excpetionErrors.DataOperationFailureErrors;
import com.github.cafdataprocessing.corepolicy.common.exceptions.excpetionErrors.LocalisedExceptionError;
import com.github.cafdataprocessing.corepolicy.common.shared.CorePolicyObjectMapper;
import com.github.cafdataprocessing.corepolicy.common.shared.ErrorCodes;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.*;

/**
 * Class containing util methods for calling our web services (Java).
 */
public abstract class WebApiBase {

    protected CorePolicyObjectMapper mapper;
    private ApiProperties apiProperties;

    private HttpClientBuilder httpClientBuilder;

    public WebApiBase(ApiProperties apiProperties){
        this.apiProperties = apiProperties;
        this.mapper = new CorePolicyObjectMapper();
    }

    protected abstract Logger getLogger();

    protected ApiProperties getApiProperties(){
        return apiProperties;
    }

    Collection<NameValuePair> createParams(RetrieveRequest retrieveRequest){
        LinkedList<NameValuePair> params = new LinkedList<>();

        if(retrieveRequest.type == null) {
            getLogger().error("Error serializing RetrieveRequest, no type. " + retrieveRequest);
            throw new RuntimeException("Error serializing RetrieveRequest, has no type.");
        }

        params.add(new BasicNameValuePair("type", retrieveRequest.type.toValue()));

        if(retrieveRequest.max_page_results != null)
            params.add(new BasicNameValuePair("max_page_results", String.valueOf(retrieveRequest.max_page_results)));

        if(retrieveRequest.start != null)
            params.add(new BasicNameValuePair("start", String.valueOf(retrieveRequest.start)));

        if(retrieveRequest.id != null && !retrieveRequest.id.isEmpty()){
            for (Long id : retrieveRequest.id)
                params.add(new BasicNameValuePair("id", String.valueOf(id)));
        }

        if(retrieveRequest.additional != null){
            try {
                params.add(new BasicNameValuePair("additional", mapper.writeValueAsString(retrieveRequest.additional)));
            } catch (JsonProcessingException e) {
                getLogger().error("Error serializing additional information for retrieve request. " + retrieveRequest, e);
                throw new RuntimeException(e);
            }
        }

        return params;
    }

    Collection<NameValuePair> createParams(DeleteRequest deleteRequest){
        LinkedList<NameValuePair> params = new LinkedList<>();

        if(deleteRequest.type == null) {
            getLogger().error("Error serializing deleteRequest, no type. " + deleteRequest);
            throw new RuntimeException("Error serializing deleteRequest, has no type.");
        }
        params.add(new BasicNameValuePair("type", deleteRequest.type.toValue()));

        if(deleteRequest.id != null && !deleteRequest.id.isEmpty()){
            for (Long id : deleteRequest.id)
                params.add(new BasicNameValuePair("id", String.valueOf(id)));
        }

        return params;
    }

    Collection<NameValuePair> createParams(UpdateRequest updateRequest){
        LinkedList<NameValuePair> params = new LinkedList<>();
        params.add(new BasicNameValuePair("update_behaviour", updateRequest.updateBehaviour.toValue()));
        try {
            String serializedObject = mapper.writeValueAsString(updateRequest.objectToUpdate);
            JsonNode root = mapper.readTree(serializedObject);
            convertObjectToParams(root,params);
        } catch (JsonProcessingException e) {
            getLogger().error("Error serialising ", e);
            throw new RuntimeException(e);
        } catch (IOException e) {
            getLogger().error("Error deserialising ", e);
            throw new RuntimeException(e);
        }

        return params;
    }

    <V> V makeSingleRequest(WebApiAction requestType, Collection<NameValuePair> params, Class webDtoClass) {
        String resp = makeRequest(requestType, params);
        try {
            TypeFactory typeFactory = TypeFactory.defaultInstance();
            return mapper.readValue(resp, typeFactory.constructType(webDtoClass));
        } catch (IOException e) {
            getLogger().error("Error deserialising ", e);
            throw new RuntimeException(e);
        }
    }

    <T> T makeSingleRequest(WebApiAction requestType, T item, Class dtoClass) {
        JsonNode jsonRoot;
        try {
            String JsonString = mapper.writeValueAsString(item);
            jsonRoot = mapper.readTree(JsonString);
            String resp = makeRequest(requestType, jsonRoot);
            return item = (T) mapper.readValue(resp, dtoClass);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    <T> Collection<T> makeMultipleRequest(WebApiAction requestType, Collection<NameValuePair> params, Class<T> dtoClass) {
        String resp = makeRequest(requestType, params);
        try {
            TypeFactory typeFactory = TypeFactory.defaultInstance();
            PageOfResults<T> results = mapper.readValue(resp, typeFactory.constructParametricType(PageOfResults.class, dtoClass));
            return results.results;
        } catch (IOException e) {
            getLogger().error("Error deserialising ", e);
            throw new RuntimeException(e);
        }
    }



    <T extends DtoBase> PageOfResults<T> makePagedRequest(WebApiAction requestType, Collection<NameValuePair> params, Class<T> webDtoClass) {
        String resp = makeRequest(requestType, params);
        try {
            TypeFactory typeFactory = TypeFactory.defaultInstance();

            return mapper.readValue(resp, typeFactory.constructParametricType(PageOfResults.class, webDtoClass));
        } catch (IOException e) {
            getLogger().error("Error deserialising ", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * This method will make a request that expects no return type.
     */
    void makeVoidRequest(WebApiAction requestType, Collection<NameValuePair> params){
        makeRequest(requestType, params);
    }

    /**
     * This method examines the response object for a 200 response and looks to see if anything failed to delete.
     */
    void makeDeleteRequest(Long id, ItemType type){
        DeleteRequest deleteRequest = new DeleteRequest();
        deleteRequest.type = type;
        deleteRequest.id.add(id);
        final Collection<NameValuePair> params = createParams(deleteRequest);
        String resp = makeRequest(WebApiAction.DELETE, params);

        Collection<String> failedIds = new LinkedList<>();

        try {
            final DeleteResponse deleteResponse = mapper.readValue(resp, DeleteResponse.class);
            for(DeleteResult r : deleteResponse.result){
                if(r.success.equals(false)){
                    failedIds.add(String.valueOf(r.id));
                }
            }

            if(failedIds.size() != 0){
                throw new BackEndRequestFailedCpeException(new DataOperationFailureErrors(DataOperationFailureErrors.FailureOperation.DELETE, type, String.join(",", failedIds)));
            }
        }
        catch (CpeException e) {
            getLogger().error("Error deleting items", e);
            throw e;
        }
        catch (IOException e) {
            getLogger().error("Error deleting items", e);
            throw new BackEndRequestFailedCpeException(new DataOperationFailureErrors(DataOperationFailureErrors.FailureOperation.DELETE, type));

        }
    }

    HttpClientBuilder getHttpClientBuilder(){
        if(httpClientBuilder==null){
            synchronized (WebApiBase.class) {
                if (httpClientBuilder == null) {
                    if (!Strings.isNullOrEmpty(apiProperties.getHttpClientBuilderClass())){
                        try {
                            Class<?> httpClientBuilderClass = Class.forName(apiProperties.getHttpClientBuilderClass());
                            httpClientBuilder = (HttpClientBuilder)httpClientBuilderClass.newInstance();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                    else{
                        httpClientBuilder = HttpClientBuilder.create();//.setProxy(new HttpHost("localhost", 8888));
                    }
                }
            }
        }

        return httpClientBuilder;
    }

    String makeRequest(WebApiAction requestType, Collection<NameValuePair> params){

        try(CloseableHttpClient httpClient = getHttpClientBuilder().build()){

            URIBuilder builder = createBaseUrl(requestType);
            HttpUriRequest request = createRequest(builder, params);

            /* Create a custom response handler
            When using a ResponseHandler, HttpClient will automatically take care of ensuring release of the connection
            back to the connection manager regardless whether the request execution succeeds or causes an exception.
             */
            ResponseHandler<String> responseHandler = response -> {
                int status = response.getStatusLine().getStatusCode();
                HttpEntity entity = response.getEntity();

                // If the contenttype encoding isn't set
                // we need to update the default charset to UTF8 which is used for JSON - not ISO-8859-1.
                String responseBody = null;

                if ( entity != null ) {

                    // before we just grab the entity text, work out how to interpret it.
                    if (entity.getContentType() != null  &&
                            "application/json".equalsIgnoreCase(entity.getContentType().getValue())) {

                        // we have a json response, change default charset to UTF8 this in now
                        // way overrides the actual charset if specified by content-type header.
                        responseBody = EntityUtils.toString(entity, HTTP.UTF_8);
                    } else {
                        responseBody = EntityUtils.toString(entity);
                    }
                }

                getLogger().trace(responseBody);

                processResponseForError(responseBody, status);

                return responseBody;
            };

            return httpClient.execute(request, responseHandler);

        } catch (IOException | URISyntaxException e) {
            getLogger().error("Encountered an error when executing api request.", e);
            throw new RuntimeException(e);
        }
    }

    String makeRequest(WebApiAction requestType, JsonNode root){

        try(CloseableHttpClient httpClient = getHttpClientBuilder().build()){

            URIBuilder builder = createBaseUrl(requestType);
            HttpUriRequest request = createRequest(builder, root);

            /* Create a custom response handler
            When using a ResponseHandler, HttpClient will automatically take care of ensuring release of the connection
            back to the connection manager regardless whether the request execution succeeds or causes an exception.
             */
            ResponseHandler<String> responseHandler = response -> {
                int status = response.getStatusLine().getStatusCode();
                HttpEntity entity = response.getEntity();

                // If the contenttype encoding isn't set
                // we need to update the default charset to UTF8 which is used for JSON - not ISO-8859-1.
                String responseBody = null;

                if ( entity != null ) {

                    // before we just grab the entity text, work out how to interpret it.
                    if (entity.getContentType() != null  &&
                            "application/json".equalsIgnoreCase(entity.getContentType().getValue())) {

                        // we have a json response, change default charset to UTF8 this in now
                        // way overrides the actual charset if specified by content-type header.
                        responseBody = EntityUtils.toString(entity, HTTP.UTF_8);
                    } else {
                        responseBody = EntityUtils.toString(entity);
                    }
                }

                getLogger().trace(responseBody);

                processResponseForError(responseBody, status);

                return responseBody;
            };

            return httpClient.execute(request, responseHandler);

        } catch (IOException | URISyntaxException e) {
            getLogger().error("Encountered an error when executing api request.", e);
            throw new RuntimeException(e);
        }
    }

    /** In case of a 200 even though an error occurred, this method should ALWAYS be called to check for an error.
     * @param responseText The text value of the web request.
     */
    private void processResponseForError(String responseText, int responseStatus) {
        if (StringUtils.isEmpty(responseText))
            return;

        JsonNode responseNode;
        try {
            responseNode = mapper.readTree(responseText);
        } catch (Exception exception) {
            CpeException cpeException = new BackEndRequestFailedCpeException(new Exception("Could not parse returned text - " + responseText));
            getLogger().error("Returned body was not JSON", exception);
            throw cpeException;
        }

        if (!checkForFailure(responseStatus, responseNode))
            return;

        int errorCode = 0;
        String errorMessage = null;

        //use the presence of an 'error' property to decide if this is an error or not on a 200 response.
        if (responseNode.has("error")) {
            errorCode = responseNode.findValue("error").intValue();

            errorMessage = responseNode.has("reason")
                    ? responseNode.findValue("reason").asText()
                    : "Unexpected response status: " + responseStatus;
        }

        if (responseNode.has("message")) {
            errorMessage = responseNode.findValue("message").asText();
        }

        if (responseNode.has("detail")) {

            JsonNode detailNode = responseNode.findValue("detail");

            if (detailNode.has("error")) {
                errorCode = responseNode.findValue("error").intValue();
            }

            // if we get to here, and we dont have a valid errorMessage, add the detail
            // contents.
            if (Strings.isNullOrEmpty(errorMessage)) {
                errorMessage = "Unexpected response status: " + responseStatus;
            }

            // add detail section onto the end - have a think about formatting of this.
            // Its very handy for us / admin people, but should we format differently for normalusers?
            if (!Strings.isNullOrEmpty(detailNode.toString())) {
                errorMessage += " Detail: " + detailNode.toString();
            }
        }

        UUID correlationCode = null;
        if (responseNode.has("correlation_code")) {
            String correlation_code = responseNode.findValue("correlation_code").textValue();
            if(StringUtils.isNotBlank(correlation_code)) {
                correlationCode = UUID.fromString(correlation_code);
            }
        }

        // if error code is still 0, then set to response status code.
        if (errorCode == 0) {
            errorCode = responseStatus;
        }

        LocalisedExceptionError localisedExceptionError = new LocalisedExceptionError(errorMessage);

        switch (errorCode) {
            case ErrorCodes.INVALID_FIELD_VALUE: {
                throw new InvalidFieldValueCpeException(localisedExceptionError, correlationCode);
            }
            case ErrorCodes.MISSING_REQUIRED_PARAMETERS: {
                throw new MissingRequiredParameterCpeException(localisedExceptionError, correlationCode);
            }
            case ErrorCodes.GENERIC_ERROR: {
                throw new BackEndRequestFailedCpeException(localisedExceptionError, correlationCode);
            }
            default:{
                throw new BackEndRequestFailedCpeException(new RuntimeException(errorCode + " " + errorMessage));
            }
        }
    }

    private boolean checkForFailure(int responseStatus, JsonNode responseNode) {

        // all non 200s have to be an error of some type.
        if (responseStatus != 200) {
            return true;
        }

        //use the presence of an 'error' property to decide if this is an error or not on a 200 response.
        if (responseNode.has("error") || responseNode.has("detail") || responseNode.has("message")) {
            return true;
        }

        // its not an error, return.
        return false;
    }

    /**
     * Creates a GET or POST request depending on configuration.
     * @return  Either a GET or POST request.
     */
    private HttpUriRequest createRequest(URIBuilder builder, Collection<NameValuePair> params) throws URISyntaxException, UnsupportedEncodingException {
        Collection<NameValuePair> p = getApiParams();
            params.addAll(p);
        if(apiProperties.getUseHttpGet()) {
            for (NameValuePair nvp : params){
                builder.addParameter(nvp.getName(), nvp.getValue());
            }
            URI uri = builder.build();

            getLogger().trace("CREATING GET: " + URLDecoder.decode(uri.toString(), "UTF-8"));

            return new HttpGet(uri);
        }
        else {
            URI uri = builder.build();
            UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(params, Charset.forName("utf-8"));

            StringWriter writer = new StringWriter();
            try {
                IOUtils.copy(formEntity.getContent(), writer, "utf-8");
            } catch (IOException e) {
                getLogger().debug("Error when writing POST entity to string: " + e.getMessage(), e);
            }

            getLogger().trace("CREATING POST: " + URLDecoder.decode(uri.toString(), "UTF-8") + "?" + URLDecoder.decode(writer.toString(), "UTF-8"));

            HttpPost post = new HttpPost(uri);
            post.setEntity(formEntity);
            return post;
        }
    }

    private HttpUriRequest createRequest(URIBuilder builder, JsonNode root) throws URISyntaxException, UnsupportedEncodingException {

        List<NameValuePair> p = new ArrayList<>();

        convertObjectToParams(root, p);

        return createRequest(builder, p);
    }

    protected <T> PageOfResults<T> getRelatedItemPageOfResults(PageRequest pageRequest, ItemType itemType, Filter filter, Class webDtoClass) {
        RetrieveRequest retrieveRequest = new RetrieveRequest();
        retrieveRequest.type = itemType;
        retrieveRequest.max_page_results = pageRequest.max_page_results;
        retrieveRequest.start = pageRequest.start;

        if (filter != null) {
            retrieveRequest.additional = new RetrieveAdditional();
            retrieveRequest.additional.filter = filter;
        }

        final Collection<NameValuePair> params = createParams(retrieveRequest);

        return makePagedRequest(WebApiAction.RETRIEVE, params, webDtoClass);
    }

    protected <T> PageOfResults<T> getRelatedItemPageOfResults(PageRequest pageRequest, ItemType itemType, Filter filter, Sort sort, Class webDtoClass) {
        RetrieveRequest retrieveRequest = new RetrieveRequest();
        retrieveRequest.type = itemType;
        retrieveRequest.max_page_results = pageRequest.max_page_results;
        retrieveRequest.start = pageRequest.start;

        if ( filter != null ) {
            retrieveRequest.additional = new RetrieveAdditional();
            retrieveRequest.additional.filter = filter;
        }
        if (sort != null) {
            retrieveRequest.additional.sort = sort;
        }

        final Collection<NameValuePair> params = createParams(retrieveRequest);

        return makePagedRequest(WebApiAction.RETRIEVE, params, webDtoClass);
    }

    protected <T> PageOfResults<T> getRelatedItemPageOfResults(PageRequest pageRequest, ItemType itemType, Sort sort, Class webDtoClass) {
        RetrieveRequest retrieveRequest = new RetrieveRequest();
        retrieveRequest.type = itemType;
        retrieveRequest.max_page_results = pageRequest.max_page_results;
        retrieveRequest.start = pageRequest.start;
        if (sort != null) {
            retrieveRequest.additional = new RetrieveAdditional();
            retrieveRequest.additional.sort = sort;
        }

        final Collection<NameValuePair> params = createParams(retrieveRequest);

        return makePagedRequest(WebApiAction.RETRIEVE, params, webDtoClass);
    }

    private void convertObjectToParams(JsonNode root, List<NameValuePair> p){
        Iterator<String> it = root.fieldNames();
        while (it.hasNext()){
            String fieldName = it.next();
            JsonNode jsonNode = root.get(fieldName);

            if(jsonNode.isNull()){
//                p.add(new BasicNameValuePair(fieldName, null));
            }
            else if(jsonNode.isTextual()) {
                p.add(new BasicNameValuePair(fieldName, jsonNode.textValue()));
            } else {
                try {
                    p.add(new BasicNameValuePair(fieldName, mapper.writeValueAsString(jsonNode)));
                } catch (JsonProcessingException e) {
                    getLogger().debug("Could not read - " + jsonNode.toString(), e);
                }
            }
        }
    }

    /*================================================
        Code below here is abstract methods that implementations need to provide
    ================================================ */

    /**
     * Should return a uri builder initialized with the base url that requests will be sent to.
     * @param apiAction The action that we we want to take for the itemType.
     * @return  URIBuilder with NO PARAMS.
     */
    protected abstract URIBuilder createBaseUrl(WebApiAction apiAction);

    /**
     * Creates and returns a non null Collection of additional params required for the impl e.g. api_key/project_id.
     * @return  Additional params to be included.
     */
    protected abstract Collection<NameValuePair> getApiParams();
}
