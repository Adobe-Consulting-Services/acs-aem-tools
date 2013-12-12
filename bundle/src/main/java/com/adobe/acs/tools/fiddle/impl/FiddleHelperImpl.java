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
