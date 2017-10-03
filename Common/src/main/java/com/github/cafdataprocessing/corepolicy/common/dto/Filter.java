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
package com.github.cafdataprocessing.corepolicy.common.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Collection;

/**
 * This filter object just contains a json blob at present
 * which is what we are going to Filter our paged results on.
 */
public class Filter extends ObjectNode {

    private Filter() {
        super(JsonNodeFactory.instance);
    }

    public Filter( JsonNodeFactory nf ){
        super(nf);
    }

    public static Filter create( ObjectNode node ) {

        try {
            // ensure our jsonnode is an object - and doesnt' have multiple children at this stage!
            if (!node.isObject())
                throw new RuntimeException("Node specified is not an object");

            // Copy all the fields onto our Filter node.
            Filter filter = new Filter();
            filter.putAll(node);
            return filter;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static Filter create( JsonNode node ) {
        try {
            // ensure our jsonnode is an object - and doesnt' have multiple children at this stage!
            if (node.isObject() ) {
                return create((ObjectNode) node);
            }

            throw new RuntimeException("Node specified is not a supported JsonNode type.");
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }


    /***
     * Utility method to create a new Filter object with an ObjectNode root
     * with the given name / value pair.
     * @param nodeName
     * @param value
     * @return
     */
    public static Filter create( String nodeName, Object value ) {
        ObjectNode node = new ObjectNode(JsonNodeFactory.instance);

        // Increase support types as you need them.
        if (value instanceof String) {
            node.put(nodeName, (String)value);
        }
        else if ( value instanceof Boolean){
            node.put(nodeName, (Boolean)value);
        }
        else if ( value instanceof Long ){
            node.put(nodeName, (Long)value);
        }
        else
        {
            throw new UnsupportedOperationException("Filter creation doesn't accept type: " + value.toString() + " at present.");
        }

        return Filter.create(node);
    }

    /***
     * Handy utility method for creating an ObjectNode with a named array inside it.
     * @param arrayName
     * @param list
     * @return
     */
    public static Filter create( String arrayName, Collection<Long> list) {

        Filter filter = new Filter();

        ArrayNode arrayNode = filter.putArray(arrayName);

        for( Long item : list )
        {
            arrayNode.add( item );
        }

        return filter;
    }

    public static JsonNode getJsonNode(Filter filter, String nodeName){
        // by default dont allow nulls
        return getJsonNode(filter, nodeName, false );
    }

    public static JsonNode getJsonNode(Filter filter, String nodeName, boolean bAllowNull) {
        JsonNode node = filter.get(nodeName);

        // Now this node could in fact by null, quick check for this.
        if ( (!bAllowNull ) && node.isNull() ) {
            throw new IllegalArgumentException(nodeName);
        }
        return node;
    }
}
