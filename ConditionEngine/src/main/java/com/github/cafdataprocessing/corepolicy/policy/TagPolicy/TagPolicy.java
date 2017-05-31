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
package com.github.cafdataprocessing.corepolicy.policy.TagPolicy;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.cafdataprocessing.corepolicy.domainModels.FieldAction;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Defines a Tag Policy which accepts an assortment of field actions to perform.
 */
public class TagPolicy {

    @JsonProperty("fieldActions")
    private Collection<FieldAction> fieldActions = new ArrayList<>();

    public Collection<FieldAction> getFieldActions() {
        return fieldActions;
    }

    public void setFieldActions(Collection<FieldAction> fieldActions) {
        this.fieldActions = fieldActions;
    }
}
