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

package com.adobe.acs.tools.csv_asset_importer.impl;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.format.ISODateTimeFormat;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

public final class Column<T> {

    private static final String MULTI = "multi";

    private static final Pattern ISO_DATE_PATTERN =
            Pattern.compile("^[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}\\.[0-9]{3}[-+]{1}[0-9]{2}[:]{0,1}[0-9]{2}$");

    private String multiDelimiter = "|";

    private String raw;

    private String propertyName;

    private boolean multi = false;

    private Class dataType = String.class;

    private int index = 0;

    private boolean ignore = false;

    public Column(final String raw, final int index) {
        this.index = index;
        this.raw = StringUtils.trim(raw);

        String paramsStr = StringUtils.substringBetween(raw, "{{", "}}");
        String[] params = StringUtils.split(paramsStr, ":");

        if (StringUtils.isBlank(paramsStr)) {
            this.propertyName = this.getRaw();
        } else {
            this.propertyName = StringUtils.trim(StringUtils.substringBefore(this.getRaw(), "{{"));

            if (params.length == 2) {
                this.dataType = nameToClass(StringUtils.stripToEmpty(params[0]));
                this.multi = StringUtils.equalsIgnoreCase(StringUtils.stripToEmpty(params[1]), MULTI);
            }

            if (params.length == 1) {
                if (StringUtils.equalsIgnoreCase(MULTI, StringUtils.stripToEmpty(params[0]))) {
                    this.multi = true;
                } else {
                    this.dataType = nameToClass(StringUtils.stripToEmpty(params[0]));
                }
            }
        }
    }

    public T getData(String data) {
        return (T) toObjectType(data, this.getDataType());
    }

    public T[] getMultiData(String data) {
        final String[] vals = StringUtils.split(data, this.multiDelimiter);

        final List<T> list = new ArrayList<T>();

        for (String val : vals) {
            T obj = (T) this.toObjectType(val, this.getDataType());
            list.add(obj);
        }

        return list.toArray((T[]) Array.newInstance((this.getDataType()), 0));
    }

    private <T> T toObjectType(String data, Class<T> klass) {
        data = StringUtils.trim(data);

        if (Double.class.equals(klass)) {
            try {
                return klass.cast(Double.parseDouble(data));
            } catch (NumberFormatException ex) {
                return null;
            }
        } else if (Long.class.equals(klass)) {
            try {
                return klass.cast(Long.parseLong(data));
            } catch (NumberFormatException ex) {
                return null;
            }
        } else if (Integer.class.equals(klass)) {
            try {
                return klass.cast(Long.parseLong(data));
            } catch (NumberFormatException ex) {
                return null;
            }
        } else if (StringUtils.equalsIgnoreCase("true", data)) {
            return klass.cast(Boolean.TRUE);
        } else if (StringUtils.equalsIgnoreCase("false", data)) {
            return klass.cast(Boolean.FALSE);
        } else if ((Date.class.equals(Date.class)
                || Calendar.class.equals(Calendar.class))
                && ISO_DATE_PATTERN.matcher(data).matches()) {
            return klass.cast(ISODateTimeFormat.dateTimeParser().parseDateTime(data).toCalendar(Locale.US));
        } else {
            return klass.cast(data);
        }
    }

    private Class nameToClass(String name) {
        if (StringUtils.equalsIgnoreCase(name, "date")
                || StringUtils.equalsIgnoreCase(name, "calendar")) {
            return Calendar.class;
        } else if (StringUtils.equalsIgnoreCase(name, "double")) {
            return Double.class;
        } else if (StringUtils.equalsIgnoreCase(name, "long")
                || StringUtils.equalsIgnoreCase(name, "int")
                || StringUtils.equalsIgnoreCase(name, "integer")) {
            return Long.class;
        } else if (StringUtils.equalsIgnoreCase(name, "boolean")) {
            return Boolean.class;
        } else {
            return String.class;
        }
    }

    public String getRaw() {
        return raw;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public boolean isMulti() {
        return multi;
    }

    public Class getDataType() {
        return dataType;
    }

    public int getIndex() {
        return index;
    }

    public boolean isIgnore() {
        return ignore;
    }

    public void setIgnore(final boolean ignore) {
        this.ignore = ignore;
    }

    public void setMultiDelimiter(final String multiDelimiter) {
        this.multiDelimiter = multiDelimiter;
    }

    public static Map<String, Column> getColumns(final String[] row,
                                                 final String multiDelimiter,
                                                 final String[] ignoreProperties,
                                                 final String[] requiredProperties) throws CsvAssetImportException {
        final Map<String, Column> map = new HashMap<String, Column>();

        for (int i = 0; i < row.length; i++) {
            final Column col = new Column(row[i], i);

            col.setIgnore(ArrayUtils.contains(ignoreProperties, col.getPropertyName()));
            col.setMultiDelimiter(multiDelimiter);

            map.put(col.getPropertyName(), col);
        }

        final List<String> missingRequiredProperties = hasRequiredFields(map.values(), requiredProperties);

        if (!missingRequiredProperties.isEmpty()) {
            throw new CsvAssetImportException("Could not find required columns in CSV: "
                    + StringUtils.join(missingRequiredProperties, ", "));
        }

        return map;
    }

    private static List<String> hasRequiredFields(final Collection<Column> columns,
                                                  final String... requiredPropertyNames) {
        final List<String> missing = new ArrayList<String>();

        for (final String propertyName : requiredPropertyNames) {
            boolean found = false;

            for (final Column column : columns) {
                if (StringUtils.equals(propertyName, column.getPropertyName())) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                missing.add(propertyName);
            }
        }

        return missing;
    }
}