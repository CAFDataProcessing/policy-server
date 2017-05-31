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
package com.github.cafdataprocessing.corepolicy.api;

import com.cedarsoftware.util.DeepEquals;
import com.github.cafdataprocessing.corepolicy.common.dto.FieldLabelType;
import com.github.cafdataprocessing.corepolicy.common.dto.FieldLabel;
import com.github.cafdataprocessing.corepolicy.common.dto.PageOfResults;
import com.github.cafdataprocessing.corepolicy.common.dto.PageRequest;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.github.cafdataprocessing.corepolicy.TestHelper.getUniqueString;
import static com.github.cafdataprocessing.corepolicy.TestHelper.shouldThrow;
import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Integration tests for verifying field labels against Classification API
 */
public class ClassificationApiFieldLabelIT extends ClassificationApiTestBase {
    @Test
    public void testCreateFieldLabel() throws Exception {
        FieldLabel fieldLabel = new FieldLabel();
        fieldLabel.name = getUniqueString("New fieldName Citroên");
        fieldLabel.fieldType = FieldLabelType.DATE;

        fieldLabel.fields = Arrays.asList("Test1 Citroên", "Test2 Citroên", "Test3 Citroên");

        FieldLabel created = sut.create(fieldLabel);

        fieldLabel.id = created.id;

        assertEquals(fieldLabel, created);
    }

    @Test
    public void testCreateSameNameFieldLabel() throws Exception {
        String fieldName= getUniqueString("New fieldLabel");
        FieldLabel fieldLabel = new FieldLabel();
        fieldLabel.name =fieldName;
        fieldLabel.fieldType = FieldLabelType.DATE;
        fieldLabel.fields = Arrays.asList("Test1", "Test2", "Test3");

        FieldLabel created = sut.create(fieldLabel);

        shouldThrow(o -> sut.create(fieldLabel));
        FieldLabel retrieveFieldLabel = sut.retrieveFieldLabel(fieldName);

        //Check that the first created item is still retrieved
        assertEquals(created, retrieveFieldLabel);
    }

    @Test
    public void testCreateNoFieldsFieldLabel() throws Exception {
        String fieldName= getUniqueString("New fieldLabel");
        FieldLabel fieldLabel = new FieldLabel();
        fieldLabel.name =fieldName;
        fieldLabel.fieldType = FieldLabelType.DATE;
        fieldLabel.fields = Arrays.asList();

        shouldThrow(o -> sut.create(fieldLabel));
    }

    @Test
    public void testUpdateFieldLabelName() throws Exception {
        FieldLabel fieldLabel = new FieldLabel();
        fieldLabel.name = getUniqueString("New fieldLabel");
        fieldLabel.fieldType = FieldLabelType.DATE;
        fieldLabel.fields = Arrays.asList("Test1", "Test2", "Test3");

        FieldLabel created = sut.create(fieldLabel);

        created.name = getUniqueString("New Name");

        FieldLabel updated = sut.update(created);

        assertEquals(created, updated);
    }

    @Test
    public void testUpdateFieldLabelType() throws Exception {
        FieldLabel fieldLabel = new FieldLabel();
        fieldLabel.name = getUniqueString("New fieldName_");
        fieldLabel.fieldType = FieldLabelType.DATE;
        fieldLabel.fields = Arrays.asList("Test1", "Test2", "Test3");

        FieldLabel created = sut.create(fieldLabel);

        created.fieldType = FieldLabelType.STRING;
        created.name = getUniqueString("New fieldName with UTF8Chars Citroên_");
        FieldLabel updated = sut.update(created);

        assertEquals(created, updated);
    }

    @Test
    public void testUpdateFieldLabelToUsedName() throws Exception {

        String fieldName = getUniqueString("New name");
        {
            //Create a fieldLabel with a new name
            FieldLabel fieldLabel = new FieldLabel();
            fieldLabel.name = fieldName;
            fieldLabel.fieldType = FieldLabelType.DATE;
            fieldLabel.fields = Arrays.asList("Test1", "Test2", "Test3");

            FieldLabel created = sut.create(fieldLabel);
        }


        FieldLabel fieldLabel = new FieldLabel();
        fieldLabel.name = getUniqueString("Temp Name");
        fieldLabel.fieldType = FieldLabelType.DATE;
        fieldLabel.fields = Arrays.asList("Test1", "Test2", "Test3");

        FieldLabel created = sut.create(fieldLabel);

        //Change to the name which is in use
        created.name = fieldName;

        //Should throw as the name is in use by another fieldlabel
        shouldThrow(u->sut.update(created));

        //Check that we can still retrieve properly
        sut.retrieveFieldLabel("New name");
    }

    @Test
    public void testDeleteGlobalFieldLabel() throws Exception {
        shouldThrow(o -> sut.deleteFieldLabel(1L));
    }

    @Test
    public void testDeleteFieldLabel() throws Exception {
        FieldLabel fieldLabel = new FieldLabel();
        fieldLabel.name = getUniqueString("New fieldName");
        fieldLabel.fieldType = FieldLabelType.DATE;
        fieldLabel.fields = Arrays.asList("Test1", "Test2", "Test3");

        FieldLabel created = sut.create(fieldLabel);

        sut.deleteFieldLabel(created.id);

        assertNull(sut.retrieveFieldLabel(created.name));
    }

    @Test
    public void testGetFieldLabelPage() throws Exception {

        PageRequest pageRequest = new PageRequest();
        pageRequest.start=1L;
        pageRequest.max_page_results=10L;

        PageOfResults<FieldLabel> page = sut.retrieveFieldLabelPage(pageRequest);

        assertEquals(10, page.results.size());
    }

    @Test
    public void testGetFakeFieldLabelPage() throws Exception {

        FieldLabel fieldLabel = sut.retrieveFieldLabel("Jasons fake field label");

        assertNull(fieldLabel);
    }

    @Test
    public void testReturnPageOrder() {
        PageRequest pageRequest = new PageRequest(1L, 5L);
        PageOfResults<FieldLabel> pageOfResults = sut.retrieveFieldLabelPage(pageRequest);

        List<FieldLabel> sortedItems = pageOfResults.results.stream().sorted((c1, c2) -> c1.name.compareTo(c2.name)).collect(Collectors.toList());

        assertEquals(sortedItems.size(), pageOfResults.results.size());
        for (int i = 0; i < sortedItems.size(); i++) {
            assertEquals(sortedItems.get(i).id, pageOfResults.results.stream().collect(Collectors.toList()).get(i).id);
        }
    }

    private void assertEquals(Object target, Object created){
        assertTrue(DeepEquals.deepEquals(target, created));
    }
}
