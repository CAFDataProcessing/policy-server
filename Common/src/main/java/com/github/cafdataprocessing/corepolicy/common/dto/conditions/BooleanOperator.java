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
package com.github.cafdataprocessing.corepolicy.common.dto.conditions;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines available boolean operators for use in conditions.
 */
public enum BooleanOperator { AND, OR;

    @JsonCreator
    public static BooleanOperator forValue(String value) {
        try {
            return BooleanOperator.valueOf(value.toUpperCase());
        }
        catch (Exception e){
            return null;
        }
    }

    @JsonValue
    public String toValue() {
        return this.name().toLowerCase();
    }
}
