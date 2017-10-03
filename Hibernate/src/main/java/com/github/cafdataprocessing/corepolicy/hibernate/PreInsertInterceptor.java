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
package com.github.cafdataprocessing.corepolicy.hibernate;

import com.github.cafdataprocessing.corepolicy.common.AdminUserContext;
import com.github.cafdataprocessing.corepolicy.common.UserContext;
import com.github.cafdataprocessing.corepolicy.common.dto.DtoBase;
import com.github.cafdataprocessing.corepolicy.common.exceptions.InvalidFieldValueCpeException;
import com.github.cafdataprocessing.corepolicy.common.exceptions.excpetionErrors.LocalisedExceptionError;
import com.github.cafdataprocessing.corepolicy.validation.ObjectValidator;
import com.github.cafdataprocessing.corepolicy.validation.ValidationResult;
import org.hibernate.EmptyInterceptor;
import org.hibernate.type.Type;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.lang.reflect.Field;

/**
 * Checks insert operations and modifies as appropriate
 */
@Component
public class PreInsertInterceptor extends EmptyInterceptor
{
    private UserContext userContext;
    private ObjectValidator objectValidator;
    private ApplicationContext applicationContext;

    @Autowired
    public PreInsertInterceptor(UserContext userContext, ObjectValidator objectValidator, ApplicationContext applicationContext){
        this.userContext = userContext;
        this.objectValidator = objectValidator;
        this.applicationContext = applicationContext;
    }

    public boolean onSave(Object entity,
                          Serializable id,
                          Object[] state,
                          String[] propertyNames,
                          Type[] types) {
        validate(entity);
        return addProjectId(propertyNames, state);
    }

    @Override
    public boolean onFlushDirty(Object entity, Serializable id, Object[] currentState, Object[] previousState, String[] propertyNames, Type[] types) {
        validate(entity);
        return addProjectId(propertyNames, currentState);
    }

    private void validate(Object entity) {
        ValidationResult validationResult = objectValidator.validate(entity);

        if(!validationResult.isValid()) {
            throw new InvalidFieldValueCpeException(new LocalisedExceptionError(validationResult.getReason()));
        }
    }

    private boolean addProjectId(String[] propertyNames, Object[] state) {
        boolean stateModified = false;

        for (int i = 0; i < propertyNames.length; i++) {
            Object o = state[i];
            if ("projectId".equalsIgnoreCase(propertyNames[i]) || "project_id".equalsIgnoreCase(propertyNames[i])) {
                // When using the AdminApi the projectId of items should be null as its the base data.

                state[i] = getProjectIdAllowingForAdminApi();
                stateModified = true;
                break;
            } else if (o instanceof DtoBase) {
                try {
                    Field projectIdField = DtoBase.class.getDeclaredField("projectId");
                    if (projectIdField != null) {
                        projectIdField.setAccessible(true);
                        try {
                            projectIdField.set(o, getProjectIdAllowingForAdminApi());
                        } catch (IllegalAccessException ignored) {
                        }
                    }
                } catch (NoSuchFieldException ignored) {
                }
            }
        }
        return stateModified;
    }

    private String getProjectIdAllowingForAdminApi( ){

        // Get the thread scoped admin context.
        AdminUserContext adminUserContext = applicationContext.getBean(AdminUserContext.class);
        if ( adminUserContext != null && adminUserContext.isAllowAdministration() ) {
            return null;
        }

        return userContext.getProjectId();
    }
}
