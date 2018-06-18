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
package com.github.cafdataprocessing.corepolicy.common.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enum that describes the possible values for the type of a field label.
 */
public enum FieldLabelType {
    STRING,
    DATE,
    NUMBER;

    /**
     * Creates FieldLabelType enum value from provided string.
     * @param value String representation of a FieldLabelType enum value.
     * @return Constructed FieldLabelType enum value. Null if invalid string representation passed.
     */
    @JsonCreator
    public static FieldLabelType forValue(String value) {
        try {
            return FieldLabelType.valueOf(value.toUpperCase());
        }
        catch (Exception e){
            return null;
        }
    }

    /**
     * Outputs enum value as a string.
     * @return String representation of the FieldLabelType.
     */
    @JsonValue
    public String toValue() {
        return this.name().toLowerCase();
    }
}
