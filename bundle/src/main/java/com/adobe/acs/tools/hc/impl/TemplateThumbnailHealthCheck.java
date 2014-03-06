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
import org.apache.felix.scr.annotations.Component;
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

import com.day.cq.wcm.api.PageManager;
import com.day.cq.wcm.api.Template;

@Component(metatype = true, label = "ACS AEM Tools - Template Thumbnail Health Check",
        description = "This health check checks that all AEM Templates have a thumbnail.",
        policy = ConfigurationPolicy.REQUIRE)
@Properties({
        @Property(name = HealthCheck.NAME, value = "Template Thumbnails", propertyPrivate = true),
        @Property(name = HealthCheck.TAGS, unbounded = PropertyUnbounded.ARRAY, label = "Tags",
                description = "Tags for this check to be used by composite health checks."),
        @Property(name = HealthCheck.MBEAN_NAME, value = "templateThumbnails", propertyPrivate = true) })
@Service
public class TemplateThumbnailHealthCheck implements HealthCheck {

    private static final Logger log = LoggerFactory.getLogger(TemplateThumbnailHealthCheck.class);

    @Property(label = "Paths", description = "Root component paths", unbounded = PropertyUnbounded.ARRAY)
    private static final String PROP_PATHS = "paths";

    @Reference
    private ResourceResolverFactory rrFactory;

    private String[] paths;

    @Activate
    protected void activate(Map<String, Object> properties) {
        this.paths = PropertiesUtil.toStringArray(properties.get(PROP_PATHS), new String[0]);
    }

    @Override
    public Result execute() {
        final FormattingResultLog resultLog = new FormattingResultLog();
        int templatesWithoutIcons = 0;

        ResourceResolver resolver = null;
        try {
            resolver = rrFactory.getAdministrativeResourceResolver(null);
            PageManager pageManager = resolver.adaptTo(PageManager.class);

            Collection<Template> templates = pageManager.getTemplates(null);
            for (Template template : templates) {
                String path = template.getPath();
                if (StringUtils.startsWithAny(path, paths)) {
                    String thumbnailPath = template.getThumbnailPath();

                    if (thumbnailPath == null) {
                        templatesWithoutIcons++;
                        resultLog.warn("Template {} doesn't have a thumbnail.", path);
                    }
                }
            }

            if (templatesWithoutIcons > 0) {
                resultLog.info("[You have templates without thumbnails.]");
            } else {
                resultLog.debug("All templates have thumbnails.");
            }
        } catch (Exception e) {
            log.error("Unable to list templates", e);
        } finally {
            if (resolver != null) {
                resolver.close();
            }
        }

        return new Result(resultLog);
    }
}
