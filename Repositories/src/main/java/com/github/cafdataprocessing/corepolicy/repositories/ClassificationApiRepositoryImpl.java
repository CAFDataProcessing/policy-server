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
package com.github.cafdataprocessing.corepolicy.repositories;

import com.github.cafdataprocessing.corepolicy.common.ApiStrings;
import com.github.cafdataprocessing.corepolicy.common.ClassificationApi;
import com.github.cafdataprocessing.corepolicy.common.dto.*;
import com.github.cafdataprocessing.corepolicy.common.dto.conditions.BooleanCondition;
import com.github.cafdataprocessing.corepolicy.common.dto.conditions.Condition;
import com.github.cafdataprocessing.corepolicy.common.dto.conditions.ConditionType;
import com.github.cafdataprocessing.corepolicy.common.dto.conditions.NotCondition;
import com.github.cafdataprocessing.corepolicy.common.exceptions.BackEndRequestFailedCpeException;
import com.github.cafdataprocessing.corepolicy.common.exceptions.excpetionErrors.BackEndRequestFailedErrors;
import com.github.cafdataprocessing.corepolicy.common.shared.CorePolicyLogger;
import com.github.cafdataprocessing.corepolicy.repositories.v2.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 *
 */
@Component
public class ClassificationApiRepositoryImpl implements ClassificationApi {
    private CollectionSequenceRepository collectionSequenceRepository;
    private CollectionSequenceEntryRepository collectionSequenceEntryRepository;
    private CollectionRepository collectionRepository;
    private ConditionRepository conditionRepository;
    private LexiconRepository lexiconRepository;
    private LexiconExpressionRepository lexiconExpressionRepository;
    private FieldLabelRepository fieldLabelRepository;

    private final ExecutionContextProvider executionContextProvider;
    private final Filter filterIsFragment;

    private PolicyRepository policyRepository;

    @Autowired
    public ClassificationApiRepositoryImpl(CollectionSequenceRepository collectionSequenceRepository,
                                           CollectionSequenceEntryRepository collectionSequenceEntryRepository,
                                           CollectionRepository collectionRepository,
                                           ConditionRepository conditionRepository,
                                           LexiconRepository lexiconRepository,
                                           LexiconExpressionRepository lexiconExpressionRepository,
                                           FieldLabelRepository fieldLabelRepository,
                                           @Qualifier("repositoryExecutionContextProvider") ExecutionContextProvider executionContextProvider,
                                           PolicyRepository policyRepository){
        this.collectionSequenceRepository = collectionSequenceRepository;
        this.collectionSequenceEntryRepository = collectionSequenceEntryRepository;
        this.collectionRepository = collectionRepository;
        this.conditionRepository = conditionRepository;
        this.lexiconRepository = lexiconRepository;
        this.lexiconExpressionRepository = lexiconExpressionRepository;
        this.executionContextProvider = executionContextProvider;
        this.fieldLabelRepository = fieldLabelRepository;
        this.policyRepository = policyRepository;

        // create a filter for IsFragment=true, which can be used on retrieveConditionFragmentsPage.
        this.filterIsFragment = Filter.create(ApiStrings.Conditions.Arguments.IS_FRAGMENT, true);
    }

    @Override
    public <T> T update(T dtoBase) {
        return update(dtoBase, null);
    }

    @Override
    public <T> T update(T dtoBase, UpdateBehaviourType updateBehaviourType) {
        if(dtoBase instanceof Condition) {
            return (T) update((Condition)dtoBase, updateBehaviourType);
        }
        else if(dtoBase instanceof Lexicon){
            return (T) update((Lexicon)dtoBase,updateBehaviourType);
        }
        else if(dtoBase instanceof LexiconExpression){
            return (T) update((LexiconExpression)dtoBase, updateBehaviourType);
        }
        else if(dtoBase instanceof FieldLabel){
            return (T) update((FieldLabel)dtoBase, updateBehaviourType);
        }
        else if(dtoBase instanceof CollectionSequence){
            return (T) update((CollectionSequence)dtoBase, updateBehaviourType);
        }
        else if(dtoBase instanceof DocumentCollection){
            return (T) update((DocumentCollection)dtoBase, updateBehaviourType);
        }
        throw new BackEndRequestFailedCpeException(BackEndRequestFailedErrors.InvalidDataDetected);
    }

    @Override
    public CollectionSequence create(CollectionSequence collectionSequence){
        try(ExecutionContext executionContext = getExecutionContext()){
            return executionContext.retry (r -> {
                try( CorePolicyLogger timingLogger = new CorePolicyLogger( "create(CollectionSequence)") ) {

                    Collection<CollectionSequenceEntry> collectionSequenceEntries = collectionSequence.collectionSequenceEntries;

                    CollectionSequence result = collectionSequenceRepository.create(executionContext, collectionSequence);
                    if (collectionSequenceEntries != null) {
                        addCollectionSequenceEntries(executionContext, collectionSequenceEntries, result);
                    }
                    return result;
                }
            });
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private ExecutionContext getExecutionContext() {
        return executionContextProvider.getExecutionContext(RepositoryType.CONDITION_ENGINE);
    }

    public CollectionSequence update(CollectionSequence collectionSequence, UpdateBehaviourType updateBehaviourType){
        try(ExecutionContext executionContext = getExecutionContext()){
            return executionContext.retry (r -> {
                List<CollectionSequenceEntry> originalEntries = new ArrayList<>();
                Collection<CollectionSequenceEntryRepository.Item> items = collectionSequenceEntryRepository.retrieveForCollectionSequence(executionContext, collectionSequence.id);
                items.stream().forEach(e -> originalEntries.add(e.collectionSequenceEntry));
                Collection<CollectionSequenceEntry> collectionSequenceEntries = collectionSequence.collectionSequenceEntries;
                CollectionSequence result = collectionSequenceRepository.update(executionContext, collectionSequence);
                if (collectionSequenceEntries != null) {
                    //CAF-1051
                    if(updateBehaviourType == null || updateBehaviourType == UpdateBehaviourType.REPLACE) {
                        collectionSequenceEntryRepository.deleteAll(executionContext, collectionSequence.id);

                        if (result.collectionSequenceEntries != null) {
                            result.collectionSequenceEntries.clear();
                        }
                    } else {
                        result.collectionSequenceEntries = originalEntries;
                    }
                    addCollectionSequenceEntries(executionContext, collectionSequenceEntries, result);
                }
                return result;
            });
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void addCollectionSequenceEntries(ExecutionContext executionContext, Collection<CollectionSequenceEntry> collectionSequenceEntries, CollectionSequence result) {
        try( CorePolicyLogger timingLogger = new CorePolicyLogger( "addCollectionSequenceEntries()") ) {
            if(result.collectionSequenceEntries == null) {
                result.collectionSequenceEntries = new ArrayList<>();
            }
            for (CollectionSequenceEntry collectionSequenceEntry : collectionSequenceEntries) {
                CollectionSequenceEntryRepository.Item item = new CollectionSequenceEntryRepository.Item();
                item.collectionSequenceId = result.id;
                item.collectionSequenceEntry = collectionSequenceEntry;
                result.collectionSequenceEntries.add(collectionSequenceEntryRepository.create(executionContext, item).collectionSequenceEntry);
            }
        }
    }

    @Override
    public Collection<CollectionSequence> retrieveCollectionSequences(Collection<Long> ids){
        if(ids.isEmpty()){
            return new ArrayList<CollectionSequence>();
        }
        try(ExecutionContext executionContext = getExecutionContext()) {
            return executionContext.retryNonTransactional(r -> {
                Collection<CollectionSequence> result = collectionSequenceRepository.retrieve(executionContext, ids);

                for (CollectionSequence collectionSequence : result) {
                    collectionSequence.collectionSequenceEntries = collectionSequenceEntryRepository
                            .retrieveForCollectionSequence(executionContext, collectionSequence.id)
                            .stream().map(e -> e.collectionSequenceEntry).collect(Collectors.toList());
                }

                return result;
            });
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public PageOfResults<CollectionSequence> retrieveCollectionSequencesPage(PageRequest pageRequest){
        try(ExecutionContext executionContext = getExecutionContext()) {
            return executionContext.retryNonTransactional(r -> collectionSequenceRepository.retrievePage(executionContext, pageRequest));
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public PageOfResults<CollectionSequence> retrieveCollectionSequencesPage(PageRequest pageRequest, Filter filter ){
        try(ExecutionContext executionContext = getExecutionContext()) {
            return executionContext.retryNonTransactional(r -> collectionSequenceRepository.retrievePage(executionContext, pageRequest, filter));
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public PageOfResults<CollectionSequence> retrieveCollectionSequencesPage(PageRequest pageRequest, Filter filter, Sort sort) {
        try(ExecutionContext executionContext = getExecutionContext()) {
            return executionContext.retryNonTransactional(r -> collectionSequenceRepository.retrievePage(executionContext, pageRequest, filter, sort));
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public PageOfResults<CollectionSequence> retrieveCollectionSequencesPage(PageRequest pageRequest, Sort sort) {
        try(ExecutionContext executionContext = getExecutionContext()) {
            return executionContext.retryNonTransactional(r -> collectionSequenceRepository.retrievePage(executionContext, pageRequest, sort));
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Collection<CollectionSequence> retrieveCollectionSequencesByName(String name){
        try(ExecutionContext executionContext = getExecutionContext()) {
            return executionContext.retryNonTransactional(r -> collectionSequenceRepository.retrieve(executionContext, name));
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteCollectionSequence(Long id){
        try(ExecutionContext executionContext = getExecutionContext()) {
            executionContext.retryNoReturn(r -> collectionSequenceEntryRepository.deleteAll(executionContext, id));
            executionContext.retryNoReturn(r -> collectionSequenceRepository.delete(executionContext, id));
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public DocumentCollection create(DocumentCollection documentCollection) {
        checkPoliciesAreValid(documentCollection);

        try (ExecutionContext executionContext = getExecutionContext()) {
            return executionContext.retry(r -> {

                try (CorePolicyLogger ourLogger = new CorePolicyLogger("create(DocumentCollection)")) {

                    CollectionRepository.Item item = new CollectionRepository.Item();
                    item.collection = documentCollection;
                    CollectionRepository.Item result = collectionRepository.create(executionContext, item);
                    setCollectionCondition(executionContext, documentCollection, result.collection);

                    if (documentCollection.policyIds != null) {
                        for (Long policyId : documentCollection.policyIds) {
                            collectionRepository.associatePolicyWithCollection(executionContext, policyId, result.collection.id);
                        }
                    }

                    result.collection.policyIds = collectionRepository.getPolicyIdsForCollection(executionContext, result.collection.id);

                    return result.collection;
                }
            });
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void checkPoliciesAreValid(DocumentCollection documentCollection) {
        if (documentCollection.policyIds != null && !documentCollection.policyIds.isEmpty()) {
            try (ExecutionContext executionContext = executionContextProvider.getExecutionContext(RepositoryType.POLICY)) {
                Collection<Policy> retrievedPolicies = policyRepository.retrieve(executionContext, documentCollection.policyIds);
                if (!documentCollection.policyIds.stream().allMatch(id -> retrievedPolicies.stream().anyMatch(u -> u.id.equals(id)))) {
                    throw new RuntimeException("Some of the supplied policy ids do not exist");
                }

                Map<Long, List<Policy>> collect = retrievedPolicies.stream().collect(Collectors.groupingBy(u -> u.typeId));
                if (collect.values().stream().anyMatch(u -> u.size() > 1)) {
                    throw new RuntimeException("A collection can not have multiple policies of the same type");
                }
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public DocumentCollection update(DocumentCollection documentCollection, UpdateBehaviourType updateBehaviourType){
        checkPoliciesAreValid(documentCollection);

        try(ExecutionContext executionContext = getExecutionContext()) {
            return executionContext.retry(r -> {
                if (documentCollection.policyIds != null) {
                    Collection<Long> existingPolicyIds = collectionRepository.getPolicyIdsForCollection(executionContext, documentCollection.id);
                    //CAF-1051
                    if(updateBehaviourType == null || updateBehaviourType == UpdateBehaviourType.REPLACE) {
                        for (Long policyId : existingPolicyIds) {
                            if (!documentCollection.policyIds.contains(policyId)) {
                                collectionRepository.dissociatePolicyFromCollection(executionContext, policyId, documentCollection.id);
                            }
                        }
                    }

                    for (Long policyId : documentCollection.policyIds) {
                        if (!existingPolicyIds.contains(policyId)) {
                            collectionRepository.associatePolicyWithCollection(executionContext, policyId, documentCollection.id);
                        }
                    }
                }

                CollectionRepository.Item item = new CollectionRepository.Item();
                item.collection = documentCollection;
                item = collectionRepository.update(executionContext, item);
                setCollectionCondition(executionContext, documentCollection, item.collection);
                item.collection.policyIds = collectionRepository.getPolicyIdsForCollection(executionContext, item.collection.id);

                return item.collection;
            });
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Collection<DocumentCollection> retrieveCollections(Collection<Long> ids){
        return retrieveCollections(ids, false, false);
    }

    @Override
    public Collection<DocumentCollection> retrieveCollections(Collection<Long> ids,
                                                   boolean includeCondition,
                                                   boolean includeConditionChildren){

        try(ExecutionContext executionContext = getExecutionContext()) {
            return executionContext.retryNonTransactional(r -> {

                Collection<CollectionRepository.Item> items = collectionRepository.retrieve(executionContext, ids);
                for (CollectionRepository.Item item : items) {
                    if (includeCondition && item.conditionId != null) {
                        item.collection.condition = conditionRepository
                                .retrieve(executionContext, Arrays.asList(item.conditionId), includeConditionChildren)
                                .stream().findFirst().get().condition;
                    }
                    item.collection.policyIds = collectionRepository.getPolicyIdsForCollection(executionContext, item.collection.id);
                }
                return items.stream().map(i -> i.collection).collect(Collectors.toList());
            });
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public PageOfResults<DocumentCollection> retrieveCollectionsPage(PageRequest pageRequest){
        try(ExecutionContext executionContext = getExecutionContext()) {
            return executionContext.retryNonTransactional(r -> {
                PageOfResults<CollectionRepository.Item> pageOfResults = collectionRepository.retrievePage(executionContext, pageRequest);
                PageOfResults<DocumentCollection> result = new PageOfResults<>();
                result.totalhits = pageOfResults.totalhits;
                result.results = pageOfResults.results.stream().map(i -> i.collection).collect(Collectors.toList());
                return result;
            });
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public PageOfResults<DocumentCollection> retrieveCollectionsPage(PageRequest pageRequest, Filter filter){
        return retrieveCollectionsPage(pageRequest,filter,null);
    }

    @Override
    public PageOfResults<DocumentCollection> retrieveCollectionsPage(PageRequest pageRequest, Filter filter, Sort sort) {
        try(ExecutionContext executionContext = getExecutionContext()) {
            return executionContext.retryNonTransactional(r -> {
                PageOfResults<CollectionRepository.Item> pageOfResults = collectionRepository.retrievePage(executionContext, pageRequest, filter, sort);
                PageOfResults<DocumentCollection> result = new PageOfResults<>();
                result.totalhits = pageOfResults.totalhits;
                result.results = pageOfResults.results.stream().map(i -> i.collection).collect(Collectors.toList());
                return result;
            });
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public PageOfResults<DocumentCollection> retrieveCollectionsPage(PageRequest pageRequest, Sort sort) {
        return retrieveCollectionsPage(pageRequest,null,sort);
    }

    @Override
    public void deleteCollection(Long id) {
        try(ExecutionContext executionContext = getExecutionContext()) {
            executionContext.retryNoReturn(r -> collectionRepository.delete(executionContext, id));
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void setCollectionCondition(ExecutionContext executionContext, DocumentCollection inputCollection, DocumentCollection resultCollection) {
        if (inputCollection.condition != null) {
            ConditionRepository.Item item = new ConditionRepository.Item();
            item.attachToCollectionId = resultCollection.id;
            item.condition = inputCollection.condition;
            Condition tmpCondition = createCondition(executionContext, item);

            // Now we are at the top, retrive the full tree using this id.
            resultCollection.condition = getFullConditionTree(executionContext, tmpCondition.id);  
        }
    }

    //Condition
    @Override
    public Condition create(Condition condition){
        try(ExecutionContext executionContext = getExecutionContext()) {
            return executionContext.retry(r -> {
                try( CorePolicyLogger timingLogger = new CorePolicyLogger( "create(Condition)") ) {
                    ConditionRepository.Item item = new ConditionRepository.Item();
                    item.condition = condition;

                    item.parentConditionId = condition.parentConditionId;

                    Condition tmpCondition = createCondition(executionContext, item);

                    // Now we are at the top, retrive the full tree using this id.
                    return getFullConditionTree(executionContext, tmpCondition.id);
                }
            });
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private Condition createCondition(ExecutionContext executionContext, ConditionRepository.Item item ) {

        try( CorePolicyLogger timingLogger = new CorePolicyLogger( "create(Condition)") ) {
            ConditionRepository.Item returnItem = conditionRepository.create(executionContext, item);

            if (item.condition instanceof BooleanCondition) {
                BooleanCondition originalBooleanCondition = (BooleanCondition) item.condition;
                BooleanCondition returnedBooleanCondition = (BooleanCondition) returnItem.condition;
                if (originalBooleanCondition.children != null) {
                    for (Condition childCondition : originalBooleanCondition.children) {
                        ConditionRepository.Item childItem = new ConditionRepository.Item();
                        childItem.condition = childCondition;
                        childItem.parentConditionId = returnItem.condition.id;
                        if (returnedBooleanCondition.children == null) {
                            returnedBooleanCondition.children = new LinkedList<>();
                        }
                        returnedBooleanCondition.children.add(createCondition(executionContext, childItem));
                    }
                }
            }

            if (item.condition.conditionType == ConditionType.NOT) {
                NotCondition originalNotCondition = (NotCondition) item.condition;
                NotCondition returnedNotCondition = (NotCondition) returnItem.condition;
                if (originalNotCondition.condition != null) {
                    ConditionRepository.Item childItem = new ConditionRepository.Item();
                    childItem.condition = originalNotCondition.condition;
                    childItem.parentConditionId = returnedNotCondition.id;
                    returnedNotCondition.condition = createCondition(executionContext, childItem);
                }
            }

            timingLogger.log(" Created condition: " + returnItem.condition.id);

            return returnItem.condition;
        }
    }

    private Condition getFullConditionTree(ExecutionContext executionContext, Long startingConditionId) {

        try(CorePolicyLogger corePolicyLogger = new CorePolicyLogger("getFullConditionTree(Condition)")) {
            // Otherwise, we are at the top level we need to replace the temporary tree with a real tree.
            Collection<ConditionRepository.Item> items = conditionRepository.retrieve(executionContext, Arrays.asList(startingConditionId), true);
            Collection<Condition> result = items.stream().map(i -> i.condition).collect(Collectors.toList());

            if (result.size() == 0) {
                throw new RuntimeException(new Exception("Failed to locate our commit conditions"));
            }

            Condition topLevelCondition = result.stream().findFirst().get();

            // as var for handy watch in debug mode.
            return topLevelCondition;
        }
    }

    public Condition update(Condition condition, UpdateBehaviourType updateBehaviourType){
        try(ExecutionContext executionContext = getExecutionContext()) {
            return executionContext.retry(r -> {

                ConditionRepository.Item item = new ConditionRepository.Item();
                item.condition = condition;

                item.parentConditionId = condition.parentConditionId;

                ConditionRepository.Item returnItem = conditionRepository.update(executionContext, item);

                if (item.condition.conditionType == ConditionType.BOOLEAN) {
                    BooleanCondition originalBooleanCondition = (BooleanCondition) item.condition;
                    BooleanCondition returnedBooleanCondition = (BooleanCondition) returnItem.condition;
                    if (originalBooleanCondition.children != null) {
                        returnedBooleanCondition.children = new LinkedList<>();
                        //CAF-1051
                        if(updateBehaviourType == null || updateBehaviourType == UpdateBehaviourType.REPLACE) {
                            conditionRepository.deleteChildren(executionContext, originalBooleanCondition.id);
                        }
//                        int order = 1;
                        for (Condition childCondition : originalBooleanCondition.children) {
//                            if(childCondition.order== null){
//                                childCondition.order = order++;
//                            }
                            childCondition.parentConditionId = returnedBooleanCondition.id;
                            ConditionRepository.Item childItem = new ConditionRepository.Item();
                            childItem.condition = childCondition;
                            childItem.parentConditionId = returnedBooleanCondition.id;
                            returnedBooleanCondition.children.add(createCondition(executionContext, childItem));
                        }
                    }
                }

                if (item.condition.conditionType == ConditionType.NOT) {
                    NotCondition originalNotCondition = (NotCondition) item.condition;
                    NotCondition returnedNotCondition = (NotCondition) returnItem.condition;
                    if (originalNotCondition.condition != null) {
                        conditionRepository.deleteChildren(executionContext, originalNotCondition.id);
                        originalNotCondition.condition.parentConditionId = returnedNotCondition.id;
                        ConditionRepository.Item childItem = new ConditionRepository.Item();
                        childItem.condition = originalNotCondition.condition;
                        childItem.parentConditionId = returnedNotCondition.id;
                        returnedNotCondition.condition = createCondition(executionContext, childItem);
                    }
                }

                // Now we are at the top, retrive the full tree using this id.
                return getFullConditionTree(executionContext, returnItem.condition.id);
            });
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteCondition(Long id){
        try(ExecutionContext executionContext = getExecutionContext()) {
            executionContext.retryNoReturn(r -> conditionRepository.delete(executionContext, id));
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public PageOfResults<Condition> retrieveConditionsPage (PageRequest pageRequest, Filter filter )
    {
        return retrieveConditionsPage(pageRequest,filter,null);
    }

    @Override
    public PageOfResults<Condition> retrieveConditionsPage(PageRequest pageRequest, Filter filter, Sort sort) {
        try(ExecutionContext executionContext = getExecutionContext()) {
            return executionContext.retryNonTransactional(r -> {
                PageOfResults<ConditionRepository.Item> pageOfResults = conditionRepository.retrievePage(executionContext, pageRequest, filter, sort);
                PageOfResults<Condition> result = new PageOfResults<>();
                result.totalhits = pageOfResults.totalhits;
                result.results = pageOfResults.results.stream().map(i -> i.condition).collect(Collectors.toList());

                return result;
            });
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public PageOfResults<Condition> retrieveConditionsPage(PageRequest pageRequest, Sort sort) {
        return retrieveConditionsPage(pageRequest,null,sort);
    }

    @Override
    public PageOfResults<Condition> retrieveConditionFragmentsPage(PageRequest pageRequest){
        try(ExecutionContext executionContext = getExecutionContext()) {
            return executionContext.retryNonTransactional(r -> {
                // Filter on is_fragment=true.
                PageOfResults<ConditionRepository.Item> pageOfResults = conditionRepository.retrievePage(executionContext, pageRequest, filterIsFragment);
                PageOfResults<Condition> result = new PageOfResults<>();
                result.totalhits = pageOfResults.totalhits;
                result.results = pageOfResults.results.stream().map(i -> i.condition).collect(Collectors.toList());

                return result;
            });
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Collection<Condition> retrieveConditions(Collection<Long> ids, Boolean includeChildren) {
        try (ExecutionContext executionContext = getExecutionContext()) {
            return executionContext.retryNonTransactional(r -> {
                Collection<ConditionRepository.Item> items = conditionRepository.retrieve(executionContext, ids, includeChildren);
                return items.stream().map(i -> i.condition).collect(Collectors.toList());
            });
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    //Lexicon
    @Override
    public Lexicon create(Lexicon lexicon){
        try(ExecutionContext executionContext = getExecutionContext()) {
            return executionContext.retry(r -> {
                Collection<LexiconExpression> lexiconExpressions = lexicon.lexiconExpressions;
                Lexicon result = lexiconRepository.create(executionContext, lexicon);

                result.lexiconExpressions = new ArrayList<>();
                if (lexiconExpressions!= null) {
                    for (LexiconExpression lexiconExpression : lexiconExpressions) {
                        lexiconExpression.lexiconId = result.id;
                        result.lexiconExpressions.add(lexiconExpressionRepository.create(executionContext, lexiconExpression));
                    }
                }

                return result;
            });
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public Lexicon update(Lexicon lexicon, UpdateBehaviourType updateBehaviourType){
        try(ExecutionContext executionContext = getExecutionContext()) {
            return executionContext.retry(r -> {

                Collection<LexiconExpression> lexiconExpressions = lexicon.lexiconExpressions;
                Lexicon result = lexiconRepository.update(executionContext, lexicon);

                result.lexiconExpressions = lexiconExpressionRepository.retrieve(executionContext, result.id);

                if (updateBehaviourType == UpdateBehaviourType.ADD) {
                    for (LexiconExpression lexiconExpression : lexiconExpressions) {
                        lexiconExpression.lexiconId = result.id;
                    }
                    result.lexiconExpressions = lexiconExpressionRepository.createAll(executionContext, lexiconExpressions);
                    return result;
                }

                if (lexiconExpressions != null) {
                    if (result.lexiconExpressions != null) {
                        for (LexiconExpression lexiconExpression : result.lexiconExpressions) {
                            lexiconExpressionRepository.delete(executionContext, lexiconExpression.id);
                        }
                    }
                    result.lexiconExpressions = new ArrayList<>();

                    for (LexiconExpression lexiconExpression : lexiconExpressions) {
                        lexiconExpression.lexiconId = result.id;
                    }
                    result.lexiconExpressions = lexiconExpressionRepository.createAll(executionContext, lexiconExpressions);
                }

                return result;
            });
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteLexicon(Long id) {
        try (ExecutionContext executionContext = getExecutionContext()) {
            executionContext.retryNoReturn(r -> lexiconRepository.delete(executionContext, id));
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public PageOfResults<Lexicon> retrieveLexiconsPage(PageRequest pageRequest) {
        try (ExecutionContext executionContext = getExecutionContext()) {
            return executionContext.retryNonTransactional(r -> lexiconRepository.retrievePage(executionContext, pageRequest));
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public PageOfResults<Lexicon> retrieveLexiconsPage(PageRequest pageRequest, Filter filter ) {
        try (ExecutionContext executionContext = getExecutionContext()) {
            return executionContext.retryNonTransactional(r -> lexiconRepository.retrievePage(executionContext, pageRequest, filter));
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Collection<Lexicon> retrieveLexicons(Collection<Long> ids) {
        try (ExecutionContext executionContext = getExecutionContext()) {
            return executionContext.retryNonTransactional(r -> {
                Collection<Lexicon> lexicons = lexiconRepository.retrieve(executionContext, ids);
                for (Lexicon lexicon : lexicons) {
                    lexicon.lexiconExpressions = lexiconExpressionRepository.retrieve(executionContext, lexicon.id);
                }
                return lexicons;
            });
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //Lexicon expression
    @Override
    public LexiconExpression create(LexiconExpression t) {
        try (ExecutionContext executionContext = getExecutionContext()) {
            return executionContext.retry(r -> lexiconExpressionRepository.create(executionContext, t));
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public LexiconExpression update(LexiconExpression t, UpdateBehaviourType updateBehaviourType){
        try(ExecutionContext executionContext = getExecutionContext()){
            return executionContext.retry (r -> lexiconExpressionRepository.update(executionContext, t));
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteLexiconExpression(Long id){
        try(ExecutionContext executionContext = getExecutionContext()) {
            executionContext.retryNoReturn(r -> lexiconExpressionRepository.delete(executionContext, id));
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public PageOfResults<LexiconExpression> retrieveLexiconExpressionsPage(PageRequest pageRequest){
        try(ExecutionContext executionContext = getExecutionContext()) {
            return executionContext.retryNonTransactional(r -> lexiconExpressionRepository.retrievePage(executionContext, pageRequest));
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public PageOfResults<LexiconExpression> retrieveLexiconExpressionsPage(PageRequest pageRequest, Filter filter ){
        try(ExecutionContext executionContext = getExecutionContext()) {
            return executionContext.retryNonTransactional(r -> lexiconExpressionRepository.retrievePage(executionContext, pageRequest, filter ));
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Collection<LexiconExpression> retrieveLexiconExpressions(Collection<Long> ids){
        try(ExecutionContext executionContext = getExecutionContext()) {
            return executionContext.retryNonTransactional(r -> lexiconExpressionRepository.retrieve(executionContext, ids));
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public FieldLabel retrieveFieldLabel(String fieldName) {
        try(ExecutionContext executionContext = getExecutionContext()) {
            return executionContext.retryNonTransactional(r -> fieldLabelRepository.retrieve(executionContext, fieldName));
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public FieldLabel create(FieldLabel t) {
        try(ExecutionContext executionContext = getExecutionContext()) {
            return executionContext.retry(r -> fieldLabelRepository.create(executionContext, t));
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public FieldLabel update(FieldLabel t, UpdateBehaviourType updateBehaviourType) {
        try(ExecutionContext executionContext = getExecutionContext()) {
            return executionContext.retry(r -> fieldLabelRepository.update(executionContext, t));
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteFieldLabel(Long id) {
        try(ExecutionContext executionContext = getExecutionContext()) {
            executionContext.retryNoReturn(r -> fieldLabelRepository.delete(executionContext, id));
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public PageOfResults<FieldLabel> retrieveFieldLabelPage(PageRequest pageRequest) {
        try(ExecutionContext executionContext = getExecutionContext()) {
            return executionContext.retryNonTransactional(r -> fieldLabelRepository.retrievePage(executionContext, pageRequest));
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public PageOfResults<FieldLabel> retrieveFieldLabelPage(PageRequest pageRequest, Filter filter ) {
        try(ExecutionContext executionContext = getExecutionContext()) {
            return executionContext.retryNonTransactional(r -> fieldLabelRepository.retrievePage(executionContext, pageRequest, filter));
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
