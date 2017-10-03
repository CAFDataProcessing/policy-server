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
package com.github.cafdataprocessing.corepolicy.common.exceptions.excpetionErrors;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Error representing an unknown field.
 */
public class UnknownFieldError implements ExceptionErrors {

    private String unknownField;

    public UnknownFieldError(String unknownField) {
        this.unknownField = unknownField;
    }

    @Override
    public String getMessage(Locale locale) {
        ResourceBundle bundle = ResourceBundle.getBundle("localisation.exceptions.exceptionErrors." + getResourceName(), locale);
        MessageFormat format = new MessageFormat(bundle.getString("messageFormat"));
        String[] args = {unknownField};
        return format.format(args);
    }

    @Override
    public String getResourceName() {
        return "unknownFieldError";
    }
}
