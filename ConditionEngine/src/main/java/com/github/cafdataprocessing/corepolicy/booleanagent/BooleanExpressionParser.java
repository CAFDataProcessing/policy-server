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

import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

import static org.elasticsearch.index.query.QueryBuilders.matchPhraseQuery;
import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;

//TODO: Fully implement parsing as per the definitive definition of Verity Universal Search Syntax when it becomes available.

/**
 * A parser that transforms a boolean expression into an Elasticsearch query.
 * It is a recursive descent parser, implementing a recursive version of the Shunting Yard algorithm
 * to transform the infix input expression format into a query tree.
 * The parser expects boolean expressions conforming to the syntax defined by the following EBNF grammar:
 *
 * ===========================================================================
 * EBNF grammar for boolean expressions parsed by this parser
 * ===========================================================================
 * expr = term { binaryOp term } ;
 * term = literal | "(" expr ")" | unaryOp term ;
 * binaryOp = "and" | "or" | proximityOp ;
 * unaryOp = "not";
 * proximityOp = "/" nonZeroDigit { digit } ;
 * nonZeroDigit = "1" | "2" | "3" | "4" | "5" | "6" | "7" | "8" | "9" ;
 * digit = "0" | nonZeroDigit ;
 * literal = quotedString | literalCharacterSequence ;
 * quotedString = '"' stringCharacter { stringCharacter } '"' ;
 * stringCharacter = anyCharacter - '"' | "\\" "\"" ;
 * literalCharacterSequence = literalCharacter { literalCharacter } ;
 * literalCharacter = ( anyCharacter - whiteSpace ) - '"' ;
 * anyCharacter = ? all visible characters ? ;
 * whiteSpace = ? white space characters ? ;
 */
public class BooleanExpressionParser {
    private String literalFieldName;
    private BooleanExpressionTokenizer tokenizer;
    private Stack<BooleanExpressionTokenizer.Token> operators = new Stack<>();
    private Stack<Node> operands = new Stack<>();
    private BooleanExpressionTokenizer.Token SENTINEL = BooleanExpressionTokenizer.END;

    public static XContentBuilder wrapQuery(QueryBuilder query) throws IOException {
        if (query == null) {
            return null;
        }
        XContentBuilder wrappedQuery = XContentFactory.jsonBuilder();
        wrappedQuery.startObject();
        wrappedQuery.field("query", query);
        wrappedQuery.endObject();
        return wrappedQuery;
    }

    public BooleanExpressionParser(String literalFieldName) {
        this.literalFieldName = literalFieldName;
    }

    public QueryBuilder parse(String expression) throws IOException {
        if (expression == null) {
            return null;
        }
        this.tokenizer = new BooleanExpressionTokenizer(expression);
        operators.push(SENTINEL);
        parseExpression();
        expect(BooleanExpressionTokenizer.END);
        return operands.peek().getQuery();
    }

    private void expect(BooleanExpressionTokenizer.Token token) throws IOException{
        if (tokenizer.next() == token) {
            tokenizer.consume();
            return;
        }
        String error = MessageFormat.format("Unexpected token. Expected \"{0}\" but encountered \"{1}\"{2}.",
                token.getClass().getSimpleName().toLowerCase(),
                tokenizer.next().getClass().getSimpleName().toLowerCase(),
                (tokenizer.next() instanceof BooleanExpressionTokenizer.Literal) ?
                        MessageFormat.format(" with value \"{0}\"", ((BooleanExpressionTokenizer.Literal) tokenizer.next()).getValue()) :
                        "");
        throw new RuntimeException(error);
    }

    private void expect(Class<? extends BooleanExpressionTokenizer.Token> tokenClazz) throws IOException{
        if (tokenizer.next().getClass().equals(tokenClazz)) {
            tokenizer.consume();
            return;
        }
        String error = MessageFormat.format("Unexpected token. Expected \"{0}\" but encountered \"{1}\"{2}.",
                tokenClazz.getSimpleName().toLowerCase(),
                tokenizer.next().getClass().getSimpleName().toLowerCase(),
                (tokenizer.next() instanceof BooleanExpressionTokenizer.Literal) ?
                        MessageFormat.format(" with value \"{0}\"", ((BooleanExpressionTokenizer.Literal) tokenizer.next()).getValue()) :
                        "");
        throw new RuntimeException(error);
    }

    private void parseExpression() throws IOException {
        parseTerm();
        while (tokenizer.next() instanceof BooleanExpressionTokenizer.BinaryOperator) {
            pushOperator(tokenizer.next());
            tokenizer.consume();
            parseTerm();
        }
        while (operators.peek() != SENTINEL) {
            popOperator();
        }
    }

    private void parseTerm() throws IOException {
        if (tokenizer.next() instanceof BooleanExpressionTokenizer.Literal) {
            operands.push(makeLeaf(tokenizer.next()));
            tokenizer.consume();
        } else if (tokenizer.next() instanceof BooleanExpressionTokenizer.OpenParenthesis) {
            tokenizer.consume();
            operators.push(SENTINEL);
            parseExpression();
            expect(BooleanExpressionTokenizer.CloseParenthesis.class);
            operators.pop();
        } else if (tokenizer.next() instanceof BooleanExpressionTokenizer.UnaryOperator) {
            pushOperator(tokenizer.next());
            tokenizer.consume();
            parseTerm();
        } else {
            String error = MessageFormat.format("Term parsing found an unexpected token. Expected \"literal\", \"openparenthesis\", or \"unaryoperator\" but encountered \"{0}\".",
                    tokenizer.next().getClass().getSimpleName().toLowerCase());
            throw new RuntimeException(error);
        }
    }

    private void popOperator() {
        if (operators.peek() instanceof BooleanExpressionTokenizer.BinaryOperator) {
            Node term2 = operands.pop();
            Node term1 = operands.pop();
            operands.push(makeNode(operators.pop(), term1, term2));
        } else {
            operands.push(makeNode(operators.pop(), operands.pop()));
        }
    }

    private void pushOperator(BooleanExpressionTokenizer.Token operator) {
        while (operators.peek().precedence() > operator.precedence()) {
            popOperator();
        }
        operators.push(operator);
    }

    private Node makeLeaf(BooleanExpressionTokenizer.Token token) {
        return makeNode(token);
    }

    private Node makeNode(BooleanExpressionTokenizer.Token token, Node... terms) {
        return new Node(token, terms);
    }


    private class Node {
        private BooleanExpressionTokenizer.Token token;
        private List<Node> childNodes = new ArrayList<>();
        private QueryBuilder query;

        public Node(BooleanExpressionTokenizer.Token token, Node... children) {
            this.token = token;
            for (Node child : children) {
                childNodes.add(child);
            }
        }

        public QueryBuilder getQuery() {
            if (query == null) {
                buildQuery();
            }
            return query;
        }

        private void buildQuery() {
            if (token instanceof BooleanExpressionTokenizer.WildcardLiteral) {
                this.query = queryStringQuery(((BooleanExpressionTokenizer.Literal) token).getValue()).analyzeWildcard(true);
                return;
            }
            if (token instanceof BooleanExpressionTokenizer.Literal) {
                this.query = matchPhraseQuery(literalFieldName, ((BooleanExpressionTokenizer.Literal) token).getValue());
                return;
            }
            if (token instanceof BooleanExpressionTokenizer.Or) {
                BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
                childNodes.forEach(n -> boolQuery.should(n.getQuery()));
                this.query = boolQuery;
                return;
            }
            if (token instanceof BooleanExpressionTokenizer.And) {
                BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
                childNodes.forEach(n -> boolQuery.must(n.getQuery()));
                this.query = boolQuery;
                return;
            }
            if (token instanceof BooleanExpressionTokenizer.Not) {
                if (childNodes.size() != 1) {
                    throw new RuntimeException("Encountered a \"not\" operator that does not have exactly one operand.");
                }
                this.query = QueryBuilders.boolQuery().mustNot(childNodes.get(0).getQuery());
                return;
            }
            if (token instanceof BooleanExpressionTokenizer.Proximity) {
                if (childNodes.stream().anyMatch(n -> !(n.token instanceof BooleanExpressionTokenizer.Literal))) {
                    throw new RuntimeException("Encountered a proximity operator with a non-literal operand.");
                }
                String concatenatedLiterals = childNodes
                        .stream()
                        .map(n -> ((BooleanExpressionTokenizer.Literal)n.token).getValue())
                        .collect(Collectors.joining(" "));
                this.query = matchPhraseQuery(literalFieldName, concatenatedLiterals).slop(((BooleanExpressionTokenizer.Proximity) token).getDistance());
                return;
            }
            String error = MessageFormat.format("Query building found an unexpected token. Expected \"literal\", \"or\", \"and\", \"not\", or \"proximity\" but encountered \"{0}\".",
                    token.getClass().getSimpleName().toLowerCase());
            throw new RuntimeException(error);
        }
    }
}
