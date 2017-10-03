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
package com.github.cafdataprocessing.corepolicy.common.shared;

import com.google.common.collect.Multimap;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 */
public class StringHelper {
    public static String toCSV(Object... things){
        if(things == null || things.length == 0)
            return "";

        //covert to string list
        return toCSV(Arrays.asList(things));
    }

    public static String toCSV(Collection<Object> things){
        if(things == null || things.size() < 1)
            return "";

        return String.join(",", things.stream().map(String::valueOf).collect(Collectors.toList()));
    }

    public static String mapToHtml(Multimap<String, String> entries) {
        StringBuilder sb = new StringBuilder();
        sb.append( "<ul>");
        for( Map.Entry entry : entries.entries())
        {
            sb.append( "<li>");
            sb.append( "<b>" + entry.getKey() + "</b> : " + entry.getValue() );
            sb.append( "</li>");
        }
        sb.append( "</ul>");

        return sb.toString();
    }
}
