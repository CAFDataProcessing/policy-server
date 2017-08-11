# Workflow API
The Workflow API provide the user a way to conditionally control the flow of their documents to various CAF Workers.

This document shows how requests can be made both through the web service and programmatically. 

## Retrieve Sequence Workflow


The following classification object types can be retrieved:

-   **sequence_workflow**. An ordered set of Collections Sequences.
-   **sequence\_workflow\_entry**. An entry in the sequence workflow set, which holds the Collections Sequence Id and order information.  
   
To retrieve a paged list of Sequence Workflow object you must specify the **id** of the SequenceWorkflow.
 
> /workflow/retrieve?type=sequence\_workflow&id=1329

<pre><code>
{
	"results": [ { 
		"type":"sequence_workflow",
		"id":1329,
		"name":"tryme",
		"description":"any description",
		"additional":{
			"sequence_entries":[],
			"notes":null
		}
	} ],
	"totalhits":1
}
</code></pre>

To retrieve a paged list of Sequence Workflow Entries you must specify the **id** of the SequenceWorkflow they belong to in **filter** object.  

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

## Filtering
You can filter the Policy objects returned from the request by adding the `filter` property to the request. The `filter` is a JSON object that specifies the property name you wish to filter by and the value to use. For certain Workflow objects you can also filter using properties on their child objects using the following syntax  `child_object.propertyname`

For example:

<pre><code> "filter":{"sequence_entries.collection_sequence_id":27} </code></pre>
 
Currently the following properties can be filtered on with **Sequence Workflow** objects:
 
*	sequence\_entries.collection\_sequence\_id

A full example request that retrieves Sequence Workflow Entries filtered by Sequence Workflow Id would be:

>/corepolicy/workflow/retrieve?type=sequence\_workflow\_entry&max\_page\_results=3&start=1&additional={"filter":{"sequence\_workflow\_id":51}}&project\_id=1

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
