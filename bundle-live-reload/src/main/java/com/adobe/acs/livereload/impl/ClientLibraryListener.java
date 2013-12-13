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
import com.day.cq.widget.ClientLibrary;
import com.day.cq.widget.HtmlLibraryManager;
import com.day.cq.widget.LibraryType;

@Component
@Service
@Properties({ @Property(name = EventConstants.EVENT_TOPIC,
        value = "com/adobe/granite/ui/librarymanager/INVALIDATED")
})
public final class ClientLibraryListener implements EventHandler {

    private static final Logger log = LoggerFactory.getLogger(ClientLibraryListener.class);

    @Reference
    private LiveReloadServer server;

    @Reference
    private HtmlLibraryManager htmlLibraryManager;

    public void handleEvent(Event event) {
        try {
            boolean minified = htmlLibraryManager.isMinifyEnabled();

            String path = (String) event.getProperty(SlingConstants.PROPERTY_PATH);
            log.info("Client Library at {} invalidated. Sending reload.", path);

            ClientLibrary library = htmlLibraryManager.getLibraries().get(path);
            if (library != null) {
                for (LibraryType type : library.getTypes()) {
                    String includePath = library.getIncludePath(type, minified);
                    server.triggerReload(includePath);
                }
            }

        } catch (JSONException e) {
            log.info("Unable to send reload", e);
        }
    }

}
