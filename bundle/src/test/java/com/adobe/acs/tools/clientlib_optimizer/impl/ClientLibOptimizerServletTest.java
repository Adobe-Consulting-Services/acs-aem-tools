package com.adobe.acs.tools.clientlib_optimizer.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.junit.Assert;

import com.day.cq.widget.ClientLibrary;
import com.day.cq.widget.LibraryType;

@RunWith(MockitoJUnitRunner.class)
public class ClientLibOptimizerServletTest {
	
	@Test
	public void testSimpleHierarchy() {
		ClientLibrary clientLibraryA = new MockClientLibraryBuilder("categoryA").getClientLibrary();
		ClientLibrary clientLibraryB = new MockClientLibraryBuilder("categoryB").setDependentLibraries(clientLibraryA).getClientLibrary();
		ClientLibrary clientLibraryC = new MockClientLibraryBuilder("categoryC").setEmbeddedJsLibraries(clientLibraryB).getClientLibrary();

		List<String> allCategories = new ArrayList<String>();
		ClientLibOptimizerServlet.getSortedDependentCategories(Collections.singleton(clientLibraryC), Collections.singleton("categoryC"), LibraryType.JS, allCategories);
	
		Assert.assertThat(allCategories, Matchers.contains("categoryA", "categoryB", "categoryC"));
	}
	
	@Test(expected=DependencyLoopException.class)
	public void testLoopHierarchy() {
		ClientLibrary clientLibraryA = new MockClientLibraryBuilder("categoryA").getClientLibrary();
		ClientLibrary clientLibraryB = new MockClientLibraryBuilder("categoryB").setDependentLibraries(clientLibraryA).getClientLibrary();
		ClientLibrary clientLibraryC = new MockClientLibraryBuilder("categoryC").setEmbeddedJsLibraries(clientLibraryB).getClientLibrary();
		
		// put in a dependency loop
		Mockito.doReturn(getPathMap(clientLibraryC)).when(clientLibraryA).getEmbedded(LibraryType.JS);
	        
		List<String> allCategories = new ArrayList<String>();
		ClientLibOptimizerServlet.getSortedDependentCategories(Collections.singleton(clientLibraryC), Collections.singleton("categoryC"), LibraryType.JS, allCategories);
	}
	
	@Test
	public void testComplexHierarchy() {
		ClientLibrary clientLibraryA =  new MockClientLibraryBuilder("categoryA").getClientLibrary();
		ClientLibrary clientLibraryB = new MockClientLibraryBuilder("categoryB").setDependentLibraries(clientLibraryA).getClientLibrary();
		ClientLibrary clientLibraryC = new MockClientLibraryBuilder("categoryC").setDependentLibraries(clientLibraryB).getClientLibrary();
		ClientLibrary clientLibraryD =  new MockClientLibraryBuilder("categoryD").setDependentLibraries(clientLibraryA).getClientLibrary();
		ClientLibrary clientLibraryE = new MockClientLibraryBuilder("categoryE").setDependentLibraries(clientLibraryD).setEmbeddedJsLibraries(clientLibraryC).getClientLibrary();
		
		List<String> allCategories = new ArrayList<String>();
		ClientLibOptimizerServlet.getSortedDependentCategories(Collections.singleton(clientLibraryE), Collections.singleton("categoryE"), LibraryType.JS, allCategories);
		
		Assert.assertThat(allCategories, Matchers.contains("categoryA","categoryD","categoryB", "categoryC", "categoryE"));
	}
	
	@Test
	public void testComplexCssAndJsHierarchy() {
		ClientLibrary clientLibraryA =  new MockClientLibraryBuilder("categoryA").getClientLibrary();
		ClientLibrary clientLibraryB = new MockClientLibraryBuilder("categoryB").setDependentLibraries(clientLibraryA).getClientLibrary();
		ClientLibrary clientLibraryC = new MockClientLibraryBuilder("categoryC").setDependentLibraries(clientLibraryB).getClientLibrary();
		ClientLibrary clientLibraryD =  new MockClientLibraryBuilder("categoryD").setEmbeddedCssLibraries(clientLibraryA).getClientLibrary();
		ClientLibrary clientLibraryE = new MockClientLibraryBuilder("categoryE").setDependentLibraries(clientLibraryD).setEmbeddedJsLibraries(clientLibraryC).getClientLibrary();
		
		List<String> allCategories = new ArrayList<String>();
		ClientLibOptimizerServlet.getSortedDependentCategories(Collections.singleton(clientLibraryE), Collections.singleton("categoryE"), LibraryType.JS, allCategories);
		Assert.assertThat(allCategories, Matchers.contains("categoryD", "categoryA","categoryB", "categoryC", "categoryE"));
		
		ClientLibOptimizerServlet.getSortedDependentCategories(Collections.singleton(clientLibraryE), Collections.singleton("categoryE"), LibraryType.CSS, allCategories);
		
		Assert.assertThat(allCategories, Matchers.contains("categoryA","categoryD","categoryB", "categoryC", "categoryE"));
	}
	
	@Test
	public void testCQJquery() {
		ClientLibrary jQuery = new MockClientLibraryBuilder("jquery").getClientLibrary();
		ClientLibrary graniteUtils = new MockClientLibraryBuilder("granite.utils").setDependentLibraries(jQuery).getClientLibrary();
		ClientLibrary graniteJQuery = new MockClientLibraryBuilder("granite.jquery").setDependentLibraries(jQuery, graniteUtils).getClientLibrary();
		ClientLibrary cqQuery = new MockClientLibraryBuilder("cq.jquery").setDependentLibraries(graniteJQuery).getClientLibrary();
		
		List<String> allCategories = new ArrayList<String>();
		ClientLibOptimizerServlet.getSortedDependentCategories(Collections.singleton(cqQuery), Collections.singleton("cq.jquery"), LibraryType.JS, allCategories);
		
		Assert.assertThat(allCategories, Matchers.contains("jquery", "granite.utils","granite.jquery", "cq.jquery"));
	}
	
	@Test
	public void testCqWidgets() {
		ClientLibrary jQuery = new MockClientLibraryBuilder("jquery").getClientLibrary();
		ClientLibrary graniteUtils = new MockClientLibraryBuilder("granite.utils").setDependentLibraries(jQuery).getClientLibrary();
		ClientLibrary graniteJQuery = new MockClientLibraryBuilder("granite.jquery").setDependentLibraries(jQuery, graniteUtils).getClientLibrary();
		ClientLibrary cqQuery = new MockClientLibraryBuilder("cq.jquery").setDependentLibraries(graniteJQuery).getClientLibrary();
		
		
	}


	private static class MockClientLibraryBuilder {
		final ClientLibrary library;
		private Set<String> jsEmbedCategories = new HashSet<String>();
		private Set<String> cssEmbedCategories = new HashSet<String>();
		
		public MockClientLibraryBuilder(String... categories) {
			 library = Mockito.mock(ClientLibrary.class);
			 Mockito.when(library.getCategories()).thenReturn(categories);
			 // take first category as path name by default (in lower case)
			 setPath(categories[0].toLowerCase());
			 Mockito.doReturn(Collections.<String, ClientLibrary> emptyMap()).when(library).getDependencies(false);
			 Mockito.doReturn(Collections.<String, ClientLibrary> emptyMap()).when(library).getEmbedded(LibraryType.CSS);
			 Mockito.doReturn(Collections.<String, ClientLibrary> emptyMap()).when(library).getEmbedded(LibraryType.JS);
			 Mockito.when(library.getDependentCategories()).thenReturn(new String[]{});
			 setEmbeddedCategories();
		}
		
		public MockClientLibraryBuilder setPath(String path) {
			Mockito.when(library.getPath()).thenReturn(path);
			return this;
		}
		
		public MockClientLibraryBuilder setEmbeddedJsLibraries(ClientLibrary... libraries) {
			return setEmbeddedJsLibraries(getLibrariesCategories(libraries), libraries);
		}
		
		public MockClientLibraryBuilder setEmbeddedJsLibraries(String[] categories, ClientLibrary... libraries) {
			this.jsEmbedCategories.clear();
			this.jsEmbedCategories.addAll(Arrays.asList(categories));
			setEmbeddedCategories();
			Mockito.doReturn(getPathMap(libraries)).when(library).getEmbedded(LibraryType.JS);
			return this;
		}
		
		public MockClientLibraryBuilder setEmbeddedCssLibraries(ClientLibrary... libraries) {
			return setEmbeddedCssLibraries(getLibrariesCategories(libraries), libraries);
		}
		
		public MockClientLibraryBuilder setEmbeddedCssLibraries(String[] categories, ClientLibrary... libraries) {
			this.cssEmbedCategories.clear();
			this.cssEmbedCategories.addAll(Arrays.asList(categories));
			setEmbeddedCategories();
			Mockito.doReturn(getPathMap(libraries)).when(library).getEmbedded(LibraryType.CSS);
			return this;
		}
		
		public MockClientLibraryBuilder setDependentLibraries(ClientLibrary... libraries) {
			return setDependentLibraries(getLibrariesCategories(libraries), libraries);
		}
		
		public MockClientLibraryBuilder setDependentLibraries(String categories[], ClientLibrary... libraries) {
			Mockito.doReturn(getPathMap(libraries)).when(library).getDependencies(false);
			Mockito.when(library.getDependentCategories()).thenReturn(categories);
			return this;
		}
		
		public ClientLibrary getClientLibrary() {
			return library;
		}
		
		private void setEmbeddedCategories() {
			Set<String> allCategories = new HashSet<String>();
			allCategories.addAll(jsEmbedCategories);
			allCategories.addAll(cssEmbedCategories);
			Mockito.when(library.getEmbeddedCategories()).thenReturn(allCategories.toArray(new String[0]));
		}
		
		private String[] getLibrariesCategories(ClientLibrary... libraries) {
			Set<String> categories = new HashSet<String>();
			for (ClientLibrary library: libraries) {
				categories.addAll(Arrays.asList(library.getCategories()));
			}
			return categories.toArray(new String[categories.size()]);
		}
	}
	
	private static Map<String, ? extends ClientLibrary> getPathMap(ClientLibrary... libraries) {
		Map<String, ClientLibrary> map = new HashMap<String, ClientLibrary>(libraries.length);
		for (ClientLibrary library : libraries) {
			map.put(library.getPath(), library);
		}
		return map;
	}
	
}
