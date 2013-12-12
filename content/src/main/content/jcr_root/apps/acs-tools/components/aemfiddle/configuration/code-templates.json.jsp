<%--
  #%L
  ACS AEM Tools Package
  %%
  Copyright (C) 2013 Adobe
  %%
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
  
       http://www.apache.org/licenses/LICENSE-2.0
  
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
  #L%
  --%>
<%@include file="/libs/foundation/global.jsp" %><%
%><%@page session="false" contentType="application/json; charset=utf-8" pageEncoding="UTF-8"
  import="com.adobe.acs.tools.fiddle.FiddleHelper,
      java.util.List,
  	  java.util.Set,
  	  java.util.HashSet,
      javax.script.ScriptEngineManager,
      javax.script.ScriptEngineFactory,
      org.apache.sling.commons.json.JSONArray,
      org.apache.sling.commons.json.JSONObject"
%><%
final String DEFAULT_TEMPLATE_NAME = "basic";
final JSONArray jsonArray = new JSONArray();
final Set<String> extensions = new HashSet<String>();

final FiddleHelper fiddleHelper = sling.getService(FiddleHelper.class);
final Resource templatesRoot = resourceResolver.getResource(component.getPath() + "/code-templates");

/* Get the extensions registered with the server */
final ScriptEngineManager scriptEngineManager = sling.getService(ScriptEngineManager.class);
for(final ScriptEngineFactory scriptEngineFactory : scriptEngineManager.getEngineFactories()) {
	extensions.addAll(scriptEngineFactory.getExtensions());
}

/* Only return templates for languages registered with the system */
for(final String extension : extensions) {
	boolean hasDefault = false;
	final Resource templates = templatesRoot.getChild(extension);

	if(templates == null) { continue; }

	for(final Resource template : templates.getChildren()) {
		final JSONObject obj = new JSONObject();	
		
		obj.put("title", template.getName());
		obj.put("scriptData", fiddleHelper.getCodeTemplate(template));
		obj.put("scriptExt", extension);

		// Check if this language has a defined default template
		if(template.getName().startsWith(DEFAULT_TEMPLATE_NAME + ".")) { 
			hasDefault = true; 
			obj.put("default", true);
		} else {
			obj.put("default", false);
		}

		jsonArray.put(obj);
	}

	// If no default template has been defined, create one as blank
	if(!hasDefault) {
		final JSONObject obj = new JSONObject();	

		obj.put("title", DEFAULT_TEMPLATE_NAME);
		obj.put("scriptData", "");
		obj.put("scriptExt", extension);
		obj.put("default", true);

		jsonArray.put(obj);
	}
}
%><%
%><%= jsonArray.toString() %>