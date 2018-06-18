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
package com.github.cafdataprocessing.corepolicy.document;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.github.cafdataprocessing.corepolicy.common.Document;
import com.github.cafdataprocessing.corepolicy.common.dtoweb.DocumentArrayWrapper;
import com.github.cafdataprocessing.corepolicy.common.exceptions.BackEndRequestFailedCpeException;
import com.github.cafdataprocessing.corepolicy.common.shared.CorePolicyObjectMapper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Collection;

/**
 * Tests for core policy object mapper.
 */
public class CorePolicyObjectMapperTest {
    CorePolicyObjectMapper corePolicyObjectMapper = new CorePolicyObjectMapper();

    @Before
    public void setup(){

    }

    @After
    public void cleanup(){

    }

    @Test
    public void createTest() throws Exception {
        String documentJson = "{\n" +
                "   \"document\" :\n" +
                "   [\n" +
                "      {\n" +
                "         \"title\" : \"This is my document\",\n" +
                "         \"reference\" : \"mydoc1\",\n" +
                "         \"myfield\" : [\"a value\",\"another value\"],\n" +
                "         \"content\" : \"A large block of text, which makes up the main body of the document.\"\n" +
                "      }, {\n" +
                "         \"title\" : \"My Other document\",\n" +
                "         \"reference\" : \"mydoc2\",\n" +
                "         \"content\" : \"This document is about something else\"\n" +
                "      }\n" +
                "   ]\n" +
                "}";

        Collection<Document> documents = fromJson(documentJson);

        ObjectMapper om = new ObjectMapper();
        om.registerModule(new GuavaModule());
        String docs = om.writer().writeValueAsString(documents);

        Assert.assertEquals(documents.size(), 2);
    }

    @Test(expected = Exception.class)
    public void createThrowsExceptionTest() throws Exception {
        String documentJson = "{\n" +
                "   \"document\" :\n" +
                "      {\n" +
                "         \"title\" : \"This is my document\",\n" +
                "         \"reference\" : \"mydoc1\",\n" +
                "         \"myfield\" : [\"a value\",\"another value\"],\n" +
                "         \"content\" : \"A large block of text, which makes up the main body of the document.\",\n" +
                "         \"fail\": {\"test\": 1}" +
                "      }\n" +
                "}";
        Collection<Document> documents = fromJson(documentJson);
    }


    /**
     * Test that a complex document JSON representation can be mapped.
     */
    @Test
    public void createComplexTest() throws Exception {
        final String documentString = "{\"document\":[{\"reference\":\"8f0ce9f40faa62b8e295a37697448a9e\"," +
                "\"app_name\":[\"Adobe PDF Library 9.9\"],\"author\":[\"Test\"],\"content-type\":[\"application/pdf\"]," +
                "\"created_date\":[\"1378283173\"],\"creator\":[\"Adobe InDesign CS5.5 (7.5)\"],\"document_attributes\":[\"0\"]," +
                "\"keyview_class\":[1],\"document_embedded_font_ratio\":[\"21\"],\"document_pct_embedded_font\":[\"100\"]," +
                "\"document_pct_probability_mismatch\":[\"21\"],\"original_size\":[637147],\"keyview_type\":[230]," +
                "\"modified_date\":[1397519305],\"page_count\":[\"4\"],\"processing_error_code\":[\"1\"]," +
                "\"processing_error_description\":[\"Document: Embedded Font\"],\"subject\":[\"HPE: built for the era of big data\"]," +
                "\"title\":\"Next-generation information analytics\"," +
                "\"content\":\"Brochure Next-generation information analytics: built for the era of big data Get your greatest return on information We are experiencing transformational changes in the computing arena. Data is doubling every 12 to 18 months, accelerating the pace of innovation and time-to-value. The vast majority of today’s data growth is driven by the creation of human-friendly information such as documents, social content, video, audio, and images. In fact, 90% of digital content being created will include these unstructured data types by 20151 . Data growth is also accelerated by the ubiquity of mobile devices, which gives us “always on” applications and users who demand anytime, anywhere access to information. As a business leader, you depend on human information to keep your business working to its full potential. The information analytics platform, has been built to address these modern challenges. Offers you a single processing layer that helps you automatically unlock key ideas and concepts in all your information, structured and unstructured. Based on advanced mathematical principles and augmented by over 170 patents, understand the key concepts present in virtually all forms of content, helping you to detect patterns and relationships between data. You can understand and act upon documents, emails, video, chat, phone calls, social media, and application data at the same time and faster than ever before. Because we know information is fragmented across a variety of repositories, streamlines information processing across networks, the web, and the cloud. We help you see your information wherever it is. After all, why should you work for your information when it can work for you? Powers many of the applications that depend upon an understanding of human information to perform, including: • Enterprise Search • eDiscovery • Voice of the Customer/Workforce • Marketing Optimization • Enterprise Content Management • Social Media Analytics Brochure | Next-generation information analytics Highlights • Understand virtually all of your information with high-performance analytics: Over 500 analytical functions available for text, audio, video, and image • Derive actionable insights: Process data in near real time to gain a competitive edge • Maximize your information reach: Connect to over 400 systems with support for over 1000 file formats, so you can find all relevant information • Let social media work for you: Detect emerging trends and influencers in this powerful media with sophisticated sentiment analysis and clustering technology • Achieve big data scalability and security: Track record of supporting the largest enterprises in volume of information, number of users, and frequency of transactions while adhering to strict security standards • Easy to use and administer: Leverage a visual dashboard and simplified processes • Manage data in place: Manage data within the original repository while ensuring its accessibility • Rapid innovation: Roadmap includes additional next-gen capabilities via quarterly releases understands virtually all information fragmented across different silos, and finds patterns and relationships surfaced through different applications 1 IDC Predictions 2012: Competing for 2020 Brochure | Next-generation information analytics If you can’t find your data, does it really exist? We have realized that traditional ways of understanding and using information simply fall short in providing the business benefits that are required in today’s world. When detectives try to uncover a crime, they look for incriminating emails and wade through surveillance videos. When marketers want to know more about their customers, they look to Twitter. Information today is diverse, dynamic, and complicated. The old method of finding answers or uncovering predictive patterns in the rows and columns of a database take far too much time, leave out gaps of knowledge, and often yield incomplete results. Similarly, keyword and metadata-based search return too many irrelevant and incomplete results, without giving actionable insights to make the information count. The explosion in the variety of file formats leaves most search and analytics technologies incapable of making sense of new file types. Since they rely on user-defined tags, which are often inconsistent and too vague to provide much meaning, you inevitably can’t connect with information you probably have, but for all practical purposes don’t know exists. There is simply too much data to sift through all at once without using a system that can actually understand it, and this unfortunately renders a lot of great information unsearchable and unusable. Understanding meaning helps you unleash the value of your information When you can analyze all types of information at once, you can make decisions while there is still time to change your course. For instance, what if you needed to simultaneously analyze all customers in one zip code who use an iPhone 4, provided product feedback when they called customer service, and posted a Yelp review in the same month? If you processed the data manually, it would take so long that the information would lose its value before you could use it. You can now perform sophisticated analytics on all types of information at the same time, and faster than ever before. Uses patented probabilistic and pattern-matching algorithms to automatically recognize concepts and ideas in all your information. Because the technology is based on the universal language of mathematics, it treats words as “symbols” and works in all languages. Unique technology detects patterns, emotions, sentiments, intentions, and preferences as they happen. Knowing more about your data in less time speeds the pace of business and strengthens your ability to compete. “Recruiters are highly skilled individuals, and that’s not something you can replace—but what we can do is give them a tool that helps them work more efficiently. Instead of 50 loosely-related candidates to review, recruiters get the 15 most suitable, and work from there.” —Daniel Richardson.\",\"md5sum\":[\"5c9e969ec9ae95fba93942ab995fc2d6\"]}]}";

        Collection<Document> documents = fromJson(documentString);
        Assert.assertEquals(1, documents.size());
    }

    /**
     * Test that Long type values are ok
     */
    @Test
    public void createWithLongFields() throws Exception {
        final String documentString = "{\"document\":[{\"longField\":1}]}";
        Collection<Document> documents = fromJson(documentString);
        Assert.assertEquals(1, documents.size());

        Document doc = documents.stream().findFirst().get();
        Assert.assertTrue(doc.getMetadata().containsKey("longField"));
        final Collection<String> longField = doc.getMetadata().get("longField");
        Assert.assertEquals(1, longField.size());
        Assert.assertEquals("1", longField.stream().findFirst().get());
    }

    /**
     * Test that multiple Long type values are ok
     */
    @Test
    public void createWithLongArrayFields() throws Exception {
        final String documentString = "{\"document\":[{\"longField\":[1,2,3]}]}";

        Collection<Document> documents = fromJson(documentString);
        Assert.assertEquals(1, documents.size());

        Document doc = documents.stream().findFirst().get();
        Assert.assertTrue(doc.getMetadata().containsKey("longField"));
        final Collection<String> longField = doc.getMetadata().get("longField");
        Assert.assertEquals(3, longField.size());
        Assert.assertTrue(longField.contains("1"));
        Assert.assertTrue(longField.contains("2"));
        Assert.assertTrue(longField.contains("3"));
    }

    /**
     * Test that non integer numbers are ok
     */
    @Test
    public void createWithNonIntegerNumberField() throws Exception {
        final String documentString = "{\"document\":[{\"longField\":1.01}]}";
        Collection<Document> documents = fromJson(documentString);
        Assert.assertEquals(1, documents.size());

        Document doc = documents.stream().findFirst().get();
        Assert.assertTrue(doc.getMetadata().containsKey("longField"));
        final Collection<String> longField = doc.getMetadata().get("longField");
        Assert.assertEquals(1, longField.size());
        Assert.assertEquals("1.01", longField.stream().findFirst().get());
    }

    /**
     * Test that multiple Long type values are ok
     */
    @Test
    public void createWithMultipleNonIntegerNumberField() throws Exception {
        final String documentString = "{\"document\":[{\"longField\":[1.01, 2.02, 3.03]}]}";

        Collection<Document> documents = fromJson(documentString);

        Assert.assertEquals(1, documents.size());

        Document doc = documents.stream().findFirst().get();
        Assert.assertTrue(doc.getMetadata().containsKey("longField"));
        final Collection<String> longField = doc.getMetadata().get("longField");
        Assert.assertEquals(3, longField.size());
        Assert.assertTrue(longField.contains("1.01"));
        Assert.assertTrue(longField.contains("2.02"));
        Assert.assertTrue(longField.contains("3.03"));
    }

    /**
     * Test that a single child works
     */
    @Test
    public void createWithSingleChildDocuments() throws Exception {
        final String documentString = "{\"document\":[{\"longField\":1, \"document\": [{\"childText\": \"Im a child\"}]}]}";
        Collection<Document> documents = fromJson(documentString);

        Assert.assertEquals(1, documents.size());

        Document doc = documents.stream().findFirst().get();
        Assert.assertEquals(1, doc.getDocuments().size());
        Assert.assertEquals("Im a child", doc.getDocuments().stream().findFirst().get().getMetadata().get("childText").stream().findFirst().get());
    }

    /**
     * Test that a single child works
     */
    @Test
    public void createWithMultipleChildDocuments() throws Exception {
        final String documentString = "{\"document\":[{\"longField\":1, \"document\": [{\"childText\": \"Im a child 1\"}, {\"childText\": \"Im a child 2\"}]}]}";
        Collection<Document> documents = fromJson(documentString);

        Assert.assertEquals(1, documents.size());

        Document doc = documents.stream().findFirst().get();
        Assert.assertEquals(2, doc.getDocuments().size());
    }

    Collection<Document> fromJson(String json){
        try {
            return corePolicyObjectMapper.readValue(json, DocumentArrayWrapper.class).document;
        } catch (IOException e) {
            throw new BackEndRequestFailedCpeException(e);
        }
    }
}
