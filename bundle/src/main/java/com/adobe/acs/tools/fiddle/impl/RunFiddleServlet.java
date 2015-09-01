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

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingConstants;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.request.RequestDispatcherOptions;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.scripting.SlingBindings;
import org.apache.sling.api.scripting.SlingScript;
import org.apache.sling.api.scripting.SlingScriptHelper;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.api.wrappers.SlingHttpServletRequestWrapper;
import org.apache.sling.scripting.core.ScriptHelper;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.acs.tools.fiddle.impl.helper.DummyRequest;
import com.adobe.acs.tools.fiddle.impl.helper.DummyResponse;
import com.adobe.acs.tools.fiddle.impl.helper.SyntheticGraniteWorkItem;
import com.adobe.acs.tools.fiddle.impl.helper.SyntheticGraniteWorkflowData;
import com.adobe.acs.tools.fiddle.impl.helper.SyntheticGraniteWorkflowSession;
import com.adobe.granite.workflow.exec.ScriptContextProvider;

@SuppressWarnings("serial")
@SlingServlet(resourceTypes = "acs-tools/components/aemfiddle", selectors = "run", methods = "POST")
@Reference(referenceInterface = ScriptContextProvider.class, policy = ReferencePolicy.DYNAMIC,
        cardinality = ReferenceCardinality.OPTIONAL_MULTIPLE)
public class RunFiddleServlet extends SlingAllMethodsServlet {
    private static final Logger log = LoggerFactory.getLogger(RunFiddleServlet.class);

    private static final String VAR_CLASSES = "/var/classes";

    private static final String COMPILED_JSP = "org/apache/jsp/apps/acs_002dtools/components/aemfiddle";

    @Reference
    private EventAdmin eventAdmin;

    private List<ScriptContextProvider> scriptContextProviders = new CopyOnWriteArrayList<ScriptContextProvider>();

    private BundleContext bundleContext;

    @Activate
    protected void activate(ComponentContext componentContext) {
        this.bundleContext = componentContext.getBundleContext();
    }

    @Override
    protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response)
            throws ServletException, IOException {

        // Clear any previously compiled fiddle scripts as they have a tendency to execute on first executions
        final ResourceResolver resourceResolver = request.getResourceResolver();
        this.clearCompiledFiddle(resourceResolver);

        final Resource resource = getResource(request);

        final String data = request.getParameter("scriptdata");
        final String ext = request.getParameter("scriptext");

        InMemoryScript script = InMemoryScript.set(ext, data);
        Resource scriptResource = script.toResource(resourceResolver);

        // doing this as a synchronous event so we ensure that
        // the JSP has been invalidated
        Map<String, String> props = Collections.singletonMap(SlingConstants.PROPERTY_PATH, script.getPath());
        eventAdmin.sendEvent(new Event(SlingConstants.TOPIC_RESOURCE_CHANGED, props));

        if (Boolean.parseBoolean(request.getParameter("runAsWorkflow"))) {
            SlingScript slingScript = scriptResource.adaptTo(SlingScript.class);

            DummyRequest req = new DummyRequest(scriptResource, resourceResolver);
            DummyResponse res = new DummyResponse();
            SlingScriptHelper helper = new ScriptHelper(bundleContext, slingScript, req, res);

            SlingBindings bindings = new SlingBindings();
            bindings.put("scriptHelper", helper);
            SyntheticGraniteWorkflowData workflowData = new SyntheticGraniteWorkflowData("JCR_PATH", resource.getPath());
            SyntheticGraniteWorkItem workItem = new SyntheticGraniteWorkItem(workflowData);
            bindings.put("graniteWorkItem", workItem);
            bindings.put("graniteWorkflowSession", new SyntheticGraniteWorkflowSession(resourceResolver));
            bindings.put("args", new String[0]);
            // todo - metadata map;
            bindings.setRequest(req);
            bindings.setResponse(res);

            // add additional variables to bindings via ScriptContextProvider services
            if (scriptContextProviders != null && scriptContextProviders.size() > 0) {
                for (ScriptContextProvider scriptContextProvider : scriptContextProviders) {
                    scriptContextProvider.addContext(bindings);
                }
            }
            bindings.put("log", new ResponseLogger(response));

            slingScript.eval(bindings);
        } else {
            fiddleAsNormalScript(resource, request, response);
        }
    }

    private void fiddleAsNormalScript(final Resource resource, SlingHttpServletRequest request,
            SlingHttpServletResponse response) throws ServletException, IOException {
        final RequestDispatcherOptions options = new RequestDispatcherOptions();
        options.setForceResourceType(Constants.PSEDUO_COMPONENT_PATH);
        options.setReplaceSelectors("");

        // Suppress ACS AEM Commons - Component Error Handler from capturing errors
        request.setAttribute("com.adobe.acs.commons.wcm.component-error-handler.suppress", true);

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

    protected void bindScriptContextProvider(ScriptContextProvider scriptContextProvider) {
        scriptContextProviders.add(scriptContextProvider);
    }

    protected void unbindScriptContextProvider(ScriptContextProvider scriptContextProvider) {
        scriptContextProviders.remove(scriptContextProvider);
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

}
