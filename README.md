# Policy Administration Service

Policy Administration Service should be used to administer Workflows, Classifications and Policies. 
This service can also be used to Classify documents into different collections and to understand which conditions caused the classification.

A Docker container is included in this project that packages the Core Policy Web Service. The container relies on an external database being available with the Policy Database schema already installed. Default supported database types are postgres and h2.

## Documentation

The Policy API is documented [here](./Docs/API.md).

Configuration details for the container can be found [here](./Docs/Container Configuration.md).