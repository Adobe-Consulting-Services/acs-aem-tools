/*
 * #%L
 * ACS AEM Tools Bundle
 * %%
 * Copyright (C) 2014 Adobe
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
        label = "ACS AEM Tools - AEM Capability Helper",
        description = "Provides information about the current AEM installation and what it can and can't do."
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
