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
package com.github.cafdataprocessing.corepolicy.common.dtoweb;

import com.github.cafdataprocessing.corepolicy.common.dto.DocumentToExecutePolicyOn;

import java.util.Collection;

/**
 * Class to wraps documents to execute policies on inside a collection
 */
public class DocumentPolicyArrayWrapper {
    public Collection<DocumentToExecutePolicyOn> document_policies;
}
