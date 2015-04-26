package com.adobe.acs.tools.tagmaker.tagdataconverters.impl;

import com.adobe.acs.tools.tagmaker.impl.TagData;
import com.adobe.acs.tools.tagmaker.tagdataconverters.TagDataConverter;
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
    public String getLabel() {
        return LABEL;
    }

    @Override
    public TagData convert(final String data) {
        String title = data;

        String name = data;
        name = StringUtils.stripToEmpty(name);
        name = StringUtils.lowerCase(name);
        name = StringUtils.replace(name, "&", " and ");
        name = StringUtils.replace(name, "/", " or ");
        name = name.replaceAll("[^a-zA-Z0-9-]+", "-");
        name = StringUtils.stripEnd(name, "-");
        name = StringUtils.stripStart(name, "-");

        final TagData tagData = new TagData(name);

        tagData.setTitle(title);

        return tagData;
    }
}
