package com.adobe.acs.tools.tag_maker.tagdataconverters.impl;

import com.adobe.acs.tools.tag_maker.TagData;
import com.adobe.acs.tools.tag_maker.tagdataconverters.TagDataConverter;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class LocalesTitleAndNodeNameConverterImplTest {
    TagDataConverter converter = new LocalizedTitleAndNodeNameConverterImpl();

    @Test
    public void convertWithDefault() throws Exception {

        String data = "default[Default Title] en[English Title] fr[French Title] es[Spanish Title] {{ my-title }}";

        Map<String, String> expected = new HashMap<String, String>();
        expected.put("en", "English Title");
        expected.put("fr", "French Title");
        expected.put("es", "Spanish Title");

        TagData tagData = converter.convert(data);

        Map<String, String> actual = tagData.getLocalizedTitles();

        assertEquals("Default Title", tagData.getTitle());
        assertEquals(expected, actual);
    }

    @Test
    public void convertWithoutDefault() throws Exception {

        String data = " en[English Title] fr[French Title] es[Spanish Title] {{my-title}} ";

        Map<String, String> expected = new HashMap<String, String>();
        expected.put("en", "English Title");
        expected.put("fr", "French Title");
        expected.put("es", "Spanish Title");

        TagData tagData = converter.convert(data);

        Map<String, String> actual = tagData.getLocalizedTitles();

        assertEquals("English Title", tagData.getTitle());
        assertEquals(expected, actual);
    }

    @Test
    public void accepts() throws Exception {
        assertTrue(converter.accepts("en[My Title] {{my-title}}"));
        assertTrue(converter.accepts("en[My Title] {{ my-title }}"));
        assertTrue(converter.accepts("en[Title] fr[Titre] es[Titulo] {{ node-name }}"));
        assertTrue(converter.accepts("en_US[Title] {{ node-name }}"));
        assertTrue(converter.accepts("default[Title]  en[Title] fr[Titre] es[Titulo] {{ node-name }}"));
        assertFalse(converter.accepts("en![Title] {{ node-name }}"));
        assertFalse(converter.accepts("en us[Title] {{ node-name }}"));

    }
}