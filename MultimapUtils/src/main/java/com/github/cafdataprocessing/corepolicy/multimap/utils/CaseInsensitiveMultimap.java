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
package com.github.cafdataprocessing.corepolicy.multimap.utils;

import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;
import com.google.common.collect.Ordering;
import com.google.common.collect.TreeMultimap;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;

/**
 * A case insensitive multimap where the key is a string
 */
public class CaseInsensitiveMultimap<V extends Comparable> implements Multimap<String, V> {

    class IgnoreCaseStringComparator implements Comparator<String> {
        public int compare(String strA, String strB) {
            return strA.compareToIgnoreCase(strB);
        }
    }

    private Multimap<String, V> inner;

    public CaseInsensitiveMultimap(){
        inner = TreeMultimap.create(new IgnoreCaseStringComparator(), Ordering.natural().<V>nullsFirst());
    }

    public static <T extends Comparable> CaseInsensitiveMultimap<T> create(){
        return new CaseInsensitiveMultimap<T>();
    }

    public static <T extends Comparable> CaseInsensitiveMultimap<T> create(Multimap<String, T> multimap){
        CaseInsensitiveMultimap<T> caseInsensitiveMultimap = new CaseInsensitiveMultimap<>();
        caseInsensitiveMultimap.putAll(multimap);
        return caseInsensitiveMultimap;
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
