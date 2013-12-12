package com.adobe.acs.tools.fiddle.impl;

import com.adobe.acs.tools.fiddle.FiddleHelper;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import java.io.IOException;

@Component
@Service
public class FiddleHelperImpl implements FiddleHelper {
    private static final Logger log = LoggerFactory.getLogger(FiddleHelperImpl.class);

    @Override
    public String getCodeTemplate(final Resource resource) {
        try {
            return com.adobe.acs.commons.util.ResourceDataUtil.getNTFileAsString(resource);
        } catch (RepositoryException ex) {
            log.error("Unable to get the AEM Fiddle code template from resource [ {} ] due to: {}",
                    resource.getPath(), ex);
            return "";
        } catch (IOException ex) {
            log.error("Unable to get the AEM Fiddle code template from resource [ {} ] due to: {}",
                    resource.getPath(), ex);
            return "";
        }
    }
}
