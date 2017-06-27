/*
 * Copyright 2015-2017 Hewlett Packard Enterprise Development LP.
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
package com.github.cafdataprocessing.corepolicy.booleanagent;

import com.github.cafdataprocessing.corepolicy.domainModels.BooleanAgentDocument;
import com.github.cafdataprocessing.corepolicy.domainModels.BooleanAgentDocuments;
import com.google.common.base.Strings;
import com.github.cafdataprocessing.corepolicy.common.ElasticsearchProperties;
import com.github.cafdataprocessing.corepolicy.common.UserContext;
import com.github.cafdataprocessing.corepolicy.common.exceptions.BackEndRequestFailedCpeException;
import com.github.cafdataprocessing.corepolicy.common.exceptions.CpeException;
import com.github.cafdataprocessing.corepolicy.common.shared.MetadataValue;
import org.apache.lucene.index.*;
import org.apache.lucene.search.DocIdSetIterator;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.action.admin.indices.get.GetIndexResponse;
import org.elasticsearch.action.admin.indices.settings.get.GetSettingsResponse;
import org.elasticsearch.action.admin.indices.validate.query.QueryExplanation;
import org.elasticsearch.action.admin.indices.validate.query.ValidateQueryRequest;
import org.elasticsearch.action.admin.indices.validate.query.ValidateQueryResponse;
import org.elasticsearch.action.percolate.PercolateResponse;
import org.elasticsearch.action.percolate.PercolateSourceBuilder;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.action.termvectors.TermVectorsResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.cluster.health.ClusterHealthStatus;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.indices.IndexAlreadyExistsException;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.highlight.HighlightBuilder;
import org.elasticsearch.search.highlight.HighlightField;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

import static org.elasticsearch.common.xcontent.XContentFactory.*;
import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * An implementation of BooleanAgentServices that uses Elasticsearch.
 */
@Primary
@Component
public final class BooleanAgentServicesElasticImpl extends BooleanAgentServicesBaseImpl implements BooleanAgentServices, DisposableBean {
    private static final String dbTypeName = "Activated";
    private static final String percolatorTypeName = ".percolator";
    private static final String booleanRestrictionFieldName = "BOOLEANRESTRICTION";
    private static final String contentFieldName = "DRECONTENT";
    private static final String referenceFieldName = "DREREFERENCE";
    private static final String dateFieldName = "DREDATE";
    private static final String dbFieldName = "DREDBNAME";
    private static final String projectIdFieldName = "project_id";
    private static final String instanceIdFieldName = "instance_id";
    private static final String conditionIdFieldName = "condition_id";
    private static final String lexiconIdFieldName = "lexicon_id";
    private static final String lexiconExpressionIdFieldName = "lexicon_expression_id";

    private static Logger logger = LoggerFactory.getLogger(BooleanAgentServicesElasticImpl.class);

    private BooleanExpressionParser parser;
    private ElasticsearchProperties elasticsearchProperties;
    private UserContext userContext;
    private String policyIndexName;
    private Client elasticClient;
    private boolean initialized = false;

    @Autowired
    public BooleanAgentServicesElasticImpl(ElasticsearchProperties elasticsearchProperties, UserContext userContext) {
        this.elasticsearchProperties = elasticsearchProperties;
        this.userContext = userContext;
        this.policyIndexName = elasticsearchProperties.getElasticsearchPolicyIndexName();
        this.parser = new BooleanExpressionParser(contentFieldName);
    }

    @Override
    public boolean getAvailable() {
        return !elasticsearchProperties.isElasticsearchDisabled();
    }

    @Override
    public BooleanAgentQueryResult query(String instanceId, Collection<MetadataValue> fieldValues) throws Exception {
        BooleanAgentQueryResult booleanAgentQueryResult = new BooleanAgentQueryResultImpl();
        if (!fieldValues.isEmpty()) {
            try {
                initialize();
                MetadataValue.getStringValues(fieldValues).stream()
                        .filter(t -> !Strings.isNullOrEmpty(t))
                        .forEach(t -> {
                            try {
                                queryBooleanAgents(instanceId, t, booleanAgentQueryResult);
                            } catch (IOException ioe) {
                                throw new UncheckedIOException(ioe);
                            }
                        });
            } catch (UncheckedIOException | IOException e) {
                throw new BackEndRequestFailedCpeException(e.getCause());
            }
        }
        return booleanAgentQueryResult;
    }

    @Override
    public void create(String instanceId, BooleanAgentDocuments documents) throws CpeException {
        if (documents == null || documents.getDocuments() == null || documents.getDocuments().isEmpty()) {
            return;
        }
        try {
            initialize();
            for (BooleanAgentDocument booleanAgentDocument : documents.getDocuments()) {
                Optional<String> booleanRestriction = booleanAgentDocument.getBooleanRestriction().stream().findFirst();
                createStoredQuery(instanceId, getTtl(), booleanAgentDocument, booleanRestriction);
            }
        } catch (IOException e) {
            throw new BackEndRequestFailedCpeException(e);
        }
    }

    @Override
    public void delete(String instanceId) {
        try {
            String storedQueryId = getStoredQueryId(instanceId);
            if (storedQueryId != null) {
                getElasticClient()
                        .prepareDelete(policyIndexName, percolatorTypeName, storedQueryId)
                        .get(elasticsearchProperties.getElasticsearchSearchTimeout());
            }
            // Note: Elasticsearch is near-realtime and there can be a default one-second delay between issuing a
            //       deletion request and the deletion being apparent in index queries. This delay has been allowed
            //       for in integration tests for Elasticsearch deletion rather than here in the delete implementation
            //       itself, as the delete method may be called multiple times.
        } catch (IOException e) {
            throw new BackEndRequestFailedCpeException(e);
        }
    }

    @Override
    public boolean existForInstanceId(String instanceId) {
        return getStoredQueryId(instanceId) != null;
    }

    private String getStoredQueryId(String instanceId) {
        try {
            initialize();
            QueryBuilder agentQueryBuilder = matchQuery(instanceIdFieldName, instanceId);
            SearchHits agentHits = getElasticClient()
                    .prepareSearch(policyIndexName)
                    .setTypes(percolatorTypeName)
                    .setQuery(agentQueryBuilder)
                    .setSize(1)
                    .get(elasticsearchProperties.getElasticsearchSearchTimeout())
                    .getHits();
            if (agentHits.getTotalHits() == 0) {
                return null;
            }
            return agentHits.getAt(0).getId();
        } catch (IOException e) {
            throw new BackEndRequestFailedCpeException(e);
        }
    }

    @Override
    public void isValidExpression(String string) {
        try {
            XContentBuilder query = BooleanExpressionParser.wrapQuery(parseQuery(string));
            if (query != null) {
                initialize();

                ValidateQueryRequest validationRequest = new ValidateQueryRequest(policyIndexName)
                        //TODO source(QuerySourceBuilder), source(Map), source(XContentBuilder), source(String),
                        // source(byte[]), source(byte[], int, int), source(BytesReference) and source()
                        // have been removed in favor of using query(QueryBuilder) and query()
                        //.source(query);
                    .query(query);
                validationRequest.explain(true);

                ValidateQueryResponse validationResponse = getElasticClient()
                        .admin()
                        .indices()
                        .validateQuery(validationRequest)
                        .actionGet(elasticsearchProperties.getElasticsearchSearchTimeout());

                if (!validationResponse.isValid()) {
                    throw new RuntimeException(validationResponse
                            .getQueryExplanation()
                            .stream()
                            .map(QueryExplanation::getError)
                            .collect(Collectors.joining(System.getProperty("line.separator"))));
                }
            }
        } catch (IOException e) {
            throw new BackEndRequestFailedCpeException(e);
        }
    }

    @Override
    public Collection<Term> doTermGetInfo(String text) {
        try {
            initialize();
            return getTerms(getTermVectors(text));
        } catch (IOException e) {
            throw new BackEndRequestFailedCpeException(e);
        }
    }

    private TermVectorsResponse getTermVectors(String text) throws IOException {
        return getElasticClient()
                .prepareTermVectors()
                .setIndex(policyIndexName)
                .setType(percolatorTypeName)
                .setDfs(true)
                .setTermStatistics(true)
                .setFieldStatistics(false)
                .setPositions(true)
                .setOffsets(true)
                .setPayloads(false)
                .setDoc(jsonBuilder()
                        .startObject()
                            .field(booleanRestrictionFieldName, text)
                        .endObject())
                .get(elasticsearchProperties.getElasticsearchSearchTimeout());
    }

    private Collection<Term> getTerms(TermVectorsResponse termVectorsResponse) throws IOException {
        List<Term> termInfos = new ArrayList<>();
        Fields fields = termVectorsResponse.getFields();
        if (fields != null) {
            for (String field : fields) {
                Terms terms = fields.terms(field);
                if (terms != null) {
                    TermsEnum termsIter = terms.iterator();
                    while (termsIter.next() != null) {
                        termInfos.add(getTerm(termsIter));
                    }
                }
            }
        }
        return termInfos;
    }

    private Term getTerm(TermsEnum termsIter) throws IOException {
        Term termResult = new Term();
        termResult.setTermString(termsIter.term() == null ? null : termsIter.term().utf8ToString());
        termResult.setDocumentOccurrences(termsIter.docFreq());
        termResult.setTotalOccurrences((int) termsIter.totalTermFreq());
        setOffsets(termResult, termsIter);
        termResult.setApcmWeight(0);
        termResult.setTermCase(0);
        return termResult;
    }

    private void setOffsets(Term termResult, TermsEnum termsIter) throws IOException {
        PostingsEnum docsAndPositionsIter = termsIter.postings(null, PostingsEnum.POSITIONS);
        if (docsAndPositionsIter != null &&
                docsAndPositionsIter.nextDoc() != DocIdSetIterator.NO_MORE_DOCS &&
                docsAndPositionsIter.freq() > 0 &&
                docsAndPositionsIter.nextPosition() != -1 &&
                docsAndPositionsIter.startOffset() != -1) {
            termResult.setStartPosition(docsAndPositionsIter.startOffset());
            if (docsAndPositionsIter.endOffset() != -1) {
                termResult.setLength(docsAndPositionsIter.endOffset() - docsAndPositionsIter.startOffset());
            }
        }
    }

    @Override
    public boolean canConnect() {
        try {
            initialize();
            return isIndexAvailable();
        } catch (Exception e) {
            logger.warn("Failed to verify an ability to connect to index \"{}\" in cluster \"{}\" in Elasticsearch at {}:{} due to error: ",
                    policyIndexName,
                    elasticsearchProperties.getElasticsearchClusterName(),
                    elasticsearchProperties.getElasticsearchHost(),
                    elasticsearchProperties.getElasticsearchPort(),
                    e);
        }
        return false;
    }

    private boolean isIndexAvailable() throws UnknownHostException {
        return canGetSettings() && isClusterHealthy();
    }

    private void initialize() throws IOException {
        if (!initialized) {
            ensureIndex();
            initialized = true;
        }
    }

    private boolean canGetSettings() throws UnknownHostException {
        GetSettingsResponse indicesResponse = getElasticClient()
                .admin()
                .indices()
                .prepareGetSettings(policyIndexName)
                .setMasterNodeTimeout(elasticsearchProperties.getElasticsearchMasterNodeTimeout())
                .get();
        if (indicesResponse.getIndexToSettings().isEmpty()) {
            logger.warn("Failed to retrieve settings for the configured Elasticsearch index {}", policyIndexName);
            return false;
        }
        return true;
    }

    private boolean isClusterHealthy() throws UnknownHostException {
        ClusterHealthResponse healthResponse = getElasticClient()
                .admin()
                .cluster()
                .prepareHealth(policyIndexName)
                .setWaitForYellowStatus()
                .setTimeout(TimeValue.timeValueSeconds(elasticsearchProperties.getElasticsearchIndexStatusTimeout()))
                .get();
        if (healthResponse.isTimedOut()) {
            logger.warn("Timed out while awaiting at least YELLOW status for Elasticsearch index {} ", policyIndexName);
            return false;
        }
        if (!healthResponse.getStatus().equals(ClusterHealthStatus.YELLOW) && !healthResponse.getStatus().equals(ClusterHealthStatus.GREEN)) {
            logger.warn("The Elasticsearch index {} is reporting status {}", policyIndexName, healthResponse.getStatus().name());
            return false;
        }
        return true;
    }

    private void ensureIndex() throws IOException {
        ensureIndex(true);
    }

    private void ensureIndex(boolean createIfAbsent) throws IOException {
        GetIndexResponse indexResponse = getElasticClient()
                .admin()
                .indices()
                .prepareGetIndex()
                .get(elasticsearchProperties.getElasticsearchSearchTimeout());

        if (Arrays.asList(indexResponse.getIndices()).stream().anyMatch(i -> i.equalsIgnoreCase(policyIndexName))) {
            ensureIndexAvailable();
            return;
        }

        if (!createIfAbsent) {
            String error = MessageFormat.format("Failed to ensure that the index \"{0}\" exists in Elasticsearch", policyIndexName);
            logger.error(error);
            throw new RuntimeException(error);
        }

        logger.warn("Index \"{}\" does not exist in Elasticsearch. Creating index...", policyIndexName);
        try {
            createIndex();
        } catch (IndexAlreadyExistsException e) {
            logger.warn("Index \"{}\" was found to exist during creation attempt.", policyIndexName);
        }
        ensureIndex(false);
    }

    private void ensureIndexAvailable() throws UnknownHostException {
        int availabilityAttempt = 0;
        try {
            while ((++availabilityAttempt <= elasticsearchProperties.getElasticsearchMaxIndexAvailabilityAttempts()) && !isIndexAvailable()) {
                Thread.sleep(elasticsearchProperties.getElasticsearchIndexAvailabilityDelay().toStandardDuration().getMillis());
            }
        } catch (InterruptedException eInterrupted) {
            logger.warn("Interrupted while waiting to test the availability of the index \"{}\".", policyIndexName);
        }
    }

    private void createIndex() throws IOException {
        getElasticClient()
                .admin()
                .indices()
                .prepareCreate(policyIndexName)
                .addMapping(dbTypeName, jsonBuilder()
                        .startObject()
                            .startObject(dbTypeName)
                                .startObject("properties")
                                    .startObject(contentFieldName)
                                        .field("type", "string")
                                        .field("analyzer", "icu_analyzer")
                                    .endObject()
                                .endObject()
                                .startObject("_ttl")
                                    .field("enabled", "true")
                                .endObject()
                            .endObject()
                        .endObject())
                .addMapping(percolatorTypeName, jsonBuilder()
                        .startObject()
                            .startObject(percolatorTypeName)
                                .startObject("_ttl")
                                    .field("enabled", "true")
                                .endObject()
                                .startObject("properties")
                                    .startObject(booleanRestrictionFieldName)
                                        .field("type", "string")
                                        .field("analyzer", "icu_analyzer")
                                    .endObject()
                                .endObject()
                            .endObject()
                        .endObject())
                .setSettings(jsonBuilder()
                        .startObject()
                            .startObject("analysis")
                                .startObject("analyzer")
                                    .startObject("icu_analyzer")
                                        .array("char_filter", "icu_normalizer")
                                        .field("tokenizer", "icu_tokenizer")
                                    .endObject()
                                .endObject()
                            .endObject()
                        .endObject())
                .get(elasticsearchProperties.getElasticsearchSearchTimeout());
    }

    private Client getElasticClient() throws UnknownHostException {
        if (this.elasticClient == null) {
            Settings settings = Settings.builder()
                    .put("cluster.name", elasticsearchProperties.getElasticsearchClusterName())
                    .put("client.transport.ping_timeout", elasticsearchProperties.getElasticsearchTransportPingTimeout())
                    .build();

            this.elasticClient = new PreBuiltTransportClient(settings)
                    .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(elasticsearchProperties.getElasticsearchHost()), elasticsearchProperties.getElasticsearchPort()));
        }

        return this.elasticClient;
    }

    private String getTtl() {
        DateTime now = DateTime.now();
        DateTime expireTime = now.plus(elasticsearchProperties.getAgentExpiry());
        return String.valueOf((expireTime.getMillis() - now.getMillis()) / 1000) + "s";
    }

    private void createStoredQuery(String instanceId, String ttl, BooleanAgentDocument booleanAgentDocument, Optional<String> booleanRestriction) throws IOException {
        String reference = booleanAgentDocument.getReference() + "_" + instanceId;

        XContentBuilder storedQuery = prepareStoredQuery(instanceId, booleanAgentDocument, booleanRestriction, reference);

        // replaces an existing document with a new document by using the reference value as the Elasticsearch _id.
        getElasticClient()
                .prepareIndex(policyIndexName, percolatorTypeName, reference)
                .setSource(storedQuery)
                .setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE)
                .setTTL(ttl)
                .get(elasticsearchProperties.getElasticsearchSearchTimeout());
    }

    private XContentBuilder prepareStoredQuery(String instanceId, BooleanAgentDocument booleanAgentDocument, Optional<String> booleanRestriction, String queryReference) throws IOException {
        XContentBuilder storedQueryBuilder = jsonBuilder()
                .startObject()
                .field(referenceFieldName, queryReference)
                .field(dateFieldName, new Date().getTime()/1000)
                .field(dbFieldName, dbTypeName)
                .field(projectIdFieldName, userContext.getProjectId() + "_" + instanceId)
                .field(instanceIdFieldName, instanceId);

        if (booleanRestriction.isPresent()) {
            storedQueryBuilder = storedQueryBuilder.field(booleanRestrictionFieldName, booleanRestriction.get());
            QueryBuilder query = parseQuery(booleanRestriction.get());
            storedQueryBuilder = storedQueryBuilder.field("query", query);
        }
        if (booleanAgentDocument.getCondition_id() != null && !booleanAgentDocument.getCondition_id().isEmpty()) {
            Optional<String> conditionId = booleanAgentDocument.getCondition_id().stream().findFirst();
            if (conditionId.isPresent()) {
                storedQueryBuilder = storedQueryBuilder.field(conditionIdFieldName, conditionId.get());
            }
        }
        if (booleanAgentDocument.getLexicon_id() != null && !booleanAgentDocument.getLexicon_id().isEmpty()) {
            Optional<String> lexiconId = booleanAgentDocument.getLexicon_id().stream().findFirst();
            if (lexiconId.isPresent()) {
                storedQueryBuilder = storedQueryBuilder.field(lexiconIdFieldName, lexiconId.get());
            }
        }
        if (booleanAgentDocument.getLexicon_expression_id() != null && !booleanAgentDocument.getLexicon_expression_id().isEmpty()) {
            Optional<String> lexiconExpressionId = booleanAgentDocument.getLexicon_expression_id().stream().findFirst();
            if (lexiconExpressionId.isPresent()) {
                storedQueryBuilder = storedQueryBuilder.field(lexiconExpressionIdFieldName, lexiconExpressionId.get());
            }
        }

        return storedQueryBuilder.endObject();
    }

    private QueryBuilder parseQuery(String booleanRestriction) throws IOException {
        return parser.parse(booleanRestriction);
    }

    private void queryBooleanAgents(String instanceId, String textToQuery, BooleanAgentQueryResult booleanAgentQueryResult) throws IOException {
        PercolateResponse response = getElasticClient()
                .preparePercolate()
                .setIndices(policyIndexName)
                .setDocumentType(dbTypeName)
                .setPercolateDoc(PercolateSourceBuilder.docBuilder()
                        .setDoc(XContentFactory.jsonBuilder()
                                .startObject()
                                .field(contentFieldName, textToQuery)
                                .endObject()))
                .setPercolateQuery(QueryBuilders.boolQuery()
                        .filter(QueryBuilders.boolQuery()
                                .must(matchQuery(projectIdFieldName, userContext.getProjectId() + "_" + instanceId))
                                .must(matchQuery(instanceIdFieldName, instanceId))))
                .setHighlightBuilder(new HighlightBuilder()
                        .field(contentFieldName)
                        .preTags(getStartTagGuid())
                        .postTags(getEndTagGuid()))
                .setSize(elasticsearchProperties.getElasticsearchMaxStoredqueryResults())
                .get(elasticsearchProperties.getElasticsearchSearchTimeout());

        List<BooleanAgentDocument> booleanAgentDocuments = convertToBooleanAgentDocuments(response);
        booleanAgentDocuments.forEach(d -> extractTermsFromBooleanAgentDocument(textToQuery, booleanAgentQueryResult, d));
    }

    private List<BooleanAgentDocument> convertToBooleanAgentDocuments(PercolateResponse response) throws UnknownHostException {
        Collection<AgentResult> agents = getAgents(response);
        return agents
                .stream()
                .map(this::convertToBooleanAgentDocument)
                .collect(Collectors.toList());
    }

    private Collection<AgentResult> getAgents(PercolateResponse response) throws UnknownHostException {
        Map<String, AgentResult> indexedAgents = new HashMap<>();

        BoolQueryBuilder agentsQueryBuilder = QueryBuilders.boolQuery();
        for (PercolateResponse.Match match : response) {
            String agentId = match.getId().string();
            agentsQueryBuilder = agentsQueryBuilder.should(termQuery("_id", agentId));
            indexedAgents.put(agentId, new AgentResult(agentId, match, null));
        }

        SearchHits agentHits = getElasticClient()
                .prepareSearch(policyIndexName)
                .setTypes(percolatorTypeName)
                .setQuery(agentsQueryBuilder)
                .setSize(elasticsearchProperties.getElasticsearchMaxStoredqueries())
                .get(elasticsearchProperties.getElasticsearchSearchTimeout())
                .getHits();

        for (SearchHit agentHit : agentHits) {
            AgentResult agentResult = indexedAgents.get(agentHit.getId());
            if (agentResult != null) {
                agentResult.setSource(agentHit.sourceAsMap());
            }
        }

        if (indexedAgents.values().stream().anyMatch(r -> r.getSource() == null)) {
            String error = "Failed to retrieve all the expected boolean agents from Elasticsearch";
            logger.error(error);
            throw new RuntimeException(error);
        }

        return indexedAgents.values();
    }

    private BooleanAgentDocument convertToBooleanAgentDocument(AgentResult agentResult) {
        BooleanAgentDocument booleanAgentDocument = new BooleanAgentDocument();

        Map<String, Object> agentSource = agentResult.getSource();

        if (!agentSource.containsKey(booleanRestrictionFieldName)) {
            String error = MessageFormat.format("Failed to retrieve boolean restriction of agent with id \"{0}\" in Elasticsearch", agentResult.getAgentId());
            logger.error(error);
            throw new RuntimeException(error);
        }

        booleanAgentDocument.setBooleanRestriction(Collections.singletonList(agentSource.get(booleanRestrictionFieldName))
                .stream()
                .map(String::valueOf)
                .collect(Collectors.toList()));

        if (agentSource.containsKey(conditionIdFieldName)) {
            booleanAgentDocument.setCondition_id(Collections.singletonList(agentSource.get(conditionIdFieldName))
                    .stream()
                    .map(String::valueOf)
                    .collect(Collectors.toList()));
        }
        if (agentSource.containsKey(lexiconIdFieldName)) {
            booleanAgentDocument.setLexicon_id(Collections.singletonList(agentSource.get(lexiconIdFieldName))
                    .stream()
                    .map(String::valueOf)
                    .collect(Collectors.toList()));
        }
        if (agentSource.containsKey(lexiconExpressionIdFieldName)) {
            booleanAgentDocument.setLexicon_expression_id(Collections.singletonList(agentSource.get(lexiconExpressionIdFieldName))
                    .stream()
                    .map(String::valueOf)
                    .collect(Collectors.toList()));
        }

        booleanAgentDocument.setLinks(getHighlightLinks(agentResult.getMatch()));

        return booleanAgentDocument;
    }

    private Collection<String> getHighlightLinks(PercolateResponse.Match match) {
        ArrayList<String> highlightTerms = new ArrayList<>();

        Map<String, HighlightField> highlightFields = match.getHighlightFields();
        if (highlightFields != null) {
            HighlightField highlightedContentField = highlightFields.get(contentFieldName);
            for (Text fragment : highlightedContentField.getFragments()) {
                highlightTerms.addAll(extractLinksFromHighlightedText(fragment.string()));
            }
        }

        return highlightTerms.stream().distinct().collect(Collectors.toList());
    }

    @Override
    public void destroy() throws Exception {
        if (this.elasticClient != null) {
            this.elasticClient.close();
            this.elasticClient = null;
        }
    }


    private class AgentResult {
        private String agentId;
        private PercolateResponse.Match match;
        private Map<String, Object> source;

        public AgentResult(String agentId, PercolateResponse.Match match, Map<String, Object> source) {
            this.agentId = agentId;
            this.match = match;
            this.source = source;
        }

        public String getAgentId() {
            return agentId;
        }

        public PercolateResponse.Match getMatch() {
            return match;
        }

        public Map<String, Object> getSource() {
            return source;
        }

        public void setSource(Map<String, Object> source) {
            this.source = source;
        }
    }
}
