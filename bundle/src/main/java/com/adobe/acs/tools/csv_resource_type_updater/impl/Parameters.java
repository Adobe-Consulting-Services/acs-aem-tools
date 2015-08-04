/*
 * #%L
 * ACS AEM Tools Bundle
 * %%
 * Copyright (C) 2015 Adobe
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package com.adobe.acs.tools.csv_resource_type_updater.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.request.RequestParameter;

import java.io.IOException;

public class Parameters extends com.adobe.acs.tools.csv.impl.Parameters {

    private String DEFAULT_PATH = "/content";

    private String DEFAULT_PROPERTY_NAME = "sling:resourceType";

    private String path = DEFAULT_PATH;

    private String propertyName = DEFAULT_PROPERTY_NAME;

    public Parameters(SlingHttpServletRequest request) throws IOException {
        super(request);

        final RequestParameter pathParam = request.getRequestParameter("path");
        final RequestParameter propertyNameParam = request.getRequestParameter("propertyName");

        this.path = DEFAULT_PATH;
        if (pathParam != null) {
            this.path = StringUtils.defaultIfEmpty(pathParam.toString(), DEFAULT_PATH);
        }

        this.propertyName = DEFAULT_PROPERTY_NAME;
        if (propertyNameParam != null) {
            this.propertyName = StringUtils.defaultIfEmpty(propertyNameParam.toString(), DEFAULT_PROPERTY_NAME);
        }
    }

    public final String getPath() {
        return path;
    }

    public final String getPropertyName() {
        return propertyName;
    }
}