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

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.SyntheticResource;

public class InMemoryScript {

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
            super(resourceResolver, path, "nt:file");
        }

        @SuppressWarnings("unchecked")
        @Override
        public <AdapterType> AdapterType adaptTo(Class<AdapterType> type) {
            if (type == InputStream.class) {
                return (AdapterType) new ByteArrayInputStream(data.getBytes(Charset.forName("UTF-8")));
            }
            return super.adaptTo(type);
        }
    }

}
