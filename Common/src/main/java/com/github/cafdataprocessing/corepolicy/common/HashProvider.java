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
package com.github.cafdataprocessing.corepolicy.common;

import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.salt.StringFixedSaltGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Provides hashes for values
 */
@Component
public class HashProvider {

    private String hashType = "MD5";
    private final String salt = "M<tg>'q]D)3@-*??x@RVR![2_yj^mw";
    private EngineProperties engineProperties;

    @Autowired
    public HashProvider(EngineProperties engineProperties) {
        this.engineProperties = engineProperties;
    }

    private StringEncryptor getEncryptor() {

        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        encryptor.setPassword(engineProperties.getHashPassword());

        encryptor.setSaltGenerator(new StringFixedSaltGenerator(salt));

        return encryptor;
    }


    public String encryptAndGetHash( String input ) throws NoSuchAlgorithmException {
        // useful for taking a long string, and then encrypting it so user can't easily
        // produce our hash simply using an md5 generator.
        String toBeHashed = getEncryptor().encrypt(input);

        return makeHash( toBeHashed );
    }

    public String makeHash(String input) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance(this.hashType);
        byte[] buffer = input.getBytes();
        md.update(buffer);
        byte[] digest = md.digest();
        StringBuilder sb = new StringBuilder();

        for (byte aDigest : digest) {
            sb.append(Integer.toString((aDigest & 0xff) + 0x100, 16).substring(1));
        }

        return sb.toString();
    }
}
