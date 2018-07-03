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

import com.google.gson.Gson;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Healthcheck extends HttpServlet
{
    private final static Logger LOG = LoggerFactory.getLogger(Healthcheck.class);

    @Override
    public void doGet(final HttpServletRequest req, final HttpServletResponse res) throws IOException
    {
        //  Construct response payload.
        final Map<String, Map<String, String>> statusResponseMap = new HashMap<>();
        final boolean isHealthy = checkPostgres(statusResponseMap);
        final Gson gson = new Gson();
        final String responseBody = gson.toJson(statusResponseMap);

        //  Get response body bytes.
        final byte[] responseBodyBytes = responseBody.getBytes(StandardCharsets.UTF_8);

        //  Set content type and length.
        res.setContentType("application/json");
        res.setContentLength(responseBodyBytes.length);

        //  Add CacheControl header to specify directives for caching mechanisms.
        res.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");

        //  Set status code.
        if (isHealthy) {
            res.setStatus(200);
        } else {
            res.setStatus(500);
        }

        //  Output response body.
        try (final ServletOutputStream out = res.getOutputStream()) {
            out.write(responseBodyBytes);
            out.flush();
        }
    }

    private static boolean checkPostgres(final Map<String, Map<String, String>> statusResponseMap)
    {
        try {
            final boolean healthy = PostgresDbHealthCheck.checkDBExists();
            updateStatusResponseWithHealthOfComponent(statusResponseMap, healthy, null, "database");
            return healthy;
        } catch (final Exception ex) {
            LOG.error("Postgres database health check reporting unhealthy", ex.toString());
            updateStatusResponseWithHealthOfComponent
                    (statusResponseMap, false, ex.toString(), "database");
            return false;
        }
    }

    private static boolean updateStatusResponseWithHealthOfComponent(
        final Map<String, Map<String, String>> statusResponseMap, final boolean isHealthy, final String message,
        final String component)
    {
        final Map<String, String> healthMap = new HashMap<>();
        if (isHealthy) {
            healthMap.put("healthy", "true");
        } else {
            healthMap.put("healthy", "false");
        }
        if (message != null) {
            healthMap.put("message", message);
        }
        statusResponseMap.put(component, healthMap);
        return isHealthy;
    }

}
