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
 * @author getty
 */

/**
 * Enumeration which controls how the Update of an Object behaves when additional information like children are 
 * specified.
 * Default behaviour when this isn't specified is to REPLACE the children with those now specified.
 */
public enum UpdateBehaviourType {
    REPLACE, // Original Default Behaviour for Update Commands when children are specified
    ADD; // Add on the specified items instead of replacing them.// Add on the specified items instead of replacing them.

    @JsonCreator
    public static UpdateBehaviourType forValue( String value ) {
        try {
            return UpdateBehaviourType.valueOf( value.toUpperCase() );
        }
        catch ( Exception e ) {
            return null;
        }
    }

    @JsonValue
    public String toValue() {
        return this.name().toLowerCase();
    }
}
