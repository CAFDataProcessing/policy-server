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
                },
                "short_name": "index"
            }
        }
    }


## Request

#### Authentication
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
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; **type** (enum &lt;Type&gt; ) &nbsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;The type of the policy object.  
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

## Filtering 

You can filter the Policy objects returned from the request by adding the `filter` property to the request. The `filter` is a JSON object that specifies the property name you wish to filter by and the value to use. 

For example:

<pre><code> "filter":{"policy_type_id":27} </code></pre>

You can specify multiple filter conditions that will be joined using an AND operation, such as:

<pre><code>"filter": {
  "policy_type_id:20",
  "id": 1
}
</code></pre>

Currently the following properties can be filtered on with **Policy** objects:

*	id
*	policy\_type\_id

Currently the following properties can be filtered on with **PolicyType** objects:

*	id

A full example request that retrieves policy's filtered by their policy type id would be:
>corepolicy/policy/retrieve?type=policy&max\_page\_results=3&start=1&additional={"filter":{"policy\_type\_id":71}}&project_id=1

This returns the following JSON format:

<pre><code>
{
  "results": [
    {
      "type": "policy",
      "id": 207,
      "name": "Policy b",
      "description": "des",
      "additional": {
        "priority": 0,
        "details": {
          "title": "Test Metadata Policy",
          "description": "Example Metadata Policy Details for unit tests"
        },
        "policy_type_id": 71
      }
    },
    {
      "type": "policy",
      "id": 203,
      "name": "Policy c",
      "description": "des",
      "additional": {
        "priority": 0,
        "details": {
          "title": "Test Metadata Policy",
          "description": "Example Metadata Policy Details for unit tests"
        },
        "policy_type_id": 71
      }
    },
    {
      "type": "policy",
      "id": 206,
      "name": "Policy e",
      "description": "des",
      "additional": {
        "priority": 0,
        "details": {
          "title": "Test Metadata Policy",
          "description": "Example Metadata Policy Details for unit tests"
        },
        "policy_type_id": 71
      }
    }
  ],
  "totalhits": 10
}
</code></pre>
