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

@SlingServlet(
        label = "ACS AEM Tools - Explain Query Servlet",
        description = "End-point for getting query explanations.",
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

    private static final String[] LANGUAGES = new String[]{SQL, SQL2, XPATH};

    @Reference
    private QueryStatManagerMBean queryStatManagerMBean;

    @Reference
    private SlingRepository slingRepository;


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
            e.printStackTrace();
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
        final Query query = queryManager.createQuery("explain " + statement, language);

        final QueryResult queryResult = query.execute();
        final RowIterator rows = queryResult.getRows();
        final Row firstRow = rows.nextRow();

        final String plan = firstRow.getValue("plan").getString();
        final boolean propertyIndex = StringUtils.contains(plan, " /* property ");

        final JSONObject json = new JSONObject();
        json.put("plan", plan);
        json.put("propertyIndex", propertyIndex);

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
