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
package com.github.cafdataprocessing.corepolicy.common.dto.conditions;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * String value of these is used when inserting to the db, so changes here need to be in sync with what's in the
 * condition add sp.
 *
 * The string value of these is used in the db, and the lower case is used for serializing to client json, so don't
 * change the values unless you know what you're doing!
 */
public enum ConditionType {
    NUMBER,
    NOT,
    LEXICON,
    EXISTS,
    DATE,
    BOOLEAN,
    TEXT,
    REGEX,
    STRING,
    FRAGMENT,
    ENTITY;

    @JsonCreator
    public static ConditionType forValue(String value) {
        try {
            return ConditionType.valueOf(value.toUpperCase());
        }
        catch (Exception e){
            return null;
        }
    }

    @JsonValue
    public String toValue() {
        return this.name().toLowerCase();
    }


    public static Class<?> getConditionClass(String conditionType) {
        return getConditionClass(ConditionType.forValue(conditionType));
    }

    /**
     * get the real concrete condition class, based on this ConditionType
     * @param conditionType
     * @return
     */
    public static Class<?> getConditionClass(ConditionType conditionType) {

        switch (conditionType) {
            case NOT: {
                return NotCondition.class;
            }
            case NUMBER: {
                return NumberCondition.class;
            }
            case LEXICON: {
                return LexiconCondition.class;
            }
            case EXISTS: {
                return ExistsCondition.class;
            }
            case DATE: {
                return DateCondition.class;
            }
            case BOOLEAN: {
                return BooleanCondition.class;
            }
            case TEXT: {
                return TextCondition.class;
            }
            case REGEX: {
                return RegexCondition.class;
            }
            case FRAGMENT: {
                return FragmentCondition.class;
            }
            case STRING: {
                return StringCondition.class;
            }
            default: {
                throw new RuntimeException(conditionType.toValue());
            }
        }
    }
}
