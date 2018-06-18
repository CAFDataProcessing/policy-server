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
package com.github.cafdataprocessing.corepolicy.common.dto;

/**
 *
 */
public class PageRequest {

    public PageRequest(){}

    public PageRequest(Long start, Long max_page_results){
        this.start = start;
        this.max_page_results = max_page_results;
    }

    public Long start = 1L;

    //We have to name like this (and not JsonProperty) so the ServletRequestDataBinder can deserialize it properly
    public Long max_page_results = 6L;
}
