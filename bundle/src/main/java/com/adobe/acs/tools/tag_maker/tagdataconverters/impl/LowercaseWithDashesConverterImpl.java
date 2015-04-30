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

@Component(
    label = "ACS AEM Tools - Tag Maker - Lowercase with Dashes Converter"
)
@Properties({
        @Property(
                label = "Name",
                name = TagDataConverter.PROP_NAME,
                value = LowercaseWithDashesConverterImpl.NAME,
                propertyPrivate = true
        ),
        @Property(
                label = "Label",
                name = TagDataConverter.PROP_LABEL,
                value = LowercaseWithDashesConverterImpl.LABEL,
                propertyPrivate = true
        )
})
@Service
public class LowercaseWithDashesConverterImpl implements TagDataConverter {

    public static final String NAME = "acs-commons-lowercase-dashes";

    public static final String LABEL = "Lowercase w/ Dashes";

    @Override
    public final String getLabel() {
        return LABEL;
    }

    @Override
    public final TagData convert(final String data) {
        String title = data;

        String name = data;
        name = StringUtils.stripToEmpty(name);
        name = StringUtils.lowerCase(name);
        name = StringUtils.replace(name, "&", " and ");
        name = StringUtils.replace(name, "/", " or ");
        name = StringUtils.replace(name, "%", " percent ");
        name = name.replaceAll("[^a-z0-9-]+", "-");
        name = StringUtils.stripEnd(name, "-");
        name = StringUtils.stripStart(name, "-");

        final TagData tagData = new TagData(name);

        tagData.setTitle(title);

        return tagData;
    }
}
