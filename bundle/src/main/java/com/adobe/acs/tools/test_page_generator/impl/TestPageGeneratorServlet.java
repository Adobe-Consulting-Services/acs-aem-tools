/*
 * #%L
 * ACS AEM Tools Bundle
 * %%
 * Copyright (C) 2013 Adobe
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

package com.adobe.acs.tools.test_page_generator.impl;

import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.commons.jcr.JcrUtil;
import com.day.cq.wcm.api.NameConstants;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.day.cq.wcm.api.Template;
import com.day.cq.wcm.api.WCMException;
import org.apache.commons.lang.StringUtils;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SlingServlet(
        label = "ACS AEM Tools - Test Page Generator",
        description = "Test page generator utility servlet end-point",
        methods = { "POST" },
        resourceTypes = { "acs-tools/components/test-page-generator" },
        selectors = { "generate-pages" },
        extensions = { "json" }
)
public class TestPageGeneratorServlet extends SlingAllMethodsServlet {
    private static final Logger log = LoggerFactory.getLogger(TestPageGeneratorServlet.class);

    @Reference
    private ScriptEngineManager scriptEngineManager;

    private static final int MILLIS_IN_SECONDS = 1000;

    private static final String NT_SLING_FOLDER = "sling:Folder";

    private static final String NODE_PREFIX = "test-page-";

    private static final String TITLE_PREFIX = "Test Page ";

    @Override
    protected final void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            final JSONObject json = this.generatePages(request.getResourceResolver(), new Parameters(request));
            response.getWriter().write(json.toString(2));
        } catch (JSONException e) {
            log.error(e.getMessage());
            this.sendJSONError(response,
                    "Form errors",
                    "Could not understand provided parameters");
        } catch (RepositoryException e) {
            log.error("Could not perform interim Save due to: {}", e.getMessage());
            this.sendJSONError(response,
                    "Repository error",
                    e.getMessage());
        } catch (WCMException e) {
            log.error("Could not create Page due to: {}", e.getMessage());
            this.sendJSONError(response,
                    "WCM Page creation error",
                    e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("Could not store JavaScript eval result into repository: {}", e.getMessage());
            this.sendJSONError(response,
                    "JavaScript-based property evaluation error",
                    e.getMessage());
        }
    }

    private JSONObject generatePages(ResourceResolver resourceResolver, Parameters parameters) throws IOException,
            WCMException, RepositoryException, JSONException {

        final ScriptEngine scriptEngine = scriptEngineManager.getEngineByExtension("ecma");

        final JSONObject jsonResponse = new JSONObject();

        final int pageCount = parameters.getTotal();
        final int bucketSize = parameters.getBucketSize();
        final int saveThreshold = parameters.getSaveThreshold();
        final String rootPath = parameters.getRootPath();

        final Session session = resourceResolver.adaptTo(Session.class);

        /* Initialize Depth Tracker */
        int[] depthTracker = this.initDepthTracker(pageCount, bucketSize);

        int i = 0;
        int bucketCount = 0;

        long start = System.currentTimeMillis();


        while (i++ < pageCount) {
            depthTracker = this.updateDepthTracker(depthTracker, bucketCount, bucketSize);

            if (this.needsNewBucket(bucketCount, bucketSize)) {
                bucketCount = 0;
            }

            final String folderPath = this.getOrCreateBucketPath(resourceResolver, rootPath, depthTracker);

            final Page page = createPage(resourceResolver,
                    folderPath,
                    NODE_PREFIX + (i + 1),
                    parameters.getTemplate(),
                    TITLE_PREFIX + (i + 1));

            final ModifiableValueMap properties = page.getContentResource().adaptTo(ModifiableValueMap.class);

            for (Map.Entry<String, Object> entry : parameters.getProperties().entrySet()) {
                properties.put(entry.getKey(), this.eval(scriptEngine, entry.getValue()));
            }

            bucketCount++;

            if (i % saveThreshold == 0) {
                log.debug("Saving at threshold for [ {} ] items", i);
                this.save(session);
            }
        }

        if (saveThreshold % i != 0) {
            this.save(session);
        }

        jsonResponse.put("totalTime", (int) ((System.currentTimeMillis() - start) / MILLIS_IN_SECONDS));
        jsonResponse.put("rootPath", rootPath);
        jsonResponse.put("bucketSize", bucketSize);
        jsonResponse.put("saveThreshold", saveThreshold);
        jsonResponse.put("depth", depthTracker.length);
        jsonResponse.put("count", pageCount);
        jsonResponse.put("success", true);

        return jsonResponse;
    }

    private void sendJSONError(SlingHttpServletResponse response, String title, String message) throws IOException {
        final JSONObject json = new JSONObject();

        response.setStatus(SlingHttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        try {
            json.put("title", title);
            json.put("message", message);
            response.getWriter().write(json.toString());
        } catch (JSONException e) {
            String fallbackJSON = "{ \"title\": \"Error creating error response. "
                    + "Please review AEM error logs.\" }";

            response.getWriter().write(fallbackJSON);
        }
    }

    /**
     * Saves the current state.
     *
     * @param session session obj
     * @return the time it took to save
     * @throws RepositoryException
     */
    private long save(Session session) throws RepositoryException {
        final long start = System.currentTimeMillis();
        session.save();
        final long total = System.currentTimeMillis() - start;

        log.debug("Save operation for batch page creation took {} ms", total);

        return total;
    }

    /**
     * Determines if a new bucket is need (if the current bucket is full).
     *
     * @param bucketCount number of items in the bucket
     * @param bucketSize  the max number of items to be added to a bucket
     * @return true if a new bucket is required
     */
    private boolean needsNewBucket(int bucketCount, int bucketSize) {
        return bucketCount >= bucketSize;
    }

    /**
     * Creates the parent bucket structure to place the Page.
     *
     * @param resourceResolver the resource resolver
     * @param rootPath         the root path used for the test page generation
     * @param depthTracker     the depth tracker indicating the current bucket
     * @return the path to the newly created bucket
     * @throws RepositoryException
     */
    private String getOrCreateBucketPath(ResourceResolver resourceResolver, String rootPath, int[] depthTracker)
            throws RepositoryException {
        final Session session = resourceResolver.adaptTo(Session.class);
        String folderPath = rootPath;

        for (int i = 0; i < depthTracker.length; i++) {
            final String tmp = Integer.toString(depthTracker[i] + 1);
            folderPath += "/" + tmp;
        }

        if (resourceResolver.getResource(folderPath) != null) {
            return folderPath;
        } else {
            Node node = JcrUtil.createPath(folderPath, NT_SLING_FOLDER, NT_SLING_FOLDER, session, false);
            log.debug("Created new folder path at [ {} ]", node.getPath());
            return node.getPath();
        }
    }

    /**
     * Creates and initializes the depth tracker array.
     *
     * @param total      total number of pages to create
     * @param bucketSize size of each bucket
     * @return the depth tracker array initialized to all 0's
     */
    private int[] initDepthTracker(int total, int bucketSize) {
        int depth = getDepth(total, bucketSize);

        int[] depthTracker = new int[depth];
        for (int i = 0; i < depthTracker.length; i++) {
            depthTracker[i] = 0;
        }

        return depthTracker;
    }

    /**
     * Manages tracker used to determine the parent bucket structure.
     *
     * @param depthTracker Array used to track which bucket is used to create the "current" page
     * @param bucketCount  Number of items already in the bucket
     * @param bucketSize   The max number of items in the bucket
     * @return The updated depth tracker array
     */
    private int[] updateDepthTracker(int[] depthTracker, int bucketCount, int bucketSize) {
        if (!this.needsNewBucket(bucketCount, bucketSize)) {
            return depthTracker;
        }

        for (int i = depthTracker.length - 1; i >= 0; i--) {
            if (depthTracker[i] >= bucketSize - 1) {
                depthTracker[i] = 0;
            } else {
                depthTracker[i] = depthTracker[i] + 1;
                log.debug("Updating depthTracker at location [ {} ] to [ {} ]", i, depthTracker[i]);
                break;
            }
        }

        return depthTracker;
    }

    /**
     * Determines the bucket depth required to organize the pages so no more than bucketSize siblings ever exist.
     *
     * @param total      Total number pages to create
     * @param bucketSize Max number of siblings
     * @return The node depth required to achieve desired bucket-size
     */
    private int getDepth(int total, int bucketSize) {
        int depth = 0;
        int remainingSize = total;

        do {
            remainingSize = (int) Math.ceil((double) remainingSize / (double) bucketSize);

            log.debug("Remaining size of [ {} ] at depth [ {} ]", remainingSize, depth);

            depth++;
        } while (remainingSize > bucketSize);

        log.debug("Final depth of [ {} ]", depth);

        return depth;
    }

    /**
     * Wrapper for CQ PageManager API since it does not create the jcr:content node with jcr:primaryType=cq:PageContent.
     *
     * @param resourceResolver the resource resolver
     * @param folderPath       the path to create page (must exist)
     * @param nodeName         the name of the node; if node of this name already exists a unique name will be generated
     * @param templatePath     the absolute path to the template to use
     * @param title            the jcr:title of the page
     * @return the new Page
     * @throws WCMException        could not create the page using CQ PageManager API
     * @throws RepositoryException could not find folderPath node
     */
    private Page createPage(ResourceResolver resourceResolver, String folderPath, String nodeName, String templatePath,
                            String title) throws RepositoryException, WCMException {

        final PageManager pageManager = resourceResolver.adaptTo(PageManager.class);
        final Template template = pageManager.getTemplate(templatePath);

        if (template != null) {
            // A template is defined so use that
            return pageManager.create(folderPath, nodeName, templatePath, title, false);
        } else {
            // Manually create the page nodes to prevent the creation of nt:unstructured-based jcr:content node
            final Session session = resourceResolver.adaptTo(Session.class);
            final Node folderNode = session.getNode(folderPath);

            nodeName = JcrUtil.createValidName(nodeName);

            final Node pageNode = JcrUtil.createUniqueNode(folderNode, nodeName, NameConstants.NT_PAGE, session);
            final Node contentNode = JcrUtil.createUniqueNode(pageNode, JcrConstants.JCR_CONTENT,
                    "cq:PageContent", session);
            JcrUtil.setProperty(contentNode, JcrConstants.JCR_TITLE, title);

            return resourceResolver.getResource(pageNode.getPath()).adaptTo(Page.class);
        }
    }


    private Object eval(final ScriptEngine scriptEngine, final Object value) {

        if (scriptEngine == null) {
            log.warn("ScriptEngine is null; cannot evaluate");
            return value;
        } else if (value instanceof String[]) {
            final List<String> scripts = new ArrayList<String>();
            final String[] values = (String[]) value;

            for (final String val : values) {
                scripts.add(String.valueOf(this.eval(scriptEngine, val)));
            }

            return scripts.toArray(new String[scripts.size()]);
        } else if (!(value instanceof String)) {
            return value;
        }

        final String stringValue = StringUtils.stripToEmpty((String) value);

        String script;
        if (StringUtils.startsWith(stringValue, "{{")
                && StringUtils.endsWith(stringValue, "}}")) {

            script = StringUtils.removeStart(stringValue, "{{");
            script = StringUtils.removeEnd(script, "}}");
            script = StringUtils.stripToEmpty(script);

            try {
                return scriptEngine.eval(script);
            } catch (ScriptException e) {
                log.error("Could not evaluation the test page property ecma [ {} ]", script);
            }
        }

        return value;
    }
}
