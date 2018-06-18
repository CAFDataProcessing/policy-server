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
package com.github.cafdataprocessing.corepolicy.common.exceptions;

import com.github.cafdataprocessing.corepolicy.common.exceptions.excpetionErrors.DataOperationFailureErrors;
import com.github.cafdataprocessing.corepolicy.common.exceptions.excpetionErrors.InvalidFieldValueErrors;
import com.github.cafdataprocessing.corepolicy.common.exceptions.excpetionErrors.UnknownFieldError;
import com.github.cafdataprocessing.corepolicy.common.shared.ErrorCodes;
import com.github.cafdataprocessing.corepolicy.common.exceptions.excpetionErrors.LocalisedExceptionError;

import java.util.UUID;

/**
 * Exception to be thrown when an invalid field value is encountered
 */
public class InvalidFieldValueCpeException extends CpeException {

    public InvalidFieldValueCpeException(InvalidFieldValueErrors error) {
        super(ErrorCodes.INVALID_FIELD_VALUE, error);
    }

    public InvalidFieldValueCpeException(InvalidFieldValueErrors error, Throwable cause) {
        super(ErrorCodes.INVALID_FIELD_VALUE, error, cause);
    }

    public InvalidFieldValueCpeException(DataOperationFailureErrors error) {
        super(ErrorCodes.INVALID_FIELD_VALUE, error);
    }

    public InvalidFieldValueCpeException(DataOperationFailureErrors error, Throwable cause) {
        super(ErrorCodes.INVALID_FIELD_VALUE, error, cause);
    }

    public InvalidFieldValueCpeException(LocalisedExceptionError error) {
        super(ErrorCodes.INVALID_FIELD_VALUE, error);
    }

    public InvalidFieldValueCpeException(LocalisedExceptionError error, UUID correlationCode) {
        super(ErrorCodes.INVALID_FIELD_VALUE, error, correlationCode);
    }

    public InvalidFieldValueCpeException(UnknownFieldError error) {
        super(ErrorCodes.INVALID_FIELD_VALUE, error);
    }

    public InvalidFieldValueCpeException(UnknownFieldError error, Throwable cause) {
        super(ErrorCodes.INVALID_FIELD_VALUE, error, cause);
    }

    @Override
    protected String getPropertyFileName() {
        return "invalidFieldValueMessage";
    }
}
