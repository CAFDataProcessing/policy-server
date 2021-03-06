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

import com.github.cafdataprocessing.corepolicy.validation.AgentExpressionValidator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Used to validate expressions against a set boolean agent validator
 */
@Component
public class CheckAgentValidator implements ConstraintValidator<CheckAgent, String> {

    private AgentExpressionValidator agentExpressionValidator;

    @Autowired
    public CheckAgentValidator(AgentExpressionValidator agentExpressionValidator) {
        this.agentExpressionValidator = agentExpressionValidator;
    }

    @Override
    public void initialize(CheckAgent constraintAnnotation) {
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if(value != null && StringUtils.isNotBlank(value)) {
            return agentExpressionValidator.validate(value).isValid();
        }
        return false;
    }
}
