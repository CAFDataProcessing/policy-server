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
import com.github.cafdataprocessing.corepolicy.common.ApiProperties;
import com.github.cafdataprocessing.corepolicy.common.ClassifyDocumentApi;
import com.github.cafdataprocessing.corepolicy.common.Document;
import com.github.cafdataprocessing.corepolicy.common.dto.ClassifyDocumentResult;
import com.github.cafdataprocessing.corepolicy.common.dto.DocumentToExecutePolicyOn;
import com.github.cafdataprocessing.corepolicy.common.dtoweb.ClassifyDocumentResponse;
import com.github.cafdataprocessing.corepolicy.common.dtoweb.DocumentArrayWrapper;
import com.github.cafdataprocessing.corepolicy.common.dtoweb.DocumentPoliciesWrapper;
import com.github.cafdataprocessing.corepolicy.common.dtoweb.ExecutePolicyResponse;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

/**
 * Base class for any web implementation of the ClassifyDocumentApi interface.
 */
public abstract class ClassifyDocumentApiWebBase extends WebApiBase implements ClassifyDocumentApi {
    private final static Logger logger = LoggerFactory.getLogger(ClassifyDocumentApiWebBase.class);

    public ClassifyDocumentApiWebBase(ApiProperties apiProperties) {
        super(apiProperties);
    }

    @Override
    protected Logger getLogger() { return logger;}

    @Override
    public Collection<ClassifyDocumentResult> classify(long collectionSequenceId, Collection<Document> documentsToClassify){
        LinkedList<NameValuePair> params = new LinkedList<>();

        params.add(new BasicNameValuePair("collection_sequence", String.valueOf(collectionSequenceId)));

        DocumentArrayWrapper classifyDocumentRequest = new DocumentArrayWrapper();
        classifyDocumentRequest.document = documentsToClassify;

        try {
            params.add(new BasicNameValuePair("json", mapper.writeValueAsString(classifyDocumentRequest)));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        String response = makeRequest(WebApiAction.CLASSIFYDOCUMENT, params);

        try {
            ClassifyDocumentResponse classifyDocumentResponse = mapper.treeToValue(mapper.readTree(response), ClassifyDocumentResponse.class);
            return classifyDocumentResponse.result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Collection<Document> execute(Long collectionSequenceId, Collection<DocumentToExecutePolicyOn> documentsToExecutePolicyOn){
        LinkedList<NameValuePair> params = new LinkedList<>();

        params.add(new BasicNameValuePair("collection_sequence", String.valueOf(collectionSequenceId)));

        DocumentPoliciesWrapper documentPoliciesWrapper = new DocumentPoliciesWrapper();
        documentPoliciesWrapper.documentPolicies= documentsToExecutePolicyOn;


        try {
            params.add(new BasicNameValuePair("json", mapper.writeValueAsString(documentPoliciesWrapper)));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        String response = makeRequest(WebApiAction.EXECUTEPOLICY, params);

        try {
            ExecutePolicyResponse executePolicyResponse = mapper.treeToValue(mapper.readTree(response), ExecutePolicyResponse.class);
            return executePolicyResponse.result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ClassifyDocumentResult classify(long collectionSequenceId, Document document) {
        return classify(collectionSequenceId, Arrays.asList(document)).stream().findAny().get();
    }

    @Override
    public Document execute(Long collectionSequenceId, DocumentToExecutePolicyOn documentToExecutePolicyOn) {
        return execute(collectionSequenceId, Arrays.asList(documentToExecutePolicyOn)).stream().findAny().get();
    }
}
