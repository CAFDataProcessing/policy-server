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
package com.github.cafdataprocessing.corepolicy.tokenizers;

import com.github.cafdataprocessing.corepolicy.common.*;
import com.github.cafdataprocessing.corepolicy.repositories.RepositoryConnectionProvider;
import com.github.cafdataprocessing.corepolicy.common.dto.ClassifyDocumentResult;
import com.github.cafdataprocessing.corepolicy.common.dto.CollectionSequence;
import com.github.cafdataprocessing.corepolicy.common.dto.CollectionSequenceEntry;
import com.github.cafdataprocessing.corepolicy.common.dto.DocumentCollection;
import com.github.cafdataprocessing.corepolicy.common.dto.conditions.RegexCondition;
import com.github.cafdataprocessing.corepolicy.common.dto.conditions.TextCondition;
import com.github.cafdataprocessing.corepolicy.repositories.RepositoryType;
import com.github.cafdataprocessing.corepolicy.testing.Assume;
import com.github.cafdataprocessing.corepolicy.testing.IntegrationTestBase;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

import java.security.InvalidParameterException;
import java.sql.Connection;
import java.util.*;

import static com.github.cafdataprocessing.corepolicy.TestHelper.getUniqueString;

/**
 * Test varying languages and check that expected results from classifications are returned.
 * Uses language codes from http://en.wikipedia.org/wiki/List_of_ISO_639-1_codes
 */
public class LanguagesIntegrationIT extends IntegrationTestBase {

    protected ClassifyDocumentApi classifyDocumentApi;
    protected ClassificationApi classificationApi;
    protected ElasticsearchProperties elasticsearchProperties;

    public LanguagesIntegrationIT()
    {
        super();
        classifyDocumentApi = genericApplicationContext.getBean(ClassifyDocumentApi.class);
        classificationApi = genericApplicationContext.getBean(ClassificationApi.class);
        elasticsearchProperties = genericApplicationContext.getBean(ElasticsearchProperties.class);
    }

    protected Connection getConnectionToClearDown(){
        if(apiProperties.getMode().equalsIgnoreCase("direct")) {
            RepositoryConnectionProvider repositoryConnectionProvider = genericApplicationContext.getBean(RepositoryConnectionProvider.class);
            return repositoryConnectionProvider.getConnection(RepositoryType.CONDITION_ENGINE);
        }
        throw new InvalidParameterException(apiProperties.getMode());
    }

    @Before
    public void setup(){

    }

    @Test
    public void testThaiTokenizer_Classify(){

        // Set up the collection sequence to evaluate our condition.
        String inputString = "hello สบายดีไหม";
        String conditionTerm = "ดี ";

        checkTextConditionMatches(inputString, conditionTerm, "tha");
    }

    @Test
    public void testThaiTokenizer_REGEX_Classify(){

        Assume.assumeFalse(Assume.AssumeReason.BUG, "PD-952", "FullTokenizerIntegration::testThaiTokenizer_REGEX_Classify",
                "Sentence breaking cannot run against docker container.",
                testingProperties.getInDocker(),genericApplicationContext);

        // Set up the collection sequence to evaluate our condition.
        String inputString = "hello สบายดีไหม";
        String conditionTerm = "REGEX:ดี ";

        checkTextConditionMatches(inputString, conditionTerm, "tha");
    }

    @Test
    public void testKoreanTokenizer_Classify(){

        Assume.assumeFalse(Assume.AssumeReason.BUG, "PD-952", "FullTokenizerIntegration::testKoreanTokenizer_Classify",
                "Sentence breaking cannot run against docker container.",
                testingProperties.getInDocker(),genericApplicationContext);

        // Set up the collection sequence to evaluate our condition.
        String inputString = "this is good 길을 잃어버렸어요";
        String conditionTerm = "길";

        checkTextConditionMatches(inputString, conditionTerm, "ko");
    }


    @Test
    public void testKoreanTokenizer_REGEX_Classify(){
        // Set up the collection sequence to evaluate our condition.
        String inputString = "this is good 길을 잃어버렸어요";
        String conditionTerm = "REGEX:길";

        checkTextConditionMatches(inputString, conditionTerm, "ko");
    }


    @Test
    public void testJapaneseTokenizer_Classify(){

        Assume.assumeTrue(Assume.AssumeReason.BUG,
                "testJapaneseTokenizer_Classify",
                "CAF-1612",
                "Cannot run testJapaneseTokenizer_Classify in elasticsearch mode as our Elasticsearch installation " +
                        "doesn't accurately tokenize Japanese.",
                elasticsearchProperties.isElasticsearchDisabled(),
                genericApplicationContext);

        // Set up the collection sequence to evaluate our condition.
        String inputString = "お手伝いしましょうか";
        String conditionTerm = "し";

        checkTextConditionMatches(inputString, conditionTerm, "jpn");
    }

    @Test
    public void testChineseTokenizer_Classify(){

        Assume.assumeTrue(Assume.AssumeReason.BUG,
                "testChineseTokenizer_Classify",
                "CAF-1612",
                "Cannot run testChineseTokenizer_Classify in elasticsearch mode as our Elasticsearch installation " +
                        "doesn't accurately tokenize Chinese.",
                elasticsearchProperties.isElasticsearchDisabled(),
                genericApplicationContext);

        // Set up the collection sequence to evaluate our condition.
        String inputString = "你好，你怎么样?";
        String conditionTerm = "你";

        checkTextConditionMatches(inputString, conditionTerm, "zh");
    }


    private void checkTextConditionMatches(String inputString, String conditionTerm, String languageCode) {

        DocumentCollection collection1 = new DocumentCollection();
        collection1.name = getUniqueString("Tokenizer Unique Test:");

        final String regEx = "REGEX:";

        if( conditionTerm.startsWith(regEx))
        {
            RegexCondition condition = new RegexCondition();
            condition.name = "testTokenizer_Classify:" + languageCode;
            condition.field = "content";
            condition.value = conditionTerm.substring(regEx.length());
            collection1.condition = condition;
        }
        else {
            TextCondition stringCondition = new TextCondition();
            stringCondition.name = "testTokenizer_Classify:" + languageCode;
            stringCondition.field = "content";
            stringCondition.value = conditionTerm;
            stringCondition.language = languageCode;
            collection1.condition = stringCondition;
        }

        collection1 = classificationApi.create(collection1);


        CollectionSequence matchedConditionRecordingCollection = new CollectionSequence();
        matchedConditionRecordingCollection.name = getUniqueString("LanguagesIntegrationIT::tests");
        matchedConditionRecordingCollection.description = "Used in LanguagesIntegrationIT tokenizer tests.";
        matchedConditionRecordingCollection.collectionSequenceEntries = new ArrayList<>();

        CollectionSequenceEntry collectionSequenceEntry = new CollectionSequenceEntry();
        collectionSequenceEntry.collectionIds = new HashSet<>(Arrays.asList(collection1.id));
        collectionSequenceEntry.stopOnMatch = true;
        collectionSequenceEntry.order = 400;
        matchedConditionRecordingCollection.collectionSequenceEntries.add(collectionSequenceEntry);

        matchedConditionRecordingCollection = classificationApi.create(matchedConditionRecordingCollection);

        // Now we can evaluate against this condition.
        Document document = new DocumentImpl();
        document.setReference(UUID.randomUUID().toString());
        document.getMetadata().put("content", inputString);
        document.setFullMetadata(true);

        Collection<ClassifyDocumentResult> results = classifyDocumentApi.classify(matchedConditionRecordingCollection.id, Arrays.asList(document));

        Assert.assertNotNull("Results must not be null", results);
        Assert.assertEquals("Must be single result", 1, results.size());
        Assert.assertEquals("Must match our collection", 1, results.stream().findFirst().get().matchedCollections.size());
    }
}
