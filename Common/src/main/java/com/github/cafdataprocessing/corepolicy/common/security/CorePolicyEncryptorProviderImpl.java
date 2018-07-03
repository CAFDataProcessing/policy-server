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
package com.github.cafdataprocessing.corepolicy.common.security;

import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.salt.StringFixedSaltGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * implementation of an encryption provider for core policy
 */
@Component
public class CorePolicyEncryptorProviderImpl implements CorePolicyEncryptorProvider {

    private String corepolicyConfigPassword;

    @Autowired
    public CorePolicyEncryptorProviderImpl(ApplicationContext applicationContext){
        corepolicyConfigPassword = applicationContext.getEnvironment().getProperty("corepolicy_config_password");
    }

    @Override
    public StringEncryptor getEncryptor() {
        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        encryptor.setPassword(getPassword());

        encryptor.setSaltGenerator(new StringFixedSaltGenerator(getSalt()));

        return encryptor;
    }

    /**
     * Reads the password from the 'corepolicy_config_password' env variable, otherwise it gets a default password
     * @return the password to use
     */
    private String getPassword() {
        return corepolicyConfigPassword == null
                ? "{g<xx}>Gc%PbtC$uY4xx4>#H)FX}*'"
                : corepolicyConfigPassword;
    }

    private String getSalt(){
        return "M]cm'q]D)3@-*??x@RVR![2_yj^mw";
    }
}
