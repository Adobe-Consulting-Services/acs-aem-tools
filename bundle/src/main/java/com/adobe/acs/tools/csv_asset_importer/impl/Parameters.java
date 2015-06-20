package com.adobe.acs.tools.csv_asset_importer.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.request.RequestParameter;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class Parameters {

    private static final String DEFAULT_CHARSET = "UTF-8";

    private static final long DEFAULT_THROTTLE = 0L;

    private static final int DEFAULT_BATCH_SIZE = 1000;

    private String uniqueProperty;

    private String relSrcPathProperty;

    private String absTargetPathProperty;

    private String mimeTypeProperty;

    private String multiDelimiter;

    private String fileLocation;

    private String[] ignoreProperties;

    private boolean fullImport;

    private Character separator;

    private Character delimiter = null;

    private String charset = DEFAULT_CHARSET;

    private InputStream file;

    private Long throttle = 0L;

    private int batchSize = 1000;

    public Parameters(SlingHttpServletRequest request) throws IOException {

        final RequestParameter charsetParam = request.getRequestParameter("charset");
        final RequestParameter delimiterParam = request.getRequestParameter("delimiter");
        final RequestParameter fileParam = request.getRequestParameter("file");
        final RequestParameter multiDelimiterParam = request.getRequestParameter("multiDelimiter");
        final RequestParameter separatorParam = request.getRequestParameter("separator");
        final RequestParameter fileLocationParam = request.getRequestParameter("fileLocation");
        final RequestParameter fullImportParam = request.getRequestParameter("fullImport");
        final RequestParameter mimeTypePropertyParam = request.getRequestParameter("mimeTypeProperty");
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
        this.fullImport = true;
        if (fullImportParam != null && StringUtils.isNotBlank(fullImportParam.toString())) {
            this.fullImport = StringUtils.equalsIgnoreCase(fullImportParam.toString(), "true");
        }

        this.fileLocation = "/dev/null";
        if (fileLocationParam != null && StringUtils.isNotBlank(fileLocationParam.toString())) {
            this.fileLocation = fileLocationParam.toString();
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

        this.ignoreProperties = new String[]{ CsvAssetImporterServlet.TERMINATED };
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

    public String getUniqueProperty() {
        return this.uniqueProperty;
    }

    public String getRelSrcPathProperty() {
        return relSrcPathProperty;
    }

    public String getAbsTargetPathProperty() {
        return absTargetPathProperty;
    }

    public String getMimeTypeProperty() {
        return mimeTypeProperty;
    }

    public String getFileLocation() {
        return fileLocation;
    }

    public boolean isFullImport() {
        return fullImport;
    }

    public Character getSeparator() {
        return separator;
    }

    public Character getDelimiter() {
        return delimiter;
    }

    public String getCharset() {
        return charset;
    }

    public InputStream getFile() {
        return file;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public String getMultiDelimiter() { return multiDelimiter; }

    public String[] getIgnoreProperties() { return ignoreProperties; }

    public long getThrottle() {
        return throttle;
    }
}