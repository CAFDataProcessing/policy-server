/*
 * Copyright 2015-2017 Hewlett Packard Enterprise Development LP.
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

import com.github.cafdataprocessing.corepolicy.booleanagent.BooleanAgentServices;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Class that checks if a string is a valid content expression to use with the configured boolean agent service.
 */
@Component("AgentExpressionValidator")
public class AgentExpressionValidator implements Validator<String>{

    /*
    according to http://www.utf8-chartable.de/unicode-utf8-table.pl
    U+FFFF is the largest codepoint that is represented by 3 bytes in UTF-8.
    */
    private final static int MAX_THREE_BYTE_CODEPOINT = Integer.parseInt("FFFF", 16);
    private final static Logger logger = LoggerFactory.getLogger(AgentExpressionValidator.class);
    private final BooleanAgentServices booleanAgentServices;

    @Autowired
    public AgentExpressionValidator(BooleanAgentServices booleanAgentServices) {
        this.booleanAgentServices = booleanAgentServices;
    }


    @Override
    public ValidationResult validate(String stringToValidate) {
        if(Strings.isNullOrEmpty(stringToValidate))
            return new ValidationResult("Content expression cannot be empty.");

        //verify no 4 byte UTF8 characters
        for (int i = 0; i < stringToValidate.length(); i++) {
            int codepoint = stringToValidate.codePointAt(i);
            if(codepoint > MAX_THREE_BYTE_CODEPOINT) {
                return new ValidationResult("Agent expression invalid - codepoint " + codepoint +
                        " is greater than max allowed " + MAX_THREE_BYTE_CODEPOINT + ".");
            }
        }

        if(!booleanAgentServices.getAvailable()){
            throw new RuntimeException("Agent service is not available to validate expressions.");
        }

        try{
            booleanAgentServices.isValidExpression(stringToValidate);
        } catch (Exception e) {
            logger.error("Error testing agent expression using documentstats :" + stringToValidate, e);
            return new ValidationResult("Content expression '" + stringToValidate + "' is not valid.");
        }

        return new ValidationResult();
    }
}
