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

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Use this as the base class for all validator tests
 */
public abstract class BaseValidatorTests<T> {

    @Before
    public void setup() throws Exception {

    }

    @After
    public void cleanup(){

    }

    @Test
    public void testForSuccess(){
        Validator<T> validator = getValidator();

        Collection<String> errors = new LinkedList<>();

        for(T testObject : getValidObjects()) {
            ValidationResult result = validator.validate(testObject);
            if(!result.isValid()){
                errors.add("Validation failed with error: " + result.getReason()
                        + System.lineSeparator()
                        + "Object: " + testObject.toString());
            }
        }

        if(errors.size() != 0){
            StringBuilder sb = new StringBuilder("Errors detected during validation.");

            for (String error : errors) {
                sb.append(System.lineSeparator());
                sb.append(error);
            }

            Assert.fail(sb.toString());
        }
    }

    @Test
    public void testForFailure(){
        Validator<T> validator = getValidator();

        Collection<String> successes = new LinkedList<>();

        for(T testObject : getInvalidObjects()) {
            ValidationResult result = validator.validate(testObject);
            if(result.isValid()){
                successes.add("Validation succeeded unexpectedly for object: " + testObject.toString());
            }
        }

        if(successes.size() != 0){
            StringBuilder sb = new StringBuilder("Some object that should have failed unexpectedly passed.");

            for (String msg : successes) {
                sb.append(System.lineSeparator());
                sb.append(msg);
            }

            Assert.fail(sb.toString());
        }
    }


    protected abstract Collection<T> getValidObjects();

    protected abstract Collection<T> getInvalidObjects();

    protected abstract Validator<T> getValidator();
}
