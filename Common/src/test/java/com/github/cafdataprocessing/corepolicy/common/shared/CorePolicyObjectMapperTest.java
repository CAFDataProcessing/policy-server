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
package com.github.cafdataprocessing.corepolicy.common.shared;

import com.github.cafdataprocessing.corepolicy.common.Document;
import com.github.cafdataprocessing.corepolicy.common.DocumentImpl;
import org.junit.Test;

/**
 *
 */
public class CorePolicyObjectMapperTest {

    CorePolicyObjectMapper sut = new CorePolicyObjectMapper();


    @Test
    public void DocumentSerializeTest() throws Exception{
        Document childDocument = new DocumentImpl();
        childDocument.setReference("childreference");
        childDocument.getMetadata().put("childfield", "test");

        Document document = new DocumentImpl();
        document.setReference("reference");
        document.getMetadata().put("field", "test");

        document.getDocuments().add(childDocument);


        String s = sut.writeValueAsString(document);

        document = sut.readValue(s, Document.class);

    }
}