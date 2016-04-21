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

package com.adobe.acs.tools.tag_maker.tagdataconverters.impl;

import com.adobe.acs.tools.tag_maker.TagData;
import com.adobe.acs.tools.tag_maker.tagdataconverters.TagDataConverter;
import org.apache.commons.lang.StringUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;

import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component(
        label = "ACS AEM Tools - Tag Maker - Localized Title and Name Converter"
)
@Properties({
        @Property(
                label = "Name",
                name = TagDataConverter.PROP_NAME,
                value = LocalizedTitleAndNodeNameConverterImpl.NAME,
                propertyPrivate = true
        ),
        @Property(
                label = "Label",
                name = TagDataConverter.PROP_LABEL,
                value = LocalizedTitleAndNodeNameConverterImpl.LABEL,
                propertyPrivate = true
        )
})
@Service
public class LocalizedTitleAndNodeNameConverterImpl implements TagDataConverter {
    public static final String NAME = "acs-commons-localized-title-and-node-name";

    public static final String LABEL = "en[Title] fr[Titre] es[Titulo] {{ node-name }}";

    private static final Pattern ACCEPT_PATTERN = Pattern.compile("^\\s*([a-zA-Z_-]+\\[[^]]+\\]\\s+)+\\{\\{(.+)}}\\s*$");

    private static final Pattern NODE_NAME_PATTERN = Pattern.compile("\\{\\{(.+)}}\\s?$");

    private static final Pattern LOCALIZED_TITLES_PATTERN = Pattern.compile("([a-zA-Z_-]+)\\[([^]]+)\\]");

    private static final String DEFAULT_LOCALE_KEY = "default";

    @Override
    public final String getLabel() {
        return LABEL;
    }

    @Override
    public final TagData convert(String data) {
        data = StringUtils.stripToEmpty(data);

        Map<String, String> localizedTitles = new TreeMap<String, String>();

        String name;
        String title = "";

        final Matcher matcher = NODE_NAME_PATTERN.matcher(data);

        if (matcher.find() && matcher.groupCount() == 1) {
            name = StringUtils.stripToEmpty(matcher.group(1));
        } else {
            return TagData.EMPTY;
        }

        final String rawLocalizedTitles = StringUtils.stripToEmpty(NODE_NAME_PATTERN.matcher(data).replaceAll(""));
        final Matcher localeMatch = LOCALIZED_TITLES_PATTERN.matcher(rawLocalizedTitles);

        String firstLocale = null;
        while (localeMatch.find()) {
            final String locale = StringUtils.stripToEmpty(localeMatch.group(1));
            final String localeTitle = StringUtils.stripToEmpty(localeMatch.group(2));

            if (firstLocale == null) {
                firstLocale = locale;
            }

            if (DEFAULT_LOCALE_KEY.equals(locale)) {
                title = localeTitle;
            } else {
                localizedTitles.put(locale, localeTitle);
            }
        }

        // It no default title was found, fall back to the first locale listed

        if (StringUtils.isEmpty(title) && firstLocale != null) {
            title = localizedTitles.get(firstLocale);
        }

        if (StringUtils.isEmpty(title)) {
            return TagData.EMPTY;
        }

        final TagData tagData = new TagData(name);
        tagData.setTitle(title);
        tagData.setLocalizedTitles(localizedTitles);
        return tagData;
    }

    @Override
    public boolean accepts(String data) {
        final Matcher matcher = ACCEPT_PATTERN.matcher(data);
        return matcher.matches();
    }
}
