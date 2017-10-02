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
package com.github.cafdataprocessing.corepolicy.common;

import com.github.cafdataprocessing.corepolicy.common.dto.*;

import java.util.Collection;

/**
 *
 */
public interface PolicyApi {
    //Policy
    Policy create(Policy t);

    Policy update(Policy t);

    void deletePolicy(Long id);

    Policy retrievePolicy(Long id);

    PageOfResults<Policy> retrievePoliciesPage(PageRequest pageRequest);
    PageOfResults<Policy> retrievePoliciesPage( PageRequest pageRequest, Filter filter );

    Collection<Policy> retrievePolicies(Collection<Long> ids);


    PolicyType create(PolicyType t);

    PolicyType update(PolicyType t);

    void deletePolicyType(Long id);

    PolicyType retrievePolicyType(Long id);

    PageOfResults<PolicyType> retrievePolicyTypesPage(PageRequest pageRequest);
    PageOfResults<PolicyType> retrievePolicyTypesPage( PageRequest pageRequest, Filter filter );

    Collection<PolicyType> retrievePolicyTypes(Collection<Long> ids);

    PolicyType retrievePolicyTypeByName(String name);

    void validate( Policy policy, PolicyType policyType ) throws Exception;
}
