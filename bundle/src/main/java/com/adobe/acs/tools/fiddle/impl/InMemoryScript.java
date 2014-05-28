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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Map;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.SyntheticResource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.ValueMapDecorator;

import com.day.cq.commons.jcr.JcrConstants;

public class InMemoryScript {

    private static final Charset charset = Charset.forName("UTF-8");

    private static ThreadLocal<InMemoryScript> holder = new ThreadLocal<InMemoryScript>();

    private final String extension;
    private final String data;
    private final String path;

    public String getExtension() {
        return extension;
    }

    public String getData() {
        return data;
    }

    private InMemoryScript(String ext, String data) {
        this.extension = ext;
        this.data = data;
        this.path = Constants.SCRIPT_PATH + "." + extension;
    }

    public static InMemoryScript set(String ext, String data) {
        InMemoryScript value = new InMemoryScript(ext, data);
        holder.set(value);
        return value;
    }

    public static InMemoryScript get() {
        return holder.get();
    }

    public static void clear() {
        holder.set(null);
    }

    public Resource toResource(ResourceResolver resourceResolver) {
        return new ScriptResource(resourceResolver);
    }

    public String getPath() {
        return path;
    }

    private class ScriptResource extends SyntheticResource {

        public ScriptResource(ResourceResolver resourceResolver) {
            super(resourceResolver, path, JcrConstants.NT_FILE);
        }

        @SuppressWarnings("unchecked")
        @Override
        public <AdapterType> AdapterType adaptTo(Class<AdapterType> type) {
            if (type == InputStream.class) {
                return (AdapterType) new ByteArrayInputStream(data.getBytes(charset));
            }
            return super.adaptTo(type);
        }

        @Override
        public Resource getChild(String relPath) {
            if (JcrConstants.JCR_CONTENT.equals(relPath)) {
                return new ScriptPropertiesResource(getResourceResolver());
            } else {
                return null;
            }
        }
    }

    private class ScriptPropertiesResource extends SyntheticResource {

        private final ValueMap properties;

        public ScriptPropertiesResource(ResourceResolver resourceResolver) {
            super(resourceResolver, path + "/" + JcrConstants.JCR_CONTENT, JcrConstants.NT_UNSTRUCTURED);
            Map<String, Object> map = Collections.<String, Object>singletonMap(JcrConstants.JCR_ENCODING, charset.name());
            properties = new ValueMapDecorator(map);
        }

        @SuppressWarnings("unchecked")
        @Override
        public <AdapterType> AdapterType adaptTo(Class<AdapterType> type) {
            if (type == ValueMap.class) {
                return (AdapterType) properties;
            }
            return super.adaptTo(type);
        }

    }

}
