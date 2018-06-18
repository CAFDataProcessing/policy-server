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
package com.github.cafdataprocessing.corepolicy.testing;

import com.github.cafdataprocessing.corepolicy.common.*;
import com.github.cafdataprocessing.corepolicy.common.dto.*;
import com.google.common.base.Strings;
import com.github.cafdataprocessing.corepolicy.common.dto.conditions.LexiconCondition;
import com.github.cafdataprocessing.corepolicy.environment.EnvironmentSnapshotApi;
import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.io.support.ResourcePropertySource;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.util.*;
import static org.junit.Assert.assertEquals;

/**
 *
 */
public abstract class IntegrationTestBase {

    private final static Logger logger = LoggerFactory.getLogger(IntegrationTestBase.class);

    protected ClassificationApi sut;
    protected EnvironmentSnapshotApi envSnapshot;
        
    private UserContext userContext;

    protected ApiProperties apiProperties;
    protected TestingProperties testingProperties;

    public static CorePolicyApplicationContext genericApplicationContext;

    public IntegrationTestBase() {
        // in certain cases we now have to shutdown the main ApiApplicationInstance as once a profile has been activated
        // it doesn't remove itself, it has to be rebuilt.  As such a quick request here will ensure we have a new context.
        // if it has been removed.
        CreateOrGetApplicationContext();

        apiProperties = genericApplicationContext.getBean(ApiProperties.class);

        if ( !genericApplicationContext.containsBean(TestingProperties.class.getName())) {


            GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
            beanDefinition.setBeanClass( TestingProperties.class );

            genericApplicationContext.registerBeanDefinition(TestingProperties.class.getName(), beanDefinition);

            // We need to add a property source to the environment.
            MutablePropertySources propertySources = genericApplicationContext.getEnvironment().getPropertySources();

            // As the property sources mentioned below may not in fact be present - they are in catch blocks.
            // Sorry it has to be manual, but it means the testing.properties file can use simple annotations.
            ResourcePropertySource propertySource = null;

            String configLocation = genericApplicationContext.getEnvironment().getProperty("CAF_COREPOLICY_CONFIG");

            // Add on the config location first as a property source, so its higher
            // up the priority list than the classPath.
            if ( !Strings.isNullOrEmpty(configLocation )){

                addPropertySource(propertySources, propertySource, "file:" + configLocation + "/testing.properties");
            }

            // Now add on the class path as a fallback.  testing.props will only be there for testing
            // project
            addPropertySource(propertySources, propertySource, "classpath:testing.properties");

        }

        testingProperties = genericApplicationContext.getBean(TestingProperties.class);
        try {
            // let our deployment type setup the usercontext correctly
            switch (apiProperties.getMode()) {
                case "direct":
                case "web": {
                    // set the real user context now!
                    userContext = genericApplicationContext.getBean(UserContext.class);
                    userContext.setProjectId(UUID.randomUUID().toString());

                    break;
                }
            }
        }
        catch(Exception ex)
        {
            getLogger().trace("Exception in IntegrationTestBase... ", ex);
            throw new RuntimeException(ex);
        }

        sut = genericApplicationContext.getBean(ClassificationApi.class);
        envSnapshot = genericApplicationContext.getBean(EnvironmentSnapshotApi.class);
    }

    public static CorePolicyApplicationContext CreateOrGetApplicationContext() {
        if ( genericApplicationContext == null) {
            genericApplicationContext = new CorePolicyApplicationContext();
            genericApplicationContext.refresh();
        }

        return genericApplicationContext;
    }

    private void addPropertySource(MutablePropertySources propertySources, ResourcePropertySource propertySource, String propSourcePath) {
        if ( !Strings.isNullOrEmpty(propSourcePath)) {
            propertySource = tryGetResourcePropertySource(propSourcePath);
        }

        if ( propertySource != null ) {
            propertySources.addLast(propertySource);
        }
    }

    private static ResourcePropertySource tryGetResourcePropertySource(String strInfo) {
        ResourcePropertySource propertySource = null;

        try{
            propertySource = new ResourcePropertySource(strInfo);
        }
        catch(Exception ex)
        {
            logger.debug("Warning only: No property source found", ex);
        }

        return propertySource;
    }

    public static Logger getLogger() {
        return logger;
    }

    @Before
    public void before(){

    }

    @After
    public void after(){
        if(userContext==null || !apiProperties.isInApiMode(ApiProperties.ApiMode.direct) || !apiProperties.isInRepository(ApiProperties.ApiDirectRepository.mysql)){
            return;
        }
        String projectId = userContext.getProjectId();

        // we have an admin test which sets projectId to null, make sure it doesn't clear out
        // our base data!
        if ( Strings.isNullOrEmpty(projectId))
            return;

        try {
            try(Connection connection = getConnectionToClearDown()){
                if(connection!=null){
                    ReleaseHistory version = new ReleaseHistory(VersionNumber.getCurrentVersion());
                    CallableStatement callableStatement = connection.prepareCall(String.format("CALL sp_clear_down_tables_v%d_%d(?)",version.majorVersion,version.minorVersion));
                    callableStatement.setString("projectId", projectId);
                    callableStatement.execute();
                }
            }
        }
        catch (RuntimeException e) {
            throw e;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected String getProjectId(){
        return userContext!=null ? userContext.getProjectId() : null;
    }

    protected abstract Connection getConnectionToClearDown();

    protected ClassificationApi getClassificationApi() {
        return sut;
    }


    
    
    // New utility methods, which are useful in all of our tests.
    protected Lexicon createNewLexicon(String name, String expressionRegex ) {
        Lexicon lexicon = new Lexicon();
        lexicon.name = name;

        {
            LexiconExpression expression = new LexiconExpression();
            expression.type = LexiconExpressionType.REGEX;
            expression.expression = expressionRegex;

            lexicon.lexiconExpressions = new LinkedList<>();
            lexicon.lexiconExpressions.add(expression);
        }

        return sut.create(lexicon);
    }

    protected LexiconCondition createLexiconCondition(Lexicon created, String field, String name )
    {
        return createLexiconCondition(created, field, name, false);
    }

    protected LexiconCondition createLexiconCondition(Lexicon created, String field, String name, Boolean dontCreateNow ) {
        LexiconCondition lexiconCondition = new LexiconCondition();
        lexiconCondition.name = name;
        lexiconCondition.field = field;
        lexiconCondition.language = "eng";
        lexiconCondition.value = created.id;

        // if someone wants to attach, or hold off creation, allow this.
        if ( dontCreateNow )
        {
            return lexiconCondition;
        }

        return sut.create(lexiconCondition);
    }

    protected DocumentCollection createUniqueDocumentCollection(String name) {
        DocumentCollection documentCollection = new DocumentCollection();

        if (Strings.isNullOrEmpty(name)) {
            documentCollection.name = ("ClassificationApiCollectionSequenceIt unique docCollection_");
        }
        else {
            documentCollection.name = name;
        }
        documentCollection.description = "used as unique doc collection to lookup by...";

        return sut.create(documentCollection);
    }

    protected CollectionSequence createCollectionSequence(DocumentCollection collection, String collectionName) {
        // supply the evaluation enabled default, to prevent other users having to do this all the time.
        return createCollectionSequence(collection, collectionName, null, new CollectionSequence().evaluationEnabled);
    }

    protected CollectionSequence createCollectionSequence(DocumentCollection collection, String collectionName, String collectionDescription, boolean evaluationEnabled) {


        CollectionSequence collectionSequence = new CollectionSequence();
        collectionSequence.name = collectionName;
        collectionSequence.description = collectionDescription;
        collectionSequence.evaluationEnabled = evaluationEnabled;

        if (collection != null) {
            CollectionSequenceEntry collectionSequenceEntry = new CollectionSequenceEntry();
            collectionSequenceEntry.collectionIds = new HashSet<>(Arrays.asList(collection.id));
            collectionSequenceEntry.order = (short) 100;
            collectionSequence.collectionSequenceEntries = Arrays.asList(collectionSequenceEntry);
        }

        collectionSequence = sut.create(collectionSequence);
        return collectionSequence;
    }
    
    protected void compareObject( CollectionSequenceEntry cse, CollectionSequenceEntry tmpVal, boolean skipFingerprint ) {
        // check each member matches.
        assertEquals( "CollectionSequenceEntry order field must match", cse.order, tmpVal.order );
        
        if ( !skipFingerprint )
        {
            assertEquals( "CollectionSequenceEntry fingerprint field must match", cse.fingerprint, tmpVal.fingerprint );
        }
        assertEquals( "CollectionSequenceEntry stopOnMatch field must match", cse.stopOnMatch, tmpVal.stopOnMatch );
        assertEquals( "CollectionSequenceEntry collectionIds field must match", cse.collectionIds, tmpVal.collectionIds );
    }

}
