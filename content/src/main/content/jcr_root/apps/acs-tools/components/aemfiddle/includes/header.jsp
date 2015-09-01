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
        <span ng-hide="data.app.running"><a href="/"><i class="icon-marketingcloud medium"></i></a></span>
        <span ng-show="data.app.running"><a href="#"></a><i class="icon-spinner spinner medium"></i></a></span>
    </div>

    <%-- Limited Breadcrumbs --%>
    <nav class="crumbs">
        <a href="/miscadmin">Tools</a>
        <a href="<%= currentPagePath %>.html">AEM Fiddle</a>
        <a href="#">{{ui.getLanguage(data.src.scriptExt)}}
            <span class="script-extension">.{{data.src.scriptExt}}</span></a>
    </nav>

    <div class="drawer">

        <%-- Resource Execution Context --%>
        <input ng-model="data.src.resource"
               type="text"
               placeholder="Absolute resource path"
               class="resource header-item"/>

        <%-- Execute as Workflow --%>
        <label><input
                ng-model="data.src.runAsWorkflow"
                type="checkbox"><span>Run as Workflow</span></label>

        <span class="divider"></span>

        <%-- Run Code Button --%>
        <button ng-click="app.run('<%= runURL %>')"
                class="primary run-code-button header-item">
            <span ng-hide="data.app.running">Run Code</span>
            <span ng-show="data.app.running">Running Code...</span>
        </button>

        <span class="divider"></span>

        <!-- New -->
        <a ng-click="ui.toggleNewPopover()"
           class="icon-add-circle medium action-icon-medium header-item">New Fiddle</a>

        <div id="popover-new"
             ng-show="data.ui.newPopover.visible">
            <div class="popover arrow-top">
                <ul>
                    <li ng-repeat="option in data.ui.scriptExtOptions">
                        <a ng-click="app.new(option.value, false)"
                           href="#new-{{option.value}}">{{option.label}} <span
                                class="script-extension">.{{option.value}}</span></a>
                    </li>
                </ul>
            </div>
        </div>

        <span class="divider"></span>

        <%-- Rail Toggle Button --%>
        <a ng-click="ui.toggleRail()"
              class="toggle-rail-button medium icon-navigation action-icon-medium header-item">Show/Hide MyFiddles</a>
    </div>
</header>