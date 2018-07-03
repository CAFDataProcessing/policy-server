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

import com.github.cafdataprocessing.corepolicy.common.exceptions.excpetionErrors.MissingRequiredParameterErrors;
import com.github.cafdataprocessing.corepolicy.common.exceptions.excpetionErrors.LocalisedExceptionError;
import com.github.cafdataprocessing.corepolicy.common.shared.ErrorCodes;

import java.util.UUID;

/**
 * Exception to be thrown when a required parameter is missing.
 */
public class MissingRequiredParameterCpeException extends CpeException {
    public MissingRequiredParameterCpeException(MissingRequiredParameterErrors error, Throwable cause) {
        super(ErrorCodes.MISSING_REQUIRED_PARAMETERS, error, cause);
    }

    public MissingRequiredParameterCpeException(MissingRequiredParameterErrors error) {
        super(ErrorCodes.MISSING_REQUIRED_PARAMETERS, error);
    }

    public MissingRequiredParameterCpeException(LocalisedExceptionError error) {
        super(ErrorCodes.MISSING_REQUIRED_PARAMETERS, error);
    }

    public MissingRequiredParameterCpeException(LocalisedExceptionError error, UUID correlationCode) {
        super(ErrorCodes.MISSING_REQUIRED_PARAMETERS, error, correlationCode);
    }

    @Override
    protected String getPropertyFileName() {
        return "missingRequiredParameterMessage";
    }
}
