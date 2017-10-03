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
package com.github.cafdataprocessing.corepolicy.common.exceptions.excpetionErrors;

import java.util.HashMap;
import java.util.Map;

/**
 * List of error causes for an invalid field error
 */
public enum InvalidFieldValueErrors implements ExceptionErrorsEnum {
    UNKNOWN(0),
    UNKNOWN_RESOLUTION(1),
    POLICY_DETAILS_INVALID(2),
    POLICY_ID_REQUIRED(3),
    POLICY_TYPE_ID_REQUIRED(4),
    TYPE_IS_INVALID(5),

    CANNOT_DELETE_HAS_DEPENDANT_ITEMS(9000),

    FIELD_LABEL_NAME_MUST_BE_UNIQUE(10001),

    CANNOT_DELETE_LEXICON_HAS_DEPENDANT_ITEMS(11051),

    NO_MATCHING_DEFAULT_COLLECTION(11501),
    NO_MATCHING_FRAGMENT_CONDITION(11502),
    NO_MATCHING_COLLECTION_SEQUENCE(11503),
    NO_MATCHING_DOCUMENT_COLLECTION(11505),
    NO_MATCHING_LEXICON(11506),
    NO_MATCHING_LEXICON_EXPRESSION(11507),
    NO_MATCHING_POLICY_TYPE(11508),
    NO_MATCHING_POLICY(11509),
    NO_MATCHING_CONDITION(11510),
    NO_MATCHING_FIELD_LABEL(11511),
    NO_MATCHING_SEQUENCE_WORKFLOW(11512),

    DOCUMENT_COLLECTION_ALREADY_EXISTS(12001),

    POLICY_TYPE_SHORT_NAME_UNIQUE(12501),

    DOCUMENT_NULL_ID(13200),

    POLICY_TYPE_IN_USE(18000),
    CONDITION_IN_USE(18001),
    COLLECTION_IN_USE(18002),
    LEXICON_IN_USE(18003)
    ;


    private final Integer errorCode;

    private InvalidFieldValueErrors(Integer errorCode) {
        this.errorCode = errorCode;
    }

    @Override
    public String getKey() {
        return errorCode.toString();
    }

    @Override
    public String getResourceName() {
        return "invalidFieldValueErrors";
    }

    private static final Map<Integer, InvalidFieldValueErrors> intToTypeMap = new HashMap<>();

    static {
        for (InvalidFieldValueErrors type : InvalidFieldValueErrors.values()) {
            intToTypeMap.put(type.errorCode, type);
        }
    }

    public static InvalidFieldValueErrors fromErrorCode(int i) {
        InvalidFieldValueErrors type = intToTypeMap.get(Integer.valueOf(i));
        if (type == null)
            return InvalidFieldValueErrors.UNKNOWN;
        return type;
    }
}