package com.adobe.acs.tools.tagmaker.tagdataconverters;

import com.adobe.acs.tools.tagmaker.impl.TagData;

public interface TagDataConverter {

    String PROP_NAME = "name";

    String PROP_LABEL = "label";

    String getLabel();

    TagData convert(String data);

}
