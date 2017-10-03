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
package com.github.cafdataprocessing.corepolicy.testing.loaders;

import java.util.HashMap;

/**
 *
 */
public class IdNameResolver {

    private final String prefix;

    public IdNameResolver(String prefix){

        this.prefix = prefix;
    }

    HashMap<String, Long> ids =  new HashMap<>();

    public String getPrefix(){
        return prefix;
    }

    public Long resolveId(String name){
        return ids.get(prefix + name);
    }

    public void registerId(String name, Long id){
        ids.put(prefix + name, id);
    }
}
