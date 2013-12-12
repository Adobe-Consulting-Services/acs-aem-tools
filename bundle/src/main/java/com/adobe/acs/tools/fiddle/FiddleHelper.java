package com.adobe.acs.tools.fiddle;

import org.apache.sling.api.resource.Resource;

public interface FiddleHelper {
    /**
     * Returns the contents of the Code Template file as a String.
     *
     * @param resource the nt:file resource whose contents is the code template
     * @return The contents of the code template as a String
     */
    String getCodeTemplate(Resource resource);
}
