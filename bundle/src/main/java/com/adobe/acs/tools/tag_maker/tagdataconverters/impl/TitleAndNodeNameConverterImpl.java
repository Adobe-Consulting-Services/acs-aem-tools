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
 *   http://www.apache.org/licenses/LICENSE-2.0
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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component(
    label = "ACS AEM Tools - Tag Maker - Title and Name Converter"
)
@Properties({
        @Property(
                label = "Name",
                name = TagDataConverter.PROP_NAME,
                value = TitleAndNodeNameConverterImpl.NAME,
                propertyPrivate = true
        ),
        @Property(
                label = "Label",
                name = TagDataConverter.PROP_LABEL,
                value = TitleAndNodeNameConverterImpl.LABEL,
                propertyPrivate = true
        )
})
@Service
public class TitleAndNodeNameConverterImpl implements TagDataConverter {

    public static final String NAME = "acs-commons-title-and-node-name";

    public static final String LABEL = "Title {{ node-name }}";

    private static final Pattern ACCEPT_PATTERN = Pattern.compile(".+\\{\\{(.+)}}$");

    private static final Pattern PATTERN = Pattern.compile("\\{\\{(.+)}}$");

    @Override
    public final String getLabel() {
        return LABEL;
    }

    @Override
    public final TagData convert(String data) {
        data = StringUtils.stripToEmpty(data);

        String name;

        final Matcher matcher = PATTERN.matcher(data);

        if (matcher.find() && matcher.groupCount() == 1) {
            name = matcher.group(1);
            name = StringUtils.stripToEmpty(name);
        } else {
           return TagData.EMPTY;
        }

        String title = PATTERN.matcher(data).replaceAll("");
        title = StringUtils.stripToEmpty(title);

        final TagData tagData = new TagData(name);
        tagData.setTitle(title);

        return tagData;
    }

    @Override
    public boolean accepts(String data) {
        final Matcher matcher = ACCEPT_PATTERN.matcher(data);
        return matcher.matches();
    }
}
