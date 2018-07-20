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
