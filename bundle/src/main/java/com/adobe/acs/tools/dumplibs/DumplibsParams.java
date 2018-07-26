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

import com.adobe.granite.ui.clientlibs.LibraryType;
import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Obtains dumplibs servlet needed parameters from request
 */
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

    /**
     * Parse string of categories into an array of strings
     * @param categories
     * @return an array of categories
     */
    private  String[] getCategoriesArray(String categories){
        if(categories == null) return null;
        String[] categoriesArr = categories.split(",");
        for (int i = 0; i < categoriesArr.length; i++) {
            categoriesArr[i] = categoriesArr[i].trim();
        }
        return categoriesArr;
    }

    private boolean parseBoolean(String str){
        return "yes".equalsIgnoreCase(str) || "1".equalsIgnoreCase(str) || "true".equalsIgnoreCase(str);
    }

    private LibraryType getLibraryType(String typeString) {

        LibraryType type;
        try {
            return LibraryType.valueOf(typeString.toUpperCase());
        } catch (Exception e) {
            return  LibraryType.JS;
        }
    }

    /**
     * Returns comma delimited categories string
     * @return
     */
    public String getCategoriesString() {
        String[] categories = getCategories();
        return categories == null ? "null" : StringUtils.join(categories, ",");
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

}
