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
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;

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

    private static final LibraryType DEFAULT_PARAM_LIBRARY_TYPE_VALUE = LibraryType.JS;

    private static final String PARAM_LIBRARY_TYPE = "type";
    private static final String PARAM_CATEGORIES = "categories";


    @Reference
    private HtmlLibraryManager htmlLibraryManager;


    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response)
            throws ServletException, IOException {

        final LibraryType type = this.getLibraryTypeParam(request);

        final LinkedHashSet<String> categories = this.getCategories(this.getCategoriesParam(request), type);

        try {
            this.writeJsonResponse(categories, type, response);
        } catch (JSONException e) {
            throw new ServletException("Error constructing valid JSON response.");
        }
    }

    private LinkedHashSet<String> getCategoriesParam(final SlingHttpServletRequest request) {
        final LinkedHashSet<String> categories = new LinkedHashSet<String>();

        final RequestParameter requestParameter = request.getRequestParameter(PARAM_CATEGORIES);
        if(requestParameter != null) {
            final String[] segments = StringUtils.split(requestParameter.getString(), ",");

            for (final String segment : segments) {
                if (StringUtils.isNotBlank(segment)) {
                    categories.add(StringUtils.stripToEmpty(segment));
                }
            }

        }
        return categories;
    }

    private LibraryType getLibraryTypeParam(final SlingHttpServletRequest request) {
        final RequestParameter requestParameter = request.getRequestParameter(PARAM_LIBRARY_TYPE);

        if(requestParameter != null) {
            final String type = StringUtils.stripToNull(requestParameter.getString());

            if (type != null) {
                final LibraryType libraryType = LibraryType.valueOf(type);

                if (libraryType != null) {
                    return libraryType;
                }
            }
        }
        return DEFAULT_PARAM_LIBRARY_TYPE_VALUE;
    }


    private void writeJsonResponse(final LinkedHashSet<String> categories, final LibraryType type,
                                   final SlingHttpServletResponse response) throws JSONException, IOException {

        final JSONObject jsonObject = new JSONObject();

        jsonObject.put("type", type.toString());
        jsonObject.put("categories", new JSONArray(categories));

        response.getWriter().print(jsonObject.toString());
    }


    private LinkedHashSet<String> getCategories(LinkedHashSet<String> categories, LibraryType type) {
        final int originalSize = categories.size();

        final Collection<ClientLibrary> clientLibraries = htmlLibraryManager.getLibraries(
                categories.toArray(new String[originalSize]),
                type,
                true,
                true);

        for(final ClientLibrary clientLibrary : clientLibraries) {
            categories.addAll(Arrays.asList(clientLibrary.getCategories()));
        }

        if(originalSize != categories.size()) {
            log.info("Category Size changed from [ {} ] to [ {} ]", originalSize, categories.size());
            categories = getCategories(categories, type);
        }

        return categories;
    }
}
