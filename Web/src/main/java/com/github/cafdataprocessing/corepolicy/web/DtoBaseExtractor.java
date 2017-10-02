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
package com.github.cafdataprocessing.corepolicy.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.github.cafdataprocessing.corepolicy.common.dto.DtoBase;
import com.github.cafdataprocessing.corepolicy.common.exceptions.ConditionEngineException;
import com.github.cafdataprocessing.corepolicy.common.exceptions.InvalidFieldValueCpeException;
import com.github.cafdataprocessing.corepolicy.common.exceptions.excpetionErrors.UnknownFieldError;

/**
 * There might well be a way to plug this into the mvc pipeline but I can't spend anymore time finding it
 */
public class DtoBaseExtractor extends BaseExtractor<DtoBase> {

    @Override
    protected DtoBase convert(JsonNode jsonNode) {

        try {
            return mapper.treeToValue(jsonNode, DtoBase.class);
        }
        catch (UnrecognizedPropertyException e) {
            throw new InvalidFieldValueCpeException(new UnknownFieldError(e.getPropertyName()), e);
        }
        catch (JsonProcessingException e) {
            throw new ConditionEngineException(new RuntimeException(e));
        }
    }
}

