package com.adobe.acs.tools.clientlib_optimizer.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.widget.ClientLibrary;
import com.day.cq.widget.LibraryType;

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
		// filter out the requested categories
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
		
    	for (String category : requestedCategories) {
    		if (categories.contains(category)) {
    			log.debug("Category {} already in list, not adding twice!", category, library.getPath());
    			if (currentPosition < categories.indexOf(category)) {
    				// if duplicate is after the current position move it directly in front of current position
    				categories.remove(category);
    	    		categories.add(currentPosition, category);
    	    		log.debug("Moved category to because it is needed earlier!");
    			} else {
    				log.debug("No need to move category because it is loaded early enough!");
    				// move current position to make sure the dependent libraries are also loaded early enough
    				currentPosition = categories.indexOf(category);
    			}
    		} else {
    			categories.add(currentPosition, category);
    		}
    	} 
    	
    	// add embedded libraries (per type)
    	if (LibraryType.JS == type) {
    		log.debug("Processing embedded JS libraries of library with path {}", library.getPath());
    		addLibraries(categories, true, library.getEmbedded(LibraryType.JS), library.getEmbeddedCategories(), currentPosition);
    		// which category to take here?
    	} if (LibraryType.CSS == type) {
    		log.debug("Processing embedded CSS libraries of library with path {}", library.getPath());
    		addLibraries(categories, true, library.getEmbedded(LibraryType.CSS), library.getEmbeddedCategories(), currentPosition);
    	}
    	
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
		return library.getPath() + "[cat:" + StringUtils.join(library.getCategories()) + ", embed:"+ isEmbed +"]";
	}
}
