/*
 * #%L
 * ACS AEM Tools Bundle
 * %%
 * Copyright (C) 2018 Adobe
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
package com.adobe.acs.tools.dumplibs;

import com.adobe.granite.ui.clientlibs.ClientLibrary;
import com.adobe.granite.ui.clientlibs.LibraryType;
import java.util.Set;

/**
 * A POJO for ClientLibrary, for JSON serialization
 */
public class DumplibsClientLibrary {

    private String path;
    private Set<LibraryType> types;
    private String[] categories;
    private String[] channels;

    DumplibsClientLibrary(ClientLibrary lib) {
        this.path = lib.getPath();
        this.types = lib.getTypes();
        this.categories = lib.getCategories();
        this.channels = lib.getChannels();
    }

}
