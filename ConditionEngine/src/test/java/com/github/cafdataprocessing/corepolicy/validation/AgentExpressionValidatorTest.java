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

import com.github.cafdataprocessing.corepolicy.booleanagent.BooleanAgentServices;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collection;
import java.util.LinkedList;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

/**
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class AgentExpressionValidatorTest extends BaseValidatorTests<String> {

    @Mock
    BooleanAgentServices booleanAgentServices;

    @Before
    @Override
    public void setup() throws Exception {
        super.setup();
        doThrow(new RuntimeException("This is wrong buddy!")).when(booleanAgentServices).isValidExpression(eq("(cat near dog"));
        when(booleanAgentServices.getAvailable()).thenReturn(true);
    }

    @Override
    protected Collection<String> getValidObjects() {
        LinkedList<String> list = new LinkedList<>();
        list.add("cat near dog");
        return list;
    }

    @Override
    protected Collection<String> getInvalidObjects() {
        LinkedList<String> list = new LinkedList<>();
        list.add("(cat near dog");
        return list;
    }

    @Override
    protected Validator<String> getValidator() {
        return new AgentExpressionValidator(booleanAgentServices);
    }
}

