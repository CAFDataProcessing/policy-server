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



/**

 *

 */

public class ApiStrings {



    public static abstract class BaseCrud {



        public static class Arguments{

            public final static String ACTION = "action";

            public final static String JSON = "json";

            public final static String PROJECT_ID = "project_id";

            public final static String ID = "id";

        }

    }



    public static abstract class BasePaging extends BaseCrud{

        public static class Arguments extends BaseCrud.Arguments {

            public final static String PAGE_SIZE = "page_size";

            public final static String PAGE = "page";

            public final static String SORT_ORDER = "sort_order";



        }

    }



    public final static class JsonProperties {

        public final static String URL = "/jsonproperties";

        public final static String TYPE = "type";

        public final static String ADDITIONAL = "additional";

    }

    public final static class PolicyCollections {

        public final static String URL = "/policycollections";



        public final static class Arguments {

            public final static String ACTION = "action";

            public final static String PROJECT_ID = "project_id";

            public final static String COLLECTION_ID = "collection_id";

            public final static String POLICY_ID = "policy_id";

        }

    }



    public final static class Policy {

        public final static String URL = "/policy";



        public final static class Arguments {



            public final static String POLICY_TYPE = "policy_type_id";

            public final static String NAME = "name";

            public final static String DESCRIPTION = "description";

            public final static String DETAILS = "details";

            public final static String PRIORITY = "priority";

            public final static String DELETED = "deleted";



        }

    }



    public final static class PolicyType {



        public final static String URL = "/policytype";



        public final static class Arguments {



            public final static String ACTION = "action";

            public final static String PROJECT_ID = "project_id";

            public final static String POLICY_TYPE_ID = "id";

            public final static String POLICY_TYPE_NAME = "name";

            public final static String POLICY_TYPE_DESC = "description";

            public final static String POLICY_TYPE_DEFINITION = "definition";

            public final static String POLICY_TYPE_INTERNAL_NAME = "short_name";

            public final static String POLICY_CONFLICT_RESOLUTION_MODE = "conflict_resolution_mode";



        }

    }



    public final static class Conditions extends BasePaging {

        public final static String URL = "/conditions";



        public final static class Arguments extends BasePaging.Arguments {

            public final static String FIELD_NAME = "field_name";

            public final static String LANGUAGE = "language";

            public final static String INCLUDE_CHILDREN = "include_children";

            public final static String TYPE = "type";

            public final static String NAME = "name";

            public final static String VALUE = "value";

            public final static String OPERATOR = "operator";

            public final static String TARGET = "target";

            public final static String INCLUDE_DESCENDANTS= "include_descendants";

            public final static String PARENT_CONDITION_ID= "parent_condition_id";

            public final static String IS_FRAGMENT = "is_fragment";

            public final static String CONDITION_TYPE = "type";

            public final static String ORDER = "order";

            public final static String NOTES = "notes";

            public final static String FIELD = "field";

            public final static String CONDITION = "condition";

            public final static String CHILDREN = "children";

            public final static String DESC = "description";

        }

    }



    public final static class CollectionSequences extends BasePaging {

        public final static String URL = "/collectionsequences";



        public final static class Arguments extends BasePaging.Arguments {

            public final static String NAME = "name";

            public final static String EXCLUDED_DOCUMENT_CONDITION_ID = "excluded_document_condition_id";

            public final static String DESCRIPTION = "description";

            public final static String DEFAULT_COLLECTION_ID = "default_collection_id";

            public final static String FULL_CONDITION_EVALUATION = "full_condition_evaluation";

            public final static String COLLECTION_SEQUENCE_ENTRIES = "collection_sequence_entries";

            public final static String COLLECTION_COUNT = "collection_count";

            public final static String LAST_MODIFIED = "last_modified";

            public final static String EVALUATION_ENABLED = "evaluation_enabled";

            public final static String FINGERPRINT = "fingerprint";

        }

    }



    public final static class CollectionSequenceEntries extends BaseCrud {

        public final static String URL = "/collectionsequenceentries";



        public final static class Arguments extends BaseCrud.Arguments {

            public final static String STOP_ON_MATCH = "stop_on_match";

            public final static String COLLECTION_IDS = "collection_ids";

            public final static String ORDER = "order";

            public final static String SEQUENCE_ID = "sequence_id";

        }

    }

    public final static class DocumentCollections extends BasePaging {

        public final static String URL = "/documentcollections";



        public final static class Arguments extends BasePaging.Arguments {

            public final static String NAME = "name";

            public final static String DESCRIPTION = "description";

            public final static String CONDITION = "condition";

            public final static String POLICY_IDS = "policy_ids";

            public final static String FINGERPRINT = "fingerprint";

        }

    }

    public final static class ExcludedFragments extends BaseCrud {

        public final static String URL = "/excludedfragments";



        public final static class Arguments extends BaseCrud.Arguments {

            public final static String NAME = "name";

            public final static String FIELD_NAMES = "field_names";

            public final static String SEQUENCE_ID = "sequence_id";

            public final static String PATTERN = "pattern";

            public final static String ORDER = "order";

        }

    }

    public final static class LexiconExpressions extends BaseCrud {

        public final static String URL = "/lexiconexpressions";



        public final static class Arguments extends BaseCrud.Arguments {

            public final static String LEXICON_ID = "lexicon_id";

            public final static String EXPRESSION = "expression";

            public final static String TYPE = "type";

        }

    }

    public final static class Lexicons extends BasePaging {

        public final static String URL = "/lexicons";



        public final static class Arguments extends BasePaging.Arguments {

            public final static String NAME = "name";

            public final static String DESCRIPTION = "description";

            public final static String LEXICON_EXPRESSIONS = "lexicon_expressions";

        }

    }



    public final static class FieldLabels extends BaseCrud {

        public final static String URL = "/fieldlabels";



        public final static class Arguments extends BaseCrud.Arguments{

            public final static String FIELD_TYPE = "field_type";

            public final static String NAME = "name";

            public final static String FIELDS = "fields";



        }

    }



    public final static class UnevaluatedConditions extends BaseCrud{

        public final static String URL = "/unevaluatedconditions";



        public final static class Arguments extends BaseCrud.Arguments{

            public final static String NAME = "name";

            public final static String REASON = "reason";

            public final static String CONDITION_TYPE = "type";

            public final static String FINGERPRINT = "fingerprint";

        }

    }



    public final static class UnmatchedConditions extends BaseCrud{

        public final static String URL = "/unmatchedconditions";



        public final static class Arguments extends BaseCrud.Arguments{

            public final static String REFERENCE = "reference";

            public final static String CONDITION_TYPE = "type";

            public final static String NAME = "name";

            public final static String FINGERPRINT = "fingerprint";

            public final static String FIELD_NAME = "field_name";

        }

    }

    public final static class SequenceWorkflow extends BaseCrud {

        public final static String URL = "/sequenceworkflow";


        public final static class Arguments extends BaseCrud.Arguments {

            public final static String NAME = "name";
            public final static String SEQUENCE_ENTRIES = "sequence_entries";
            public final static String DESCRIPTION = "description";
            public final static String NOTES = "notes";

        }
    }

    public final static class SequenceWorkflowEntry extends BaseCrud {

        public final static String URL = "/sequenceworkflowentry";


        public final static class Arguments extends BaseCrud.Arguments {

            public final static String SEQUENCE_WORKFLOW_ID = "sequence_workflow_id";
            public final static String ORDER = "order";
            public final static String SEQUENCE_ID = "collection_sequence_id";
            public final static String SEQUENCE = "collection_sequence";

        }
    }

}

