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

/**
 *
 */
public class DocumentFields {


    // Internal fields used on the document, but should not be persisted, prefixed to help avoid collision with existing fields.
    public final static String ChildDocumentDepth = "CPE_CHILD_DOCUMENT_DEPTH";
    public final static String ChildDocumentCount = "CPE_CHILD_DOCUMENT_COUNT";

    /*
    *
    * Public Fields - DO NOT CHANGE!
    *
    */

    // Static Fields supplied by connectors which are out of our control!
    public final static String Reference = "reference";
    //DREREFERENCE seems to still be used by connectors...
    public final static String DreReference = "DREREFERENCE";

    /*
    * Public fields returned by CollateDocument
     */
    public final static String MatchedCollection = "matched_collection";
    public final static String IncompleteCollection = "incomplete_collection";
    public final static String MatchedConditions = "matched_conditions";
    public final static String UnmatchedConditions = "unmatched_conditions";

    // fields added temporarily to document to prevent - reevaluation ( from ProcessDocument )
    public final static String EvaluationInformationBlob = "POLICY_EVAL_METADATA";
    public final static String MetadataHash = "POLICY_EVAL_METADATA_MARKER";

    // Transitory fields which are passed in on the Document metadata and are setup usually within the connectors / CFS.
    public final static String CollectionSequence = "POLICY_COLLECTION_SEQUENCE";
    public final static String KV_Metadata_Present_FieldName = "POLICY_KV_METADATA_PRESENT";

    public final static String IsClassified = "POLICY_CLASSIFIED";


    /*
    * Public fields returned by processDocument
     */

    // public search fields added to document, these actually get persisted
    public final static String SearchField_MatchedCollection = "POLICY_MATCHED_COLLECTION";
    private final static String SearchField_MatchedCondition = "POLICY_MATCHED_CONDITION_";

    public static String getMatchedConditionField(long collectionId){
        return SearchField_MatchedCondition + collectionId;
    }
}
