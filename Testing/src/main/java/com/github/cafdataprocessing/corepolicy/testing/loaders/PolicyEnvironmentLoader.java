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
package com.github.cafdataprocessing.corepolicy.testing.loaders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.cafdataprocessing.corepolicy.common.ClassificationApi;
import com.github.cafdataprocessing.corepolicy.common.PolicyApi;
import com.github.cafdataprocessing.corepolicy.common.dto.*;
import com.github.cafdataprocessing.corepolicy.common.exceptions.ConditionEngineException;
import com.github.cafdataprocessing.corepolicy.common.dto.conditions.Condition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;

/**
 *
 */
@Component
public class PolicyEnvironmentLoader  {

    private final PolicyApi policyApi;
    private final ClassificationApi classificationApi;
    private ObjectMapper objectMapper = new ObjectMapper();
    protected IdManager idManager;

    @Autowired
    public PolicyEnvironmentLoader(PolicyApi policyApi, ClassificationApi classificationApi){

        this.policyApi = policyApi;
        this.classificationApi = classificationApi;
    }

    public void loadCollectionSequences(Collection<CollectionSequence> collectionSequences) throws Exception {
        if(collectionSequences==null) return;
        for(CollectionSequence collectionSequence:collectionSequences){
            classificationApi.create(collectionSequence);
        }
    }

    public void loadCollections(Collection<DocumentCollection> documentCollections) throws ConditionEngineException {
        if(documentCollections==null) return;
        for(DocumentCollection documentCollection:documentCollections){
            classificationApi.create(documentCollection);
        }
    }

    public void loadConditions(Collection<Condition> conditions) throws ConditionEngineException {
        if(conditions==null) return;
        for(Condition condition:conditions){
            classificationApi.create(condition);
        }
    }

    public void loadLexicons(Collection<Lexicon> lexicons) throws ConditionEngineException {
        if(lexicons==null) return;
        for(Lexicon lexicon:lexicons){
            classificationApi.create(lexicon);
        }
    }

    public void loadLexiconExpressions(Collection<LexiconExpression> lexiconExpressions) throws ConditionEngineException {
        if(lexiconExpressions==null) return;
        for(LexiconExpression lexiconExpression:lexiconExpressions){
            classificationApi.create(lexiconExpression);
        }
    }

    public void loadPolicies(Collection<Policy> policies) throws ConditionEngineException {
        if(policies==null) return;
        for(Policy policy:policies){
            policyApi.create(policy);
        }
    }

    public void loadPolicyTypes(Collection<PolicyType> policyTypes) throws ConditionEngineException {
        if(policyTypes==null) return;
        for(PolicyType policyType:policyTypes){
            policyApi.create(policyType);
        }
    }
}
