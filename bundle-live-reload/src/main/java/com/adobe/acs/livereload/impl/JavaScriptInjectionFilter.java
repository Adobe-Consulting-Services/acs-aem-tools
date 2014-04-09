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
package com.adobe.acs.livereload.impl;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.wrappers.SlingHttpServletResponseWrapper;

public final class JavaScriptInjectionFilter implements Filter {

    private class BufferingResponse extends SlingHttpServletResponseWrapper {
        private StringWriter stringWriter;

        public BufferingResponse(final SlingHttpServletResponse slingResponse) {
            super(slingResponse);
        }

        public String getContents() {
            if (this.stringWriter != null) {
                return this.stringWriter.toString();
            }
            return null;
        }

        public PrintWriter getWriter() throws IOException {
            if (stringWriter == null) {
                stringWriter = new StringWriter();
            }
            return new PrintWriter(stringWriter);
        }

        @Override
        public void resetBuffer() {
            if (this.stringWriter != null) {
                this.stringWriter = new StringWriter();
            }
            super.resetBuffer();
        }
    }

    private int port;

    private String[] prefixes;

    public JavaScriptInjectionFilter(int port, String[] prefixes) {
        this.port = port;
        this.prefixes = prefixes;
    }

    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        if (!(servletRequest instanceof SlingHttpServletRequest)
                || !(servletResponse instanceof SlingHttpServletResponse)) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        final SlingHttpServletRequest slingRequest = (SlingHttpServletRequest) servletRequest;
        final SlingHttpServletResponse slingResponse = (SlingHttpServletResponse) servletResponse;

        if (!this.accepts(slingRequest)) {
            filterChain.doFilter(slingRequest, slingResponse);
            return;
        }

        final BufferingResponse capturedResponse = new BufferingResponse(slingResponse);

        filterChain.doFilter(slingRequest, capturedResponse);

        // Get contents
        final String contents = capturedResponse.getContents();

        if (contents != null) {
            if (StringUtils.contains(slingResponse.getContentType(), "html")) {

                final int bodyIndex = contents.indexOf("</body>");
                if (bodyIndex != -1) {

                    final PrintWriter printWriter = slingResponse.getWriter();

                    printWriter.write(contents.substring(0, bodyIndex));
                    printWriter.write(String.format(
                            "<script type=\"text/javascript\" src=\"http://%s:%s/livereload.js\"></script>",
                            slingRequest.getServerName(), port));
                    printWriter.write(contents.substring(bodyIndex));
                    return;
                }
            }
        }

        if (contents != null) {
            slingResponse.getWriter().write(contents);
        }

    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    private boolean accepts(final SlingHttpServletRequest slingRequest) {
        final Resource resource = slingRequest.getResource();

        if (!StringUtils.equalsIgnoreCase("get", slingRequest.getMethod())) {
            // Only inject on GET requests
            return false;
        } else if (!StringUtils.startsWithAny(resource.getPath(), prefixes)) {
            return false;
        } else if (StringUtils.equals(slingRequest.getHeader("X-Requested-With"), "XMLHttpRequest")) {
            // Do not inject into XHR requests
            return false;
        }
        return true;
    }

}
