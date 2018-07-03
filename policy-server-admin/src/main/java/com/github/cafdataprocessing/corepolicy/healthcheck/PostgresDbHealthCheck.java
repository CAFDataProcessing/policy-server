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
package com.github.cafdataprocessing.corepolicy.healthcheck;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class PostgresDbHealthCheck
{
    private static final Logger LOG = LoggerFactory.getLogger(PostgresDbHealthCheck.class);
    private final static String SQL_STATEMENT = "SELECT * FROM information_schema.tables LIMIT 1";

    private PostgresDbHealthCheck()
    {
    }

    public static boolean checkDBExists()
    {

        try {
            final String postgresUser = System.getenv("hibernate.user");
            final String postgresPass = System.getenv("hibernate.password");
            final String postgresDatabase = System.getenv("hibernate.databasename");
            final String postgresUrl = System.getenv("hibernate.connectionstring");
            final String postgresDriver = "org.postgresql.Driver";
            try(final Connection dbConn = openConnection(postgresDatabase, postgresUrl,
                    postgresDriver, postgresUser, postgresPass)){
                return executeSqlStatement(SQL_STATEMENT, dbConn);
            }
        } catch (final Exception ex) {
            LOG.info(ex.getMessage(), ex);
            return false;
        }
    }

    private static Connection openConnection(final String databaseName, final String postgresURL,
                                             final String postgresDriver, final String postgresUsername,
                                             final String postgresPassword) throws SQLException, ClassNotFoundException
    {
        Class.forName(postgresDriver);
        final String postgresConnectionString = postgresURL.replace("<dbname>", databaseName);
        LOG.info("Postgres connection");
        return DriverManager.getConnection(postgresConnectionString, postgresUsername, postgresPassword);
    }

    private static boolean executeSqlStatement(final String sqlStatement, final Connection dbConn) throws SQLException
    {
        try (final Statement cmdStatement = dbConn.createStatement()) {
            final boolean response = cmdStatement.execute(sqlStatement);
            return response;
        } catch (final SQLException ex) {
            LOG.debug(ex.getMessage(), ex);
            throw ex;
        }
    }
}
