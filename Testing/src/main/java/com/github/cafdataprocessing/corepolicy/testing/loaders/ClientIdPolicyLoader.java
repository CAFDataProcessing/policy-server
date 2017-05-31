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
package com.github.cafdataprocessing.corepolicy.testing.loaders;

import com.github.cafdataprocessing.corepolicy.common.ClassificationApi;
import com.github.cafdataprocessing.corepolicy.common.PolicyApi;
import com.github.cafdataprocessing.corepolicy.common.dto.*;
import com.github.cafdataprocessing.corepolicy.common.exceptions.ConditionEngineException;
import com.github.cafdataprocessing.corepolicy.common.dto.conditions.Condition;
import com.github.cafdataprocessing.corepolicy.testing.models.PolicyEnvironment;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 */
public class ClientIdPolicyLoader extends PolicyEnvironmentLoader {

    public ClientIdPolicyLoader(PolicyApi policyApi, ClassificationApi classificationApi)
    {
        super(policyApi, classificationApi);
    }

    protected IdManager idManager;
    public void setIdManager(IdManager idManager) {
        this.idManager = idManager;
    }

    public void load(PolicyEnvironment policyEnvironment) throws Exception{
        if(policyEnvironment==null){
            return;
        }
        loadLexicons(policyEnvironment.lexicons);
        loadLexiconExpressions(policyEnvironment.lexiconExpressions);
        loadCollectionSequences(policyEnvironment.collectionSequences);
        loadCollections(policyEnvironment.documentCollections);
        loadConditions(policyEnvironment.conditions);
//        loadCollectionSequenceEntries(policyEnvironment.collectionSequenceEntries);

        loadPolicyTypes(policyEnvironment.policyTypes);
        loadPolicies(policyEnvironment.policies);
    }

    @Override
    public void loadCollectionSequences(Collection<CollectionSequence> collectionSequences) throws Exception {
        if(collectionSequences==null) return;

        List<Long> clientIds = collectionSequences.stream().map(c -> c.id).collect(Collectors.toList());
        collectionSequences.stream().forEach(c -> {
            c.id = null;
            c.defaultCollectionId = idManager.resolveClientId(c.defaultCollectionId);
        });

        super.loadCollectionSequences(collectionSequences);

        int index=0;
        for(CollectionSequence collectionSequence:collectionSequences){
            idManager.registerServerId(clientIds.get(index), collectionSequence.id);
            collectionSequence.id = (clientIds.get(index));
            index++;
        }
    }

    @Override
    public void loadCollections(Collection<DocumentCollection> documentCollections) throws ConditionEngineException {
//        if(documentCollections==null) return;
//
//        List<Long> clientIds = documentCollections.stream().map(c -> c.getId()).collect(Collectors.toList());
//        documentCollections.stream().forEach(c -> {
//            c.id = null;
//            c. (idManager.resolveClientId(c.getConditionId()));
//        });
//
//        super.loadCollections(documentCollections);
//
//        int index=0;
//        for(DocumentCollection documentCollection:documentCollections){
//            idManager.registerServerId(clientIds.get(index), documentCollection.getId());
//            documentCollection.setId(clientIds.get(index));
//            index++;
//        }
    }

    @Override
    public void loadConditions(Collection<Condition> conditions) throws ConditionEngineException {
//        if(conditions==null) return;
//
//        List<Long> clientIds = conditions.stream().map(c -> c.getId()).collect(Collectors.toList());
//        conditions.stream().forEach(c -> {
//            c.setId(null);
//            c.setCollectionId(idManager.resolveClientId(c.getCollectionId()));
//            c.setBooleanConditionId(idManager.resolveClientId(c.getBooleanConditionId()));
//            c.setNotConditionId(idManager.resolveClientId(c.getNotConditionId()));
//        });
//
//        super.loadConditions(conditions);
//
//        int index=0;
//        for(Condition condition:conditions){
//            idManager.registerServerId(clientIds.get(index), condition.id);
//            condition.id = clientIds.get(index);
//            index++;
//        }
    }

    @Override
    public void loadLexicons(Collection<Lexicon> lexicons) throws ConditionEngineException {
        if(lexicons==null) return;

        List<Long> clientIds = lexicons.stream().map(c -> c.id).collect(Collectors.toList());
        lexicons.stream().forEach(c -> c.id = null);

        super.loadLexicons(lexicons);

        int index=0;
        for(Lexicon lexicon:lexicons){
            idManager.registerServerId(clientIds.get(index), lexicon.id);
            lexicon.id = clientIds.get(index);
            index++;
        }
    }

    @Override
    public void loadLexiconExpressions(Collection<LexiconExpression> lexiconExpressions) throws ConditionEngineException {
        if(lexiconExpressions==null) return;

        List<Long> clientIds = lexiconExpressions.stream().map(c -> c.id).collect(Collectors.toList());
        lexiconExpressions.stream().forEach(c -> {
            c.id = null;
            c.lexiconId = idManager.resolveClientId(c.lexiconId);
        });

        super.loadLexiconExpressions(lexiconExpressions);

        int index=0;
        for(LexiconExpression lexiconExpression:lexiconExpressions){
            idManager.registerServerId(clientIds.get(index), lexiconExpression.id);
            lexiconExpression.id = clientIds.get(index);
            index++;
        }
    }

    @Override
    public void loadPolicies(Collection<Policy> policies) throws ConditionEngineException {
        if(policies==null) return;

        List<Long> clientIds = policies.stream().map(c -> c.id).collect(Collectors.toList());
        policies.stream().forEach(c -> {
            c.id = null;
            c.typeId = idManager.resolveClientId(c.typeId);
        });

        super.loadPolicies(policies);

        int index=0;
        for(Policy policy:policies){
            idManager.registerServerId(clientIds.get(index), policy.id);
            policy.id = clientIds.get(index);
            index++;
        }
    }

    @Override
    public void loadPolicyTypes(Collection<PolicyType> policyTypes) throws ConditionEngineException {
        if(policyTypes==null) return;

        List<Long> clientIds = policyTypes.stream().map(c -> c.id).collect(Collectors.toList());
        policyTypes.stream().forEach(c -> c.id = null);

        super.loadPolicyTypes(policyTypes);

        int index=0;
        for(PolicyType policyType:policyTypes){
            idManager.registerServerId(clientIds.get(index), policyType.id);
            policyType.id = clientIds.get(index);
            index++;
        }
    }
}
