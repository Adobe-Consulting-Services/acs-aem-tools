package com.adobe.acs.tools.util.impl;

import com.adobe.acs.tools.util.AEMCapabilityHelper;
import org.apache.commons.lang.StringUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.jcr.api.SlingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;

@Component(
        label = "ACS AEM Tools - AEM Version Helper",
        description = "Provides information about the current AEM installation."
)
@Service
public class AEMCapabilityHelperImpl implements AEMCapabilityHelper {
    private static final Logger log = LoggerFactory.getLogger(AEMCapabilityHelperImpl.class);

    @Reference
    private SlingRepository slingRepository;

    @Override
    public final boolean isOak() throws RepositoryException {
        final String repositoryName = slingRepository.getDescriptorValue(SlingRepository.REP_NAME_DESC).getString();
        return StringUtils.equalsIgnoreCase("Apache Jackrabbit Oak", repositoryName);
    }
}
