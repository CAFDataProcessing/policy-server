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
package com.github.cafdataprocessing.corepolicy.common.exceptions.excpetionErrors;

/**
 * Possible error causes when exception is thrown due to missing parameters
 */
//Maybe we should change this to an object and allow the missing field to be specified in the constructor
public enum MissingRequiredParameterErrors implements ExceptionErrorsEnum {
    GroupByMissingFromRange(1),
    StartAndEndRequiredForGroupBy(2),
    PROJECT_ID_REQUIRED(3),
    TYPE_REQUIRED(4);

    private final Integer errorCode;

    private MissingRequiredParameterErrors(Integer errorCode) {
        this.errorCode = errorCode;
    }

    @Override
    public String getKey() {
        return errorCode.toString();
    }

    @Override
    public String getResourceName() {
        return "missingRequiredParameterErrors";
    }
}
