/*
 * #%L
 * ACS AEM Tools Bundle
 * %%
 * Copyright (C) 2014 Adobe
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
package com.adobe.acs.tools.qe.impl;

import java.io.IOException;

import javax.servlet.ServletException;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.io.JSONWriter;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.ComponentFactory;

@SuppressWarnings("serial")
@SlingServlet(paths = "/bin/acs-tools/qe/predicates.json")
public class PredicateListServlet extends SlingSafeMethodsServlet {

    private BundleContext bundleContext;

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        JSONWriter writer = new JSONWriter(response.getWriter());
        try {
            writer.array();
            ServiceReference[] refs = bundleContext.getServiceReferences(ComponentFactory.class.getName(),
                    "(component.factory=com.day.cq.search.eval.PredicateEvaluator/*)");
            for (ServiceReference ref : refs) {
                writer.value(ref.getProperty("component.factory").toString().substring(42));
            }
            writer.endArray();
        } catch (InvalidSyntaxException e) {
            throw new ServletException(e);
        } catch (JSONException e) {
            throw new ServletException(e);
        }

    }

    @Activate
    protected void activate(ComponentContext ctx) {
        this.bundleContext = ctx.getBundleContext();
    }

}
