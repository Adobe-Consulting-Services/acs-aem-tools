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
import com.adobe.acs.commons.util.OsgiPropertyUtil;
import com.day.cq.search.PredicateGroup;
import com.day.cq.search.QueryBuilder;
import com.day.cq.search.result.SearchResult;
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
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.Marker;

import javax.jcr.NodeIterator;
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

    //MDC key defined in org.apache.jackrabbit.oak.query.QueryEngineImpl
    private static final String OAK_QUERY_ANALYZE = "oak.query.analyze";

    private static final String SQL = "sql";

    private static final String SQL2 = "JCR-SQL2";

    private static final String XPATH = "xpath";

    private static final String QUERY_BUILDER = "queryBuilder";

    private static final String[] LANGUAGES = new String[]{SQL, SQL2, XPATH};

    private static final Pattern PROPERTY_INDEX_PATTERN =
            Pattern.compile("\\/\\*\\sproperty\\s([^\\s=]+)[=\\s]");

    private static final Pattern FILTER_PATTERN =
            Pattern.compile("\\[[^\\s]+\\]\\sas\\s\\[[^\\s]+\\]\\s\\/\\*\\sFilter\\(");

    @Property(label = "Query Logger Names",
            description = "Logger names from which logs need to be collected while a query is executed. "
                    + "Provide in the format '<package-name>=<mdc-filter-name>' where <mdc-filter-name>' is optional.",
            unbounded = PropertyUnbounded.ARRAY,
            value = {
                    "org.apache.jackrabbit.oak.query=" + OAK_QUERY_ANALYZE,
                    "org.apache.jackrabbit.oak.plugins.index=" + OAK_QUERY_ANALYZE,
                    "com.day.cq.search.impl.builder.QueryImpl"
            }
    )
    private static final String PROP_LOGGER_NAMES = "log.logger-names";

    private static final String DEFAULT_PATTERN = "%msg%n";

    @Property(label = "Log Pattern",
            description = "Message Pattern for formatting the log messages. [ Default: " + DEFAULT_PATTERN + " ]",
            value = DEFAULT_PATTERN
    )
    private static final String PROP_MSG_PATTERN = "log.pattern";

    private static final int DEFAULT_LIMIT = 100;

    @Property(label = "Log Limit",
            description = "Number of log message which should be collected in memory",
            intValue = DEFAULT_LIMIT
    )
    private static final String PROP_LOG_COUNT_LIMIT = "log.message-count-limit";

    @Reference
    private QueryStatManagerMBean queryStatManagerMBean;

    @Reference
    private QueryBuilder queryBuilder;

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

        try {

            // Mark this thread as an Explain Query thread for TurboFiltering
            EXPLAIN_QUERY_THREAD.set(true);

            final JSONObject json = new JSONObject();
            json.put("statement", statement);
            json.put("language", language);

            json.put("explain", explainQuery(session, statement, language));

            boolean collectExecutionTime = "true".equals(
                    StringUtils.defaultIfEmpty(request.getParameter("executionTime"), "false"));

            boolean collectCount = "true".equals(
                    StringUtils.defaultIfEmpty(request.getParameter("resultCount"), "false"));

            if (collectExecutionTime) {
                json.put("heuristics", this.getHeuristics(session, statement, language, collectCount));
            }

            response.setContentType("application/json");
            response.getWriter().print(json.toString());

        } catch (RepositoryException e) {
            log.error(e.getMessage());
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } catch (JSONException e) {
            log.error(e.getMessage());
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } finally {
            EXPLAIN_QUERY_THREAD.remove();
        }
    }

    private JSONObject explainQuery(final Session session, final String statement,
                                    final String language) throws RepositoryException, JSONException {
        final QueryManager queryManager = session.getWorkspace().getQueryManager();
        final JSONObject json = new JSONObject();
        final String collectorKey = startCollection();
        final QueryResult queryResult;
        final String effectiveLanguage;
        final String effectiveStatement;

        if (language.equals(QUERY_BUILDER)) {
            effectiveLanguage = XPATH;
            final String[] lines = StringUtils.split(statement, '\n');
            final Map<String, String> params = OsgiPropertyUtil.toMap(lines, "=", false, null, true);

            final com.day.cq.search.Query query = queryBuilder.createQuery(PredicateGroup.create(params), session);
            effectiveStatement = query.getResult().getQueryStatement();
        } else {
            effectiveStatement = statement;
            effectiveLanguage = language;
        }
        try {
            final Query query = queryManager.createQuery("explain " + effectiveStatement, effectiveLanguage);
            queryResult = query.execute();
        } finally {
            if (logCollector != null) {
                List<String> logs = logCollector.getLogs(collectorKey);
                json.put("logs", logCollector.getLogs(collectorKey));

                if (logs.size() == logCollector.msgCountLimit) {
                    json.put("logsTruncated", true);
                }
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
        if (filterMatcher.find()) {
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

    private JSONObject getHeuristics(final Session session,
                                     final String statement,
                                     final String language,
                                     final boolean getCount) throws RepositoryException, JSONException {

        int count = 0;

        final QueryManager queryManager = session.getWorkspace().getQueryManager();
        final JSONObject json = new JSONObject();

        long executionTime;
        long getNodesTime;
        long countTime = 0L;

        if (language.equals(QUERY_BUILDER)) {
            final String[] lines = StringUtils.split(statement, '\n');
            final Map<String, String> params = OsgiPropertyUtil.toMap(lines, "=", false, null, true);

            final com.day.cq.search.Query query = queryBuilder.createQuery(PredicateGroup.create(params), session);
            long start = System.currentTimeMillis();
            SearchResult result = query.getResult();
            executionTime = System.currentTimeMillis() - start;

            start = System.currentTimeMillis();
            result.getNodes();
            getNodesTime = System.currentTimeMillis() - start;


            if (getCount) {
                count = result.getHits().size();
                countTime = System.currentTimeMillis() - start;
            }

        } else {
            final Query query = queryManager.createQuery(statement, language);

            long start = System.currentTimeMillis();
            final QueryResult queryResult = query.execute();
            executionTime = System.currentTimeMillis() - start;

            start = System.currentTimeMillis();
            queryResult.getNodes();
            getNodesTime = System.currentTimeMillis() - start;


            if (getCount) {
                final NodeIterator nodes = queryResult.getNodes();

                while (nodes.hasNext()) {
                    nodes.next();
                    count++;
                }

                countTime = System.currentTimeMillis() - start;
            }
        }

        json.put("executeTime", executionTime);
        json.put("getNodesTime", getNodesTime);

        if (getCount) {
            json.put("count", count);
            json.put("countTime", countTime);
        }

        json.put("totalTime", executionTime + getNodesTime + countTime);

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

    private String startCollection() {
        final String collectorKey = UUID.randomUUID().toString();
        MDC.put(QueryLogCollector.COLLECTOR_KEY, collectorKey);
        return collectorKey;
    }

    private void stopCollection(String key) {
        MDC.remove(key);
        if (logCollector != null) {
            logCollector.stopCollection(key);
        }
    }

    private static boolean checkMDCSupport(BundleContext context) {
        //MDC support is present since 1.0.9                     s
        Version versionWithMDCSupport = new Version(1, 0, 9);
        for (Bundle b : context.getBundles()) {
            if ("org.apache.jackrabbit.oak-core".equals(b.getSymbolicName())) {
                return versionWithMDCSupport.compareTo(b.getVersion()) <= 0;
            }
        }

        //By default it is assumed that MDC support is present
        return true;
    }

    @Activate
    private void activate(Map<String, ?> config, BundleContext context) {

        Map<String, String> loggers = OsgiPropertyUtil.toMap(PropertiesUtil.toStringArray(
                config.get(PROP_LOGGER_NAMES), new String[0]), "=", true, null);

        if (loggers != null && !loggers.isEmpty()) {
            String pattern = PropertiesUtil.toString(config.get(PROP_MSG_PATTERN), DEFAULT_PATTERN);
            int msgCountLimit = PropertiesUtil.toInteger(config.get(PROP_LOG_COUNT_LIMIT), DEFAULT_LIMIT);
            logCollector = new QueryLogCollector(loggers, pattern, msgCountLimit, checkMDCSupport(context));
            logCollectorRegistration = context.registerService(TurboFilter.class.getName(), logCollector, null);
        }
    }

    @Deactivate
    private void deactivate() {
        if (logCollectorRegistration != null) {
            logCollectorRegistration.unregister();
        }
    }

    private static final class QueryLogCollector extends TurboFilter {
        private static final String COLLECTOR_KEY = "collectorKey";

        private final Map<String, String> loggers;

        private final Layout<ILoggingEvent> layout;

        private final int msgCountLimit;

        private final Map<String, List<ILoggingEvent>> logEvents
                = new ConcurrentHashMap<String, List<ILoggingEvent>>();

        private final boolean mdcEnabled;

        private QueryLogCollector(Map<String, String> loggers, String pattern, int msgCountLimit, boolean
                mdcEnabled) {
            this.loggers = loggers;
            this.msgCountLimit = msgCountLimit;
            this.layout = createLayout(pattern);
            this.mdcEnabled = mdcEnabled;

            if (!mdcEnabled) {
                log.debug("Current Oak version does not provide MDC. Explain log would have some extra entries");
            }
        }

        @Override
        public FilterReply decide(Marker marker, ch.qos.logback.classic.Logger logger,
                                  Level level, String format, Object[] params, Throwable t) {

            /** NO LOGGING IN THIS METHOD **/

            // If request is NOT an Explain Query generated thread, then always reply NEUTRAL
            // This check is extremely fast and incur almost no overhead.

            if (!EXPLAIN_QUERY_THREAD.get()) {
                return FilterReply.NEUTRAL;
            }

            String collectorKey = MDC.get(COLLECTOR_KEY);
            if (collectorKey == null) {
                return FilterReply.NEUTRAL;
            }

            if (!acceptLogStatement(logger.getName())) {
                return FilterReply.NEUTRAL;
            }

            //isXXXEnabled call. Accept the call to allow actual message to be logged
            if (format == null) {
                return FilterReply.ACCEPT;
            }

            ILoggingEvent logEvent = new LoggingEvent(ch.qos.logback.classic.Logger.FQCN,
                    logger, level, format, t, params);
            log(collectorKey, logEvent);

            // Return NEUTRAL to allow normal logging of message depending on level
            return FilterReply.NEUTRAL;
        }

        public List<String> getLogs(String collectorKey) {
            List<ILoggingEvent> eventList = logEvents.get(collectorKey);
            if (eventList == null) {
                return Collections.emptyList();
            }
            List<String> result = new ArrayList<String>(eventList.size());
            for (ILoggingEvent e : eventList) {
                result.add(layout.doLayout(e));
            }
            return result;
        }

        public void stopCollection(String key) {
            logEvents.remove(key);
        }

        private void log(String collectorKey, ILoggingEvent e) {
            List<ILoggingEvent> eventList = logEvents.get(collectorKey);
            if (eventList == null) {
                eventList = new ArrayList<ILoggingEvent>();
                logEvents.put(collectorKey, eventList);
            }

            if (eventList.size() < msgCountLimit) {
                eventList.add(e);
            }
        }

        private boolean acceptLogStatement(String name) {
            for (final Map.Entry<String, String> entry : this.loggers.entrySet()) {
                if (name.startsWith(entry.getKey())) {
                    // log entry logger matches a configured logger

                    if (!mdcEnabled) {
                        // If MDC is not enabled, then matching logger name is good enough
                        return true;
                    } else if (mdcEnabled && entry.getValue() == null) {
                        // If MDC is enabled, but the logger is not configured to use MDC filtering then accept it
                        return true;
                    } else if (mdcEnabled && entry.getValue() != null && MDC.get(entry.getValue()) != null) {
                        // If MDC is enabled and a MDC filter value is specified, then the entry must have the
                        // MDC value
                        return true;
                    } else {
                        return false;
                    }
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

    // EXPLAIN_THREAD_LOCAL variable is used to expedite the check for is the TurboFilter should Accept.
    private static final ThreadLocal<Boolean> EXPLAIN_QUERY_THREAD = new ThreadLocal<Boolean>() {
        @Override
        protected Boolean initialValue() {
            set(false);
            return get();
        }
    };
}