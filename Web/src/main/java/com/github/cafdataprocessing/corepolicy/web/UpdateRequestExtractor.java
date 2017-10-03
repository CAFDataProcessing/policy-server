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
package com.github.cafdataprocessing.corepolicy.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.cafdataprocessing.corepolicy.common.dto.DtoBase;
import com.github.cafdataprocessing.corepolicy.common.dto.UpdateBehaviourType;
import com.github.cafdataprocessing.corepolicy.common.dtoweb.UpdateRequest;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author getty
 */
public class UpdateRequestExtractor extends BaseExtractor<UpdateRequest> {

    @Override
    protected UpdateRequest convert(JsonNode node) {

        JsonNode typeNode = node.get("type");
        if (typeNode == null) {
            throw new RuntimeException("Type is required.");
        }

        UpdateRequest updateRequest = new UpdateRequest();
        JsonNode updateBehaviourNode = node.get("update_behaviour");
        UpdateBehaviourType updateBehaviour;

        if (updateBehaviourNode != null) {
            try {
                updateBehaviour = mapper.treeToValue(updateBehaviourNode, UpdateBehaviourType.class);
                updateRequest.updateBehaviour = updateBehaviour;
                // Take out of the node, our property which may be called UpdateBehaviour and extract the rest of the properties
                // into a normal DtoBase object.
                ((ObjectNode) node).remove("update_behaviour");
            } catch (JsonProcessingException ex) {
                Logger.getLogger(UpdateRequestExtractor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }


        try {
            DtoBase rootObject = mapper.treeToValue(node, DtoBase.class);
            updateRequest.objectToUpdate = rootObject;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return updateRequest;
    }

}
