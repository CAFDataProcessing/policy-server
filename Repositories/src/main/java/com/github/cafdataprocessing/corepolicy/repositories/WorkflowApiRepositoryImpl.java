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

import com.github.cafdataprocessing.corepolicy.common.WorkflowApi;
import com.github.cafdataprocessing.corepolicy.common.dto.*;
import com.github.cafdataprocessing.corepolicy.common.exceptions.BackEndRequestFailedCpeException;
import com.github.cafdataprocessing.corepolicy.common.exceptions.CpeException;
import com.github.cafdataprocessing.corepolicy.common.exceptions.excpetionErrors.DataOperationFailureErrors;
import com.github.cafdataprocessing.corepolicy.common.shared.CorePolicyLogger;
import com.github.cafdataprocessing.corepolicy.repositories.v2.ExecutionContext;
import com.github.cafdataprocessing.corepolicy.repositories.v2.SequenceWorkflowEntryRepository;
import com.github.cafdataprocessing.corepolicy.repositories.v2.SequenceWorkflowRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Workflow API implementation
 */
@Component
public class WorkflowApiRepositoryImpl implements WorkflowApi {
    private final ExecutionContextProvider executionContextProvider;
    private final SequenceWorkflowRepository sequenceWorkflowRepository;
    private final SequenceWorkflowEntryRepository sequenceWorkflowEntryRepository;

    @Autowired
    public WorkflowApiRepositoryImpl(SequenceWorkflowRepository sequenceWorkflowRepository,
                                     SequenceWorkflowEntryRepository sequenceWorkflowEntryRepository,
                                  @Qualifier("repositoryExecutionContextProvider") ExecutionContextProvider executionContextProvider ) {


        this.sequenceWorkflowRepository = sequenceWorkflowRepository;
        this.sequenceWorkflowEntryRepository = sequenceWorkflowEntryRepository;
        this.executionContextProvider = executionContextProvider;
    }

    private ExecutionContext getExecutionContext() {
        // for now workflow information is going into the condition engine DB.
        return executionContextProvider.getExecutionContext(RepositoryType.CONDITION_ENGINE);
    }

    @Override
    public SequenceWorkflow create(SequenceWorkflow collectionSequence) {

        try(ExecutionContext executionContext = getExecutionContext()){
            return executionContext.retry (r -> {
                try (CorePolicyLogger timingLogger = new CorePolicyLogger("create(SequenceWorkflow)")) {

                    Collection<SequenceWorkflowEntry> sequenceWorkflowEntries = collectionSequence.sequenceWorkflowEntries;

                    SequenceWorkflow result = sequenceWorkflowRepository.create(executionContext, collectionSequence);

                    if (sequenceWorkflowEntries != null) {
                        addSequenceWorkflowEntries(executionContext, sequenceWorkflowEntries, result);
                    }
                    return result;
                }
            });
        } catch (CpeException e) {
            throw e;
        } catch (Exception e) {
            throw new BackEndRequestFailedCpeException(new DataOperationFailureErrors(DataOperationFailureErrors.FailureOperation.CREATE, ItemType.SEQUENCE_WORKFLOW), e);
        }
    }

    @Override
    public SequenceWorkflow update(SequenceWorkflow sequenceWorkflow) {

        try(ExecutionContext executionContext = getExecutionContext()){
            return executionContext.retry (r -> {

                Collection<SequenceWorkflowEntry> sequenceWorkflowEntries = sequenceWorkflow.sequenceWorkflowEntries;
                SequenceWorkflow result = sequenceWorkflowRepository.update(executionContext, sequenceWorkflow);
                if (sequenceWorkflowEntries != null) {

                    sequenceWorkflowEntryRepository.deleteAll(executionContext, sequenceWorkflow.id);

                    if(result.sequenceWorkflowEntries != null) {
                        result.sequenceWorkflowEntries.clear();
                    }
                    addSequenceWorkflowEntries(executionContext, sequenceWorkflowEntries, result);
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
    public void deleteSequenceWorkflow(Long id) {

        try(ExecutionContext executionContext = getExecutionContext()) {
            executionContext.retryNoReturn(r -> sequenceWorkflowEntryRepository.deleteAll(executionContext, id));
            executionContext.retryNoReturn(r -> sequenceWorkflowRepository.delete(executionContext, id));
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public PageOfResults<SequenceWorkflow> retrieveSequenceWorkflowsPage(PageRequest pageRequest) {
        try (ExecutionContext executionContext = getExecutionContext()) {
            return executionContext.retryNonTransactional(r -> sequenceWorkflowRepository.retrievePage(executionContext, pageRequest));
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public PageOfResults<SequenceWorkflow> retrieveSequenceWorkflowsPage(PageRequest pageRequest, Filter filter ) {
        try (ExecutionContext executionContext = getExecutionContext()) {
            return executionContext.retryNonTransactional(r -> sequenceWorkflowRepository.retrievePage(executionContext, pageRequest, filter));
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public PageOfResults<SequenceWorkflowEntry> retrieveSequenceWorkflowEntriesPage(PageRequest pageRequest) {
        try (ExecutionContext executionContext = getExecutionContext()) {
            return executionContext.retryNonTransactional(r -> convertSequenceEntry(sequenceWorkflowEntryRepository.retrievePage(executionContext, pageRequest)));
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public PageOfResults<SequenceWorkflowEntry> retrieveSequenceWorkflowEntriesPage(PageRequest pageRequest, Filter filter, Sort sort) {
        try (ExecutionContext executionContext = getExecutionContext()) {
            return executionContext.retryNonTransactional(r -> convertSequenceEntry(sequenceWorkflowEntryRepository.retrievePage(executionContext, pageRequest, filter, sort)));
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public PageOfResults<SequenceWorkflowEntry> retrieveSequenceWorkflowEntriesPage(PageRequest pageRequest, Filter filter) {
        try (ExecutionContext executionContext = getExecutionContext()) {
            return executionContext.retryNonTransactional(r -> convertSequenceEntry(sequenceWorkflowEntryRepository.retrievePage(executionContext,pageRequest,filter)));
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public PageOfResults<SequenceWorkflowEntry> retrieveSequenceWorkflowEntriesPage(PageRequest pageRequest, Filter filter, Boolean includeCollectionSequences) {
        try (ExecutionContext executionContext = getExecutionContext()) {
            return executionContext.retryNonTransactional(r -> convertSequenceEntry(sequenceWorkflowEntryRepository.retrievePage(executionContext,pageRequest,filter,null,includeCollectionSequences)));
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public PageOfResults<SequenceWorkflowEntry> retrieveSequenceWorkflowEntriesPage(PageRequest pageRequest, Filter filter, Sort sort, Boolean includeCollectionSequences) {
        try (ExecutionContext executionContext = getExecutionContext()) {
            return executionContext.retryNonTransactional(r -> convertSequenceEntry(sequenceWorkflowEntryRepository.retrievePage(executionContext,pageRequest,filter,sort,includeCollectionSequences)));
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private PageOfResults<SequenceWorkflowEntry> convertSequenceEntry(PageOfResults<SequenceWorkflowEntryRepository.Item> pageOfResults){
        PageOfResults<SequenceWorkflowEntry> converted = new PageOfResults<>();
        converted.results = new ArrayList<>();
        converted.totalhits = pageOfResults.totalhits;
        pageOfResults.results.forEach(e -> {
            SequenceWorkflowEntry entry = new SequenceWorkflowEntry();
            entry.id = e.sequenceWorkflowEntry.id;
            entry.collectionSequenceId = e.sequenceWorkflowEntry.collectionSequenceId;
            entry.order = e.sequenceWorkflowEntry.order;
            entry.collectionSequence = e.sequenceWorkflowEntry.collectionSequence;
            entry.sequenceWorkflowId = e.sequenceWorkflowEntry.sequenceWorkflowId;
            converted.results.add(entry);
        });
        return converted;
    }

    @Override
    public Collection<SequenceWorkflow> retrieveSequenceWorkflows(Collection<Long> ids) {
        try(ExecutionContext executionContext = getExecutionContext()) {
            return executionContext.retryNonTransactional(r -> {
                Collection<SequenceWorkflow> result = sequenceWorkflowRepository.retrieve(executionContext, ids);

                for (SequenceWorkflow sequenceWorkflow : result) {
                    sequenceWorkflow.sequenceWorkflowEntries= sequenceWorkflowEntryRepository
                            .retrieveForSequenceWorkflow(executionContext, sequenceWorkflow.id)
                            .stream().map(e -> e.sequenceWorkflowEntry).collect(Collectors.toList());
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
    public SequenceWorkflow retrieveSequenceWorkflow(Long id) {
        Collection<SequenceWorkflow> items = retrieveSequenceWorkflows(Arrays.asList(id));
        if (items.isEmpty()) return null;
        return items.stream().findFirst().get();
    }


    private void addSequenceWorkflowEntries(ExecutionContext executionContext, Collection<SequenceWorkflowEntry> sequenceWorkflowEntries, SequenceWorkflow result) {
        try( CorePolicyLogger timingLogger = new CorePolicyLogger( "addSequenceWorkflowEntries()") ) {
            result.sequenceWorkflowEntries = new ArrayList<>();
            for (SequenceWorkflowEntry sequenceWorkflowEntry : sequenceWorkflowEntries) {
                SequenceWorkflowEntryRepository.Item item = new SequenceWorkflowEntryRepository.Item();
                item.sequenceWorkflowId = result.id;
                item.sequenceWorkflowEntry = sequenceWorkflowEntry;
                result.sequenceWorkflowEntries.add(sequenceWorkflowEntryRepository.create(executionContext, item).sequenceWorkflowEntry);
            }
        }
    }
}
