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
package com.github.cafdataprocessing.corepolicy.database;

import com.github.cafdataprocessing.corepolicy.common.CorePolicyApplicationContext;
import com.github.cafdataprocessing.corepolicy.hibernate.HibernateProperties;
import liquibase.Contexts;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.logging.LogLevel;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.apache.commons.dbcp2.BasicDataSource;

import java.sql.SQLException;

/**
 * Application to create/manage a core policy database
 */
public class Application {
    /**
     * Main entry point, creates an instance of the Application class and calls run.
     *
     * @param args
     * @throws Exception
     */
    private boolean allowDBCreation;
    private boolean allowDBDeletion;
    private HibernateProperties databaseProperties;
    private LogLevel logLevel = LogLevel.WARNING;

    public static void main(String[] args) throws Exception {
        new Application().run(args);
    }

    private void run(String[] args) throws Exception {

        if (args.length > 0) {
            for (String arg : args) {
                switch (arg) {
                    case "-h":
                    case "--help": {
                        // Show help for args.
                        System.out.println("***************************************************************************");
                        System.out.println("* Database creation utility.  Supported arguments:");
                        System.out.println("* -c to allow creation of a new database during a fresh installation.");
                        System.out.println("* -v to output verbose logging.");
                        System.out.println("* ");
                        System.out.println("* PRE-PRODUCTION ONLY ENVIRONMENT OPTIONS.");
                        System.out.println("* -fd to force deletion of an existing database to perform a fresh installation.");
                        System.out.println("***************************************************************************");
                        return;
                    }
                    case "-c":
                        allowDBCreation = true;
                        break;
                    case "-fd":
                        allowDBDeletion = true;
                        break;
                    case "-v":
                        logLevel = LogLevel.INFO;
                        break;
                }
            }
        }

        applyRealSchema();
    }

    private void applyRealSchema() throws SQLException, LiquibaseException {
        try (CorePolicyApplicationContext applicationContext = new CorePolicyApplicationContext()) {
            applicationContext.refresh();

            databaseProperties = applicationContext.getBean(HibernateProperties.class);

            boolean dbExists = checkDBExists();

            if ( dbExists && allowDBDeletion )
            {
                // Ensure to drop the existing connection or we cannot delete in all DB impls.
                System.out.println();
                System.out.println("DB - Exists, and force deletion has been specified for: " + databaseProperties.getDatabaseName());
                System.out.println();

                BasicDataSource basicDataSourceNoDB = new BasicDataSource();
                basicDataSourceNoDB.setUrl(databaseProperties.getBaseConnectionString());
                basicDataSourceNoDB.setUsername(databaseProperties.getUser());
                basicDataSourceNoDB.setPassword(databaseProperties.getPassword());

                try (java.sql.Connection c = basicDataSourceNoDB.getConnection()) {
                    // ok we have a standard JDBC connection, create the db.
                    try (java.sql.Statement statement = c.createStatement()) {
                        statement.executeUpdate("DROP DATABASE " + databaseProperties.getDatabaseName());
                        System.out.println("DELETED database: " + databaseProperties.getDatabaseName());
                    }
                    dbExists = false;
                }
            }

            if (!dbExists) {

                // should we always do a creation, or make it so they have to specify an option.
                // Im adding an option, to prevent this accidentally running on a future upgrade scenario.
                if (!allowDBCreation) {
                    System.out.println();
                    System.out.println("DB - " + databaseProperties.getDatabaseName() + " does not exist, please specify the -c option to allow creation of the database.");
                    System.out.println();
                    throw new RuntimeException("Database - " + databaseProperties.getDatabaseName() + " does not exist or cannot be contacted.  Please specify the -c option to allow creation of the database if this is a new installation.");
                }
                System.out.println("about to perform DB installation from scratch.");

                BasicDataSource basicDataSourceNoDB = new BasicDataSource();
                basicDataSourceNoDB.setUrl(databaseProperties.getBaseConnectionString());
                basicDataSourceNoDB.setUsername(databaseProperties.getUser());
                basicDataSourceNoDB.setPassword(databaseProperties.getPassword());

                try (java.sql.Connection c = basicDataSourceNoDB.getConnection()) {
                    // ok we have a standard JDBC connection, create the db.
                    try (java.sql.Statement statement = c.createStatement()) {
                        statement.executeUpdate("CREATE DATABASE " + databaseProperties.getDatabaseName());
                        System.out.println("Created new database: " + databaseProperties.getDatabaseName());
                    }
                }
            }

            updateDB();
        }
    }

    private boolean checkDBExists() {

        try (BasicDataSource basicDataSource = new BasicDataSource()) {

            basicDataSource.setUrl(databaseProperties.getConnectionString());
            basicDataSource.setUsername(databaseProperties.getUser());
            basicDataSource.setPassword(databaseProperties.getPassword());

            try (java.sql.Connection c = basicDataSource.getConnection()) {
                return true;
            } catch (Exception e) {
                // got no DB with default connection string, we need to try to create it?
                return false;
            }
        } catch (SQLException e) {
            // dont care about exception on close during our check.
            e.printStackTrace();
        } catch (Exception e) {

        }
        return false;
    }

    private void updateDB() throws SQLException, LiquibaseException {
        System.out.println("About to perform DB update.");

        try (BasicDataSource basicDataSource = new BasicDataSource()) {

            basicDataSource.setUrl(databaseProperties.getConnectionString());
            basicDataSource.setUsername(databaseProperties.getUser());
            basicDataSource.setPassword(databaseProperties.getPassword());

            try (java.sql.Connection c = basicDataSource.getConnection()) {

                Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(c));

                // Check that the Database does indeed exist before we try to run the liquibase update.
            	Liquibase liquibase = new Liquibase("changelog1.xml", new ClassLoaderResourceAccessor(), database);
            	liquibase.getLog().setLogLevel(logLevel);
            	liquibase.update(new Contexts());
            	System.out.println("DB update finished.");
            }

        } catch (SQLException e) {
            // dont care about exception on close during our check.
            e.printStackTrace();
        }
    }
}
