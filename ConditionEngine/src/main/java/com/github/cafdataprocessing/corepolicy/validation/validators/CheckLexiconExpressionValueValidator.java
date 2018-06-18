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
package com.github.cafdataprocessing.corepolicy.validation.validators;

import com.github.cafdataprocessing.corepolicy.common.dto.LexiconExpression;
import com.github.cafdataprocessing.corepolicy.validation.AgentExpressionValidator;
import com.github.cafdataprocessing.corepolicy.validation.RegexValidator;
import com.github.cafdataprocessing.corepolicy.validation.ValidationResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Checks a lexicon expression value is valid
 */
@Component
public class CheckLexiconExpressionValueValidator implements ConstraintValidator<CheckLexiconExpressionValue, LexiconExpression> {

    private AgentExpressionValidator agentExpressionValidator;
    private RegexValidator regexValidator;

    @Autowired
    public CheckLexiconExpressionValueValidator(AgentExpressionValidator agentExpressionValidator, RegexValidator regexValidator) {
        this.agentExpressionValidator = agentExpressionValidator;
        this.regexValidator = regexValidator;
    }

    @Override
    public void initialize(CheckLexiconExpressionValue constraintAnnotation) {

    }

    @Override
    public boolean isValid(LexiconExpression value, ConstraintValidatorContext context) {
        if(value.type == null){
            return false;
        }

        ValidationResult validationResult = null;
        switch (value.type) {
            case REGEX:
                validationResult = regexValidator.validate(value.expression);
                break;
            case TEXT:
                validationResult = agentExpressionValidator.validate(value.expression);
                break;
        }
        return validationResult != null && validationResult.isValid();
    }
}
