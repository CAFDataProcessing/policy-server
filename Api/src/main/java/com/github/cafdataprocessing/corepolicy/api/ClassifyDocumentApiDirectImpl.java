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

import com.github.cafdataprocessing.corepolicy.PolicyEngine;
import com.github.cafdataprocessing.corepolicy.PolicyHandler;
import com.github.cafdataprocessing.corepolicy.common.ClassifyDocumentApi;
import com.github.cafdataprocessing.corepolicy.common.Document;
import com.github.cafdataprocessing.corepolicy.common.PolicyApi;
import com.github.cafdataprocessing.corepolicy.common.dto.ClassifyDocumentResult;
import com.github.cafdataprocessing.corepolicy.common.dto.DocumentToExecutePolicyOn;
import com.github.cafdataprocessing.corepolicy.common.dto.PolicyType;
import com.github.cafdataprocessing.corepolicy.policy.MetadataPolicy.MetadataPolicyHandler;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collection;
import java.util.ServiceLoader;

/**
 *
 */
public class ClassifyDocumentApiDirectImpl implements ClassifyDocumentApi {

    private final PolicyApi policyApi;
    private final PolicyEngine policyEngine;
    private volatile boolean initialised;

    @Autowired
    public ClassifyDocumentApiDirectImpl(PolicyApi policyApi, PolicyEngine policyEngine){
        this.policyApi = policyApi;
        this.policyEngine = policyEngine;
    }

    void initialisePolicyHandlerIfNeeded() {
        if (!initialised) {
            synchronized (ClassifyDocumentApiDirectImpl.class) {
                if (!initialised) {
                    PolicyType metadataPolicyType = policyApi.retrievePolicyTypeByName("MetadataPolicy");
                    //This is a singleton and so is PolicyEngine so this should only happen once!
                    registerHandler(new MetadataPolicyHandler(metadataPolicyType.id));

                    //Add handlers from classpath
                    ServiceLoader<PolicyHandler> loader = ServiceLoader.load(PolicyHandler.class);
                    for (PolicyHandler d : loader) {
                        registerHandler(d);
                    }
                    initialised = true;
                }
            }
        }
    }

    public void registerHandler(PolicyHandler policyHandler) {
        this.policyEngine.registerPolicyHandler(policyHandler);
    }

    @Override
    public Collection<ClassifyDocumentResult> classify(long collectionSequenceId, Collection<Document> documentsToClassify) {
        initialisePolicyHandlerIfNeeded();
        ArrayList<ClassifyDocumentResult> results = new ArrayList<>();
        for(Document documentToClassify:documentsToClassify){
            results.add(policyEngine.classify(collectionSequenceId, documentToClassify));
        }
        return results;
    }

    @Override
    public ClassifyDocumentResult classify(long collectionSequenceId, Document document) {
        initialisePolicyHandlerIfNeeded();

        return policyEngine.classify(collectionSequenceId, document);
    }

    @Override
    public Collection<Document> execute(Long collectionSequenceId, Collection<DocumentToExecutePolicyOn> documentsToExecutePolicyOn) {
        initialisePolicyHandlerIfNeeded();
        ArrayList<Document> results = new ArrayList<>();
        for(DocumentToExecutePolicyOn documentToExecutePolicyOn:documentsToExecutePolicyOn) {
            policyEngine.execute(collectionSequenceId, documentToExecutePolicyOn.document, documentToExecutePolicyOn.policyIds);
            results.add(documentToExecutePolicyOn.document);
        }
        return results;
    }

    @Override
    public Document execute(Long collectionSequenceId, DocumentToExecutePolicyOn documentToExecutePolicyOn) {

        initialisePolicyHandlerIfNeeded();

        policyEngine.execute(collectionSequenceId, documentToExecutePolicyOn.document, documentToExecutePolicyOn.policyIds);

        return documentToExecutePolicyOn.document;
    }
}
