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
package com.github.cafdataprocessing.corepolicy.common.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * ObjectNode that sorts nodes alphabetically
 */
public class Sort extends ObjectNode {

    private Sort() {
        super(JsonNodeFactory.instance);
    }

    public Sort(JsonNodeFactory nc) {
        super(nc);
    }

    private static Sort create(ObjectNode node) {
        try {
            // ensure our jsonnode is an object - and doesn't have multiple children at this stage!
            if (!node.isObject())
                throw new RuntimeException("Node specified is not an object");

            // Copy all the fields onto our Filter node.
            Sort sort = new Sort();
            sort.setAll(node);
            return sort;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static Sort create(JsonNode node) {
        if(node.isNull() || node.textValue() == "null"){
            return null;
        }
        try {
            // ensure our jsonnode is an object
            if (node.isObject()) {
                return create((ObjectNode) node);
            }

            throw new RuntimeException("Node specified is not a supported JsonNode type.");
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static Sort create(String fieldName, Object ascOrder) {
        ObjectNode node = new ObjectNode(JsonNodeFactory.instance);
        //Only allow boolean type for value. This specifies whether to sort ascending or descending
        if (ascOrder instanceof Boolean) {
            node.put(fieldName, (Boolean) ascOrder);
        } else {
            throw new UnsupportedOperationException("Sort creation doesn't accept type: " + ascOrder.toString() + " at present.");
        }

        return Sort.create(node);
    }


    public static JsonNode getJsonNode(Sort sort, String nodeName) {
        // by default dont allow nulls
        return getJsonNode(sort, nodeName, false);
    }

    public static JsonNode getJsonNode(Sort sort, String nodeName, boolean bAllowNull) {
        JsonNode node = sort.get(nodeName);

        // Now this node could in fact by null, quick check for this.
        if ((!bAllowNull) && node.isNull()) {
            throw new IllegalArgumentException(nodeName);
        }
        return node;
    }
}
