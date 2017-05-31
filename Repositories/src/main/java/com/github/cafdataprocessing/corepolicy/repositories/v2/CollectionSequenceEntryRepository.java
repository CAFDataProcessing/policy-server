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
package com.github.cafdataprocessing.corepolicy.repositories.v2;

import com.github.cafdataprocessing.corepolicy.common.dto.CollectionSequenceEntry;
import com.github.cafdataprocessing.corepolicy.common.dto.DtoBase;

import java.util.Collection;

/**
 *
 */
public interface CollectionSequenceEntryRepository  extends Repository<CollectionSequenceEntryRepository.Item>{

    Collection<CollectionSequenceEntryRepository.Item> retrieveForCollectionSequence(ExecutionContext executionContext, Long collectionSequenceId);

    void deleteAll(ExecutionContext executionContext, Long collectionSequenceId);

    public static class Item extends DtoBase {
        public Long collectionSequenceId;
        public CollectionSequenceEntry collectionSequenceEntry;
    }
}
