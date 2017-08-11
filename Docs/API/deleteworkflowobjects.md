# Workflow API
The Workflow API provide the user a way to conditionally control the flow of their documents to various CAF Workers.

This document shows how requests can be made both through the web service and programmatically. 

## Delete Workflow

The following workflow object type can be deleted:  

- **sequence_workflow**. A workflow object is deleted.
 
To delete a Workflow object you must specify the **id** of the sequenceWorkflow you wish to delete.

The **id** parameter identifies the workflow object to be deleted. A
single id or array of ids can be specified.

For example:
>   /workflow/delete?type=sequence_workflow&id=62

## Programmatically 
Programmatically you would call:
> WorkflowApi.delete(Long id).


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
        <td> The type of workflow object to delete.</td>
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
        <td><b>type</b> <br/> The type of workflow object.</td>
    </tr>
    <tr>
        <td> sequence_workflow </td>
        <td> <b>Sequence Workflow</b><br/> A workflow object of type sequence_workflow. </td>
    </tr>
</table>

## Response
This API returns a JSON response that is described by the model below. This single model is presented both as an easy to read 
abstract definition and as the formal JSON schema.

##### Model
This is an abstract definition of the response that describes each of the properties that might be returned.

**Delete SequenceWorkflow Response {**  
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
