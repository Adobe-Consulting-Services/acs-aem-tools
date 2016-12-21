package com.adobe.acs.tools.clientlib_optimizer.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.adobe.granite.ui.clientlibs.ClientLibrary;
import com.adobe.granite.ui.clientlibs.LibraryType;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.junit.Assert;

@RunWith(MockitoJUnitRunner.class)
public class ClientLibOptimizerServletTest {
	
	@Test
	public void testSimpleHierarchy() {
		ClientLibrary clientLibraryA = new MockClientLibraryBuilder(MockClientLibraryBuilder.MockClientLibraryType.JS, "categoryA").getClientLibrary();
		ClientLibrary clientLibraryB = new MockClientLibraryBuilder(MockClientLibraryBuilder.MockClientLibraryType.JS, "categoryB").setDependentLibraries(clientLibraryA).getClientLibrary();
		ClientLibrary clientLibraryC = new MockClientLibraryBuilder(MockClientLibraryBuilder.MockClientLibraryType.JS, "categoryC").setEmbeddedJsLibraries(clientLibraryB).getClientLibrary();

		List<String> allCategories = new ArrayList<String>();
		ClientLibOptimizerServlet.getSortedDependentCategories(Collections.singleton(clientLibraryC), Collections.singleton("categoryC"), LibraryType.JS, allCategories);
	
		Assert.assertThat(allCategories, Matchers.contains("categoryA", "categoryB", "categoryC"));
	}
	
	@Test(expected=DependencyLoopException.class)
	public void testLoopHierarchy() {
		ClientLibrary clientLibraryA = new MockClientLibraryBuilder(MockClientLibraryBuilder.MockClientLibraryType.JS, "categoryA").getClientLibrary();
		ClientLibrary clientLibraryB = new MockClientLibraryBuilder(MockClientLibraryBuilder.MockClientLibraryType.JS, "categoryB").setDependentLibraries(clientLibraryA).getClientLibrary();
		ClientLibrary clientLibraryC = new MockClientLibraryBuilder(MockClientLibraryBuilder.MockClientLibraryType.JS, "categoryC").setEmbeddedJsLibraries(clientLibraryB).getClientLibrary();
		
		// put in a dependency loop
		Mockito.doReturn(getPathMap(clientLibraryC)).when(clientLibraryA).getEmbedded(LibraryType.JS);
	        
		List<String> allCategories = new ArrayList<String>();
		ClientLibOptimizerServlet.getSortedDependentCategories(Collections.singleton(clientLibraryC), Collections.singleton("categoryC"), LibraryType.JS, allCategories);
	}
	
	@Test
	public void testComplexHierarchy() {
		ClientLibrary clientLibraryA = new MockClientLibraryBuilder(MockClientLibraryBuilder.MockClientLibraryType.JSANDCSS, "categoryA").getClientLibrary();
		ClientLibrary clientLibraryB = new MockClientLibraryBuilder(MockClientLibraryBuilder.MockClientLibraryType.JSANDCSS, "categoryB").setDependentLibraries(clientLibraryA).getClientLibrary();
		ClientLibrary clientLibraryC = new MockClientLibraryBuilder(MockClientLibraryBuilder.MockClientLibraryType.JSANDCSS, "categoryC").setDependentLibraries(clientLibraryB).getClientLibrary();
		ClientLibrary clientLibraryD = new MockClientLibraryBuilder(MockClientLibraryBuilder.MockClientLibraryType.JSANDCSS, "categoryD").setDependentLibraries(clientLibraryA).getClientLibrary();
		ClientLibrary clientLibraryE = new MockClientLibraryBuilder(MockClientLibraryBuilder.MockClientLibraryType.JSANDCSS, "categoryE").setDependentLibraries(clientLibraryD).setEmbeddedJsLibraries(clientLibraryC).getClientLibrary();
		
		List<String> allCategories = new ArrayList<String>();
		ClientLibOptimizerServlet.getSortedDependentCategories(Collections.singleton(clientLibraryE), Collections.singleton("categoryE"), LibraryType.JS, allCategories);
		
		Assert.assertThat(allCategories, Matchers.contains("categoryA","categoryD","categoryB", "categoryC", "categoryE"));
	}
	
	
	@Test
	public void testComplexCssAndJsHierarchy() {
		ClientLibrary clientLibraryA =  new MockClientLibraryBuilder(MockClientLibraryBuilder.MockClientLibraryType.CSS, "categoryA").getClientLibrary();
		ClientLibrary clientLibraryB = new MockClientLibraryBuilder(MockClientLibraryBuilder.MockClientLibraryType.JSANDCSS, "categoryB").setDependentLibraries(clientLibraryA).getClientLibrary();
		ClientLibrary clientLibraryC = new MockClientLibraryBuilder(MockClientLibraryBuilder.MockClientLibraryType.JS, "categoryC").setDependentLibraries(clientLibraryB).getClientLibrary();
		ClientLibrary clientLibraryD =  new MockClientLibraryBuilder(MockClientLibraryBuilder.MockClientLibraryType.JSANDCSS, "categoryD").setEmbeddedCssLibraries(clientLibraryA).getClientLibrary();
		ClientLibrary clientLibraryE = new MockClientLibraryBuilder(MockClientLibraryBuilder.MockClientLibraryType.JSANDCSS, "categoryE").setDependentLibraries(clientLibraryD).setEmbeddedJsLibraries(clientLibraryC).getClientLibrary();
		
		List<String> allCategories = new ArrayList<String>();
		ClientLibOptimizerServlet.getSortedDependentCategories(Collections.singleton(clientLibraryE), Collections.singleton("categoryE"), LibraryType.JS, allCategories);
		Assert.assertThat(allCategories, Matchers.contains("categoryD", "categoryB", "categoryC", "categoryE"));
		
		allCategories.clear();
		ClientLibOptimizerServlet.getSortedDependentCategories(Collections.singleton(clientLibraryE), Collections.singleton("categoryE"), LibraryType.CSS, allCategories);
		Assert.assertThat(allCategories, Matchers.contains("categoryA","categoryD","categoryB","categoryE"));
	}
	
	@Test
	public void testCQJquery() {
		ClientLibrary jQuery = new MockClientLibraryBuilder(MockClientLibraryBuilder.MockClientLibraryType.JS, "jquery").getClientLibrary();
		ClientLibrary graniteUtils = new MockClientLibraryBuilder(MockClientLibraryBuilder.MockClientLibraryType.JS, "granite.utils").setDependentLibraries(jQuery).getClientLibrary();
		ClientLibrary graniteJQuery = new MockClientLibraryBuilder(MockClientLibraryBuilder.MockClientLibraryType.JS, "granite.jquery").setDependentLibraries(jQuery, graniteUtils).getClientLibrary();
		ClientLibrary cqQuery = new MockClientLibraryBuilder(MockClientLibraryBuilder.MockClientLibraryType.JS, "cq.jquery").setDependentLibraries(graniteJQuery).getClientLibrary();
		
		List<String> allCategories = new ArrayList<String>();
		ClientLibOptimizerServlet.getSortedDependentCategories(Collections.singleton(cqQuery), Collections.singleton("cq.jquery"), LibraryType.JS, allCategories);
		
		Assert.assertThat(allCategories, Matchers.contains("jquery", "granite.utils","granite.jquery", "cq.jquery"));
	}


	private static class MockClientLibraryBuilder {
		final ClientLibrary library;
		private Set<String> jsEmbedCategories = new HashSet<String>();
		private Set<String> cssEmbedCategories = new HashSet<String>();
		
		private static enum MockClientLibraryType {
			JS,
			CSS,
			JSANDCSS,
			NONE
		};
		
		public MockClientLibraryBuilder(MockClientLibraryType type, String... categories) {
			 library = Mockito.mock(ClientLibrary.class);
			 Mockito.when(library.getCategories()).thenReturn(categories);
			 Set<LibraryType> types = new HashSet<LibraryType>();
			 switch (type) {
			 	case JS:
			 		types.add(LibraryType.JS);
			 		break;
			 	case CSS:
			 		types.add(LibraryType.CSS);
			 		break;
			 	case JSANDCSS:
			 		types.add(LibraryType.JS);
			 		types.add(LibraryType.CSS);
			 		break;
			 	case NONE:
			 		// no type at all
			 		break;
			 }
			 Mockito.when(library.getTypes()).thenReturn(types);
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
