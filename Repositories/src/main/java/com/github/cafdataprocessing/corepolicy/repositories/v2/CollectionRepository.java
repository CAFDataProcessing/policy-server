/*
 * Copyright 2015-2017 EntIT Software LLC, a Micro Focus company.
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

import com.github.cafdataprocessing.corepolicy.common.FilterName;
import com.github.cafdataprocessing.corepolicy.common.HibernateWrappedItem;
import com.github.cafdataprocessing.corepolicy.common.dto.DocumentCollection;
import java.util.Set;

/**
 *
 */
public interface CollectionRepository extends Repository<CollectionRepository.Item> {
    public static class Item  {
        public static final String CollectionWrapperFilterName = "collection"; // filter name is of format x_y not xY

        // Internal Only.  As the collectionrespository$Item is what is used by hibernate, we need
        // to be able to perform the look up from the dto on the object Collection for Condition.Id.
        // Allow the dto property to map to this field.
        @FilterName("condition.id")
        public Long conditionId;

        @FilterName(CollectionWrapperFilterName)
        @HibernateWrappedItem("collection")
        public DocumentCollection collection = new DocumentCollection();

        //Used by hibernate
//        private Long id;

        private Long getId() {
            if(collection == null){
                return null;
            }
            return collection.id;
        }

        private void setId(Long id) {
//            if(collection == null) {
//                collection = new DocumentCollection();
//            }
            collection.id = id;
        }
    }

    public void associatePolicyWithCollection(ExecutionContext executionContext, long policyId, long collectionId);
    public void dissociatePolicyFromCollection(ExecutionContext executionContext, long policyId, long collectionId);
    public Set<Long> getPolicyIdsForCollection(ExecutionContext executionContext, long collectionId);
    Set<Long> getCollectionIdsForPolicy(ExecutionContext executionContext, long policyId);
}
