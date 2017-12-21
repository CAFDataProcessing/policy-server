!not-ready-for-release!

#### Version Number
${version-number}

#### New Features
- Project now contains a new base image policy-elasticsearch-tomcat that includes Elasticsearch and Tomcat with updated copyright and multi-process logging. The Policy Administration Service docker container utilises this new base image.

- [CAF-3538](https://jira.autonomy.com/browse/CAF-3538): Add Chained Action policy type to database base data.  
  The Chained Action policy type has been added to the base data of the database installer. This is is so that it is available by default in a core policy system for use with the chained workers enhancement. Previously a policy worker would have been responsible for registering types in the database but that worker is not present when using chaining. The new Chained Action type supports the same properties as Composite Document Worker and is intended to replace it for use in chaining of workers going forward.

#### Known Issues
