package com.adobe.acs.tools.dumplibs;

import com.adobe.granite.ui.clientlibs.LibraryType;
import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DumplibsParams {

    private static final Logger log = LoggerFactory.getLogger(DumplibsParams.class);


    private static final String PARAM_CATEGORIES = "categories";
    private static final String PARAM_PATH = "path";
    private static final String PARAM_TYPE = "type";
    private static final String PARAM_TRANS = "trans";
    private static final String PARAM_THEMED = "themed";

    private String[] categories;
    private String path;
    private LibraryType type;
    private boolean trans;
    private boolean themed;

    DumplibsParams(SlingHttpServletRequest request){
        path = request.getParameter(PARAM_PATH);
        categories = getCategoriesArray(request.getParameter(PARAM_CATEGORIES));

        themed = parseBoolean(request.getParameter(PARAM_THEMED));
        trans = parseBoolean(request.getParameter(PARAM_TRANS));
        type = getLibraryType(request.getParameter(PARAM_TYPE));
    }

    private  String[] getCategoriesArray(String categories){
        if(categories == null) return null;
        String[] categoriesArr = categories.split(",");
        for (int i = 0; i < categoriesArr.length; i++) {
            categoriesArr[i] = categoriesArr[i].trim();
        }
        return categoriesArr;
    }

    private LibraryType getLibraryType(String typeString) {

        LibraryType type;
        try {
            return LibraryType.valueOf(typeString.toUpperCase());
        } catch (Exception e) {
            return  LibraryType.JS;
        }
    }

    private boolean parseBoolean(String str){
        return "yes".equalsIgnoreCase(str) || "1".equalsIgnoreCase(str) || "true".equalsIgnoreCase(str);
    }

    public String[] getCategories() {
        return categories;
    }

    public String getPath() {
        return path;
    }

    public LibraryType getType() {
        return type;
    }

    public boolean isTrans() {
        return trans;
    }

    public boolean isThemed() {
        return themed;
    }

    public String getCategoriesString() {
        String[] categories = getCategories();
        return categories == null ? "null" : StringUtils.join(categories, ",");
    }
}
