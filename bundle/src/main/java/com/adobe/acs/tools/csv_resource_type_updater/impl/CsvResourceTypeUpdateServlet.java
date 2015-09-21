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

import com.adobe.acs.tools.csv.impl.CsvUtil;
import org.apache.commons.lang.StringUtils;
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

    // 3 to account for Line Termination
    private static final int VALID_ROW_LENGTH = 3;


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
                final Result result = this.update(request.getResourceResolver(), params, rows);

                log.info("Updated as TOTAL of [ {} ] resources in {} ms", result.getSuccess().size(),
                        System.currentTimeMillis() - start);

                try {
                    jsonResponse.put("success", result.getSuccess());
                    jsonResponse.put("failure", result.getFailure());
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

    /**
     * Update all resources that have matching property values with the new values in the CSV.
     * @param resourceResolver the resource resolver object
     * @param params the request params
     * @param rows the CSV rows
     * @return a list of the resource paths updated
     * @throws PersistenceException
     */
    private Result update(final ResourceResolver resourceResolver,
                                final Parameters params,
                                final Iterator<String[]> rows) throws PersistenceException {

        final Result result = new Result();

        final Map<String, String> map = new HashMap<String, String>();

        while (rows.hasNext()) {
            String[] row = rows.next();

            if (row.length == VALID_ROW_LENGTH) {
                map.put(row[0], row[1]);
                log.debug("Adding type translation [ {} ] ~> [ {} ]", row[0], row[1]);
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

            String newValue = map.get(properties.get(params.getPropertyName(), String.class));

            if (newValue != null) {
                try {
                    properties.put(params.getPropertyName(), newValue);
                    result.addSuccess(resource.getPath());
                    count++;
                } catch (Exception e) {
                    result.addFailure(resource.getPath());
                    log.warn("Could not update [ {}@" + params.getPropertyName() + " ]", resource.getPath(), e);
                }

                if (count == DEFAULT_BATCH_SIZE) {
                    this.save(resourceResolver, count);
                    count = 0;
                }
            }
        }

        this.save(resourceResolver, count);

        return result;
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


    private class Result {
        private List<String> success;
        private List<String> failure;

        public Result() {
            success = new ArrayList<String>();
            failure = new ArrayList<String>();
        }

        public List<String> getSuccess() {
            return success;
        }

        public void addSuccess(String success) {
            this.success.add(success);
        }

        public List<String> getFailure() {
            return failure;
        }

        public void addFailure(String failure) {
            this.failure.add(failure);
        }
    }
}