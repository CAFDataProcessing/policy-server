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
                                    "ADD_FIELD_VALUE",
                                    "SET_FIELD_VALUE"
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

## esponse
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
