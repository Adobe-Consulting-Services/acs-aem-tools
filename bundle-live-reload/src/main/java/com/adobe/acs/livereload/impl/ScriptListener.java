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

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;

import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.SlingConstants;
import org.apache.sling.commons.json.JSONException;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.acs.livereload.LiveReloadServer;

@Component(immediate = true)
@Service
@Property(name = EventConstants.EVENT_TOPIC, value = "javax/script/ScriptEngineFactory/*")
public final class ScriptListener implements EventHandler {

    private static final Logger log = LoggerFactory.getLogger(ScriptListener.class);

    private BundleContext bundleContext;

    private ServiceRegistration extensionRegistration;

    @Reference
    private ScriptEngineManager scriptEngineManager;

    @Reference
    private LiveReloadServer server;

    public void handleEvent(Event event) {
        String topic = event.getTopic();
        if (topic.equals(SlingConstants.TOPIC_RESOURCE_CHANGED)) {
            try {
                String path = (String) event.getProperty(SlingConstants.PROPERTY_PATH);
                log.debug("script reload {}", path);
                server.triggerReload(path);
            } catch (JSONException e) {
                log.error("unable to reload from jsp", e);
            }
        } else if (extensionRegistration != null) {
            registerOrUpdate(getProperties());
        }
    }

    private List<String> getExtensions() {
        List<String> extensions = new ArrayList<String>();
        for (ScriptEngineFactory factory : scriptEngineManager.getEngineFactories()) {
            for (String extension : factory.getExtensions()) {
                if (!extensions.contains(extension)) {
                    extensions.add(extension);
                }
            }
        }
        return extensions;
    }

    private Dictionary<?, ?> getProperties() {
        StringBuilder filter = new StringBuilder();
        List<String> extensions = getExtensions();
        if (extensions.size() == 0) {
            return null;
        } else if (extensions.size() == 1) {
            filter.append("(path=*.").append(extensions.get(0)).append(")");
        } else {
            filter.append("(|");
            for (String extension : extensions) {
                filter.append("(path=*.").append(extension).append(")");
            }
            filter.append(")");
        }
        java.util.Properties properties = new java.util.Properties();
        properties.put(EventConstants.EVENT_TOPIC, SlingConstants.TOPIC_RESOURCE_CHANGED);
        properties.put(EventConstants.EVENT_FILTER, filter.toString());

        return properties;
    }

    private void registerOrUpdate(Dictionary<?, ?> properties) {
        if (properties == null) {
            if (extensionRegistration != null) {
                extensionRegistration.unregister();
                extensionRegistration = null;
            }
        } else if (extensionRegistration != null) {
            extensionRegistration.setProperties(properties);
        } else {
            extensionRegistration = bundleContext.registerService(EventHandler.class.getName(), this, properties);
        }
    }

    @Activate
    protected void activate(ComponentContext ctx) {
        bundleContext = ctx.getBundleContext();
        registerOrUpdate(getProperties());
    }

    @Deactivate
    protected void deactivate() {
        registerOrUpdate(null);
    }
}
