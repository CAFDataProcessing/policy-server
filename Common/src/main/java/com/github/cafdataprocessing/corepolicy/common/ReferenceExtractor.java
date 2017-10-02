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

import com.github.cafdataprocessing.corepolicy.common.shared.MetadataValue;
import com.google.common.base.Strings;
import com.google.common.collect.Multimap;
import com.github.cafdataprocessing.corepolicy.common.exceptions.InvalidFieldValueCpeException;
import com.github.cafdataprocessing.corepolicy.common.exceptions.excpetionErrors.InvalidFieldValueErrors;

import java.util.Optional;

/**
 * use to extract and set reference/DREREFERENCE from documents. WE do this in a few places, so I've centralized it
 * here.
 */
public class ReferenceExtractor {
    public static String getReference(Document document){
        return getReference(document.getMetadata(), !document.getDocuments().isEmpty());
    }

    public static void setReference(Document document, String reference){
        setReference(document.getMetadata(), reference);
    }

    public static String getReference(Multimap<String,String> metadata, boolean hasChildren){
        if(metadata.containsKey(DocumentFields.Reference)) {
            if(hasChildren && metadata.get(DocumentFields.Reference).contains(null)){
                throw new InvalidFieldValueCpeException(InvalidFieldValueErrors.DOCUMENT_NULL_ID);
            }
            else if(metadata.get(DocumentFields.Reference).contains(null)){
                return null;
            }
            else {
                Optional<String> reference = metadata.get(DocumentFields.Reference).stream().findFirst();
                return reference.isPresent() ? reference.get() : null;
            }
        }

        if(metadata.containsKey(DocumentFields.DreReference))
            return metadata.get(DocumentFields.DreReference).stream().findFirst().get();

        return null;
    }

    public static String getReferenceFromMap(Multimap<String,MetadataValue> metadata, boolean hasChildren){
        if(metadata.containsKey(DocumentFields.Reference)) {
            // Reference field must be present, if we have child docs, if not it is allowed, just we wont have the name
            // of the document in the matched terms.
            boolean refIsNull = metadata.get(DocumentFields.Reference).stream().filter(u -> Strings.isNullOrEmpty(u.getAsString())).findFirst().isPresent();
            if(hasChildren && refIsNull ){
                throw new InvalidFieldValueCpeException(InvalidFieldValueErrors.DOCUMENT_NULL_ID);
            }
            else if(refIsNull){
                return null;
            }
            else {
                Optional<MetadataValue> reference = metadata.get(DocumentFields.Reference).stream().findFirst();
                return reference.isPresent() ? reference.get().getAsString() : null;
            }
        }

        if(metadata.containsKey(DocumentFields.DreReference)) {
            // DREREFERENCE if present, is always non null!
            return metadata.get(DocumentFields.DreReference).stream().findFirst().get().getAsString();
        }

        return null;
    }

    public static void setReference(Multimap<String,String> metadata, String reference){
        if(metadata.containsKey(DocumentFields.Reference))
            metadata.get(DocumentFields.Reference).clear();

        if(metadata.containsKey(DocumentFields.DreReference))
            metadata.get(DocumentFields.DreReference).clear();

        metadata.put(DocumentFields.Reference, reference);
        metadata.put(DocumentFields.DreReference, reference);
    }

    public static void setReferenceAsMetadataValue(Multimap<String, MetadataValue> metadata, String reference, ApiProperties apiProperties  ){
        // Note this doesn't use removeAll / replace as this is used by the Extractor in the connector
        // task library code.  Its uses our own wrapped map with JNI callbacks.
        // Its implementation needs to be tested with replaceValues before we make this generic....
        if(metadata.containsKey(DocumentFields.Reference))
            metadata.get(DocumentFields.Reference).clear();

        if(metadata.containsKey(DocumentFields.DreReference))
            metadata.get(DocumentFields.DreReference).clear();

        metadata.put(DocumentFields.Reference, new MetadataValue( apiProperties, reference));
        metadata.put(DocumentFields.DreReference, new MetadataValue( apiProperties, reference));
    }

}
