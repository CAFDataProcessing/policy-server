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
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.cafdataprocessing.corepolicy.common.dto.CollectionSequence;
import com.github.cafdataprocessing.corepolicy.common.dto.Filter;
import com.github.cafdataprocessing.corepolicy.common.dto.conditions.Condition;
import com.github.cafdataprocessing.corepolicy.common.dto.conditions.ConditionType;
import com.github.cafdataprocessing.corepolicy.common.shared.FilterHelper;
import junit.framework.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Tests for the AnnotationHelper class
 */
public class AnnotationHelperTest {

    private final static String priorityColumn = ApiStrings.Policy.Arguments.PRIORITY;
    private final static String policyTypeColumn = ApiStrings.Policy.Arguments.POLICY_TYPE;

    private final static String collectionSequenceEntries_collection_ids_relational_column =
            ApiStrings.CollectionSequences.Arguments.COLLECTION_SEQUENCE_ENTRIES + "." + ApiStrings.CollectionSequenceEntries.Arguments.COLLECTION_IDS;

    private final static long policyTypeTestID = 5;

    private final static Logger logger = LoggerFactory.getLogger(AnnotationHelperTest.class);


    @Test
    public void testAbilityToFindMatchingSupportFilterNameByAnnotation() throws Exception {

        ObjectNode newNode = new ObjectNode(JsonNodeFactory.instance);

        // e.g. retrieveCondition( Filter { "type"="lexicon" and "value"=5 } )
        newNode.put(ApiStrings.Conditions.Arguments.TYPE, ConditionType.LEXICON.toValue());
        newNode.put(ApiStrings.Conditions.Arguments.VALUE, policyTypeTestID);

        Filter filter = Filter.create(newNode);

        Assert.assertEquals("Must have 2 nodes.", 2, newNode.size());
        Assert.assertEquals("Filter must match object node supplied.", newNode.size(), filter.size());

        System.out.println("Attempting to get compiled in annotations.");

        Iterator<String> fieldNames = filter.fieldNames();

        Class<?> realRequestedObjectType = AnnotationHelper.getRealRequestClassType(Condition.class, filter);

        while ( fieldNames.hasNext() ) {
            String fieldName = fieldNames.next();

            // Every Filter field must have a @FilterName annotation, if not we dont deem it to be
            // a valid filter - so it throws.
            List<Field> propFields = AnnotationHelper.getPropertyForFilterName(fieldName, realRequestedObjectType);

            Field actualProperty = propFields.stream().findFirst().get();

            String propertyName = actualProperty.getName();

            Assert.assertTrue(propertyName.length() > 0 );

            // Now we know its a valid filter - get the value for the criterion.
            JsonNode node = filter.get(fieldName);

            // check if its null
            if (node.getNodeType() == JsonNodeType.NULL) {
                // we dont have any valid value here - throw as this is invalid.
                throw new IllegalArgumentException("retrievePage on Field: " + fieldName);
            }

        }
    }
    @Test
    public void testAbilityToDecodeRelationalObject() throws Exception {

        // e.g. retrieveCondition( Filter { "type"="lexicon" and "value"=5 } )
        // retrieveCollectionSequence ( Filter { COLLECTION_SEQUENCE_ENTRIES + "." + COLLECTION_IDS );

        Collection<Long> ids = Arrays.asList(policyTypeTestID, policyTypeTestID + 1);
        Filter filter = Filter.create(collectionSequenceEntries_collection_ids_relational_column, ids);

        Assert.assertTrue( filter.getNodeType()==JsonNodeType.OBJECT );

        // ensure we can get the single object node entry with this column name!
        JsonNode node = filter.get(collectionSequenceEntries_collection_ids_relational_column);

        // only has one entry so is an object node as well.
        List<Field>  propFields = AnnotationHelper.getPropertyForFilterName(collectionSequenceEntries_collection_ids_relational_column, CollectionSequence.class);

        Assert.assertTrue("Property fields must have 2 entries.", propFields.size() == 2);

        Field actualProperty = (Field)propFields.toArray()[propFields.size() - 1 ];

        String propertyName = actualProperty.getName();

        // property name must be the actual property on the collection sequence entry called collectionIds for not json / filtername collection_ids.

        Assert.assertTrue(propertyName.length() > 0);

        // Must be actual property name: CollectionSequenceEntry.collectionIds
        Assert.assertEquals("Must be named: ", "collectionIds", propertyName);

        Assert.assertTrue( node.getNodeType() == JsonNodeType.ARRAY );

        // Find out the real type of this collection...
        Class<?> realClass = AnnotationHelper.getRealClassType( actualProperty );

        // Ensure the real type inside the List class, is of type Long.
        Assert.assertEquals("Real class type must of equal", Long.class, realClass );

        // Finally can we get the real types array, as the initial collection type.
        // We return it as an object[] as this is what is used by the restrictions in other classes.
        Object realValue = FilterHelper.getValueFromNode(node, actualProperty);

        Assert.assertNotNull( realValue );

        // This object must be of type Long[] in reality.
        Object[] arrayValue = (Object[])realValue;

        Assert.assertEquals("object array must contain same number of elements", ids.size(), arrayValue.length );

        Object[] sourceArray = ids.toArray();

        for ( int index = 0; index < ids.size(); index++ ) {
            Assert.assertEquals("Values must be equal", sourceArray[index], arrayValue[index]);
        }
    }





}
