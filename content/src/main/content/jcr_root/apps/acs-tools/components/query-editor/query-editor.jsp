<%--
  #%L
  ACS AEM Tools Package
  %%
  Copyright (C) 2014 Adobe
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

<%@include file="/libs/foundation/global.jsp" %>
<%
    final String faviconPath = resourceResolver.map(component.getPath() + "/clientlibs/images/favicon.png");
%>
<!doctype html>
<html>
<head>
    <meta charset="UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">

    <title>Query Editor | ACS AEM Tools</title>

    <link rel="shortcut icon" href="<%= faviconPath %>"/>

    <cq:includeClientLib css="query-editor.app"/>
</head>

<body>
<div id="qeApp" ng-controller="QueryEditorCtrl" ng-init="running = true; refresh()">
    <header class="top">

        <div class="logo">
            <a href="/"><i class="icon-marketingcloud medium"></i></a>
            <span ng-hide="running" class="spinner icon-spinner spinner medium"></span>
        </div>

        <nav class="crumbs">
            <a href="/miscadmin">Tools</a>
            <a href="<%= currentPage.getPath() %>.html">Query Editor</a>
        </nav>

        <div class="drawer theme-dark">
            <label><input type="checkbox" ng-model="autoQuery" ng-change="refresh()"><span>Auto Query</span></label>
            &nbsp;
            <button class="primary" ng-click="query()" ng-disabled="autoQuery">
                <span ng-show="!status.requesting">Run Query</span>
                <span ng-show="status.requesting">Querying...</span>
            </button>
        </div>

    </header>

    <div class="page" role="main">
        <div class="content">

        <pre id="ace-input" ui-ace="{
          mode: 'querybuilder',
          theme: 'vibrant_ink',
          onLoad: initEditor,
          onChange: $parent.refresh
        }" ng-model="$parent.source" ng-controller="QueryInputCtrl"></pre>

        <pre id="ace-output" ui-ace="{
          mode: 'json',
          theme: 'vibrant_ink',
          onLoad: initEditor
        }" readonly="true" ng-model="$parent.json" ng-controller="QueryOutputCtrl"></pre>

        </div>
    </div>

    <footer ui-ace-statusbar="#ace-input">
        <span ng-show="status.requesting" class="loader"></span>
        <span ng-hide="status.requesting">Query took {{status.duration / 1000 | number}} seconds</span>
    </footer>

</div>
<cq:includeClientLib js="query-editor.app"/>
</body>
</html>