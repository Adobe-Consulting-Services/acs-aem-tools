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
import javax.servlet.ServletException;
import java.io.IOException;

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

    private static final int MILLIS_IN_SECONDS = 1000;

    private static final String NT_SLING_FOLDER = "sling:Folder";

    private static final String NODE_PREFIX = "test-page-";

    private static final String TITLE_PREFIX = "Test Page ";

    @Override
    protected final void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response)
            throws ServletException, IOException {

        try {
            final JSONObject json = this.generatePages(request.getResourceResolver(), new Parameters(request));
            response.getWriter().write(json.toString(2));
        } catch (JSONException e) {
            log.error(e.getMessage());
            response.sendError(SlingHttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Could not understand provided parameters");
        } catch (RepositoryException e) {
            log.error("Could not perform interim Save due to: {}", e.getMessage());
            response.sendError(SlingHttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } catch (WCMException e) {
            log.error("Could not create Page due to: {}", e.getMessage());
            response.sendError(SlingHttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private JSONObject generatePages(ResourceResolver resourceResolver, Parameters parameters) throws IOException,
            WCMException, RepositoryException, JSONException {

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

            final ModifiableValueMap mvp = page.getContentResource().adaptTo(ModifiableValueMap.class);

            mvp.putAll(parameters.getProperties());

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
}
