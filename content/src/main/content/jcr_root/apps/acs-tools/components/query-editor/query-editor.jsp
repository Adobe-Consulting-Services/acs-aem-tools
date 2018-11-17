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
    pageContext.setAttribute("faviconPath",
            resourceResolver.map(slingRequest, component.getPath() + "/clientlibs/images/favicon.png"));

    /* ACE JS Base path */
    pageContext.setAttribute("aceEditorBasePath",
            resourceResolver.map(slingRequest, "/etc/clientlibs/acs-tools/vendor/aceeditor"));

    /* Application paths */
    pageContext.setAttribute("queryBuilderPath",
            resourceResolver.map(slingRequest, "/bin/querybuilder.json"));

    pageContext.setAttribute("nodeTypesPath",
            resourceResolver.map(slingRequest, "/crx/de/nodetypes.jsp"));

    pageContext.setAttribute("fileSearchPath",
            resourceResolver.map(slingRequest, "/crx/de/filesearch.jsp"));

    pageContext.setAttribute("predicatesPath",
            resourceResolver.map(slingRequest, "/bin/acs-tools/qe/predicates.json"));

%><!doctype html>
<html class="coral-App">
<head>
    <meta charset="UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">

    <title>Query Editor | ACS AEM Tools</title>

    <link rel="shortcut icon" href="${faviconPath}"/>

    <cq:includeClientLib css="query-editor.app"/>

</head>

<body id="acs-tools-query-editor" class="endor-Panel coral--light"
    ng-controller="QueryEditorCtrl"
     ng-init="init({
                    queryBuilderPath: '${queryBuilderPath}',
                    nodeTypesPath: '${nodeTypesPath}',
                    fileSearchPath: '${fileSearchPath}',
                    predicatesPath: '${predicatesPath}'
                }); running = true; refresh();">

    <%-- Header --%>

    <coral-shell-header
            class="coral--dark granite-shell-header coral3-Shell-header"
            role="region"
            aria-label="Header Bar"
            aria-hidden="false">
        <coral-shell-header-home class="globalnav-toggle"
                                 data-globalnav-toggle-href="/"
                                 role="heading"
                                 aria-level="2">
            <a is="coral-shell-homeanchor"
               style="display: inline-block; padding-right: 0;"
               icon="adobeExperienceManagerColor"
               href="/"
               class="coral3-Shell-homeAnchor">
                <coral-shell-homeanchor-label>Adobe Experience Manager</coral-shell-homeanchor-label>
            </a>
            <span style="line-height: 2.375rem;">/ ACS AEM Tools / Query Editor</span>
        </coral-shell-header-home>
        <coral-shell-header-actions>
            <span class="auto-query-wrapper">
                <label acs-coral-checkbox>
                    <input type="checkbox" ng-model="autoQuery" ng-change="refresh()"/>
                    <span>Auto Query</span>
                </label>
                <div ng-show="showAutoQueryWarning"
                     class="auto-query-warning tooltip notice arrow-top">

                    Enabling Auto Query may result in long running queries that
                    can cause unresponsiveness.
                </div>
            </span>
            <button class="coral-Button coral-Button--primary" ng-click="query()" ng-disabled="autoQuery">
                <span ng-show="!status.requesting">Run Query</span>
                <span ng-show="status.requesting">Querying...</span>
            </button>
        </coral-shell-header-actions>
    </coral-shell-header>



    <pre id="ace-input"
         ui-ace="{
              mode: 'querybuilder',
              theme: 'vibrant_ink',
              onLoad: initEditor,
              onChange: $parent.refresh
         }"
         ng-init="aceEditorBasePath='${aceEditorBasePath}'"
         ng-model="$parent.source"
         ng-controller="QueryInputCtrl"></pre>

    <pre id="ace-output"
         ui-ace="{
              mode: 'json',
              theme: 'vibrant_ink',
              onLoad: initEditor
         }"
         readonly="true"
         ng-init="aceEditorBasePath='${aceEditorBasePath}'"
         ng-model="$parent.json"
         ng-controller="QueryOutputCtrl"></pre>

    <div ng-show="status.requesting" class="query-run-overlay-wrapper">
        <div class="spinner large"></div>
    </div>

    <footer ui-ace-statusbar="#ace-input">
        <span ng-show="status.requesting" class="loader"></span>
        <span ng-hide="status.requesting">Query took {{status.duration / 1000 | number}} seconds</span>
    </footer>

<cq:includeClientLib js="query-editor.app"/>
</body>
</html>