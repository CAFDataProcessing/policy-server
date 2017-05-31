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
package com.github.cafdataprocessing.corepolicy.api;

import com.github.cafdataprocessing.corepolicy.common.ApiProperties;
import com.github.cafdataprocessing.corepolicy.common.PolicyApi;
import com.github.cafdataprocessing.corepolicy.common.dto.*;
import com.github.cafdataprocessing.corepolicy.common.dtoweb.RetrieveAdditional;
import com.github.cafdataprocessing.corepolicy.common.dtoweb.RetrieveRequest;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

import static com.github.cafdataprocessing.corepolicy.common.shared.JsonValidation.validateJson;

/**
 * Base class for any web implementation of the PolicyApi interface.
 */
public abstract class PolicyApiWebBase extends WebApiBase implements PolicyApi {
    private static final Logger logger = LoggerFactory.getLogger(PolicyApiWebBase.class);

    public PolicyApiWebBase(ApiProperties apiProperties) {
        super(apiProperties);
    }

    protected Logger getLogger() {
        return logger;
    }
    @Override
    public Policy create(Policy policy) {
        return  makeSingleRequest(WebApiAction.CREATE,policy, DtoBase.class);
    }

    @Override
    public Policy update(Policy policy) {
        return makeSingleRequest(WebApiAction.UPDATE,policy, DtoBase.class);
    }

    @Override
    public void deletePolicy(Long id) {
        makeDeleteRequest(id, ItemType.POLICY);
    }

    @Override
    public Policy retrievePolicy(Long id) {
        return retrievePolicies(Arrays.asList(id)).stream().findFirst().get();
    }

    @Override
    public PageOfResults<Policy> retrievePoliciesPage(PageRequest pageRequest) {
        return getRelatedItemPageOfResults(pageRequest, ItemType.POLICY, null, null, Policy.class);
    }

    @Override
    public PageOfResults<Policy> retrievePoliciesPage(PageRequest pageRequest, Filter filter) {

        return getRelatedItemPageOfResults(pageRequest, ItemType.POLICY, filter, Policy.class);
    }

    @Override
    public Collection<Policy> retrievePolicies(Collection<Long> ids) {
        RetrieveRequest retrieveRequest = new RetrieveRequest();
        retrieveRequest.type = ItemType.POLICY;
        retrieveRequest.id = ids;

        final Collection<NameValuePair> params = createParams(retrieveRequest);

        return makeMultipleRequest(WebApiAction.RETRIEVE, params, Policy.class);
    }

    @Override
    public PolicyType create(PolicyType policyType) {
        return makeSingleRequest(WebApiAction.CREATE, policyType, DtoBase.class);
    }

    @Override
    public PolicyType update(PolicyType policyType) {
        return makeSingleRequest(WebApiAction.UPDATE, policyType, DtoBase.class);
    }

    @Override
    public void deletePolicyType(Long id) {
        makeDeleteRequest(id, ItemType.POLICY_TYPE);
    }

    @Override
    public PolicyType retrievePolicyType(Long id) {
        Collection<NameValuePair> params = new LinkedList<>();
        params.add(new BasicNameValuePair("id", String.valueOf(id)));
        params.add(new BasicNameValuePair("type", ItemType.POLICY_TYPE.toValue()));
        return makeMultipleRequest(WebApiAction.RETRIEVE, params, PolicyType.class).stream().findFirst().get();
    }

    @Override
    public PageOfResults<PolicyType> retrievePolicyTypesPage(PageRequest pageRequest) {
        RetrieveRequest retrieveRequest = new RetrieveRequest();
        retrieveRequest.type = ItemType.POLICY_TYPE;
        retrieveRequest.max_page_results = pageRequest.max_page_results;
        retrieveRequest.start = pageRequest.start;
        final Collection<NameValuePair> params = createParams(retrieveRequest);

        return makePagedRequest(WebApiAction.RETRIEVE, params, PolicyType.class);
    }

    @Override
    public PageOfResults<PolicyType> retrievePolicyTypesPage(PageRequest pageRequest, Filter filter) {

        return getRelatedItemPageOfResults(pageRequest, ItemType.POLICY_TYPE, filter, PolicyType.class);
    }

    @Override
    public Collection<PolicyType> retrievePolicyTypes(Collection<Long> ids) {
        RetrieveRequest retrieveRequest = new RetrieveRequest();
        retrieveRequest.id = ids;
        retrieveRequest.type = ItemType.POLICY_TYPE;
        final Collection<NameValuePair> params = createParams(retrieveRequest);

        return makeMultipleRequest(WebApiAction.RETRIEVE, params, PolicyType.class);
    }

    @Override
    public PolicyType retrievePolicyTypeByName(String name) {

        RetrieveRequest retrieveRequest = new RetrieveRequest();
        retrieveRequest.type = ItemType.POLICY_TYPE;

        retrieveRequest.additional = new RetrieveAdditional();
        retrieveRequest.additional.name = name;

        final Collection<NameValuePair> params = createParams(retrieveRequest);

        Collection<PolicyType> policyTypes = makeMultipleRequest(WebApiAction.RETRIEVE, params, PolicyType.class);
        if (policyTypes.size() > 0) {
            return policyTypes.stream().findFirst().get();
        }

        throw new RuntimeException("More than one policy type found for short name: " + name);
    }

    @Override
    public void validate(Policy policy, PolicyType policyType) throws Exception {
        validateJson(policy.details, policyType.definition);
    }
}
