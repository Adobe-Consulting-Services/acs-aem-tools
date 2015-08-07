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

package com.adobe.acs.tools.csv.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.request.RequestParameter;

import java.io.IOException;
import java.io.InputStream;

public class Parameters {

    private static final String DEFAULT_CHARSET = "UTF-8";

    private String charset = DEFAULT_CHARSET;

    private Character delimiter = null;

    private InputStream file;

    private String multiDelimiter;

    private Character separator;

    public Parameters(SlingHttpServletRequest request) throws IOException {

        final RequestParameter charsetParam = request.getRequestParameter("charset");
        final RequestParameter delimiterParam = request.getRequestParameter("delimiter");
        final RequestParameter fileParam = request.getRequestParameter("file");
        final RequestParameter multiDelimiterParam = request.getRequestParameter("multiDelimiter");
        final RequestParameter separatorParam = request.getRequestParameter("separator");

        this.charset = DEFAULT_CHARSET;
        if (charsetParam != null) {
            this.charset = StringUtils.defaultIfEmpty(charsetParam.toString(), DEFAULT_CHARSET);
        }

        this.delimiter = null;
        if (delimiterParam != null && StringUtils.isNotBlank(delimiterParam.toString())) {
            this.delimiter = delimiterParam.toString().charAt(0);
        }

        this.separator = null;
        if (separatorParam != null && StringUtils.isNotBlank(separatorParam.toString())) {
            this.separator = separatorParam.toString().charAt(0);
        }

        this.multiDelimiter = "|";
        if (multiDelimiterParam != null && StringUtils.isNotBlank(multiDelimiterParam.toString())) {
            this.multiDelimiter = multiDelimiterParam.toString();
        }

        if (fileParam != null && fileParam.getInputStream() != null) {
            this.file = fileParam.getInputStream();
        }
    }

    public final Character getSeparator() {
        return separator;
    }

    public final Character getDelimiter() {
        return delimiter;
    }

    public final String getCharset() {
        return charset;
    }

    public final InputStream getFile() {
        return file;
    }

    public final String getMultiDelimiter() {
        return multiDelimiter;
    }
}