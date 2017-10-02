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
package com.github.cafdataprocessing.corepolicy.validation;

import com.google.common.base.Strings;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Tests if a string is a valid regex.
 */
@Component("RegexValidator")
public class RegexValidator implements Validator<String> {

    @Override
    public ValidationResult validate(String regexString) {
        if(Strings.isNullOrEmpty(regexString))
            return new ValidationResult("Regex must have a value.");

        try{
            //noinspection ResultOfMethodCallIgnored
            Pattern.compile(regexString);
        }
        catch (PatternSyntaxException e){
            return new ValidationResult("Regex was invalid. " + e.getMessage());
        }

        return new ValidationResult();
    }
}
