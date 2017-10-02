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
package com.github.cafdataprocessing.corepolicy.common;

import java.util.Comparator;

/**
 * Comparator for use with classes that cannot normally be compared.
 */
public class NonComparableComparator implements Comparator<Object> {

    static private final int LEFT_IS_GREATER = 1;
    static private final int RIGHT_IS_GREATER = -1;

    public int compare(Object objA, Object objB) {

        // we can just do our null first implementation, so we dont need to check for null later.
        if (objA == null && objB == null) {
            return 0;
        }
        if (objA == null) {
            return RIGHT_IS_GREATER;
        }
        if (objB == null) {
            return LEFT_IS_GREATER;
        }

        // now we know we have both values, so try to compare!
        // only use comparator if both have the comparator present.
        // it theoretically possible we could get a mixture of inputstream types.
        if (objA instanceof Comparator && objB instanceof Comparator) {
            return ((Comparator) objA).compare(objA, objB);
        }

        // it isn't comparable, try to do something useful, base on types we expected.

        // order based on the class name / ident of the stream,
        // it should remain consistent at least.
        return objA.toString().compareTo(objB.toString());

    }

}
