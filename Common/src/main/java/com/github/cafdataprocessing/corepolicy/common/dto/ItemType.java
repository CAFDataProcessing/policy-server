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
 *
 */
public enum ItemType {
    POLICY, POLICY_TYPE, COLLECTION_SEQUENCE, COLLECTION, CONDITION, FIELD_LABEL, LEXICON, LEXICON_EXPRESSION, SEQUENCE_WORKFLOW, SEQUENCE_WORKFLOW_ENTRY;

    @JsonCreator
    public static ItemType forValue(String value) {
        if(value == null) {
            return null;
        }
        return ItemType.valueOf(value.toUpperCase());
    }

    @JsonValue
    public String toValue() {
        return this.name().toLowerCase();
    }
}