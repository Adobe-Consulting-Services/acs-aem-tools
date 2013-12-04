package com.adobe.acs.tools.fiddle.impl;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;

import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingConstants;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.request.RequestDispatcherOptions;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.api.wrappers.SlingHttpServletRequestWrapper;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

@SuppressWarnings("serial")
@SlingServlet(resourceTypes = "acs-tools/components/aemfiddle", selectors = "run", methods = "POST")
public class RunFiddleServlet extends SlingAllMethodsServlet {

    @Reference
    private EventAdmin eventAdmin;

    @Override
    protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response)
            throws ServletException, IOException {
        final Resource resource = getResource(request);

        final String data = request.getParameter("scriptdata");
        final String ext = request.getParameter("scriptext");

        InMemoryScript script = InMemoryScript.set(ext, data);

        // doing this as a synchronous event so we ensure that 
        // the JSP has been invalidated
        Map<String, String> props = Collections.singletonMap(
                SlingConstants.PROPERTY_PATH, script.getPath());
        eventAdmin.sendEvent(new Event(SlingConstants.TOPIC_RESOURCE_CHANGED, props));

        final RequestDispatcherOptions options = new RequestDispatcherOptions();
        options.setForceResourceType(Constants.PSEDUO_COMPONENT_PATH);
        options.setReplaceSelectors("");

        RequestDispatcher dispatcher = request.getRequestDispatcher(resource, options);
        dispatcher.forward(new GetRequest(request), response);
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
