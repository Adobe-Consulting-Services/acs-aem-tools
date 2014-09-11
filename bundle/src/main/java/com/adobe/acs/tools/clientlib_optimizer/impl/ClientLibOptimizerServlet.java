package com.adobe.acs.tools.clientlib_optimizer.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;

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

import com.day.cq.widget.ClientLibrary;
import com.day.cq.widget.HtmlLibraryManager;
import com.day.cq.widget.LibraryType;

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


        final List<String> categories = this.getCategories(this.getCategoriesParam(request), types);

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


    private void writeJsonResponse(final List<String> categories,
                                   final SlingHttpServletResponse response) throws JSONException, IOException {

        final JSONObject jsonObject = new JSONObject();

        jsonObject.put("categories", new JSONArray(categories));

        response.getWriter().print(jsonObject.toString());
    }
                         
    private List<String> getSortedDependentCategories(Set<String> originalCategories, LibraryType type, List<String> existingCategories) {
    	final Collection<ClientLibrary> libraries = htmlLibraryManager.getLibraries(
    			originalCategories.toArray(new String[0]),
                type,
                true,
                false);
        
    	return getSortedDependentCategories(libraries, originalCategories, type, existingCategories);
    }
    
    static List<String> getSortedDependentCategories(Collection<ClientLibrary> libraries, Set<String> requestedCategories, LibraryType type, List<String> existingCategories) {
    	// sort libraries by path name
    	List<ClientLibrary> sortedLibraries = new ArrayList<ClientLibrary>(libraries);
    	Collections.sort(sortedLibraries, new ClientLibraryPathComparator());
    	 
    	for (ClientLibrary library : libraries) {
    		int index = existingCategories.isEmpty() ? 0 : existingCategories.size();
    		ClientLibraryDependency dependency = new ClientLibraryDependency(null, library, requestedCategories, false, type);
    		
    		// don't give out all categories but only the requested ones and all dependent ones!
    		existingCategories = dependency.buildDependencyTree(existingCategories, index);
    	}
    	return existingCategories;
    }

   // see  https://github.com/Adobe-Consulting-Services/acs-aem-tools/pull/47 for a discussion around that
    // https://github.com/Adobe-Consulting-Services/acs-aem-tools/issues/12
    private List<String> getCategories(Set<String> originalCategories, Map<LibraryType, Boolean> types) {

        List<String> categories = new ArrayList<String>();
        
        /* JS */
        if (types.get(LibraryType.JS)) {
        	categories = getSortedDependentCategories(originalCategories, LibraryType.JS, categories);
        } 
        if (types.get(LibraryType.CSS)) {
        	categories = getSortedDependentCategories(originalCategories, LibraryType.CSS, categories);
        } 
		return categories;
    }
}

