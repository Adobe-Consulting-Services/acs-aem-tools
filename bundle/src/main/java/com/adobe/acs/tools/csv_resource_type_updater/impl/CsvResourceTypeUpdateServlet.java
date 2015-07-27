/*
 * #%L
 * ACS AEM Tools Bundle
 * %%
 * Copyright (C) 2015 Adobe
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package com.adobe.acs.tools.csv_resource_type_updater.impl;

import com.adobe.acs.tools.csv.impl
        .CsvUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.apache.sling.commons.mime.MimeTypeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@SlingServlet(
        label = "ACS AEM Tools - CSV Resource Type Updater Servlet",
        methods = { "POST" },
        resourceTypes = { "acs-tools/components/csv-resource-type-updater" },
        selectors = { "update" },
        extensions = { "json" }
)
public class CsvResourceTypeUpdateServlet extends SlingAllMethodsServlet {
    private static final Logger log = LoggerFactory.getLogger(CsvResourceTypeUpdateServlet.class);

    private static final int DEFAULT_BATCH_SIZE = 1000;


    @Reference
    private MimeTypeService mimeTypeService;

    @Override
    protected final void doPost(final SlingHttpServletRequest request, final SlingHttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        final JSONObject jsonResponse = new JSONObject();
        final Parameters params = new Parameters(request);

        if (params.getFile() != null) {

            final long start = System.currentTimeMillis();
            final Iterator<String[]> rows = CsvUtil.getRowsFromCsv(params);

            try {
                // Process Asset row entries
                final List<String> result = new ArrayList<String>();
                final List<String> batch = new ArrayList<String>();
                int failures = 0;

                List<String> paths = this.doWork(request.getResourceResolver(), params, rows);

                // Final save to catch any non-modulo stragglers; will only invoke persist if there are changes
                this.save(request.getResourceResolver(), DEFAULT_BATCH_SIZE);
                result.addAll(batch);

                log.info("Imported as TOTAL of [ {} ] assets in {} ms", result.size(),
                        System.currentTimeMillis() - start);

                try {
                    jsonResponse.put("paths", paths);
                } catch (JSONException e) {
                    log.error("Could not serialized results into JSON", e);
                    this.addMessage(jsonResponse, "Could not serialized results into JSON");
                    response.setStatus(SlingHttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                }
            } catch (Exception e) {
                log.error("Could not process CSV type update replacement", e);
                this.addMessage(jsonResponse, "Could not process CSV type update. " + e.getMessage());
                response.setStatus(SlingHttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } else {
            log.error("Could not find CSV file in request.");
            this.addMessage(jsonResponse, "CSV file is missing");
            response.setStatus(SlingHttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

        response.getWriter().print(jsonResponse.toString());
    }

    private List<String> doWork(final ResourceResolver resourceResolver,
                        final Parameters params,
                        final Iterator<String[]> rows) throws PersistenceException {

        final List<String> results = new ArrayList<String>();
        final Map<String, String> map = new HashMap<String, String>();

        while (rows.hasNext()) {
            String[] row = rows.next();

            if (row.length == 3) {
                map.put(row[0], row[1]);
                log.debug("Adding translation [ {} ] ~> [ {} ]", row[0], row[1]);
            } else {
                log.warn("Row {} is malformed", Arrays.asList(row));
            }
        }

        String query = "SELECT * FROM [nt:base] WHERE ";
        query += "ISDESCENDANTNODE([" + params.getPath() + "]) AND (";

        final List<String> conditions = new ArrayList<String>();

        for (String key : map.keySet()) {
            conditions.add("[" + params.getPropertyName() + "] = '" + key + "'");
        }

        query += StringUtils.join(conditions, " OR ");
        query += ")";

        log.debug("Query: {}", query);

        final Iterator<Resource> resources = resourceResolver.findResources(query, "JCR-SQL2");

        int count = 0;
        while (resources.hasNext()) {
            final Resource resource = resources.next();
            final ModifiableValueMap properties = resource.adaptTo(ModifiableValueMap.class);

            String newValue = map.get(properties.get( params.getPropertyName(), String.class));

            if (newValue != null) {
                properties.put( params.getPropertyName(), newValue);
                results.add(resource.getPath());
                count++;

                if(count == DEFAULT_BATCH_SIZE) {
                    this.save(resourceResolver, count);
                    count = 0;
                }
            }
        }

        this.save(resourceResolver, count);

        return results;
    }

    /**
     * Helper for saving changes to the JCR; contains timing logging.
     *
     * @param resourceResolver the resource resolver
     * @param size             the number of changes to save
     * @throws PersistenceException
     */
    private void save(final ResourceResolver resourceResolver, final int size) throws PersistenceException {
        if (resourceResolver.hasChanges()) {
            final long start = System.currentTimeMillis();
            resourceResolver.commit();
            log.info("Imported a BATCH of [ {} ] assets in {} ms", size, System.currentTimeMillis() - start);
        } else {
            log.debug("Nothing to save");
        }
    }

    /**
     * Helper method; adds a message to the JSON Response object.
     *
     * @param jsonObject the JSON object to add the message to
     * @param message    the message to add.
     */
    private void addMessage(final JSONObject jsonObject, final String message) {
        try {
            jsonObject.put("message", message);
        } catch (JSONException e) {
            log.error("Could not formulate JSON Response", e);
        }
    }
}