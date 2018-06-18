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

import com.github.cafdataprocessing.corepolicy.common.ApiProperties;
import com.github.cafdataprocessing.corepolicy.common.ClassificationApi;
import com.github.cafdataprocessing.corepolicy.common.dto.*;
import com.github.cafdataprocessing.corepolicy.common.dto.conditions.Condition;
import com.github.cafdataprocessing.corepolicy.common.dtoweb.RetrieveAdditional;
import com.github.cafdataprocessing.corepolicy.common.dtoweb.RetrieveRequest;
import com.github.cafdataprocessing.corepolicy.common.dtoweb.UpdateRequest;
import org.apache.http.NameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 *  Base class for any web implementation of the ClassificationApi interface.
 */
public abstract class ClassificationApiWebBase extends WebApiBase implements ClassificationApi {

    private static final Logger logger = LoggerFactory.getLogger(ClassificationApiWebBase.class);

    protected Logger getLogger(){
        return logger;
    }

    public ClassificationApiWebBase(ApiProperties apiProperties){super(apiProperties);}

    public CollectionSequence create(CollectionSequence collectionSequence) {
        return makeSingleRequest(WebApiAction.CREATE,collectionSequence,DtoBase.class);
    }

    @Override
    public <T> T update(T dtoBase) {
        return makeSingleRequest(WebApiAction.UPDATE, dtoBase, DtoBase.class);
    }

    @Override
    public <T> T update(T dtoBase, UpdateBehaviourType updateBehaviourType) {
        UpdateRequest updateRequest = new UpdateRequest();
        updateRequest.objectToUpdate = (DtoBase) dtoBase;
        updateRequest.updateBehaviour = updateBehaviourType;
        Collection<NameValuePair> params = createParams(updateRequest);
        return makeSingleRequest(WebApiAction.UPDATE, params, DtoBase.class);
    }

    @Override
    public Collection<CollectionSequence> retrieveCollectionSequences(Collection<Long> ids) {
        if(ids.isEmpty()){
            return new ArrayList<CollectionSequence>();
        }
        RetrieveRequest retrieveRequest = new RetrieveRequest();
        retrieveRequest.id = ids;
        retrieveRequest.type = ItemType.COLLECTION_SEQUENCE;
        final Collection<NameValuePair> params = createParams(retrieveRequest);

        return makeMultipleRequest(WebApiAction.RETRIEVE, params, CollectionSequence.class);
    }

    @Override
    public PageOfResults<CollectionSequence> retrieveCollectionSequencesPage(PageRequest pageRequest) {

        RetrieveRequest retrieveRequest = new RetrieveRequest();
        retrieveRequest.type = ItemType.COLLECTION_SEQUENCE;
        retrieveRequest.max_page_results = pageRequest.max_page_results;
        retrieveRequest.start = pageRequest.start;
        final Collection<NameValuePair> params = createParams(retrieveRequest);

        return makePagedRequest(WebApiAction.RETRIEVE, params, CollectionSequence.class);
    }

    @Override
    public PageOfResults<CollectionSequence> retrieveCollectionSequencesPage(PageRequest pageRequest, Filter filter ) {

        return getRelatedItemPageOfResults(pageRequest, ItemType.COLLECTION_SEQUENCE, filter, CollectionSequence.class);
    }

    @Override
    public PageOfResults<CollectionSequence> retrieveCollectionSequencesPage(PageRequest pageRequest, Filter filter, Sort sort) {
        return getRelatedItemPageOfResults(pageRequest, ItemType.COLLECTION_SEQUENCE, filter, sort, CollectionSequence.class);
    }

    @Override
    public PageOfResults<CollectionSequence> retrieveCollectionSequencesPage(PageRequest pageRequest, Sort sort) {
        return getRelatedItemPageOfResults(pageRequest, ItemType.COLLECTION_SEQUENCE, sort, CollectionSequence.class);
    }


    @Override
    public Collection<CollectionSequence> retrieveCollectionSequencesByName(String name) {
        RetrieveRequest retrieveRequest = new RetrieveRequest();
        retrieveRequest.additional = new RetrieveAdditional();
        retrieveRequest.additional.name = name;
        retrieveRequest.type = ItemType.COLLECTION_SEQUENCE;
        final Collection<NameValuePair> params = createParams(retrieveRequest);

        return makeMultipleRequest(WebApiAction.RETRIEVE, params, CollectionSequence.class);
    }

    @Override
    public void deleteCollectionSequence(Long id) {
        makeDeleteRequest(id, ItemType.COLLECTION_SEQUENCE);
    }

    @Override
    public DocumentCollection create(DocumentCollection documentCollection) {
        return makeSingleRequest(WebApiAction.CREATE,documentCollection,DtoBase.class);
    }

    @Override
    public Collection<DocumentCollection> retrieveCollections(Collection<Long> ids) {
        RetrieveRequest retrieveRequest = new RetrieveRequest();
        retrieveRequest.id = ids;
        retrieveRequest.type = ItemType.COLLECTION;
        final Collection<NameValuePair> params = createParams(retrieveRequest);

        return makeMultipleRequest(WebApiAction.RETRIEVE, params, DocumentCollection.class);
    }

    @Override
    public Collection<DocumentCollection> retrieveCollections(Collection<Long> ids, boolean includeCondition, boolean includeConditionChildren) {
        RetrieveRequest retrieveRequest = new RetrieveRequest();
        retrieveRequest.id = ids;
        retrieveRequest.type = ItemType.COLLECTION;
        retrieveRequest.additional = new RetrieveAdditional();
        retrieveRequest.additional.includeChildren = includeConditionChildren;
        retrieveRequest.additional.includeCondition = includeCondition;
        final Collection<NameValuePair> params = createParams(retrieveRequest);

        return makeMultipleRequest(WebApiAction.RETRIEVE, params, DocumentCollection.class);
    }

    @Override
    public PageOfResults<DocumentCollection> retrieveCollectionsPage(PageRequest pageRequest) {
        RetrieveRequest retrieveRequest = new RetrieveRequest();
        retrieveRequest.type = ItemType.COLLECTION;
        retrieveRequest.max_page_results = pageRequest.max_page_results;
        retrieveRequest.start = pageRequest.start;

        final Collection<NameValuePair> params = createParams(retrieveRequest);
        return makePagedRequest(WebApiAction.RETRIEVE, params, DocumentCollection.class);
    }

    @Override
    public PageOfResults<DocumentCollection> retrieveCollectionsPage(PageRequest pageRequest, Filter filter ) {

        return getRelatedItemPageOfResults(pageRequest, ItemType.COLLECTION, filter, DocumentCollection.class);
    }

    @Override
    public PageOfResults<DocumentCollection> retrieveCollectionsPage(PageRequest pageRequest, Filter filter, Sort sort) {
        return getRelatedItemPageOfResults(pageRequest, ItemType.COLLECTION, filter, sort, DocumentCollection.class);
    }

    @Override
    public PageOfResults<DocumentCollection> retrieveCollectionsPage(PageRequest pageRequest, Sort sort) {
        return getRelatedItemPageOfResults(pageRequest, ItemType.COLLECTION, sort, DocumentCollection.class);
    }

    @Override
    public void deleteCollection(Long id) {
        makeDeleteRequest(id, ItemType.COLLECTION);
    }

    @Override
    public Condition create(Condition condition) {
        return makeSingleRequest(WebApiAction.CREATE,condition,DtoBase.class);
    }

    @Override
    public void deleteCondition(Long id) {
        makeDeleteRequest(id, ItemType.CONDITION);
    }

    @Override
    public Collection<Condition> retrieveConditions(Collection<Long> ids, Boolean includeChildren) {
        RetrieveRequest retrieveRequest = new RetrieveRequest();
        retrieveRequest.id = ids;
        retrieveRequest.type = ItemType.CONDITION;
        retrieveRequest.additional = new RetrieveAdditional();
        retrieveRequest.additional.includeChildren = includeChildren;
        final Collection<NameValuePair> params = createParams(retrieveRequest);

        return makeMultipleRequest(WebApiAction.RETRIEVE, params, Condition.class);
    }

    @Override
    public PageOfResults<Condition> retrieveConditionFragmentsPage(PageRequest pageRequest) {
        RetrieveRequest retrieveRequest = new RetrieveRequest();
        retrieveRequest.type = ItemType.CONDITION;
        retrieveRequest.max_page_results = pageRequest.max_page_results;
        retrieveRequest.start = pageRequest.start;
        final Collection<NameValuePair> params = createParams(retrieveRequest);

        return makePagedRequest(WebApiAction.RETRIEVE, params, Condition.class);
    }

    @Override
    public PageOfResults<Condition> retrieveConditionsPage(PageRequest pageRequest, Filter filter ) {

        return getRelatedItemPageOfResults(pageRequest, ItemType.CONDITION, filter, Condition.class);
    }

    @Override
    public PageOfResults<Condition> retrieveConditionsPage(PageRequest pageRequest, Filter filter, Sort sort) {
        return getRelatedItemPageOfResults(pageRequest, ItemType.CONDITION, filter, sort, Condition.class);
    }

    @Override
    public PageOfResults<Condition> retrieveConditionsPage(PageRequest pageRequest, Sort sort) {
        return getRelatedItemPageOfResults(pageRequest, ItemType.CONDITION, sort, Condition.class);
    }

    @Override
    public Lexicon create(Lexicon lexicon) {
//        final LexiconWeb webDto = LexiconWeb.put(lexicon);
//        final Collection<NameValuePair> params = createParams(webDto);
//
//        return makeSingleRequest(WebApiAction.CREATE, params, lexiconTranslator, LexiconWeb.class);
        return makeSingleRequest(WebApiAction.CREATE,lexicon,DtoBase.class);
    }

    @Override
    public void deleteLexicon(Long id) {
        makeDeleteRequest(id, ItemType.LEXICON);
    }

    @Override
    public PageOfResults<Lexicon> retrieveLexiconsPage(PageRequest pageRequest) {
        RetrieveRequest retrieveRequest = new RetrieveRequest();
        retrieveRequest.type = ItemType.LEXICON;
        retrieveRequest.max_page_results = pageRequest.max_page_results;
        retrieveRequest.start = pageRequest.start;
        final Collection<NameValuePair> params = createParams(retrieveRequest);

        return makePagedRequest(WebApiAction.RETRIEVE, params, Lexicon.class);
    }

    @Override
    public PageOfResults<Lexicon> retrieveLexiconsPage(PageRequest pageRequest, Filter filter ) {

        return getRelatedItemPageOfResults(pageRequest, ItemType.LEXICON, filter, Lexicon.class);
    }

    @Override
    public Collection<Lexicon> retrieveLexicons(Collection<Long> ids) {
        RetrieveRequest retrieveRequest = new RetrieveRequest();
        retrieveRequest.id = ids;
        retrieveRequest.type = ItemType.LEXICON;
        final Collection<NameValuePair> params = createParams(retrieveRequest);

        return makeMultipleRequest(WebApiAction.RETRIEVE, params, Lexicon.class);
    }

    @Override
    public LexiconExpression create(LexiconExpression t) {
        return makeSingleRequest(WebApiAction.CREATE,t,DtoBase.class);
    }

    @Override
    public void deleteLexiconExpression(Long id) {
        makeDeleteRequest(id, ItemType.LEXICON_EXPRESSION);
    }

    @Override
    public PageOfResults<LexiconExpression> retrieveLexiconExpressionsPage(PageRequest pageRequest) {
        RetrieveRequest retrieveRequest = new RetrieveRequest();
        retrieveRequest.type = ItemType.LEXICON_EXPRESSION;
        retrieveRequest.max_page_results = pageRequest.max_page_results;
        retrieveRequest.start = pageRequest.start;

        final Collection<NameValuePair> params = createParams(retrieveRequest);
        return makePagedRequest(WebApiAction.RETRIEVE, params, LexiconExpression.class);
    }

    @Override
    public PageOfResults<LexiconExpression> retrieveLexiconExpressionsPage(PageRequest pageRequest, Filter filter ) {

        return getRelatedItemPageOfResults(pageRequest, ItemType.LEXICON_EXPRESSION, filter, LexiconExpression.class);
    }

    @Override
    public Collection<LexiconExpression> retrieveLexiconExpressions(Collection<Long> ids) {
        RetrieveRequest retrieveRequest = new RetrieveRequest();
        retrieveRequest.id = ids;
        retrieveRequest.type = ItemType.LEXICON_EXPRESSION;

        final Collection<NameValuePair> params = createParams(retrieveRequest);
        return makeMultipleRequest(WebApiAction.RETRIEVE, params, LexiconExpression.class);
    }

    @Override
    public FieldLabel retrieveFieldLabel(String fieldName) {
        RetrieveRequest retrieveRequest = new RetrieveRequest();
        retrieveRequest.type = ItemType.FIELD_LABEL;

        retrieveRequest.additional = new RetrieveAdditional();
        retrieveRequest.additional.name = fieldName;

        final Collection<NameValuePair> params = createParams(retrieveRequest);
        Collection<FieldLabel> returnedFieldLabels = makeMultipleRequest(WebApiAction.RETRIEVE, params, FieldLabel.class);

        returnedFieldLabels = returnedFieldLabels.stream().filter(u->u!= null).collect(Collectors.toList());

        if(returnedFieldLabels.size() > 1){
            throw new RuntimeException("Multiple field labels returned");
        }
        else if(returnedFieldLabels.size() == 0){
            return null;
        }
        return returnedFieldLabels.stream().findFirst().get();
    }

    @Override
    public FieldLabel create(FieldLabel t) {
        return makeSingleRequest(WebApiAction.CREATE,t,DtoBase.class);
    }

    @Override
    public void deleteFieldLabel(Long id) {
        makeDeleteRequest(id, ItemType.FIELD_LABEL);
    }

    @Override
    public PageOfResults<FieldLabel> retrieveFieldLabelPage(PageRequest pageRequest) {
        RetrieveRequest retrieveRequest = new RetrieveRequest();
        retrieveRequest.type = ItemType.FIELD_LABEL;
        retrieveRequest.max_page_results = pageRequest.max_page_results;
        retrieveRequest.start = pageRequest.start;

        final Collection<NameValuePair> params = createParams(retrieveRequest);
        return makePagedRequest(WebApiAction.RETRIEVE, params, FieldLabel.class);
    }

    @Override
    public PageOfResults<FieldLabel> retrieveFieldLabelPage(PageRequest pageRequest, Filter filter ) {

        return getRelatedItemPageOfResults(pageRequest, ItemType.FIELD_LABEL, filter, FieldLabel.class);
    }
}
