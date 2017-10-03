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
package com.github.cafdataprocessing.corepolicy.repositories;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import com.github.cafdataprocessing.corepolicy.common.VersionNumber;
import com.github.cafdataprocessing.corepolicy.common.dto.ReleaseHistory;
import com.github.cafdataprocessing.corepolicy.repositories.v2.ExecutionContext;
import com.github.cafdataprocessing.corepolicy.repositories.v2.JdbcExecutionContext;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 */
public class JdbcRepositoryBase {

    // Update to match our SP numbers with each release.
    protected static final ReleaseHistory version = new ReleaseHistory(VersionNumber.getCurrentVersion());
    protected static final String DB_VERSION_STRING = String.format("v%d_%d",version.majorVersion,version.minorVersion);
    protected static final String SQL_CALLABLE_PREFIX = "CALL";

    private String toString(JsonNode json) {
        ObjectMapper mapper = new ObjectMapper();

        try {
            return json == null || json instanceof NullNode ? null: mapper.writeValueAsString(json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    protected void setRequired(CallableStatement callableStatement, String argumentName, Object value) {
        if(value instanceof JsonNode){
            value = toString((JsonNode)value);
        }

        if(value==null){
            throw new RuntimeException(argumentName + " is required.");
        }
        try {
            callableStatement.setObject(argumentName, value);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    protected void setRequired(PreparedStatement preparedStatement, int parameterIndex, Object value) {
        if(value instanceof JsonNode){
            value = toString((JsonNode)value);
        }

        if(value==null){
            throw new RuntimeException(parameterIndex + " is required param index.");
        }
        try {
            preparedStatement.setObject(parameterIndex, value);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    protected void set(CallableStatement callableStatement, String argumentName, Object value) {
        if(value instanceof JsonNode){
            value = toString((JsonNode)value);
        }

        try {
            if(value==null){
                callableStatement.setNull(argumentName, java.sql.Types.NULL);
            }
            else {
                callableStatement.setObject(argumentName, value);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    protected void set(PreparedStatement preparedStatement, int argumentPos, Object value) {
        if(value instanceof JsonNode){
            value = toString((JsonNode)value);
        }

        try {
            if(value==null){
                preparedStatement.setNull(argumentPos, java.sql.Types.NULL);
            }
            else {
                preparedStatement.setObject(argumentPos, value);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    protected void registerOut(CallableStatement callableStatement, String argumentName, int sqlType){
        try {
            callableStatement.registerOutParameter(argumentName, sqlType);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    protected Long getLong(ResultSet resultSet, String argumentName){
        try {
            Long result = resultSet.getLong(argumentName);
            if(resultSet.wasNull()){
                return null;
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    protected Integer getInt(ResultSet resultSet, String argumentName){
        try {
            Integer result = resultSet.getInt(argumentName);
            if(resultSet.wasNull()){
                return null;
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    protected Short getShort(ResultSet resultSet, String argumentName){
        try {
            Short result = resultSet.getShort(argumentName);
            if(resultSet.wasNull()){
                return null;
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    protected Boolean getBoolean(ResultSet resultSet, String argumentName){
        try {
            Boolean result = resultSet.getBoolean(argumentName);
            if(resultSet.wasNull()){
                return null;
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    protected static String createSQLSPStmnt(String stmntExecutionType, String spName, String spVersion, String spParamConvention)
    {
        // Statements are usually of form.
        // CALL sp_add_document_collection_vX.X(?,.....)");
        return String.format("%s %s_%s%s", stmntExecutionType, spName, spVersion, spParamConvention);
    }

    private JdbcExecutionContext getExecutionContext(ExecutionContext executionContext){

        //Dont particularly like that we assume a type, if we think of a better way to do this then change this.
        return ((JdbcExecutionContext)executionContext);
    }

    protected CallableStatement prepareCall(String addConditionSql, ExecutionContext executionContext){
        return getExecutionContext(executionContext).prepareCall(addConditionSql);
    }

    protected PreparedStatement getPreparedStatement(String addConditionSql, ExecutionContext executionContext){
        return getExecutionContext(executionContext).getPreparedStatement(addConditionSql);
    }
}
