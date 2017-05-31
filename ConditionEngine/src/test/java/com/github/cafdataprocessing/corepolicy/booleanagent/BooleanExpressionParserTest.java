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
package com.github.cafdataprocessing.corepolicy.booleanagent;

import junit.framework.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.File;
import java.util.Scanner;

public class BooleanExpressionParserTest {
    private static final String CONTENTFIELDNAME = "DRECONTENT";
    private static BooleanExpressionParser parser;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @BeforeClass
    public static void setup() {
        parser = new BooleanExpressionParser(CONTENTFIELDNAME);
    }

    @Test
    public void testLiteralTerm() throws Exception {
        String query = BooleanExpressionParser.wrapQuery(parser.parse("aaa")).string();
        Assert.assertEquals("{\"query\":{\"match\":{\"DRECONTENT\":{\"query\":\"aaa\",\"type\":\"phrase\"}}}}", query.toString());
    }

    @Test
    public void testConcatenatedLiteralTerm() throws Exception {
        String query = BooleanExpressionParser.wrapQuery(parser.parse("aaa bbb ccc")).string();
        Assert.assertEquals("{\"query\":{\"match\":{\"DRECONTENT\":{\"query\":\"aaa bbb ccc\",\"type\":\"phrase\"}}}}", query.toString());
    }

    @Test
    public void testOrTerm() throws Exception {
        String query = BooleanExpressionParser.wrapQuery(parser.parse("aaa or bbb")).string();
        Assert.assertEquals("{\"query\":{\"bool\":{\"should\":[{\"match\":{\"DRECONTENT\":{\"query\":\"aaa\",\"type\":\"phrase\"}}},{\"match\":{\"DRECONTENT\":{\"query\":\"bbb\",\"type\":\"phrase\"}}}]}}}", query.toString());
    }

    @Test
    public void testAndTerm() throws Exception {
        String query = BooleanExpressionParser.wrapQuery(parser.parse("aaa and bbb")).string();
        Assert.assertEquals("{\"query\":{\"bool\":{\"must\":[{\"match\":{\"DRECONTENT\":{\"query\":\"aaa\",\"type\":\"phrase\"}}},{\"match\":{\"DRECONTENT\":{\"query\":\"bbb\",\"type\":\"phrase\"}}}]}}}", query.toString());
    }

    @Test
    public void testNotTerm() throws Exception {
        String query = BooleanExpressionParser.wrapQuery(parser.parse("not aaa")).string();
        Assert.assertEquals("{\"query\":{\"bool\":{\"must_not\":{\"match\":{\"DRECONTENT\":{\"query\":\"aaa\",\"type\":\"phrase\"}}}}}}", query.toString());
    }

    @Test
    public void testNotParenthesizedTerm() throws Exception {
        String query = BooleanExpressionParser.wrapQuery(parser.parse("not (aaa)")).string();
        Assert.assertEquals("{\"query\":{\"bool\":{\"must_not\":{\"match\":{\"DRECONTENT\":{\"query\":\"aaa\",\"type\":\"phrase\"}}}}}}", query.toString());
    }

    @Test
    public void testProximityTerm() throws Exception {
        String query = BooleanExpressionParser.wrapQuery(parser.parse("aaa /10 bbb")).string();
        Assert.assertEquals("{\"query\":{\"match\":{\"DRECONTENT\":{\"query\":\"aaa bbb\",\"type\":\"phrase\",\"slop\":10}}}}", query.toString());
    }

    @Test
    public void testCompoundTerm() throws Exception {
        String query = BooleanExpressionParser.wrapQuery(parser.parse("not((aaa and bbb) or (ccc and not ddd))")).string();
        Assert.assertEquals("{\"query\":{\"bool\":{\"must_not\":{\"bool\":{\"should\":[{\"bool\":{\"must\":[{\"match\":{\"DRECONTENT\":{\"query\":\"aaa\",\"type\":\"phrase\"}}},{\"match\":{\"DRECONTENT\":{\"query\":\"bbb\",\"type\":\"phrase\"}}}]}},{\"bool\":{\"must\":[{\"match\":{\"DRECONTENT\":{\"query\":\"ccc\",\"type\":\"phrase\"}}},{\"bool\":{\"must_not\":{\"match\":{\"DRECONTENT\":{\"query\":\"ddd\",\"type\":\"phrase\"}}}}}]}}]}}}}}", query.toString());
    }

    @Test
    public void testCompoundQuotedWildcardTerm() throws Exception {
        String query = BooleanExpressionParser.wrapQuery(parser.parse("((\"do not reply\" AND \"email\") OR \"Newsletter*\" OR \"marketing email*\" OR \"promotional e-mail\" OR \"promotional email\") AND (\"chang?* your email preferences\" OR \"unsubscribe\" OR \"opt-out\" OR \"no longer receive\")")).string();
        Assert.assertEquals("{\"query\":{\"bool\":{\"must\":[{\"bool\":{\"should\":[{\"bool\":{\"must\":[{\"match\":{\"DRECONTENT\":{\"query\":\"do not reply\",\"type\":\"phrase\"}}},{\"match\":{\"DRECONTENT\":{\"query\":\"email\",\"type\":\"phrase\"}}}]}},{\"bool\":{\"should\":[{\"query_string\":{\"query\":\"Newsletter*\",\"analyze_wildcard\":true}},{\"bool\":{\"should\":[{\"query_string\":{\"query\":\"marketing email*\",\"analyze_wildcard\":true}},{\"bool\":{\"should\":[{\"match\":{\"DRECONTENT\":{\"query\":\"promotional e-mail\",\"type\":\"phrase\"}}},{\"match\":{\"DRECONTENT\":{\"query\":\"promotional email\",\"type\":\"phrase\"}}}]}}]}}]}}]}},{\"bool\":{\"should\":[{\"query_string\":{\"query\":\"chang?* your email preferences\",\"analyze_wildcard\":true}},{\"bool\":{\"should\":[{\"match\":{\"DRECONTENT\":{\"query\":\"unsubscribe\",\"type\":\"phrase\"}}},{\"bool\":{\"should\":[{\"match\":{\"DRECONTENT\":{\"query\":\"opt-out\",\"type\":\"phrase\"}}},{\"match\":{\"DRECONTENT\":{\"query\":\"no longer receive\",\"type\":\"phrase\"}}}]}}]}}]}}]}}}", query.toString());
    }

    @Test
    public void testMissingCloseParenthesis() throws Exception {
        thrown.expectMessage("Unexpected token. Expected \"closeparenthesis\" but encountered \"end\".");
        parser.parse("a and (aa").toString();
    }

    @Test
    public void testMissingOpenParenthesis() throws Exception {
        thrown.expectMessage("Unexpected token. Expected \"end\" but encountered \"closeparenthesis\".");
        parser.parse("a and aa)").toString();
    }

    @Test
    public void testMissingOperator() throws Exception {
        thrown.expectMessage("Unexpected token. Expected \"end\" but encountered \"openparenthesis\".");
        parser.parse("(aaa) (bbb)").toString();
    }

    @Test
    public void testMissingTerm() throws Exception {
        thrown.expectMessage("Term parsing found an unexpected token. Expected \"literal\", \"openparenthesis\", or \"unaryoperator\" but encountered \"and\".");
        parser.parse(" and bbb").toString();
    }

    @Test
    public void testInvalidProximityTerm() throws Exception {
        thrown.expectMessage("Tokenizer found an invalid proximity operator: \"/34x\".");
        parser.parse("aaa /34x bbb").toString();
    }

    @Test
    public void testInvalidLiteralTerm() throws Exception {
        thrown.expectMessage("No closing quote was found for the quoted string: \"bc def");
        parser.parse("a\"bc def").toString();
    }

    @Test
    public void testParseSampleExpressions() throws Exception {
        parseSampleExpressions("lexiconExpressions1.txt");
        parseSampleExpressions("lexiconExpressions2.txt");
    }


    private void parseSampleExpressions(String sampleExpressionsFilename) throws Exception {
        File sampleExpressionsFile = new File(getClass().getClassLoader().getResource(sampleExpressionsFilename).getFile());
        try (Scanner sampleExpressionsScanner = new Scanner(sampleExpressionsFile)) {
            while (sampleExpressionsScanner.hasNextLine()) {
                String expression = sampleExpressionsScanner.nextLine();
                if (!expression.isEmpty()) {
                    try {
                        String query = BooleanExpressionParser.wrapQuery(parser.parse(expression)).string();
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to parse expression \"" + expression + "\" in sample expressions file \"" + sampleExpressionsFilename + "\".", e);
                    }
                }
            }
        }
    }
}
