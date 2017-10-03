/*
 * Copyright 2015-2017 EntIT Software LLC, a Micro Focus company.
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

import com.github.cafdataprocessing.corepolicy.common.ApiProperties;
import org.junit.internal.AssumptionViolatedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import static org.hamcrest.CoreMatchers.is;

/**
 * A wrapper class around the junit Assume logic.
 * This provides the ability to conditionally skip / ignore tests but to provide
 * the reason why and JIRA number which can optionally be logged out to a central location.
 */
public class Assume implements ApplicationContextAware {

    private static long testCount = 0;
    protected static Logger logger = LoggerFactory.getLogger(Assume.class);
    private static ApplicationContext applicationContext;

    // we have varying reasons why something may be skipped, consider long and hard before
    // adding new reasons.
    public enum AssumeReason {

        UNCLASSIFIED, // default used if its not specified with the reason text.  So its clear in report if someone just missed this.
        BUG,
        DEBT, // to be done ( technical debt. )
        BY_DESIGN, // skipped by design e.g. isome tests should only be ran in certain modes
    }

    // Maps for Assume Reason to loglevel.
    /*
     * UNCLASSIFIED -> Error
     * BUG -> Warn
     * DEBT -> Info
     * BY_DESIGN -> Debug
      */

    @Override
    public void setApplicationContext(ApplicationContext applicationContext)
    {
        this.applicationContext = applicationContext;
    }

    /**
     * If called with an expression evaluating to {@code false}, the test will halt and be ignored.
     */
    public static void assumeTrue(boolean b) throws Exception {

        throw new Exception("DO NOT USE THIS assumeTrue METHOD, AS PROVIDES NO EXPLANATION OR JIRA");
    }

    /**
     * If called with an expression evaluating to {@code false}, the test will halt and be ignored.
     *
     * @param b If <code>false</code>, the method will attempt to stop the test and ignore it by
     * throwing {@link AssumptionViolatedException}.
     * @param message A message to pass to {@link AssumptionViolatedException}.
     */
    public static void assumeTrue(String message, boolean b, ApplicationContext applicationContext) {

        // defaulting anyone who doesn't specify a reason to unclassified.
        assumeTrue(AssumeReason.UNCLASSIFIED, message, b, applicationContext);
    }


    public static void assumeTrue(AssumeReason reason, String message, boolean b, ApplicationContext applicationContext) {

        // defaulting to not specify the test name / jira.
        assumeTrue(reason, null, null, message, b, applicationContext);
    }

    /**
     * Useful override to specify the testName, and a reason but no jira - used for BY_DESIGN mostly.
     * @param reason
     * @param testName
     * @param message
     * @param b
     */
    public static void assumeTrue(AssumeReason reason, String testName, String message, boolean b, ApplicationContext applicationContext) {

        assumeTrue(reason, testName, null, message, b, applicationContext);
    }

    /**
     *
     * @param reason
     * @param testName
     * @param jira
     * @param message
     * @param b
     * @param context
     */
    public static void assumeTrue(AssumeReason reason, String testName, String jira, String message, boolean b, ApplicationContext context) {

        try {
            org.junit.Assume.assumeTrue(message, b);
        }
        catch(AssumptionViolatedException ex)
        {
            // if the assumption is violated, log it now!
            LogCentrally( reason, testName, jira, message, context );

            throw ex;
        }

    }
    /**
     * The inverse of {@link #assumeTrue(boolean)}.
     */
    public static void assumeFalse(boolean b)throws Exception  {
        throw new Exception("DO NOT USE THIS assumeFalse METHOD, AS PROVIDES NO EXPLANATION OR JIRA");
    }

    public static void assumeFalse(String message, boolean b, ApplicationContext applicationContext) {

        // defaulting anyone who doesn't specify a reason to unclassified.
        assumeFalse(AssumeReason.UNCLASSIFIED, null, null, message, b, applicationContext);
    }

    public static void assumeFalse(AssumeReason reason, String message, boolean b, ApplicationContext applicationContext) {

        // defaulting to not specify the test name / jira.
        assumeFalse(reason, null, null, message, b, applicationContext);
    }

    /**
     * Useful override to specify the testName, and a reason but no jira - used for BY_DESIGN mostly.
     * @param reason
     * @param testName
     * @param message
     * @param b
     */
    public static void assumeFalse(AssumeReason reason, String testName, String message, boolean b, ApplicationContext applicationContext) {

        assumeFalse(reason, testName, null, message, b, applicationContext);
    }


    /***
     *
     * @param reason
     * @param testName
     * @param jira
     * @param message
     * @param b
     * @param context
     */
    public static void assumeFalse(AssumeReason reason, String testName, String jira, String message, boolean b, ApplicationContext context ) {

        try {
            org.junit.Assume.assumeFalse(message, b);
        }
        catch(AssumptionViolatedException ex)
        {
            // if the assumption is violated, log it now!
            LogCentrally( reason, testName, jira, message, context );

            throw ex;
        }
    }

    private static void LogCentrally( AssumeReason reason, String testName, String jira, String message )
    {
        LogCentrally( reason, testName, jira, message, null );
    }

    private static void LogCentrally( AssumeReason reason, String testName, String jira, String message, ApplicationContext context )
    {
        ApiProperties apiProperties = null;

        try {
            if ( context != null )
            {
                apiProperties = context.getBean(ApiProperties.class);
            }
            else if ( applicationContext == null )
            {
                // just log dont throw as it then appears in error log, something we dont want!!
                logger.debug("Warning only: No application context specified, to obtain api environment information from...");
            }
            else {
                apiProperties = applicationContext.getBean(ApiProperties.class);
            }
        }catch(Exception ex) {
            // ignore all we get. we could log it for the fun of it.
            logger.error("Unexpected exception in logCentrally.", ex);
        }

        long testCounter = getAndIncrementTestCount();

        // format the output depending on what is required.
        String logOutput = String.format("Skipped Test(%s): {%s}   JIRA: {%s}   Description: {%s}  Current Env: api.mode={%s}  api.direct.repository={%s}",
                testCounter,
                testName == null ? "Unspecified" : testName,
                jira == null ? "Unspecified" : jira,
                message == null ? "Unspecified" : message,
                apiProperties == null ? "Unspecified" : apiProperties.getMode(),
                apiProperties == null ? "Unspecified" : apiProperties.getRepository());

        switch( reason )
        {
            case UNCLASSIFIED:
                logger.error(logOutput);
                break;
            case BUG:
                logger.warn(logOutput);
                break;
            case DEBT:
                logger.info(logOutput);
                break;
            case BY_DESIGN:
                // we dont want the JIRA entry for the bydesign output. So change it.
                logOutput = String.format("Skipped Test(%s): {%s} bydesign.  Description: {%s}  Current Env: api.mode={%s}  api.direct.repository={%s}",
                        testCounter,
                        testName == null ? "Unspecified" : testName,
                        message == null ? "Unspecified" : message,
                        apiProperties == null ? "Unspecified" : apiProperties.getMode(),
                        apiProperties == null ? "Unspecified" : apiProperties.getRepository());

                logger.debug(logOutput);
                break;
            default:
                logger.error(logOutput);
                throw new RuntimeException("Unknown reason given: "+ reason.toString() );
        }


    }

    synchronized
    private static long getAndIncrementTestCount() {
        long currentTestCount = testCount;

        testCount++;

        return currentTestCount;
    }
}
