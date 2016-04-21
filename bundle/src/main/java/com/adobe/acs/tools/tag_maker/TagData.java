/*
 * #%L
 * ACS AEM Tools Bundle
 * %%
 * Copyright (C) 2015 Adobe
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

package com.adobe.acs.tools.tag_maker;

import org.apache.commons.lang.StringUtils;
import java.util.Map;

public final class TagData {

    public static final TagData EMPTY = new TagData(null);

    private String name;
    private String title;
    private String locale;
    private String description;
	private Map<String, String> localizedTitles;
	
    public TagData(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(final String locale) {
        this.locale = locale;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }
    
    public Map<String, String> getLocalizedTitles() {
        return this.localizedTitles;
    }
    
    public void setLocalizedTitles(Map<String, String> localizedTitles) {
        this.localizedTitles = localizedTitles;
    }

    public boolean isValid() {
        return StringUtils.isNotBlank(this.getTitle()) && StringUtils.isNotBlank(this.getName());
    }
}
