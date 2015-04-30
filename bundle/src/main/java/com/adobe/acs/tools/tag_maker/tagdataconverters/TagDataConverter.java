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

package com.adobe.acs.tools.tag_maker.tagdataconverters;

import com.adobe.acs.tools.tag_maker.TagData;

public interface TagDataConverter {

    String PROP_NAME = "name";

    String PROP_LABEL = "label";

    /**
     * Returns the human friendly label for this Tag Data Converter; Used to allow human users to select what Tag
     * Data Converter to use.
     * @return the human friendly label
     */
    String getLabel();

    /**
     * Converts the String representation of the Tag Data into a Tag Data object.
     * @param data the raw String representation of the Tag Data
     * @return the TagData object derived from the data param
     */
    TagData convert(String data);

}
