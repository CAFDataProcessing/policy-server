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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.cafdataprocessing.corepolicy.common.dto.ClassifyDocumentResult;
import com.github.cafdataprocessing.corepolicy.common.dto.ConditionEngineResult;
import com.github.cafdataprocessing.corepolicy.common.dto.MatchedCollection;
import com.github.cafdataprocessing.corepolicy.common.dto.MatchedCondition;
import com.github.cafdataprocessing.corepolicy.common.exceptions.BackEndRequestFailedCpeException;
import com.github.cafdataprocessing.corepolicy.common.exceptions.CpeException;
import com.github.cafdataprocessing.corepolicy.common.exceptions.excpetionErrors.BackEndRequestFailedErrors;
import com.github.cafdataprocessing.corepolicy.common.shared.CorePolicyObjectMapper;
import com.github.cafdataprocessing.corepolicy.common.shared.MetadataValue;
import com.google.common.base.Strings;
import com.google.common.collect.Multimap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;

/**
 * Methods to manage results from the condition engine.
 */
@Component
public class ConditionEngineMetadata {
    private static final Logger logger = LoggerFactory.getLogger(ConditionEngineMetadata.class);
    private static final ObjectMapper mapper = new CorePolicyObjectMapper();
    private HashProvider hashProvider;

    @Autowired
    public ConditionEngineMetadata(HashProvider hashProvider){
        this.hashProvider = hashProvider;
    }


    public ConditionEngineResult createResult(ClassifyDocumentResult classifyDocumentResult){

        if(Strings.isNullOrEmpty(classifyDocumentResult.signature))
            return new ConditionEngineResult();

        try {
            String decodedSignature = ZipUtils.decompressEncodedString(classifyDocumentResult.signature);

            if(Strings.isNullOrEmpty(decodedSignature))
                return new ConditionEngineResult();

            ObjectMapper mapper = new CorePolicyObjectMapper();
            return mapper.readValue(decodedSignature, ConditionEngineResult.class);
        } catch (IOException e) {
            logger.info("Problem de-serializing Classify signature, ignoring.");
            return new ConditionEngineResult();
        }
    }

    public ConditionEngineResult createResult(Multimap<String, String> metadata) {
        //if the eval info and hash are not present, disregard them
        if (!metadata.containsKey(DocumentFields.EvaluationInformationBlob)
                || !metadata.containsKey(DocumentFields.MetadataHash)) {
            logger.debug("No previous evaluation info or corresponding hash supplied - starting evaluation from scratch.");
            return new ConditionEngineResult();
        }

        //if there are more than 1 of the has fields, disregard them
        Collection<String> allBlobFields = metadata.get(DocumentFields.EvaluationInformationBlob);
        Collection<String> allHashFields = metadata.get(DocumentFields.MetadataHash);

        if (allBlobFields.size() != 1 || allHashFields.size() != 1) {
            logger.debug(String.format("Incorrect number of blob fields %d or condition fields %d found, ignoring.", allBlobFields.size(), allHashFields.size()));
            return new ConditionEngineResult();
        }

        String blob = allBlobFields.stream().findFirst().get();
        String hash = allHashFields.stream().findFirst().get();

        return validateConditionEngineResult(blob, hash);
    }

    public ConditionEngineResult createResultFromMetadata(Multimap<String, MetadataValue> metadata){
        //if the eval info and hash are not present, disregard them
        if(!metadata.containsKey(DocumentFields.EvaluationInformationBlob)
                || !metadata.containsKey(DocumentFields.MetadataHash)){
            logger.debug("No previous evaluation info or corresponding hash supplied - starting evaluation from scratch.");
            return new ConditionEngineResult();
        }

        //if there are more than 1 of the has fields, disregard them
        Collection<MetadataValue> allBlobFields = metadata.get(DocumentFields.EvaluationInformationBlob);
        Collection<MetadataValue> allHashFields = metadata.get(DocumentFields.MetadataHash);

        if(allBlobFields.size() != 1 || allHashFields.size() != 1) {
            logger.debug(String.format("Incorrect number of blob fields %d or condition fields %d found, ignoring.", allBlobFields.size(), allHashFields.size()));
            return new ConditionEngineResult();
        }

        MetadataValue blob = allBlobFields.stream().findFirst().get();
        MetadataValue hash = allHashFields.stream().findFirst().get();

        //deserialize the blob and validate with the hash, if all checks out then we're good.
        return validateConditionEngineResult(blob.getAsString(), hash.getAsString());
    }

    private ConditionEngineResult validateConditionEngineResult(String blob, String hash) {
        //deserialize the blob and validate with the hash, if all checks out then we're good.
        try {
            ConditionEngineResult conditionEngineResult = mapper.readValue(ZipUtils.decompressEncodedString(blob), ConditionEngineResult.class);

            if (!isValidHash(conditionEngineResult, hash)) {
                logger.info("Invalid hash for deserialized classify result, ignoring.");
                return new ConditionEngineResult();
            }
            return conditionEngineResult;
        } catch (IOException e) {
            logger.debug("There was a problem de-serializing ClassifyDocumentResult, ignoring.", e);
            return new ConditionEngineResult();
        }
    }
    /**
     * Verifies that a hash matches the supplied classify result.
     */
    public boolean isValidHash(ConditionEngineResult conditionEngineResult, String hash){
        String generatedHash = generateSecurityHash(conditionEngineResult);
        return generatedHash.equals(hash);
    }

    /**
     * This method will REMOVE any existing temp (e.g. validation hash) metadata fields, and add new ones that represent
     * this metadata.
     * @param document The document who's metadata should be updated.
     */
    public void applyTemporaryMetadataToDocument( ConditionEngineResult conditionEngineResult, Document document ){
        try {
            //Lets remove all the field values
            document.getMetadata().removeAll(DocumentFields.EvaluationInformationBlob);
            document.getMetadata().removeAll(DocumentFields.MetadataHash);

            //now lets get new values and add them to the document
            String sourceJson = mapper.writeValueAsString(conditionEngineResult);
            String compressedBlob = ZipUtils.compressStringAndEncode(sourceJson);
            String securityHash = generateSecurityHash(conditionEngineResult);

            document.getMetadata().put(DocumentFields.EvaluationInformationBlob, compressedBlob);
            document.getMetadata().put(DocumentFields.MetadataHash, securityHash);

        } catch (IOException e) {
            CpeException cpeException = new BackEndRequestFailedCpeException(BackEndRequestFailedErrors.UnableToAddTemporaryMetadata,e );
            logger.error("Error adding temporary evaluation metadata to a document.", cpeException);
            throw cpeException;
        }
    }

    /**
     * This method will REMOVE any existing persistent metadata fields (e.g. POLICY_MATCHED_COLLECTION), and add new ones
     * that represent this metadata.
     * @param document The document who's metadata should be updated.
     */
    public void applyPersistentMetadataToDocument(ConditionEngineResult conditionEngineResult, Document document ){
        //In this case, remove all the field values first
        try{
            //we will need to get field values to clear from the document itself, e.g.
            //"POLICY_MATCHED_COLLECTION"
            //"POLICY_MATCHED_COLLECTION_<COLLECTION_ID>"
            for(String fieldValue : document.getMetadata().get(DocumentFields.SearchField_MatchedCollection)){
                try{
                    long matchedCollectionId = Long.parseLong(fieldValue);
                    //remove all condition match fields
                    document.getMetadata().removeAll(DocumentFields.getMatchedConditionField(matchedCollectionId));

                } catch (NumberFormatException e){
                    logger.debug("Unexpected value parsing document for matched collections, skipping: " + fieldValue);
                }
            }

            //now we can remove match collection fields
            document.getMetadata().removeAll(DocumentFields.SearchField_MatchedCollection);

            //now that we have removed the old fields, we can re-add new ones

            for(MatchedCollection matchedCollection : conditionEngineResult.matchedCollections){
                document.getMetadata().put(DocumentFields.SearchField_MatchedCollection, String.valueOf(matchedCollection.getId()));

                for(MatchedCondition matchedCondition : matchedCollection.getMatchedConditions()){
                    document.getMetadata().put(
                            DocumentFields.getMatchedConditionField(matchedCollection.getId()),
                            String.valueOf(matchedCondition.id)
                    );
                }
            }
        } catch(Exception e) {
            CpeException cpeException = new BackEndRequestFailedCpeException(BackEndRequestFailedErrors.GeneralFailure, e);
            logger.error("Error adding matched collection info to document.", cpeException);
            throw cpeException;
        }
    }


    public String compressAndEncode(ConditionEngineResult conditionEngineResult) throws IOException {
        String sourceJson = mapper.writeValueAsString(conditionEngineResult);
        return ZipUtils.compressStringAndEncode(sourceJson);
    }


    public String generateSecurityHash(ConditionEngineResult conditionEngineResult) {
        if(conditionEngineResult == null)
            throw new IllegalArgumentException("classifyDocumentResult");

        try {
            String evaluationMetadataString = mapper.writeValueAsString(conditionEngineResult);
            return hashProvider.encryptAndGetHash(evaluationMetadataString);

        } catch (NoSuchAlgorithmException e) {
            CpeException cpeException = new BackEndRequestFailedCpeException(BackEndRequestFailedErrors.GeneralFailure, e);
            logger.error("Unable to create hash provider.", cpeException);
            throw cpeException;
        } catch (JsonProcessingException e) {
            CpeException cpeException = new BackEndRequestFailedCpeException(BackEndRequestFailedErrors.GeneralFailure, e);
            logger.error("Unable to serialise Classify result.", cpeException);
            throw cpeException;
        }
    }
}
