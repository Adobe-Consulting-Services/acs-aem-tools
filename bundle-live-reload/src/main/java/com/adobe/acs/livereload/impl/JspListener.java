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

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.SlingConstants;
import org.apache.sling.commons.json.JSONException;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.acs.livereload.LiveReloadServer;

@Component
@Service
@Properties({
        @Property(name = EventConstants.EVENT_TOPIC, value = "org/apache/sling/api/resource/Resource/CHANGED"),
        @Property(name = EventConstants.EVENT_FILTER, value = "(path=*.jsp)") })
public final class JspListener implements EventHandler {

    private static final Logger log = LoggerFactory.getLogger(JspListener.class);

    @Reference
    private LiveReloadServer server;

    public void handleEvent(Event event) {
        try {
            String path = (String) event.getProperty(SlingConstants.PROPERTY_PATH);
            log.debug("jsp reload {}", path);
            server.triggerReload(path);
        } catch (JSONException e) {
            log.error("unable to reload from jsp", e);
        }
    }
}
