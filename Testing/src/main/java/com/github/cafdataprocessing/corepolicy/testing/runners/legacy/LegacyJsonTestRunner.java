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
package com.github.cafdataprocessing.corepolicy.testing.runners.legacy;

import com.github.cafdataprocessing.corepolicy.api.ApiProvider;
import com.github.cafdataprocessing.corepolicy.common.dto.ClassifyDocumentResult;
import com.github.cafdataprocessing.corepolicy.common.dto.CollectionSequence;
import com.github.cafdataprocessing.corepolicy.testing.TestRunner;
import com.github.cafdataprocessing.corepolicy.testing.loaders.ClientIdPolicyLoader;
import com.github.cafdataprocessing.corepolicy.testing.models.PolicyEnvironment;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.junit.Assert;


import java.io.File;
import java.util.Arrays;
import java.util.Collection;

/**
 *
 */
public class LegacyJsonTestRunner extends TestRunner<LegacyTest, LegacyTestSet, ClassifyDocumentResult> {
    private ClientIdPolicyLoader policyEnvironmentLoader;

    public static void main(String[] args) throws Exception {
        LegacyJsonTestRunner testRunner = new LegacyJsonTestRunner();
        testRunner.run(args);
    }

    public LegacyJsonTestRunner(){
        policyEnvironmentLoader = applicationContext.getBean(ClientIdPolicyLoader.class);
        policyEnvironmentLoader.setIdManager(idManager);
    }

    @Override
    protected LegacyTestSet deserialise(File file) throws Exception {
        File policyXmlFile = new File(file.getAbsolutePath() + File.pathSeparator + file.getName().replace("DOC_", "POLICY_"));

        PolicyEnvironment policyEnvironment = convertPolicyXml(policyXmlFile);

        LegacyTestSet legacyTestSet = objectMapper.reader(LegacyTestSet.class).readValue(file);
        legacyTestSet.policyEnvironment = policyEnvironment;

        return legacyTestSet;
    }

    private PolicyEnvironment convertPolicyXml(File policyXmlFile) {
        return new PolicyEnvironment();
    }

    @Override
    protected void arrange(LegacyTestSet testSet) throws Exception {
        policyEnvironmentLoader.load(testSet.policyEnvironment);
    }

    @Override
    protected ClassifyDocumentResult act(LegacyTest test) throws Exception {

        ApiProvider apiProvider = new ApiProvider(applicationContext);

        Long sequenceId = getSequenceIdByName(test, apiProvider);

        Collection<ClassifyDocumentResult> conditionEngineResults = apiProvider.getClassifyDocumentApi().classify( sequenceId, Arrays.asList(test.document));

        Validate.notEmpty(conditionEngineResults);
        Validate.isTrue(conditionEngineResults.size()==1, "ClassifyDocumentResults must contain only 1 result");

        return conditionEngineResults.stream().findFirst().get();
    }

    private Long getSequenceIdByName(LegacyTest test, ApiProvider apiProvider) throws Exception {
        Long sequenceId;
        if(StringUtils.isNumeric(test.collectionSequenceName)) {
           sequenceId = Long.valueOf(test.collectionSequenceName);
        }
       else {
            Collection<CollectionSequence> results = apiProvider.getClassificationApi().retrieveCollectionSequencesByName(test.collectionSequenceName);

            Validate.notEmpty(results);
            Validate.isTrue(results.size()==1, "CollectionSequence must only have a single match for this given name.");

            sequenceId = results.stream().findFirst().get().id;
        }
        return sequenceId;
    }

    @Override
    protected void assertForResult(LegacyTest test, ClassifyDocumentResult conditionEngineResult) {
        Assert.assertEquals(test.expectedResult.collectionIdAssignedByDefault, conditionEngineResult.collectionIdAssignedByDefault);
    }
}
