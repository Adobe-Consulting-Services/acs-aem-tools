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
package com.adobe.acs.tools.fiddle.impl;

import java.io.IOException;
import java.io.PrintWriter;

import org.apache.sling.api.SlingHttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.helpers.MarkerIgnoringBase;
import org.slf4j.helpers.MessageFormatter;

public final class ResponseLogger extends MarkerIgnoringBase implements Logger {

    private static final String TRACE = "[TRACE]";
    private static final String DEBUG = "[DEBUG]";
    private static final String ERROR = "[ERROR]";
    private static final String WARN = "[WARN]";
    private static final String INFO = "[INFO]";
    
    private final PrintWriter writer;

    public ResponseLogger(SlingHttpServletResponse response) throws IOException {
        this.writer = response.getWriter();
        this.name = "response";
    }

    

    @Override
    public void debug(String msg) {
        log(DEBUG, msg);
    }

    @Override
    public void debug(String format, Object arg) {
        log(DEBUG, format, arg);
    }

    @Override
    public void debug(String format, Object arg1, Object arg2) {
        log(DEBUG, format, arg1, arg2);
    }

    @Override
    public void debug(String format, Object[] argArray) {
        log(DEBUG, format, argArray);
    }

    @Override
    public void debug(String msg, Throwable t) {
        log(DEBUG, msg, t);
    }

    

    @Override
    public void error(String msg) {
        log(ERROR, msg);
    }

    @Override
    public void error(String format, Object arg) {
        log(ERROR, format, arg);
    }

    @Override
    public void error(String format, Object arg1, Object arg2) {
        log(ERROR, format, arg1, arg2);
    }

    @Override
    public void error(String format, Object[] argArray) {
        log(ERROR, format, argArray);
    }

    @Override
    public void error(String msg, Throwable t) {
        log(ERROR, msg, t);
    }
    @Override
    public void info(String msg) {
        log(INFO, msg);
    }

    @Override
    public void info(String format, Object arg) {
        log(INFO, format, arg);
    }

    @Override
    public void info(String format, Object arg1, Object arg2) {
        log(INFO, format, arg1, arg2);
    }

    @Override
    public void info(String format, Object[] argArray) {
        log(INFO, format, argArray);
    }

    @Override
    public void info(String msg, Throwable t) {
        log(INFO, msg, t);
    }

    @Override
    public boolean isDebugEnabled() {
        return true;
    }

    @Override
    public boolean isErrorEnabled() {
        return true;
    }

    @Override
    public boolean isInfoEnabled() {
        return true;
    }

    @Override
    public boolean isTraceEnabled() {
        return true;
    }

    @Override
    public boolean isWarnEnabled() {
        return true;
    }

    @Override
    public void trace(String msg) {
        log(TRACE, msg);
    }

    @Override
    public void trace(String format, Object arg) {
        log(TRACE, format, arg);
    }

    @Override
    public void trace(String format, Object arg1, Object arg2) {
        log(TRACE, format, arg1, arg2);
    }

    @Override
    public void trace(String format, Object[] argArray) {
        log(TRACE, format, argArray);
    }

    @Override
    public void trace(String msg, Throwable t) {
        log(TRACE, msg, t);
    }

    @Override
    public void warn(String msg) {
        log(WARN, msg);
    }

    @Override
    public void warn(String format, Object arg) {
        log(WARN, format, arg);

    }

    @Override
    public void warn(String format, Object arg1, Object arg2) {
        log(WARN, format, arg1, arg2);
    }

    @Override
    public void warn(String format, Object[] argArray) {
        log(WARN, format, argArray);
    }

    @Override
    public void warn(String msg, Throwable t) {
        log(WARN, msg, t);
    }

    private void log(String level, String msg) {
        writer.print(level);
        writer.print(" ");
        writer.print(msg);
        writer.println("<br/>");
    }

    private void log(String level, String format, Object arg) {
        log(level, MessageFormatter.format(format, arg).getMessage());
    }

    private void log(String level, String format, Object arg1, Object arg2) {
        log(level, MessageFormatter.format(format, arg1, arg2).getMessage());
    }

    private void log(String level, String format, Object[] argArray) {
        log(level, MessageFormatter.arrayFormat(format, argArray).getMessage());
    }

}
