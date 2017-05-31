#Workflow API

The Workflow API provide the user a way to conditionally control the flow of their documents to various CAF Workers.

This document shows how requests can be made both through the web service and programmatically. 

## Create Workflow 

The **type** of workflow object you are adding determines the
additional information that can be provided in the **additional**
parameter.

The following Workflow object type can be created:

-   **sequence\_workflow**. An ordered set of Collections Sequences.

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

The **sequence\_entries** array defines one or more entries to add to the sequence workflow.  
Each entry contains one **collection\_sequence\_id** and has an **order** attribute.

The **order** field identifies where the sequence workflow entry appears in the
workflow sequence. Any numeric value can be specified and entries are
ordered by their values, from lowest to highest.  If no order value is specified an incremental value is added to each item in the list automatically.

The **collection\_sequence\_id** field identifies the collections sequence that is being
added to the sequence workflow. 

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
        <td> <b>id</b> </td>
        <td> number </td>
        <td> The id of the workflow object to update.</td>
    </tr>
    <tr>
        <td> <b>name</b> </td>
        <td> String </td>
        <td> The name of the workflow object to add.</td>
    </tr>
    <tr>
        <td> description </td>
        <td> String </td>
        <td> An textual description for the workflow object.</td>
    </tr>
    <tr>
        <td> <b>type</b> </td>
        <td> enum &#60;type&#62; </td>
        <td> The type of workflow object.</td>
    </tr>
    <tr>
        <td> <b> additional </b> </td>
        <td> object </td>
        <td> Specify a JSON object of additional parameters relevant for the type of workflow object being added.</td>
    </tr>
</table>

*Required parameters are shown with names in bold.*

####Enumeration Types
This API's parameters use the enumerations described below:

<table>
    <tr>
        <td><b>type</b> <br/> The type of policy object.</td>
    </tr>
    <tr>
        <td> sequence_workflow </td>
        <td> <b>Sequence Workflow</b><br/> A workflow object of type sequence_workflow. </td>
    </tr>
    <tr>
        <td> sequence_workflow_entry </td>
        <td> <b>Sequence Workflow Entry</b><br/> A workflow object of type sequence_workflow_entry. </td>
    </tr>
</table>

## Programmatically 
Programmatically you would call: 
> WorkflowApi.create(SequenceWorkflow newWorkflow).  
  
This returns the created SequenceWorkflow.