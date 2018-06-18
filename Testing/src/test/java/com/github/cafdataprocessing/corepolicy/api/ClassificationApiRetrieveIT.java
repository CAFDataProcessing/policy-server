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

import com.github.cafdataprocessing.corepolicy.common.dto.*;
import com.github.cafdataprocessing.corepolicy.common.dto.conditions.ExistsCondition;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;

/**
 *
 */
@RunWith(Parameterized.class)
public class ClassificationApiRetrieveIT extends ClassificationApiTestBase {
    private final ItemType itemType;
    Consumer<Integer> create;
    Function<PageRequest, PageOfResults<?>> retrieve;

    public ClassificationApiRetrieveIT(ItemType itemType) {
        this.itemType = itemType;
        switch (itemType) {
            case COLLECTION_SEQUENCE: {
                this.create = (index) -> {
                    CollectionSequence collectionSequence = new CollectionSequence();
                    collectionSequence.name = String.valueOf(index);
                    sut.create(collectionSequence);
                };
                this.retrieve = sut::retrieveCollectionSequencesPage;
                break;
            }
            case COLLECTION: {
                this.create = (index) -> {
                    DocumentCollection collection = new DocumentCollection();
                    collection.name = String.valueOf(index);
                    sut.create(collection);
                };
                this.retrieve = sut::retrieveCollectionsPage;
                break;
            }
            case CONDITION: {
                this.create = (index) -> {
                    ExistsCondition existsCondition = new ExistsCondition();
                    existsCondition.name = String.valueOf(index);
                    existsCondition.field = "some field";
                    sut.create(existsCondition);
                };
                this.retrieve = sut::retrieveConditionFragmentsPage;
                break;
            }
            case LEXICON: {
                this.create = (index) -> {
                    Lexicon lexicon = new Lexicon();
                    lexicon.name = String.valueOf(index);
                    sut.create(lexicon);
                };
                this.retrieve = sut::retrieveLexiconsPage;
                break;
            }
        }
    }

    @Parameterized.Parameters
    public static Collection<Object[]> getParameters() {
        //TODO Can't page lexicon expressions right now.
        return Arrays.asList(new Object[][]{
                {ItemType.COLLECTION_SEQUENCE}, {ItemType.COLLECTION}, {ItemType.CONDITION}, {ItemType.LEXICON}
        });
    }

    @Test
    public void testRetrieve() throws Exception {
        System.out.println(itemType.toValue());

        // Ok when creating we need to find out what our first ID is.
        // So we know how many we have in the system, and where to start our paging from.
        Long totalHits = 0L;
        Long startIndex = 0L;

        {
            // before we create any new collection sequences find out how many are in system!
            PageOfResults pageOfResults = retrieve.apply( new PageRequest(1L, 1L));

            totalHits = pageOfResults.totalhits;
            startIndex = totalHits;
        }

        // All requests should now find totalHits + 15L new results.
        // Start index is how many hits we have in the system, prior to our new items
        totalHits = totalHits + 15L;

        for (int i = 0; i < 15; i++) {
            create.accept(i);
        }

        {
            PageRequest pageRequest = new PageRequest();
            pageRequest.start = startIndex + 1L;
            pageRequest.max_page_results = 10L;
            PageOfResults pageOfResults = retrieve.apply(pageRequest);
            assertEquals(totalHits, pageOfResults.totalhits);
            assertEquals(10, pageOfResults.results.size());
        }

        {
            PageRequest pageRequest = new PageRequest();
            pageRequest.start = startIndex + 1L;
            pageRequest.max_page_results = 15L;
            PageOfResults pageOfResults = retrieve.apply(pageRequest);
            assertEquals(totalHits, pageOfResults.totalhits);
            assertEquals(15, pageOfResults.results.size());
        }

        {
            PageRequest pageRequest = new PageRequest();
            pageRequest.start = startIndex + 5L;
            pageRequest.max_page_results = 10L;
            PageOfResults pageOfResults = retrieve.apply(pageRequest);
            assertEquals(totalHits, pageOfResults.totalhits);
            assertEquals(10, pageOfResults.results.size());
        }

        {
            PageRequest pageRequest = new PageRequest();
            pageRequest.start = startIndex + 14L;
            pageRequest.max_page_results = 10L;
            PageOfResults pageOfResults = retrieve.apply(pageRequest);
            assertEquals(totalHits, pageOfResults.totalhits);
            assertEquals(2, pageOfResults.results.size());
        }
        {
            PageRequest pageRequest = new PageRequest();
            pageRequest.start = startIndex + 30L;
            pageRequest.max_page_results = 10L;
            PageOfResults pageOfResults = retrieve.apply(pageRequest);
            assertEquals(totalHits, pageOfResults.totalhits);
            assertEquals(0, pageOfResults.results.size());
        }
    }

    @Test(expected = RuntimeException.class)
    public void testRetrieveCollectionsPageWrongStartThrows() throws Exception {
        create.accept(1);

        PageRequest pageRequest = new PageRequest();
        pageRequest.start = 0L;
        pageRequest.max_page_results = 10L;

        retrieve.apply(pageRequest);
    }

    @Test(expected = RuntimeException.class)
    public void testRetrieveCollectionsPageWrongMaxPageResultsThrows() throws Exception {
        create.accept(1);

        PageRequest pageRequest = new PageRequest();
        pageRequest.start = 1L;
        pageRequest.max_page_results = 0L;

        retrieve.apply(pageRequest);
    }

}
