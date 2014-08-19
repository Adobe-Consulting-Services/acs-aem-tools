/*
 * #%L
 * ACS AEM Tools Bundle
 * %%
 * Copyright (C) 2014 Adobe
 * %%
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
 * #L%
 */
package com.adobe.acs.tools.explain_query.impl;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.jackrabbit.api.jmx.QueryStatManagerMBean;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.commons.json.JSONArray;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.apache.sling.jcr.api.SlingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;
import javax.management.openmbean.CompositeData;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SlingServlet(
        label = "ACS AEM Tools - Explain Query Servlet",
        description = "End-point for Apache Jackrabbit Oak query explanations.",
        methods = { "GET", "POST" },
        resourceTypes = { "acs-tools/components/explain-query" },
        selectors = { "explain" },
        extensions = { "json" }
)
public class ExplainQueryServlet extends SlingAllMethodsServlet {
    private static final Logger log = LoggerFactory.getLogger(ExplainQueryServlet.class);

    private static final String SQL = "sql";

    private static final String SQL2 = "JCR-SQL2";

    private static final String XPATH = "xpath";

    private static final String[] LANGUAGES = new String[]{ SQL, SQL2, XPATH };

    private static final Pattern PROPERTY_INDEX_PATTERN = Pattern.compile("\\/\\*\\sproperty\\s([^\\s=]+)[=\\s]");
    private static final Pattern FILTER_PATTERN = Pattern.compile("\\[[^\\s]+\\]\\sas\\s\\[[^\\s]+\\]\\s\\/\\*\\sFilter\\(");

    @Reference
    private QueryStatManagerMBean queryStatManagerMBean;

    @Reference
    private SlingRepository slingRepository;

    @SuppressWarnings("unchecked")
    @Override
    protected final void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response)
            throws ServletException, IOException {
        final JSONObject json = new JSONObject();

        try {
            json.put("slowQueries",
                    this.compositeQueryDataToJSON((Collection<CompositeData>) queryStatManagerMBean
                            .getSlowQueries().values())
            );
        } catch (JSONException e) {
            log.error("Unable to serial Slow Queries into JSON: {}", e.getMessage());
        }

        try {
            json.put("popularQueries", this.compositeQueryDataToJSON((Collection<CompositeData>) queryStatManagerMBean
                    .getPopularQueries().values()));
        } catch (JSONException e) {
            log.error("Unable to serial Popular Queries into JSON: {}", e.getMessage());
        }

        response.setContentType("application/json");
        response.getWriter().print(json.toString());
    }

    @Override
    protected final void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response)
            throws ServletException, IOException {

        final ResourceResolver resourceResolver = request.getResourceResolver();

        String statement = StringUtils.removeStartIgnoreCase(request.getParameter("statement"), "EXPLAIN ");
        String language = request.getParameter("language");

        final Session session = resourceResolver.adaptTo(Session.class);
        final QueryManager queryManager;

        try {
            queryManager = session.getWorkspace().getQueryManager();

            final JSONObject json = new JSONObject();
            json.put("statement", statement);
            json.put("language", language);

            json.put("explain", explainQuery(queryManager, statement, language));

            if (request.getParameter("executionTime") != null
                    && StringUtils.equals("true", request.getParameter("executionTime"))) {
                json.put("timing", this.executionTimes(queryManager, statement, language));
            }

            response.setContentType("application/json");
            response.getWriter().print(json.toString());

        } catch (RepositoryException e) {
            log.error(e.getMessage());
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } catch (JSONException e) {
            log.error(e.getMessage());
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private JSONObject explainQuery(final QueryManager queryManager, final String statement,
                                    final String language) throws RepositoryException, JSONException {
        final JSONObject json = new JSONObject();
        final Query query = queryManager.createQuery("explain " + statement, language);

        final QueryResult queryResult = query.execute();
        final RowIterator rows = queryResult.getRows();
        final Row firstRow = rows.nextRow();

        final String plan = firstRow.getValue("plan").getString();
        json.put("plan", plan);



        final JSONArray propertyIndexes = new JSONArray();

        final Matcher propertyMatcher = PROPERTY_INDEX_PATTERN.matcher(plan);
        /* Property Index */
        while (propertyMatcher.find()) {
            final String match = propertyMatcher.group(1);
            if (StringUtils.isNotBlank(match)) {
                propertyIndexes.put(StringUtils.stripToEmpty(match));
            }
        }

        if (propertyIndexes.length() > 0) {
            json.put("propertyIndexes", propertyIndexes);
        }

        final Matcher filterMatcher = FILTER_PATTERN.matcher(plan);
        if(filterMatcher.find()) {
            /* Filter (nodeType index) */

            propertyIndexes.put("nodeType");
            json.put("propertyIndexes", propertyIndexes);
            json.put("slow", true);
        }

        if (StringUtils.contains(plan, " /* traverse ")) {
            /* Traversal */
            json.put("traversal", true);
            json.put("slow", true);
        }

        if (StringUtils.contains(plan, " /* aggregate ")) {
            /* Aggregate - Fulltext */
            json.put("aggregate", true);
        }

        return json;
    }

    private JSONObject executionTimes(final QueryManager queryManager, final String statement,
                                      final String language) throws RepositoryException, JSONException {
        final JSONObject json = new JSONObject();

        final Query query = queryManager.createQuery(statement, language);

        long start = System.currentTimeMillis();
        final QueryResult queryResult = query.execute();
        long executionTime = System.currentTimeMillis() - start;

        start = System.currentTimeMillis();
        queryResult.getNodes();
        long getNodesTime = System.currentTimeMillis() - start;

        json.put("executeTime", executionTime);
        json.put("getNodesTime", getNodesTime);
        json.put("totalTime", executionTime + getNodesTime);

        return json;
    }


    private JSONArray compositeQueryDataToJSON(Collection<CompositeData> queries) throws JSONException {
        final JSONArray jsonArray = new JSONArray();

        for (CompositeData query : queries) {
            Long duration = (Long) query.get("duration");
            Integer occurrenceCount = (Integer) query.get("occurrenceCount");
            String language = (String) query.get("language");
            String statement = (String) query.get("statement");

            if (!ArrayUtils.contains(LANGUAGES, language)) {
                // Not a supported language
                continue;
            } else if (StringUtils.startsWithIgnoreCase(statement, "EXPLAIN ")
                    || StringUtils.startsWithIgnoreCase(statement, "MEASURE ")) {
                // Don't show EXPLAIN or MEASURE queries
                continue;
            }

            final JSONObject json = new JSONObject();

            try {
                json.put("duration", duration);
                json.put("language", language);
                json.put("occurrenceCount", occurrenceCount);
                json.put("statement", statement);

                jsonArray.put(json);
            } catch (JSONException e) {
                log.warn("Could not add query to results [ {} ]", statement);
                continue;
            }
        }

        return jsonArray;
    }
}
