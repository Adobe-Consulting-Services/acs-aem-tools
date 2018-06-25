package com.adobe.acs.tools.dumplibs;

import com.adobe.granite.ui.clientlibs.ClientLibrary;
import com.adobe.granite.ui.clientlibs.HtmlLibrary;
import com.adobe.granite.ui.clientlibs.HtmlLibraryManager;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.commons.json.JSONArray;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@SlingServlet(
        label = "ACS AEM Tools - Dumplibs",
        description = "ACS AEM Tools End-point for Dumplibs",
        methods = {"GET"},
        paths = {"/bin/acs-tools/dumplibs"},
        extensions = {"json"},
        metatype = true
)
public class DumplibsServlet extends SlingSafeMethodsServlet {

    private static final Logger log = LoggerFactory.getLogger(DumplibsServlet.class);
    private static final String CONTENT_TYPE_JSON = "application/json";

    @Reference
    HtmlLibraryManager libraryManager;

    @Override
    protected final void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response)
            throws ServletException, IOException {

        DumplibsParams p = new DumplibsParams(request);

        // if path is present in request params, it takes precedence
        // get clientlib by path
        if (p.getPath() != null) {
            handlePathRequest(request, response, p);
        }
        // If path is NOT present and categories is present in request params
        // get clientlibs by categories
        else if (p.getCategories() != null) {
            handleCategoriesRequest(request, response, p);
        }
        // Neither path nor categories are present in request params
        // get all clientlibs
        else {
            handleAllLibsRequest(request, response, p);
        }

    }

    /**
     * Handles request to get clientlib info by path
     */
    private void handlePathRequest(SlingHttpServletRequest request, SlingHttpServletResponse response, DumplibsParams p)
            throws IOException {
        HtmlLibrary lib = libraryManager.getLibrary(p.getType(), p.getPath());
        try {

            JSONObject libJSON = htmlLibraryToJSON(lib); // should never be null
            response.setContentType(CONTENT_TYPE_JSON);
            response.getWriter().print(libJSON.toString());

        } catch (JSONException e) {
            log.error("JSON Exception while building ClientLib JSON for clientlib path:" + p.getPath(), e.getMessage());
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Handles request to get clientlib info by categories
     */
    private void handleCategoriesRequest(SlingHttpServletRequest request, SlingHttpServletResponse response, DumplibsParams p)
            throws IOException {

        Collection<ClientLibrary> libs = libraryManager.getLibraries(p.getCategories(), p.getType(), !p.isThemed(), p.isTrans());
        try {

            JSONArray libsJSON = clientLibrariesCollectionToJSON(libs); // should never be null
            response.setContentType(CONTENT_TYPE_JSON);
            response.getWriter().print(libsJSON.toString());

        } catch (JSONException e) {
            log.error("JSON Exception while building ClientLib JSON for clientlib categories:" + p.getCategoriesString(), e.getMessage());
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Handles request to get all clientlib info
     */
    private void handleAllLibsRequest(SlingHttpServletRequest request, SlingHttpServletResponse response, DumplibsParams p)
            throws IOException {

        Map<String, ClientLibrary> libs = libraryManager.getLibraries();
        try {

            JSONArray libsJSON = clientLibrariesMapToJSON(libs); // should never be null
            response.setContentType(CONTENT_TYPE_JSON);
            response.getWriter().print(libsJSON.toString());

        } catch (JSONException e) {
            log.error("JSON Exception while building ClientLib JSON for all clientlibs", e.getMessage());
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Transforms an HtmlLibrary object to JSON
     *
     * @param clientlib
     * @return A non null JSONObject
     * @throws JSONException
     */
    private JSONObject htmlLibraryToJSON(HtmlLibrary clientlib)
            throws JSONException {
        JSONObject libJSON = new JSONObject();

        if (clientlib != null) {
            libJSON.put("name", clientlib.getName());
            libJSON.put("type", clientlib.getType());
            libJSON.put("path", clientlib.getPath());
            libJSON.put("minifiedPath", clientlib.getPath(true));
            libJSON.put("libraryPath", clientlib.getLibraryPath());
            libJSON.put("scripts", toJSONArray(clientlib.getScripts()));
        }
        return libJSON;
    }

    /**
     * Transforms an ClientLibrary object to JSON
     *
     * @param clientlib
     * @return A non null JSONObject
     * @throws JSONException
     */
    private JSONObject clientLibraryToJSON(ClientLibrary clientlib)
            throws JSONException {
        JSONObject libJSON = new JSONObject();
        if (clientlib != null) {
            libJSON.put("path", clientlib.getPath());
            libJSON.put("types", toJSONArray(clientlib.getTypes()));
            libJSON.put("categories", toJSONArray(clientlib.getCategories()));
            libJSON.put("channels", toJSONArray(clientlib.getChannels()));
        }
        return libJSON;
    }

    /**
     * Transforms a ClientLibrary map to a JSONArray
     *
     * @param libraries
     * @return a non null JSONArray
     * @throws JSONException
     */
    private JSONArray clientLibrariesMapToJSON(Map<String, ClientLibrary> libraries)
            throws JSONException {
        return clientLibrariesCollectionToJSON(libraries.values());
    }

    /**
     * Transforms a ClientLibrary Collection to a JSONArray
     *
     * @param libraries
     * @return @return a non null JSONArray
     * @throws JSONException
     */
    private JSONArray clientLibrariesCollectionToJSON(Collection<ClientLibrary> libraries)
            throws JSONException {
        JSONArray clientlibs = new JSONArray();

        if (libraries != null || !libraries.isEmpty()) {
            for (ClientLibrary lib : libraries) {
                clientlibs.put(clientLibraryToJSON(lib));
            }
        }

        return clientlibs;
    }

    /**
     * Shorthand method to convert any Collection to a JSONArray
     *
     * @param collection
     * @return
     */
    private JSONArray toJSONArray(Collection<?> collection) {
        return new JSONArray(collection);
    }

    /**
     * Shorthand method to convert any Array to a JSONArray
     *
     * @param arr
     * @return
     */
    private JSONArray toJSONArray(Object[] arr) {
        List<String> list = new ArrayList<String>();
        for (Object o : arr) {
            list.add(o.toString()); // assuming the converted object has a decent toString method :)
        }
        return toJSONArray(list);
    }
}