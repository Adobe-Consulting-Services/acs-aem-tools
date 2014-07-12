/*
 * #%L
 * ACS AEM Tools Bundle
 * %%
 * Copyright (C) 2013 Adobe
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

package com.adobe.acs.tools.test_page_generator.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.commons.json.JSONArray;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Parameters {
    private static final Logger log = LoggerFactory.getLogger(Parameters.class);

    private static final int DEFAULT_SAVE_THRESHOLD = 1000;

    private static final int DEFAULT_BUCKET_SIZE = 100;

    private final String rootPath;

    private final String template;

    private final int total;

    private final int bucketSize;

    private final int saveThreshold;

    private final Map<String, Object> properties;

    public Parameters(SlingHttpServletRequest request) throws JSONException {

        final String data = request.getParameter("json");

        JSONObject json = new JSONObject(data);

        rootPath = json.optString("rootPath", "");
        template = json.optString("template", "");
        total = json.optInt("total", 0);
        bucketSize = json.optInt("bucketSize", DEFAULT_BUCKET_SIZE);
        saveThreshold = json.optInt("saveThreshold", DEFAULT_SAVE_THRESHOLD);

        properties = new HashMap<String, Object>();

        JSONArray jsonArray = json.getJSONArray("properties");

        if (jsonArray != null) {
            for (int i = 0; i < jsonArray.length(); i++) {
                final JSONObject item = jsonArray.getJSONObject(i);

                boolean isMulti = item.optBoolean("multi", false);
                String name = item.optString("name", "");

                if (StringUtils.isNotBlank(name)) {
                    if (isMulti) {
                        final List<String> values = new ArrayList<String>();
                        for (String value : StringUtils.split(item.optString("value", ""), ",")) {
                            final String tmp = StringUtils.stripToNull(value);
                            if (tmp != null) {
                                values.add(value);
                            }
                        }

                        properties.put(name, values.toArray(new String[values.size()]));
                    } else {
                        String value = item.optString("value", "");
                        properties.put(name, value);
                    }
                }
            }
        }
    }

    public final String getRootPath() {
        if (!StringUtils.isBlank(this.rootPath)) {
            return rootPath;
        } else {
            return "/content" + "/" + Long.toString(new Date().getTime());
        }
    }

    public final String getTemplate() {
        return template;
    }

    public final int getTotal() {
        if (total > 0) {
            return total;
        } else {
            return 0;
        }
    }

    public final int getBucketSize() {
        if (bucketSize > 0) {
            return bucketSize;
        } else {
            return DEFAULT_BUCKET_SIZE;
        }
    }

    public final int getSaveThreshold() {
        if (saveThreshold > 0) {
            return saveThreshold;
        } else {
            return DEFAULT_SAVE_THRESHOLD;
        }
    }

    public final Map<String, Object> getProperties() {
        return properties;
    }

    public final String toString() {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);

        printWriter.println("Template: " + this.getTemplate());
        printWriter.println("Total Pages to Create: " + this.getTotal());
        printWriter.println("Bucket Size: " + this.getBucketSize());
        printWriter.println("Save Threshold: " + this.getSaveThreshold());

        return printWriter.toString();
    }
}
