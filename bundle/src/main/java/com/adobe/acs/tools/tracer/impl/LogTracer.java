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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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
import javax.servlet.http.HttpServletRequest;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.turbo.TurboFilter;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.helpers.CyclicBuffer;
import ch.qos.logback.core.spi.FilterReply;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.ConfigurationPolicy;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.PropertyUnbounded;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.request.RequestProgressTracker;
import org.apache.sling.commons.osgi.ManifestHeader;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.helpers.MessageFormatter;

/**
 * Tracer provides support for enabling the logs for specific category at specific level and
 * only for specific request. It provides a very fine level of control via config provided
 * as part of HTTP request around how the logging should be performed for given category.
 *
 * This is specially useful for those parts of the system which are involved in every request.
 * For such parts enabling the log at global level would flood the logs and create lots of noise.
 * Using Tracer one can enable log for that request which is required to be probed
 */
@Component(
        label = "ACS AEM Tools - Log Tracer",
        description = "Provides support for enabling log for specific loggers on per request basis",
        policy = ConfigurationPolicy.REQUIRE,
        metatype = true
)
public class LogTracer {
    /**
     * Request parameter name having comma separated value to determine list of tracers to
     * enable
     */
    public static final String PARAM_TRACER = "tracers";

    /**
     * Request param used to determine tracer config as part of request itself. Like
     *
     * org.apache.sling;level=trace,org.apache.jackrabbit
     */
    public static final String PARAM_TRACER_CONFIG = "tracerConfig";

    public static final String HEADER_TRACER_CONFIG = "X-AEM-Tracer-Config";

    public static final String HEADER_TRACER = "X-AEM-Tracers";


    private static final String QUERY_LOGGER = "org.apache.jackrabbit.oak.query.QueryEngineImpl";

    /**
     * Following queries are internal to Oak and are fired for login/access control
     * etc. They should be ignored. With Oak 1.2+ such queries are logged at trace
     * level (OAK-2304)
     */
    private static final String[] IGNORABLE_QUERIES = {
            "SELECT * FROM [nt:base] WHERE [jcr:uuid] = $id",
            "SELECT * FROM [nt:base] WHERE PROPERTY([rep:members], 'WeakReference') = $uuid",
            "SELECT * FROM [rep:Authorizable]WHERE [rep:principalName] = $principalName",
    };

    @Property(label = "Tracer Sets",
            description = "Default list of tracer sets configured. Tracer Set config confirms " +
                    "to following format. <set name> : <logger name>;level=<level name>, other loggers",
            unbounded = PropertyUnbounded.ARRAY,
            value = {
                    "oak-query : org.apache.jackrabbit.oak.query.QueryEngineImpl;level=debug",
                    "oak-writes : org.apache.jackrabbit.oak.jcr.operations.writes;level=trace"
            }
    )
    private static final String PROP_TRACER_SETS = "tracerSets";

    private static final boolean PROP_TRACER_ENABLED_DEFAULT = true;
    @Property(label = "Enabled",
            description = "Enable the Tracer",
            boolValue = PROP_TRACER_ENABLED_DEFAULT
    )
    private static final String PROP_TRACER_ENABLED = "enabled";

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(LogTracer.class);

    private final Map<String, TracerSet> tracers = new HashMap<String, TracerSet>();

    private BundleContext bundleContext;

    private ServiceRegistration slingFilterRegistration;

    private ServiceRegistration filterRegistration;

    private final AtomicReference<ServiceRegistration> logCollectorReg
            = new AtomicReference<ServiceRegistration>();

    private final AtomicInteger logCollectorRegCount = new AtomicInteger();

    private final ThreadLocal<TracerContext> requestContextHolder = new ThreadLocal<TracerContext>();

    @Activate
    private void activate(Map<String, ?> config, BundleContext context) {
        this.bundleContext = context;
        initializeTracerSet(config);
        boolean enabled = PropertiesUtil.toBoolean(config.get(PROP_TRACER_ENABLED), PROP_TRACER_ENABLED_DEFAULT);
        if (enabled) {
            registerFilters(context);
            LOG.info("Log tracer enabled. Required filters registered");
        }
    }

    @Deactivate
    private void deactivate() {
        if (slingFilterRegistration != null) {
            slingFilterRegistration.unregister();
            slingFilterRegistration = null;
        }

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

    TracerContext getTracerContext(String tracerSetNames, String tracerConfig) {
        //No config or tracer set name provided. So tracing not required
        if (tracerSetNames == null && tracerConfig == null) {
            return null;
        }

        List<TracerConfig> configs = new ArrayList<TracerConfig>();

        List<String> invalidNames = new ArrayList<String>();
        if (tracerSetNames != null) {
            for (String tracerSetName : tracerSetNames.split(",")) {
                TracerSet ts = tracers.get(tracerSetName.toLowerCase(Locale.ENGLISH));
                if (ts != null) {
                    configs.addAll(ts.configs);
                } else {
                    invalidNames.add(tracerSetName);
                }
            }
        }

        if (!invalidNames.isEmpty()) {
            LOG.warn("Invalid tracer set names passed [{}] as part of [{}]", invalidNames, tracerSetNames);
        }

        if (tracerConfig != null) {
            TracerSet ts = new TracerSet("custom", tracerConfig);
            configs.addAll(ts.configs);
        }

        return new TracerContext(configs.toArray(new TracerConfig[configs.size()]));
    }

    Map<String, TracerSet> getTracers() {
        return Collections.unmodifiableMap(tracers);
    }

    private void initializeTracerSet(Map<String, ?> config) {
        String[] tracerSetConfigs = PropertiesUtil.toStringArray(config.get(PROP_TRACER_SETS), new String[0]);

        for (String tracerSetConfig : tracerSetConfigs) {
            TracerSet tc = new TracerSet(tracerSetConfig);
            tracers.put(tc.name, tc);
        }
    }

    private void registerFilters(BundleContext context) {
        Properties slingFilterProps = new Properties();
        slingFilterProps.setProperty("filter.scope", "REQUEST");
        slingFilterProps.setProperty(Constants.SERVICE_DESCRIPTION, "Sling Filter required for Log Tracer");
        slingFilterRegistration = context.registerService(Filter.class.getName(),
                new SlingTracerFilter(), slingFilterProps);

        Properties filterProps = new Properties();
        filterProps.setProperty("pattern", "/.*");
        filterProps.setProperty(Constants.SERVICE_DESCRIPTION, "Servlet Filter required for Log Tracer");
        filterRegistration = context.registerService(Filter.class.getName(),
                new TracerFilter(), filterProps);
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

    private abstract class AbstractFilter implements Filter {
        @Override
        public void init(FilterConfig filterConfig) throws ServletException {
        }

        @Override
        public void destroy() {

        }

        protected void enableCollector(TracerContext tracerContext) {
            requestContextHolder.set(tracerContext);
            registerLogCollector();
        }

        protected void disableCollector() {
            requestContextHolder.remove();
            unregisterLogCollector();
        }
    }

    /**
     * Filter which registers at root and check for Tracer related params. If found to
     * be enabled then perform required setup for the logs to be captured.
     */
    private class TracerFilter extends AbstractFilter {

        @Override
        public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                             FilterChain filterChain) throws IOException, ServletException {

            //At generic filter level we just check for tracer hint via Header (later Cookie)
            //and not touch the request parameter to avoid eager initialization of request
            //parameter map

            HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
            TracerContext tracerContext = getTracerContext(httpRequest.getHeader(HEADER_TRACER),
                    httpRequest.getHeader(HEADER_TRACER_CONFIG));
            try {
                if (tracerContext != null) {
                    enableCollector(tracerContext);
                }
                filterChain.doFilter(servletRequest, servletResponse);
            } finally {
                if (tracerContext != null) {
                    disableCollector();
                }
            }
        }


    }

    /**
     * Sling level filter to extract the RequestProgressTracker and passes that to current
     * thread's TracerContent
     */
    private class SlingTracerFilter extends AbstractFilter {
        @Override
        public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                             FilterChain filterChain) throws IOException, ServletException {
            SlingHttpServletRequest slingRequest = (SlingHttpServletRequest) servletRequest;
            TracerContext tracerContext = requestContextHolder.get();

            boolean createdContext = false;

            //Check if the global filter created context based on HTTP headers. If not
            //then check from request params
            if (tracerContext == null) {
                tracerContext = getTracerContext(slingRequest.getParameter(PARAM_TRACER),
                        slingRequest.getParameter(PARAM_TRACER_CONFIG));
                if (tracerContext != null) {
                    createdContext = true;
                }
            }

            try {
                if (tracerContext != null) {
                    tracerContext.registerProgressTracker(slingRequest.getRequestProgressTracker());

                    //if context created in this filter then enable the collector
                    if (createdContext) {
                        enableCollector(tracerContext);
                    }
                }
                filterChain.doFilter(servletRequest, servletResponse);
            } finally {
                if (tracerContext != null) {
                    tracerContext.done();

                    if (createdContext) {
                        disableCollector();
                    }
                }
            }
        }
    }

    private class LogCollector extends TurboFilter {

        @Override
        public FilterReply decide(Marker marker, Logger logger, Level level,
                                  String format, Object[] params, Throwable t) {
            TracerContext tracer = requestContextHolder.get();
            if (tracer == null) {
                return FilterReply.NEUTRAL;
            }

            if (tracer.shouldLog(logger.getName(), level)) {
                if (format == null) {
                    return FilterReply.ACCEPT;
                }
                if (tracer.log(logger.getName(), format, params)) {
                    return FilterReply.ACCEPT;
                }
            }

            return FilterReply.NEUTRAL;
        }
    }

    static class TracerContext {
        private static final int LOG_BUFFER_SIZE = 50;
        /*
         * In memory buffer to store logs till RequestProgressTracker is registered.
         * This would be required for those case where TracerContext is created at
         * normal Filter level which gets invoked before Sling layer is hit.
         *
         * Later when Sling layer is hit and SlingTracerFilter is invoked
         * then it would register the RequestProgressTracker and then these inmemory logs
         * would be dumped there
         */
        private CyclicBuffer<String> buffer;
        private RequestProgressTracker progressTracker;
        int queryCount;
        final TracerConfig[] tracers;

        public TracerContext(TracerConfig[] tracers) {
            this.tracers = tracers;

            //Say if the list is like com.foo;level=trace,com.foo.bar;level=info.
            // Then first config would result in a match and later config would
            // not be able to suppress the logs from a child category
            //To handle such cases we sort the config. With having more depth i.e. more specific
            //coming first and others later
            Arrays.sort(tracers);
        }

        public boolean shouldLog(String logger, Level level) {
            for (TracerConfig tc : tracers) {
                MatchResult mr = tc.match(logger, level);
                if (mr == MatchResult.MATCH_LOG) {
                    return true;
                } else if (mr == MatchResult.MATCH_NO_LOG) {
                    return false;
                }
            }
            return false;
        }

        public boolean log(String logger, String format, Object[] params) {
            if (QUERY_LOGGER.equals(logger)
                    && params != null && params.length == 2) {
                return logQuery((String) params[1]);
            }
            return logWithLoggerName(logger, format, params);
        }

        public void done() {
            if (queryCount > 0) {
                progressTracker.log("JCR Query Count {0}", queryCount);
            }
        }

        /**
         * Registers the progress tracker and also logs all the in memory logs
         * collected so far to the tracker
         */
        public void registerProgressTracker(RequestProgressTracker requestProgressTracker) {
            this.progressTracker = requestProgressTracker;
            if (buffer != null) {
                for (String msg : buffer.asList()) {
                    progressTracker.log(msg);
                }
                buffer = null;
            }
        }

        private boolean logWithLoggerName(String loggerName, String format, Object... params) {
            String msg = MessageFormatter.arrayFormat(format, params).getMessage();
            msg = "[" + loggerName + "] " + msg;
            if (progressTracker == null) {
                if (buffer == null) {
                    buffer = new CyclicBuffer<String>(LOG_BUFFER_SIZE);
                }
                buffer.add(msg);
            } else {
                progressTracker.log(msg);
            }
            return true;
        }

        private boolean logQuery(String query) {
            if (ignorableQuery(query)) {
                return false;
            }
            queryCount++;
            logWithLoggerName("JCR", " Query {}", query);
            return true;
        }

        private boolean ignorableQuery(String msg) {
            for (String ignorableQuery : IGNORABLE_QUERIES) {
                if (msg.contains(ignorableQuery)) {
                    return true;
                }
            }
            return false;
        }
    }

    static class TracerSet {
        public static final String LEVEL = "level";
        final String name;
        final List<TracerConfig> configs;

        public TracerSet(String config) {
            int indexOfColon = config.indexOf(':');
            if (indexOfColon == -1) {
                throw new IllegalArgumentException("Invalid tracer config format. TracerSet " +
                        "name cannot be determined " + config);
            }

            name = config.substring(0, indexOfColon).toLowerCase().trim();
            configs = parseTracerConfigs(config.substring(indexOfColon + 1));
        }

        public TracerSet(String name, String config) {
            this.name = name;
            this.configs = parseTracerConfigs(config);
        }

        public TracerConfig getConfig(String category) {
            for (TracerConfig tc : configs) {
                if (tc.match(category)) {
                    return tc;
                }
            }
            return null;
        }

        private static List<TracerConfig> parseTracerConfigs(String config) {
            ManifestHeader parsedConfig = ManifestHeader.parse(config);
            List<TracerConfig> result = new ArrayList<TracerConfig>(parsedConfig.getEntries().length);
            for (ManifestHeader.Entry e : parsedConfig.getEntries()) {
                String category = e.getValue();

                //Defaults to Debug
                Level level = Level.valueOf(e.getAttributeValue(LEVEL));
                result.add(new TracerConfig(category, level));
            }
            return Collections.unmodifiableList(result);
        }
    }

    static class TracerConfig implements Comparable<TracerConfig> {
        final String loggerName;
        final Level level;
        final int depth;

        public TracerConfig(String loggerName, Level level) {
            this.loggerName = loggerName;
            this.level = level;
            this.depth = getDepth(loggerName);
        }

        public boolean match(String loggerName) {
            if (loggerName.startsWith(this.loggerName)) {
                return true;
            }
            return false;
        }

        public MatchResult match(String loggerName, Level level) {
            if (loggerName.startsWith(this.loggerName)) {
                if (level.isGreaterOrEqual(this.level)) {
                    return MatchResult.MATCH_LOG;
                }
                return MatchResult.MATCH_NO_LOG;
            }
            return MatchResult.NO_MATCH;
        }

        @Override
        public int compareTo(TracerConfig o) {
            int comp = depth > o.depth ? -1 : depth < o.depth ? 1 : 0;
            if (comp == 0) {
                comp = loggerName.compareTo(o.loggerName);
            }
            return comp;
        }

        private static int getDepth(String loggerName) {
            int depth = 0;
            int fromIndex = 0;
            while (true) {
                int index = getSeparatorIndexOf(loggerName, fromIndex);
                depth++;
                if (index == -1) {
                    break;
                }
                fromIndex = index + 1;
            }
            return depth;
        }

        /*
         * Taken from LoggerNameUtil. Though its accessible Logback is might not maintain
         * strict backward compatibility for such util classes. So copy the logic
         */
        private static int getSeparatorIndexOf(String name, int fromIndex) {
            int i = name.indexOf(CoreConstants.DOT, fromIndex);
            if (i != -1) {
                return i;
            } else {
                return name.indexOf(CoreConstants.DOLLAR, fromIndex);
            }
        }
    }

    enum MatchResult {
        MATCH_LOG,
        /**
         * Logger category matched but level not. So logging should
         * not be performed and no further TracerConfig should be matched for this
         */
        MATCH_NO_LOG,

        NO_MATCH
    }
}
