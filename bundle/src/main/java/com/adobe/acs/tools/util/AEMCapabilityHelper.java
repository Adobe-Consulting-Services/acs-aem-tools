package com.adobe.acs.tools.util;

import javax.jcr.RepositoryException;

public interface AEMCapabilityHelper {

    /**
     * Determines if the AEM installation is running on an Apache Jackrabbit Oak-based repository
     * @return true is running on Oak
     * @throws RepositoryException
     */
    boolean isOak() throws RepositoryException;
}
