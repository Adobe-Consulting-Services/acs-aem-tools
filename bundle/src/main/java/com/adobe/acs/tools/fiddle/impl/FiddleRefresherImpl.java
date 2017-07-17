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

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.observation.ResourceChange;
import org.apache.sling.spi.resource.provider.ResolveContext;
import org.apache.sling.spi.resource.provider.ResourceContext;
import org.apache.sling.spi.resource.provider.ResourceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

@Component
@Properties({
    @Property(
            name = ResourceProvider.PROPERTY_NAME,
            value = "acs-aem-tools.aem-fiddle"
    ),
    @Property(
            name = ResourceProvider.PROPERTY_ROOT,
            value = "/apps/acs-tools/components/aemfiddle/fiddle"
    )
})
@Service(value = {ResourceProvider.class, FiddleRefresher.class})
public class FiddleRefresherImpl extends ResourceProvider<Object> implements FiddleRefresher {
    private static final Logger log = LoggerFactory.getLogger(FiddleRefresherImpl.class);

    //private volatile ProviderContext providerContext;

    public Resource getResource(ResolveContext ctx, String path, ResourceContext resourceContext, Resource parent) {
        final ResourceResolver resourceResolver = ctx.getResourceResolver();
        final InMemoryScript script = InMemoryScript.get();
        if (script != null && path.equals(script.getPath())) {
            return script.toResource(resourceResolver);
        }

        return null;
    }

    public Iterator<Resource> listChildren(ResolveContext ctx, Resource parent) {
        if (parent.getPath().equals(Constants.PSEDUO_COMPONENT_PATH)) {
            InMemoryScript script = InMemoryScript.get();
            if (script != null) {
                Resource scriptResource = script.toResource(parent.getResourceResolver());
                return Collections.singleton(scriptResource).iterator();
            }
        }
        return null;
    }

    public void refresh(String path) {
        if (getProviderContext() != null) {
            final List<ResourceChange> resourceChangeList = new ArrayList<ResourceChange>();
            final ResourceChange resourceChange = new ResourceChange(
                    ResourceChange.ChangeType.CHANGED,
                    path,
                    false,
                    Collections.<String>emptySet(),
                    Collections.<String>emptySet(),
                    Collections.<String>emptySet()
            );

            resourceChangeList.add(resourceChange);
            getProviderContext().getObservationReporter().reportChanges(resourceChangeList, false);
        } else {
            log.warn("Unable to obtain a Observation Changer for AEM Fiddle script resource provider");
        }
    }
}
