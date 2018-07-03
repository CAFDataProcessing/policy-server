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
package com.github.cafdataprocessing.corepolicy.booleanagent;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.StringReader;
import java.text.MessageFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BooleanExpressionTokenizer {
    private String expression;
    private PushbackReader reader;
    private Token nextToken;
    private Token futureToken;

    public static final Token END = new End();
    private static Pattern proximityPattern = Pattern.compile("^/(\\d*)$");
    private static Pattern wildcardPattern = Pattern.compile("[?*]");

    public BooleanExpressionTokenizer(String expression) {
        this.expression = expression;
        reader = new PushbackReader(new StringReader(expression));
    }

    public Token next() throws IOException {
        while (nextToken == null) {
            nextToken = futureToken;
            futureToken = readNextToken();
            if (nextToken instanceof Literal) {
                while (futureToken instanceof Literal) {
                    ((Literal)nextToken).append((Literal)futureToken);
                    futureToken = readNextToken();
                }
            }
        }
        return nextToken;
    }

    public void consume() {
        if (nextToken != END) {
            nextToken = null;
        }
    }

    private Token readNextToken() throws IOException {
        int nextCh = reader.read();
        while (nextCh != -1 && Character.isWhitespace(nextCh)) {
            nextCh = reader.read();
        }
        if (nextCh == -1) {
            return END;
        }

        if ((char)nextCh == '(') {
            return new OpenParenthesis();
        }
        if ((char)nextCh == ')') {
            return new CloseParenthesis();
        }

        String token = readTokenText(nextCh);
        switch (token.toLowerCase())
        {
            case "and":
                return new And();
            case "or":
                return new Or();
            case "not":
                return new Not();
            default: {
                String proximity = getProximity(token);
                if (proximity != null) {
                    return new Proximity(Integer.parseInt(proximity));
                }
                if (containsWildcards(token)) {
                    return new WildcardLiteral(token);
                }
                return new Literal(token);
            }
        }
    }

    private boolean containsWildcards(String token) {
        Matcher matcher = wildcardPattern.matcher(token);
        return matcher.find();
    }

    private String readTokenText(int nextCh) throws IOException {
        if ((char)nextCh == '"') {
            return readQuotedString(nextCh);
        }
        StringBuilder text = new StringBuilder();
        while (nextCh != -1 && !Character.isWhitespace(nextCh) && (char)nextCh != '(' && (char)nextCh != ')' && (char)nextCh != '"') {
            text.append((char)nextCh);
            nextCh = reader.read();
        }
        if ((char)nextCh == '(' || (char)nextCh == ')' || (char)nextCh == '"') {
            reader.unread(nextCh);
        }
        return text.toString();
    }

    private String readQuotedString(int nextCh) throws IOException {
        if (nextCh != -1 && (char)nextCh == '"') {
            nextCh = reader.read();
        }
        StringBuilder text = new StringBuilder();
        while (nextCh != -1 && (char)nextCh != '"') {
            text.append((char)nextCh);
            nextCh = reader.read();
        }
        if (nextCh == -1) {
            String error = MessageFormat.format("No closing quote was found for the quoted string: \"{0}", text);
            throw new RuntimeException(error);
        }
        return text.toString();
    }

    private String getProximity(String text) {
        if (text.startsWith("/")) {
            Matcher matcher = proximityPattern.matcher(text);
            if (matcher.find()) {
                return matcher.group(1);
            }
            String error = MessageFormat.format("Tokenizer found an invalid proximity operator: \"{0}\".", text);
            throw new RuntimeException(error);
        }
        return null;
    }

    public abstract static class Token {
        public abstract int precedence();
    }

    public static class End extends Token {
        @Override
        public int precedence() { return 0; }
    }

    public abstract static class Operator extends Token {}

    public abstract static class BinaryOperator extends Operator {}

    public abstract static class UnaryOperator extends Operator {}

    public static class Proximity extends BinaryOperator {
        private int distance;
        public Proximity(int distance) {
            this.distance = distance;
        }
        @Override
        public int precedence() { return 2; }
        public int getDistance() { return distance; }
    }

    public static class Or extends BinaryOperator {
        @Override
        public int precedence() { return 3; }
    }

    public static class And extends BinaryOperator {
        @Override
        public int precedence() { return 4; }
    }

    public static class Not extends UnaryOperator {
        @Override
        public int precedence() { return 5; }
    }

    public static class Parenthesis extends Token {
        @Override
        public int precedence() { return 6; }
    }

    public static class CloseParenthesis extends Parenthesis {}

    public static class OpenParenthesis extends Parenthesis {}


    public static class Literal extends Token {
        private String value;

        public Literal(String value) {
            this.value = value;
        }

        @Override
        public int precedence() { return 1; }

        public String getValue() { return value; }

        public Literal append(Literal other) {
            this.value = new StringBuilder(this.value).append(' ').append(other.getValue()).toString();
            return this;
        }
    }

    public static class WildcardLiteral extends Literal {
        public WildcardLiteral(String value) {
            super(value);
        }
    }
}
