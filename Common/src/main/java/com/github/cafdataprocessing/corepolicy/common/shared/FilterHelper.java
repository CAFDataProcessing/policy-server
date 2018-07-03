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
package com.github.cafdataprocessing.corepolicy.common.shared;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.github.cafdataprocessing.corepolicy.common.AnnotationHelper;
import com.github.cafdataprocessing.corepolicy.common.dto.conditions.ConditionType;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Utility class to help obtain the real values of JsonNodes / ObjectNodes / ArrayNodes which are contained within our Filter object.
 */
public class FilterHelper {

    /***
     *  we cannot rely on the actual node type, as this changes based on the actual number value transmitted.
     *  So if you create a JsonNode with Long, it internally requests if the range is in in the Int range, an
     *  creates it of type IntNode and not Long Node. as such we need to use the real property type of the Field.
     * @param node
     * @param actualProperty
     * @return
     */
    // package local for unit tests.
    public static Object getValueFromNode(JsonNode node, Field actualProperty ) {

        // for an array / list type, we need to get the actual internal real class type and use it
        // to dictate how we return the value.
        if ( isFieldRepresentedByArrayType( actualProperty ) ){

            // Find out the real type of this collection...
            return getObjectArrayOfRealType(node, actualProperty);
        }

        return getNodeValueByType(node, actualProperty.getType());
    }

    /**
     * Helper method which turns the json node value(s) into a string based upon
     * the real representing property type.
     * @param node
     * @param actualProperty
     * @return
     */
    public static String getValueFromNodeAsString(JsonNode node, Field actualProperty ){

        // get value as object, and turn to string, comma seperated if an array type.
        Object realValue = FilterHelper.getValueFromNode(node, actualProperty);

        StringBuilder sb = new StringBuilder();

        if ( realValue instanceof Object[])
        {
            Object[] array = ((Object[]) realValue);
            for( int index=0; index < array.length; index++) {
                Object subValue = array[index];
                if (index > 0) {
                    sb.append(",");
                }
                sb.append(subValue);
            }
        }
        else
        {
            sb.append(realValue);
        }

        return sb.toString();
    }

    public static boolean isFieldRepresentedByArrayType( Field actualProperty ){
        if(( actualProperty.getType() == Array.class )
                || ( actualProperty.getType() == List.class )
                || ( actualProperty.getType() == Collection.class )
                || ( actualProperty.getType() == Set.class )){
            return true;
        }

        return false;
    }


    // Helper method where the user can supply the value object, and based
    // on its original type we return a single entity.
    public static Long getSingleValue(JsonNode node, Object value) {

        // For now we only support having a single related item, not a list of ids
        if ( node.isArray() )
        {
            // if we have an array, it may be of only one item, if so let
            // it through
            if ( value instanceof Object[] ) {
                if (((Object[]) value).length > 1)
                {
                    throw new UnsupportedOperationException("Unsupported requested for an array of ids.");
                }

                return (Long)(((Object[]) value)[0]);
            }
        }

        return (Long)value;
    }

    // Get single value from the node passed in.
    // If the item is an array, ensure it only has 0 or 1 entries for this method
    public static Long getSingleValue(JsonNode node) {

        if ( !node.isArray() ) {
            return node.longValue();
        }

        ArrayNode arrayNode = (ArrayNode) node;

        if ( arrayNode.size() > 1 )
        {
            throw new InvalidParameterException("Array node cannot contain more than 1 entry.");
        }

        if ( arrayNode.size() == 0 )
        {
            // return null, there is no value, 0 size array.
            return null;
        }

        JsonNode subNode = arrayNode.get(0);
        return (Long) getNodeValueByType(subNode, Long.class);
    }

    public static Object getNodeValueByType(JsonNode node, Class<?> requestType ) {

        if ((requestType == Long.class)  || (requestType == long.class) ){
            return node.numberValue().longValue();
        }

        if (requestType == Integer.class) {
            return node.numberValue().intValue();
        }

        if ((requestType == Double.class) || (requestType == double.class) ) {
            return node.numberValue().doubleValue();
        }

        if ((requestType == Boolean.class) || ( requestType == boolean.class ) ){
            return node.booleanValue();
        }

        if (requestType == String.class) {
            return node.textValue();
        }

        if ( requestType == ConditionType.class ){
            // Map the text into the real conditiontype, its case sensitive when used
            // as a discriminator in hibernate.
            return ConditionType.forValue(node.textValue()).toString();
        }

        // fallback - return all other type as default type - string
        // if this isn't good enough either add to the list above or throw
        // here until we put every type explicitally in this list!


        return node.textValue();
    }

    public static Object[] getObjectArrayOfRealType(JsonNode node, Field actualProperty) {

        Class<?> realClass = AnnotationHelper.getRealClassType(actualProperty);
        return toObjectArray( node, actualProperty, realClass );
    }

    public static <T> Object[] toObjectArray( JsonNode node, Field actualProperty, Class<T> realClass ) {

        // We need to turn the node List into a typed List depending on the real class type.
        Collection<T> realArray = new ArrayList<>();

        // decide whether to add a single item by real type or to cycle over them.
        if (!node.isArray()) {
            // we have a single value, just grab by our real type, and this is the list..
            T value = (T) getNodeValueByType(node, realClass);
            realArray.add(value);
            return realArray.toArray();
        }

        ArrayNode arrayNode = (ArrayNode) node;

        for (int index = 0; index < arrayNode.size(); index++) {
            // get each sub value in turn, and add to our type return list.
            JsonNode subNode = arrayNode.get(index);
            T subValue = (T) getNodeValueByType(subNode, realClass);
            realArray.add(subValue);
        }

        return realArray.toArray();
    }

}
