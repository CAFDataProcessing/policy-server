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
package com.github.cafdataprocessing.corepolicy.web;

import com.github.cafdataprocessing.corepolicy.ConditionEngine;
import com.github.cafdataprocessing.corepolicy.GenerateDemoContent;
import com.github.cafdataprocessing.corepolicy.common.*;
import com.github.cafdataprocessing.corepolicy.document.DocumentUnderEvaluation;
import com.github.cafdataprocessing.corepolicy.document.DocumentUnderEvaluationImpl;
import com.github.cafdataprocessing.corepolicy.common.Version;
import com.github.cafdataprocessing.corepolicy.common.dto.CollectionSequence;
import com.github.cafdataprocessing.corepolicy.common.dto.CollectionSequenceEntry;
import com.github.cafdataprocessing.corepolicy.common.dto.DocumentCollection;
import com.github.cafdataprocessing.corepolicy.common.dto.conditions.ConditionTarget;
import com.github.cafdataprocessing.corepolicy.common.dto.conditions.FieldCondition;
import com.github.cafdataprocessing.corepolicy.common.exceptions.ConditionEngineException;
import com.github.cafdataprocessing.corepolicy.common.shared.CommaSeparated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;

;

/**
* Created to assist in debugging code.
*/
@RestController
@RequestMapping(value = "/debug", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
public class DebugController extends BaseErrorHandlingController {

    final static Logger logger = LoggerFactory.getLogger(DebugController.class);
    private GenerateDemoContent generateDemoContent;
    private ClassificationApi classificationApi;
    private ConditionEngine conditionEngine;
    private ConditionEngineMetadata conditionEngineMetadata;
    private ApiProperties apiProperties;
    private EngineProperties engineProperties;
    private ApplicationContext applicationContext;

    @Autowired
    public DebugController(
            GenerateDemoContent generateDemoContent,
            ClassificationApi classificationApi,
            ConditionEngine conditionEngine,
            ConditionEngineMetadata conditionEngineMetadata,
            ApiProperties apiProperties,
            ApplicationContext applicationContext,
            EngineProperties engineProperties
    ){
        this.generateDemoContent = generateDemoContent;
        this.classificationApi = classificationApi;
        this.conditionEngine = conditionEngine;
        this.conditionEngineMetadata = conditionEngineMetadata;
        this.apiProperties = apiProperties;
        this.engineProperties = engineProperties;
        // now created using beans, so we can shutdown the item, on reload of webcontext.
        this.applicationContext = applicationContext;
    }

    /**
     * This method prints a version string that is injected by the build process.
     * @return  String indicating the build version
     */
    @RequestMapping(value = "/buildversion")
    public String getBuildVersion(){
        return VersionNumber.getCurrentVersion();
    }

    /**
     * This method prints a version string with information that is injected by the build process.
     * @return  String indicating the build version
     */
    @RequestMapping(value = "/config", method = RequestMethod.GET ,produces = "text/html")
    public String getCurrentConfiguration(){

        StringBuilder sb = new StringBuilder();
        sb.append("<h2>Current Configuration</h2>");
        sb.append("<ul>");
        sb.append("<li>");
            sb.append( "<h3>VersionInfo:</h3> " + Version.getCurrentVersionInfo() + "<br>");
        sb.append("</li>");
        sb.append("<li>");
            sb.append( "<h3>Current Config Location:</h3> " +
                    applicationContext.getEnvironment().getProperty("CAF_COREPOLICY_CONFIG") + "<br>");
        sb.append("</li>");
        sb.append("<li>");
            sb.append( "<h3>ApiProperties:</h3> " + apiProperties.toString() + "<br>");
        sb.append("</li>");
        sb.append("<li>");
            sb.append( "<h3>EngineProperties:</h3> " + engineProperties.toString() + "<br>");
        sb.append("</li>");
        sb.append("</ul>");
        return sb.toString();
    }
    /**
     * Use to test that the server is contactable and params are understandable. Returns a string representation of
     * the url and all parameters called.
     * @param request
     * @return
     * @throws ConditionEngineException
     * @throws IOException
     */
    @RequestMapping(value = "/echo")
    public ApiResult echoRequest(
            HttpServletRequest request
    ) throws ConditionEngineException, IOException {
        logRequest(logger, request);
        String result = getParametersAsString(request);
        logger.info(result);
        return new ApiResult<>(result);
    }

    class DocumentStatsJsonEntry
    {
        private String label;
        private String value;

        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
        public String getValue() {
            return value;
        }
        public void setValue(String value) {
            this.value = value;
        }

        public DocumentStatsJsonEntry(String label, String value)
        {
            this.label = label;
            this.value = value;
        }
    }

    private static final String ocrKey = "OCR";
    private static final String textExtractKey = "TEXT_EXTRACT";
    private static final String speechToTextKey = "SPEECH_TO_EXTRACT";
    private static final Map<String, String> definedKeyMappings;
    static
    {
        definedKeyMappings = new HashMap<String, String>();
        definedKeyMappings.put(ocrKey, "OCR");
        definedKeyMappings.put(textExtractKey, "Text Extract");
        definedKeyMappings.put(speechToTextKey, "Speech Recognition");
    }

    enum StatsRequest {
        ALL,
        CLASSIFICATION,
        WORKER
    }


    @RequestMapping(value = "/populate", method = RequestMethod.GET ,produces = "text/html")
    public String showPopulateOptions(HttpServletRequest request) throws ConditionEngineException {

        logRequest(logger, request);

        StringBuilder sb = new StringBuilder("<h2>Possible options: <strong>debug/populate/{option}</strong></h2>" + System.lineSeparator());
        sb.append("<ul>");

        for(PopulateOptions o : PopulateOptions.values()){
            sb.append("<li>");
            sb.append(String.format("\t<strong>%s</strong>: %s", o.toString(), o.getDescription()));
            sb.append("</li>");
        }

        sb.append("</ul>");
        return sb.toString();
    }

    @RequestMapping(value = "/populate/{populateOption}")
    public ApiResult populate(
            @PathVariable("populateOption") String populateOption,
            HttpServletRequest request
    ) throws Exception {

        logRequest(logger, request);

        PopulateOptions option = PopulateOptions.valueOf(populateOption.toUpperCase());

        switch(option){
            case SIMPLE:
                return new ApiResult<>(generateDemoContent.createSimpleConditionSequence());
            default:
                throw new Exception("Not implemented");
        }
    }

    @RequestMapping(value = "/log")
    public void log(
            HttpServletRequest request
    ) throws Exception {
        logRequest(logger, request);

        logger.error("ERROR LOG");
        logger.warn("WARN LOG");
        logger.info("INFO LOG");
        logger.debug("DEBUG LOG");
        logger.trace("TRACE LOG");
    }


        /**
         * A utility method to execute a condition against a field value and see the result. This is done by creating a
         * full collection sequence with a copy of the condition and evaluating it (because agents etc might need to be
         * created, so we can't just use the condition evaluator).
         * @param request       For logging
         * @param conditionId   The condition id to execute
         * @param fieldValue    The string value of the field. The field name is calculated automatically
         * @return              The condition result.
         * @throws Exception
         */
    @RequestMapping(value="/testfieldcondition")
    public ApiResult testfieldcondition(
            HttpServletRequest request,
            @RequestParam(value="condition_id") long conditionId,
            @RequestParam(value="field_value") String fieldValue
    ) throws Exception {

        logRequest(logger, request);

        //FieldCondition condition = (FieldCondition) conditionRepository.findOne(conditionId);
        ArrayList<Long> ids = new ArrayList<>(1);
        ids.add(conditionId);
        FieldCondition condition = (FieldCondition) classificationApi.retrieveConditions(ids, false).stream().findFirst().get();
        if(condition == null)
            return new ApiResult<>("Invalid field condition");

        logger.debug("Testing field condition " + condition.id + ", name " + condition.name);

        Document d = new DocumentImpl();
        d.getMetadata().put("reference", "Test Document");
        d.getMetadata().put(condition.field, fieldValue);

        DocumentUnderEvaluation evaluationDocument = new DocumentUnderEvaluationImpl(d, conditionEngineMetadata, apiProperties);

        Long newSequenceId = null;
        Long newCollectionId = null;
        ApiResult result;

        UUID identifier = UUID.randomUUID();

        try {

            //we will clear the id of the existing condition and use it to save a new one, with the correct target etc.
            condition.id = null;
            condition.name = "Test Field Condition Copy " + identifier;
            condition.target = ConditionTarget.CONTAINER;
            condition.includeDescendants = false;
            condition.isFragment = false;

            DocumentCollection collection = new DocumentCollection();
            collection.name = "Test Field Condition Collection " + identifier;
            collection.description = "Test Collection";
            collection.condition = condition;
            collection = classificationApi.create(collection);

            collection = classificationApi.create(collection);
            newCollectionId = collection.id;


            CollectionSequenceEntry entry = new CollectionSequenceEntry();
            entry.stopOnMatch = false;
            entry.collectionIds.add(newCollectionId);
            entry.order= (short)100;

            CollectionSequence sequence = new CollectionSequence();
            sequence.name = "Test Field Condition Sequence " + identifier;
            sequence.description = "Test sequence";
            sequence.collectionSequenceEntries.add(entry);

            sequence = classificationApi.create(sequence);
            newSequenceId = sequence.id;

            result = new ApiResult<>(conditionEngine.evaluate(evaluationDocument, newSequenceId));
        }
        catch(Exception ex){
            logger.error("Error testing a field condition.", ex);
            result = new ApiResult<>("There was a problem: " + ex.getMessage());
        }

        /*
         * CLEANUP
         */
        try {
            if (newSequenceId != null) {
                classificationApi.deleteCollectionSequence(newSequenceId);
            }
            if (newCollectionId != null) {
                classificationApi.deleteCollection(newCollectionId);
            }
        } catch (Exception ignored){

        }

        return result;
    }


    private String getParametersAsString(HttpServletRequest request){
        StringBuilder sb = new StringBuilder();
        final Enumeration<String> parameterNames = request.getParameterNames();
        while(parameterNames.hasMoreElements()){
            String paramName = parameterNames.nextElement();
            final String[] parameterValues = request.getParameterValues(paramName);
            sb
                    .append(paramName)
                    .append(": ")
                    .append(CommaSeparated.getString(parameterValues))
                    .append(System.lineSeparator());
        }
        return sb.toString();
    }

    /**
     * Used to decide what options to do when you call the debug controller.
     */
    enum PopulateOptions {
        SIMPLE;

        public String getDescription(){
            switch (this){
                case SIMPLE:
                    return "A simple collection sequence to get data in the system.";
                default:
                    return "Unknown value: " + this.toString();
            }
        }
    }
}
