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

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * Holds details of a term returned in a TermGetInfo call
 */
public class Term {

    private int apcmWeight;
    private int documentOccurrences;
    private int totalOccurrences;
    private int termCase;
    private int startPosition;
    private int length;
    private String termString;

    public Term(){

    }

    public Term(
            int apcmWeight,
            int documentOccurrences,
            int totalOccurrences,
            int termCase,
            int startPosition,
            int length,
            String termString){
        this.apcmWeight = apcmWeight;
        this.documentOccurrences = documentOccurrences;
        this.totalOccurrences = totalOccurrences;
        this.termCase = termCase;
        this.startPosition = startPosition;
        this.length = length;
        this.termString = termString;
    }


    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getStartPosition() {
        return startPosition;
    }

    public void setStartPosition(int startPosition) {
        this.startPosition = startPosition;
    }

    public int getTermCase() {
        return termCase;
    }

    public void setTermCase(int termCase) {
        this.termCase = termCase;
    }

    public int getTotalOccurrences() {
        return totalOccurrences;
    }

    public void setTotalOccurrences(int totalOccurrences) {
        this.totalOccurrences = totalOccurrences;
    }

    public int getDocumentOccurrences() {
        return documentOccurrences;
    }

    public void setDocumentOccurrences(int documentOccurrences) {
        this.documentOccurrences = documentOccurrences;
    }

    public int getApcmWeight() {
        return apcmWeight;
    }

    public void setApcmWeight(int apcmWeight) {
        this.apcmWeight = apcmWeight;
    }

    /**
     * Returns a string representation of the object.
     *
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        return ReflectionToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    public String getTermString() {
        return termString;
    }

    public void setTermString(String termString) {
        this.termString = termString;
    }
}
