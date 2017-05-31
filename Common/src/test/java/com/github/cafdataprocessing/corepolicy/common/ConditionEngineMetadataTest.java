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
package com.github.cafdataprocessing.corepolicy.common;

import com.github.cafdataprocessing.corepolicy.common.dto.*;
import com.github.cafdataprocessing.corepolicy.common.dto.conditions.ExistsCondition;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.github.cafdataprocessing.corepolicy.TestHelper.getUniqueString;
import static org.mockito.Mockito.when;

/**
 * Tests for the ConditionEngineMetadata class
 */
@RunWith(MockitoJUnitRunner.class)
public class ConditionEngineMetadataTest {

    ExistsCondition matchedCondition = null;
    ExistsCondition unmatchedCondition = null;
    ExistsCondition unevaluatedCondition = null;
    DocumentCollection documentCollection = null;
    DocumentCollection documentCollectionIncomplete = null;
    ConditionEngineResult cer = null;
    @Mock
    EngineProperties engineProperties;
    @Mock
    ApiProperties apiProperties;

    @InjectMocks
    HashProvider hashProvider;
    ConditionEngineMetadata conditionEngineMetadata;

    @Before
    public void setup(){
        conditionEngineMetadata = new ConditionEngineMetadata(hashProvider);

        // setup dummy conditions for later verification.
        matchedCondition = new ExistsCondition();
        matchedCondition.field = "MyField_matchedCondition";
        matchedCondition.id = 1L;

        unmatchedCondition = new ExistsCondition();
        unmatchedCondition.field ="MyField_unmatchedCondition";
        unmatchedCondition.id = 2L;

        unevaluatedCondition = new ExistsCondition();
        unevaluatedCondition.field ="MyField_unevaluatedCondition";
        unevaluatedCondition.id = 3L;

        documentCollection = new DocumentCollection();
        documentCollection.name = getUniqueString("SampleName_");
        documentCollection.id = 4L;
        documentCollection.condition = matchedCondition;

        documentCollectionIncomplete = new DocumentCollection();
        documentCollectionIncomplete.name = getUniqueString("SampleName_");
        documentCollectionIncomplete.id = 5L;
        documentCollectionIncomplete.condition = unevaluatedCondition;

        when(engineProperties.getHashPassword()).thenReturn("password");

        cer = new ConditionEngineResult();

        String reference = getUniqueString("DocumentRef_");

        cer.matchedConditions.add( new MatchedCondition(reference, matchedCondition ) );
        cer.unmatchedConditions.add( new UnmatchedCondition(reference, unmatchedCondition));
        cer.unevaluatedConditions.add( new UnevaluatedCondition(unevaluatedCondition, UnevaluatedCondition.Reason.MISSING_FIELD));
        cer.matchedCollections.add(new MatchedCollection(documentCollection, cer.matchedConditions));
        cer.incompleteCollections.add(6L);
    }

//    @Test
//    public void testConstruction()
//    {
//        shouldThrow( u-> new ConditionEngineMetadata( null ), "Construction of ConditionEngineMetadata with null should throw");
//
//        ConditionEngineMetadata metadata = new ConditionEngineMetadata(hashProvider);
//
//        assertNotNull("ConditionEngineResult must be not null on new object", metadata.getConditionEngineResult());
//    }

//    @Test
//    public void testCreationFromConditionEngineResult(){
//
//
//        // now we have a valid CDR create our condition engine metadata object from it
//        ConditionEngineMetadata metadata = conditionEngineMetadata.createResult(cer);
//
//        Assert.assertNotNull("Metadata cant be null", metadata);
//        Assert.assertNotNull("Meatadata must have a valid CER", metadata.getConditionEngineResult());
//    }

    @Test
    public void testCreationFromClassifyDocumentResult(){

        // create the Classify result from our main CER.
        ClassifyDocumentResult cdr = ClassifyDocumentResult.create( cer, conditionEngineMetadata );

        Assert.assertEquals("Unevaluated conditions must be equal.", cdr.unevaluatedConditions.size(), cer.unevaluatedConditions.size());
        Assert.assertEquals( "Unevaluated conditions must be equal.", cdr.unevaluatedConditions.stream().findFirst().get().id, cer.unevaluatedConditions.stream().findFirst().get().id );

        Assert.assertEquals( "matchedConditions conditions size be equal.", cdr.matchedCollections.size(), cer.matchedCollections.size() );
        Assert.assertEquals( "matchedConditions conditions must be equal.", cdr.matchedCollections.stream().findFirst().get().getId(), cer.matchedCollections.stream().findFirst().get().getId());

        Assert.assertEquals( "unmatchedConditions conditions must be equal.", cdr.incompleteCollections.size(), cer.incompleteCollections.size());
        Assert.assertEquals( "unmatchedConditions conditions must be equal.", cdr.incompleteCollections.stream().findFirst().get().longValue(), cer.incompleteCollections.stream().findFirst().get().longValue());

        // now we have a valid CDR create our condition engine metadata object from it
        ConditionEngineResult metadata = conditionEngineMetadata.createResult(cdr);

        Assert.assertNotNull("Metadata cant be null", metadata);
    }

    @Test
    public void testApplyTemporaryMetadataToDocument() throws Exception {

        Document document = new DocumentImpl();
        document.setReference(cer.reference);

//        ConditionEngineMetadata metadata = conditionEngineMetadata.createResult(cer);
        conditionEngineMetadata.applyTemporaryMetadataToDocument(cer, document);

        String blob = conditionEngineMetadata.compressAndEncode(cer);
        String hash = conditionEngineMetadata.generateSecurityHash(cer);

        Assert.assertEquals("Blob data must be equal", blob, document.getMetadata().get(DocumentFields.EvaluationInformationBlob).stream().findFirst().get());
        Assert.assertEquals("Hash data must be equal", hash, document.getMetadata().get(DocumentFields.MetadataHash).stream().findFirst().get());
    }

    @Test
    public void testCreationFromBlob() throws Exception {

        Document document = new DocumentImpl();
        document.setReference(cer.reference);


//        ConditionEngineMetadata metadata = ConditionEngineMetadata.createResult(cer);

        conditionEngineMetadata.applyTemporaryMetadataToDocument(cer, document);

        // Now create another CEM from this metadata.

        ConditionEngineResult recreatedResult = conditionEngineMetadata.createResult( document.getMetadata() );

        Assert.assertEquals( "Unevaluated conditions must be equal.", recreatedResult.unevaluatedConditions.size(), cer.unevaluatedConditions.size() );
        Assert.assertEquals( "Unevaluated conditions must be equal.", recreatedResult.unevaluatedConditions.stream().findFirst().get().id, cer.unevaluatedConditions.stream().findFirst().get().id );

        Assert.assertEquals( "matchedConditions conditions size be equal.", recreatedResult.matchedCollections.size(), cer.matchedCollections.size() );
        Assert.assertEquals( "matchedConditions conditions must be equal.", recreatedResult.matchedCollections.stream().findFirst().get().getId(), cer.matchedCollections.stream().findFirst().get().getId());

        Assert.assertEquals( "unmatchedConditions conditions must be equal.", recreatedResult.incompleteCollections.size(), cer.incompleteCollections.size());
        Assert.assertEquals( "unmatchedConditions conditions must be equal.", recreatedResult.incompleteCollections.stream().findFirst().get().longValue(), cer.incompleteCollections.stream().findFirst().get().longValue());

    }

    @Test
    public void testHashIsValid()
    {
        // generate a hash
        String hash = conditionEngineMetadata.generateSecurityHash(cer);

        //use hash validation.
        Assert.assertTrue("Hash must be valid", conditionEngineMetadata.isValidHash(cer, hash));
    }

}
