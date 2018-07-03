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
package com.github.cafdataprocessing.corepolicy.common.shared;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Allows temporary modification of environment properties and resetting once complete
 */
public class TemporaryEnvChanger implements AutoCloseable {

    private String envName;
    private String origEnvValue;
    private boolean haveGotValue = false;
    private boolean noEnvKeyPresent = false;
    private final static Logger logger = LoggerFactory.getLogger(TemporaryEnvChanger.class);

    public TemporaryEnvChanger( String envName, String newValue )
    {
        this.envName = envName;

        setValue(newValue);
    }

    synchronized
    public String getOriginalValue()
    {
        if ( haveGotValue )
        {
            return origEnvValue;
        }

        if (! System.getenv().containsKey(envName) ) {
            noEnvKeyPresent = true;
            logger.debug("Original key: " + envName  + " has no value present.");
        }
        else
        {
            // Get the original property and cache it off for reset later on.
            origEnvValue = System.getProperty(envName);
            logger.debug("Original key: " + envName +  " value: " + origEnvValue);
        }

        // Ensure to indicate we have done this already!
        haveGotValue = true;

        return origEnvValue;
    }

    synchronized
    public void resetOriginalValue()
    {
        // ensure it is only reset once.
        if ( haveGotValue ) {
            haveGotValue = false;

            if ( noEnvKeyPresent )
            {
                System.clearProperty(envName);
                return;
            }

            System.setProperty(envName, origEnvValue);
        }
    }

    public void setValue( String newValue )
    {
        // ensure we have original value
        getOriginalValue();

        if ( newValue == null ) {
            System.clearProperty(envName);
            return;
        }

        System.setProperty( envName, newValue );
    }


    @Override
    public void close()  {

        // call reset in close!
        resetOriginalValue();
    }
}
