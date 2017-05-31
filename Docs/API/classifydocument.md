# Classify Document
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

##Response
This API returns a JSON response that is described by the model below. This single model is presented both as an easy to read abstract 
definition and as the formal JSON schema.

#####Model

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


#####Model Schema
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

