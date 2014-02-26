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
<!doctype html>
<html ng-app="qeApp">
<head>
  <meta charset="UTF-8">
  <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">

  <title>Query Editor | ACS AEM Tools</title>

  <link rel="shortcut icon" href="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAAA2hpVFh0WE1MOmNvbS5hZG9iZS54bXAAAAAAADw/eHBhY2tldCBiZWdpbj0i77u/IiBpZD0iVzVNME1wQ2VoaUh6cmVTek5UY3prYzlkIj8+IDx4OnhtcG1ldGEgeG1sbnM6eD0iYWRvYmU6bnM6bWV0YS8iIHg6eG1wdGs9IkFkb2JlIFhNUCBDb3JlIDUuMy1jMDExIDY2LjE0NTY2MSwgMjAxMi8wMi8wNi0xNDo1NjoyNyAgICAgICAgIj4gPHJkZjpSREYgeG1sbnM6cmRmPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5LzAyLzIyLXJkZi1zeW50YXgtbnMjIj4gPHJkZjpEZXNjcmlwdGlvbiByZGY6YWJvdXQ9IiIgeG1sbnM6eG1wTU09Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC9tbS8iIHhtbG5zOnN0UmVmPSJodHRwOi8vbnMuYWRvYmUuY29tL3hhcC8xLjAvc1R5cGUvUmVzb3VyY2VSZWYjIiB4bWxuczp4bXA9Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC8iIHhtcE1NOk9yaWdpbmFsRG9jdW1lbnRJRD0ieG1wLmRpZDowMjgwMTE3NDA3MjA2ODExODA4Mzg1QkVDMDdCMTk1OCIgeG1wTU06RG9jdW1lbnRJRD0ieG1wLmRpZDo0OTRCQUU3RURBN0QxMUUyQUE3NzgzNzUxMTU5RTYyQyIgeG1wTU06SW5zdGFuY2VJRD0ieG1wLmlpZDo0OTRCQUU3RERBN0QxMUUyQUE3NzgzNzUxMTU5RTYyQyIgeG1wOkNyZWF0b3JUb29sPSJBZG9iZSBQaG90b3Nob3AgQ1M2IChNYWNpbnRvc2gpIj4gPHhtcE1NOkRlcml2ZWRGcm9tIHN0UmVmOmluc3RhbmNlSUQ9InhtcC5paWQ6MDE4MDExNzQwNzIwNjgxMTgwODNCRUQ5QjhGQzgxQzIiIHN0UmVmOmRvY3VtZW50SUQ9InhtcC5kaWQ6MDI4MDExNzQwNzIwNjgxMTgwODM4NUJFQzA3QjE5NTgiLz4gPC9yZGY6RGVzY3JpcHRpb24+IDwvcmRmOlJERj4gPC94OnhtcG1ldGE+IDw/eHBhY2tldCBlbmQ9InIiPz4B+tbuAAABe0lEQVR42pSTPU8CQRCGx8METUxAKxs7Ck0sSDR+QY2VP8H4FSuNhYkNDYW1Mf4VQSOoldpZQGMjKIgkakFET9jjbm/cOXcJp3w5yZvZ7O3zzkxuV4N/RD76itntAoplQGiI9rRe4VgshrxWB/ODQSZyey+2gmTSk0HtZBp3J5MsWtgDs1yFevkLrscSV2TS1aB2OoPIOXg0feBo6Y5vve04JlaF0edQXyeYnc2ibZpAQpmr7xoPH9qeXC4XF0du+tvCyTkHRstqEgevlwm4dCyOZMig5QgsNY8KULIpcwtGNktxCSeF0n8MjPMFdCD+A9hCKDW88ZyQcEpm3WVgXISwAbpaF/B6keC0qkyw6x4Yl2EH/g1SN/61p5Zww6CYDqAWikCruf2r+bZww2AsmAXLqEB9atw1t3/lsSPsGmHQdwAWK8PnhNfpxLf80BVuDlSicUSmX7UvFFaPpl2om0iva1Fmihe6JL1UVgZUZbSpmi5N9G5v5VuAAQARSw91DwEPugAAAABJRU5ErkJggg=="/>

  <cq:includeClientLib css="query-editor.app"/>
</head>

<body ng-controller="QueryEditorCtrl" ng-init="running = true; refresh()">

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

<cq:includeClientLib js="query-editor.app"/>

</body>
</html>