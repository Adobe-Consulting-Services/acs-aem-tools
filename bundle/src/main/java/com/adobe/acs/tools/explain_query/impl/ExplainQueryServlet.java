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

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.classic.turbo.TurboFilter;
import ch.qos.logback.core.Layout;
import ch.qos.logback.core.spi.FilterReply;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.PropertyUnbounded;
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
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.apache.sling.jcr.api.SlingRepository;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.Marker;

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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SlingServlet(
        label = "ACS AEM Tools - Explain Query Servlet",
        description = "End-point for Apache Jackrabbit Oak query explanations.",
        methods = { "GET", "POST" },
        resourceTypes = { "acs-tools/components/explain-query" },
        selectors = { "explain" },
        extensions = { "json" },
        metatype = true
)
public class ExplainQueryServlet extends SlingAllMethodsServlet {
    private static final Logger log = LoggerFactory.getLogger(ExplainQueryServlet.class);

    private static final String SQL = "sql";

    private static final String SQL2 = "JCR-SQL2";

    private static final String XPATH = "xpath";

    private static final String[] LANGUAGES = new String[]{ SQL, SQL2, XPATH };

    private static final Pattern PROPERTY_INDEX_PATTERN = Pattern.compile("\\/\\*\\sproperty\\s([^\\s=]+)[=\\s]");
    private static final Pattern FILTER_PATTERN = Pattern.compile("\\[[^\\s]+\\]\\sas\\s\\[[^\\s]+\\]\\s\\/\\*\\sFilter\\(");

    @Property(label = "Query Logger Names",
            description = "Logger names from which logs need to be collected while a query is executed",
            unbounded = PropertyUnbounded.ARRAY,
            value = {
                    "org.apache.jackrabbit.oak.query",
                    "org.apache.jackrabbit.oak.plugins.index"
            }
    )
    private static final String PROP_LOGGER_NAMES = "loggerNames";

    private static final String DEFAULT_PATTERN = "\"%d{dd.MM.yyyy HH:mm:ss.SSS} *%level* %logger %msg%n";

    @Property(label = "Log Pattern",
            description = "Message Pattern for formatting the log messages",
            value = DEFAULT_PATTERN
    )
    private static final String PROP_MSG_PATTERN = "logPattern";

    @Reference
    private QueryStatManagerMBean queryStatManagerMBean;

    @Reference
    private SlingRepository slingRepository;

    private QueryLogCollector logCollector;

    private ServiceRegistration logCollectorRegistration;

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

    @Activate
    private void activate(Map<String, ?> config, BundleContext context){
        String[] loggerNames = PropertiesUtil.toStringArray(config.get(PROP_LOGGER_NAMES), null);

        if (loggerNames != null){
            String pattern = PropertiesUtil.toString(config.get(PROP_MSG_PATTERN), DEFAULT_PATTERN);
            logCollector = new QueryLogCollector(loggerNames, pattern);
            logCollectorRegistration = context.registerService(TurboFilter.class.getName(), logCollector, null);
        }
    }

    @Deactivate
    private void deactivate(){
        if (logCollectorRegistration != null){
            logCollectorRegistration.unregister();
        }
    }

    private JSONObject explainQuery(final QueryManager queryManager, final String statement,
                                    final String language) throws RepositoryException, JSONException {
        final JSONObject json = new JSONObject();
        final String collectorKey = startCollection();
        final QueryResult queryResult;

        try {
            final Query query = queryManager.createQuery("explain " + statement, language);
            queryResult = query.execute();
        }
        finally {
            if (logCollector != null) {
                json.put("logs", logCollector.getLogs(collectorKey));
            }
            stopCollection(collectorKey);
        }

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

    private String startCollection(){
        final String collectorKey = UUID.randomUUID().toString();
        MDC.put(QueryLogCollector.COLLECTOR_KEY, collectorKey);
        return collectorKey;
    }

    private void stopCollection(String key){
        MDC.remove(key);
        if (logCollector != null){
            logCollector.stopCollection(key);
        }
    }

    private static class QueryLogCollector extends TurboFilter {
        //MDC key defined in org.apache.jackrabbit.oak.query.QueryEngineImpl
        private static final String QUERY_ANALYZE = "oak.query.analyze";
        private static final String COLLECTOR_KEY = "collectorKey";

        private final String[] loggerNames;
        private final Layout<ILoggingEvent> layout;
        private final Map<String, List<ILoggingEvent>> logEvents
                = new ConcurrentHashMap<String, List<ILoggingEvent>>();

        private QueryLogCollector(String[] loggerNames, String pattern) {
            this.loggerNames = loggerNames;
            this.layout = createLayout(pattern);
        }

        @Override
        public FilterReply decide(Marker marker, ch.qos.logback.classic.Logger logger,
                                  Level level, String format, Object[] params, Throwable t) {
            if (MDC.get(QUERY_ANALYZE) == null){
                return FilterReply.NEUTRAL;
            }

            String collectorKey = MDC.get(COLLECTOR_KEY);

            if (collectorKey == null){
                return FilterReply.NEUTRAL;
            }

            if (!configuredLogger(logger.getName())){
                return FilterReply.NEUTRAL;
            }

            //isXXXEnabled call. Accept the call to allow actual message to be
            //logged
            if (format == null){
                return FilterReply.ACCEPT;
            }

            ILoggingEvent logEvent = new LoggingEvent(ch.qos.logback.classic.Logger.FQCN,
                    logger, level, format, t, params);
            log(collectorKey, logEvent);

            //Return NEUTRAL to allow normal logging of message depending on level
            return FilterReply.NEUTRAL;
        }

        public List<String> getLogs(String collectorKey){
            List<ILoggingEvent> eventList = logEvents.get(collectorKey);
            if (eventList == null){
                return Collections.emptyList();
            }
            List<String> result = new ArrayList<String>(eventList.size());
            for (ILoggingEvent e : eventList){
                result.add(layout.doLayout(e));
            }
            return result;
        }

        public void stopCollection(String key) {
            logEvents.remove(key);
        }

        private void log(String collectorKey, ILoggingEvent e) {
            List<ILoggingEvent> eventList = logEvents.get(collectorKey);
            if (eventList == null){
                eventList = new ArrayList<ILoggingEvent>();
                logEvents.put(collectorKey, eventList);
            }
            eventList.add(e);
        }

        private boolean configuredLogger(String name) {
            for (String loggerName : loggerNames){
                if (name.startsWith(loggerName)){
                    return true;
                }
            }
            return false;
        }

        private static Layout<ILoggingEvent> createLayout(String pattern) {
            PatternLayout pl = new PatternLayout();
            pl.setPattern(pattern);
            pl.setOutputPatternAsHeader(false);
            pl.setContext((LoggerContext) LoggerFactory.getILoggerFactory());
            pl.start();
            return pl;
        }
    }
}
