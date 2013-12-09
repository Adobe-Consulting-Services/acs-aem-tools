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

import com.day.cq.wcm.api.WCMMode;

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
        GetRequest getRequest = new GetRequest(request);
        WCMMode.DISABLED.toRequest(getRequest);
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
