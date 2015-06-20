package com.adobe.acs.tools.csv_asset_importer.impl;

public class CsvAssetImportException extends Exception {
    public CsvAssetImportException() {
        super();
    }

    public CsvAssetImportException(String message) {
        super(message);
    }

    public CsvAssetImportException(Exception e) {
        super(e);
    }

    public CsvAssetImportException(String message, Exception e) {
        super(message, e);
    }


}
