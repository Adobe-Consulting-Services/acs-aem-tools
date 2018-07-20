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
