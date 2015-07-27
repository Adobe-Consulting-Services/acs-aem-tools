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

import com.day.text.csv.Csv;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Iterator;


public class CsvUtil {
    private static final Logger log = LoggerFactory.getLogger(CsvUtil.class);

    public static final String TERMINATED = "_LINE_TERMINATED";


    /**
     * Interrogates the Request parameters and returns a prepared and parsed set of rows from the CSV file.
     *
     * @param params the Request parameters
     * @return The rows from the uploaded CSV file
     * @throws IOException
     */
    public static Iterator<String[]> getRowsFromCsv(final Parameters params) throws IOException {
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
        is = terminateLines(is,
                params.getSeparator() != null ? params.getSeparator() : csv.getFieldSeparatorRead(),
                params.getCharset());

        return csv.read(is, params.getCharset());
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
    public static InputStream terminateLines(final InputStream is, final char separator, final String charset)
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
}
