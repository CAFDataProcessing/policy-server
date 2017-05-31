## Policy Administration Container Configuration

The following configuration settings are used to specify the configuration of the Policy Classification and Workflow Administration Containers. They are specified as environment variables. The majority can be left with their default values with the exception of Hibernate Properties.

### Required Configuration Properties
The Policy Administration containers can only run in Direct mode. The required properties are:

*  API properties:
	*  api.mode
	*  api.direct.repository
*  Hibernate properties
*  Elasticsearch properties

**API Properties**  

* api.mode (direct) - configures the mode of the API and how it accesses the database. This container can only run in direct mode.
* api.direct.repository - specifies the type of repository to be used in direct mode. The default uses Hibernate.

**Hibernate Properties**  

* hibernate.connectionstring - details which database to connect to e.g. `jdbc:postgresql://localhost:5432/<dbname>?characterEncoding=UTF8&rewriteBatchedStatements=true"`  "dbname" will be substituted with the databasename property
* hibernate.databasename - specifies the name of the database 
* hibernate.user - username to use when connecting to the database
* hibernate.password - password to use when connecting to database

**Engine Properties**  
These properties configure the condition engine in both Workflow and Classification Administration Containers.  

* engine.regexcache.maxsize - the maximum number of entries to hold in the regular expressions cache e.g. 100000
* engine.regexcache.expiryhours - expiry time in hours for regular expression cache entries e.g. 24
* engine.environmentcache.maxsize - the maximum number of entries to hold in the environment cache e.g. 10000
* engine.environmentcache.expiry - expiry time period for environment cache entries. In ISO 8601 time period format. e.g PT1H. This setting replaces a previous setting engine.environmentcache.expiryhours (expiry time in hours, e.g. 24) which is now deprecated and is only used if engine.environmentcache.expiry is not specified. The default is 24 hours if neither is specified.
* engine.environmentcache.verifyperiod - The period of time to verify the cached environment against the environment in the database. In ISO 8601 time period format. e.g PT5M
* engine.environmentcache.location - directory to store environment cache, default is '.' (current directory)
* engine.environmentcache.mode - the caching mode to be used from 'fs' (stored on local filesystem) or default 'memory' (stored in memory)

**Elasticsearch Properties**

* POLICY_ELASTICSEARCH_DISABLED - Whether Elasticsearch should be used during condition evaluation. Defaults to false.
* POLICY_ELASTICSEARCH_AGENT_EXPIRY - The amount of time before agents expire.
* POLICY_ELASTICSEARCH_HOST - the host that Elasticsearch is located on
* POLICY_ELASTICSEARCH_PORT - the port number to use when sending actions to Elasticsearch
* POLICY_ELASTICSEARCH_CLUSTER_NAME - the name of the Elasticsearch cluster; this must match the cluster.name value specified in elasticsearch-x.x.x/config/elasticsearch.yml
* POLICY_ELASTICSEARCH_POLICY_INDEX_NAME - the name of the Elasticsearch index used by Policy
* POLICY_ELASTICSEARCH_TRANSPORT_PING_TIMEOUT - the time to wait for a ping response from an Elasticsearch node. In ISO 8601 time period format. e.g PT10S
* POLICY_ELASTICSEARCH_MASTER_NODE_TIMEOUT - the time to wait during discovery of the Elasticsearch master node. In ISO 8601 time period format. e.g PT10S
* POLICY_ELASTICSEARCH_INDEX_STATUS_TIMEOUT - the time to wait for the Elasticsearch cluster to reach a usable status. In ISO 8601 time period format. e.g PT2S
* POLICY_ELASTICSEARCH_SEARCH_TIMEOUT - the time to wait for elasticsearch queries to complete, returning with the hits accumulated up to that point when expired. In ISO 8601 time period format. e.g PT60S
* POLICY_ELASTICSEARCH_MAX_STORED_QUERIES - the maximum number of stored queries that will be requested from the Elasticsearch index
* POLICY_ELASTICSEARCH_MAX_STORED_QUERY_RESULTS - the maximum number of query results requested from stored queries in Elasticsearch
* POLICY_ELASTICSEARCH_MAX_INDEX_AVAILABILITY_ATTEMPTS - the maximum number of times that a newly created policy index in Elasticsearch will be checked to verify that it is available for use
* POLICY_ELASTICSEARCH_INDEX_AVAILABILITY_DELAY - the delay between attempts to verify that a newly created policy index in Elasticsearch is available for use. In ISO 8601 time period format. e.g PT1S.
* POLICY_ELASTICSEARCH_VERIFY_ATTEMPTS - the number of times to check if Elasticsearch has started during launch of the container. If Elasticsearch has not started within the specified number of retries then the container will exit.