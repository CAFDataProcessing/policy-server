#Workflow API
The Workflow API provide the user a way to conditionally control the flow of their documents to various CAF Workers.

This document shows how requests can be made both through the web service and programmatically. 

##Update Workflow

To update a Workflow object you must specify the **id** of the sequenceWorkflow you wish to update and supply additional parameters in **additional**.   
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

##Response
This API returns a JSON response that is described by the model below. This single model is presented both as an easy to read 
abstract definition and as the formal JSON schema.

#####Model
This is an abstract definition of the response that describes each of the properties that might be returned.

**Create Sequence Workflow Response {**  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; **id** (number, *optional*) &nbsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp; Id of the workflow object.  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; **name** ( string, *optional*) &emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp; Name of the workflow object.  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; **description** ( string or null , *optional*) &nbsp;&nbsp;&nbsp;Description of the workflow object.  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; **type** (enum &lt;Type&gt;) &nbsp;&nbsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;The type of the workflow object.  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; **additional** (object, *optional*) &nbsp;&emsp;&emsp;&emsp;&emsp;&emsp;JSON formatted additional information depending on the object type. See the schema for more details.  
**}**

**enum&lt;Type&gt; {**  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;'sequence\_workflow', 'sequence\_workflow\_entry'  
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

## Programmatically
Programmatically you would call:
> WorkflowApi.update(SequenceWorkflow updatedWorkflow).  

This returns the updated SequenceWorkflow Object.  