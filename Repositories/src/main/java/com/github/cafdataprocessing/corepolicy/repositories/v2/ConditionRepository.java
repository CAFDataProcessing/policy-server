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

import com.github.cafdataprocessing.corepolicy.common.dto.conditions.Condition;

import java.util.Collection;

/**
 *
 */
public interface ConditionRepository extends Repository<ConditionRepository.Item> {
    Collection<Item> retrieve(ExecutionContext executionContext, Collection<Long> ids, Boolean includeChildren);

    void deleteChildren(ExecutionContext executionContext, Long id);

    public static class Item<TCondition extends Condition> {
        public Long attachToCollectionId;
        public Long parentConditionId;

        //Used when retrieving with children
        public Long attachedToPatentId;

        public TCondition condition;
    }

}
