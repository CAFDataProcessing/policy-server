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
package com.github.cafdataprocessing.corepolicy.repositories;

import com.github.cafdataprocessing.corepolicy.common.AdminApi;
import com.github.cafdataprocessing.corepolicy.common.ApiProperties;
import com.github.cafdataprocessing.corepolicy.common.dto.ItemType;
import com.github.cafdataprocessing.corepolicy.common.dto.PolicyType;
import com.github.cafdataprocessing.corepolicy.common.exceptions.BackEndRequestFailedCpeException;
import com.github.cafdataprocessing.corepolicy.common.exceptions.CpeException;
import com.github.cafdataprocessing.corepolicy.common.exceptions.InvalidFieldValueCpeException;
import com.github.cafdataprocessing.corepolicy.common.exceptions.excpetionErrors.DataOperationFailureErrors;
import com.github.cafdataprocessing.corepolicy.common.exceptions.excpetionErrors.InvalidFieldValueErrors;
import com.github.cafdataprocessing.corepolicy.repositories.v2.ExecutionContext;
import com.github.cafdataprocessing.corepolicy.repositories.v2.PolicyTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * Implementation of AdminApi at repository level
 */
@Component
public class AdminApiRepositoryImpl implements AdminApi {

    private final ExecutionContextProvider executionContextProvider;
    private final PolicyTypeRepository policyTypeRepository;
    private final ApiProperties apiProperties;

    @Autowired
    public AdminApiRepositoryImpl(PolicyTypeRepository policyTypeRepository,
                                   @Qualifier("repositoryExecutionContextProvider") ExecutionContextProvider executionContextProvider,
                                  ApiProperties apiProperties ) {


        this.policyTypeRepository = policyTypeRepository;
        this.executionContextProvider = executionContextProvider;
        this.apiProperties = apiProperties;
    }

    @Override
    public PolicyType create(PolicyType t) {

        validateAllowAdministration();

        try (ExecutionContext executionContext = getExecutionContext()) {
            return executionContext.retry(r -> policyTypeRepository.create(executionContext, t, true));
        } catch (CpeException e) {
            throw e;
        } catch (Exception e) {
            throw new BackEndRequestFailedCpeException(new DataOperationFailureErrors(DataOperationFailureErrors.FailureOperation.CREATE, ItemType.POLICY_TYPE), e);
        }
    }

    @Override
    public PolicyType update(PolicyType t) {

        validateAllowAdministration();

        try (ExecutionContext executionContext = getExecutionContext()) {
            return executionContext.retry(r -> policyTypeRepository.update(executionContext, t, true));
        } catch (CpeException e) {
            throw e;
        } catch (Exception e) {
            throw new BackEndRequestFailedCpeException(new DataOperationFailureErrors(DataOperationFailureErrors.FailureOperation.UPDATE, ItemType.POLICY_TYPE), e);
        }
    }

    @Override
    public void deletePolicyType(Long id) {

        validateAllowAdministration();
        validatePolicyTypeId(id);
        try (ExecutionContext executionContext = getExecutionContext()) {
            executionContext.retryNoReturn(r -> policyTypeRepository.delete(executionContext, id, true));
        } catch (CpeException e) {
            throw e;
        } catch (Exception e) {
            throw new BackEndRequestFailedCpeException(new DataOperationFailureErrors(DataOperationFailureErrors.FailureOperation.DELETE, ItemType.POLICY_TYPE), e);
        }
    }

    /**
     * getSession has adminApi validation as well, but I have copy here to throw early.
     */
    private void validateAllowAdministration(){

        if ( !apiProperties.getAdminBaseDataEnabled() )
        {
            throw new RuntimeException("Administration Api is currently disabled.");
        }
    }

    private void validatePolicyTypeId(Long id) {
        if (id == null) {
            throw new InvalidFieldValueCpeException(InvalidFieldValueErrors.POLICY_TYPE_ID_REQUIRED);
        }
    }

    private ExecutionContext getExecutionContext() {
        return executionContextProvider.getExecutionContext(RepositoryType.POLICY);
    }

}
