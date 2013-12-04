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
