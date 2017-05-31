/*
 * Copyright 2015-2017 Hewlett Packard Enterprise Development LP.
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

import com.google.common.collect.Multimap;

import java.io.InputStream;
import java.util.Collection;

/**
 * A document to be collated against a collection sequence and it's contained collections and conditions.
 */
public interface Document extends AutoCloseable {

    public String getReference();
    public void setReference(String reference);
    public boolean getFullMetadata();
    public void setFullMetadata(boolean fullMetadata);

    /**
     * The metadata for the document, implementation should use case insensitive keys.
     * To allow identification of documents in the result supply a "reference" field in the metadata.
     * @return
     */
    public Multimap<String,String> getMetadata();

    /**
     * The streams available for the document, implementation should use case insensitive keys.
     * note we do not own the streams, they are the callers responsibility who setup this map.
     * and as such all items should be closed in the calling context when it returns back to the caller.
     * @return
     */
    public Multimap<String,InputStream> getStreams();

    /**
     * Child documents of the document to be collated, each child document should have a "reference" field in the metadata.
     * @return
     */
    public Collection<Document> getDocuments();
}


