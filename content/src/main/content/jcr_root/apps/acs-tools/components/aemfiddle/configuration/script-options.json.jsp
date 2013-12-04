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
  import="java.util.Map,
      java.util.HashMap,
      javax.script.ScriptEngineManager,
      javax.script.ScriptEngineFactory,
      org.apache.sling.commons.json.JSONArray"
%><%
  final ScriptEngineManager scriptEngineManager = sling.getService(ScriptEngineManager.class);
  final JSONArray jsonArray = new JSONArray();  

  for(final ScriptEngineFactory scriptEngineFactory : scriptEngineManager.getEngineFactories()) {
        if (scriptEngineFactory.getExtensions().isEmpty()) {
          continue;
        } 

    final Map<String, String> map = new HashMap();
        map.put("label", scriptEngineFactory.getLanguageName());
        map.put("value", scriptEngineFactory.getExtensions().get(0));

        jsonArray.put(map);
  } 
%>
<%= jsonArray.toString() %>