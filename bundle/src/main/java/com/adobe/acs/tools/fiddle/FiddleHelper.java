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
package com.adobe.acs.tools.fiddle;

import org.apache.sling.api.resource.Resource;

/**
 * Utility methods to support AEM Fiddle.
 *
 */
public interface FiddleHelper {
    /**
     * Returns the contents of the Code Template file as a String.
     *
     * @param resource the nt:file resource whose contents is the code template
     * @return The contents of the code template as a String
     */
    String getCodeTemplate(Resource resource);
}
