/*
 * #%L
 * ACS AEM Tools Bundle
 * %%
 * Copyright (C) 2018 Adobe
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
package com.adobe.acs.tools.dumplibs;

import com.adobe.granite.ui.clientlibs.ClientLibrary;
import com.adobe.granite.ui.clientlibs.HtmlLibrary;
import com.adobe.granite.ui.clientlibs.HtmlLibraryManager;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

@SlingServlet(
        label = "ACS AEM Tools - Dumplibs",
        description = "ACS AEM Tools End-point for Dumplibs",
        methods = {"GET"},
        resourceTypes = {"acs-tools/components/dumplibs"},
        selectors = {"app"},
        extensions = {"json"},
        metatype = true
)
public class DumplibsServlet extends SlingSafeMethodsServlet {

    private static final Logger log = LoggerFactory.getLogger(DumplibsServlet.class);
    private static final String CONTENT_TYPE_JSON = "application/json";
    private static final Gson gson = new Gson();

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
        response.setContentType(CONTENT_TYPE_JSON);
        JsonObject jsonObject = lib == null
                                ? new JsonObject()
                                : (JsonObject) htmlLibraryToJSON(lib);
        response.getWriter().print(jsonObject.toString());
    }

    /**
     * Handles request to get clientlib info by categories
     */
    private void handleCategoriesRequest(SlingHttpServletRequest request, SlingHttpServletResponse response, DumplibsParams p)
            throws IOException {

        Collection<ClientLibrary> libs = libraryManager.getLibraries(p.getCategories(), p.getType(), !p.isThemed(), p.isTrans());

        JsonArray libsJSON = clientLibrariesCollectionToJSON(libs); // should never be null
        response.setContentType(CONTENT_TYPE_JSON);
        response.getWriter().print(libsJSON.toString());
    }

    /**
     * Handles request to get all clientlib info
     */
    private void handleAllLibsRequest(SlingHttpServletRequest request, SlingHttpServletResponse response, DumplibsParams p)
            throws IOException {

        Map<String, ClientLibrary> libs = libraryManager.getLibraries();
        JsonArray libsJSON = clientLibrariesMapToJSON(libs); // should never be null
        response.setContentType(CONTENT_TYPE_JSON);
        response.getWriter().print(libsJSON.toString());
    }

    /**
     * Transforms an HtmlLibrary object to JsonElement;
     *
     * @param clientlib
     * @return the JsonElement representation of the HtmlLibrary
     */
    private JsonElement htmlLibraryToJSON(HtmlLibrary clientlib) {

        DumplibsHtmlLibrary lib = new DumplibsHtmlLibrary(clientlib);
        return gson.toJsonTree(lib);

    }

    /**
     * Transforms an ClientLibrary object to JsonElement;
     *
     * @param clientlib
     * @return the JsonElement representation of the ClientLibrary
     */
    private JsonElement clientLibraryToJSON(ClientLibrary clientlib) {
        DumplibsClientLibrary lib = new DumplibsClientLibrary(clientlib);
        return gson.toJsonTree(lib);
    }

    /**
     * Transforms a ClientLibrary map to a JsonArray
     *
     * @param libraries
     * @return a non null JsonArray
     */
    private JsonArray clientLibrariesMapToJSON(Map<String, ClientLibrary> libraries) {
        return clientLibrariesCollectionToJSON(libraries.values());
    }

    /**
     * Transforms a ClientLibrary Collection to a JsonArray
     *
     * @param libraries
     * @return @return a non null JsonArray
     */
    private JsonArray clientLibrariesCollectionToJSON(Collection<ClientLibrary> libraries) {

        JsonArray clientlibs = new JsonArray();

        if (libraries != null || !libraries.isEmpty()) {
            for (ClientLibrary lib : libraries) {
                clientlibs.add(clientLibraryToJSON(lib));
            }
        }

        return clientlibs;
    }

}