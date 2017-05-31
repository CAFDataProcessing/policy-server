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
package com.github.cafdataprocessing.corepolicy.policy.MetadataPolicy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.cafdataprocessing.corepolicy.PolicyHandler;
import com.github.cafdataprocessing.corepolicy.ProcessingAction;
import com.github.cafdataprocessing.corepolicy.common.Document;
import com.github.cafdataprocessing.corepolicy.common.dto.Policy;
import com.github.cafdataprocessing.corepolicy.common.exceptions.BackEndRequestFailedCpeException;
import com.github.cafdataprocessing.corepolicy.domainModels.FieldAction;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collectors;

/**
 *
 */
public class MetadataPolicyHandler implements PolicyHandler {

    private final long metadataPolicyTypeId;

    public MetadataPolicyHandler(long metadataPolicyTypeId){
        this.metadataPolicyTypeId = metadataPolicyTypeId;
    }

    @Override
    public long getPolicyTypeId() {
        return metadataPolicyTypeId;
    }

    @Override
    public Collection<Policy> resolve(Document document, Collection<Policy> policies) {
        if (policies == null || policies.isEmpty()) {
            return new HashSet<>();
        }

        if (!policies.stream().allMatch(u -> policies.stream().findFirst().get().typeId.equals(u.typeId))) {
            throw new BackEndRequestFailedCpeException(new Exception("The policies are not of the same type"));
        }

        //compare policy priority values, and select the highest priority policies
        return policies.stream().collect(Collectors.groupingBy(u -> u.priority)).entrySet()
                .stream().max((x, y) -> Long.compare(x.getKey(), y.getKey())).get().getValue();
    }

    @Override
    public ProcessingAction handle(Document document, Policy policy, Long collectionSequenceId) {
        MetadataPolicy metadataPolicy = getMetadataPolicy(policy);
        Collection<FieldAction> fieldActions = metadataPolicy.getFieldActions();
        applyFieldActions(document, fieldActions);
        return ProcessingAction.CONTINUE_PROCESSING;
    }

    private MetadataPolicy getMetadataPolicy(Policy policy) {
        MetadataPolicy metadataPolicy;
        try {
            ObjectMapper mapper = new ObjectMapper();
            metadataPolicy = mapper.treeToValue(policy.details, MetadataPolicy.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return metadataPolicy;
    }

    private void applyFieldActions(Document document, Collection<FieldAction> fieldActions) {
        for (FieldAction fieldAction : fieldActions) {

            FieldAction.Action action = fieldAction.getAction();

            // N.B. Weird javac behaviour whereby if a switch is used below 2 ProcessDocument class files are
            // generated, once called ProcessDocument$1.class and once ProcessDocument.class which stops us from
            // correctly running javah on this class.
            if ( action == FieldAction.Action.SET_FIELD_VALUE ){
                document.getMetadata().get(fieldAction.getFieldName()).clear();
                document.getMetadata().put(fieldAction.getFieldName(), fieldAction.getFieldValue());
            }
            else if ( action == FieldAction.Action.ADD_FIELD_VALUE ) {
                document.getMetadata().put(fieldAction.getFieldName(), fieldAction.getFieldValue());
            }
        }
    }
}
