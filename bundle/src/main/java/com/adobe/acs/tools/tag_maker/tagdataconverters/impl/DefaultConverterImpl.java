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
import com.day.cq.commons.jcr.JcrUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;

@Component(
    label = "ACS AEM Tools - Tag Maker - Default Converter"
)
@Properties({
        @Property(
                label = "Name",
                name = TagDataConverter.PROP_NAME,
                value = DefaultConverterImpl.NAME,
                propertyPrivate = true
        ),
        @Property(
                label = "Label",
                name = TagDataConverter.PROP_LABEL,
                value = DefaultConverterImpl.LABEL,
                propertyPrivate = true
        )
})
@Service
public class DefaultConverterImpl implements TagDataConverter {

    public static final String NAME = "default";

    public static final String LABEL = "Default (Normal AEM Node Naming)";

    @Override
    public final String getLabel() {
        return LABEL;
    }

    @Override
    public final TagData convert(final String data) {
        final String name =
                StringUtils.lowerCase(JcrUtil.createValidName(StringUtils.strip(data)));

        final TagData tagData = new TagData(name);

        tagData.setTitle(data);

        return tagData;
    }

    @Override
    public boolean accepts(String data) {
        // Default accepts all formats
        return true;
    }
}
