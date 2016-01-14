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
package com.adobe.acs.tools.jsp_code_display.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.apache.sling.settings.SlingSettingsService;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;

@SuppressWarnings("serial")
@SlingServlet(resourceTypes = "acs-tools/components/jsp-code-display", extensions = "json", methods = "POST",
        selectors = "fetch")
public class JspCodeServlet extends SlingAllMethodsServlet {

    @Reference
    private SlingSettingsService slingSettingsService;

    private File fileRoot;

    @Override
    protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response)
            throws ServletException, IOException {
        try {
            JSONObject result = new JSONObject();

            String line = request.getParameter("line");
            if (StringUtils.isNotBlank(line)) {
                Pattern pattern = Pattern.compile("^(.+)\\.(\\w+)\\.(\\w+)\\((\\w+\\.java):(\\d+)\\)$");
                Matcher matcher = pattern.matcher(line);
                if (matcher.matches()) {
                    String packageName = matcher.group(1);
                    String fileName = matcher.group(4);
                    String lineNumber = matcher.group(5);

                    String sourceFilePath = "/" + packageName.replace('.', '/') + "/" + fileName;

                    InputStream instream = null;

                    if (fileRoot != null) {
                        // we are on 6.1 or otherwise using the fs classloader
                        File classFile = new File(fileRoot, sourceFilePath);
                        if (classFile.exists()) {
                            instream = new FileInputStream(classFile);
                        }
                    } else {
                        ResourceResolver resourceResolver = request.getResourceResolver();
    
                        Resource classesRoot = resourceResolver.getResource("/var/classes");
                        if (classesRoot != null) {
                            if (classesRoot.getChild("org") != null) {
                                // assume this is 5.6.1 or before
                                Resource fileResource = resourceResolver.getResource(classesRoot.getPath() + sourceFilePath);
                                if (fileResource != null) {
                                    instream = fileResource.adaptTo(InputStream.class);
                                }
                            } else {
                                // assume this is 6.0, so take the first child node and try underneath that
                                Iterator<Resource> roots = classesRoot.listChildren();
                                if (roots.hasNext()) {
                                    Resource fileResource = resourceResolver.getResource(roots.next().getPath()
                                            + sourceFilePath);
                                    if (fileResource != null) {
                                        instream = fileResource.adaptTo(InputStream.class);
                                    }
                                }
                            }
                        }
                    }

                    if (instream != null) {
                        result.put("success", true);
                        result.put("lineNumber", Integer.parseInt(lineNumber));
                        result.put("code", IOUtils.toString(instream, "UTF-8"));
                        IOUtils.closeQuietly(instream);
                    } else {
                        result.put("success", false);
                        result.put("error", "Compiled JSP could not be found.");
                    }
                }
            } else {
                result.put("success", false);
                result.put("error", "No line was provided");
            }

            response.setContentType("application/json");
            result.write(response.getWriter());
        } catch (JSONException e) {
            throw new ServletException(e);
        }
    }

    @Activate
    protected void activate(ComponentContext ctx) {
        BundleContext bundleContext = ctx.getBundleContext();

        // this is less than ideal, but there's no better way to get to the fs classloader's data directory
        for (Bundle bundle : bundleContext.getBundles()) {
            if (bundle.getSymbolicName().equals("org.apache.sling.commons.fsclassloader")) {
                this.fileRoot = new File(slingSettingsService.getSlingHomePath(), "launchpad/felix/bundle"+bundle.getBundleId()+"/data/classes");
                break;
            }
        }
    }
}
