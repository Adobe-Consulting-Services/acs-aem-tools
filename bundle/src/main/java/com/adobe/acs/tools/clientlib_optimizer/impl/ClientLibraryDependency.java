/*
 * #%L
 * ACS AEM Tools Bundle
 * %%
 * Copyright (C) 2016 Adobe
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
package com.adobe.acs.tools.clientlib_optimizer.impl;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import com.adobe.granite.ui.clientlibs.ClientLibrary;
import com.adobe.granite.ui.clientlibs.LibraryType;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientLibraryDependency {

    private final ClientLibrary library;
    private final ClientLibraryDependency parent;
    private final LibraryType type;
    private final boolean isEmbed;
    private final SortedSet<String> requestedCategories;

    private static final Logger log = LoggerFactory.getLogger(ClientLibraryDependency.class);

    public ClientLibraryDependency(ClientLibraryDependency parent, ClientLibrary library, Set<String> requestedCategories, boolean isEmbed, LibraryType type) {
        this.library = library;
        this.parent = parent;
        if (isDependencyLoop(parent, library.getPath())) {
            throw new DependencyLoopException("Dependency loop detected: " + getStackTrace(parent, library.getPath()));
        }
        this.type = type;
        this.isEmbed = isEmbed;

        this.requestedCategories = new TreeSet<String>(java.util.Collections.reverseOrder());
        // only the requested categories are relevant (from all the categories of this library)
        for (String category : library.getCategories()) {
            if (requestedCategories.contains(category)) {
                this.requestedCategories.add(category);
            }
        }
    }


    /**
     * Build the dependency tree of this client library and return the necessary categories in the correct order!
     * @param categories
     * @return
     */
    public List<String> buildDependencyTree(List<String> categories, int currentPosition) {
        log.debug("Giving out dependencies for {}", getStackTrace(parent, library.getPath()));
        // only consider entry if it is of the required type!
        if (library.getTypes().contains(type)) {
            for (String category : requestedCategories) {
                if (categories.contains(category)) {
                    log.debug("Category {} already in list, not adding twice!", category, library.getPath());
                    if (currentPosition < categories.indexOf(category)) {
                        // if duplicate is after the current position move it directly in front of current position
                        categories.remove(category);
                        categories.add(currentPosition, category);
                        log.debug("Move category {} to the current position because it is needed earlier!", category);
                    } else {
                        log.debug("No need to move category because it is loaded early enough!");
                        // move current position to make sure the dependent libraries are also loaded early enough
                        currentPosition = categories.indexOf(category);
                    }
                } else {
                    categories.add(currentPosition, category);
                }
            }
        } else {
            log.debug("Not considering categories of this client library because they have the wrong type {}, request was type {}", library.getTypes(), type);
        }


        // add embedded libraries (per type)
        // always embed all types to correctly include transitive categories of the right type (even if intermediate embed has the wrong type)
        log.debug("Processing embedded JS libraries of library with path {}", library.getPath());
        addLibraries(categories, true, library.getEmbedded(LibraryType.JS), library.getEmbeddedCategories(), currentPosition);
        log.debug("Processing embedded CSS libraries of library with path {}", library.getPath());
        addLibraries(categories, true, library.getEmbedded(LibraryType.CSS), library.getEmbeddedCategories(), currentPosition);


        log.debug("Processing dependent libraries of library with path {}", library.getPath());
        // add dependent libraries
        // current position might move
        addLibraries(categories, false, library.getDependencies(false), library.getDependentCategories(), currentPosition);
        return categories;
    }

    private List<String> addLibraries(List<String> categories, boolean isEmbed, Map<String, ? extends ClientLibrary> librariesMap, String[] requestedCategories, int currentPosition) {
        // the order is given by the paths
        // we just sort alphabetically in here
        TreeMap<String, ClientLibrary> sortedLibrariesMap = new TreeMap<String, ClientLibrary>(librariesMap);

        // add in reverse order (because each might add a number of dependent libraries)
        for (Map.Entry<String, ClientLibrary> entry: sortedLibrariesMap.descendingMap().entrySet()) {
            ClientLibraryDependency dependency = new ClientLibraryDependency(this, entry.getValue(), new HashSet<String>(Arrays.asList(requestedCategories)), isEmbed, type);
            categories = dependency.buildDependencyTree(categories, currentPosition);
        }
        return categories;
    }

    public static boolean isDependencyLoop(ClientLibraryDependency parent, String path) {
        while (parent != null) {
            if (path.equals(parent.library.getPath())) {
                return true;
            }
            parent = parent.parent;
        }
        return false;
    }

    public static String getStackTrace(ClientLibraryDependency parent, String currentPath) {
        // give out all paths from the root
        StringBuffer tmp = new StringBuffer();
        printStack(tmp, parent);
        tmp.append(currentPath);
        return tmp.toString();
    }

    private static void printStack(StringBuffer buffer, ClientLibraryDependency parent) {
        if (parent != null) {
            printStack(buffer, parent.parent);
            buffer.append(parent.toString() + "->");
        }
    }

    public String toString() {
        return library.getPath() + "[cat:" + StringUtils.join(library.getCategories()) + ", embed:"+ isEmbed +", type: "+ library.getTypes() +"]";
    }
}
