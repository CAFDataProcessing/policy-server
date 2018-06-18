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
package com.github.cafdataprocessing.corepolicy.common.exceptions.excpetionErrors;/** * Defines errors that can occurs trying to access core policy backend */public enum BackEndRequestFailedErrors implements ExceptionErrorsEnum {    GeneralFailure(0),    DatabaseConnectionFailed(1),    ClassifyDocumentResultCannotBeNull(2),    UnableToAddTemporaryMetadata(3),    IncorrectConfiguration(4),    InvalidDataDetected(5),    ElasticsearchConnectionFailed(6),    CANNOT_SEND_TO_QUEUE(7),    CANT_CONNECT_TO_DATA_STORE(8)    ;    private final Integer errorCode;    BackEndRequestFailedErrors(Integer errorCode) {        this.errorCode = errorCode;    }    @Override    public String getKey() {        return errorCode.toString();    }    @Override    public String getResourceName() {        return "backEndRequestFailedErrors";    }}