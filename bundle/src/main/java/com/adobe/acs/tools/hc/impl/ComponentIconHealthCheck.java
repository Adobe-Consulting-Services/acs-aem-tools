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
package com.adobe.acs.tools.hc.impl;

import java.util.Collection;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.ConfigurationPolicy;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.PropertyUnbounded;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.apache.sling.hc.api.HealthCheck;
import org.apache.sling.hc.api.Result;
import org.apache.sling.hc.util.FormattingResultLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.wcm.api.components.Component;
import com.day.cq.wcm.api.components.ComponentManager;

@org.apache.felix.scr.annotations.Component(metatype = true, label = "ACS AEM Tools - Component Icon Health Check",
        description = "This health check checks that all AEM Components have a icon.", policy = ConfigurationPolicy.REQUIRE)
@Properties({
        @Property(name = HealthCheck.NAME, value = "Component Icons", propertyPrivate = true),
        @Property(name = HealthCheck.TAGS, unbounded = PropertyUnbounded.ARRAY, label = "Tags",
                description = "Tags for this check to be used by composite health checks."),
        @Property(name = HealthCheck.MBEAN_NAME, value = "componentIcons", propertyPrivate = true) })
@Service
public class ComponentIconHealthCheck implements HealthCheck {

    private static final String FOUNDATION_PAGE_TYPE = "foundation/components/page";
    
    private static final String CLOUD_SERVICE_CONFIG_PAGE_TYPE = "cq/cloudserviceconfigs/components/configpage";

    private static final Logger log = LoggerFactory.getLogger(ComponentIconHealthCheck.class);

    @Property(label = "Root Page Types", description = "Root sling:resourceTypes for 'page' components",
            unbounded = PropertyUnbounded.ARRAY, value = { FOUNDATION_PAGE_TYPE, CLOUD_SERVICE_CONFIG_PAGE_TYPE })
    private static final String PROP_PAGE_TYPES = "page.types";

    @Property(label = "Paths", description = "Root component paths", unbounded = PropertyUnbounded.ARRAY)
    private static final String PROP_PATHS = "paths";

    @Reference
    private ResourceResolverFactory rrFactory;

    private String[] paths;

    private String[] pageTypes;

    @Activate
    protected void activate(Map<String, Object> properties) {
        this.paths = PropertiesUtil.toStringArray(properties.get(PROP_PATHS), new String[0]);
        this.pageTypes = PropertiesUtil.toStringArray(properties.get(PROP_PAGE_TYPES),
                new String[] { FOUNDATION_PAGE_TYPE, CLOUD_SERVICE_CONFIG_PAGE_TYPE });
    }

    @Override
    public Result execute() {
        final FormattingResultLog resultLog = new FormattingResultLog();
        int componentsWithoutIcons = 0;

        ResourceResolver resolver = null;
        try {
            resolver = rrFactory.getAdministrativeResourceResolver(null);
            ComponentManager compManager = resolver.adaptTo(ComponentManager.class);

            Collection<Component> components = compManager.getComponents();
            for (Component component : components) {
                String path = component.getPath();
                if (StringUtils.startsWithAny(path, paths)) {
                    String iconPath = component.getIconPath();

                    if (component.isEditable() && !isPageType(component) && iconPath == null) {
                        componentsWithoutIcons++;
                        resultLog.warn("Component {} is editable, but doesn't have an icon.", path);
                    }
                }
            }

            if (componentsWithoutIcons > 0) {
                resultLog.info("[You have component without icons.]");
            } else {
                resultLog.debug("All components have icons.");
            }
        } catch (Exception e) {
            log.error("Unable to list components", e);
        } finally {
            if (resolver != null) {
                resolver.close();
            }
        }

        return new Result(resultLog);
    }

    private boolean isPageType(Component component) {
        if (component == null) {
            return false;
        } else if (equalsAny(component.getResourceType(), pageTypes)) {
            return true;
        } else {
            return isPageType(component.getSuperComponent());
        }
    }

    private static boolean equalsAny(String text, String[] arr) {
        for (String string : arr) {
            if (text.equals(string)) {
                return true;
            }
        }
        return false;
    }

}
