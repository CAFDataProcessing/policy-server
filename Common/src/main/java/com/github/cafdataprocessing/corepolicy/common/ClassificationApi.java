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
import com.github.cafdataprocessing.corepolicy.common.dto.conditions.Condition;

import java.util.Collection;

/**
 *
 */
public interface ClassificationApi {

    <T> T update (T dtoBase);
    <T> T update (T dtoBase, UpdateBehaviourType updateBehaviourType);

    CollectionSequence create(CollectionSequence collectionSequence);
    Collection<CollectionSequence> retrieveCollectionSequences(Collection<Long> ids);
    PageOfResults<CollectionSequence> retrieveCollectionSequencesPage (PageRequest pageRequest);
    PageOfResults<CollectionSequence> retrieveCollectionSequencesPage(PageRequest pageRequest, Filter filter);
    PageOfResults<CollectionSequence> retrieveCollectionSequencesPage(PageRequest pageRequest, Filter filter, Sort sort);
    PageOfResults<CollectionSequence> retrieveCollectionSequencesPage(PageRequest pageRequest, Sort sort);
    Collection<CollectionSequence> retrieveCollectionSequencesByName(String name);


    void deleteCollectionSequence(Long id);

    DocumentCollection create(DocumentCollection documentCollection);
    Collection<DocumentCollection> retrieveCollections(Collection<Long> ids);
    Collection<DocumentCollection> retrieveCollections(Collection<Long> ids,
                                            boolean includeCondition,
                                            boolean includeConditionChildren);
    PageOfResults<DocumentCollection> retrieveCollectionsPage(PageRequest pageRequest);
    PageOfResults<DocumentCollection> retrieveCollectionsPage(PageRequest pageRequest, Filter filter );
    PageOfResults<DocumentCollection> retrieveCollectionsPage(PageRequest pageRequest, Filter filter, Sort sort);
    PageOfResults<DocumentCollection> retrieveCollectionsPage(PageRequest pageRequest, Sort sort);

    void deleteCollection(Long id);

    <T extends Condition> T create(T condition);
    void deleteCondition(Long id);
    Collection<Condition> retrieveConditions(Collection<Long> ids, Boolean includeChildren);

    // Paging on conditions alone doesn't make much sense but we should be able to get back LexiconConditions
    // by a Lexicon itself. This is now dictated by the filter.  Really same method could be used for conditionFragments.
    // with is_fragment=true as a filter.
    PageOfResults<Condition> retrieveConditionsPage(PageRequest pageRequest, Filter filter );
    PageOfResults<Condition> retrieveConditionsPage(PageRequest pageRequest, Filter filter, Sort sort );
    PageOfResults<Condition> retrieveConditionsPage(PageRequest pageRequest, Sort sort );

    PageOfResults<Condition> retrieveConditionFragmentsPage(PageRequest pageRequest);

    Lexicon create(Lexicon lexicon);
    void deleteLexicon(Long id);
    PageOfResults<Lexicon> retrieveLexiconsPage(PageRequest pageRequest);
    PageOfResults<Lexicon> retrieveLexiconsPage(PageRequest pageRequest, Filter filter );
    Collection<Lexicon> retrieveLexicons(Collection<Long> ids);

    LexiconExpression create(LexiconExpression t);
    void deleteLexiconExpression(Long id);
    PageOfResults<LexiconExpression> retrieveLexiconExpressionsPage(PageRequest pageRequest);
    PageOfResults<LexiconExpression> retrieveLexiconExpressionsPage(PageRequest pageRequest, Filter filter );
    Collection<LexiconExpression> retrieveLexiconExpressions(Collection<Long> ids);

    FieldLabel retrieveFieldLabel(String fieldName);
    FieldLabel create(FieldLabel t);
    void deleteFieldLabel(Long id);
    PageOfResults<FieldLabel> retrieveFieldLabelPage(PageRequest pageRequest);
    PageOfResults<FieldLabel> retrieveFieldLabelPage(PageRequest pageRequest, Filter filter);
}
