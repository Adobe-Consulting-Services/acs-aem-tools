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

import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.AssetManager;
import com.day.cq.dam.api.DamConstants;
import com.day.cq.dam.commons.util.DamUtil;
import com.day.text.csv.Csv;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.jackrabbit.util.Text;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.apache.sling.commons.mime.MimeTypeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@SlingServlet(
        label = "ACS AEM Tools - Excel to Asset Servlet",
        methods = { "POST" },
        resourceTypes = { "acs-tools/components/csv-asset-importer" },
        selectors = { "import" },
        extensions = { "json" }
)
public class CsvAssetImporterServlet extends SlingAllMethodsServlet {
    private static final Logger log = LoggerFactory.getLogger(CsvAssetImporterServlet.class);

    public static final String TERMINATED = "_LINE_TERMINATED";

    @Reference
    private MimeTypeService mimeTypeService;

    @Override
    protected final void doPost(final SlingHttpServletRequest request, final SlingHttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        final JSONObject jsonResponse = new JSONObject();
        final Parameters params = new Parameters(request);

        if (params.getFile() != null) {

            final long start = System.currentTimeMillis();
            final Iterator<String[]> rows = this.getRowsFromCsv(params);

            try {
                // First row is property names

                // Get the required properties for this tool (source and dest)
                final String[] requiredProperties = new String[]{
                        params.getRelSrcPathProperty(),
                        params.getAbsTargetPathProperty()
                };

                // Get the columns from the first row of the CSV
                final Map<String, Column> columns = Column.getColumns(rows.next(),
                        params.getMultiDelimiter(),
                        params.getIgnoreProperties(),
                        requiredProperties);

                // Process Asset row entries
                final List<String> result = new ArrayList<String>();
                final List<String> batch = new ArrayList<String>();
                int failures = 0;

                while (rows.hasNext()) {
                    final String[] row = rows.next();

                    log.debug("Processing row {}", Arrays.asList(row));

                    try {
                        batch.add(this.importAsset(request.getResourceResolver(),
                                params,
                                columns,
                                row));
                    } catch (FileNotFoundException e) {
                        failures++;
                        log.error("Could not find file for row ", Arrays.asList(row), e);
                    } catch (CsvAssetImportException e) {
                        failures++;
                        log.error("Could not import the row due to ", e.getMessage(), e);
                    }

                    log.debug("Processed row {}", Arrays.asList(row));

                    if (batch.size() % params.getBatchSize() == 0) {
                        this.save(request.getResourceResolver(), params.getBatchSize());
                        result.addAll(batch);
                        batch.clear();

                        // Throttle saves
                        if (params.getThrottle() > 0) {
                            log.info("Throttling CSV Asset Importer batch processing for {} ms", params.getThrottle());
                            Thread.sleep(params.getThrottle());
                        }
                    }
                }

                // Final save to catch any non-modulo stragglers; will only invoke persist if there are changes
                this.save(request.getResourceResolver(), params.getBatchSize());
                result.addAll(batch);

                log.info("Imported as TOTAL of [ {} ] assets in {} ms", result.size(),
                        System.currentTimeMillis() - start);

                try {
                    jsonResponse.put("assets", result);
                    jsonResponse.put("failures", failures);
                } catch (JSONException e) {
                    log.error("Could not serialized Excel Importer results into JSON", e);
                    this.addMessage(jsonResponse, "Could not serialized Excel Importer results into JSON");
                    response.setStatus(SlingHttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                }
            } catch (RepositoryException e) {
                log.error("Could not save Assets to JCR", e);
                this.addMessage(jsonResponse, "Could not save assets. " + e.getMessage());
                response.setStatus(SlingHttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            } catch (Exception e) {
                log.error("Could not process CSV import", e);
                this.addMessage(jsonResponse, "Could not process CSV import. " + e.getMessage());
                response.setStatus(SlingHttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } else {
            log.error("Could not find CSV file in request.");
            this.addMessage(jsonResponse, "CSV file is missing");
            response.setStatus(SlingHttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

        response.getWriter().print(jsonResponse.toString());
    }

    /**
     * Interrogates the Request parameters and returns a prepared and parsed set of rows from the CSV file.
     *
     * @param params the Request parameters
     * @return The rows from the uploaded CSV file
     * @throws IOException
     */
    private Iterator<String[]> getRowsFromCsv(final Parameters params) throws IOException {
        final Csv csv = new Csv();
        InputStream is = params.getFile();

        if (params.getDelimiter() != null) {
            log.debug("Setting Field Delimiter to [ {} ]", params.getDelimiter());
            csv.setFieldDelimiter(params.getDelimiter());
        }

        if (params.getSeparator() != null) {
            log.debug("Setting Field Separator to [ {} ]", params.getSeparator());
            csv.setFieldSeparatorRead(params.getSeparator());
        }

        // Hack to prevent empty-value ending lines from breaking
        is = this.terminateLines(is,
                params.getSeparator() != null ? params.getSeparator() : csv.getFieldSeparatorRead(),
                params.getCharset());

        return csv.read(is, params.getCharset());
    }

    /**
     * Import the row of data into the DAM.
     *
     * @param resourceResolver the resource
     * @param params           the CSV Asset Importer params
     * @param columns          the Columns of the CSV
     * @param row              the row data
     * @return the path of the imported Asset
     * @throws FileNotFoundException
     * @throws RepositoryException
     * @throws PersistenceException
     * @throws CsvAssetImportException
     */
    private String importAsset(final ResourceResolver resourceResolver,
                               final Parameters params,
                               final Map<String, Column> columns,
                               final String[] row)
            throws FileNotFoundException, RepositoryException, PersistenceException, CsvAssetImportException {

        try {
            // Get, create or move the asset in the JCR
            final Asset asset = this.getOrCreateAsset(resourceResolver, params, columns, row);
            log.debug("Imported asset: {}", asset.getPath());
            // Add/Update/Delete the asset's properties in the JCR
            this.updateProperties(columns, row, params.getIgnoreProperties(), asset);
            log.debug("Updated properties on asset: {}", asset.getPath());

            // Return the asset path
            return asset.getPath();
        } catch (Exception e) {
            throw new CsvAssetImportException("Could not import row", e);
        }
    }

    /**
     * Updates the Metadata Properties of the Asset.
     *
     * @param columns          the Columns of the CSV
     * @param row              the row data
     * @param ignoreProperties Properties which to ignore when persisting property values to the Asset
     * @param asset            the Asset to persist the data to
     */
    private void updateProperties(final Map<String, Column> columns,
                                  final String[] row,
                                  final String[] ignoreProperties,
                                  final Asset asset) {
        final ModifiableValueMap properties = this.getMetadataProperties(asset);

        // Copy properties
        for (final Map.Entry<String, Column> entry : columns.entrySet()) {
            if (ArrayUtils.contains(ignoreProperties, entry.getKey())) {
                continue;
            }

            if (StringUtils.isBlank(entry.getKey())) {
                log.warn("Found a blank property name for: {}", Arrays.asList(entry.getValue()));
                continue;
            }

            final Column column = entry.getValue();
            final String valueStr = row[column.getIndex()];

            if (StringUtils.isNotBlank(valueStr)) {
                if (column.isMulti()) {
                    properties.put(entry.getKey(), column.getMultiData(valueStr));
                    log.debug("Setting multi property [ {} ~> {} ]",
                            entry.getKey(),
                            Arrays.asList(column.getMultiData(valueStr)));
                } else {
                    properties.put(entry.getKey(), column.getData(valueStr));
                    log.debug("Setting property [ {} ~> {} ]",
                            entry.getKey(),
                            column.getData(valueStr));
                }
            } else {
                if (properties.containsKey(entry.getKey())) {
                    properties.remove(entry.getKey());
                    log.debug("Removing property [ {} ]", entry.getKey());
                }
            }
        }
    }

    /**
     * Gets an existing Asset to update or creates a new Asset.
     *
     * @param resourceResolver the resource resolver
     * @param params           the CSV Asset Importer params
     * @param columns          the Columns of the CSV
     * @param row              a row in the CSV
     * @return the Asset
     * @throws FileNotFoundException
     * @throws RepositoryException
     * @throws CsvAssetImportException
     */
    private Asset getOrCreateAsset(final ResourceResolver resourceResolver,
                                   final Parameters params,
                                   final Map<String, Column> columns,
                                   final String[] row)
            throws FileNotFoundException, RepositoryException, CsvAssetImportException {

        final AssetManager assetManager = resourceResolver.adaptTo(AssetManager.class);

        String uniqueId = null;
        if (StringUtils.isNotBlank(params.getUniqueProperty())) {
            uniqueId = row[columns.get(params.getUniqueProperty()).getIndex()];
        }

        final String srcPath = params.getFileLocation()
                + "/"
                + row[columns.get(params.getRelSrcPathProperty()).getIndex()];
        final String mimeType = this.getMimeType(params, columns, row);
        final String absTargetPath = row[columns.get(params.getAbsTargetPathProperty()).getIndex()];

        if (StringUtils.endsWith(absTargetPath, "/")) {
            throw new CsvAssetImportException("Absolute path [ " + absTargetPath
                    + " ] is to a folder, not a file. Skipping");
        }

        Asset asset = null;

        if (StringUtils.isNotBlank(params.getUniqueProperty())
                && StringUtils.isNotBlank(uniqueId)) {
            // Check for existing Assets
            asset = this.findExistingAsset(resourceResolver,
                    row[columns.get(params.getAbsTargetPathProperty()).getIndex()],
                    params.getUniqueProperty(),
                    uniqueId);
        }

        final FileInputStream fileInputStream = new FileInputStream(srcPath);

        boolean createAsset = false;

        if (asset != null) {
            if (!params.isFullImport() && !StringUtils.equals(asset.getPath(), absTargetPath)) {
                // Moving the existing asset
                final Session session = resourceResolver.adaptTo(Session.class);

                if (!session.nodeExists(absTargetPath)) {
                    JcrUtils.getOrCreateByPath(StringUtils.substringBeforeLast(absTargetPath, "/"),
                            "sling:OrderedFolder", session);
                }

                session.move(asset.getPath(), absTargetPath);
                log.info("Moved asset from [ {} ~> {}]", asset.getPath(), absTargetPath);
                asset = DamUtil.resolveToAsset(resourceResolver.getResource(absTargetPath));

            } else if (params.isFullImport()) {
                // Remove existing asset so it can be recreated
                asset.adaptTo(Resource.class).adaptTo(Node.class).remove();
                createAsset = true;
                log.info("Removed existing asset so it can be re-created");
            }
        } else {
            log.info("Existing asset could not be found at [ {} ]", absTargetPath);
            createAsset = true;
        }

        if (createAsset) {
            // Create new Asset
            try {
                asset = assetManager.createAsset(absTargetPath, fileInputStream, mimeType, true);
            } catch (Exception e) {
                log.error("Could not create Asset at [ {} ]", absTargetPath);
                throw new CsvAssetImportException("Could not create Asset at [ " + absTargetPath + " ]", e);
            }
            log.info("Created new asset [ {} ]", asset.getPath());
        }

        return asset;
    }

    /**
     * Gets the mimeType of the asset based on the filename of how it will be stored in the AEM DAM.
     * The destination filename is used since source files can be very messy (and may not even have extensions
     * depending on export options)
     *
     * @param params  the CSV Asset Importer params
     * @param columns the Columns of the CSV
     * @param row     a row in the CSV
     * @return The mimeType or blank if one cannot be derived
     */
    private String getMimeType(final Parameters params, final Map<String, Column> columns, final String[] row) {
        String mimeType = "";
        final Column mimeTypeCol = columns.get(params.getMimeTypeProperty());

        if (mimeTypeCol != null) {
            mimeType = row[columns.get(params.getMimeTypeProperty()).getIndex()];
        }

        if (StringUtils.isNotBlank(mimeType)) {
            // Get the tar
            final Column destColumn = columns.get(params.getAbsTargetPathProperty());
            final String fileName = Text.getName(row[destColumn.getIndex()]);
            mimeType = mimeTypeService.getMimeType(fileName);
        }

        return mimeType;
    }

    /**
     * Search for an existing asset based on the uniqueId property and value.
     *
     * @param resourceResolver   the resource resolver
     * @param absTargetPath      the absolute target path of the asset
     * @param uniquePropertyName the rel property to search for on the dam:AssetContent node
     * @param uniqueId           the unique value
     * @return the Asset if one can be found, else null
     */
    private Asset findExistingAsset(final ResourceResolver resourceResolver,
                                    final String absTargetPath,
                                    final String uniquePropertyName,
                                    final String uniqueId) {

        // First try the very fast check against the absolute target path for the Asset.
        // If this a repeat processing there is a good change this is where the Asset lives.

        final String absMetadataPath = absTargetPath
                + "/"
                + JcrConstants.JCR_CONTENT
                + "/"
                + DamConstants.METADATA_FOLDER;

        final Resource resource = resourceResolver.getResource(absMetadataPath);

        if (resource != null) {
            final ValueMap properties = resource.adaptTo(ValueMap.class);
            final String val = properties.get(uniquePropertyName, String.class);
            if (StringUtils.equals(val, uniqueId)) {
                log.debug("Found  Asset at [ {} ] with matching unique property value of [ {} ]",
                        resource.getPath(), uniqueId);
                // Good news! Found the Asset at the absolute target path
                return DamUtil.resolveToAsset(resource);
            }
        }

        // Could not find the resource by guessing it exists in the absolute target path, so resort of a query.
        // In AEM6 this property should have a supporting index so it is very fast.

        final Iterator<Resource> resourceIterator = resourceResolver.findResources(
                "SELECT * FROM [dam:AssetContent] WHERE ["
                        + DamConstants.METADATA_FOLDER
                        + "/"
                        + uniquePropertyName
                        + "] = '"
                        + uniqueId
                        + "'",
                "JCR-SQL2");

        if (resourceIterator.hasNext()) {
            return DamUtil.resolveToAsset(resourceIterator.next());
        } else {
            return null;
        }
    }

    /**
     * Adds a populated terminating field to the ends of CSV entries.
     * If the last entry in a CSV row is empty, the CSV library has difficulty understanding that is the end of the row.
     *
     * @param is        the CSV file as an inputstream
     * @param separator The field separator
     * @param charset   The charset
     * @return An inputstream that is the same as is, but each line has a populated line termination entry
     * @throws IOException
     */
    private InputStream terminateLines(final InputStream is, final char separator, final String charset)
            throws IOException {

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final PrintStream printStream = new PrintStream(baos);

        final LineIterator lineIterator = IOUtils.lineIterator(is, charset);

        while (lineIterator.hasNext()) {
            String line = StringUtils.stripToNull(lineIterator.next());

            if (line != null) {
                line += separator + TERMINATED;
                printStream.println(line);
            }
        }

        return new ByteArrayInputStream(baos.toByteArray());
    }

    /**
     * Helper for saving changes to the JCR; contains timing logging.
     *
     * @param resourceResolver the resource resolver
     * @param size             the number of changes to save
     * @throws PersistenceException
     */
    private void save(final ResourceResolver resourceResolver, final int size) throws PersistenceException {
        if (resourceResolver.hasChanges()) {
            final long start = System.currentTimeMillis();
            resourceResolver.commit();
            log.info("Imported a BATCH of [ {} ] assets in {} ms", size, System.currentTimeMillis() - start);
        } else {
            log.debug("Nothing to save");
        }
    }

    /**
     * Gets the ModifiableValueMap for the Asset's metadata node.
     *
     * @param asset the asset to get the properties for
     * @return the ModifiableValueMap for the Asset's metadata node
     */
    private ModifiableValueMap getMetadataProperties(final Asset asset) {
        Resource assetResource = asset.adaptTo(Resource.class);
        Resource metadataResource = assetResource.getChild(JcrConstants.JCR_CONTENT
                + "/"
                + DamConstants.METADATA_FOLDER);
        return metadataResource.adaptTo(ModifiableValueMap.class);
    }

    /**
     * Helper method; adds a message to the JSON Response object.
     *
     * @param jsonObject the JSON object to add the message to
     * @param message    the message to add.
     */
    private void addMessage(final JSONObject jsonObject, final String message) {
        try {
            jsonObject.put("message", message);
        } catch (JSONException e) {
            log.error("Could not formulate JSON Response", e);
        }
    }
}