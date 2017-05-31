## Delete Classification Objects
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

The API allows collection sequences, collections, conditions, field labels, lexicons and lexicon expressions to be deleted.

Using this API you can delete classification objects. For example:

> /classification/delete/?type=collection\_sequence&id=1&id=2

The following classification object types can be deleted:

-   **collection**. A set of documents that share some common criteria.
-   **collection sequence**. An ordered set of Collections.
-   **condition**. Each Collection is identified using one or
    more Conditions.
-   **field label.** Information on how to retrieve a property value
    using one or more document fields.
-   **lexicon**. A list of terms, phrases or expressions that can be
    used when defining Conditions.
-   **lexicon expression**. An entry in a Lexicon.

A collection cannot be deleted if it is used in a collection sequence. A
lexicon cannot be deleted if it is used in a condition.

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
        <td> <b>type</b> </td>
        <td> enum &#60;type&#62; </td>
        <td> The type of classification object.</td>
    </tr>
    <tr>
        <td> <b>id</b> </td>
        <td> array </td>
        <td> The ids of the object or objects to delete.</td>
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

**Delete Classification Response {**  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; **result** (array[Result])    
**}**

**Result {**  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; <b>id</b> (number)  	
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; <b>success</b> (boolean)  	
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; <b>error_message</b> ( string or null , *optional*)  
**}**

#####Model Schema 
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