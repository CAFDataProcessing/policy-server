# Classify
The Policy Management APIs provide an abstracted layer on top of entity
extraction, categorization and related functionality to allow the
definition of classifications and policies for the management of
information. Classifications can be created to group documents and other
objects into collections. Policies enact actions upon documents
associated with a collection.

The API returns the collections, satisfied conditions, and relevant
policies for a document.

You can use this API to classify documents into different collections,
understand which conditions caused the classification, and understand
which policies are relevant to the documents.

You must provide the name or id of the collection sequence that you want
to use to classify the document. For example:

> /classifydocument/?collection\_sequence=corporate\_sequence

You can modify this collection sequence in the **collectionsequence**
API.

You can use the **json** parameter to add documents to your index. For
example:

> /classifydocument/?index=myindex&json=

The required format for the json is:

    {
        "document" :
        [
            {
                "title" : "This is my document",
                "reference" : "mydoc1",
                "myfield" : ["a value"],
                "content" : "A large block of text, which makes up the main body of the document."
            }, {
                "title" : "My Other document",
                "reference" : "mydoc2",
                "content" : "This document is about something else"
            }
        ]
    }

-   You can include additional fields, such as **myfield** in
    the example.

Rather than submitting documents in json format, you can alternatively
submit a file, reference or URL to the API. For example:

> /classifydocument/?collection\_sequence=corporate\_sequence&file=
> or **url=** or **reference=**

In this case, the API uses the Extract Text API to extract the text
content from the file, and creates the json documents automatically. In
this case, the document reference field is the file name or URL that you
submit.

> /classifydocument/?collection\_sequence=corporate\_sequence&file=myfile.txt

    {
        "index" : "myindex",
        "references" : [{
                "reference" : "myfile.txt",
                "id" : 108
            }
        ]
    }


## Request

#### Input source
This API accepts a single input source that can be supplied using one of the following parameters:

<table>
    <tr>
        <td><b>Parameter</b></td>
        <td><b>Description</b></td>
    </tr>
    <tr>
        <td>json</td>
        <td>The JSON document to examine.</td>
    </tr>
    <tr>
        <td>file</td>
        <td>A file to examine. The API passes the file to the Text Extraction API to extract the contents for examination. Multi part POST only.</td>
    </tr>
    <tr>
        <td>url</td>
        <td>A publicly accessible HTTP URL from which the JSON document can be retrieved.</td>
    </tr>
</table>


#### Parameters
In addition to the above input source, this API accepts the following parameters:

<table>
    <tr>
        <td><b>Name</b></td>
        <td><b>Type</b></td>
        <td><b>Description</b></td>
    </tr>
    <tr>
        <td> <b>collection_sequence</b> </td>
        <td> String </td>
        <td> The ID of the collection sequence the documents should be evaluated against.</td>
    </tr>
</table>

*Required parameters are shown with names in bold.*

## Response
This API returns a JSON response that is described by the model below. This single model is presented both as an easy to read abstract 
definition and as the formal JSON schema.

##### Model

This is an abstract definition of the response that describes each of the properties that might be returned.

**Classify Document Response {**  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; **result** (array[Result], *optional*)&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; Indicates the collections that were matched along with the conditions that caused that match, 
and any conditions that were unable to be evaluated.  
**}**

**Result {**  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; **unevaluated_conditions** ( array or null , *optional*)&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; Indicates any conditions that were unable to be evaluated and the reason.  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; **matched_collections** ( array or null , *optional*)&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; 	A list of collections that the supplied document matched.  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; **collection_id_assigned_by_default** ( number or null , *optional*)  	
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; **incomplete_collections** ( array or null , *optional*)  	
**}**


##### Model Schema
This is a JSON schema that describes the syntax of the response. See json-schema.org for a complete reference.

<pre><code>
{
    "type": "object",
    "properties": {
        "result": {
            "type": "array",
            "items": {
                "type": "object",
                "properties": {
                    "unevaluated_conditions": {
                        "type": [
                            "array",
                            "null"
                        ],
                        "items": {
                            "type": "object",
                            "items": {
                                "type": "object",
                                "properties": {
                                    "id": {
                                        "type": "number",
                                        "multipleOf": 1
                                    },
                                    "name": {
                                        "type": "string"
                                    },
                                    "type": {
                                        "type": "string"
                                    },
                                    "reason": {
                                        "type": "string",
                                        "enum": [
                                            "missing_field",
                                            "missing_service"
                                        ]
                                    }
                                },
                                "required": [
                                    "id",
                                    "name"
                                ]
                            }
                        }
                    },
                    "matched_collections": {
                        "type": [
                            "array",
                            "null"
                        ],
                        "items": {
                            "type": "object",
                            "properties": {
                                "id": {
                                    "type": "number",
                                    "multipleOf": 1
                                },
                                "name": {
                                    "type": "string"
                                },
                                "matched_conditions": {
                                    "type": "array",
                                    "items": {
                                        "type": "object",
                                        "properties": {
                                            "reference": {
                                                "type": "string"
                                            },
                                            "terms": {
                                                "type": "array",
                                                "items": {
                                                    "type": "string"
                                                }
                                            },
                                            "matched_lexicon_expressions": {
                                                "type": "array",
                                                "items": {
                                                    "type": "object",
                                                    "properties": {
                                                        "lexicon_expression_id": {
                                                            "type": "number",
                                                            "multipleOf": 1
                                                        },
                                                        "terms": {
                                                            "type": "array",
                                                            "items": {
                                                                "type": "string"
                                                            }
                                                        }
                                                    },
                                                    "required": [
                                                        "id"
                                                    ]
                                                }
                                            },
                                            "field_name": {
                                                "type": "string"
                                            }
                                        }
                                    }
                                }
                            },
                            "required": [
                                "id",
                                "name"
                            ]
                        }
                    },
                    "collection_id_assigned_by_default": {
                        "type": [
                            "number",
                            "null"
                        ],
                        "multipleOf": 1
                    },
                    "incomplete_collections": {
                        "type": [
                            "array",
                            "null"
                        ],
                        "items": {
                            "type": "number",
                            "multipleOf": 1
                        }
                    }
                }
            }
        }
    }
}
</code></pre>



## Create Classification Objects
The Policy Management APIs provide an abstracted layer on top of entity
extraction, categorization and related functionality to allow the
definition of classifications and policies for the management of
information. Classifications can be created to group documents and other
objects into collections. Policies enact actions upon documents
associated with a collection.

Classification involves sorting documents into Collections; all
documents in a collection share some common criteria, identified using
Conditions.

The collection or collections that a document matches can influence how
it is indexed and determine policies that are executed against it.

A Collection Sequence is an ordered list of collections that documents
are assessed against; each collection is considered in order until the
document matches a collection with a stop on match instruction.
Documents can match multiple collections in a collection sequence.

The API allows collection sequences, collections, conditions, field labels, lexicons and lexicon expressions to be created.

Using this API you can create classification objects. For example:

> /classification/create/?type=collection\_sequence&name=my\_collection\_sequence&description=Some%20descriptive%20text

The **type** of classification object you are adding determines the
additional information that can be provided in the **additional**
parameter.

The following classification object types can be created:

-   **collection**. A set of documents that share some common criteria.
-   **collection sequence**. An ordered set of Collections.
-   **condition**. Each Collection is identified using one or
    more Conditions.
-   **field label**. Information on how to retrieve a property value
    using one or more document fields.
-   **lexicon**. A list of terms, phrases or expressions that can be
    used when defining Conditions.
-   **lexicon expression**. An entry in a Lexicon.

The **name** and **description** can be any values that help you
identify the classification object.

**name** is mandatory for collections, collection sequences and lexicons
and ignored for conditions, lexicon expressions and
field labels.

**description** is optional for collections, collection sequences,
lexicons and ignored for conditions, lexicon expressions and field labels.

The **additional** parameter takes a JSON object specifying additional
parameters that can be provided for the classification object type.

#### Collection

An example of the required format for **additional** JSON for a
collection object is:

    {
        "policy_ids" : [1, 2],
        "condition" :
        {
            "type" : "condition",
            "additional": {
                "type" : "string",
                "field" : "DRECONTENT",
                "value" : "cat DNEAR4 dog",
                "operator" : "contains"
            }
        }
    }

The **policy\_ids** field, if present, identifies one or more policies
that should be assigned to the collection. For more information about
policies, see Create Policy. You cannot have multiple policies of the
same type on a collection.

The **condition** field, if present, identifies one or more conditions
that are used to determine if documents are in the collection.
Conditions can be added to the collection or updated after addition via
Update Classification.

#### Collection Sequence

An example of the required format for **additional** JSON for a
collection sequence object is:

    {
        "collection_sequence_entries" :
            [{
                "order" : 10,
                "collection_ids" : [7],
                "stop_on_match" : "true"
            },
            {
                "order" : 20,
                "collection_ids" : [10,11],
                "stop_on_match" : "false"
            }],
        "default_collection_id" : 5,
        "full_condition_evaluation": true
    }

The **collection\_sequence\_entries** array defines one or more entries
to add to the collection sequence. Each entry contains one or more
collections and has an **order** and a **stop\_on\_match** attribute.

The **order** field identifies where the collection entry appears in the
collection sequence. Any numeric value can be specified and entries are
ordered by their values, from lowest to highest.

The **collection\_ids** field identifies the collections that are being
added to the collection sequence. There will typically be one collection
per collection entry.

If a collection sequence entry has a **stop\_on\_match** parameter set
to true then classification against further collections in the
collection sequence will stop if a document matches one of the
collections in the collection entry. The default is false.

The **default\_collection\_id** field defines a default collection that
is assigned to documents that fail to match any collection in the
sequence.

The **full\_condition\_evaluation** parameter can be set to true to
cause all conditions within a boolean OR condition to be evaluated
instead of stopping after the first matching condition. The default is
false.

#### Condition

The **type** field identifies the type of the condition being added.
Valid values are Boolean, Date, Exists, Fragment, Lexicon, Not, Number,
Regex, String, Text and Entity.

##### Boolean Conditions

An example of the format for **additional** JSON for a condition object
of type **Boolean** is:

    {
        "type" : "boolean",
        "operator" : "or",
        "notes" : "Author is John Smith or Sarah Smith",
        "children": [{
            "type": "condition",
            "additional": {
                "type" : "string",
                "field" : "AUTHOR",
                "operator" : "is",
                "value" : "John Smith"
            }
        },{
            "type": "condition",
            "additional": {
                "type" : "string",
                "field" : "AUTHOR",
                "operator" : "is",
                "value" : "Sarah Smith"
            }
        }]
    }

This condition will match a document that has an AUTHOR field of John
Smith or Sarah Smith.

Boolean condition types require an **operator** parameter.

The **operator** parameter indicates the type of Boolean join that is
being performed on child conditions. Valid operators for Boolean
conditions are and and or.

The **notes** parameter allows you to store some additional infomation
about a condition.

##### Date Property Conditions

An example of the format for **additional** JSON for a condition object
of type **Date** is:

    {
        "type" : "date",
        "field" : "AU_IMPORT_CREATEDDATE_EPOCHSECONDS",
        "operator" : "before",
        "value" : "2014-10-10T10.13:19Z"
    }

Date condition types require **field**, **operator** and **value**
parameters.

The **field** parameter indicates the name of the date property being
tested.

The **operator** parameter indicates the type of test that is being
performed on the date property. Valid operators for Date conditions are
before, after and on.

The **value** parameter indicates the value that the date property is
being tested against.

The value can have the following formats:

-   A date in epoch post fixed with e: 1412935999e corresponds to
    2014-10-10T10.13:19Z
-   A date in ISO8601 format: 2014-10-10T10.13:19Z
-   A date period in ISO8601 period format: P3Y6M4DT12H30M5S corresponds
    to 3 years, 6 months, 4 days, 12 hours, 30 minutes and 5 seconds
-   A day of the week - The following values may be truncated to Mon
    etc.: Monday, Tuesday, Wednesday, Thursday, Friday, Saturday, Sunday
-   A time of day in hh:mm or hh:mm:ss format: 12:00 or 14:30

##### Exists Conditions

An example of the format for **additional** JSON for a condition object
of type **Exists** is:

    {
        "type" : "exists",
        "field" : "VITAL_FLAG"
    }

Exists condition types require a **field** parameter.

The **field** parameter indicates the name of the property being tested.

##### Fragment Conditions

An example of the format for **additional** JSON for a condition object
of type **Fragment** is (where 132 is the id of an existing condition
that was created with is\_fragment set to true):

    {
        "type" : "fragment",
        "value" : "132"
    }

Fragment condition types require a **value** parameter.

The **value** parameter indicates the condition fragment to be included.

##### Lexicon Conditions

An example of the format for **additional** JSON for a condition object
of type **Lexicon** is (where 12 is the id of an existing lexicon):

    {
        "type" : "lexicon",
        "field" : "TITLE",
        "value" : "12"
    }

Lexicon condition types require **field** and **value** parameters.

The **field** parameter indicates the name of the string property being
tested.

The **value** parameter indicates the lexicon that the string property
is being tested against.

##### Not Conditions

An example of the format for **additional** JSON for a condition object
of type **Not** is:

    {
        "type" : "not",
        "condition": {
            "type": "condition",
            "additional": {
                "type" : "string",
                "field" : "AUTHOR",
                "operator" : "is",
                "value" : "John Smith"
            }
        }
    }

This condition will match documents which have an AUTHOR field that is
NOT John Smith.

##### Number Property Conditions

An example of the format for **additional** JSON for a condition object
of type **Number** is:

    {
        "type" : "number",
        "field" : "SIZE",
        "operator" : "gt",
        "value" : 1000
    }

Number condition types require **field**, **operator** and **value**
parameters.

The **field** parameter indicates the name of the numeric property being
tested.

The **operator** parameter indicates the type of test that is being
performed on the numeric property. Valid operators for Number conditions
are gt, lt and eq.

The **value** parameter indicates the value that the numeric property is
being tested against.

##### Regex Property Conditions

An example of the format for **additional** JSON for a condition object
of type **Regex** is:

    {
        "type" : "regex",
        "field" : "DRECONTENT",
        "value" : "\\b[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,4}\\b"
    }

Regex condition types require **field** and **value** parameters.

The **field** parameter indicates the name of the property being tested.

The **value** parameter indicates the regular expression that the
property is being tested against.

##### String Property Conditions

An example of the format for **additional** JSON for a condition object
of type **String** is:

    {
        "type" : "string",
        "field" : "AUTHOR",
        "operator" : "is",
        "value" : "John Smith"
    }

String condition types require **field**, **operator** and **value**
parameters.

The **field** parameter indicates the name of the string property being
tested.

The **operator** parameter indicates the type of test that is being
performed on the string property. Valid operators for String conditions
are is, starts\_with and ends\_with.

The **value** parameter indicates the value that the string property is
being tested against.

##### Text Property Conditions

An example of the format for **additional** JSON for a condition object
of type **Text** is:

    {
        "type" : "text",
        "field" : "DRECONTENT",
        "value" : "cat DNEAR4 dog"
    }

Text condition types require **field** and **value** parameters.

The **field** parameter indicates the name of the string property being
tested. This will normally be DRECONTENT.

The **value** parameter indicates the value that the string property is
being tested against.

#### Field Label

An example of the required format for **additional** JSON for a field
label object is:

    {
        "field_type" : "string",
        "fields": ["AU_REPOSITORY_TO_RFC822","To","To"]
    }

The **field\_type** indicates the type of the field label. This can be
string, number or date.

The **fields** argument can contain one or more field names that
provide the property value to be used. If multiple field names are
provided they are checked in order and the first value found is used.

#### Lexicon {.post}

An example of the required format for **additional** JSON for a lexicon
object is:

    {
        "lexicon_expressions": [
            {
                "type": "lexicon_expression",
                "additional": {
                    "type": "text",
                    "expression": "antelope"
                }
            },
            {
                "type": "lexicon_expression",
                "additional": {
                    "type": "text",
                    "expression": "african DNEAR elephants"
                }
            }
        ]
    }

The **lexicon\_expressions** array defines one or more lexicon
expressions to add to the lexicon. Each lexicon expression has a
**type** and an **expression**.

The **type** field indicates the type of the lexicon expression, one of
text or regex.

The **expression** field contains the expression to add to the lexicon.
It can be a term, phrase or proximity expression for text type or a
regular expression for regex type.

#### Lexicon Expression {.post}

An example of the required format for **additional** JSON for a lexicon
expression object is:

    {
        "lexicon_id" : 2,
        "type" : "text",
        "expression" : "lion"
    }

The **lexicon\_id** identifies the lexicon that the lexicon expression
is to be added to.

The **type** field indicates the type of the lexicon expression, one of
text or regex.

The **expression** field contains the expression to add to the lexicon.
It can be a term, phrase or proximity expression for text type or a
regular expression for regex type.

## Request

#### Parameters
This API accepts the following parameters:

<table>
    <tr>
        <td><b>Name</b></td>
        <td><b>Type</b></td>
        <td><b>Description</b></td>
    </tr>
    <tr>
        <td> name </td>
        <td> String </td>
        <td> The name of the classification object to add. Does not apply to lexicon_expression type.</td>
    </tr>
    <tr>
        <td> description </td>
        <td> String </td>
        <td> A textual description for the classification object. Does not apply to lexicon_expression or condition types.</td>
    </tr>
    <tr>
        <td> <b>type</b> </td>
        <td> enum &#60;type&#62; </td>
        <td> The type of classification object.</td>
    </tr>
    <tr>
        <td> additional </td>
        <td> object </td>
        <td> Specify a JSON object of additional parameters relevant for the type of classification object being added.</td>
    </tr>
</table>
*Required parameters are shown with names in bold.*

#### Enumeration Types
This API's parameters use the enumerations described below:

<table>
    <tr>
        <td><b>type</b> <br/> The type of classification object.</td>
    </tr>
    <tr>
        <td> collection_sequence </td>
        <td> <b>Collection Sequence</b><br/> A Collection Sequence classification object. </td>
    </tr>
    <tr>
        <td> collection </td>
        <td> <b>Collection</b><br/> A Collection classification object. </td>
    </tr>
    <tr>
        <td> condition </td>
        <td> <b>Condition</b><br/> A Condition classification object. </td>
    </tr>
    <tr>
        <td> lexicon </td>
        <td> <b>Lexicon</b><br/>A Lexicon classification object. </td>
    </tr>
    <tr>
        <td> lexicon_expression </td>
        <td> <b>Lexicon Expression</b><br/>A Lexicon Expression classification object. </td>
    </tr>
</table>

## Response
This API returns a JSON response that is described by the model below. This single model is presented both as an easy to read 
abstract definition and as the formal JSON schema.

##### Model
This is an abstract definition of the response that describes each of the properties that might be returned.


**Create Classification Response {**  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; **id** (number) &nbsp;&nbsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp; Id of the classification object.  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; **name** ( string or null , *optional*) &emsp;&emsp;&emsp;&emsp; Name of the classification object.  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; **description** ( string or null , *optional*) &emsp; Description of the classification object.  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; **type** (enum &lt;Type&gt;) &nbsp;&nbsp;&nbsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;The type of the classification object.  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; **additional** (object, *optional*) &nbsp;&nbsp;&emsp;&emsp;&emsp;&emsp;&emsp;JSON formatted additional information depending on the object type. See the schema for more details.  
**}**

**enum&lt;Type&gt; {**  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;'collection_sequence', 'collection', 'condition', 'lexicon', 'lexicon_expression'  
**}**

##### Model Schema 
This is a JSON schema that describes the syntax of the response. See json-schema.org for a complete reference.

<pre><code>
{
    "type": "object",
    "properties": {
        "id": {
            "type": "number",
            "multipleOf": 1
        },
        "name": {
            "type": [
                "string",
                "null"
            ],
            "minLength": 1
        },
        "type": {
            "enum": [
                "collection_sequence",
                "collection",
                "condition",
                "lexicon",
                "lexicon_expression"
            ]
        },
        "additional": {
            "type": "object"
        }
    },
    "required": [
        "id",
        "type"
    ],
    "oneOf": [
        {
            "$ref": "#/definitions/collection_sequence"
        },
        {
            "$ref": "#/definitions/collection"
        },
        {
            "$ref": "#/definitions/condition"
        },
        {
            "$ref": "#/definitions/lexicon"
        },
        {
            "$ref": "#/definitions/lexicon_expression"
        }
    ],
    "definitions": {
        "collection_sequence": {
            "type": "object",
            "properties": {
                "type": {
                    "enum": [
                        "collection_sequence"
                    ]
                },
                "additional": {
                    "type": "object",
                    "properties": {
                        "collection_sequence_entries": {
                            "type": [
                                "array",
                                "null"
                            ],
                            "items": {
                                "$ref": "#/definitions/collection_sequence_entry"
                            }
                        },
                        "default_collection_id": {
                            "type": [
                                "number",
                                "null"
                            ],
                            "multipleOf": 1
                        },
                        "full_condition_evaluation": {
                            "type": "boolean"
                        }
                    }
                }
            },
            "required": [
                "name"
            ]
        },
        "collection_sequence_entry": {
            "type": "object",
            "properties": {
                "order": {
                    "type": "number",
                    "multipleOf": 1
                },
                "collection_ids": {
                    "type": [
                        "array",
                        "null"
                    ],
                    "items": {
                        "type": "number",
                        "multipleOf": 1
                    }
                },
                "stop_on_match": {
                    "type": "boolean"
                }
            }
        },
        "collection": {
            "type": "object",
            "properties": {
                "type": {
                    "enum": [
                        "collection"
                    ]
                },
                "additional": {
                    "type": "object",
                    "properties": {
                        "condition": {
                            "$ref": "#/definitions/condition"
                        },
                        "policy_ids": {
                            "type": [
                                "array",
                                "null"
                            ],
                            "items": {
                                "type": "number",
                                "multipleOf": 1
                            }
                        }
                    }
                }
            },
            "required": [
                "name"
            ]
        },
        "condition": {
            "type": "object",
            "properties": {
                "type": {
                    "enum": [
                        "condition"
                    ]
                },
                "additional": {
                    "type": "object",
                    "properties": {
                        "type": {
                            "enum": [
                                "boolean",
                                "regex",
                                "date",
                                "fragment",
                                "lexicon",
                                "not",
                                "number",
                                "string",
                                "exists",
                                "text"
                            ]
                        },
                        "notes": {
                            "type": [
                                "string",
                                "null"
                            ]
                        },
                        "target": {
                            "enum": [
                                "all",
                                "children",
                                "container"
                            ]
                        },
                        "include_descendants": {
                            "type": "boolean"
                        }
                    },
                    "required": [
                        "type"
                    ],
                    "oneOf": [
                        {
                            "$ref": "#/definitions/boolean_condition"
                        },
                        {
                            "$ref": "#/definitions/regex_condition"
                        },
                        {
                            "$ref": "#/definitions/date_condition"
                        },
                        {
                            "$ref": "#/definitions/exists_condition"
                        },
                        {
                            "$ref": "#/definitions/fragment_condition"
                        },
                        {
                            "$ref": "#/definitions/lexicon_condition"
                        },
                        {
                            "$ref": "#/definitions/not_condition"
                        },
                        {
                            "$ref": "#/definitions/number_condition"
                        },
                        {
                            "$ref": "#/definitions/string_condition"
                        },
                        {
                            "$ref": "#/definitions/text_condition"
                        }
                    ]
                }
            },
            "required": [
                "additional"
            ]
        },
        "boolean_condition": {
            "type": "object",
            "properties": {
                "type": {
                    "enum": [
                        "boolean"
                    ]
                },
                "operator": {
                    "type": "string",
                    "enum": [
                        "and",
                        "or"
                    ]
                },
                "children": {
                    "type": [
                        "array",
                        "null"
                    ],
                    "items": {
                        "$ref": "#/definitions/condition"
                    }
                }
            },
            "required": [
                "operator"
            ]
        },
        "regex_condition": {
            "type": "object",
            "properties": {
                "type": {
                    "enum": [
                        "regex"
                    ]
                },
                "field": {
                    "$ref": "#/definitions/field"
                },
                "value": {
                    "type": "string",
                    "minLength": 1
                }
            },
            "required": [
                "field",
                "value"
            ]
        },
        "date_condition": {
            "type": "object",
            "properties": {
                "type": {
                    "enum": [
                        "date"
                    ]
                },
                "field": {
                    "$ref": "#/definitions/field"
                },
                "value": {
                    "type": "string",
                    "minLength": 1
                },
                "operator": {
                    "enum": [
                        "before",
                        "after",
                        "on"
                    ]
                }
            },
            "required": [
                "field",
                "value",
                "operator"
            ]
        },
        "exists_condition": {
            "type": "object",
            "properties": {
                "type": {
                    "enum": [
                        "exists"
                    ]
                },
                "field": {
                    "$ref": "#/definitions/field"
                }
            },
            "required": [
                "field"
            ]
        },
        "fragment_condition": {
            "type": "object",
            "properties": {
                "type": {
                    "enum": [
                        "fragment"
                    ]
                },
                "value": {
                    "type": "number",
                    "multipleOf": 1
                }
            },
            "required": [
                "value"
            ]
        },
        "lexicon_condition": {
            "type": "object",
            "properties": {
                "type": {
                    "enum": [
                        "lexicon"
                    ]
                },
                "field": {
                    "$ref": "#/definitions/field"
                },
                "value": {
                    "type": "number",
                    "multipleOf": 1
                }
            },
            "required": [
                "field",
                "value"
            ]
        },
        "not_condition": {
            "type": "object",
            "properties": {
                "type": {
                    "enum": [
                        "not"
                    ]
                },
                "condition": {
                    "$ref": "#/definitions/condition"
                }
            }
        },
        "number_condition": {
            "type": "object",
            "properties": {
                "type": {
                    "enum": [
                        "number"
                    ]
                },
                "field": {
                    "$ref": "#/definitions/field"
                },
                "value": {
                    "type": "number",
                    "multipleOf": 1
                },
                "operator": {
                    "enum": [
                        "gt",
                        "lt",
                        "eq"
                    ]
                }
            },
            "required": [
                "field",
                "value",
                "operator"
            ]
        },
        "string_condition": {
            "type": "object",
            "properties": {
                "type": {
                    "enum": [
                        "string"
                    ]
                },
                "field": {
                    "$ref": "#/definitions/field"
                },
                "value": {
                    "type": "string"
                },
                "operator": {
                    "enum": [
                        "is",
                        "starts_with",
                        "ends_with"
                    ]
                }
            },
            "required": [
                "field",
                "value",
                "operator"
            ]
        },
        "text_condition": {
            "type": "object",
            "properties": {
                "type": {
                    "enum": [
                        "text"
                    ]
                },
                "field": {
                    "$ref": "#/definitions/field"
                },
                "value": {
                    "type": "string"
                },
                "language": {
                    "type": "string"
                }
            },
            "required": [
                "field",
                "value"
            ]
        },
        "field": {
            "type": "string",
            "minLength": 1
        },
        "lexicon": {
            "type": "object",
            "properties": {
                "type": {
                    "enum": [
                        "lexicon"
                    ]
                },
                "additional": {
                    "type": "object",
                    "properties": {
                        "lexicon_expressions": {
                            "type": "array",
                            "items": {
                                "$ref": "#/definitions/lexicon_expression"
                            }
                        }
                    }
                }
            },
            "required": [
                "name"
            ]
        },
        "lexicon_expression": {
            "type": "object",
            "properties": {
                "type": {
                    "enum": [
                        "lexicon_expression"
                    ]
                },
                "additional": {
                    "type": "object",
                    "properties": {
                        "lexicon_id": {
                            "type": "number",
                            "multipleOf": 1
                        },
                        "type": {
                            "enum": [
                                "regex",
                                "text"
                            ]
                        },
                        "expression": {
                            "type": "string",
                            "minLength": 1
                        }
                    },
                    "required": [
                        "lexicon_id",
                        "type",
                        "expression"
                    ]
                }
            }
        }
    }
}
</code></pre>

## Create Policy Objects
The Policy Management APIs provide an abstracted layer on top of entity
extraction, categorization and related functionality to allow the
definition of classifications and policies for the management of
information. Classifications can be created to group documents and other
objects into collections. Policies enact actions upon documents
associated with a collection.

Classification involves sorting documents into Collections; all
documents in a collection share some common criteria, identified using
Conditions.

The collection or collections that a document matches can influence how
it is indexed and determine policies that are executed against it.

The API allows policy objects to be created.

To create a policy object you must specify a policy object **name**,
**type** and supply additional parameters in **additional**.

For example:

> /policy/create/?name=policy1&type=policy

The **name** can be any value that allows you to identify the policy
object.

The following policy object types can be created:

-   **policy**. A policy is created.
-   **policy\_type**. A policy type is defined.

The **additional** parameter takes a JSON object specifying additional
parameters that can be provided for the policy object type.

#### Policy

An example of the required format for **additional** JSON for a policy
is:

    {
        "policy_type_id": 1,
        "priority": 0,
        "details": {
            "fieldActions": [
                {
                    "action": "ADD_FIELD_VALUE",
                    "name": "FLAGGED",
                    "value": "TRUE"
                }
            ]
        }
    }

The **policy\_type\_id** field is mandatory, identifying the type of
policy being created. It is necessary to look up the ID of the desired
policy type, using the Retrieve Policy API call with the type parameter
set to **policy\_type**. The results of this call include the names and
IDs of all policy types. There are two built-in policy types, namely
Metadata and External. Other policy types can be created.

The **priority** field is mandatory. It contains the priority of the
policy which is used when resolving policy conflicts.

The **details** field is mandatory. It provides information for the
policy being created. It is specific to the policy type.

For a Metadata type policy, the details field includes the
**fieldActions**, which describe the additional fields to be added to a document.

For an External type policy, the details field includes the
**externalReference**, which contains the reference for the external
policy.

#### Policy Type

An example of the required format for **additional** JSON for a policy
type is:

    {
        "definition": {
            "title": "Metadata Policy Type",
            "description": "A metadata policy.",
            "type": "object",
            "properties": {
                "fieldActions": {
                    "items": {
                        "properties": {
                            "action": {
                                "description": "The type of action to perform on the field.",
                                "enum": [
                                    "ADD_FIELD_VALUE"
                                ],
                                "type": "string"
                            },
                            "name": {
                                "description": "The name of the field to perform the action on.",
                                "minLength": 1,
                                "type": "string"
                            },
                            "value": {
                                "description": "The value to use for the field action.",
                                "type": "string"
                            }
                        },
                        "required": [
                            "name",
                            "action"
                        ],
                        "title": "Field Action",
                        "type": "object"
                    },
                    "type": "array"
                }
            }
        },
        "short_name": "MetadataPolicy-HR",
        "conflict_resolution_mode": "priority"
    }

The **short\_name** field is mandatory. It is used to identify the
policy type internally, and its value must be unique within the system.

The **conflict\_resolution\_mode** field is optional. It is used when
resolving policy conflicts. It can have one of two values:

- priority

- custom

or it can be null, with null implying priority mode.

## Request

#### Parameters
This API accepts the following parameters:

<table>
    <tr>
        <td><b>Name</b></td>
        <td><b>Type</b></td>
        <td><b>Description</b></td>
    </tr>
    <tr>
        <td> name </td>
        <td> String </td>
        <td> The name of the policy object to add. Does not apply to lexicon_expression type.</td>
    </tr>
    <tr>
        <td> description </td>
        <td> String </td>
        <td> A textual description for the policy object. Does not apply to lexicon_expression or condition types.</td>
    </tr>
    <tr>
        <td> <b>type</b> </td>
        <td> enum &#60;type&#62; </td>
        <td> The type of policy object.</td>
    </tr>
    <tr>
        <td> <b>additional</b> </td>
        <td> object </td>
        <td> Specify a JSON object of additional parameters relevant for the type of policy object being added.</td>
    </tr>
</table>
*Required parameters are shown with names in bold.*

#### Enumeration Types
This API's parameters use the enumerations described below:

<table>
    <tr>
        <td><b>type</b> <br/> The type of policy object.</td>
    </tr>
    <tr>
        <td> policy </td>
        <td> <b>Policy</b><br/> A policy object of type policy. </td>
    </tr>
    <tr>
        <td> policy_type </td>
        <td> <b>Policy Type</b><br/> A policy object of type policy_type. </td>
    </tr>
</table>

## Response
This API returns a JSON response that is described by the model below. This single model is presented both as an easy to read 
abstract definition and as the formal JSON schema.

##### Model
This is an abstract definition of the response that describes each of the properties that might be returned.


**Create Policy Response {**  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; **id** (number, *optional*) &nbsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp; Id of the policy object.  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; **name** ( string, *optional*) &emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp; Name of the policy object.  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; **description** ( string or null , *optional*) &nbsp;&nbsp;&nbsp;Description of the policy object.  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; **type** (enum &lt;Type&gt;) &nbsp;&nbsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;The type of the policy object.  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; **additional** (object, *optional*) &nbsp;&emsp;&emsp;&emsp;&emsp;&emsp;JSON formatted additional information depending on the object type. See the schema for more details.  
**}**

**enum&lt;Type&gt; {**  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;'policy', 'policy_type'  
**}**

##### Model Schema 
This is a JSON schema that describes the syntax of the response. See json-schema.org for a complete reference.

<pre><code>
{
    "type": "object",
    "properties": {
        "id": {
            "type": "number",
            "multipleOf": 1
        },
        "name": {
            "type": "string",
            "minLength": 1
        },
        "type": {
            "enum": [
                "policy",
                "policy_type"
            ]
        },
        "additional": {
            "type": "object"
        }
    }
}
</code></pre>

## Delete Classification Objects
The Policy Management APIs provide an abstracted layer on top of entity
extraction, categorization and related functionality to allow the
definition of classifications and policies for the management of
information. Classifications can be created to group documents and other
objects into collections. Policies enact actions upon documents
associated with a collection.

Classification involves sorting documents into Collections; all
documents in a collection share some common criteria, identified using
Conditions.

The collection or collections that a document matches can influence how
it is indexed and determine policies that are executed against it.

A Collection Sequence is an ordered list of collections that documents
are assessed against; each collection is considered in order until the
document matches a collection with a stop on match instruction.
Documents can match multiple collections in a collection sequence.

The API allows collection sequences, collections, conditions, field labels, lexicons and lexicon expressions to be deleted.

Using this API you can delete classification objects. For example:

> /classification/delete/?type=collection\_sequence&id=1&id=2

The following classification object types can be deleted:

-   **collection**. A set of documents that share some common criteria.
-   **collection sequence**. An ordered set of Collections.
-   **condition**. Each Collection is identified using one or
    more Conditions.
-   **field label.** Information on how to retrieve a property value
    using one or more document fields.
-   **lexicon**. A list of terms, phrases or expressions that can be
    used when defining Conditions.
-   **lexicon expression**. An entry in a Lexicon.

A collection cannot be deleted if it is used in a collection sequence. A
lexicon cannot be deleted if it is used in a condition.

## Request

#### Parameters
This API accepts the following parameters:

<table>
    <tr>
        <td><b>Name</b></td>
        <td><b>Type</b></td>
        <td><b>Description</b></td>
    </tr>
    <tr>
        <td> <b>type</b> </td>
        <td> enum &#60;type&#62; </td>
        <td> The type of classification object.</td>
    </tr>
    <tr>
        <td> <b>id</b> </td>
        <td> array </td>
        <td> The ids of the object or objects to delete.</td>
    </tr>
</table>

*Required parameters are shown with names in bold.*

#### Enumeration Types
This API's parameters use the enumerations described below:

<table>
    <tr>
        <td><b>type</b> <br/> The type of classification object.</td>
    </tr>
    <tr>
        <td> collection_sequence </td>
        <td> <b>Collection Sequence</b><br/> A Collection Sequence classification object. </td>
    </tr>
    <tr>
        <td> collection </td>
        <td> <b>Collection</b><br/> A Collection classification object. </td>
    </tr>
    <tr>
        <td> condition </td>
        <td> <b>Condition</b><br/> A Condition classification object. </td>
    </tr>
    <tr>
        <td> lexicon </td>
        <td> <b>Lexicon</b><br/>A Lexicon classification object. </td>
    </tr>
    <tr>
        <td> lexicon_expression </td>
        <td> <b>Lexicon Expression</b><br/>A Lexicon Expression classification object. </td>
    </tr>
</table>

## Response
This API returns a JSON response that is described by the model below. This single model is presented both as an easy to read 
abstract definition and as the formal JSON schema.

##### Model
This is an abstract definition of the response that describes each of the properties that might be returned.

**Delete Classification Response {**  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; **result** (array[Result])    
**}**

**Result {**  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; <b>id</b> (number)  	
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; <b>success</b> (boolean)  	
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; <b>error_message</b> ( string or null , *optional*)  
**}**

##### Model Schema 
This is a JSON schema that describes the syntax of the response. See json-schema.org for a complete reference.

<pre><code>
{
    "type": "object",
    "required": [
        "result"
    ],
    "properties": {
        "result": {
            "type": "array",
            "items": {
                "type": "object",
                "properties": {
                    "id": {
                        "type": "number",
                        "multipleOf": 1
                    },
                    "success": {
                        "type": "boolean"
                    },
                    "error_message": {
                        "type": [
                            "string",
                            "null"
                        ]
                    }
                },
                "required": [
                    "id",
                    "success"
                ]
            }
        }
    }
}
</code></pre>

## Delete Policy Objects
The Policy Management APIs provide an abstracted layer on top of entity
extraction, categorization and related functionality to allow the
definition of classifications and policies for the management of
information. Classifications can be created to group documents and other
objects into collections. Policies enact actions upon documents
associated with a collection.

Classification involves sorting documents into Collections; all
documents in a collection share some common criteria, identified using
Conditions.

The collection or collections that a document matches can influence how
it is indexed and determine policies that are executed against it.

The API allows policy objects to be deleted.

To delete a policy object you must specify a policy object **type** and
**id**.

For example:

> /policy/delete/?id=3&type=policy

The following policy object types can be deleted:

-   **policy**. A policy object is deleted. You cannot delete a policy
    which is associated with a collection.

<!-- -->

-   **policy\_type**. A policy type is deleted.

The **id** parameter identifies the policy object to be deleted. A
single id or array of ids can be specified.

## Request

#### Parameters
This API accepts the following parameters:

<table>
    <tr>
        <td><b>Name</b></td>
        <td><b>Type</b></td>
        <td><b>Description</b></td>
    </tr>
    <tr>
        <td> <b>type</b> </td>
        <td> enum &#60;type&#62; </td>
        <td> The type of policy object to delete.</td>
    </tr>
    <tr>
        <td> <b>id</b> </td>
        <td> array </td>
        <td> The ids of the object or objects to delete.</td>
    </tr>
</table>

*Required parameters are shown with names in bold.*

#### Enumeration Types
This API's parameters use the enumerations described below:

<table>
    <tr>
        <td><b>type</b> <br/> The type of policy object.</td>
    </tr>
    <tr>
        <td> policy </td>
        <td> <b>Policy</b><br/> A policy object of type policy. </td>
    </tr>
    <tr>
        <td> policy_type </td>
        <td> <b>Policy Type</b><br/> A policy object of type policy_type. </td>
    </tr>
</table>

## Response
This API returns a JSON response that is described by the model below. This single model is presented both as an easy to read 
abstract definition and as the formal JSON schema.

##### Model
This is an abstract definition of the response that describes each of the properties that might be returned.

**Delete Policy Response {**  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; **result** (array[Result])    
**}**

**Result {**  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; <b>id</b> (number)  	
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; <b>success</b> (boolean)  	
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; <b>error_message</b> ( string or null , optional)  
**}**

##### Model Schema 
This is a JSON schema that describes the syntax of the response. See json-schema.org for a complete reference.

<pre><code>
{
    "type": "object",
    "required": [
        "result"
    ],
    "properties": {
        "result": {
            "type": "array",
            "items": {
                "type": "object",
                "properties": {
                    "id": {
                        "type": "number",
                        "multipleOf": 1
                    },
                    "success": {
                        "type": "boolean"
                    },
                    "error_message": {
                        "type": [
                            "string",
                            "null"
                        ]
                    }
                },
                "required": [
                    "id",
                    "success"
                ]
            }
        }
    }
}
</code></pre>

## Retrieve Classification Objects
The Policy Management APIs provide an abstracted layer on top of entity
extraction, categorization and related functionality to allow the
definition of classifications and policies for the management of
information. Classifications can be created to group documents and other
objects into collections. Policies enact actions upon documents
associated with a collection.

Classification involves sorting documents into Collections; all
documents in a collection share some common criteria, identified using
Conditions.

The collection or collections that a document matches can influence how
it is indexed and determine policies that are executed against it.

A Collection Sequence is an ordered list of collections that documents
are assessed against; each collection is considered in order until the
document matches a collection with a stop on match instruction.
Documents can match multiple collections in a collection sequence.

The API allows collection sequences, collections, conditions, field labels, lexicons and lexicon expressions to be retrieved.

Using this API you can retrieve classification objects. For example:

> /classification/retrieve?type=collection\_sequence&id=1&id=2

The following classification object types can be retrieved:

-   **collection**. A set of documents that share some common criteria.
-   **collection sequence**. An ordered set of Collections.
-   **condition**. Each Collection is identified using one or
    more Conditions.
-   **field label**. Information on how to retrieve a property value
    using one or more document fields.
-   **lexicon**. A list of terms, phrases or expressions that can be
    used when defining Conditions.
-   **lexicon expression**. An entry in a Lexicon.

**Start** and **max\_page\_results** can be used to specify the first
object and the number of objects to return.

The **additional** parameter takes a JSON object specifying additional
parameters that can be provided for Collection, Condition or Field Label object types.

#### Collection

An example of the required format for **additional** JSON for a
collection is:

    {
        "include_condition" : true,
        "include_children" : false
    }

**Include\_condition** indicates whether details should be returned for
the conditions of the collection.

**Include\_children** indicates whether child conditions for Boolean and
Not conditions should be returned.

#### Condition

An example of the required format for **additional** JSON for a
condition is:

    {
        "include_children" : true
    }

**Include\_children** indicates whether child conditions for Boolean and
Not conditions should be returned.

#### Field Label

An example of the required format for **additional** JSON for a field
label is:

    {
        "name" : "Field Label1"
    }

**Name** is used to request information for a specific field label with
the given name.

#### Sample Return Schema

Objects of type **collection** are returned in the following format when
**include\_condition** is true:

    {
        "id" : 34,
        "name" : "Collection 1",
        "description" : "Description for collection 1 ",
        "type" : "collection",
        "additional" : {
            "condition" : {
            "type" : "condition",
            "additional" : {
                "type" : "number",
                "field" : "SIZE",
                "operator" : "gt"
            }
        }
        "policy_ids" : [3,4]
    }

Objects of type **collection sequence** are returned in the following
format, provided that ids of the sequence or sequences to retrieve have
been supplied:

    {
        "id": 2,
        "name": "Collection Sequence 1",
        "description": "Description for collection sequence 1",
        "type": "collection_sequence",
        "additional": {
            "collection_count": 2,
            "collection_sequence_entries": [{
                "order": 100,
                "collection_ids": [125,77],
                "stop_on_match": false
            }],
            "default_collection_id": 4,
            "full_condition_evaluation": true,
            "modified_timestamp": "2014-12-08T17:54:40.000Z"
        }
    }

Objects of type **condition** are returned in the following format:

    {
        "id": 1994,
        "name": "Key Field Exists",
        "description": "",
        "type": "condition",
        "additional": {
            "type": "exists",
            "field": "KEY_FIELD",
            "notes": "Infomation about the condition"
        }
    }

Objects of type **field label** are returned in the following format:

    {
        "id": 1,
        "name": "Address",
        "description": null,
        "type": "field_label",
        "additional": {
            "field_type": "string",
            "fields": [ "City", "Town", "Village" ]
        }
    }

Objects of type **lexicon** are returned in the following format:

    {
        "id": 17,
        "name": "Animals",
        "description": "Finds documents relating to animals of interest.",
        "type": "lexicon",
        "additional": {
            "lexicon_expressions": [{
                "id": 17,
                "name": "African Elephant check",
                "description": "Checks for african elephants",
                "type" : "text",
                "expression" : "african DNEAR elephants"
            }]
        }
    }

Objects of type **lexicon expression** are returned in the following
format:

    {
        "id": 17,
        "name": "African Elephant check",
        "description": "Checks for african elephants",
        "type": "lexicon_expression",
        "additional": {
            "lexicon_id": 17,
            "type": "text",
            "expression": "african DNEAR elephants"
        }
    }


## Request

#### Parameters
This API accepts the following parameters:

<table>
    <tr>
        <td><b>Name</b></td>
        <td><b>Type</b></td>
        <td><b>Description</b></td>
    </tr>
    <tr>
        <td> id </td>
        <td> array </td>
        <td> The ids of the object or objects to retrieve.</td>
    </tr>
    <tr>
        <td> <b>type</b> </td>
        <td> enum &#60;type&#62; </td>
        <td> The type of classification object.</td>
    </tr>
    <tr>
        <td> start </td>
        <td> number </td>
        <td> The number of the first object to retrieve. Default value: 1.</td>
    </tr>
    <tr>
        <td> max_page_results </td>
        <td> number </td>
        <td> The maximum number of results to return. If you have set the Start parameter, 
        max_page_results sets the maximum number of results to return from the total results set. Default value: 6.</td>
    </tr>
    <tr>
        <td> additional </td>
        <td> object </td>
        <td> Specify a JSON object of additional parameters relevant for the type of classification object being added.</td>
    </tr>
</table>
*Required parameters are shown with names in bold.*

#### Enumeration Types
This API's parameters use the enumerations described below:

<table>
    <tr>
        <td><b>type</b> <br/> The type of classification object.</td>
    </tr>
    <tr>
        <td> collection_sequence </td>
        <td> <b>Collection Sequence</b><br/> A Collection Sequence classification object. </td>
    </tr>
    <tr>
        <td> collection </td>
        <td> <b>Collection</b><br/> A Collection classification object. </td>
    </tr>
    <tr>
        <td> condition </td>
        <td> <b>Condition</b><br/> A Condition classification object. </td>
    </tr>
    <tr>
        <td> lexicon </td>
        <td> <b>Lexicon</b><br/>A Lexicon classification object. </td>
    </tr>
    <tr>
        <td> lexicon_expression </td>
        <td> <b>Lexicon Expression</b><br/>A Lexicon Expression classification object. </td>
    </tr>
</table>

## Response
This API returns a JSON response that is described by the model below. This single model is presented both as an easy to read 
abstract definition and as the formal JSON schema.

##### Model
This is an abstract definition of the response that describes each of the properties that might be returned.

**Create Classification Response {**  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; **results** (number)  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; **Totalhits** (number, *optional*)	Total number of results.  
**}**  
**Results {**    
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; **id** (number) &nbsp;&nbsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;The id of the classification object.  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; **name** ( string or null , optional) &emsp;&emsp;&emsp;&emsp;The name of the classification object.  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; **description** ( string or null , optional) &emsp;The description of the classification object.  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; **type** (enum<Type>) &nbsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;The type of the classification object.  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; **additional** ( object or null , optional) &nbsp;&emsp;JSON formatted additional information depending on the object type.  
**}**  
**enum&lt;Type&gt; {**  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;'collection_sequence', 'collection', 'condition', 'lexicon', 'lexicon_expression'  
**}**

##### Model Schema 
This is a JSON schema that describes the syntax of the response. See json-schema.org for a complete reference.

<pre><code>
{
    "type": "object",
    "required": [
        "results"
    ],
    "properties": {
        "results": {
            "type": "array",
            "items": {
                "type": "object",
                "properties": {
                    "id": {
                        "type": "number",
                        "multipleOf": 1
                    },
                    "name": {
                        "type": [
                            "string",
                            "null"
                        ],
                        "minLength": 1
                    },
                    "type": {
                        "enum": [
                            "collection_sequence",
                            "collection",
                            "condition",
                            "lexicon",
                            "lexicon_expression"
                        ]
                    },
                    "additional": {
                        "type": [
                            "object",
                            "null"
                        ]
                    }
                },
                "required": [
                    "id",
                    "type"
                ],
                "oneOf": [
                    {
                        "$ref": "#/definitions/collection_sequence"
                    },
                    {
                        "$ref": "#/definitions/collection"
                    },
                    {
                        "$ref": "#/definitions/condition"
                    },
                    {
                        "$ref": "#/definitions/lexicon"
                    },
                    {
                        "$ref": "#/definitions/lexicon_expression"
                    }
                ]
            }
        },
        "totalhits": {
            "type": "number",
            "multipleOf": 1
        }
    },
    "definitions": {
        "collection_sequence": {
            "type": "object",
            "properties": {
                "type": {
                    "enum": [
                        "collection_sequence"
                    ]
                },
                "additional": {
                    "type": "object",
                    "properties": {
                        "collection_sequence_entries": {
                            "type": [
                                "array",
                                "null"
                            ],
                            "items": {
                                "$ref": "#/definitions/collection_sequence_entry"
                            }
                        },
                        "default_collection_id": {
                            "type": [
                                "number",
                                "null"
                            ],
                            "multipleOf": 1
                        },
                        "modified_timestamp": {
                            "type": "string"
                        },
                        "full_condition_evaluation": {
                            "type": [
                                "boolean",
                                "null"
                            ]
                        }
                    }
                }
            },
            "required": [
                "name",
                "modified_timestamp"
            ]
        },
        "collection_sequence_entry": {
            "type": "object",
            "properties": {
                "order": {
                    "type": "number",
                    "multipleOf": 1
                },
                "collection_ids": {
                    "type": [
                        "array",
                        "null"
                    ],
                    "items": {
                        "type": "number",
                        "multipleOf": 1
                    }
                },
                "stop_on_match": {
                    "type": [
                        "boolean",
                        "null"
                    ]
                }
            }
        },
        "collection": {
            "type": "object",
            "properties": {
                "type": {
                    "enum": [
                        "collection"
                    ]
                },
                "additional": {
                    "type": [
                        "object",
                        "null"
                    ],
                    "properties": {
                        "condition": {
                            "$ref": "#/definitions/condition"
                        },
                        "policy_ids": {
                            "type": [
                                "array",
                                "null"
                            ],
                            "items": {
                                "type": "number",
                                "multipleOf": 1
                            }
                        }
                    }
                }
            },
            "required": [
                "name"
            ]
        },
        "condition": {
            "type": [
                "object",
                "null"
            ],
            "properties": {
                "type": {
                    "enum": [
                        "condition"
                    ]
                },
                "additional": {
                    "type": "object",
                    "properties": {
                        "type": {
                            "enum": [
                                "boolean",
                                "regex",
                                "date",
                                "fragment",
                                "lexicon",
                                "not",
                                "number",
                                "string",
                                "exists",
                                "text"
                            ]
                        },
                        "notes": {
                            "type": [
                                "string",
                                "null"
                            ]
                        },
                        "target": {
                            "enum": [
                                "all",
                                "children",
                                "container"
                            ]
                        },
                        "include_descendants": {
                            "type": "boolean"
                        }
                    },
                    "required": [
                        "type"
                    ],
                    "oneOf": [
                        {
                            "$ref": "#/definitions/boolean_condition"
                        },
                        {
                            "$ref": "#/definitions/regex_condition"
                        },
                        {
                            "$ref": "#/definitions/date_condition"
                        },
                        {
                            "$ref": "#/definitions/exists_condition"
                        },
                        {
                            "$ref": "#/definitions/fragment_condition"
                        },
                        {
                            "$ref": "#/definitions/lexicon_condition"
                        },
                        {
                            "$ref": "#/definitions/not_condition"
                        },
                        {
                            "$ref": "#/definitions/number_condition"
                        },
                        {
                            "$ref": "#/definitions/string_condition"
                        },
                        {
                            "$ref": "#/definitions/text_condition"
                        }
                    ]
                }
            },
            "required": [
                "additional"
            ]
        },
        "boolean_condition": {
            "type": "object",
            "properties": {
                "type": {
                    "enum": [
                        "boolean"
                    ]
                },
                "operator": {
                    "type": "string",
                    "enum": [
                        "and",
                        "or"
                    ]
                },
                "children": {
                    "type": [
                        "array",
                        "null"
                    ],
                    "items": {
                        "$ref": "#/definitions/condition"
                    }
                }
            },
            "required": [
                "operator"
            ]
        },
        "regex_condition": {
            "type": "object",
            "properties": {
                "type": {
                    "enum": [
                        "regex"
                    ]
                },
                "field": {
                    "$ref": "#/definitions/field"
                },
                "value": {
                    "type": "string",
                    "minLength": 1
                }
            },
            "required": [
                "field",
                "value"
            ]
        },
        "date_condition": {
            "type": "object",
            "properties": {
                "type": {
                    "enum": [
                        "date"
                    ]
                },
                "field": {
                    "$ref": "#/definitions/field"
                },
                "value": {
                    "type": "string",
                    "minLength": 1
                },
                "operator": {
                    "enum": [
                        "before",
                        "after",
                        "on"
                    ]
                }
            },
            "required": [
                "field",
                "value",
                "operator"
            ]
        },
        "exists_condition": {
            "type": "object",
            "properties": {
                "type": {
                    "enum": [
                        "exists"
                    ]
                },
                "field": {
                    "$ref": "#/definitions/field"
                }
            },
            "required": [
                "field"
            ]
        },
        "fragment_condition": {
            "type": "object",
            "properties": {
                "type": {
                    "enum": [
                        "fragment"
                    ]
                },
                "value": {
                    "type": "number",
                    "multipleOf": 1
                }
            },
            "required": [
                "value"
            ]
        },
        "lexicon_condition": {
            "type": "object",
            "properties": {
                "type": {
                    "enum": [
                        "lexicon"
                    ]
                },
                "field": {
                    "$ref": "#/definitions/field"
                },
                "value": {
                    "type": "number",
                    "multipleOf": 1
                }
            },
            "required": [
                "field",
                "value"
            ]
        },
        "not_condition": {
            "type": "object",
            "properties": {
                "type": {
                    "enum": [
                        "not"
                    ]
                },
                "condition": {
                    "$ref": "#/definitions/condition"
                }
            }
        },
        "number_condition": {
            "type": "object",
            "properties": {
                "type": {
                    "enum": [
                        "number"
                    ]
                },
                "field": {
                    "$ref": "#/definitions/field"
                },
                "value": {
                    "type": "number",
                    "multipleOf": 1
                },
                "operator": {
                    "enum": [
                        "gt",
                        "lt",
                        "eq"
                    ]
                }
            },
            "required": [
                "field",
                "value",
                "operator"
            ]
        },
        "string_condition": {
            "type": "object",
            "properties": {
                "type": {
                    "enum": [
                        "string"
                    ]
                },
                "field": {
                    "$ref": "#/definitions/field"
                },
                "value": {
                    "type": "string"
                },
                "operator": {
                    "enum": [
                        "is",
                        "starts_with",
                        "ends_with"
                    ]
                }
            },
            "required": [
                "field",
                "value",
                "operator"
            ]
        },
        "text_condition": {
            "type": "object",
            "properties": {
                "type": {
                    "enum": [
                        "text"
                    ]
                },
                "field": {
                    "$ref": "#/definitions/field"
                },
                "value": {
                    "type": "string"
                },
                "language": {
                    "type": "string"
                }
            },
            "required": [
                "field",
                "value"
            ]
        },
        "field": {
            "type": "string",
            "minLength": 1
        },
        "lexicon": {
            "type": "object",
            "properties": {
                "type": {
                    "enum": [
                        "lexicon"
                    ]
                },
                "additional": {
                    "type": [
                        "object",
                        "null"
                    ],
                    "properties": {
                        "lexicon_expressions": {
                            "type": "array",
                            "items": {
                                "$ref": "#/definitions/lexicon_expression"
                            }
                        }
                    }
                }
            },
            "required": [
                "name"
            ]
        },
        "lexicon_expression": {
            "type": "object",
            "properties": {
                "type": {
                    "enum": [
                        "lexicon_expression"
                    ]
                },
                "additional": {
                    "type": [
                        "object",
                        "null"
                    ],
                    "properties": {
                        "lexicon_id": {
                            "type": "number",
                            "multipleOf": 1
                        },
                        "type": {
                            "enum": [
                                "regex",
                                "text"
                            ]
                        },
                        "expression": {
                            "type": "string",
                            "minLength": 1
                        }
                    },
                    "required": [
                        "lexicon_id",
                        "type",
                        "expression"
                    ]
                }
            }
        }
    }
}
</code></pre>

## Retrieve Policy Objects
The Policy Management APIs provide an abstracted layer on top of entity
extraction, categorization and related functionality to allow the
definition of classifications and policies for the management of
information. Classifications can be created to group documents and other
objects into collections. Policies enact actions upon documents
associated with a collection.

Classification involves sorting documents into Collections; all
documents in a collection share some common criteria, identified using
Conditions.

The collection or collections that a document matches can influence how
it is indexed and determine policies that are executed against it.

The API allows policy objects to be retrieved.

Using this API you can retrieve policy objects.

For example:

> /policy/retrieve?id=3&type=policy

The following policy object types can be retrieved:

-   **policy**. A policy is of a specific type and defines how documents
    should be treated.

-   **policy\_type**. A definition of a policy type.

**Start** and **max\_page\_results** can be used to specify the first
object and the number of objects to return.

The **additional** parameter takes a JSON object specifying additional
parameters that can be provided. For example:

    {
        "include_deleted": true
    }

**Include\_deleted** indicates whether the response should include
information on deleted policies.

Objects of type **policy** are returned in the following format:

    {
        "id": 3,
        "name": "Policy 1",
        "description": "Description for policy 1 ",
        "type": "policy",
        "additional": {
            "policy_type_id": 1,
            "priority": 0,
            "details": {
                "fieldActions": [
                    {
                        "action": "ADD_FIELD_VALUE",
                        "name": "FLAGGED",
                        "value": "TRUE"
                    }
                ]
            },
            "is_deleted": false
        }
    }

Objects of type **policy\_type** are returned in the following format:

    {
        "id": 7,
        "name": "Policy Type 1",
        "description": "Description for policy type 1 ",
        "type": "policy_type",
        "additional": {
            "definition": {
                "title": "Metadata Policy Type",
                "description": "A metadata policy.",
                "type": "object",
                "properties": {
                    "fieldActions": {
                        "items": {
                            "properties": {
                                "action": {
                                    "description": "The type of action to perform on the field.",
                                    "enum": [
                                        "ADD_FIELD_VALUE"
                                    ],
                                    "type": "string"
                                },
                                "name": {
                                    "description": "The name of the field to perform the action on.",
                                    "minLength": 1,
                                    "type": "string"
                                },
                                "value": {
                                    "description": "The value to use for the field action.",
                                    "type": "string"
                                }
                            },
                            "required": [
                                "name",
                                "action"
                            ],
                            "title": "Field Action",
                            "type": "object"
                        },
                        "type": "array"
                    }
                }
                "short_name": "index"
            }
        }
    }


## Request

#### Parameters
This API accepts the following parameters:

<table>
    <tr>
        <td><b>Name</b></td>
        <td><b>Type</b></td>
        <td><b>Description</b></td>
    </tr>
    <tr>
        <td> id </td>
        <td> array </td>
        <td> The ids of the object or objects to retrieve.</td>
    </tr>
    <tr>
        <td> <b>type</b> </td>
        <td> enum &#60;type&#62; </td>
        <td> The type of policy object.</td>
    </tr>
    <tr>
        <td> start </td>
        <td> number </td>
        <td> The number of the first object to retrieve. Default value: 1.</td>
    </tr>
    <tr>
        <td> max_page_results </td>
        <td> number </td>
        <td> The maximum number of results to return. If you have set the Start parameter, 
        max_page_results sets the maximum number of results to return from the total results set. Default value: 6.</td>
    </tr>
    <tr>
        <td> additional </td>
        <td> object </td>
        <td> Specify a JSON object of additional parameters relevant for the type of policy object being added.</td>
    </tr>
</table>
*Required parameters are shown with names in bold.*

#### Enumeration Types
This API's parameters use the enumerations described below:

<table>
    <tr>
        <td><b>type</b> <br/> The type of policy object.</td>
    </tr>
    <tr>
        <td> policy </td>
        <td> <b>Policy</b><br/> A policy object of type policy. </td>
    </tr>
    <tr>
        <td> policy_type </td>
        <td> <b>Policy Type</b><br/> A policy object of type policy_type. </td>
    </tr>
</table>

## Response
This API returns a JSON response that is described by the model below. This single model is presented both as an easy to read 
abstract definition and as the formal JSON schema.

##### Model
This is an abstract definition of the response that describes each of the properties that might be returned.

**Create Policy Response {**  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; **results** (number)  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; **Totalhits** (number, *optional*)	Total number of results.  
**}**  
**Results {**    
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; **id** (number) &nbsp;&nbsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;The id of the policy object.  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; **name** ( string or null , optional) &emsp;&emsp;&emsp;&emsp;The name of the policy object.  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; **description** ( string or null , optional) &emsp;The description of the policy object.  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; **type** (enum<Type>) &nbsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;The type of the policy object.  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; **additional** ( object or null , optional) &nbsp;&emsp;JSON formatted additional information depending on the object type.  
**}**  
**enum&lt;Type&gt; {**  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;'policy_type', 'policy'    
**}**

##### Model Schema 
This is a JSON schema that describes the syntax of the response. See json-schema.org for a complete reference.

<pre><code>
{
    "type": "object",
    "required": [
        "results"
    ],
    "properties": {
        "results": {
            "type": "array",
            "items": {
                "type": "object",
                "properties": {
                    "id": {
                        "type": "number",
                        "multipleOf": 1
                    },
                    "name": {
                        "type": [
                            "string",
                            "null"
                        ],
                        "minLength": 1
                    },
                    "type": {
                        "enum": [
                            "policy",
                            "policy_type"
                        ]
                    },
                    "additional": {
                        "type": [
                            "object",
                            "null"
                        ]
                    }
                },
                "required": [
                    "id",
                    "type"
                ],
                "oneOf": [
                    {
                        "$ref": "#/definitions/policy"
                    },
                    {
                        "$ref": "#/definitions/policy_type"
                    }
                ]
            }
        },
        "totalhits": {
            "type": "number",
            "multipleOf": 1
        }
    },
    "definitions": {
        "policy": {
            "type": "object",
            "properties": {
                "id": {
                    "type": "number",
                    "multipleOf": 1
                },
                "name": {
                    "type": "string"
                },
                "type": {
                    "enum": [
                        "policy"
                    ]
                },
                "additional": {
                    "type": "object",
                    "properties": {
                        "policy_type_id": {
                            "type": "number",
                            "multipleOf": 1
                        },
                        "priority": {
                            "type": "number",
                            "multipleOf": 1
                        },
                        "details": {
                            "type": "object"
                        }
                    },
                    "required": [
                        "id",
                        "policy_type_id",
                        "priority",
                        "details"
                    ]
                }
            },
            "required": [
                "name",
                "additional"
            ]
        },
        "policy_type": {
            "type": "object",
            "properties": {
                "id": {
                    "type": "number",
                    "multipleOf": 1
                },
                "name": {
                    "type": "string"
                },
                "type": {
                    "enum": [
                        "policy_type"
                    ]
                },
                "additional": {
                    "type": "object",
                    "properties": {
                        "definition": {
                            "type": "object"
                        },
                        "short_name": {
                            "type": "string"
                        }
                    },
                    "required": [
                        "short_name",
                        "definition"
                    ]
                }
            },
            "required": [
                "id",
                "name",
                "additional"
            ]
        }
    }
}
</code></pre>

## Update Classification Objects
The Policy Management APIs provide an abstracted layer on top of entity
extraction, categorization and related functionality to allow the
definition of classifications and policies for the management of
information. Classifications can be created to group documents and other
objects into collections. Policies enact actions upon documents
associated with a collection.

Classification involves sorting documents into Collections; all
documents in a collection share some common criteria, identified using
Conditions.

The collection or collections that a document matches can influence how
it is indexed and determine policies that are executed against it.

A Collection Sequence is an ordered list of collections that documents
are assessed against; each collection is considered in order until the
document matches a collection with a stop on match instruction.
Documents can match multiple collections in a collection sequence.

The API allows collection sequences, collections, conditions, field labels, lexicons and lexicon expressions to be
updated.

Using this API you can update existing classification objects.

> /classification/update?type=collection\_sequence&name=new\_sequence\_name&description=Updated%20descriptive%20text

The type of classification object you are updating determines the
additional information that can be provided in the additional parameter.

The following classification object types can be updated:

-   **collection**. A set of documents that share some common criteria.
-   **collection sequence**. An ordered set of Collections.
-   **condition**. Each Collection is identified using one or
    more Conditions.
-   **field label**. Information on how to retrieve a property value
    using one or more document fields. 
-   **lexicon**. A list of terms, phrases or expressions that can be
    used when defining Conditions.
-   **lexicon expression**. An entry in a Lexicon.

The **name** and **description** can be any values that help you
identify the classification object.

**name** is mandatory for collections, collection sequences and lexicons
and ignored for condition, lexicon\_expression and
field label types.

**description** is optional for collections, collection sequences and
lexicons and ignored for conditions, lexicon expressions and field labels.

The **additional** parameter takes a JSON object specifying additional
parameters that can be provided for the classification object type.

#### Collection

An example of the required format for **additional** JSON for a
collection object is:

    {
        "policy_ids" : [1, 2],
        "condition" :
        {
            "type" : "condition",
            "additional": {
                "type" : "string"
                "field" : "DRECONTENT",
                "value" : "cat DNEAR4 dog",
                "operator" : "contains"
            }
        }
    }

The **policy\_ids** field, if present, identifies one or more policies
that should be assigned to the collection. For more information about
policies, see Create Policy. You cannot have multiple policies of the
same type on a collection.

The **condition** field, if present, identifies one or more conditions
that are used to determine if documents are in the collection.

You can use undefined or null for **policy\_ids** or **condition**
fields to leave any existing entry unchanged; to remove all existing
entries use an empty array.

#### Collection Sequence

An example of the required format for **additional** JSON for a
collection sequence object is:

    {
        "collection_sequence_entries" :
            [{
                "order" : 10,
                "collection_ids" : [7],
                "stop_on_match" : "true"
            },
            {
                "order" : 20,
                "collection_ids" : [10,11],
                "stop_on_match" : "false"
            }],
        "default_collection_id" : 5
    }

The **collection\_sequence\_entries** array defines one or more entries
to add to the collection sequence. Each entry contains one or more
collections and has an **order** and a **stop\_on\_match** attribute.

The **order** field identifies where the collection entry appears in the
collection sequence. Any numeric value can be specified and entries are
ordered by their values, from lowest to highest.

The **collection\_ids** field identifies the collections that are being
added to the collection sequence. There will typically be one collection
per collection entry.

If a collection sequence entry has a **stop\_on\_match** parameter set
to true then classification against further collections in the
collection sequence will stop if a document matches one of the
collections in the collection entry. The default is false.

The **default\_collection\_id** field defines a default collection that
is assigned to documents that fail to match any collection in the
sequence.

The **full\_condition\_evaluation** parameter can be set to true to
cause all conditions within a boolean OR condition to be evaluated
instead of stopping after the first matching condition. The default is
false.

#### Condition

The **type** field identifies the type of the condition being added.
Valid values are Boolean, Date, Exists, Fragment, Lexicon, Not, Number,
Regex, String, Text and Entity.

##### Boolean Conditions

An example of the format for **additional** JSON for a condition object
of type **Boolean** is:

    {
        "type" : "boolean",
        "operator" : "or",
        "notes" : "Author is John Smith or Sarah Smith",
        "children": [
        {
            "type": "condition",
            "additional": {
                "type" : "string",
                "field" : "AUTHOR",
                "operator" : "is",
                "value" : "John Smith"
            } 
        },
        {
            "type": "condition",
            "additional": {
                "type" : "string",
                "field" : "AUTHOR",
                "operator" : "is",
                "value" : "Sarah Smith"
            }
        } ]
    }

This condition will match a document that has an AUTHOR field of John
Smith or Sarah Smith.

Boolean condition types require an **operator** parameter.

The **operator** parameter indicates the type of Boolean join that is
being performed on child conditions. Valid operators for Boolean
conditions are and and or.

The **notes** parameter allows you to store some additional infomation
about a condition.

##### Date Property Conditions

An example of the format for **additional** JSON for a condition object
of type **Date** is:

    {
        "type" : "date",
        "field" : "AU_IMPORT_CREATEDDATE_EPOCHSECONDS",
        "operator" : "before",
        "value" : "2014-10-10T10.13:19Z"
    }

Date condition types require **field**, **operator** and **value**
parameters.

The **field** parameter indicates the name of the date property being
tested.

The **operator** parameter indicates the type of test that is being
performed on the date property. Valid operators for Date conditions are
before, after and on.

The **value** parameter indicates the value that the date property is
being tested against.

The value can have the following formats:

-   A date in epoch post fixed with e: 1412935999e corresponds to
    2014-10-10T10.13:19Z
-   A date in ISO8601 format: 2014-10-10T10.13:19Z
-   A date period in ISO8601 period format: P3Y6M4DT12H30M5S corresponds
    to 3 years, 6 months, 4 days, 12 hours, 30 minutes and 5 seconds
-   A day of the week - The following values may be truncated to Mon
    etc.: Monday, Tuesday, Wednesday, Thursday, Friday, Saturday, Sunday
-   A time of day in hh:mm or hh:mm:ss format: 12:00 or 14:30

##### Exists Conditions

An example of the format for **additional** JSON for a condition object
of type **Exists** is:

    {
        "type" : "exists",
        "field" : "VITAL_FLAG"
    }

Exists condition types require a **field** parameter.

The **field** parameter indicates the name of the property being tested.

##### Fragment Conditions

An example of the format for **additional** JSON for a condition object
of type **Fragment** is (where 132 is the id of an existing condition
that was created with is\_fragment set to true):

    {
        "type" : "fragment",
        "value" : "132"
    }

Fragment condition types require a **value** parameter.

The **value** parameter indicates the condition fragment to be included.

##### Lexicon Conditions

An example of the format for **additional** JSON for a condition object
of type **Lexicon** is (where 12 is the id of an existing lexicon):

    {
        "type" : "lexicon",
        "field" : "TITLE",
        "value" : "12"
    }

Lexicon condition types require **field** and **value** parameters.

The **field** parameter indicates the name of the string property being
tested.

The **value** parameter indicates the lexicon that the string property
is being tested against.

##### Not Conditions

An example of the format for **additional** JSON for a condition object
of type **Not** is:

    {
        "type" : "not",
        "condition": {
            "type": "condition",
            "additional": {
                "type" : "string",
                "field" : "AUTHOR",
                "operator" : "is",
                "value" : "John Smith"
            }
        }
    }

This condition will match documents which have an AUTHOR field that is
NOT John Smith.

##### Number Property Conditions

An example of the format for **additional** JSON for a condition object
of type **Number** is:

    {
        "type" : "number",
        "field" : "SIZE",
        "operator" : "gt",
        "value" : 1000
    }

Number condition types require **fiel**d, **operator** and **value**
parameters.

The **field** parameter indicates the name of the numeric property being
tested.

The **operator** parameter indicates the type of test that is being
performed on the numeric property. Valid operators for Number conditions
are gt, lt and eq.

The **value** parameter indicates the value that the numeric property is
being tested against.

##### Regex Property Conditions

An example of the format for **additional** JSON for a condition object
of type **Regex** is:

    {
        "type" : "regex",
        "field" : "DRECONTENT",
        "value" : "\\b[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,4}\\b"
    }

Regex condition types require **field** and **value** parameters.

The **field** parameter indicates the name of the property being tested.

The **value** parameter indicates the regular expression that the
property is being tested against.

##### String Property Conditions

An example of the format for **additional** JSON for a condition object
of type **String** is:

    {
        "type" : "string",
        "field" : "AUTHOR",
        "operator" : "is",
        "value" : "John Smith"
    }

String condition types require **field**, **operator** and **value**
parameters.

The **field** parameter indicates the name of the string property being
tested.

The **operator** parameter indicates the type of test that is being
performed on the string property. Valid operators for String conditions
are is, starts\_with and ends\_with.

The **value** parameter indicates the value that the string property is
being tested against.

##### Text Property Conditions

An example of the format for **additional** JSON for a condition object
of type **Text** is:

    {
        "type" : "text",
        "field" : "DRECONTENT",
        "value" : "cat DNEAR4 dog"
    }

Text condition types require **field** and **value** parameters.

The **field** parameter indicates the name of the string property being
tested. This will normally be DRECONTENT.

The **value** parameter indicates the value that the string property is
being tested against.

#### Field Label

An example of the required format for **additional** JSON for a field
label object is:

    {
        "field_type" : "string",
        "fields": ["AU_REPOSITORY_TO_RFC822","To","To"]
    }

The **field\_type** indicates the type of the field label. This can be
string, number or date.

The **fields** argument can contain one or more field names that
provide the property value to be used. If multiple field names are
provided they are checked in order and the first value found is used.

#### Lexicon {.post}

An example of the required format for **additional** JSON for a lexicon
object is:

    {
        "lexicon_expressions": [
            {
                "type": "lexicon_expression",
                "additional": {
                    "type": "text",
                    "expression": "antelope"
                }
            },
            {
                "type": "lexicon_expression",
                "additional": {
                    "type": "text",
                    "expression": "african DNEAR elephants"
                }
            }
        ]
    }

The **lexicon\_expressions** array defines one or more lexicon
expressions to add to the lexicon. Each lexicon expression has a
**type** and an **expression**. You can use undefined or null to leave
any existing entry unchanged; to remove all existing entries use an
empty array.

The **type** field indicates the type of the lexicon expression, one of
text or regex.

The **expression** field contains the expression to add to the lexicon.
It can be a term, phrase or proximity expression for text type or a
regular expression for regex type.

#### Lexicon Expression

An example of the required format for **additional** JSON for a lexicon
expression object is:

    {
        "lexicon_id" : 2,
        "type" : "text",
        "expression" : "lion"
    }

The **lexicon\_id** identifies the lexicon that the lexicon expression
is to be added to. You can use undefined or null to leave any existing
entry unchanged; to remove all existing entries use an empty array.

The **type** field indicates the type of the lexicon expression, one of
text or regex.

The **expression** field contains the expression to add to the lexicon.
It can be a term, phrase or proximity expression for text type or a
regular expression for regex type.

## Request

#### Parameters
This API accepts the following parameters:

<table>
    <tr>
        <td><b>Name</b></td>
        <td><b>Type</b></td>
        <td><b>Description</b></td>
    </tr>
    <tr>
        <td> <b>id</b> </td>
        <td> number </td>
        <td> The id of the classification object to be updated.</td>
    </tr>
    <tr>
        <td> name </td>
        <td> String </td>
        <td> The name of the classification object to add. Does not apply to lexicon_expression type.</td>
    </tr>
    <tr>
        <td> description </td>
        <td> String </td>
        <td> A textual description for the classification object. Does not apply to lexicon_expression or condition types.</td>
    </tr>
    <tr>
        <td> <b>type</b> </td>
        <td> enum &#60;type&#62; </td>
        <td> The type of classification object.</td>
    </tr>
    <tr>
        <td> additional </td>
        <td> object </td>
        <td> Specify a JSON object of additional parameters relevant for the type of classification object being added.</td>
    </tr>
</table>
*Required parameters are shown with names in bold.*

#### Enumeration Types
This API's parameters use the enumerations described below:

<table>
    <tr>
        <td><b>type</b> <br/> The type of classification object.</td>
    </tr>
    <tr>
        <td> collection_sequence </td>
        <td> <b>Collection Sequence</b><br/> A Collection Sequence classification object. </td>
    </tr>
    <tr>
        <td> collection </td>
        <td> <b>Collection</b><br/> A Collection classification object. </td>
    </tr>
    <tr>
        <td> condition </td>
        <td> <b>Condition</b><br/> A Condition classification object. </td>
    </tr>
    <tr>
        <td> lexicon </td>
        <td> <b>Lexicon</b><br/>A Lexicon classification object. </td>
    </tr>
    <tr>
        <td> lexicon_expression </td>
        <td> <b>Lexicon Expression</b><br/>A Lexicon Expression classification object. </td>
    </tr>
</table>

## Response
This API returns a JSON response that is described by the model below. This single model is presented both as an easy to read 
abstract definition and as the formal JSON schema.

##### Model
This is an abstract definition of the response that describes each of the properties that might be returned.


**Create Classification Response {**  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; **id** (number) &nbsp;&nbsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp; Id of the classification object.  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; **name** ( string or null , *optional*) &emsp;&emsp;&emsp;&emsp; Name of the classification object.  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; **description** ( string or null , *optional*) &emsp; Description of the classification object.  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; **type** (enum &lt;Type&gt;) &nbsp;&nbsp;&nbsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;The type of the classification object.  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; **additional** (object, *optional*) &nbsp;&nbsp;&emsp;&emsp;&emsp;&emsp;&emsp;JSON formatted additional information depending on the object type. See the schema for more details.  
**}**

**enum&lt;Type&gt; {**  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;'collection_sequence', 'collection', 'condition', 'lexicon', 'lexicon_expression'  
**}**

##### Model Schema 
This is a JSON schema that describes the syntax of the response. See json-schema.org for a complete reference.

<pre><code>
{
    "type": "object",
    "properties": {
        "id": {
            "type": "number",
            "multipleOf": 1
        },
        "name": {
            "type": "string",
            "minLength": 1
        },
        "type": {
            "enum": [
                "collection_sequence",
                "collection",
                "condition",
                "lexicon",
                "lexicon_expression"
            ]
        },
        "additional": {
            "type": "object"
        }
    },
    "required": [
        "id",
        "name",
        "type"
    ],
    "oneOf": [
        {
            "$ref": "#/definitions/collection_sequence"
        },
        {
            "$ref": "#/definitions/collection"
        },
        {
            "$ref": "#/definitions/condition"
        },
        {
            "$ref": "#/definitions/lexicon"
        },
        {
            "$ref": "#/definitions/lexicon_expression"
        }
    ],
    "definitions": {
        "collection_sequence": {
            "type": "object",
            "properties": {
                "type": {
                    "enum": [
                        "collection_sequence"
                    ]
                },
                "additional": {
                    "type": "object",
                    "properties": {
                        "collection_sequence_entries": {
                            "type": [
                                "array",
                                "null"
                            ],
                            "items": {
                                "$ref": "#/definitions/collection_sequence_entry"
                            }
                        },
                        "default_collection_id": {
                            "type": [
                                "number",
                                "null"
                            ],
                            "multipleOf": 1
                        },
                        "full_condition_evaluation": {
                            "type": "boolean"
                        }
                    }
                }
            },
            "required": [
                "name"
            ]
        },
        "collection_sequence_entry": {
            "type": "object",
            "properties": {
                "order": {
                    "type": "number",
                    "multipleOf": 1
                },
                "collection_ids": {
                    "type": [
                        "array",
                        "null"
                    ],
                    "items": {
                        "type": "number",
                        "multipleOf": 1
                    }
                },
                "stop_on_match": {
                    "type": "boolean"
                }
            }
        },
        "collection": {
            "type": "object",
            "properties": {
                "type": {
                    "enum": [
                        "collection"
                    ]
                },
                "additional": {
                    "type": "object",
                    "properties": {
                        "condition": {
                            "$ref": "#/definitions/condition"
                        },
                        "policy_ids": {
                            "type": [
                                "array",
                                "null"
                            ],
                            "items": {
                                "type": "number",
                                "multipleOf": 1
                            }
                        }
                    }
                }
            },
            "required": [
                "name"
            ]
        },
        "condition": {
            "type": "object",
            "properties": {
                "type": {
                    "enum": [
                        "condition"
                    ]
                },
                "additional": {
                    "type": "object",
                    "properties": {
                        "type": {
                            "enum": [
                                "boolean",
                                "regex",
                                "date",
                                "fragment",
                                "lexicon",
                                "not",
                                "number",
                                "string",
                                "exists",
                                "text"
                            ]
                        },
                        "notes": {
                            "type": [
                                "string",
                                "null"
                            ]
                        },
                        "target": {
                            "enum": [
                                "all",
                                "children",
                                "container"
                            ]
                        },
                        "include_descendants": {
                            "type": "boolean"
                        }
                    },
                    "required": [
                        "type"
                    ],
                    "oneOf": [
                        {
                            "$ref": "#/definitions/boolean_condition"
                        },
                        {
                            "$ref": "#/definitions/regex_condition"
                        },
                        {
                            "$ref": "#/definitions/date_condition"
                        },
                        {
                            "$ref": "#/definitions/exists_condition"
                        },
                        {
                            "$ref": "#/definitions/fragment_condition"
                        },
                        {
                            "$ref": "#/definitions/lexicon_condition"
                        },
                        {
                            "$ref": "#/definitions/not_condition"
                        },
                        {
                            "$ref": "#/definitions/number_condition"
                        },
                        {
                            "$ref": "#/definitions/string_condition"
                        },
                        {
                            "$ref": "#/definitions/text_condition"
                        }
                    ]
                }
            },
            "required": [
                "additional"
            ]
        },
        "boolean_condition": {
            "type": "object",
            "properties": {
                "type": {
                    "enum": [
                        "boolean"
                    ]
                },
                "operator": {
                    "type": "string",
                    "enum": [
                        "and",
                        "or"
                    ]
                },
                "children": {
                    "type": [
                        "array",
                        "null"
                    ],
                    "items": {
                        "$ref": "#/definitions/condition"
                    }
                }
            },
            "required": [
                "operator"
            ]
        },
        "regex_condition": {
            "type": "object",
            "properties": {
                "type": {
                    "enum": [
                        "regex"
                    ]
                },
                "field": {
                    "$ref": "#/definitions/field"
                },
                "value": {
                    "type": "string",
                    "minLength": 1
                }
            },
            "required": [
                "field",
                "value"
            ]
        },
        "date_condition": {
            "type": "object",
            "properties": {
                "type": {
                    "enum": [
                        "date"
                    ]
                },
                "field": {
                    "$ref": "#/definitions/field"
                },
                "value": {
                    "type": "string",
                    "minLength": 1
                },
                "operator": {
                    "enum": [
                        "before",
                        "after",
                        "on"
                    ]
                }
            },
            "required": [
                "field",
                "value",
                "operator"
            ]
        },
        "exists_condition": {
            "type": "object",
            "properties": {
                "type": {
                    "enum": [
                        "exists"
                    ]
                },
                "field": {
                    "$ref": "#/definitions/field"
                }
            },
            "required": [
                "field"
            ]
        },
        "fragment_condition": {
            "type": "object",
            "properties": {
                "type": {
                    "enum": [
                        "fragment"
                    ]
                },
                "value": {
                    "type": "number",
                    "multipleOf": 1
                }
            },
            "required": [
                "value"
            ]
        },
        "lexicon_condition": {
            "type": "object",
            "properties": {
                "type": {
                    "enum": [
                        "lexicon"
                    ]
                },
                "field": {
                    "$ref": "#/definitions/field"
                },
                "value": {
                    "type": "number",
                    "multipleOf": 1
                }
            },
            "required": [
                "field",
                "value"
            ]
        },
        "not_condition": {
            "type": "object",
            "properties": {
                "type": {
                    "enum": [
                        "not"
                    ]
                },
                "condition": {
                    "$ref": "#/definitions/condition"
                }
            }
        },
        "number_condition": {
            "type": "object",
            "properties": {
                "type": {
                    "enum": [
                        "number"
                    ]
                },
                "field": {
                    "$ref": "#/definitions/field"
                },
                "value": {
                    "type": "number",
                    "multipleOf": 1
                },
                "operator": {
                    "enum": [
                        "gt",
                        "lt",
                        "eq"
                    ]
                }
            },
            "required": [
                "field",
                "value",
                "operator"
            ]
        },
        "string_condition": {
            "type": "object",
            "properties": {
                "type": {
                    "enum": [
                        "string"
                    ]
                },
                "field": {
                    "$ref": "#/definitions/field"
                },
                "value": {
                    "type": "string"
                },
                "operator": {
                    "enum": [
                        "is",
                        "starts_with",
                        "ends_with"
                    ]
                }
            },
            "required": [
                "field",
                "value",
                "operator"
            ]
        },
        "text_condition": {
            "type": "object",
            "properties": {
                "type": {
                    "enum": [
                        "text"
                    ]
                },
                "field": {
                    "$ref": "#/definitions/field"
                },
                "value": {
                    "type": "string"
                },
                "language": {
                    "type": [
                        "string",
                        "null"
                    ]
                }
            },
            "required": [
                "field",
                "value"
            ]
        },
        "field": {
            "type": "string",
            "minLength": 1
        },
        "lexicon": {
            "type": "object",
            "properties": {
                "type": {
                    "enum": [
                        "lexicon"
                    ]
                },
                "additional": {
                    "type": "object",
                    "properties": {
                        "lexicon_expressions": {
                            "type": "array",
                            "items": {
                                "$ref": "#/definitions/lexicon_expression"
                            }
                        }
                    }
                }
            },
            "required": [
                "name"
            ]
        },
        "lexicon_expression": {
            "type": "object",
            "properties": {
                "type": {
                    "enum": [
                        "lexicon_expression"
                    ]
                },
                "additional": {
                    "type": "object",
                    "properties": {
                        "lexicon_id": {
                            "type": "number",
                            "multipleOf": 1
                        },
                        "type": {
                            "enum": [
                                "regex",
                                "text"
                            ]
                        },
                        "expression": {
                            "type": "string",
                            "minLength": 1
                        }
                    },
                    "required": [
                        "lexicon_id",
                        "type",
                        "expression"
                    ]
                }
            }
        }
    }
}
</code></pre>

## Update Policy Objects
The Policy Management APIs provide an abstracted layer on top of entity
extraction, categorization and related functionality to allow the
definition of classifications and policies for the management of
information. Classifications can be created to group documents and other
objects into collections. Policies enact actions upon documents
associated with a collection.

Classification involves sorting documents into Collections; all
documents in a collection share some common criteria, identified using
Conditions.

The collection or collections that a document matches can influence how
it is indexed and determine policies that are executed against it.

The API allows policy objects to be updated.

To update a policy object you must specify a policy object **id**,
**type** and supply additional parameters in **additional**.

For example:

> /policy/update?id=3&type=policy&description=NewDescription

The **id** identifies the policy object to be updated.

The following policy object types can be updated:

-   **policy**. A policy is updated.

-   **policy\_type**. A policy type is updated.

The **additional** parameter takes a JSON object specifying additional
parameters that can be provided for the policy object type.

#### Policy

An example of the required format for **additional** JSON for a policy
is:

    {
        "policy_type_id": 1,
        "priority": 0,
        "details": {
            "fieldActions": [
                {
                    "action": "ADD_FIELD_VALUE",
                    "name": "FLAGGED",
                    "value": "TRUE"
                }
            ]
        }
    }

The **policy\_type\_id** field is mandatory, identifying the type of
policy being created. Valid values are:

1 - Metadata policy

The **priority** field is mandatory. It contains the priority of the
policy which is used when resolving policy conflicts.

The **details** field is mandatory. It provides information for the
policy being created.

#### Policy Type

An example of the required format for **additional** JSON for a policy
type is:

    {
        "definition": {
            "title": "Metadata Policy Type",
            "description": "A metadata policy.",
            "type": "object",
            "properties": {
                "fieldActions": {
                    "items": {
                        "properties": {
                            "action": {
                                "description": "The type of action to perform on the field.",
                                "enum": [
                                    "ADD_FIELD_VALUE"
                                ],
                                "type": "string"
                            },
                            "name": {
                                "description": "The name of the field to perform the action on.",
                                "minLength": 1,
                                "type": "string"
                            },
                            "value": {
                                "description": "The value to use for the field action.",
                                "type": "string"
                            }
                        },
                        "required": [
                            "name",
                            "action"
                        ],
                        "title": "Field Action",
                        "type": "object"
                    },
                    "type": "array"
                }
            }
        },
        "conflict_resolution_mode": "priority"
    }


## Request

#### Parameters
This API accepts the following parameters:

<table>
    <tr>
        <td><b>Name</b></td>
        <td><b>Type</b></td>
        <td><b>Description</b></td>
    </tr>
    <tr>
        <td> <b>id</b> </td>
        <td> number </td>
        <td> The id of the policy object to update.</td>
    </tr>
    <tr>
        <td> <b>name</b> </td>
        <td> String </td>
        <td> The name of the policy object to add. Does not apply to lexicon_expression type.</td>
    </tr>
    <tr>
        <td> description </td>
        <td> String </td>
        <td> A textual description for the policy object. Does not apply to lexicon_expression or condition types.</td>
    </tr>
    <tr>
        <td> <b>type</b> </td>
        <td> enum &#60;type&#62; </td>
        <td> The type of policy object.</td>
    </tr>
    <tr>
        <td> <b> additional </b> </td>
        <td> object </td>
        <td> Specify a JSON object of additional parameters relevant for the type of policy object being added.</td>
    </tr>
</table>
*Required parameters are shown with names in bold.*

#### Enumeration Types
This API's parameters use the enumerations described below:

<table>
    <tr>
        <td><b>type</b> <br/> The type of policy object.</td>
    </tr>
    <tr>
        <td> policy </td>
        <td> <b>Policy</b><br/> A policy object of type policy. </td>
    </tr>
    <tr>
        <td> policy_type </td>
        <td> <b>Policy Type</b><br/> A policy object of type policy_type. </td>
    </tr>
</table>

## Response
This API returns a JSON response that is described by the model below. This single model is presented both as an easy to read 
abstract definition and as the formal JSON schema.

##### Model
This is an abstract definition of the response that describes each of the properties that might be returned.

**Create Policy Response {**  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; **id** (number, *optional*) &nbsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp; Id of the policy object.  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; **name** ( string, *optional*) &emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp; Name of the policy object.  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; **description** ( string or null , *optional*) &nbsp;&nbsp;&nbsp;Description of the policy object.  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; **type** (enum &lt;Type&gt;) &nbsp;&nbsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;The type of the policy object.  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; **additional** (object, *optional*) &nbsp;&emsp;&emsp;&emsp;&emsp;&emsp;JSON formatted additional information depending on the object type. See the schema for more details.  
**}**

**enum&lt;Type&gt; {**  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;'policy', 'policy_type'  
**}**


##### Model Schema 
This is a JSON schema that describes the syntax of the response. See json-schema.org for a complete reference.

<pre><code>
{
    "type": "object",
    "properties": {
        "id": {
            "type": "number",
            "multipleOf": 1
        },
        "name": {
            "type": "string",
            "minLength": 1
        },
        "type": {
            "enum": [
                "policy",
                "policy_type"
            ]
        },
        "additional": {
            "type": "object"
        }
    }
}
</code></pre>

# Workflow API
The Workflow API provide the user a way to conditionally control the flow of their documents to various CAF Workers.

This document shows how requests can be made both through the web service and programmatically. 

## Create Workflow 

To create a Workflow object you must specify the **name** and supply additional parameters in **additional**. 
You can also supply a **description** to help identify the workflow.
For example:

>   /workflow/create?type=sequence_workflow&name=tryme&additional={}

Here is an example of the **additional** JSON format:

    {  
        "sequence_entries":[
            {
                "collection_sequence_id":669,"order":null
            }
        ],  
        "notes":null   
    }
    
Programmatically you would call WorkflowApi.create(SequenceWorkflow newWorkflow). This returns the created SequenceWorkflow.

## Update Workflow

To update a Workflow object you must specify the **id** of the sequenceWorkflow you wish to update and supply additional parameters in **additional** 
You can also supply a **description** if you wish to also update that. 

For example: 
>   /workflow/update?type=sequence_workflow&id=69&description=newDescription

Here is an example of the **additional** JSON format:

    {
        "sequence_entries":[
            {
                "collection_sequence_id":669,"order":null
            },
            {
                "collection_sequence_id":670,"order":200
            }
        ],
        "notes":null
    }
    
Programmatically you would call WorkflowApi.update(SequenceWorkflow updatedWorkflow). This returns the updated SequenceWorkflow.

## Delete Workflow

To delete a Workflow object you must specify the **id** of the sequenceWorkflow you wish to delete.

For example:
>   /workflow/delete?type=sequence_workflow&id=62

Programmatically you would call WorkflowApi.delete(Long id). 

## Retrieve Workflow

To retrieve a Workflow object you must specify the **id** of the sequenceWorkflow you wish to retrieve.

For Example:
>   /workflow/retrieve?type=sequence_workflow&max_page_results=6&start=1&id=62

This will return the following JSON format:

    {
        "results":[
            {
                "type":"sequence_workflow",
                "id":62,
                "name":"tryme",
                "description":"any description",
                "additional":
                {
                    "sequence_entries":
                    [
                        {
                            "collection_sequence_id":669,"order":400
                        },
                        {
                            "collection_sequence_id":670,"order":200
                        }
                    ],
                    "notes":null
                }
            }
        ],
        "totalhits":1
    }
    
Programmatically you would call WorkflowApi.retrieve(Long id). This returns the SequenceWorkflow with that ID.

## Retrieve Workflow Entries

To retrieve a paged list of Workflow Entries you must specify the **id** of the SequenceWorkflow they belong to in **filter** object.

For Example:

>   /workflow/retrieve?type=sequence\_workflow\_entry&max\_page\_results=6&start=1&additional={"filter":{"sequence\_workflow\_id":377}}

This will return the following JSON format:

<pre><code>
{
  "results": [
    {
      "type": "sequence_workflow_entry",
      "id": null,
      "additional": {
        "collection_sequence_id": 3624,
        "order": 200,
        "collection_sequence": null
      }
    },
    {
      "type": "sequence_workflow_entry",
      "id": null,
      "additional": {
        "collection_sequence_id": 3625,
        "order": 300,
        "collection_sequence": null
      }
    },
    {
      "type": "sequence_workflow_entry",
      "id": null,
      "additional": {
        "collection_sequence_id": 3623,
        "order": 400,
        "collection_sequence": null
      }
    }
  ],
  "totalhits": 3
}
</code></pre>

You can also specify the optional parameter _include\_collection\_sequences_ to retrieve the collection sequence they entry maps to.

For Example:
>  /workflow/retrieve?type=sequence\_workflow\_entry&max\_page\_results=2&start=1&additional={"filter":{"sequence\_workflow\_id":377}, "include\_collection\_sequences":"true"}

 
This will return the following JSON format:
<pre><code>
  {
  "results": [
    {
      "type": "sequence_workflow_entry",
      "id": null,
      "additional": {
        "collection_sequence_id": 3624,
        "order": 200,
        "collection_sequence": {
          "type": "collection_sequence",
          "id": 3624,
          "name": "WorkflowApiTesting96fe837b-d27e-4120-96ec-fbecd60c75a1",
          "description": "My description",
          "additional": {
            "collection_sequence_entries": [],
            "default_collection_id": null,
            "collection_count": 1,
            "last_modified": "2016-02-18T13:44:07.227Z",
            "full_condition_evaluation": false,
            "evaluation_enabled": true,
            "excluded_document_condition_id": null,
            "fingerprint": null
          }
        }
      }
    },
    {
      "type": "sequence_workflow_entry",
      "id": null,
      "additional": {
        "collection_sequence_id": 3625,
        "order": 300,
        "collection_sequence": {
          "type": "collection_sequence",
          "id": 3625,
          "name": "WorkflowApiTestingea2c8afc-b8c5-49e5-98b4-4ff600baabcf",
          "description": "My description",
          "additional": {
            "collection_sequence_entries": [],
            "default_collection_id": null,
            "collection_count": 1,
            "last_modified": "2016-02-18T13:44:07.227Z",
            "full_condition_evaluation": false,
            "evaluation_enabled": true,
            "excluded_document_condition_id": null,
            "fingerprint": null
          }
        }
      }
    }
  ],
  "totalhits": 3
}
</code></pre>

## Filtering
You can filter the Policy objects returned from the request by adding the `filter` property to the request. The `filter` is a JSON object that specifies the property name you wish to filter by and the value to use. For certain Workflow objects you can also filter using properties on their child objects using the following syntax  `child_object.propertyname`

For example:

<pre><code> "filter":{"sequence_entries.collection_sequence_id":27} </code></pre>

You can specify multiple filter conditions that will be joined using an AND operation, such as:

<pre><code>"filter": {
  "sequence_entries.collection_sequence_id:27",
  "sequence_entries.collection_sequence.evaluation_enabled": true
}
</code></pre>

Currently the following properties can be filtered on with **Sequence Workflow** objects:

*	id
*	name
*	sequence\_entries.id
*	sequence\_entries.collection\_sequence\_id
*	sequence\_entries.collection\_sequence.name
*	sequence\_entries.collection\_sequence.description
*	sequence\_entries.collection\_sequence.collection\_sequence\_entries
*	sequence\_entries.collection\_sequence.evaluation\_enabled

Currently the following properties can be filtered on with **Sequence Workflow Entry** objects:

*	id
*	sequence\_workflow\_id
*	collection\_sequence\_id
*	collection\_sequence.name
*	collection\_sequence.description
*	collection\_sequence.collection\_sequence_entries
*	collection\_sequence.evaluation\_enabled

A full example request that retrieves Sequence Workflow Entries filtered by Sequence Workflow Id and their Collection Sequence is enabled would be:

>/corepolicy/workflow/retrieve?type=sequence\_workflow\_entry&max\_page\_results=3&start=1&additional={"filter":{"sequence\_workflow\_id":51,"collection\_sequence.evaluation\_enabled":true},"sort":{"order":false},"include\_collection\_sequences":true}&project\_id=1

This returns the following JSON format:

<pre><code>{
  "results": [
    {
      "type": "sequence_workflow_entry",
      "id": null,
      "additional": {
        "collection_sequence_id": 324,
        "order": 9,
        "collection_sequence": {
          "type": "collection_sequence",
          "id": 324,
          "name": "createCollectionSequenceLocal_2bbcead6-7578-4412-992b-8d65c2c7ebea",
          "description": "createCollectionSequenceLocal_4686fff1-7e6d-4d77-ae0e-2f734d025884",
          "additional": {
            "collection_sequence_entries": [
              
            ],
            "default_collection_id": null,
            "collection_count": 1,
            "last_modified": "2016-03-02T10:54:44.690Z",
            "full_condition_evaluation": false,
            "evaluation_enabled": true,
            "excluded_document_condition_id": null,
            "fingerprint": null
          }
        },
        "sequence_workflow_id": 51
      }
    },
    {
      "type": "sequence_workflow_entry",
      "id": null,
      "additional": {
        "collection_sequence_id": 323,
        "order": 8,
        "collection_sequence": {
          "type": "collection_sequence",
          "id": 323,
          "name": "createCollectionSequenceLocal_bff51a1b-3825-4862-8e84-dc1f26059bed",
          "description": "createCollectionSequenceLocal_5e1226d0-f246-426d-8111-220c48a60ba7",
          "additional": {
            "collection_sequence_entries": [
              
            ],
            "default_collection_id": null,
            "collection_count": 1,
            "last_modified": "2016-03-02T10:54:44.690Z",
            "full_condition_evaluation": false,
            "evaluation_enabled": true,
            "excluded_document_condition_id": null,
            "fingerprint": null
          }
        },
        "sequence_workflow_id": 51
      }
    },
    {
      "type": "sequence_workflow_entry",
      "id": null,
      "additional": {
        "collection_sequence_id": 322,
        "order": 7,
        "collection_sequence": {
          "type": "collection_sequence",
          "id": 322,
          "name": "createCollectionSequenceLocal_b48241ee-a36c-4790-9d0e-032189981d4b",
          "description": "createCollectionSequenceLocal_3a258d83-6322-4e43-b856-ae85e579263f",
          "additional": {
            "collection_sequence_entries": [
              
            ],
            "default_collection_id": null,
            "collection_count": 1,
            "last_modified": "2016-03-02T10:54:44.690Z",
            "full_condition_evaluation": false,
            "evaluation_enabled": true,
            "excluded_document_condition_id": null,
            "fingerprint": null
          }
        },
        "sequence_workflow_id": 51
      }
    }
  ],
  "totalhits": 10
}
</code></pre>

A more advanced filter example which filters Seqeuence Workflow Entries by Sequence Workflow Id, evaluation enabled and Collection Sequence name would be:

> /corepolicy/workflow/retrieve?type=sequence\_workflow\_entry&max\_page\_results=5&start=1&additional={"filter":{"sequence\workflow\_id":51,"collection\_sequence.evaluation\_enabled":true,"collection\_sequence.name":"My\_Collection\_Sequence"},"sort":{"order":false},"include\_collection\_sequences":true}&project\_id=1

<pre><code>{
  "results": [
    {
      "type": "sequence_workflow_entry",
      "id": null,
      "additional": {
        "collection_sequence_id": 321,
        "order": 6,
        "collection_sequence": {
          "type": "collection_sequence",
          "id": 321,
          "name": "My_Collection_Sequence",
          "description": "An example Collection Sequence",
          "additional": {
            "collection_sequence_entries": [
              
            ],
            "default_collection_id": null,
            "collection_count": 1,
            "last_modified": "2016-03-02T10:54:44.690Z",
            "full_condition_evaluation": false,
            "evaluation_enabled": true,
            "excluded_document_condition_id": null,
            "fingerprint": null
          }
        },
        "sequence_workflow_id": 51
      }
    }
  ],
  "totalhits": 1
}
</code></pre>


## Sorting

You can override the default ordering of the retrieved items by passing the **sort** property into the request. Similar to the **filter** property, the sort specifies the field to sort on and a boolean specifying ascending (true) or descending (false). Sorting can be used in combination with filtering.

For example: 

	"sort":{"order":true}
Will sort Workflow entries by their order (priority) ascending.

Currently, **only** Workflow Entries can be sorted on with the following properties:

- order
- collection_sequence.name

A full example request that retrieves workflow entries filtered by workflow Id, attaches the collection sequences and sorts by the collection sequence name would be:
> /workflow/retrieve?type=sequence\_workflow\_entry&max\_page\_results=6&start=1&additional={"filter":{"sequence\_workflow_id":377},"sort":{"collection\_sequence.name":true},"include\_collection\_sequences":"true"}

This returns the following JSON format:
<pre><code>{
  "results": [
    {
      "type": "sequence_workflow_entry",
      "id": null,
      "additional": {
        "collection_sequence_id": 3624,
        "order": 200,
        "collection_sequence": {
          "type": "collection_sequence",
          "id": 3624,
          "name": "CollectionSequence_A",
          "description": "My description",
          "additional": {
            "collection_sequence_entries": [],
            "default_collection_id": null,
            "collection_count": 1,
            "last_modified": "2016-02-18T13:44:07.227Z",
            "full_condition_evaluation": false,
            "evaluation_enabled": true,
            "excluded_document_condition_id": null,
            "fingerprint": null
          }
        }
      }
    },
    {
      "type": "sequence_workflow_entry",
      "id": null,
      "additional": {
        "collection_sequence_id": 3623,
        "order": 400,
        "collection_sequence": {
          "type": "collection_sequence",
          "id": 3623,
          "name": "CollectionSequence_B",
          "description": "My description",
          "additional": {
            "collection_sequence_entries": [],
            "default_collection_id": null,
            "collection_count": 1,
            "last_modified": "2016-02-18T13:44:07.227Z",
            "full_condition_evaluation": false,
            "evaluation_enabled": true,
            "excluded_document_condition_id": null,
            "fingerprint": null
          }
        }
      }
    },
    {
      "type": "sequence_workflow_entry",
      "id": null,
      "additional": {
        "collection_sequence_id": 3625,
        "order": 300,
        "collection_sequence": {
          "type": "collection_sequence",
          "id": 3625,
          "name": "CollectionSequence_C",
          "description": "My description",
          "additional": {
            "collection_sequence_entries": [],
            "default_collection_id": null,
            "collection_count": 1,
            "last_modified": "2016-02-18T13:44:07.227Z",
            "full_condition_evaluation": false,
            "evaluation_enabled": true,
            "excluded_document_condition_id": null,
            "fingerprint": null
          }
        }
      }
    }
  ],
  "totalhits": 3
}
</code></pre>
