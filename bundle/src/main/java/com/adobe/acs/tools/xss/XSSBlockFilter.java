package com.adobe.acs.tools.xss;

import com.day.cq.wcm.api.PageManager;
import java.io.IOException;
import javax.jcr.Session;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import org.apache.felix.scr.annotations.sling.SlingFilter;
import org.apache.felix.scr.annotations.sling.SlingFilterScope;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.ResourceResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Kills XSS attacks dead.
 */
@SlingFilter(
        scope = SlingFilterScope.REQUEST,
        description = "LQ.Com XSS block filter",
        order = -5000,
        metatype = false
        )
public class XSSBlockFilter implements javax.servlet.Filter {
    Logger log = LoggerFactory.getLogger(this.getClass());
    boolean debug = false;
    ResourceResolver resolver;
    Session session;
    PageManager pageManager;

    @Override
    public void init(FilterConfig fc) throws ServletException {
        // Do nothing
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        // It is possible you would want to modify this to only wrap the request for specific pages!
        chain.doFilter(new XSSProofRequest((SlingHttpServletRequest) request), response);
    }

    @Override
    public void destroy() {
        // Do nothing
    }

    private void initSession(SlingHttpServletRequest request) {
        resolver = request.getResourceResolver();
        session = resolver.adaptTo(Session.class);
        pageManager = resolver.adaptTo(PageManager.class);
    }

    private void disposeSession() {
        resolver = null;
        session = null;
        pageManager = null;
    }
}
