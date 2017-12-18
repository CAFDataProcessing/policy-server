!not-ready-for-release!

#### Version Number
${version-number}

#### New Features
- Use latest release of policy-elasticsearch-container base image with updated copyright and logging.

- [CAF-3538](https://jira.autonomy.com/browse/CAF-3538): Add Document Worker, Composite Document Worker and Chained Action policy types to database base data.
  The Document Worker, Composite Document Worker and Chained Action policy types have been added to the base data of the database installer. This is is so that they are available by default in a core policy system for use with the chained workers enhancement. Previously a worker would have registered the Document and Composite Document types in the database but that worker is not present when using chaining. The new Chained Action type supports the same properties as Composite Document Worker and is intended to replace it for use in chaining of workers going forward (the older types are provided to support existing enrichment workflows using Document and Composite Document types).

#### Known Issues
