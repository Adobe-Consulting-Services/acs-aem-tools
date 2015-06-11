package com.adobe.acs.tools.tag_maker.tagdataconverters.impl;

import com.adobe.acs.tools.tag_maker.TagData;
import com.adobe.acs.tools.tag_maker.tagdataconverters.TagDataConverter;
import junit.framework.TestCase;

public class LowercaseWithDashesConverterImplTest extends TestCase {

    TagDataConverter converter = new LowercaseWithDashesConverterImpl();

    public void testConvert() throws Exception {

        String expectedTitle = "?! This  is @ crazy title #  WITH funky (& weird/bizarre) chArs!? in it!!!!  ";
        String expectedName = "this-is-crazy-title-with-funky-and-weird-or-bizarre-chars-in-it";

        TagData actual = converter.convert(expectedTitle);

        assertEquals(expectedTitle, actual.getTitle());
        assertEquals(expectedName, actual.getName());
    }

}