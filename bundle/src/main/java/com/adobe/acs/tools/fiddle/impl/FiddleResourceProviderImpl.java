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

import com.adobe.granite.license.ProductInfo;
import com.adobe.granite.license.ProductInfoService;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.SlingConstants;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.observation.ResourceChange;
import org.apache.sling.spi.resource.provider.ResolveContext;
import org.apache.sling.spi.resource.provider.ResourceContext;
import org.apache.sling.spi.resource.provider.ResourceProvider;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.Version;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Component(
        immediate = true
)
@Service(value = { FiddleRefresher.class })
public class FiddleResourceProviderImpl extends ResourceProvider<Object> implements FiddleRefresher, org.apache.sling.api.resource.ResourceProvider {
    private static final Logger log = LoggerFactory.getLogger(FiddleResourceProviderImpl.   class);

    private static final String ROOT = "/apps/acs-tools/components/aemfiddle/fiddle";
    private static final Version AEM_63_VERSION = new Version(6, 3, 0);

    @Reference
    private EventAdmin eventAdmin;

    @Reference
    private ProductInfoService productInfoService;

    private volatile ServiceRegistration resourceProviderRegistration = null;
    private volatile ServiceRegistration legacyResourceProviderRegistration = null;

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
        if (resourceProviderRegistration != null) {
            // Only execute for non-legacy RP's
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
        } else {
             // AEM 6.2 Support - Legacy Sling Resource Provider Implementation
            final Map<String, String> props = Collections.singletonMap(SlingConstants.PROPERTY_PATH, path);
            eventAdmin.sendEvent(new Event(SlingConstants.TOPIC_RESOURCE_CHANGED, props));
        }
    }

    @Activate
    protected void activate(final BundleContext context) {
        final Dictionary<String, Object> props = new Hashtable<String, Object>();

        final ProductInfo[] productInfos = productInfoService.getInfos();
        if (productInfos.length > 0) {
            final Version actualVersion = productInfos[0].getVersion();

            if (actualVersion.compareTo(AEM_63_VERSION) < 0) {
                // AEM 6.2 Support - Legacy Sling Resource Provider Implementation
                props.put(org.apache.sling.api.resource.ResourceProvider.ROOTS, ROOT);
                legacyResourceProviderRegistration =
                        context.registerService(org.apache.sling.api.resource.ResourceProvider.class.getName(), this, props);
            } else {
                // Is >= 6.3.0, use new Resource Provider
                props.put(ResourceProvider.PROPERTY_NAME, "acs-aem-tools.aem-fiddle");
                props.put(ResourceProvider.PROPERTY_ROOT, ROOT);
                props.put(ResourceProvider.PROPERTY_REFRESHABLE, true);
                resourceProviderRegistration = context.registerService(ResourceProvider.class.getName(), this, props);
            }
        }
    }

    @Deactivate
    protected void deactivate(final BundleContext context) {
        // AEM 6.2 Support - Legacy Sling Resource Provider Implementation
        if (legacyResourceProviderRegistration != null) {
            resourceProviderRegistration.unregister();
        }

        if (resourceProviderRegistration != null) {
            resourceProviderRegistration.unregister();
        }
    }

    // AEM 6.2 Support - Legacy Sling Resource Provider Implementation
    public Resource getResource(ResourceResolver resourceResolver, HttpServletRequest request, String path) {
        return getResource(resourceResolver, path);
    }

    // AEM 6.2 Support - Legacy Sling Resource Provider Implementation
    public Resource getResource(ResourceResolver resourceResolver, String path) {
        InMemoryScript script = InMemoryScript.get();
        if (script != null && path.equals(script.getPath())) {
            return script.toResource(resourceResolver);
        }

        return null;
    }

    // AEM 6.2 Support - Legacy Sling Resource Provider Implementation
    public Iterator<Resource> listChildren(Resource parent) {
        return listChildren(null, parent);
    }
}