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

import com.adobe.granite.ui.clientlibs.HtmlLibrary;
import com.adobe.granite.ui.clientlibs.LibraryType;
import java.util.List;

/**
 * A POJO for HtmlLibrary, for JSON serialization
 */
public class DumplibsHtmlLibrary {
    private String name;
    private LibraryType type;
    private String path;
    private String minifiedPath;
    private String libraryPath;
    private List<String> scripts;

    DumplibsHtmlLibrary (HtmlLibrary lib) {
        this.name = lib.getName();
        this.type = lib.getType();
        this.path = lib.getPath();
        this.minifiedPath = lib.getPath(true);
        this.libraryPath = lib.getLibraryPath();
        this.scripts = lib.getScripts();
    }


}
