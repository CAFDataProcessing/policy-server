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
package com.github.cafdataprocessing.corepolicy.policy.MetadataPolicy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.cafdataprocessing.corepolicy.common.PolicyApi;
import com.github.cafdataprocessing.corepolicy.common.dto.Policy;
import com.github.cafdataprocessing.corepolicy.common.dto.PolicyType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component("MetadataPolicy_Converter")
public class MetadataPolicyConverterImpl implements MetadataPolicyConverter {
    private final static Logger logger = LoggerFactory.getLogger(MetadataPolicyConverterImpl.class);
    private final PolicyApi policyApi;

    private PolicyType policyType;

    @Autowired
    public MetadataPolicyConverterImpl(PolicyApi policyApi){
        this.policyApi = policyApi;
    }

    @Override
    public MetadataPolicy convert(Policy policy) throws Exception {

        final ObjectMapper objectMapper = new ObjectMapper();
        validate(policy);
        final MetadataPolicy metadataPolicy = objectMapper.treeToValue(policy.details, MetadataPolicy.class);
        return metadataPolicy;
    }

    @Override
    public void validate(Policy policy) throws Exception {

        if(this.policyType==null){
            // Policy type is either setup by Id or by using our Internal name.
            // In the case of the MetadataPolicyConvertor its name must be what is
            // in front of the JSON property- x_Convertor prefix. e.g. MetadataPolicy_Converter
            this.policyType = this.policyApi.retrievePolicyType( policy.typeId );
        }

        // Validate the json in this policy against the schema held for the given policyType.
        policyApi.validate( policy, policyType );
    }

}
