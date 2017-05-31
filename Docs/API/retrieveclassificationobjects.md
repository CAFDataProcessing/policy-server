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
            "modified_timestamp": "2014-12-08T17:54:40.000Z",
            "evaluation_enabled": true
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

#####Model Schema 
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
                        },
                        "evaluation_enabled": {
                            "type": [
                                "boolean"
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

##Filtering 

You can filter the Classification objects returned from the request by adding the `filter` property to the request. The `filter` is a JSON object that specifies the property name you wish to filter by and the value to use. For certain classification objects you can also filter using properties on their child objects using the following syntax  `child_object.propertyname`

For example:

<pre><code> "filter":{"collection_sequence_entries.collection_ids:[20]"} </code></pre>
Can be used to filter Collection Sequences by the Id of one of their Collections.

You can specify multiple filter conditions that will be joined using an AND operation, such as:

<pre><code>"filter": {
  "collection_sequence_entries.collection_ids:[20]",
  "evaluation_enabled": true
}
</code></pre>

Currently the following properties can be filtered on with **Collection Sequence** objects:

*	id
*	name
*	description
*	evaluation_enabled
*	collection\_sequence\_entries.collection_ids

Currently the following properties can be filtered on with **Collection** objects:

*	id
*	policy_ids

Currently the following properties can be filtered on with **Condition** objects:

*	id
*	type
*	is_fragment
*	notes
*	value (Only with Lexicon Conditions)


Currently the following properties can be filtered on with **Lexicon**, and **Lexicon Expression** objects:

*	id

A full example request that retrieves Collection Sequences based on a Collection ID would be: 

> /corepolicy/classification/retrieve?type=collection\_sequence&max\_page\_results=10&start=1&additional={"filter":{"collection\_sequence\_entries.collection\_ids":[398]}}&project_id=1

This returns the following JSON format:

<pre><code>
{
  "results": [
    {
      "type": "collection_sequence",
      "id": 302,
      "name": "aSequence_",
      "description": "Used in testCollectionSequencePagedByFilter_DocumentCollection",
      "additional": {
        "collection_sequence_entries": [
          {
            "collection_ids": [
              398
            ],
            "order": 100,
            "stop_on_match": false
          }
        ],
        "default_collection_id": null,
        "collection_count": 1,
        "last_modified": "2016-03-02T10:30:50.597Z",
        "full_condition_evaluation": false,
        "evaluation_enabled": true,
        "excluded_document_condition_id": null,
        "fingerprint": null
      }
    },
    {
      "type": "collection_sequence",
      "id": 301,
      "name": "bSequence_2baa8f5c-88f3-4bbe-b969-507b4de4e2af",
      "description": "Used in testCollectionSequencePagedByFilter_DocumentCollection ",
      "additional": {
        "collection_sequence_entries": [
          {
            "collection_ids": [
              398
            ],
            "order": 100,
            "stop_on_match": false
          }
        ],
        "default_collection_id": null,
        "collection_count": 2,
        "last_modified": "2016-03-02T10:30:50.597Z",
        "full_condition_evaluation": false,
        "evaluation_enabled": true,
        "excluded_document_condition_id": null,
        "fingerprint": null
      }
    }
  ],
  "totalhits": 2
}
</code></pre>

##Sorting

You can override the default ordering of the retrieved items by passing the **sort** property into the request. Similar to the **filter** property, the sort specifies the field to sort on and a boolean
specifying ascending (true) or descending (false). Sorting can be used in combination with filtering.

For Example:

    "sort":{"name":true}
    
Will sort the objects by name ascending.

Currently, the following properties can be sorted on with the **Collection Sequence** object:

-  name
-  description

Currently, the following properties can be sorted on with the **Condition** object:

-	notes

   
A full example request that retrieves collection sequences and sorts the results by name descending would be:

> /classification/retrieve?type=collection\_sequence&max\_page\_results=10&start=1&additional={"sort":{"name":false}}

This returns the following JSON format:

<pre><code>
{
  "results": [
    {
      "type": "collection_sequence",
      "id": 4382,
      "name": "cSequence_",
      "description": "Used in testCollectionSequencePagedByFilter_DocumentCollection",
      "additional": {
        "collection_sequence_entries": [],
        "default_collection_id": null,
        "collection_count": 1,
        "last_modified": "2016-02-22T09:36:44.632Z",
        "full_condition_evaluation": false,
        "evaluation_enabled": true,
        "excluded_document_condition_id": null,
        "fingerprint": null
      }
    },
    {
      "type": "collection_sequence",
      "id": 4380,
      "name": "bSequence_",
      "description": "Used in testCollectionSequencePagedByFilter_DocumentCollection ",
      "additional": {
        "collection_sequence_entries": [],
        "default_collection_id": null,
        "collection_count": 2,
        "last_modified": "2016-02-22T09:36:44.632Z",
        "full_condition_evaluation": false,
        "evaluation_enabled": true,
        "excluded_document_condition_id": null,
        "fingerprint": null
      }
    },
    {
      "type": "collection_sequence",
      "id": 4381,
      "name": "aSequence_",
      "description": "Used in testCollectionSequencePagedByFilter_DocumentCollection",
      "additional": {
        "collection_sequence_entries": [],
        "default_collection_id": null,
        "collection_count": 1,
        "last_modified": "2016-02-22T09:36:44.632Z",
        "full_condition_evaluation": false,
        "evaluation_enabled": true,
        "excluded_document_condition_id": null,
        "fingerprint": null
      }
    }
  ],
  "totalhits": 3
}
</code></pre>

It is also possible to combine a Filter and Sort into one request. 

An example condition retrieve request that filters by if it's a fragment and sorts by the notes field would be: 

>/corepolicy/classification/retrieve?type=condition&max_page_results=3&start=1&additional={"filter":{"is\_fragment":false},"sort":{"notes":false}}&project_id=1

This returns the following JSON format:

<pre><code>{
  "results": [
	{
      "type": "condition",
      "id": 3,
      "name": "FileType Exists with - ImportMagicExtension",
      "additional": {
        "type": "exists",
        "is_fragment": false,
        "order": 100,
        "notes": "Note_A",
        "include_descendants": false,
        "parent_condition_id": null,
        "target": "container",
        "field": "ImportMagicExtension"
      }
    },
	{
      "type": "condition",
      "id": 1,
      "name": "FileType Exists with - ImportMagicExtension",
      "additional": {
        "type": "exists",
        "is_fragment": false,
        "order": 100,
        "notes": "Note_B",
        "include_descendants": false,
        "parent_condition_id": null,
        "target": "container",
        "field": "ImportMagicExtension"
      }
    },
    {
      "type": "condition",
      "id": 2,
      "name": "FileType Exists with - ImportMagicExtension",
      "additional": {
        "type": "exists",
        "is_fragment": false,
        "order": 100,
        "notes": "Note_C",
        "include_descendants": false,
        "parent_condition_id": null,
        "target": "container",
        "field": "ImportMagicExtension"
      }
    }
  ],
  "totalhits": 14
}
</code></pre>