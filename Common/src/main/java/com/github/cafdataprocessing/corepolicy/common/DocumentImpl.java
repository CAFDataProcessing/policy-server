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

import com.github.cafdataprocessing.corepolicy.multimap.utils.CaseInsensitiveMultimap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;

/**
 *
 */
public class DocumentImpl implements Document {
    private static Logger logger = LoggerFactory.getLogger(DocumentImpl.class);
    private CaseInsensitiveMultimap<String> metadata = CaseInsensitiveMultimap.create();
    private CaseInsensitiveKeyMultimap<InputStream> streams = CaseInsensitiveKeyMultimap.create();

    private Collection<Document> documents = new ArrayList<>();

    public String getReference() {
        return ReferenceExtractor.getReference(this);
    }

    public void setReference(String reference) {
        ReferenceExtractor.setReference(this, reference);
    }

    public boolean getFullMetadata() {
        if (metadata.containsKey(DocumentFields.KV_Metadata_Present_FieldName)) {
            return Boolean.valueOf(metadata.get(DocumentFields.KV_Metadata_Present_FieldName).stream().findFirst().get());
        }
        return true;
    }

    public void setFullMetadata(boolean fullMetadata) {
        if (metadata.containsKey(DocumentFields.KV_Metadata_Present_FieldName)) {
            metadata.get(DocumentFields.KV_Metadata_Present_FieldName).clear();
        }
        metadata.put(DocumentFields.KV_Metadata_Present_FieldName, String.valueOf(fullMetadata));
    }

    public CaseInsensitiveMultimap<String> getMetadata() {
        return metadata;
    }

    public CaseInsensitiveKeyMultimap<InputStream> getStreams() {
        return streams;
    }

    @Override
    public Collection<Document> getDocuments() {
        return documents;
    }

    /**
     * Note ownership is not forced onto this internal document object.
     * The caller has the option of using this helper to close all streams across all documents,
     * or keeping them alive for later reuse.
     * @throws Exception
     */
    @Override
    public void close() throws Exception {
        Exception firstStreamCloseException = null;

        for (InputStream is : getStreams().values()) {
            try {
                is.close();
            } catch (IOException e) {
                logger.warn("Could not close stream", e);

                // do not throw this exception immediately we need to hold
                // onto first exception until the end of all streams being closed.
                if (firstStreamCloseException == null) {
                    firstStreamCloseException = e;
                }
            }
        }
        if (documents != null) {
            for (Document document : documents) {
                try {
                    document.close();
                } catch (Exception e) {
                    logger.warn("Could not close document", e);
                    // do not throw this exception immediately we need to hold
                    // onto first exception until the end of all streams being closed.
                    if (firstStreamCloseException == null) {
                        firstStreamCloseException = e;
                    }
                }
            }
        }

        // if we have happen to have had an exception, throw now.
        if (firstStreamCloseException == null)
            return;

        throw firstStreamCloseException;
    }
}
