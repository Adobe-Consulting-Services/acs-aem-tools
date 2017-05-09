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

import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.request.RequestParameter;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Parameters {

    public enum ImportStrategy {
        FULL, DELTA;
    };

    private static final String DEFAULT_CHARSET = "UTF-8";

    private static final long DEFAULT_THROTTLE = 0L;

    private static final int DEFAULT_BATCH_SIZE = 1000;

    private String uniqueProperty;

    private String relSrcPathProperty;

    private String absTargetPathProperty;

    private String mimeTypeProperty;
    
    private String skipProperty;

    private String multiDelimiter;

    private String fileLocation;


	private String[] ignoreProperties;

    private ImportStrategy importStrategy;
    
    private Character separator;

    private Character delimiter = null;

    private String charset = DEFAULT_CHARSET;

    private InputStream file;
    
    private String inFileMimeType;

    private Long throttle = DEFAULT_THROTTLE;

    private int batchSize = DEFAULT_BATCH_SIZE;
    
    private boolean updateBinary = false;

    public Parameters(SlingHttpServletRequest request) throws IOException {

        final RequestParameter charsetParam = request.getRequestParameter("charset");
        final RequestParameter delimiterParam = request.getRequestParameter("delimiter");
        final RequestParameter fileParam = request.getRequestParameter("file");
        final RequestParameter multiDelimiterParam = request.getRequestParameter("multiDelimiter");
        final RequestParameter separatorParam = request.getRequestParameter("separator");
        final RequestParameter fileLocationParam = request.getRequestParameter("fileLocation");
        final RequestParameter importStrategyParam = request.getRequestParameter("importStrategy");
        final RequestParameter updateBinaryParam = request.getRequestParameter("updateBinary");
        final RequestParameter mimeTypePropertyParam = request.getRequestParameter("mimeTypeProperty");
        final RequestParameter skipPropertyParam = request.getRequestParameter("skipProperty");
        final RequestParameter absTargetPathPropertyParam = request.getRequestParameter("absTargetPathProperty");
        final RequestParameter relSrcPathPropertyParam = request.getRequestParameter("relSrcPathProperty");
        final RequestParameter uniquePropertyParam = request.getRequestParameter("uniqueProperty");
        final RequestParameter ignorePropertiesParam = request.getRequestParameter("ignoreProperties");
        final RequestParameter throttleParam = request.getRequestParameter("throttle");
        final RequestParameter batchSizeParam = request.getRequestParameter("batchSize");

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
        
        this.importStrategy = ImportStrategy.FULL;
        if (importStrategyParam != null && StringUtils.isNotBlank(importStrategyParam.toString())) {
            this.importStrategy = ImportStrategy.valueOf(importStrategyParam.toString());
        }

        this.updateBinary = false;
        if (updateBinaryParam != null && StringUtils.isNotBlank(updateBinaryParam.toString())) {
            this.updateBinary = StringUtils.equalsIgnoreCase(updateBinaryParam.toString(), "true");
        }
        
        this.fileLocation = "/dev/null";
        if (fileLocationParam != null && StringUtils.isNotBlank(fileLocationParam.toString())) {
            this.fileLocation = fileLocationParam.toString();
        }

        this.skipProperty = null;
        if (skipPropertyParam != null && StringUtils.isNotBlank(skipPropertyParam.toString())) {
            skipProperty = StringUtils.stripToNull(skipPropertyParam.toString());
        }
        
        this.mimeTypeProperty = null;
        if (mimeTypePropertyParam != null && StringUtils.isNotBlank(mimeTypePropertyParam.toString())) {
            mimeTypeProperty = StringUtils.stripToNull(mimeTypePropertyParam.toString());
        }

        this.absTargetPathProperty = null;
        if (absTargetPathPropertyParam != null && StringUtils.isNotBlank(absTargetPathPropertyParam.toString())) {
            this.absTargetPathProperty = StringUtils.stripToNull(absTargetPathPropertyParam.toString());
        }

        this.relSrcPathProperty = null;
        if (relSrcPathPropertyParam != null && StringUtils.isNotBlank(relSrcPathPropertyParam.toString())) {
            this.relSrcPathProperty = StringUtils.stripToNull(relSrcPathPropertyParam.toString());
        }

        this.uniqueProperty = null;
        if (uniquePropertyParam != null && StringUtils.isNotBlank(uniquePropertyParam.toString())) {
            this.uniqueProperty = StringUtils.stripToNull(uniquePropertyParam.toString());
        }

        this.ignoreProperties = new String[]{CsvAssetImporterServlet.TERMINATED};
        if (ignorePropertiesParam != null && StringUtils.isNotBlank(ignorePropertiesParam.toString())) {
            final String[] tmp = StringUtils.split(StringUtils.stripToNull(ignorePropertiesParam.toString()), ",");
            final List<String> list = new ArrayList<String>();

            for (final String t : tmp) {
                String val = StringUtils.stripToNull(t);
                if (val != null) {
                    list.add(val);
                }
            }

            list.add(CsvAssetImporterServlet.TERMINATED);
            this.ignoreProperties = list.toArray(new String[]{});
        }

        if (fileParam != null && fileParam.getInputStream() != null) {
        	this.inFileMimeType = fileParam.getContentType();
            this.file = fileParam.getInputStream();
        }

        this.throttle = DEFAULT_THROTTLE;
        if (throttleParam != null && StringUtils.isNotBlank(throttleParam.toString())) {
            try {
                this.throttle = Long.valueOf(throttleParam.toString());
                if (this.throttle < 0) {
                    this.throttle = DEFAULT_THROTTLE;
                }
            } catch (Exception e) {
                this.throttle = DEFAULT_THROTTLE;
            }
        }

        batchSize = DEFAULT_BATCH_SIZE;
        if (batchSizeParam != null && StringUtils.isNotBlank(batchSizeParam.toString())) {
            try {
                this.batchSize = Integer.valueOf(batchSizeParam.toString());
                if (this.batchSize < 1) {
                    this.batchSize = DEFAULT_BATCH_SIZE;
                }
            } catch (Exception e) {
                this.batchSize = DEFAULT_BATCH_SIZE;
            }
        }
    }

    public final String getUniqueProperty() {
        return this.uniqueProperty;
    }

    public final String getRelSrcPathProperty() {
        return relSrcPathProperty;
    }

    public final String getAbsTargetPathProperty() {
        return absTargetPathProperty;
    }

    public final String getMimeTypeProperty() {
        return mimeTypeProperty;
    }

    public final String getSkipProperty() {
        return skipProperty;
    }

    public final String getFileLocation() {
        return fileLocation;
    }

    public final ImportStrategy getImportStrategy() { return importStrategy; }

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

    public final int getBatchSize() {
        return batchSize;
    }

    public final String getMultiDelimiter() {
        return multiDelimiter;
    }

    public final String[] getIgnoreProperties() {
        return ignoreProperties;
    }

    public final long getThrottle() {
        return throttle;
    }

    public boolean isUpdateBinary() {
        return updateBinary;
    }

    public String toString() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        
        pw.println();
        pw.println("CSV Asset Importer Config");
        pw.println("----------------------------------------");
        pw.println("File Provided: " + String.valueOf(this.getFile() != null));
        pw.println("Import Strategy: " + this.getImportStrategy());
        if (ImportStrategy.DELTA.equals(this.getImportStrategy())) {
            pw.println("Update Binary: " + this.isUpdateBinary());
        }
        pw.println("File Location: " + this.getFileLocation());
        pw.println("Unique Property: " + this.getUniqueProperty());
        pw.println("Absolute Target Path Property: " + this.getAbsTargetPathProperty());
        pw.println("Relative Src Path Property: " + this.getRelSrcPathProperty());
        pw.println("MIME Type Property: " + this.getMimeTypeProperty());
        pw.println("Skip Property: " + this.getSkipProperty());
        pw.println("Ignore Properties: " + Arrays.asList(this.getIgnoreProperties()));
        pw.println("Batch Size: " + this.getBatchSize());
        pw.println("Throttle: " + this.getThrottle());
        pw.println("Charset: " + this.getCharset());
        pw.println("Delimiter: " + this.getDelimiter());
        pw.println("Separator: " + this.getSeparator());

        return sw.toString();
    }

	/**
	 * @return the inFileMimeType
	 */
	public String getInFileMimeType() {
		return inFileMimeType;
	}

	/**
	 * @param fileLocation the fileLocation to set
	 */
	public void setFileLocation(String fileLocation) {
		this.fileLocation = fileLocation;
	}
}
