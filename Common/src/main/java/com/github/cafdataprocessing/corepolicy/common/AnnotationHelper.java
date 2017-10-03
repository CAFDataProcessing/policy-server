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
package com.github.cafdataprocessing.corepolicy.common;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.cafdataprocessing.corepolicy.common.dto.conditions.Condition;
import com.google.common.base.Strings;
import com.github.cafdataprocessing.corepolicy.common.dto.conditions.ConditionType;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.security.InvalidParameterException;
import java.util.*;

/**
 * Helper methods for Annotations
 */
public class AnnotationHelper {

    /***
     *  search through all the fields, on the given class for an annotation of type FilterName which
     *  has a name which equals the given fieldName param.
     * @param filterName
     * @param classToSearch
     * @return field name inside the class which has then specified FilterName.
     * @throws IllegalArgumentException
     */
    public static List<Field> getPropertyForFilterName( String filterName, Class<?> classToSearch ) {

        List<Field> allFields = getPropertyForFilterName(filterName, classToSearch, FilterName.class );
        if (allFields.size() == 0) {
            throw new RuntimeException("Could not determine the real property to use for filter supplied.");
        }

        return allFields;
    }

    public static List<Field> getPropertyForSortName(String sortName, Class<?> classToSearch) {
        List<Field> allFields = getPropertyForFilterName(sortName, classToSearch, SortName.class);
        if (allFields.size() == 0) {
            throw new RuntimeException("Could not determine the real property to use for sort supplied.");
        }

        return allFields;
    }

    public static boolean isWrappedItemField(Class<?> requestClassType, Field field) {
        String wrapperName = getAnnotationValueFromField( field, HibernateWrappedItem.class );

        if ( Strings.isNullOrEmpty(wrapperName))
        {
            return false;
        }

        boolean fieldNameMatches = wrapperName.equalsIgnoreCase( field.getName() );

        // now some users, may wish to confirm that the field, is on the requested class, and not just any old class.
        if ( requestClassType == null ){
            return fieldNameMatches;
        }

        return field.getDeclaringClass() == requestClassType;
    }

    /**
     * the get property for filter name, is better named, get property for Annotation name, as it matches
     * a given name, against an given annotation type, on a given class.
     * @param annotationName
     * @param classToSearch
     * @param annotationClass
     * @param <T>
     * @return
     */
    public static <T extends Annotation> List<Field> getPropertyForFilterName(String annotationName, Class<?> classToSearch, Class<T> annotationClass) {
        List<Field> allFields = new ArrayList<>();
        return getPropertyForFilterName(annotationName, classToSearch, annotationClass, allFields );
    }

    private static <T extends Annotation> List<Field> getPropertyForFilterName( String annotationName, Class<?> classToSearch, Class<T> annotationClass, List<Field> sourceList ) {

        if ( sourceList == null ) {
            throw new InvalidParameterException("Fields list is null");
        }

        // Now map this fieldName from the json to the FilterName on the object so we can get the actual
        // property on the object. ( used by hibernate. )
        if (Strings.isNullOrEmpty(annotationName)) {
            throw new IllegalArgumentException("annotationName");
        }

        // This method is recursive, if we happen to have a relational object type where the object and fields are seperated by .
        // e.g. collection_sequence_entries.collection_ids
        String [] annotationParts = annotationName.split("\\.");

        return getPropertyForFilterName(annotationParts, classToSearch, annotationClass, sourceList );
    }

    private static <T extends Annotation> List<Field> getPropertyForFilterName(  String[] annotationParts, Class<?> classToSearch, Class<T> annotationClass, List<Field> fieldListSoFar ) {

        if ( annotationParts.length == 0 ) {
            throw new InvalidParameterException("Invalid annotation name given.");
        }

        String annotationPart = annotationParts[0];

        if ( Strings.isNullOrEmpty(annotationPart) ) {
            throw new InvalidParameterException("annotationName");
        }

        String remainingPropName = String.join(".", annotationParts);

        for (Field field : classToSearch.getFields()) {
            T[] annotations = field.getAnnotationsByType(annotationClass);

            // filter names can only ever by 0 or 1, I dont ever give more than 1 name to a property.
            // Now on a class that could be different.
            if (annotations.length == 0)
                continue;

            if ( annotations.length > 1 )
            {
                throw new InvalidParameterException("AnnotationType has multiple annoations for given type.");
            }

            String filterNameValue = getNameFromAnnotationInstance(annotations[0]);

            // otherwise, check if the FilterName annotation given is the one we expected.
            if (!annotationPart.equalsIgnoreCase(filterNameValue)) {

                // just before we give up, I have added additional functionality to map Public dto objects which may be multi part
                // e.g. Collection.Condition.Id to internal object such as Collection$Item.conditionId.
                // as such I may have a multiple part name on a single property. Check for this now.
                if ( ( annotationParts.length > 1 ) && filterNameValue.equalsIgnoreCase( remainingPropName ) ){

                    // got a match on this object, return it, if we change to allowing partial matches
                    // dont bail here, take off the annotationParts where we have got to and keep going!
                    fieldListSoFar.add(field);
                    return fieldListSoFar;
                }

                continue;
            }

            fieldListSoFar.add(field);

            // we have a match for the fieldName we are currently using, either:
            // a) add the field, and return if its a single part annotation
            // b) add the field, and continue over the remaining parts of the annoation.
            if ( annotationParts.length == 1 ){
                return fieldListSoFar;
            }

            // b) recurse into the object we have found, and try for the next property annotation in the list.
            String [] restOfAnnotationParts = Arrays.copyOfRange(annotationParts, 1, annotationParts.length);

            Class<?> searchThisClassType = getRealClassType(field);

            return getPropertyForFilterName( restOfAnnotationParts, searchThisClassType, annotationClass, fieldListSoFar );
        }

        // if we get to here, we didn't find a matching FilterName on this object to the field specified.
        throw new IllegalArgumentException("Unable to find an annotation with value: " + annotationPart + " on class: " + classToSearch.getName());
    }

    public static <T extends Annotation> String getAnnotationValueFromField( Field field, Class<T> annotationClass )
    {
        T[] annotations = field.getAnnotationsByType(annotationClass);

        // filter names can only ever by 0 or 1, I dont ever give more than 1 name to a property.
        // Now on a class that could be different.
        if (annotations.length == 0)
            return null;

        if ( annotations.length > 1 )
        {
            throw new InvalidParameterException("AnnotationType has multiple annoations for given type.");
        }

        String filterNameValue = getNameFromAnnotationInstance(annotations[0]);

        // we could check for a match here, but leaving to higher level....
        return filterNameValue;
    }

    private static String getNameFromAnnotationInstance( Annotation annotation ) {

        if (annotation instanceof FilterName) {
            return ((FilterName) annotation).value();
        } else if (annotation instanceof SortName) {
            return ((SortName) annotation).value();
        } else if (annotation instanceof HibernateWrappedItem) {
            return ((HibernateWrappedItem) annotation).value();
        }
        return annotation.toString();
    }

    /***
     * Helper method, to return the real class type of a Field.
     * @param field
     * @return
     */
    public static Class<?> getRealClassType(Field field) {

        // ensure that for any of the list / array types, we return the internal type, not the list as the type.
        if (( field.getType() == List.class ) ||
                ( field.getType() == Set.class ) ||
                ( field.getType() == Array.class ) ||
            ( field.getType() == Collection.class )) {
            ParameterizedType stringListType = (ParameterizedType) field.getGenericType();
            return (Class<?>) stringListType.getActualTypeArguments()[0];
        }


        return field.getType();
    }

    public static Class<?> getRealRequestClassType( Class<?> requestClassType, ObjectNode objectNode ){

        if ( requestClassType == Condition.class ) {
            // we know this class has a discriminator field called "type"
            // if its present, use its given concrete class instead.

            // Now we know its a valid filter - get the value for the criterion.
            if (objectNode.hasNonNull(ApiStrings.Conditions.Arguments.TYPE)) {
                JsonNode node = objectNode.get(ApiStrings.Conditions.Arguments.TYPE);

                return ConditionType.getConditionClass(node.textValue());
            }
        }

        // default is just to use the source request class.
        return requestClassType;
    }
}
