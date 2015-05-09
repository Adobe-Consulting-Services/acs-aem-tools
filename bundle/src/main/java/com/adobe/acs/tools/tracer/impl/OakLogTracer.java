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
package com.adobe.acs.tools.tracer.impl;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.turbo.TurboFilter;
import ch.qos.logback.core.spi.FilterReply;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.ConfigurationPolicy;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Property;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.request.RequestProgressTracker;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.Version;
import org.slf4j.Marker;
import org.slf4j.helpers.MessageFormatter;

@Component(
        label = "ACS AEM Tools - Oak Log Tracer",
        description = "Integrates Oak logging with RequestProgress Logs",
        policy = ConfigurationPolicy.REQUIRE,
        metatype = true
)
public class OakLogTracer {
    private static final String QUERY_LOGGER = "org.apache.jackrabbit.oak.query.QueryEngineImpl";
    private static final String WRITES_LOGGER = "org.apache.jackrabbit.oak.jcr.operations.writes";
    private static final String READS_LOGGER = "org.apache.jackrabbit.oak.jcr.operations.reads";

    /**
     * Following queries are internal to Oak and are fired for login/access control
     * etc. They should be ignored. With Oak 1.2+ such queries are logged at trace
     * level
     */
    private static final String[] IGNORABLE_QUERIES = {
            "SELECT * FROM [nt:base] WHERE [jcr:uuid] = $id",
            "SELECT * FROM [nt:base] WHERE PROPERTY([rep:members], 'WeakReference') = $uuid",
            "SELECT * FROM [rep:Authorizable]WHERE [rep:principalName] = $principalName",
    };

    private BundleContext bundleContext;

    private ServiceRegistration filterRegistration;

    private final AtomicReference<ServiceRegistration> logCollectorReg
            = new AtomicReference<ServiceRegistration>();

    private final AtomicInteger logCollectorRegCount = new AtomicInteger();

    private static final boolean PROP_LOG_READ_DEFAULT = false;
    @Property(label = "Log Reads",
            description = "Log what property are read",
            boolValue = PROP_LOG_READ_DEFAULT
    )
    private static final String PROP_LOG_READS = "logReads";
    private boolean logReads;

    private static final boolean PROP_LOG_WRITE_DEFAULT = false;
    @Property(label = "Log Writes",
            description = "Log what property are written to",
            boolValue = PROP_LOG_WRITE_DEFAULT
    )
    private static final String PROP_LOG_WRITES = "logWrites";
    private boolean logWrites;

    private final ThreadLocal<RequestContext> requestContextHolder = new ThreadLocal<RequestContext>();

    private boolean oakVersion_1_2_ORAbove;

    @Activate
    private void activate(Map<String, ?> config, BundleContext context) {
        this.bundleContext = context;
        this.oakVersion_1_2_ORAbove = isOakVersion1_2OrAbove(context);
        this.logReads = PropertiesUtil.toBoolean(config.get(PROP_LOG_READS), PROP_LOG_READ_DEFAULT);
        this.logWrites = PropertiesUtil.toBoolean(config.get(PROP_LOG_WRITES), PROP_LOG_WRITE_DEFAULT);

        Properties filterProps = new Properties();
        filterProps.setProperty("filter.scope", "REQUEST");
        filterRegistration = context.registerService(Filter.class.getName(), new DebugFilter(), filterProps);
    }

    @Deactivate
    private void deactivate() {
        if (filterRegistration != null) {
            filterRegistration.unregister();
            filterRegistration = null;
        }

        ServiceRegistration reg = logCollectorReg.getAndSet(null);
        if (reg != null) {
            reg.unregister();
        }

        requestContextHolder.remove();
    }

    /**
     * TurboFilters causes slowness as they are executed on critical path
     * Hence care is taken to only register the filter only when required
     * Logic below ensures that filter is only registered for the duration
     * or request which needs to be "monitored".
     * <p/>
     * If multiple such request are performed then also only one filter gets
     * registered
     */
    private void registerLogCollector() {
        synchronized (logCollectorRegCount) {
            int count = logCollectorRegCount.getAndIncrement();
            if (count == 0) {
                ServiceRegistration reg = bundleContext.registerService(TurboFilter.class.getName(),
                        new LogCollector(), null);
                logCollectorReg.set(reg);
            }
        }
    }

    private void unregisterLogCollector() {
        synchronized (logCollectorRegCount) {
            int count = logCollectorRegCount.decrementAndGet();
            if (count == 0) {
                ServiceRegistration reg = logCollectorReg.getAndSet(null);
                reg.unregister();
            }
        }
    }

    private boolean shouldTrack(SlingHttpServletRequest slingRequest) {
        //TODO Need to think of way when this should be enabled
        //say based on some request param or cookie. To be discussed
        //Currently once this class is enabled via config this would
        //always be enabled
        return true;
    }

    private static boolean isQueryLogger(Logger logger) {
        return QUERY_LOGGER.equals(logger.getName());
    }

    private static boolean isWritesLogger(Logger logger) {
        return WRITES_LOGGER.equals(logger.getName());
    }

    private static boolean isReadsLogger(Logger logger) {
        return READS_LOGGER.equals(logger.getName());
    }

    private static boolean isOakVersion1_2OrAbove(BundleContext context) {
        Version version = new Version(1, 2, 0);
        for (Bundle b : context.getBundles()) {
            if ("org.apache.jackrabbit.oak-core".equals(b.getSymbolicName())) {
                return version.compareTo(b.getVersion()) <= 0;
            }
        }
        return false;
    }

    private class DebugFilter implements Filter {

        @Override
        public void init(FilterConfig filterConfig) throws ServletException {
        }

        @Override
        public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                             FilterChain filterChain) throws IOException, ServletException {
            SlingHttpServletRequest slingRequest = (SlingHttpServletRequest) servletRequest;
            boolean logCollectorRegistered = false;
            RequestContext requestContext = null;
            try {
                if (shouldTrack(slingRequest)) {
                    requestContext = new RequestContext(slingRequest);
                    requestContextHolder.set(requestContext);
                    registerLogCollector();
                    logCollectorRegistered = true;
                }
                filterChain.doFilter(servletRequest, servletResponse);
            } finally {
                requestContextHolder.remove();
                if (logCollectorRegistered) {
                    unregisterLogCollector();
                    requestContext.done();
                }
            }
        }

        @Override
        public void destroy() {
        }
    }

    private class LogCollector extends TurboFilter {

        @Override
        public FilterReply decide(Marker marker, Logger logger, Level level,
                                  String format, Object[] params, Throwable t) {
            RequestContext request = requestContextHolder.get();
            if (request == null) {
                return FilterReply.NEUTRAL;
            }

            if (Level.DEBUG.equals(level) && isQueryLogger(logger)) {
                if (format == null) {
                    return FilterReply.ACCEPT;
                }

                //org.apache.jackrabbit.oak.query.QueryEngineImpl
                //LOG.debug("Parsing {} statement: {}", language, statement);
                if (format.startsWith("Parsing") && params != null && params.length == 2) {
                    request.logQuery(params[1].toString());
                }

            }

            if (logWrites && Level.TRACE.equals(level) && isWritesLogger(logger)) {
                if (format == null) {
                    return FilterReply.ACCEPT;
                }

                request.logWrite(MessageFormatter.arrayFormat(format, params).getMessage());
            }

            if (logReads && Level.TRACE.equals(level) && isReadsLogger(logger)) {
                if (format == null) {
                    return FilterReply.ACCEPT;
                }

                request.logReads(MessageFormatter.arrayFormat(format, params).getMessage());
            }

            return FilterReply.NEUTRAL;
        }
    }

    private class RequestContext {
        final SlingHttpServletRequest request;
        final RequestProgressTracker progressTracker;
        int queryCount;
        int saveCount;

        public RequestContext(SlingHttpServletRequest request) {
            this.request = request;
            this.progressTracker = request.getRequestProgressTracker();
        }

        public void logQuery(String msg) {
            if (ignorableQuery(msg)) {
                return;
            }
            queryCount++;
            progressTracker.log("JCR QUERY {0}", msg);
        }

        public void logWrite(String msg) {
            //crude way to detect session save call
            if (msg.endsWith("save")) {
                saveCount++;
            }
            progressTracker.log("JCR WRITE {0}", msg);
        }

        public void logReads(String msg) {
            progressTracker.log("JCR READ {0}", msg);
        }

        public void done() {
            if (queryCount > 0) {
                progressTracker.log("JCR Query Count {0}", queryCount);
            }
            if (saveCount > 0) {
                progressTracker.log("JCR Session Save Count {0}", saveCount);
            }
        }

        private boolean ignorableQuery(String msg) {
            //Oak 1.2 onward ignorable internal queries are logged at trace
            //hence would not be hit us at all
            if (oakVersion_1_2_ORAbove) {
                return false;
            }

            for (String ignorableQuery : IGNORABLE_QUERIES) {
                if (msg.contains(ignorableQuery)) {
                    return true;
                }
            }
            return false;
        }
    }
}
