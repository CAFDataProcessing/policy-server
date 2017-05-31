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

The **evaluation\_enabled** parameter can be set to false to disable the
collection sequence's document evaluation. The default is true.


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

A Boolean condition that only specifies the type 'boolean' and operator 'and' properties with no child conditions will always evaluate to a match. An example usage for this would be when you have a collection sequence containing some collections that you only want to match some items and other collections that you want to match all items.

<pre>
 {
    "type": "boolean",
    "operator": "and"
}
</pre>

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

##Request

####Authentication
This API requires an authentication token to be supplied in the following parameter:

<table>
    <tr>
        <td><b>Parameter</b></td>
        <td><b>Description</b></td>
    </tr>
    <tr>
        <td>project_id</td>
        <td>The Project Id to use to authenticate the API request</td>
    </tr>
</table>

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

####Enumeration Types
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

##Response
This API returns a JSON response that is described by the model below. This single model is presented both as an easy to read 
abstract definition and as the formal JSON schema.

#####Model
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

#####Model Schema 
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
                        },
                        "evaluation_enabled": {
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