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
package com.github.cafdataprocessing.corepolicy.repositories;

import com.github.cafdataprocessing.corepolicy.common.PolicyApi;
import com.github.cafdataprocessing.corepolicy.common.dto.*;
import com.github.cafdataprocessing.corepolicy.common.exceptions.BackEndRequestFailedCpeException;
import com.github.cafdataprocessing.corepolicy.common.exceptions.CpeException;
import com.github.cafdataprocessing.corepolicy.common.exceptions.InvalidFieldValueCpeException;
import com.github.cafdataprocessing.corepolicy.common.exceptions.excpetionErrors.DataOperationFailureErrors;
import com.github.cafdataprocessing.corepolicy.common.exceptions.excpetionErrors.InvalidFieldValueErrors;
import com.github.cafdataprocessing.corepolicy.repositories.v2.CollectionRepository;
import com.github.cafdataprocessing.corepolicy.repositories.v2.ExecutionContext;
import com.github.cafdataprocessing.corepolicy.repositories.v2.PolicyRepository;
import com.github.cafdataprocessing.corepolicy.repositories.v2.PolicyTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collection;

import static com.github.cafdataprocessing.corepolicy.common.shared.JsonValidation.validateJson;

/**
 *
 */
@Component
public class PolicyApiRepositoryImpl implements PolicyApi {
    private PolicyRepository policyRepository;
    private PolicyTypeRepository policyTypeRepository;
    private CollectionRepository collectionRepository;

    private final ExecutionContextProvider executionContextProvider;


    @Autowired
    public PolicyApiRepositoryImpl(PolicyRepository policyRepository,
                                   PolicyTypeRepository policyTypeRepository,
                                   @Qualifier("repositoryExecutionContextProvider") ExecutionContextProvider executionContextProvider,
                                   CollectionRepository collectionRepository) {

        this.policyRepository = policyRepository;
        this.policyTypeRepository = policyTypeRepository;
        this.executionContextProvider = executionContextProvider;
        this.collectionRepository = collectionRepository;
    }

    //Policy
    @Override
    public Policy create(Policy t) {

        try (ExecutionContext executionContext = getExecutionContext()) {

            // Before we try and create a policy of this type, validate its schema against
            // definition held.
            PolicyType policyType = retrievePolicyType(t.typeId);

            if (policyType == null) {
                // policy type is required, to validate against, throw if we can't find one.
                throw new RuntimeException("PolicyType: " + t.name + " by id: " + t.typeId + " does not exist.");
            }

            validate(t, policyType);

            return executionContext.retry(r -> policyRepository.create(executionContext, t));
        } catch (CpeException e) {
            throw e;
        } catch (Exception e) {
            throw new BackEndRequestFailedCpeException(new DataOperationFailureErrors(DataOperationFailureErrors.FailureOperation.CREATE, ItemType.POLICY), e);
        }
    }

    @Override
    public Policy update(Policy t) {
        try (ExecutionContext executionContext = getExecutionContext()) {

            // Before we try and create a policy of this type, validate its schema against
            // definition held.
            validatePolicyTypeId(t.typeId);
            PolicyType policyType = retrievePolicyType(t.typeId);

            if (policyType == null) {
                //todo PD-648
                throw new RuntimeException("PolicyType: " + t.name + " by id: " + t.typeId + " does not exist.");
            }

            validate(t, policyType);

            return executionContext.retry(r -> policyRepository.update(executionContext, t));
        } catch (CpeException e) {
            throw e;
        } catch (Exception e) {
            throw new BackEndRequestFailedCpeException(new DataOperationFailureErrors(DataOperationFailureErrors.FailureOperation.UPDATE, ItemType.POLICY), e);
        }
    }

    @Override
    public void deletePolicy(Long id) {
        validatePolicyId(id);
        try (ExecutionContext executionContextConditionEngine = executionContextProvider.getExecutionContext(RepositoryType.CONDITION_ENGINE)) {
            Collection<Long> retrievedPolicies = executionContextConditionEngine.retry(r -> collectionRepository.getCollectionIdsForPolicy(executionContextConditionEngine, id));
            if (!retrievedPolicies.isEmpty()) {
                throw new RuntimeException("Policy is assigned to a collection");
            }
        } catch (CpeException e) {
            throw e;
        } catch (Exception e) {
            throw new BackEndRequestFailedCpeException(new DataOperationFailureErrors(DataOperationFailureErrors.FailureOperation.DELETE, ItemType.POLICY), e);
        }


        try (ExecutionContext executionContext = getExecutionContext()) {
            executionContext.retryNoReturn(r -> policyRepository.delete(executionContext, id));
        } catch (CpeException e) {
            throw e;
        } catch (Exception e) {
            throw new BackEndRequestFailedCpeException(new DataOperationFailureErrors(DataOperationFailureErrors.FailureOperation.DELETE, ItemType.POLICY), e);
        }
    }

    @Override
    public Policy retrievePolicy(Long id) {
        Collection<Policy> policies = retrievePolicies(Arrays.asList(id));
        if (policies.isEmpty()) return null;
        return policies.stream().findFirst().get();
    }

    @Override
    public PageOfResults<Policy> retrievePoliciesPage(PageRequest pageRequest) {
        try (ExecutionContext executionContext = getExecutionContext()) {
            return executionContext.retryNonTransactional(r -> policyRepository.retrievePage(executionContext, pageRequest));
        } catch (Exception e) {
            throw new BackEndRequestFailedCpeException(new DataOperationFailureErrors(DataOperationFailureErrors.FailureOperation.RETRIEVE, ItemType.POLICY), e);
        }
    }

    @Override
    public PageOfResults<Policy> retrievePoliciesPage(PageRequest pageRequest, Filter filter) {

        try (ExecutionContext executionContext = getExecutionContext()) {
            return executionContext.retryNonTransactional(r -> policyRepository.retrievePage(executionContext, pageRequest, filter));
        } catch (CpeException e) {
            throw e;
        } catch (Exception e) {
            throw new BackEndRequestFailedCpeException(new DataOperationFailureErrors(DataOperationFailureErrors.FailureOperation.RETRIEVE, ItemType.POLICY), e);
        }

    }

    @Override
    public Collection<Policy> retrievePolicies(Collection<Long> ids) {
        validatePolicyId(ids);
        try (ExecutionContext executionContext = getExecutionContext()) {
            return executionContext.retryNonTransactional(r -> policyRepository.retrieve(executionContext, ids));
        } catch (CpeException e) {
            throw e;
        } catch (Exception e) {
            throw new BackEndRequestFailedCpeException(new DataOperationFailureErrors(DataOperationFailureErrors.FailureOperation.RETRIEVE, ItemType.POLICY), e);
        }
    }

    @Override
    public PolicyType create(PolicyType t) {
        try (ExecutionContext executionContext = getExecutionContext()) {
            return executionContext.retry(r -> policyTypeRepository.create(executionContext, t));
        } catch (CpeException e) {
            throw e;
        } catch (Exception e) {
            throw new BackEndRequestFailedCpeException(new DataOperationFailureErrors(DataOperationFailureErrors.FailureOperation.CREATE, ItemType.POLICY_TYPE), e);
        }
    }

    @Override
    public PolicyType update(PolicyType t) {
        try (ExecutionContext executionContext = getExecutionContext()) {
            return executionContext.retry(r -> policyTypeRepository.update(executionContext, t));
        } catch (CpeException e) {
            throw e;
        } catch (Exception e) {
            throw new BackEndRequestFailedCpeException(new DataOperationFailureErrors(DataOperationFailureErrors.FailureOperation.UPDATE, ItemType.POLICY_TYPE), e);
        }
    }

    @Override
    public void deletePolicyType(Long id) {
        validatePolicyTypeId(id);
        try (ExecutionContext executionContext = getExecutionContext()) {
            executionContext.retryNoReturn(r -> policyTypeRepository.delete(executionContext, id));
        } catch (CpeException e) {
            throw e;
        } catch (Exception e) {
            throw new BackEndRequestFailedCpeException(new DataOperationFailureErrors(DataOperationFailureErrors.FailureOperation.DELETE, ItemType.POLICY_TYPE), e);
        }
    }

    @Override
    public PolicyType retrievePolicyType(Long id) {
        Collection<PolicyType> policyTypes = retrievePolicyTypes(Arrays.asList(id));
        if (policyTypes.isEmpty()) return null;
        return policyTypes.stream().findFirst().get();
    }

    @Override
    public PageOfResults<PolicyType> retrievePolicyTypesPage(PageRequest pageRequest) {
        try (ExecutionContext executionContext = getExecutionContext()) {
            return executionContext.retryNonTransactional(r -> policyTypeRepository.retrievePage(executionContext, pageRequest));
        } catch (CpeException e) {
            throw e;
        } catch (Exception e) {
            throw new BackEndRequestFailedCpeException(new DataOperationFailureErrors(DataOperationFailureErrors.FailureOperation.RETRIEVE, ItemType.POLICY_TYPE), e);
        }
    }

    @Override
    public PageOfResults<PolicyType> retrievePolicyTypesPage(PageRequest pageRequest, Filter filter) {
        try (ExecutionContext executionContext = getExecutionContext()) {
            return executionContext.retryNonTransactional(r -> policyTypeRepository.retrievePage(executionContext, pageRequest, filter));
        } catch (CpeException e) {
            throw e;
        } catch (Exception e) {
            throw new BackEndRequestFailedCpeException(new DataOperationFailureErrors(DataOperationFailureErrors.FailureOperation.RETRIEVE, ItemType.POLICY_TYPE), e);
        }
    }

    @Override
    public Collection<PolicyType> retrievePolicyTypes(Collection<Long> ids) {
        validatePolicyTypeId(ids);
        try (ExecutionContext executionContext = getExecutionContext()) {
            return executionContext.retryNonTransactional(r -> policyTypeRepository.retrieve(executionContext, ids));
        } catch (CpeException e) {
            throw e;
        } catch (Exception e) {
            throw new BackEndRequestFailedCpeException(new DataOperationFailureErrors(DataOperationFailureErrors.FailureOperation.RETRIEVE, ItemType.POLICY_TYPE), e);
        }
    }

    @Override
    public PolicyType retrievePolicyTypeByName(String name) {
        try (ExecutionContext executionContext = getExecutionContext()) {
            return executionContext.retryNonTransactional(r -> policyTypeRepository.retrieve(executionContext, name));
        } catch (CpeException e) {
            throw e;
        } catch (Exception e) {
            throw new BackEndRequestFailedCpeException(new DataOperationFailureErrors(DataOperationFailureErrors.FailureOperation.RETRIEVE, ItemType.POLICY_TYPE), e);
        }
    }

    @Override
    public void validate(Policy policy, PolicyType policyType) {
        try {
            validateJson(policy.details, policyType.definition);
        } catch (CpeException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidFieldValueCpeException(InvalidFieldValueErrors.POLICY_DETAILS_INVALID, e);
        }
    }

    private ExecutionContext getExecutionContext() {
        return executionContextProvider.getExecutionContext(RepositoryType.POLICY);
    }

    private void validatePolicyTypeId(Long id) {
        if (id == null) {
            throw new InvalidFieldValueCpeException(InvalidFieldValueErrors.POLICY_TYPE_ID_REQUIRED);
        }
    }

    private void validatePolicyTypeId(Collection<Long> ids) {
        if (ids == null || ids.stream().anyMatch(u -> u == null)) {
            throw new InvalidFieldValueCpeException(InvalidFieldValueErrors.POLICY_TYPE_ID_REQUIRED);
        }
    }


    private void validatePolicyId(Long id) {
        if (id == null) {
            throw new InvalidFieldValueCpeException(InvalidFieldValueErrors.POLICY_ID_REQUIRED);
        }
    }

    private void validatePolicyId(Collection<Long> ids) {
        if (ids == null || ids.stream().anyMatch(u -> u == null)) {
            throw new InvalidFieldValueCpeException(InvalidFieldValueErrors.POLICY_ID_REQUIRED);
        }
    }
}
