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
package com.github.cafdataprocessing.corepolicy.policy.GenericPolicy;

import com.github.cafdataprocessing.corepolicy.common.PolicyApi;
import com.github.cafdataprocessing.corepolicy.common.dto.PolicyType;
import com.github.cafdataprocessing.corepolicy.common.dto.Policy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Implementation of the GenericPolicyConverter to handle a Generic Policy.
 */

@Component("GenericPolicy_Converter")
public class GenericPolicyConverterImpl implements GenericPolicyConverter {
    private final static Logger logger = LoggerFactory.getLogger(GenericPolicyConverterImpl.class);
    private final PolicyApi policyApi;

    private PolicyType policyType;

    @Autowired
    public GenericPolicyConverterImpl(PolicyApi policyApi) {
        this.policyApi = policyApi;
    }

    @Override
    public GenericPolicy convert(Policy policy) throws Exception {

        // we do need to validate what the policy has - but nothing more!
        validate(policy);

        return new GenericPolicy();
    }

    @Override
    public void validate(Policy policy) throws Exception {

        if (this.policyType == null) {
            // as the custom policies can have any internal_name we need to use ID, instead of internal_name
            this.policyType = this.policyApi.retrievePolicyType(policy.typeId);
        }

        // Validate the json in this policy against the schema held for the given policyType.
        policyApi.validate(policy, policyType);
    }

}
