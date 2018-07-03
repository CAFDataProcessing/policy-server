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
package com.github.cafdataprocessing.corepolicy.repositories.v2;

import com.github.cafdataprocessing.corepolicy.common.dto.Filter;
import com.github.cafdataprocessing.corepolicy.common.dto.PageOfResults;
import com.github.cafdataprocessing.corepolicy.common.dto.PageRequest;
import com.github.cafdataprocessing.corepolicy.common.dto.Sort;

import java.util.Collection;

/**
 *
 */
public interface Repository<Type> {
    Type create(ExecutionContext executionContext, Type t);
    Type update(ExecutionContext executionContext, Type t);
    void delete(ExecutionContext executionContext, Long id);
    //createAll method has been added to the LexiconRepository interface.
    PageOfResults<Type> retrievePage(ExecutionContext executionContext, PageRequest pageRequest);
    PageOfResults<Type> retrievePage(ExecutionContext executionContext, PageRequest pageRequest, Filter filter );
    PageOfResults<Type> retrievePage(ExecutionContext executionContext, PageRequest pageRequest, Filter filter, Sort sort );
    PageOfResults<Type> retrievePage(ExecutionContext executionContext, PageRequest pageRequest, Sort sort );
    Collection<Type> retrieve(ExecutionContext executionContext, Collection<Long> ids);
}
