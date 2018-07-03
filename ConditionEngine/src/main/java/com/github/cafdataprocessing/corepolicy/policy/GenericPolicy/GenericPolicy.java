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
package com.github.cafdataprocessing.corepolicy.policy.GenericPolicy;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * A generic policy implementation that performs no logic beyond indicating that processing should continue.
 */
@JsonIgnoreProperties
public class GenericPolicy{

    // as this isn't a metadata policy, we can only indicate to continue on processing for
    // custom policies.
    public boolean continueProcessing() { return true; }

}
