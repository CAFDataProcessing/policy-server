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
package com.github.cafdataprocessing.corepolicy.testing;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.github.cafdataprocessing.corepolicy.testing.loaders.IdManager;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public abstract class TestRunner<TestType, TestSetType extends TestSet<TestType>, ResultType> {
    protected ObjectMapper objectMapper = new ObjectMapper();
    protected Logger logger = LoggerFactory.getLogger(TestRunner.class);

    protected TestRunnerApplicationContext applicationContext = new TestRunnerApplicationContext();
    protected IdManager idManager = new IdManager();

    @Option(name = "-projectid", usage = "Project ID for the Java web server.", required = false)
    protected String projectId;

    @Argument
    private List<String> inputDirs = new ArrayList<>();

    public TestRunner(){
        objectMapper.registerModule(new GuavaModule());
    }

    protected void run(String[] args) throws Exception {
        CmdLineParser parser = new CmdLineParser(this);
        try {
            parser.parseArgument(args);
            if (projectId == null) {
                throw new CmdLineException(parser, "-projectid must be specified.");
            } else if (projectId != null) {
                applicationContext.initialiseWithProjectId(projectId);
            }

            if (inputDirs.isEmpty()) {
                throw new CmdLineException(parser, "No input directory provided.");
            }
        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
            System.err.println("java -jar TestRunner [options] inputDir");
            parser.printUsage(System.err);

            return;
        }

        File file = new File(inputDirs.get(0));
        if (file.isDirectory()) {
            for (File fileInDirectory : file.listFiles()) {
                runTestPrivate(fileInDirectory);
            }
        } else {
            runTestPrivate(file);
        }
    }

    private void runTestPrivate(File file) throws Exception{
        if (!file.isFile() || !file.getName().endsWith(".json")) {
            logger.warn("Ignoring " + file.getAbsolutePath());
            return;
        }
        TestSetType testSet = deserialise(file);
        if(testSet==null){
            logger.warn("Null TestSet " + file.getAbsolutePath());
            return;
        }

        arrange(testSet);

        if(testSet.tests==null || testSet.tests.isEmpty()){
            logger.warn("TestSet has no tests " + file.getAbsolutePath());
            return;
        }

        for(TestType test:testSet.tests){
            assertForResult(test, act(test));
        }
    }

    protected abstract TestSetType deserialise(File file) throws Exception;

    protected abstract void arrange(TestSetType testSet) throws Exception;
    protected abstract ResultType act(TestType test) throws Exception;
    protected abstract void assertForResult(TestType test, ResultType resultType) throws Exception;

}
