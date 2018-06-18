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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintViolation;
import javax.validation.ValidatorFactory;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Checks that provided object is valid.
 */
@Primary
@Component
public class ObjectValidator<Object> implements Validator<Object> {

    private ValidatorFactory validatorFactory;

    @Autowired
    public ObjectValidator(ValidatorFactory validatorFactory){
        this.validatorFactory = validatorFactory;
    }

    @Override
    public ValidationResult validate(Object objectToValidate) {
        Set<ConstraintViolation<Object>> validate = validatorFactory.getValidator().validate(objectToValidate);

        if(validate!= null && validate.size() > 0) {
            return new ValidationResult(validate.stream().map(ConstraintViolation::getMessage).distinct().collect(Collectors.joining("\n")));
        }
        return new ValidationResult();
    }
}
