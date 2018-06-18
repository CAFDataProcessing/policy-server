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
package com.github.cafdataprocessing.corepolicy.common.exceptions;import com.github.cafdataprocessing.corepolicy.common.exceptions.excpetionErrors.BackEndRequestFailedErrors;import com.github.cafdataprocessing.corepolicy.common.exceptions.excpetionErrors.DataOperationFailureErrors;import com.github.cafdataprocessing.corepolicy.common.exceptions.excpetionErrors.LocalisedExceptionError;import com.github.cafdataprocessing.corepolicy.common.shared.ErrorCodes;import java.util.UUID;/** * Custom exception type for when exceptions occur accessing core policy backend. */public class BackEndRequestFailedCpeException extends CpeException {    public BackEndRequestFailedCpeException() {        super(ErrorCodes.GENERIC_ERROR, BackEndRequestFailedErrors.GeneralFailure);    }    public BackEndRequestFailedCpeException(Throwable cause) {        super(ErrorCodes.GENERIC_ERROR, BackEndRequestFailedErrors.GeneralFailure , cause);    }    public BackEndRequestFailedCpeException(BackEndRequestFailedErrors error) {        super(ErrorCodes.GENERIC_ERROR, error);    }    public BackEndRequestFailedCpeException(BackEndRequestFailedErrors error, Throwable cause) {        super(ErrorCodes.GENERIC_ERROR, error, cause);    }    public BackEndRequestFailedCpeException(DataOperationFailureErrors error) {        super(ErrorCodes.GENERIC_ERROR, error);    }    public BackEndRequestFailedCpeException(DataOperationFailureErrors error, Throwable cause) {        super(ErrorCodes.GENERIC_ERROR, error, cause);    }    public BackEndRequestFailedCpeException(LocalisedExceptionError error) {        super(ErrorCodes.GENERIC_ERROR, error);    }    public BackEndRequestFailedCpeException(LocalisedExceptionError error, UUID correlationCode) {        super(ErrorCodes.GENERIC_ERROR, error, correlationCode);    }    @Override    protected String getPropertyFileName() {        return "backEndRequestMessage";    }}