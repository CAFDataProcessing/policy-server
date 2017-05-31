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
package com.github.cafdataprocessing.corepolicy.common.shared;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.InvalidParameterException;

/**
 * Custom logger class for core policy
 */
public class CorePolicyLogger implements AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(CorePolicyLogger.class);
    private Level logLevel;
    private String methodSignature;
    private DateTime entered;
    private DateTime exited;

    public CorePolicyLogger( ) {
        this.logLevel = Level.TRACE;
        this.methodSignature = "Default: ";
        this.entered = DateTime.now();

        logInternal(getTime(entered) + " Entered: " + methodSignature);
    }

    public CorePolicyLogger( String newSignature ) {
        this.logLevel = Level.TRACE;
        this.methodSignature = newSignature;
        this.entered = DateTime.now();

        logInternal(getTime(entered) + " Entered: " + methodSignature);
    }

    public CorePolicyLogger( String newSignature, Level logLevel) {
        this.logLevel = logLevel;
        this.methodSignature = newSignature;
        this.entered = DateTime.now();
    }

    /**
     * Useful helper method which forces the exit time to be taken
     * now and the logging put out.  This is useful when you wish to force out
     * a log message, before the autoclose.
     */
    public void exitNow() {
        exitAndLog();
    }

    /**
     * Logs at appropriate logger level, and outputs timestamp.
     * @param logString
     */
    public void log(String logString) {
        logInternal(getTime(DateTime.now()) + " : " + logString);
    }

    private void logInternal(String logString) {
        switch(logLevel.level) {
            case Level.TRACE_INT:
                logger.trace(logString);
                return;
            case Level.DEBUG_INT:
                logger.debug(logString);
                return;
            case Level.ERROR_INT:
                logger.error(logString);
                return;
            case Level.WARN_INT:
                logger.warn(logString);
                return;
            case Level.INFO_INT:
                logger.info(logString);
                return;
            case Level.ALL_INT:
                logger.trace(logString);
                logger.debug(logString);
                logger.error(logString);
                logger.warn(logString);
                logger.info(logString);
                return;
            default:
                throw new InvalidParameterException("Unknown Logging Level" + logLevel);
        }
    }

    @Override
    public void close() {
        // log our the exiting messsage now.
        exitAndLog();
    }


    public Level getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(Level logLevel) {
        this.logLevel = logLevel;
    }

    public DateTime getEntered() {
        return entered;
    }

    public void setEntered(DateTime entered) {
        this.entered = entered;
    }

    public DateTime getExit() {
        return exited;
    }

    public void setExit(DateTime exit) {
        this.exited = exit;
    }

    private void exitAndLog() {
        checkExited();

        logInternal(getTime(exited) + " Exited: " + methodSignature + "  Duration: " + getDuration());
    }

    private void checkExited() {
        if ( exited == null ) {
            exited = DateTime.now();
        }
    }

    private String getTime(DateTime dateTime)
    {
        // Changed this to return blank as our logging in log4j is inserting the timestamp at beginning of each line,
        // if this changes, or we want this info out to console - put this back in, in a controllable fashion.
        return "";
        // return dateTime.toString();
    }

    private String getDuration(){
        if ( entered == null || exited == null ){
            return " No time?";
        }

        if ( entered.getMillis() <= exited.getMillis() )
        {
            return exited.getMillis() - entered.getMillis() + " ms";
        }

        // entered is after exited?
        return entered.getMillis() - exited.getMillis() + " ms";
    }
}

