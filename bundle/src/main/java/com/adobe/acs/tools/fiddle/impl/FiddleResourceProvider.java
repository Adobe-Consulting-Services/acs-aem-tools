package com.adobe.acs.tools.fiddle.impl;

import java.util.Collections;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceProvider;
import org.apache.sling.api.resource.ResourceResolver;

@Component
@Service
@Property(name = "provider.roots", value = "/apps/acs-tools/components/aemfiddle/fiddle")
public class FiddleResourceProvider implements ResourceProvider {

    public Resource getResource(ResourceResolver resourceResolver, HttpServletRequest request, String path) {
        return getResource(resourceResolver, path);
    }

    public Resource getResource(ResourceResolver resourceResolver, String path) {
        InMemoryScript script = InMemoryScript.get();
        if (script != null) {
            if (path.equals(script.getPath())) {
                return script.toResource(resourceResolver);
            }
        }

        return null;
    }

    public Iterator<Resource> listChildren(Resource parent) {
        if (parent.getPath().equals(Constants.PSEDUO_COMPONENT_PATH)) {
            InMemoryScript script = InMemoryScript.get();
            if (script != null) {
                Resource scriptResource = script.toResource(parent.getResourceResolver());
                return Collections.singleton(scriptResource).iterator();
            }
        }
        return null;
    }

}
