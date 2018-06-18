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
package com.github.cafdataprocessing.corepolicy.hibernate;

import com.google.common.base.Strings;
import com.github.cafdataprocessing.corepolicy.common.PropertiesBase;
import com.github.cafdataprocessing.corepolicy.common.exceptions.BackEndRequestFailedCpeException;
import com.github.cafdataprocessing.corepolicy.common.exceptions.excpetionErrors.BackEndRequestFailedErrors;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 *
 */
@Configuration
@PropertySource("classpath:hibernate.properties")
@PropertySource(value = "file:${CAF_COREPOLICY_CONFIG}/hibernate.properties", ignoreResourceNotFound = true)
@PropertySource(value = "file:${CAF_COREPOLICY_CONFIG}/${CAF_COREPOLICY_HIBERNATE_CONFIG}/hibernate.properties", ignoreResourceNotFound = true)
public class HibernateProperties extends PropertiesBase {
    private final String dbNamePlaceholder = "<dbname>";

    public String getConnectionString(){
        // the connection string, is a combination of 2 properties.
        // The main hibernate formatted connection string, and the databasename properties.
        String baseConnectionString = getSetting("hibernate.connectionstring");
        if (Strings.isNullOrEmpty(baseConnectionString))
            return baseConnectionString;

        // otherwise, check for presence of the <dbname> place holder.
        if ( !baseConnectionString.contains(dbNamePlaceholder)) {
            return baseConnectionString;
        }

        // replace the placeholder with the real dbname, from here on, this property
        // is a required field - DO NOT DEFAULT this value.
        // Different dbms systems enforce different cases.
        return baseConnectionString.replace(dbNamePlaceholder, getDatabaseName());
    }

    public String getBaseConnectionString(){
        // this can be used to get the connection string, without the DB name present, in order to create
        // the DB.
        String baseConnectionString = getSetting("hibernate.connectionstring");
        if (Strings.isNullOrEmpty(baseConnectionString))
            return baseConnectionString;

        // otherwise, check for presence of the <dbname> place holder.
        if ( !baseConnectionString.contains(dbNamePlaceholder)) {
            throw new BackEndRequestFailedCpeException(BackEndRequestFailedErrors.IncorrectConfiguration, new Exception( "Unable to create a new DB without hibernate.connectionstring containing the <dbname> placeholder."));
        }

        // otherwise, read up to this point, and chop off the rest.
        int index = baseConnectionString.lastIndexOf(dbNamePlaceholder);

        return baseConnectionString.substring(0, index);
    }

    public String getUser(){
        return getSetting("hibernate.user");
    }

    public String getPassword(){
        return getSetting("hibernate.password");
    }

    public String getDatabaseName(){

        if ( Strings.isNullOrEmpty( getSetting("hibernate.databasename" ))){
            // invalid hibernate configuration.
            throw new BackEndRequestFailedCpeException(BackEndRequestFailedErrors.IncorrectConfiguration, new Exception("Invalid configuration - hibernate.databasename is not specified."));
        }

        return getSetting("hibernate.databasename");
    }

}
