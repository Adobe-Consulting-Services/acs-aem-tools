/*
 * #%L
 * ACS AEM Tools Bundle
 * %%
 * Copyright (C) 2013 Adobe
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
package com.adobe.acs.livereload.impl;

public class LiveReloadConstants {

    private LiveReloadConstants() {
    }

    static final String PROTOCOL_VERSION_7 = "http://livereload.com/protocols/official-7";

    static final String COMMAND = "command";
    static final String PATH = "path";
    static final String URL = "url";
    static final String PROTOCOLS = "protocols";

    static final String CMD_RELOAD = "reload";
    static final String CMD_HELLO = "hello";
    static final String CMD_INFO = "info";

}
