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
%><%@page session="false"%>

<%-- Header --%>
<header class="top">

    <%-- Logo --%>
    <div class="logo">
        <span ng-hide="data.execution.running"><a href="/"><i class="icon-marketingcloud medium"></i></a></span>
        <span ng-show="data.execution.running"><span class="spinner"></span></span>
    </div>

    <%-- Limited Breadcrumbs --%>
    <nav class="crumbs">
        <a href="/miscadmin">Tools</a>
        <a href="<%= currentPagePath %>.html">AEM Fiddle</a>
    </nav>

    <div class="drawer">
        <!-- Script language -->
        <select ng-model="data.ui.scriptExtOption"
              ng-options="option.value as option.label for option in data.ui.scriptExtOptions"
              class="script-language-select">
        </select> 
        
        <!-- New Fiddle -->
        <a href="#new"
           ng-disabled="data.ui.myfiddles.createFiddle.visible"
           ng-click="app.new()"
           class="icon-add medium"
           title="New">New Fiddle</a>

        <span class="divider"></span>

        <%-- Resource Execution Context --%>
        <input ng-model="data.execution.params.resource"
               type="text"
               placeholder="Absolute path to resource"
               class="resource"/>

        <span class="divider"></span>

        <%-- Run Code Button --%>
        <button ng-click="app.run('<%= runURL %>')"
                class="primary run-code-button">
            <span ng-hide="data.execution.running">Run Code</span>
            <span ng-show="data.execution.running">Running Code...</span>
        </button>

        <span class="divider"></span>

        <%-- Rail Toggle Button --%>
        <a ng-click="ui.toggleRail()"
              class="toggle-rail-button medium icon-viewlist">Show/Hide MyFiddles</a>
    </div>

</header>