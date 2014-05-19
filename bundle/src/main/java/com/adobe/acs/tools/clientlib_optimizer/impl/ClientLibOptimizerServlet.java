package com.adobe.acs.tools.clientlib_optimizer.impl;

import com.day.cq.widget.ClientLibrary;
import com.day.cq.widget.HtmlLibraryManager;
import com.day.cq.widget.LibraryType;
import org.apache.commons.lang.StringUtils;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.request.RequestParameter;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.commons.json.JSONArray;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SlingServlet(
        label = "ACS AEM Tools - ClientLibrary Optimizer Servlett",
        description = "...",
        methods = { "GET" },
        resourceTypes = { "acs-tools/components/clientlibs-optimizer" },
        selectors = { "optimize" },
        extensions = { "json" }
)

public class ClientLibOptimizerServlet extends SlingSafeMethodsServlet {
    private static final Logger log = LoggerFactory.getLogger(ClientLibOptimizerServlet.class);

    private static final String PARAM_LIBRARY_TYPE_CSS = "css";

    private static final String PARAM_LIBRARY_TYPE_JS = "js";

    private static final String PARAM_CATEGORIES = "categories";

    @Reference
    private HtmlLibraryManager htmlLibraryManager;

    @Override
    protected final void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response)
            throws ServletException, IOException {

        final Map<LibraryType, Boolean> types = new HashMap<LibraryType, Boolean>();
        types.put(LibraryType.JS, this.hasLibraryTypeParam(request, PARAM_LIBRARY_TYPE_JS));
        types.put(LibraryType.CSS, this.hasLibraryTypeParam(request, PARAM_LIBRARY_TYPE_CSS));


        final Set<String> categories = this.getCategories(this.getCategoriesParam(request), types);

        try {
            this.writeJsonResponse(categories, response);
        } catch (JSONException e) {
            throw new ServletException("Error constructing valid JSON response.");
        }
    }

    private Set<String> getCategoriesParam(final SlingHttpServletRequest request) {
        final LinkedHashSet<String> categories = new LinkedHashSet<String>();
        final RequestParameter requestParameter = request.getRequestParameter(PARAM_CATEGORIES);

        if (requestParameter != null) {
            final String[] segments = StringUtils.split(requestParameter.getString(), ",");

            for (final String segment : segments) {
                if (StringUtils.isNotBlank(segment)) {
                    categories.add(StringUtils.stripToEmpty(segment));
                }
            }

        }
        return categories;
    }

    private boolean hasLibraryTypeParam(final SlingHttpServletRequest request, final String paramLibraryType) {
        final RequestParameter requestParameter = request.getRequestParameter(paramLibraryType);

        if (requestParameter != null) {
            return Boolean.parseBoolean(requestParameter.getString());
        }
        return false;
    }


    private void writeJsonResponse(final Set<String> categories,
                                   final SlingHttpServletResponse response) throws JSONException, IOException {

        final JSONObject jsonObject = new JSONObject();

        jsonObject.put("categories", new JSONArray(categories));

        response.getWriter().print(jsonObject.toString());
    }


    private Set<String> getCategories(Set<String> originalCategories, Map<LibraryType, Boolean> types) {
        final int originalSize = originalCategories.size();

        LinkedHashSet<String> categories = new LinkedHashSet<String>();
        final Collection<String> paths = new HashSet<String>();

        /* JS */
        if (types.get(LibraryType.JS)) {

            final Collection<ClientLibrary> jsClientLibraries = htmlLibraryManager.getLibraries(
                    originalCategories.toArray(new String[0]),
                    LibraryType.JS,
                    true,
                    true);

            log.debug("Adding [ {} ] JS ClientLibs for [ {} ]", jsClientLibraries.size(), originalCategories);

            for (final ClientLibrary clientLibrary : jsClientLibraries) {
                paths.add(clientLibrary.getPath());
            }
        }

        /* CSS */
        if (types.get(LibraryType.CSS)) {

            final Collection<ClientLibrary> cssClientLibraries = htmlLibraryManager.getLibraries(
                    originalCategories.toArray(new String[0]),
                    LibraryType.CSS,
                    true,
                    true);

            log.debug("Adding [ {} ] CSS ClientLibs for [ {} ]", cssClientLibraries.size(), originalCategories);

            for (final ClientLibrary clientLibrary : cssClientLibraries) {
                paths.add(clientLibrary.getPath());
            }
        }


        // Get all the Transitive Dependencies
        final Set<String> dependencyPaths = new LinkedHashSet<String>();
        for (final String path : paths) {
            final ClientLibrary clientLibrary = htmlLibraryManager.getLibraries().get(path);
            final Collection<? extends ClientLibrary> dependencies = clientLibrary.getDependencies(true).values();

            for (ClientLibrary dependency : dependencies) {
                dependencyPaths.add(dependency.getPath());
            }
        }
        paths.addAll(dependencyPaths);

        /* Get categories for Client Libraries */

        /* Sort the paths */
        List<String> sortedPaths = new ArrayList<String>(paths);

        log.error(">>>>> Pre-sort: {}", sortedPaths);

        Collections.sort(sortedPaths, new ClientLibraryComparator());

        log.error(">>>>> Post-sort: {}", sortedPaths);

        /* Convert to Categories */
        for (final String path : sortedPaths) {
            final ClientLibrary clientLibrary = htmlLibraryManager.getLibraries().get(path);

            // Use the first Category ?

            categories.addAll(Arrays.asList(clientLibrary.getCategories()[0]));
        }

        /*
        if (originalSize != categories.size()) {
            log.info("Category Size changed from [ {} ] to [ {} ]", originalSize, originalCategories.size());
            return this.getCategories(categories, types);
        } else {
            return categories;
        }
        */
        return categories;
    }

    /**
     * Comparator for ClientLibrary Paths.
     */
    public class ClientLibraryComparator implements Comparator<String> {
        @Override
        public final int compare(final String p1, final String p2) {
            final ClientLibrary cl1 = htmlLibraryManager.getLibraries().get(p1);
            final ClientLibrary cl2 = htmlLibraryManager.getLibraries().get(p2);

            if (this.isUsedBy(cl1, cl2)) {
                log.debug("{} < {}", cl1.getPath(), cl2.getPath());
                return -1;
            }

            if (this.isUsedBy(cl2, cl1)) {
                log.debug("{} > {}", cl1.getPath(), cl2.getPath());
                return 1;
            }

            int d1 = cl1.getDependencies(true).size();
            int d2 = cl2.getDependencies(true).size();

            if (d1 < d2) {
                return -1;
            } else if (d2 > d1) {
                return 1;
            } else {
                return 0;
            }
        }


        private boolean isUsedBy(ClientLibrary used, ClientLibrary by) {
            final Set<String> paths = new HashSet<String>();

            for (ClientLibrary dependency : by.getDependencies(true).values()) {
                paths.add(dependency.getPath());
            }

            for (ClientLibrary embedJS : by.getEmbedded(LibraryType.JS).values()) {
                paths.add(embedJS.getPath());
            }

            for (ClientLibrary embedCSS : by.getEmbedded(LibraryType.CSS).values()) {
                paths.add(embedCSS.getPath());
            }

            log.debug("{} ...", used.getPath());
            log.debug("{} => {} ", by.getPath(), paths);
            return paths.contains(used.getPath());
        }
    }
}

