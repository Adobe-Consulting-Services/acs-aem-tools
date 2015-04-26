package com.adobe.acs.tools.tagmaker.tagdataconverters.impl;

import com.adobe.acs.tools.tagmaker.impl.TagData;
import com.adobe.acs.tools.tagmaker.tagdataconverters.TagDataConverter;
import org.apache.commons.lang.StringUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component(
    label = "ACS AEM Tools - Tag Maker - Handlebars Converter"
)
@Properties({
        @Property(
                label = "Name",
                name = TagDataConverter.PROP_NAME,
                value = HandlebarsConverterImpl.NAME,
                propertyPrivate = true
        ),
        @Property(
                label = "Label",
                name = TagDataConverter.PROP_LABEL,
                value = HandlebarsConverterImpl.LABEL,
                propertyPrivate = true
        )
})
@Service
public class HandlebarsConverterImpl implements TagDataConverter {

    public static final String NAME = "acs-commons-handlebars";

    public static final String LABEL = "Handlebars";

    private static final Pattern PATTERN = Pattern.compile("\\{\\{(.+)}}$");

    @Override
    public String getLabel() {
        return LABEL;
    }

    @Override
    public TagData convert(String data) {
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
}
