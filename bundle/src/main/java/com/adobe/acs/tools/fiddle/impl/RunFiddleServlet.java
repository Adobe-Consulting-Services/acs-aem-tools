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
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package com.adobe.acs.tools.fiddle.impl;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.request.RequestDispatcherOptions;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.api.wrappers.SlingHttpServletRequestWrapper;
import org.apache.sling.commons.classloader.ClassLoaderWriter;
import org.apache.sling.settings.SlingSettingsService;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

@SuppressWarnings("serial")
@SlingServlet(resourceTypes = "acs-tools/components/aemfiddle", selectors = "run", extensions = "html", methods = "POST")
public class RunFiddleServlet extends SlingAllMethodsServlet {
    private static final Logger log = LoggerFactory.getLogger(RunFiddleServlet.class);

    private static final String VAR_CLASSES = "/var/classes";

    private static final String COMPILED_JSP = "org/apache/jsp/apps/acs_002dtools/components/aemfiddle";

    private static final String[] COMPILED_JSP_FILES = new String[]{
            "org/apache/jsp/apps/acs_002dtools/components/aemfiddle/fiddle/fiddle_jsp.class",
            "org/apache/jsp/apps/acs_002dtools/components/aemfiddle/fiddle/fiddle_jsp.deps",
            "org/apache/jsp/apps/acs_002dtools/components/aemfiddle/fiddle/fiddle_jsp.java"
    };

    private File fileRoot;

    @Reference
    private FiddleRefresher fiddleRefresher;

    @Reference
    private SlingSettingsService slingSettingsService;

    @Reference
    private ClassLoaderWriter classLoaderWriter;

    @Override
    protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response)
            throws ServletException, IOException {

        // Clear any previously compiled fiddle scripts as they have a tendency to execute on first executions
        this.clearCompiledFiddle(request.getResourceResolver());


        final Resource resource = getResource(request);

        final String data = request.getParameter("scriptdata");
        final String ext = request.getParameter("scriptext");

        InMemoryScript script = InMemoryScript.set(ext, data);

        // doing this as a synchronous event so we ensure that
        // the JSP has been invalidated
        fiddleRefresher.refresh(script.getPath());

        final RequestDispatcherOptions options = new RequestDispatcherOptions();
        options.setForceResourceType(Constants.PSEDUO_COMPONENT_PATH);
        options.setReplaceSelectors("");

        // Suppress ACS AEM Commons - Component Error Handler from capturing errors
        request.setAttribute("com.adobe.acs.commons.wcm.component-error-handler.suppress", true);

        try {
            request.getResourceResolver().adaptTo(Session.class).getWorkspace().getObservationManager().setUserData("acs-aem-tools.aem-fiddle");
        } catch (RepositoryException e) {
            log.warn("Unable to set [ user-event-data = acs-aem-tools.aem-fiddle ] for fiddle execution.", e);
        }

        RequestDispatcher dispatcher = request.getRequestDispatcher(resource, options);
        GetRequest getRequest = new GetRequest(request);
        dispatcher.forward(getRequest, response);
    }

    private Resource getResource(SlingHttpServletRequest request) {
        String path = request.getParameter("resource");

        if (path == null || "".equals(path)) {
            return request.getResource();
        } else {
            Resource resource = request.getResourceResolver().resolve(path);
            if (resource != null) {
                return resource;
            } else {
                return request.getResource();
            }
        }
    }


    private void clearCompiledFiddle(final ResourceResolver resourceResolver) {
        classLoaderWriter.delete("/apps/org/apache");

        final Resource varClasses = resourceResolver.getResource(VAR_CLASSES);

        if (varClasses != null) {
            /* AEM 5.6.x does not have the /var/classes/UUID */
            this.removeResource(varClasses.getChild(COMPILED_JSP));

            /* /var/classes structure builds out under a UUID that is different between AEM6 instances */
            final Iterator<Resource> iterator = varClasses.listChildren();

            while (iterator.hasNext()) {
                final Resource varClass = iterator.next();
                this.removeResource(varClass.getChild(COMPILED_JSP));
            }
        }

        if (this.fileRoot != null) {
            // AEM 6.1+
            for (String fileName : COMPILED_JSP_FILES) {
                File file = new File(fileRoot, fileName);
                if (file.exists()) {
                    file.delete();
                }
            }
        }
    }

    private void removeResource(final Resource resource) {
        if (resource != null) {
            final Node node = resource.adaptTo(Node.class);

            if (node != null) {
                try {
                    log.trace("Removing AEM Fiddle compiled scripts at: {}", node.getPath());
                    node.remove();
                    node.getSession().save();
                } catch (RepositoryException e) {
                    log.error("Could not remove compiled AEM Fiddle scripts: {}", e);
                }
            }
        }
    }

    private static class GetRequest extends SlingHttpServletRequestWrapper {

        public GetRequest(SlingHttpServletRequest wrappedRequest) {
            super(wrappedRequest);
        }

        @Override
        public String getMethod() {
            return "GET";
        }
    }

    @Activate
    protected void activate(ComponentContext ctx) {
        BundleContext bundleContext = ctx.getBundleContext();

        // this is less than ideal, but there's no better way to get to the fs classloader's data directory
        for (Bundle bundle : bundleContext.getBundles()) {
            if (bundle.getSymbolicName().equals("org.apache.sling.commons.fsclassloader")) {
                this.fileRoot = new File(slingSettingsService.getSlingHomePath(), "launchpad/felix/bundle" + bundle.getBundleId() + "/data/classes");
                break;
            }
        }
    }
}
