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
package com.github.cafdataprocessing.corepolicy.api;

import com.github.cafdataprocessing.corepolicy.common.ClassificationApi;
import com.github.cafdataprocessing.corepolicy.common.Document;
import com.github.cafdataprocessing.corepolicy.common.DocumentFields;
import com.github.cafdataprocessing.corepolicy.common.dto.CollectionSequence;
import com.github.cafdataprocessing.corepolicy.common.dto.ItemType;
import com.github.cafdataprocessing.corepolicy.common.exceptions.BackEndRequestFailedCpeException;
import com.github.cafdataprocessing.corepolicy.common.exceptions.excpetionErrors.DataOperationFailureErrors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Collection;
import java.util.Optional;

/**
 *
 */
public class CollectionSequenceIdExtractorImpl implements CollectionSequenceIdExtractor, ApplicationContextAware {

    private ClassificationApi classificationApi;
    private ApplicationContext applicationContext;

    public CollectionSequenceIdExtractorImpl( ApplicationContext applicationContext ){

        this.applicationContext = applicationContext;
    }

    /**
     * THIS IS A PACKAGE PRIVATE CONSTRUCTOR FOR TESTING ONLY!
     */
    CollectionSequenceIdExtractorImpl(ClassificationApi classificationApi)
    {
        // force use of our mock objects.
        this.classificationApi = classificationApi;
    }

    @Override
    public Long extract(Document document) throws Exception {
        Optional<String> collectionSequenceIdFieldValue = document.getMetadata().get(DocumentFields.CollectionSequence).stream().findFirst();
        if(!collectionSequenceIdFieldValue.isPresent()){
            throw new Exception("No " + DocumentFields.CollectionSequence + " field");
        }
        return getCollectionSequenceId(collectionSequenceIdFieldValue.get());
    }

    public Long getCollectionSequenceId(String collectionSequenceValue) {
        Long collectionSequenceId;
        if(StringUtils.isNumeric(collectionSequenceValue)){
            collectionSequenceId = Long.parseLong(collectionSequenceValue);
        }
        else {
            final Collection<CollectionSequence> collectionSequences = getClassificationApi().retrieveCollectionSequencesByName(collectionSequenceValue);
            if(collectionSequences == null || collectionSequences.isEmpty()){
                throw new BackEndRequestFailedCpeException(new DataOperationFailureErrors(DataOperationFailureErrors.FailureOperation.RETRIEVE, ItemType.COLLECTION_SEQUENCE, collectionSequenceValue), new Exception("Could not find a collection sequence with given name: " + collectionSequenceValue));
            }
            collectionSequenceId = collectionSequences.stream().findFirst().get().id;
        }
        return collectionSequenceId;
    }


    public ClassificationApi getClassificationApi() {
        if ( classificationApi == null )
        {
            classificationApi = applicationContext.getBean(ClassificationApi.class);
        }

        return classificationApi;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
}
