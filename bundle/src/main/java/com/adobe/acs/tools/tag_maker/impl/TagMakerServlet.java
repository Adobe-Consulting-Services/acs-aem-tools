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
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package com.adobe.acs.tools.tag_maker.impl;


import com.adobe.acs.tools.tag_maker.TagData;
import com.adobe.acs.tools.tag_maker.tagdataconverters.TagDataConverter;
import com.adobe.acs.tools.tag_maker.tagdataconverters.impl.DefaultConverterImpl;
import com.day.cq.tagging.InvalidTagFormatException;
import com.day.cq.tagging.Tag;
import com.day.cq.tagging.TagConstants;
import com.day.cq.tagging.TagManager;
import com.day.text.csv.Csv;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.StringUtils;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.References;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.request.RequestParameter;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.commons.json.JSONArray;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.ServletException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("PackageAccessibility")
@SlingServlet(
        label = "ACS AEM Tools - Tag Maker Servlet",
        methods = {"GET", "POST"},
        resourceTypes = {"acs-tools/components/tag-maker"},
        selectors = {"init", "make-tags"},
        extensions = {"json"}
)
@References({
        @Reference(
                name = "tagDataConverter",
                referenceInterface = TagDataConverter.class,
                policy = ReferencePolicy.DYNAMIC,
                cardinality = ReferenceCardinality.OPTIONAL_MULTIPLE)
})
public class TagMakerServlet extends SlingAllMethodsServlet {
    private static final Logger log = LoggerFactory.getLogger(TagMakerServlet.class);

    private static final String DEFAULT_CHARSET = "UTF-8";

    private static final String DEFAULT_CONVERTER = DefaultConverterImpl.LABEL;

    private static final String NONE_CONVERTER = "__NONE";

    private static final boolean DEFAULT_CLEAN = true;

    private Map<String, TagDataConverter> tagDataConverters = new ConcurrentHashMap<String, TagDataConverter>();

    @Override
    protected final void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        final JSONArray jsonArray = new JSONArray();
        try {

            for (Map.Entry<String, TagDataConverter> entry : this.tagDataConverters.entrySet()) {
                final JSONObject jsonObject = new JSONObject();

                jsonObject.put("label", entry.getValue().getLabel());
                jsonObject.put("value", entry.getKey());

                jsonArray.put(jsonObject);
            }

            response.getWriter().print(jsonArray.toString());

        } catch (JSONException e) {
            response.setStatus(SlingHttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    protected final void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        final JSONObject jsonResponse = new JSONObject();

        final TagManager tagManager = request.getResourceResolver().adaptTo(TagManager.class);

        final RequestParameter charsetParam = request.getRequestParameter("charset");
        final RequestParameter cleanParam = request.getRequestParameter("clean");
        final RequestParameter delimiterParam = request.getRequestParameter("delimiter");
        final RequestParameter fileParameter = request.getRequestParameter("file");
        final RequestParameter separatorParam = request.getRequestParameter("separator");
        final RequestParameter converterParam = request.getRequestParameter("converter");
        final RequestParameter fallbackConverterParam = request.getRequestParameter("fallbackConverter");


        boolean clean = DEFAULT_CLEAN;
        if (cleanParam != null) {
            clean = Boolean.valueOf(StringUtils.defaultIfEmpty(cleanParam.toString(),
                    String.valueOf(DEFAULT_CLEAN)));
        }

        String converter = DEFAULT_CONVERTER;
        if (converterParam != null) {
            converter = StringUtils.defaultIfEmpty(converterParam.toString(), DEFAULT_CONVERTER);
        }

        String fallbackConverter = NONE_CONVERTER;
        if (fallbackConverterParam != null) {
            fallbackConverter = StringUtils.defaultIfEmpty(fallbackConverterParam.toString(), NONE_CONVERTER);
        }

        String charset = DEFAULT_CHARSET;
        if (charsetParam != null) {
            charset = StringUtils.defaultIfEmpty(charsetParam.toString(), DEFAULT_CHARSET);
        }

        Character delimiter = null;
        if (delimiterParam != null && StringUtils.isNotBlank(delimiterParam.toString())) {
            delimiter = delimiterParam.toString().charAt(0);
        }

        Character separator = null;
        if (separatorParam != null && StringUtils.isNotBlank(separatorParam.toString())) {
            separator = separatorParam.toString().charAt(0);
        }


        final List<TagDataConverter> tagDataConverters = new ArrayList<TagDataConverter>();

        final TagDataConverter primaryTagConverter = this.getTagDataConverter(converter);
        if (primaryTagConverter != null) {
            tagDataConverters.add(primaryTagConverter);
        }

        final TagDataConverter fallbackTagConverter = this.getTagDataConverter(fallbackConverter);
        if (fallbackTagConverter != null) {
            tagDataConverters.add(fallbackTagConverter);
        }

        if (tagDataConverters.isEmpty()) {
            log.error("Could not find Tag Data Converter [ {} ]", converter);

            response.setStatus(SlingHttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        } else if (fileParameter != null && fileParameter.getInputStream() != null) {

            InputStream is = fileParameter.getInputStream();

            final Csv csv = new Csv();

            if (delimiter != null) {
                log.debug("Setting Field Delimiter to [ {} ]", delimiter);
                csv.setFieldDelimiter(delimiter);
            }

            if (separator != null) {
                log.debug("Setting Field Separator to [ {} ]", separator);
                csv.setFieldSeparatorRead(separator);
            }

            if (clean) {
                is = this.stripLineEnds(is, charset, csv.getFieldSeparatorRead());
            }

            final Iterator<String[]> rows = csv.read(is, charset);

            try {
                request.getResourceResolver().adaptTo(Session.class).getWorkspace().getObservationManager().setUserData("acs-aem-tools.tag-maker");

                final List<String> result = this.makeTags(tagManager, tagDataConverters, rows);

                try {
                    jsonResponse.put("tagIds", result);
                } catch (JSONException e) {
                    log.error("Could not serialized Tag Maker results into JSON", e);
                }

                this.addMessage(jsonResponse, result.size() + " tags were processed");
            } catch (InvalidTagFormatException e) {
                log.error("Could not create Tag due to illegal formatting", e);
                this.addMessage(jsonResponse, "Could not create tags due to illegal formatting");
                response.setStatus(SlingHttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            } catch (RepositoryException e) {
                log.error("Could not save Tags to JCR", e);
                this.addMessage(jsonResponse, "Could not save tags");
                response.setStatus(SlingHttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } else {
            log.error("Could not find CSV file in request.");
            this.addMessage(jsonResponse, "CSV file is missing");
            response.setStatus(SlingHttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

        response.getWriter().print(jsonResponse.toString());
    }

    private List<String> makeTags(final TagManager tagManager,
                                  final List<TagDataConverter> tagDataConverters,
                                  final Iterator<String[]> rows) throws InvalidTagFormatException, RepositoryException {

        final Set<String> result = new LinkedHashSet<String>();

        while (rows.hasNext()) {
            final String[] row = rows.next();

            log.debug("Processing data from row {}", Arrays.asList(row));

            String tagId = null;

            for (int i = 0; i < row.length; i++) {
                TagData tagData = null;

                final String element = StringUtils.trimToNull(row[i]);

                if (element == null) {
                    log.warn("Element is null skipping this row [ {} ]", tagId);
                    break;
                }

                for (final TagDataConverter tagDataConverter : tagDataConverters) {
                    if (tagDataConverter.accepts(element)) {
                        tagData = tagDataConverter.convert(element);
                        break;
                    }
                }

                if (tagData == null) {
                    log.warn("Could not find a Tag Data Converter that accepts CSV element [ {} ]; skipping...");
                    break;
                } else if (!tagData.isValid()) {
                    log.warn("Could not convert CSV element [ {} ] into valid Tag Data; skipping...");
                    break;
                }
                if (i == 0) {
                    // Tag Namespace
                    tagId = tagData.getName() + TagConstants.NAMESPACE_DELIMITER;
                } else if (i == 1) {
                    // First Tag under Namespace
                    tagId += tagData.getName();
                } else {
                    // Subsequent Tags
                    tagId += "/" + tagData.getName();
                }

                final Tag tag = tagManager.createTag(tagId, tagData.getTitle(), tagData.getDescription());
                
                if(tagData.getLocalizedTitles()!=null){
	                Map<String,String> translationsMap = tagData.getLocalizedTitles();
	                Node node = tag.adaptTo(Node.class);
	                for (Map.Entry<String, String> entry : translationsMap.entrySet()) {
						node.setProperty("jcr:title."+entry.getKey(), entry.getValue());
	                }
	                node.getSession().save();
                }
                log.trace("Created Tag [ {} ] with Title [ {} ]", tag.getTagID(), tagData.getTitle());

                result.add(tagId);
            }
        }

        if (tagManager.getSession().hasPendingChanges()) {
            final long start = System.currentTimeMillis();
            tagManager.getSession().save();
            if (log.isInfoEnabled()) {
                log.info("Persisting tags to JCR in {} ms", System.currentTimeMillis() - start);
            }
        }

        return new ArrayList<String>(result);
    }

    private InputStream stripLineEnds(InputStream is, String charset, char chartoStrip) throws IOException {
        log.debug("Stripping [ {} ] from the end of lines.", chartoStrip);

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final PrintStream printStream = new PrintStream(baos);

        final LineIterator lineIterator = IOUtils.lineIterator(is, charset);

        while (lineIterator.hasNext()) {
            String line = StringUtils.stripToNull(lineIterator.next());
            if (line != null) {
                line = StringUtils.stripEnd(line, String.valueOf(chartoStrip));
                printStream.println(line);
            }
        }

        return new ByteArrayInputStream(baos.toByteArray());
    }

    private TagDataConverter getTagDataConverter(final String name) {
        if (StringUtils.isNotBlank(name)) {
            return this.tagDataConverters.get(name);
        }

        return null;
    }

    private void addMessage(JSONObject jsonObject, String message) {
        try {
            jsonObject.put("message", message);
        } catch (JSONException e) {
            log.error("Could not formulate JSON Response", e);
        }
    }

    protected final void bindTagDataConverter(final TagDataConverter service,
                                              final Map<Object, Object> props) {
        final String type = PropertiesUtil.toString(props.get(TagDataConverter.PROP_NAME), null);
        if (type != null) {
            this.tagDataConverters.put(type, service);
        }
    }

    protected final void unbindTagDataConverter(final TagDataConverter service,
                                                final Map<Object, Object> props) {
        final String type = PropertiesUtil.toString(props.get(TagDataConverter.PROP_NAME), null);
        if (type != null) {
            this.tagDataConverters.remove(type);
        }
    }
}

