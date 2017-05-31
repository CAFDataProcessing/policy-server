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

import com.google.common.base.Strings;
import com.github.cafdataprocessing.corepolicy.common.ClassificationApi;
import com.github.cafdataprocessing.corepolicy.common.ClassifyDocumentApi;
import com.github.cafdataprocessing.corepolicy.common.exceptions.ConditionEngineException;
import com.github.cafdataprocessing.corepolicy.common.dto.CollectionSequence;
import com.github.cafdataprocessing.corepolicy.common.dtoweb.ClassifyDocumentResponse;
import com.github.cafdataprocessing.corepolicy.common.dtoweb.DocumentArrayWrapper;
import com.github.cafdataprocessing.corepolicy.common.dtoweb.DocumentPoliciesWrapper;
import com.github.cafdataprocessing.corepolicy.common.dtoweb.ExecutePolicyResponse;
import com.github.cafdataprocessing.corepolicy.common.logging.LogHelper;
import com.github.cafdataprocessing.corepolicy.common.shared.CorePolicyObjectMapper;
import com.github.cafdataprocessing.corepolicy.common.shared.ErrorCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collection;

/**
* API front end for evaluating conditions. This is designed just to be a test interface, the real work will be done by
* ExecutePolicy interface when it is designed.
*/
@RestController
public class ClassifyDocumentController extends BaseErrorHandlingController {
    final static Logger logger = LoggerFactory.getLogger(ClassifyDocumentController.class);

    private ClassificationApi classificationApi;
    private final ClassifyDocumentApi classifyDocumentApi;
    CorePolicyObjectMapper objectMapper = new CorePolicyObjectMapper();

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    public ClassifyDocumentController(ClassificationApi classificationApi, ClassifyDocumentApi classifyDocumentApi){
        this.classificationApi = classificationApi;
        this.classifyDocumentApi = classifyDocumentApi;
    }

    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = "/classifydocument")
    public ClassifyDocumentResponse classifyDocument(
            @RequestParam("json") String json,
            @RequestParam("collection_sequence") String sequenceIdentifier,
            HttpServletRequest request
    ) throws Exception {
        logRequest(logger, request);
        if(json == null || json.isEmpty())
            throw new Exception("Expected documents in json argument.");

        CollectionSequence sequence = getCollectionSequence(sequenceIdentifier);

        DocumentArrayWrapper classifyDocumentRequest = objectMapper.readValue(json, DocumentArrayWrapper.class);
        ClassifyDocumentResponse classifyDocumentResponse = new ClassifyDocumentResponse();
        if (classifyDocumentRequest.document != null) {
            classifyDocumentResponse.result = classifyDocumentApi.classify(sequence.id, classifyDocumentRequest.document);
        }
        return classifyDocumentResponse;
    }

    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = "/executepolicy")
    public ExecutePolicyResponse executePolicy(
            @RequestParam("json") String json,
            @RequestParam("collection_sequence") String sequenceIdentifier,
            HttpServletRequest request
    ) throws Exception {

        logRequest(logger, request);

        DocumentPoliciesWrapper documentPoliciesWrapper = objectMapper.readValue(json, DocumentPoliciesWrapper.class);

        Long sequenceId = null;
        if(!Strings.isNullOrEmpty(sequenceIdentifier)){
            //If the collection sequence id is not specified then policies will be rertrieved from the policy api
            CollectionSequence sequence = getCollectionSequence(sequenceIdentifier);
            sequenceId = sequence.id;
        }
        ExecutePolicyResponse executePolicyResponse = new ExecutePolicyResponse();
        executePolicyResponse.result = classifyDocumentApi.execute(sequenceId, documentPoliciesWrapper.documentPolicies);

        return executePolicyResponse;
    }

    private CollectionSequence getCollectionSequence(String sequenceIdentifier) throws Exception {
        if(sequenceIdentifier == null)
            throw new Exception("Expected a collection sequence identifier.");

        CollectionSequence sequence;
        try{
            Long sequenceId = Long.valueOf(sequenceIdentifier);
            ArrayList<Long> ids = new ArrayList<>(1);
            ids.add(sequenceId);
            sequence = this.classificationApi.retrieveCollectionSequences(ids).stream().findFirst().get();
        } catch (NumberFormatException ignored){
            logger.warn("Could not resolve id: " + LogHelper.removeWhiteSpace(sequenceIdentifier) + ", attempting to resolve by name. Resolving by id is preferred.");
            Collection<CollectionSequence> collectionSequences = this.classificationApi.retrieveCollectionSequencesByName(sequenceIdentifier);
            if(collectionSequences.size() != 1)
                throw new ConditionEngineException(ErrorCodes.GENERIC_ERROR, "Could not resolve the collection sequence" + sequenceIdentifier + " to a single collection sequence, found " + collectionSequences.size());
            sequence = collectionSequences.stream().findFirst().get();
        }
        return sequence;
    }
}
