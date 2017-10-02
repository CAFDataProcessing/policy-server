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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.cafdataprocessing.corepolicy.common.dto.Filter;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import static com.github.cafdataprocessing.corepolicy.TestHelper.shouldThrow;

/**
 * Unit tests for Filter class
 */
@RunWith(MockitoJUnitRunner.class)
public class FilterTest {

    private final static String priorityColumn = ApiStrings.Policy.Arguments.PRIORITY;
    private final static String policyTypeColumn = ApiStrings.Policy.Arguments.POLICY_TYPE;
    private final static String descColumn = ApiStrings.Policy.Arguments.DESCRIPTION;
    private final static String collectionSequenceEntries_collection_ids_relational_column =
            ApiStrings.CollectionSequences.Arguments.COLLECTION_SEQUENCE_ENTRIES + "." + ApiStrings.CollectionSequenceEntries.Arguments.COLLECTION_IDS;

    private final static String testString = "testStringWith special char, Citroên";

    private final static long policyTypeTestID = 5;
    private final static ObjectMapper mapper = new ObjectMapper();
    private final static Logger logger = LoggerFactory.getLogger(FilterTest.class);
    private final static ObjectNode objectNodePolicyTypeFilter = new ObjectNode(JsonNodeFactory.instance);

    // setup some base members
    @Before
    public void setup(){

        objectNodePolicyTypeFilter.put(policyTypeColumn, policyTypeTestID);

        logger.debug("Created json filter blob: " + objectNodePolicyTypeFilter.toString());
    }

    @Test
    public void testFilterCreation() throws Exception {

        // test creation from the objectnode.
        {
            Filter filter = Filter.create(objectNodePolicyTypeFilter);

            Assert.assertEquals("Node must have 1 entry", 1, filter.size());
            Assert.assertTrue("column requested must be present", filter.has(policyTypeColumn));
            Assert.assertNotNull("value must be present for column", filter.get(policyTypeColumn));
            Assert.assertEquals("Value of field must be equal", policyTypeTestID, filter.get(policyTypeColumn).longValue());
        }

        {
            // Now test creation from a json node.
            JsonNode jnode = mapper.readTree(objectNodePolicyTypeFilter.toString());

            // ensure our jsonnode is an object - and doesnt' have multiple children at this stage!
            if (!jnode.isObject())
                throw new Exception("not an object");

            Filter filter = Filter.create(jnode);

            Assert.assertEquals("Node must have 1 entry", 1, filter.size());
            Assert.assertTrue("column requested must be present", filter.has(policyTypeColumn));
            Assert.assertNotNull("value must be present for column", filter.get(policyTypeColumn));
            Assert.assertEquals("Value of field must be equal", policyTypeTestID, filter.get(policyTypeColumn).longValue());
        }

        {
            // Now test creation from the utility method
            Filter filter = Filter.create(policyTypeColumn, policyTypeTestID );

            Assert.assertEquals("Node must have 1 entry", 1, filter.size());
            Assert.assertTrue("column requested must be present", filter.has(policyTypeColumn));
            Assert.assertNotNull("value must be present for column", filter.get(policyTypeColumn));
            Assert.assertEquals("Value of field must be equal", policyTypeTestID, filter.get(policyTypeColumn).longValue());
        }

        {
            // Now test creation from the utility method for boolean
            Filter filter = Filter.create(ApiStrings.Conditions.Arguments.IS_FRAGMENT, true);

            Assert.assertEquals("Node must have 1 entry", 1, filter.size());
            Assert.assertTrue("column requested must be present", filter.has(ApiStrings.Conditions.Arguments.IS_FRAGMENT));
            Assert.assertNotNull("value must be present for column", filter.get(ApiStrings.Conditions.Arguments.IS_FRAGMENT));
            Assert.assertEquals("Value of field must be equal", true, filter.get(ApiStrings.Conditions.Arguments.IS_FRAGMENT).booleanValue());
        }

        {
            // Test creation from utility method with unsupported type throws!
            shouldThrow( u -> Filter.create( policyTypeColumn, new Integer(1) ) );
        }

    }

    @Test
    public void testFilterPopulationAfterCreation() throws Exception {

        JsonNode jnode = mapper.readTree(objectNodePolicyTypeFilter.toString());

        // ensure our jsonnode is an object - and doesnt' have multiple children at this stage!
        if ( !jnode.isObject() )
            throw new Exception("not an object");

        // Populate the Filter object from the details of an ObjectNode.
        ObjectNode node = (ObjectNode)jnode;

        Filter filter = new Filter(JsonNodeFactory.instance);
        filter.putAll(node);

        Assert.assertEquals("Node must have 1 entry", 1, filter.size());
        Assert.assertTrue("column requested must be present", filter.has(policyTypeColumn));
        Assert.assertNotNull("value must be present for column", filter.get(policyTypeColumn));
        Assert.assertEquals("Value of field must be equal", policyTypeTestID, filter.get(policyTypeColumn).longValue());
    }

    @Test
    public void testFilterCreationWithMultipleNodes()
    {
        ObjectNode newNode = new ObjectNode(JsonNodeFactory.instance);

        newNode.put(policyTypeColumn, policyTypeTestID);
        newNode.put(priorityColumn, policyTypeTestID + 1);

        logger.debug("Created jsonNodes: " + newNode);

        Filter filter = Filter.create(newNode);

        Assert.assertEquals("Must have 2 nodes.", 2, newNode.size());
        Assert.assertEquals("Filter must match object node supplied.", newNode.size(), filter.size() );
    }

    @Test
    public void testFilterQueriesOnDifferentTypes()
    {
        ObjectNode newNode = new ObjectNode(JsonNodeFactory.instance);
        {
            // add a string test simple string case first.
            newNode.put(descColumn,testString );
            Filter filter = Filter.create(newNode);

            JsonNode node = filter.get(descColumn);

            Assert.assertEquals("PolicyType Description value should be null.", testString, node.asText());
        }

        {
            newNode = new ObjectNode(JsonNodeFactory.instance);

            // Now try to use null values.
            // add null long value
            newNode.put(policyTypeColumn, (JsonNode) null);

            // add a string
            newNode.put(descColumn, (JsonNode) null);

            logger.debug("Created jsonNodes: " + newNode);

            Filter filter = Filter.create(newNode);

            Assert.assertEquals("Must have 2 nodes.", 2, newNode.size());
            Assert.assertEquals("Filter must match object node supplied.", newNode.size(), filter.size());

            JsonNode node = filter.get( policyTypeColumn );

            // now the node value should be null.
            Assert.assertTrue("policytype ID node should be null", node.getNodeType() == JsonNodeType.NULL);
            Assert.assertNull("PolicyType ID value should be null.", node.numberValue());

            // beware a direct as aslong actually returns 0.....
            Assert.assertEquals("policytype id as long should be 0 for null value", 0, node.longValue());

            Assert.assertFalse("Desc column should be null", filter.hasNonNull(descColumn));

            node = filter.get( descColumn );
            Assert.assertTrue("policytype description node should be null", node.getNodeType() == JsonNodeType.NULL);

            Collection<Long> ids =  Arrays.asList(policyTypeTestID, policyTypeTestID + 1);
            filter = Filter.create(collectionSequenceEntries_collection_ids_relational_column,ids);
            node = filter.get(collectionSequenceEntries_collection_ids_relational_column);

            Assert.assertTrue(node.isArray());
            Assert.assertEquals( "Node must contain all ids from our list", node.size(),ids.size() );


        }
    }


    @Test
    public void testFilterByTokenName()
    {
        ObjectNode newNode = new ObjectNode(JsonNodeFactory.instance);

        newNode.put(policyTypeColumn, policyTypeTestID);
        newNode.put(priorityColumn, policyTypeTestID + 1);

        Filter filter = Filter.create(newNode);

        Assert.assertEquals("Must have 2 nodes.", 2, newNode.size());
        Assert.assertEquals("Filter must match object node supplied.", newNode.size(), filter.size());

        // We now should be able to list the token by the column name we put in it above.
        Iterator<String> fieldNames = filter.fieldNames();

        while ( fieldNames.hasNext() ) {
            String fieldName = fieldNames.next();

            Assert.assertTrue("Field name: " + fieldName + " should be in the fieldnames list.", fieldName.contains(policyTypeColumn) || fieldName.contains(priorityColumn));

            logger.debug("FieldName: " + fieldName + " FieldValue: " + filter.get(fieldName));

        }

        Assert.assertEquals(policyTypeTestID, filter.get(policyTypeColumn).asLong());
        Assert.assertEquals(policyTypeTestID + 1, filter.get(priorityColumn).asLong() );
    }

}
