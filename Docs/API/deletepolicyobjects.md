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
