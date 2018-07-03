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
package com.github.cafdataprocessing.corepolicy.api;

import com.github.cafdataprocessing.corepolicy.common.ApiProperties;
import com.github.cafdataprocessing.corepolicy.common.dto.*;
import com.github.cafdataprocessing.corepolicy.testing.Assume;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;
import java.util.stream.Collectors;

import static com.github.cafdataprocessing.corepolicy.TestHelper.shouldThrow;
import static org.junit.Assert.*;

/**
 *
 */
public class ClassificationApiLexiconIT extends ClassificationApiTestBase {
    public ClassificationApiLexiconIT() {
    }

    @Test
    public void testAddLexicon() throws Exception {

        // updated to ensure types can cope with UTF8
        Lexicon lexicon = new Lexicon();
        lexicon.name = "My lexicon Citroên";
        lexicon.description = "My description Citroên";
        lexicon.lexiconExpressions = new ArrayList<>();

        LexiconExpression lexiconExpression = new LexiconExpression();
        lexiconExpression.type = LexiconExpressionType.REGEX;
        lexiconExpression.expression = "ABD Citroên";
        lexicon.lexiconExpressions = new LinkedList<>();
        lexicon.lexiconExpressions.add(lexiconExpression);

        Lexicon created = sut.create(lexicon);

        assertLexiconsSame(lexicon, created, false, true);
    }

    //PD-600
    @Test
    public void testAddLexiconExpressionInvalidValue() throws Exception {

        // updated to ensure types can cope with UTF8
        Lexicon lexicon = new Lexicon();
        lexicon.name = "My lexicon Citroên";
        lexicon.description = "My description Citroên";
        lexicon.lexiconExpressions = new ArrayList<>();

        LexiconExpression lexiconExpression = new LexiconExpression();
        lexiconExpression.type = LexiconExpressionType.REGEX;
        lexiconExpression.expression = "(Value";
        lexicon.lexiconExpressions.add(lexiconExpression);

        shouldThrow(o-> sut.create(lexicon));
    }

    @Test
    public void testAddLexiconNoName() throws Exception {
        Lexicon lexicon = new Lexicon();
        lexicon.description = "I should fail with no name";

        LexiconExpression expression = new LexiconExpression();
        expression.type = LexiconExpressionType.REGEX;
        expression.expression = "abc";

        lexicon.lexiconExpressions = new LinkedList<>();
        lexicon.lexiconExpressions.add(expression);

        shouldThrow(o -> sut.create(lexicon));
    }

    @Test
    public void testAddLexiconNoExpressions() throws Exception {
        Lexicon lexicon = new Lexicon();
        lexicon.name = "Lexicon with no entries";
        sut.create(lexicon);
    }

    @Test
    public void testUpdateLexicon() throws Exception {
        Lexicon lexicon = new Lexicon();

        // updated to ensure it can cope with UTF8 characters.
        lexicon.name = "Testing update Citroên";

        LexiconExpression expression = new LexiconExpression();
        expression.type = LexiconExpressionType.REGEX;
        expression.expression = "Citroên";

        lexicon.lexiconExpressions = new LinkedList<>();
        lexicon.lexiconExpressions.add(expression);
        Lexicon created = sut.create(lexicon);

        assertLexiconsSame(lexicon, created, false, true);

        created.description = "I HAVE BEEN UPDATED Citroên";
        created.name = "I HAVE BEEN UPDATED Citroên";

        Lexicon updated = sut.update(created);

        assertEquals(created.id, updated.id);
        assertLexiconsSame(created, updated, false, true);
    }

    @Test
    public void testUpdateLexiconAddExpression() throws Exception {
        Lexicon lexicon = new Lexicon();
        lexicon.name = "Testing update";

        {
            LexiconExpression expression = new LexiconExpression();
            expression.type = LexiconExpressionType.REGEX;
            expression.expression = "abc";

            lexicon.lexiconExpressions = new LinkedList<>();
            lexicon.lexiconExpressions.add(expression);
        }
        Lexicon created = sut.create(lexicon);

        assertLexiconsSame(lexicon, created, false, true);

        {
            LexiconExpression expression = new LexiconExpression();
            expression.type = LexiconExpressionType.REGEX;
            expression.expression = "def";

            created.lexiconExpressions.add(expression);
        }

        Lexicon updated = sut.update(created);

        assertEquals(created.id, updated.id);
        assertEquals(2, updated.lexiconExpressions.size());
        assertLexiconsSame(created, updated, false, true);
    }

    @Test
    public void testUpdateLexiconWithNullExpressions() throws Exception {
        Lexicon lexicon = new Lexicon();
        lexicon.name = "Testing update";

        LexiconExpression expression = new LexiconExpression();
        expression.type = LexiconExpressionType.REGEX;
        expression.expression = "abc";

        lexicon.lexiconExpressions = new LinkedList<>();
        lexicon.lexiconExpressions.add(expression);
        Lexicon created = sut.create(lexicon);

        assertLexiconsSame(lexicon, created, false, true);

        created.description = "I HAVE BEEN UPDATED";
        created.name = "I HAVE BEEN UPDATED";
        created.lexiconExpressions = null;

        Lexicon updated = sut.update(created);

        assertEquals(created.id, updated.id);
        assertEquals(1, updated.lexiconExpressions.size());
        assertLexiconsSame(created, updated, false, true);
    }

    @Test
    public void testUpdateDeletedLexicon() throws Exception {
        Lexicon lexicon = new Lexicon();
        lexicon.name = "Testing update";

        LexiconExpression expression = new LexiconExpression();
        expression.type = LexiconExpressionType.REGEX;
        expression.expression = "abc";

        lexicon.lexiconExpressions = new LinkedList<>();
        lexicon.lexiconExpressions.add(expression);
        Lexicon created = sut.create(lexicon);

        assertLexiconsSame(lexicon, created, false, true);

        sut.deleteLexicon(created.id);


        created.description = "I HAVE BEEN UPDATED";
        created.name = "I HAVE BEEN UPDATED";

        shouldThrow(o -> sut.update(created));
    }

    @Test
    public void testUpdateLexiconExpressions() throws Exception {
        //Existing expressions should be removed

        Lexicon lexicon = new Lexicon();
        lexicon.name = "My lexicon";
        lexicon.description = "My description";
        lexicon.lexiconExpressions = new ArrayList<>();

        {
            LexiconExpression lexiconExpression = new LexiconExpression();
            lexiconExpression.type = LexiconExpressionType.REGEX;
            lexiconExpression.expression = "ABD";
            lexicon.lexiconExpressions.add(lexiconExpression);
        }

        Lexicon created = sut.create(lexicon);

        assertLexiconsSame(lexicon, created, false, true);

        LexiconExpression expressionThatShouldBeDeleted = created.lexiconExpressions.stream().findFirst().get();

        created.lexiconExpressions.clear();

        {
            LexiconExpression lexiconExpression = new LexiconExpression();
            lexiconExpression.type = LexiconExpressionType.REGEX;
            lexiconExpression.expression = "LALALA";
            created.lexiconExpressions.add(lexiconExpression);
        }

        Lexicon updated = sut.update(created);

        assertLexiconsSame(created, updated, false, true);

        ArrayList<Long> ids = new ArrayList<>();
        ids.add(expressionThatShouldBeDeleted.id);
        shouldThrow(o -> sut.retrieveLexiconExpressions(ids));
    }

    @Test
    public void testUpdateLexiconExpressions_Additive() {
        Assume.assumeFalse(Assume.AssumeReason.BY_DESIGN, "CAF-1051", "ClassificationApiLexiconIT::testUpdateLexiconExpressions_Additive", "Batch creating is not supported in MySQL.",
                !testingProperties.getWebInHibernate() && apiProperties.isInApiMode(ApiProperties.ApiMode.web), genericApplicationContext);

        Lexicon lexicon = new Lexicon();
        lexicon.name = "My lexicon";
        lexicon.description = "My description";
        lexicon.lexiconExpressions = new ArrayList<>();

        LexiconExpression lexiconExpression = new LexiconExpression();
        lexiconExpression.type = LexiconExpressionType.REGEX;
        lexiconExpression.expression = "ABC";
        lexicon.lexiconExpressions.add(lexiconExpression);

        Lexicon created = sut.create(lexicon);

        lexiconExpression.lexiconId = created.id;

        assertLexiconsSame(lexicon, created, false, true);

        //Remove current expressions and add new ones
        created.lexiconExpressions.clear();
        LexiconExpression lexiconExpression2 = new LexiconExpression();
        lexiconExpression2.type = LexiconExpressionType.REGEX;
        lexiconExpression2.expression = "DEF";
        lexiconExpression2.lexiconId = created.id;

        LexiconExpression lexiconExpression3 = new LexiconExpression();
        lexiconExpression3.type = LexiconExpressionType.REGEX;
        lexiconExpression3.expression = "GHI";
        lexiconExpression3.lexiconId = created.id;

        created.lexiconExpressions.add(lexiconExpression2);
        created.lexiconExpressions.add(lexiconExpression3);

        //Update the Lexicon to add new expressions
        created = sut.update(created, UpdateBehaviourType.ADD);

        Assert.assertEquals("Lexicon should now have 3 expressions", 3, created.lexiconExpressions.size());
        LexiconExpression savedExpression = created.lexiconExpressions.stream().filter(e -> e.expression.equalsIgnoreCase(lexiconExpression.expression)).findFirst().get();
        assertLexiconExpressionSame(lexiconExpression, savedExpression);
        savedExpression = created.lexiconExpressions.stream().filter(e -> e.expression.equalsIgnoreCase(lexiconExpression2.expression)).findFirst().get();
        assertLexiconExpressionSame(lexiconExpression2, savedExpression);
        savedExpression = created.lexiconExpressions.stream().filter(e -> e.expression.equalsIgnoreCase(lexiconExpression3.expression)).findFirst().get();
        assertLexiconExpressionSame(lexiconExpression3, savedExpression);
    }

    @Test
    public void testDeleteExpressionFromLexicon() throws Exception {
        Lexicon lexicon1 = new Lexicon();
        lexicon1.name = "Lexicon 1";

        Lexicon created = sut.create(lexicon1);
        assertLexiconsSame(lexicon1, created, false, true);

        //add an expression to the lexicon

        LexiconExpression expr1 = new LexiconExpression();
        expr1.type = LexiconExpressionType.REGEX;
        expr1.expression = "abc";
        expr1.lexiconId = created.id;

        LexiconExpression createdExpression = sut.create(expr1);
        assertEquals(expr1.lexiconId, createdExpression.lexiconId);
        assertEquals(expr1.expression, createdExpression.expression);
        assertEquals(expr1.type, createdExpression.type);
        assertNotNull(createdExpression.id);

        //remove the expression from the lexicon
        sut.deleteLexiconExpression(createdExpression.id);

        //fetch the lexicon again and make sure no expressions
        ArrayList<Long> ids = new ArrayList<>();
        ids.add(created.id);

        assertEquals(0, sut.retrieveLexicons(ids).stream().findFirst().get().lexiconExpressions.size());
    }

    @Test
    public void addExpressionToLexiconAndMoveToAnother() throws Exception {
        //lexicon 1
        //  expr1 -> move to Lexicon 2
        //Lexicon 2

        Lexicon lexicon1 = new Lexicon();
        lexicon1.name = "Lexicon 1";

        Lexicon created = sut.create(lexicon1);
        assertLexiconsSame(lexicon1, created, false, true);

        //add an expression to the lexicon

        LexiconExpression expr1 = new LexiconExpression();
        expr1.type = LexiconExpressionType.REGEX;
        expr1.expression = "abc";
        expr1.lexiconId = created.id;

        LexiconExpression createdExpression = sut.create(expr1);
        assertEquals(expr1.lexiconId, createdExpression.lexiconId);
        assertEquals(expr1.expression, createdExpression.expression);
        assertEquals(expr1.type, createdExpression.type);
        assertNotNull(createdExpression.id);


        //create a new lexicon
        Lexicon lexicon2 = new Lexicon();
        lexicon2.name = "Lexicon 2";
        Lexicon created2 = sut.create(lexicon2);

        //change the expressions parent and see if it moved.
        createdExpression.lexiconId = created2.id;
        LexiconExpression movedExpression = sut.update(createdExpression);

        assertEquals(createdExpression.lexiconId, movedExpression.lexiconId);
        assertEquals(createdExpression.expression, movedExpression.expression);
        assertEquals(createdExpression.type, movedExpression.type);
        assertEquals(createdExpression.id, movedExpression.id);
    }

    @Test
    public void testDeleteLexicon() throws Exception {

        Lexicon lexicon = new Lexicon();
        lexicon.name = "Testing update";

        LexiconExpression expression = new LexiconExpression();
        expression.type = LexiconExpressionType.REGEX;
        expression.expression = "abc";

        lexicon.lexiconExpressions = new LinkedList<>();
        lexicon.lexiconExpressions.add(expression);
        Lexicon created = sut.create(lexicon);

        assertLexiconsSame(lexicon, created, false, true);

        sut.deleteLexicon(created.id);


        ArrayList<Long> ids = new ArrayList<>();
        ids.add(created.id);
        shouldThrow(o -> sut.retrieveLexicons(ids));
    }

    private void assertLexiconExpressionSame(LexiconExpression expected, LexiconExpression actual){
        if(expected == null && actual == null){
            return;
        }
        Assert.assertEquals("Lexicon expression should match",expected.expression, actual.expression);
        Assert.assertEquals("lexicon expression type should match",expected.type,actual.type);
        Assert.assertEquals("lexicon id should match",expected.lexiconId,actual.lexiconId);
    }

    private void assertLexiconsSame(Lexicon expected, Lexicon actual, boolean includeIds, boolean assertActualIds){

        if(includeIds){
            assertEquals(expected.id, actual.id);
        }

        if(assertActualIds){
            assertNotNull(actual.id);
        }


        assertEquals(expected.name, actual.name);
        assertEquals(expected.description, actual.description);
        if(expected.lexiconExpressions!=null && actual.lexiconExpressions != null) {
            assertEquals(expected.lexiconExpressions.size(), actual.lexiconExpressions.size());
        }

        //comparing expressions
        if(expected.lexiconExpressions != null) {
            assertEquals(expected.lexiconExpressions.size(), actual.lexiconExpressions.size());
        }

        //check that all actual expressions have non null ids
        if(assertActualIds && actual.lexiconExpressions != null){
            assertFalse(actual.lexiconExpressions.stream().filter(e -> e.id == null).findAny().isPresent());
        }

        if(expected.lexiconExpressions != null) {
            for (LexiconExpression expectedExpression : expected.lexiconExpressions) {
                if (includeIds) {
                    Optional<LexiconExpression> matchedExpression = actual.lexiconExpressions.stream().filter(l -> l.id.equals(expectedExpression.id)).findFirst();
                    assertTrue(matchedExpression.isPresent());

                    assertEquals(expectedExpression.lexiconId, matchedExpression.get().lexiconId);
                    assertEquals(expectedExpression.expression, matchedExpression.get().expression);
                    assertEquals(expectedExpression.type, matchedExpression.get().type);
                } else {
                    //if we are not comparing ids, just find one that has the same expression and type
                    Optional<LexiconExpression> matchedExpression = actual.lexiconExpressions.stream().filter(e ->
                            e.expression.equals(expectedExpression.expression) &&
                                    e.type.equals(expectedExpression.type)).findAny();

                    assertTrue(matchedExpression.isPresent());
                }
            }
        }
    }

    @Test
    public void testReturnPageOrder() {
        Collection<Lexicon> newItems = new LinkedList<>();

        {
            Lexicon item = new Lexicon();
            item.name = "CMy lexicon";
            item.description = "My description";
            item.lexiconExpressions = new ArrayList<>();

            item = sut.create(item);

            newItems.add(item);
        }
        {
            Lexicon item = new Lexicon();
            item.name = "AMy lexicon";
            item.description = "My description";
            item.lexiconExpressions = new ArrayList<>();

            item = sut.create(item);

            newItems.add(item);
        }
        {
            Lexicon item = new Lexicon();
            item.name = "BMy lexicon";
            item.description = "My description";
            item.lexiconExpressions = new ArrayList<>();

            item = sut.create(item);

            newItems.add(item);
        }

        checkSortedItems(newItems);
    }


    private void checkSortedItems(Collection<Lexicon> newItems) {
        List<Lexicon> sortedItems;
        PageRequest pageRequest = new PageRequest(1L, 10L);
        PageOfResults<Lexicon> pageOfResults = sut.retrieveLexiconsPage(pageRequest);
        Collection<Lexicon> allItems = new ArrayList<>();

        if (pageOfResults.totalhits > newItems.size()) {
            // Go through and reqeust all pages, and ensure the sorting matches that expected.
            // Now finally ensure we get all the items in one big page to compare.
            pageOfResults = sut.retrieveLexiconsPage(new PageRequest(1L, pageOfResults.totalhits + 10L));
            allItems.addAll(pageOfResults.results);

            assertEquals("Sorted Pages of result should match", Long.valueOf(allItems.size()), pageOfResults.totalhits);

        } else {
            // all items is just what we have created dont need to request it all again.
            allItems = newItems;
        }
        sortedItems = allItems.stream().sorted((c1, c2) -> {
            if (c1.name == null ) {
                if(c2.name == null) {
                    return c1.id.compareTo(c2.id);
                }
                return -1; // null comes before any text
            } else if (c2.name == null) {
                return 1; // source has text, dest null, so after
            }
            else{
                return c1.name.compareToIgnoreCase(c2.name);
            }
        }).collect(Collectors.toList());

        assertEquals(Long.valueOf(sortedItems.size()), pageOfResults.totalhits);
        List<Lexicon> returnedOrder = pageOfResults.results.stream().collect(Collectors.toList());

        for (int i = 0; i < sortedItems.size(); i++) {
            assertEquals("Order of id not correct for index: " + String.valueOf(i), sortedItems.get(i).id, returnedOrder.get(i).id);
            assertEquals("Order of Name not correct for index: " + String.valueOf(i), sortedItems.get(i).name, returnedOrder.get(i).name);
        }

    }
}
