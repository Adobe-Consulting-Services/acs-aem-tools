package com.adobe.acs.tools.tagmaker.tagdataconverters.impl;

import com.adobe.acs.tools.tagmaker.impl.TagData;
import com.adobe.acs.tools.tagmaker.tagdataconverters.TagDataConverter;
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
    public String getLabel() {
        return LABEL;
    }

    @Override
    public TagData convert(final String data) {
        final String name =
                StringUtils.lowerCase(JcrUtil.createValidName(StringUtils.strip(data)));

        final TagData tagData = new TagData(name);

        tagData.setTitle(data);

        return tagData;
    }
}
