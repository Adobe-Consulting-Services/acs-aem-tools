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

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Map;
import java.util.TreeMap;

@Component(
    label = "ACS AEM Tools - Tag Maker - Local Title and Name Converter"
)
@Properties({
        @Property(
                label = "Name",
                name = TagDataConverter.PROP_NAME,
                value = LocalesTitleAndNodeNameConverterImpl.NAME,
                propertyPrivate = true
        ),
        @Property(
                label = "Label",
                name = TagDataConverter.PROP_LABEL,
                value = LocalesTitleAndNodeNameConverterImpl.LABEL,
                propertyPrivate = true
        )
})
@Service
public class LocalesTitleAndNodeNameConverterImpl implements TagDataConverter {

    public static final String NAME = "acs-commons-locales-title-and-node-name";

    public static final String LABEL = "en[Title] fr[Titre] es[Titulo] {{ node-name }}";

    private static final Pattern ACCEPT_PATTERN = Pattern.compile(".+\\{\\{(.+)}}$");

    private static final Pattern PATTERN = Pattern.compile("\\{\\{(.+)}}$");
    
    private static final Pattern LOCALES_PATTERN = Pattern.compile("(.?[a-z]{2})(.?\\[(.*?)])");
    
    @Override
    public final String getLabel() {
        return LABEL;
    }

    @Override
    public final TagData convert(String data) {
        data = StringUtils.stripToEmpty(data);
        
        Map<String, String> titlesMap = new TreeMap<String, String>();
        
        String name;
		String title="";
		
        final Matcher matcher = PATTERN.matcher(data);

        if (matcher.find() && matcher.groupCount() == 1) {
            name = matcher.group(1);
            name = StringUtils.stripToEmpty(name);
        } else {
           return TagData.EMPTY;
        }
        
        String multipleTitles = PATTERN.matcher(data).replaceAll("");
        multipleTitles = StringUtils.stripToEmpty(multipleTitles);
        
        final Matcher matcherLocales = LOCALES_PATTERN.matcher(multipleTitles);
        
        while(matcherLocales.find()) {
        	String locale = StringUtils.stripToEmpty(matcherLocales.group(1));
        	String localeTitle = StringUtils.stripToEmpty(matcherLocales.group(3));
            // en set to default
        	if(locale.equals("en")){
            	title=localeTitle;
        	}else{
        		titlesMap.put(locale,localeTitle);
        	}
        }
        
        if(StringUtils.isEmpty(title)){
        	return TagData.EMPTY;
        } 

        final TagData tagData = new TagData(name);
        tagData.setTitle(title);
		tagData.setTranslations(titlesMap);
        return tagData;
    }

    @Override
    public boolean accepts(String data) {
        final Matcher matcher = ACCEPT_PATTERN.matcher(data);
        return matcher.matches();
    }
}
