!not-ready-for-release!

#### Version Number
${version-number}

#### New Features
- Use latest release of policy-elasticsearch-container base image with updated copyright and logging.

- [CAF-3538](https://jira.autonomy.com/browse/CAF-3538): Add Document Worker and Composite Document Worker policy types to database base data.
  The Document and Composite Document Worker policy types have been added to the base data of the database installer. This is is so that they are available by default in a core policy system for use with the chained workers enhancement (previously a worker would have registered these types in the database but that worker is not present when using chaining).

#### Known Issues
