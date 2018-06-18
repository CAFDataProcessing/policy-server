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
package com.github.cafdataprocessing.corepolicy.common;

import com.google.common.collect.*;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * A case insensitive implementation of a multimap.
 */
public class CaseInsensitiveKeyMultimap<V> implements Multimap<String, V> {

        private Multimap<String, V> inner;

        public CaseInsensitiveKeyMultimap(){

            inner = TreeMultimap.create(new IgnoreCaseStringComparator(), new NonComparableComparator());
        }

        public static <String> CaseInsensitiveKeyMultimap<String> create(){
            return new CaseInsensitiveKeyMultimap<>();
        }

        @Override
        public int size() {
            return inner.size();
        }

        @Override
        public boolean isEmpty() {
            return inner.isEmpty();
        }

        @Override
        public boolean containsKey(Object o) {
            return inner.containsKey(o);
        }

        @Override
        public boolean containsValue(@Nullable Object o) {
            return inner.containsValue(o);
        }

        @Override
        public boolean containsEntry(Object o, @Nullable Object o2) {
            return inner.containsEntry(o, o2);
        }

        /*
        Put a name and value into the multimap. Null names are invalid.
         */
        @Override
        public boolean put(String s, @Nullable V v) {
            return inner.put(s, v);
        }

        @Override
        public boolean remove(Object o, @Nullable Object o2) {
            return inner.remove(o, o2);
        }

        @Override
        public boolean putAll(String s, Iterable<? extends V> vs) {
            return inner.putAll(s, vs);
        }

        @Override
        public boolean putAll(Multimap<? extends String, ? extends V> multimap) {
            return inner.putAll(multimap);
        }

        @Override
        public Collection<V> replaceValues(String s, Iterable<? extends V> vs) {
            return inner.replaceValues(s, vs);
        }

        @Override
        public Collection<V> removeAll(@Nullable Object o) {
            return inner.removeAll(o);
        }

        @Override
        public void clear() {
            inner.clear();
        }

        @Override
        public Collection<V> get(String s) {
            return inner.get(s);
        }

        @Override
        public Set<String> keySet() {
            return inner.keySet();
        }

        @Override
        public Multiset<String> keys() {
            return inner.keys();
        }

        @Override
        public Collection<V> values() {
            return inner.values();
        }

        @Override
        public Collection<Map.Entry<String, V>> entries() {
            return inner.entries();
        }

        @Override
        public Map<String, Collection<V>> asMap() {
            return inner.asMap();
        }
    }
