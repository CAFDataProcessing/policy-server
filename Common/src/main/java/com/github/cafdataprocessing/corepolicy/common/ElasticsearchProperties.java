/*
 * Copyright 2015-2018 Micro Focus or one of its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.cafdataprocessing.corepolicy.common;

import org.joda.time.Period;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

/**
 * Properties relating to configuration of Elasticsearch in Policy.
 */
@Configuration
@PropertySource("classpath:elasticsearch.properties")
@PropertySource(value = "file:${CAF_COREPOLICY_CONFIG}/elasticsearch.properties", ignoreResourceNotFound = true)
public class ElasticsearchProperties {
    @Autowired
    private Environment environment;

    public boolean isElasticsearchDisabled(){
        String disableString = environment.getProperty("POLICY_ELASTICSEARCH_DISABLED");
        return Boolean.parseBoolean(disableString);
    }

    public String getElasticsearchHost() {
        return environment.getProperty("POLICY_ELASTICSEARCH_HOST");
    }

    public Integer getElasticsearchPort() { return Integer.parseInt(environment.getProperty("POLICY_ELASTICSEARCH_PORT")); }

    /**
     * This property should match the cluster.name value specified in elasticsearch-x.x.x/config/elasticsearch.yml
     */
    public String getElasticsearchClusterName() {
        return environment.getProperty("POLICY_ELASTICSEARCH_CLUSTER_NAME");
    }

    public String getElasticsearchPolicyIndexName() {
        return environment.getProperty("POLICY_ELASTICSEARCH_POLICY_INDEX_NAME");
    }

    public String getElasticsearchTransportPingTimeout() {
        Period timeout = Period.parse(environment.getProperty("POLICY_ELASTICSEARCH_TRANSPORT_PING_TIMEOUT"));
        return new StringBuilder(String.valueOf(Math.max(1000, timeout.toStandardDuration().getMillis()) / 1000))
                .append("s").toString();
    }

    public String getElasticsearchMasterNodeTimeout() {
        Period timeout = Period.parse(environment.getProperty("POLICY_ELASTICSEARCH_MASTER_NODE_TIMEOUT"));
        return new StringBuilder(String.valueOf(Math.max(1000, timeout.toStandardDuration().getMillis()) / 1000))
                .append("s").toString();
    }

    public Integer getElasticsearchIndexStatusTimeout() {
        Period timeout = Period.parse(environment.getProperty("POLICY_ELASTICSEARCH_INDEX_STATUS_TIMEOUT"));
        return (int)(Math.max(1000, timeout.toStandardDuration().getMillis()) / 1000);
    }

    public String getElasticsearchSearchTimeout() {
        Period timeout = Period.parse(environment.getProperty("POLICY_ELASTICSEARCH_SEARCH_TIMEOUT"));
        return new StringBuilder(String.valueOf(Math.max(1000, timeout.toStandardDuration().getMillis()) / 1000))
                .append("s").toString();
    }

    public Integer getElasticsearchMaxStoredqueries() {
        return Integer.parseInt(environment.getProperty("POLICY_ELASTICSEARCH_MAX_STORED_QUERIES"));
    }

    public Integer getElasticsearchMaxStoredqueryResults() {
        return Integer.parseInt(environment.getProperty("POLICY_ELASTICSEARCH_MAX_STORED_QUERY_RESULTS"));
    }

    public Integer getElasticsearchMaxIndexAvailabilityAttempts() {
        return Integer.parseInt(environment.getProperty("POLICY_ELASTICSEARCH_MAX_INDEX_AVAILABILITY_ATTEMPTS"));
    }

    public Period getElasticsearchIndexAvailabilityDelay() {
        return Period.parse(environment.getProperty("POLICY_ELASTICSEARCH_INDEX_AVAILABILITY_DELAY"));
    }

    public Period getAgentExpiry(){
        return Period.parse(environment.getProperty("POLICY_ELASTICSEARCH_AGENT_EXPIRY"));
    }
}
