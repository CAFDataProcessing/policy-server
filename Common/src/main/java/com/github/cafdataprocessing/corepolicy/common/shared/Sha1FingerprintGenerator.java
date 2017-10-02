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
package com.github.cafdataprocessing.corepolicy.common.shared;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.xml.bind.DatatypeConverter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Generates SHA-1 fingerprints
 */
@Component
public class Sha1FingerprintGenerator implements FingerprintGenerator {
    private static final Logger logger = LoggerFactory.getLogger(Sha1FingerprintGenerator.class);

    @Override
    public String generate(String input) {
        try {
            MessageDigest crypt = MessageDigest.getInstance("SHA-1");
            crypt.reset();
            crypt.update(input.getBytes());
            return DatatypeConverter.printHexBinary(crypt.digest());
        } catch (NoSuchAlgorithmException e) {
            logger.error("Expected crypto algorithm SHA-1 was not found.", e);
            throw new RuntimeException(e);
        }
    }
}
