package com.adobe.acs.tools.tagmaker.impl;

import org.apache.commons.lang.StringUtils;

public final class TagData {

    public static final TagData EMPTY = new TagData(null);

    private String name;
    private String title;
    private String locale;
    private String description;

    public TagData(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(final String locale) {
        this.locale = locale;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public boolean isValid() {
        return StringUtils.isNotBlank(this.getTitle()) && StringUtils.isNotBlank(this.getName());
    }
}
