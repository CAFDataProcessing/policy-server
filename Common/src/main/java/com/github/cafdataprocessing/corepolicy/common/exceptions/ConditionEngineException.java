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
package com.github.cafdataprocessing.corepolicy.common.exceptions;

/**
 * Use one of the cpeExceptions
 */
@Deprecated
public class ConditionEngineException extends RuntimeException {
    private int errorCode;

    public ConditionEngineException(Throwable cause) {
        this(5000, cause);
    }

    public ConditionEngineException(int errorCode, Throwable cause){
        super(cause);
        this.errorCode = errorCode;
    }

    public ConditionEngineException(int errorCode, String message, Throwable cause){
        super(message, cause);
        this.errorCode = errorCode;
    }

    public ConditionEngineException(int errorCode, String cause){
        super(cause);
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return errorCode;
    }
}
