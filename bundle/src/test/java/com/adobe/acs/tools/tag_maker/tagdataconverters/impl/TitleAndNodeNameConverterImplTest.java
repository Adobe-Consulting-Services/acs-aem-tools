package com.adobe.acs.tools.tag_maker.tagdataconverters.impl;

import com.adobe.acs.tools.tag_maker.TagData;
import com.adobe.acs.tools.tag_maker.tagdataconverters.TagDataConverter;
import junit.framework.TestCase;

public class TitleAndNodeNameConverterImplTest extends TestCase {

    TagDataConverter converter = new TitleAndNodeNameConverterImpl();

    public void testConvert() throws Exception {

        String expectedTitle = "Hello World";
        String expectedName = "hello-world";

        TagData actual = converter.convert("Hello World {{ hello-world }}");

        assertEquals(expectedTitle, actual.getTitle());
        assertEquals(expectedName, actual.getName());
    }

}