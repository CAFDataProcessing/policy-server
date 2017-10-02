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

import com.github.cafdataprocessing.corepolicy.common.security.CorePolicyEncryptorProvider;
import org.apache.commons.lang3.StringUtils;
import org.jasypt.encryption.StringEncryptor;
import org.jasypt.properties.PropertyValueEncryptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

/**
 * Base class for properties.
 */
public class PropertiesBase {
    @Autowired
    protected Environment environment;

    @Autowired
    CorePolicyEncryptorProvider corePolicyEncryptorProvider;

    StringEncryptor stringEncryptor;

    protected String getSetting(String name) {
        if(stringEncryptor==null){
            stringEncryptor = corePolicyEncryptorProvider.getEncryptor();
        }
        String value = environment.getProperty(name);
        if(PropertyValueEncryptionUtils.isEncryptedValue(value)){
            return PropertyValueEncryptionUtils.decrypt(value, stringEncryptor);
        }
        return value;
    }

    protected String getSetting(String name, String defaultValue) {
        String settingValue = getSetting(name);
        return StringUtils.isBlank(settingValue) ? defaultValue : settingValue;
    }
}