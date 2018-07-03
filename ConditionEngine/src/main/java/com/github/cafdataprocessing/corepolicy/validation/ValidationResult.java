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
package com.github.cafdataprocessing.corepolicy.validation;

/**
 * Allows reporting of a reason for condition validation failure.
 */
public class ValidationResult {
    private String reason;
    private boolean valid;

    /**
     * Constructor that should be used if validation fails.
     * @param reason    The reason that the condition is not valid.
     */
    public ValidationResult(String reason) {
        this.reason = reason;
        this.valid = false;
    }

    /**
     * Default constructor, use for a successful validation.
     */
    public ValidationResult() {
        this.reason = null;
        this.valid = true;
    }

    public String getReason() {
        return reason;
    }

    public boolean isValid() {
        return valid;
    }
}
