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
package com.github.cafdataprocessing.corepolicy.document;

import com.github.cafdataprocessing.corepolicy.common.*;
import com.github.cafdataprocessing.corepolicy.conditionEvaluators.ConditionEvaluationResult;
import com.google.common.base.Stopwatch;
import com.google.common.base.Strings;
import com.google.common.collect.Multimap;
import com.github.cafdataprocessing.corepolicy.booleanagent.BooleanAgentQueryResult;
import com.github.cafdataprocessing.corepolicy.common.dto.ConditionEngineResult;
import com.github.cafdataprocessing.corepolicy.common.dto.MatchedCondition;
import com.github.cafdataprocessing.corepolicy.common.dto.UnmatchedCondition;
import com.github.cafdataprocessing.corepolicy.common.exceptions.BackEndRequestFailedCpeException;
import com.github.cafdataprocessing.corepolicy.common.shared.MetadataValue;
import com.github.cafdataprocessing.corepolicy.conditionEvaluators.CachedConditionEvaluationResult;
import com.github.cafdataprocessing.corepolicy.multimap.utils.CaseInsensitiveMultimap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.github.cafdataprocessing.corepolicy.common.TimeUnitLogging.TimeUnitToString;

/**
 *
 */
public class DocumentUnderEvaluationImpl implements DocumentUnderEvaluation {

    private final static Logger logger = LoggerFactory.getLogger(DocumentUnderEvaluationImpl.class);

    private Multimap<String, MetadataValue> labelValues = new CaseInsensitiveMultimap<>();
    private Multimap<String, MetadataValue> languageValues = new CaseInsensitiveMultimap<>();
    private Map<String, BooleanAgentQueryResult> fieldBooleanAgentQueryResults = new HashMap<>();
    private Collection<DocumentUnderEvaluation> documents = new ArrayList<>();
    private Boolean isExcluded = false;
    private Map<Long, MatchedCondition> matchedConditionEvaluationResults = new HashMap<>();
    private Map<Long, UnmatchedCondition> unmatchedConditionEvaluationResults = new HashMap<>();

    private HashSet<Long> conditionsEvaluatedThisRun = new HashSet<>();
    ConcurrentHashMap<String, Long> timings = new ConcurrentHashMap<>();
    /**
     * A Multimap of document Metadata, we are not handling structured fields at present.
     * N.B. We hold onto an object metadata which can represent a string or stream value.
     * This also allows for a hidden caching implementation if required between the 2.
     */
    private Multimap<String, MetadataValue> metadata = CaseInsensitiveMultimap.create();
    private Multimap<String, MetadataValue> streams = CaseInsensitiveKeyMultimap.create();

    private ConditionEngineMetadata conditionEngineMetadata;
    private ApiProperties apiProperties;

    public DocumentUnderEvaluationImpl(ConditionEngineMetadata conditionEngineMetadata, ApiProperties apiProperties){
        this.conditionEngineMetadata = conditionEngineMetadata;
        this.apiProperties = apiProperties;
    }

    public DocumentUnderEvaluationImpl(Document document, ConditionEngineMetadata conditionEngineMetadata, ApiProperties apiProperties){
        this(conditionEngineMetadata, apiProperties);

        // we hold onto a list of  MetadataValues internally, so convert at this point so we do it only once.
        for( Map.Entry<String, String> entry : document.getMetadata().entries())
        {
            this.metadata.put( entry.getKey(), new MetadataValue(apiProperties, entry.getValue()));
        }

        // we hold onto a list of  MetadataValues internally, so convert at this point so we do it only once.
        for( Map.Entry<String, InputStream> entry : document.getStreams().entries())
        {
            this.streams.put( entry.getKey(), new MetadataValue(apiProperties, entry.getValue()));
        }


        this.addMetadataString(DocumentFields.ChildDocumentCount, String.valueOf(document.getDocuments().size()));
        final int depth = getDepth();
        this.documents = document.getDocuments().stream().
                map(d -> {
                    DocumentUnderEvaluation documentUnderEvaluation = new DocumentUnderEvaluationImpl(d, conditionEngineMetadata, apiProperties);
                    documentUnderEvaluation.addMetadataString( DocumentFields.ChildDocumentDepth, String.valueOf(depth+1));
                    return documentUnderEvaluation;
                }).collect(Collectors.toList());

        setupPreevaluatedInformation(document);
    }

    public void addConditionEvaluationResult(ConditionEvaluationResult conditionEvaluationResult, Long conditionID ){

        if ( conditionID == null )
        {
            // this is only here for mocked tests which have passed in conditions without
            // any id field on them.
            logger.debug("addConditionEvaluationResult:: Warning - Mocked item has conditionID null.");
            return;
        }

        // add the evaluation result, to the list of eval results. ( only top level conditions, single node is added at a time. )
        if ( conditionEvaluationResult.isMatch() )
        {
            Optional<MatchedCondition> mc = conditionEvaluationResult.getMatchedConditions().stream().filter(u-> ( u.id != null ) && u.id.equals( conditionID ) ).findFirst();

            if ( !mc.isPresent() )
            {
                // we have a couple of exceptions where we dont actually cache off the parent result.
                // Fragments Conditions are one of these.  The parent fragment result isn't actually returned as a match
                // I have skipped calling this for matches on fragments, so we can keep throwing.
                throw new BackEndRequestFailedCpeException(new RuntimeException( "Unexpected exception - unable to get the conditionevaluation result, for an evaluated condition Id: " + conditionID ));
            }

            matchedConditionEvaluationResults.put( conditionID, mc.get() );
        }
        else
        {
            Optional<UnmatchedCondition> umc = conditionEvaluationResult.getUnmatchedConditions().stream().filter(u->( u.id != null ) && u.id.equals( conditionID ) ).findFirst();

            if ( !umc.isPresent() )
            {
                // its possible we have the condition isMatch = false but its unevaluated, as such we can just leave now - we have nothing
                // more info other than its been run already.
                return;
            }

            unmatchedConditionEvaluationResults.put( conditionID, umc.get() );
        }
    }

    public void addConditionEvaluationResult(MatchedCondition mc ){
        if(mc!=null && mc.id != null ) {
            matchedConditionEvaluationResults.put(mc.id, mc);
        }
    }

    public void addConditionEvaluationResult( UnmatchedCondition umc ){
        if(umc!=null && umc.id != null ) {
            unmatchedConditionEvaluationResults.put(umc.id, umc);
        }
    }

    public CachedConditionEvaluationResult getConditionEvaluationResult(Long conditionId){

        CachedConditionEvaluationResult result = new CachedConditionEvaluationResult();

        // first try the matched conditions.
        if ( matchedConditionEvaluationResults.containsKey(conditionId) )
        {
            result.setIsMatch(true);
            result.setMatchedCondition( matchedConditionEvaluationResults.get(conditionId));
            return result;
        }
        else if ( unmatchedConditionEvaluationResults.containsKey(conditionId )) {
            result.setIsMatch(false);
            result.setUnmatchedCondition(unmatchedConditionEvaluationResults.get(conditionId));
            return result;
        }

        // otherwise if its not cached, just return null.
        return null;
    }


    @Override
    public void logTime(String category, Stopwatch stopwatch) {
        final Stopwatch finalStopwatch = stopwatch.stop();
        timings.computeIfAbsent(category, (key) -> finalStopwatch.elapsed(TimeUnit.NANOSECONDS));
        timings.computeIfPresent(category, (key, value) -> value + finalStopwatch.elapsed(TimeUnit.NANOSECONDS));
    }

    @Override
    public void log(Logger logger) {
        StringBuilder logMessage = new StringBuilder();
        for(String key:timings.keySet().stream().sorted().collect(Collectors.toList())){
            long nanos = timings.get(key);
            TimeUnitToString(logMessage, key, nanos);
        }
        logger.debug(logMessage.toString());
    }

    @Override
    public Multimap<String, MetadataValue> getMetadata() {
        return metadata;
    }

    /**
     * Please note the getStreams interface on document under evaluation is different from the public getStreams
     * interface which uses simple types such as InputStream and not MetadataValue.
     * @return
     */
    @Override
    public Multimap<String, MetadataValue> getStreams() {
        return streams;
    }


    @Override
    public Collection<DocumentUnderEvaluation> getDocuments() {
        return documents;
    }

    @Override
    public void addBooleanAgentQueryResult(String fieldName, LanguagesEnum language, BooleanAgentQueryResult booleanAgentQueryResult) {
        addBooleanAgentQueryResult(getFieldLanguageString(fieldName, language), booleanAgentQueryResult);
    }

    @Override
    public void addBooleanAgentQueryResult(String fieldName, BooleanAgentQueryResult booleanAgentQueryResult) {
        fieldBooleanAgentQueryResults.put(fieldName, booleanAgentQueryResult);
    }

    @Override
    public BooleanAgentQueryResult getBooleanAgentQueryResult(String fieldName, LanguagesEnum language) {
        return getBooleanAgentQueryResult(getFieldLanguageString(fieldName, language));
    }

    @Override
    public BooleanAgentQueryResult getBooleanAgentQueryResult(String fieldName) {
        return fieldBooleanAgentQueryResults.get(fieldName);
    }

    @Override
    public void addMetadataString(String key, String value){
        metadata.put(key, new MetadataValue(apiProperties, value));
    }

    @Override
    public void addMetadataStream(String key, InputStream value){
        streams.put(key, new MetadataValue(apiProperties, value));
    }

    @Override
    public boolean metadataContains(String key, String value) {
        return getMetadata().get(key).stream().filter(u -> (!Strings.isNullOrEmpty( u.getAsString() ) && u.getAsString().contains(value))).findFirst().isPresent();
    }

    public void setDocuments(Collection<DocumentUnderEvaluation> documents) {
        this.documents = documents;
    }

    public Integer getDepth(){
        if(this.metadata.containsKey(DocumentFields.ChildDocumentDepth)){
            return Integer.valueOf(this.metadata.get(DocumentFields.ChildDocumentDepth).stream().findFirst().get().getAsString());
        }
        return 0;
    }

    @Override
    public void addLabelValues(String labelName, Collection<MetadataValue> values) {
        labelValues.putAll(labelName, values);
    }

    @Override
    public Collection<MetadataValue> getLabelValues(String labelName) {
        return labelValues.get(labelName);
    }

    @Override
    public boolean hasLabelValues(String labelName) {
        return labelValues.containsKey(labelName);
    }

    public String getReference(){
        return ReferenceExtractor.getReferenceFromMap(this.metadata, !this.documents.isEmpty());
    }

    public void setReference(String reference){
        ReferenceExtractor.setReferenceAsMetadataValue(this.metadata, reference, apiProperties);
    }

    public boolean getFullMetadata(){
        if(metadata.containsKey(DocumentFields.KV_Metadata_Present_FieldName)){
            return Boolean.valueOf(metadata.get(DocumentFields.KV_Metadata_Present_FieldName).stream().findFirst().get().getAsString());
        }
        return true;
    }

    public void setFullMetadata(boolean fullMetadata){
        if(metadata.containsKey(DocumentFields.KV_Metadata_Present_FieldName)){
            metadata.get(DocumentFields.KV_Metadata_Present_FieldName).clear();
        }
        addMetadataString(DocumentFields.KV_Metadata_Present_FieldName, String.valueOf(fullMetadata));
    }

    @Override
    public Boolean getIsExcluded() {
        return isExcluded;
    }

    @Override
    public void setIsExcluded(Boolean isExcluded) {
        this.isExcluded = isExcluded;
    }

    @Override
    public Collection<MetadataValue> getValues(String name) {
        if(labelValues.containsKey(name)){
            return getLabelValues(name);
        } else if(getMetadata().containsKey(name) || this.streams.containsKey(name)) {
            // We merge the metadata and streams list together when requested.
            Collection<MetadataValue> values = getMetadata().get(name);

            if (this.streams.containsKey(name))
            {
                values.addAll( this.streams.get(name));
            }
            return values;
        }
        //return the empty collection. Retrieving it from metadata so that the type is an exact match.
        return getMetadata().get(name);

    }

    private String getCombinedFieldName(String fieldName, String dif) {
        if(dif == null){
            return fieldName;
        }
        return fieldName + "-<" + dif + ">";
    }

    private String getFieldLanguageString(String fieldName, LanguagesEnum language) {
        return getCombinedFieldName(fieldName, language == null ? null: language.toString());
    }

    @Override
    public void addLanguageValue(String fieldName, LanguagesEnum language, MetadataValue value) {
        languageValues.put(getFieldLanguageString(fieldName, language), value);
    }

    @Override
    public Collection<MetadataValue> getLanguageValue(String fieldName, LanguagesEnum language) {
        if(!languageValues.containsKey(getFieldLanguageString(fieldName, language))){
            return null;
        }
        return languageValues.get(getFieldLanguageString(fieldName, language));
    }

    private void setupPreevaluatedInformation(Document document) {

        // Check if we have any supplied metadata which has been evaluated before.
        ConditionEngineResult result = conditionEngineMetadata.createResult(document.getMetadata());
        applyEvaluationInfo(result, this);
    }

    private static void applyEvaluationInfo(ConditionEngineResult previousResult, DocumentUnderEvaluation documentUnderEvaluation) {

        // recurse down the children applying to each one.
        Collection<DocumentUnderEvaluation> documents = documentUnderEvaluation.getDocuments();

        for (DocumentUnderEvaluation childDocument : documents) {

            applyEvaluationInfo(previousResult, childDocument);
        }

        // Now apply the information to this document itself.
        setupEvaluationInfoOnThisDocument(previousResult, documentUnderEvaluation);
    }

    private static void setupEvaluationInfoOnThisDocument(ConditionEngineResult previousResult, DocumentUnderEvaluation documentUnderEvaluation) {

        // Now apply the information to this document itself, ensure the matches are specific to our current document reference.
        String docRef = documentUnderEvaluation.getReference();

        // Add all matched conditions
        if(previousResult != null) {
            if (previousResult.matchedConditions.size() > 0) {

                for (MatchedCondition mc : previousResult.matchedConditions) {

                    // add on if our docRef isn't empty, but the condition reference is.
                    // or both are not empty and match.
                    if ((Strings.isNullOrEmpty(docRef) && Strings.isNullOrEmpty(mc.getReference())) ||
                            (!Strings.isNullOrEmpty(docRef) && !Strings.isNullOrEmpty(mc.getReference()) && mc.getReference().equals(docRef))) {

                        // ok so both are null, or both have same name.
                        documentUnderEvaluation.addConditionEvaluationResult(mc);
                    }
                }
            }

            // Add all unmatched conditions
            if (previousResult.unmatchedConditions.size() > 0) {

                for (UnmatchedCondition umc : previousResult.unmatchedConditions) {

                    if ((Strings.isNullOrEmpty(docRef) && Strings.isNullOrEmpty(umc.getReference())) ||
                            (!Strings.isNullOrEmpty(docRef) && !Strings.isNullOrEmpty(umc.getReference()) && !umc.getReference().equals(docRef))) {

                        documentUnderEvaluation.addConditionEvaluationResult(umc);
                    }
                }
            }
        }
    }

    public void setConditionHasBeenEvaluatedThisRun( Long conditionId  )
    {
        conditionsEvaluatedThisRun.add( conditionId );
    }

    public Boolean hasConditionBeenEvaluatedThisRun( Long conditionId )
    {
        return conditionsEvaluatedThisRun.contains( conditionId );
    }

}