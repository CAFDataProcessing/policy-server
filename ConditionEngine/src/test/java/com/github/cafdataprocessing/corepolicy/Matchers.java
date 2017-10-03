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
package com.github.cafdataprocessing.corepolicy;

import org.hamcrest.Description;
import org.mockito.ArgumentMatcher;

import java.util.Collection;

import static org.mockito.Matchers.argThat;

/**
 * Assist tests in verifying that collections are matches.
 */
public class Matchers {
    /**
     * Custom matcher for verifying actual and expected ValueObjects match.
     */
    static class CollectionMatches<T> extends ArgumentMatcher<Collection<T>> {

        private final Collection<T> expected;

        public CollectionMatches(Collection<T> expected) {
            this.expected = expected;
        }

        @Override
        public boolean matches(Object actual) {
            if(actual==null){
                return false;
            }

            Collection<T> actualCollection = (Collection<T>)actual;
            if(expected.size()!=actualCollection.size()){
                return false;
            }
            for(T item: actualCollection){
                if(!expected.contains(item)){
                    return false;
                }
            }
            return true;
        }

        @Override
        public void describeTo(Description description) {
            description.appendText(expected == null ? null : expected.toString());
        }
    }

    /**
     * Checks if the items in a list match.
     */
    public static <T> Collection<T> collectionMatches(Collection<T> expected) {
        CollectionMatches<T> tValueObjectMatcher = new CollectionMatches<>(expected);
        return argThat(tValueObjectMatcher);
    }
}
