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
<%@include file="/libs/foundation/global.jsp" %><%
%><%@page session="false"
          import="com.adobe.acs.tools.util.AEMCapabilityHelper" %><%

    final AEMCapabilityHelper aemCapabilityHelper = sling.getService(AEMCapabilityHelper.class);

    pageContext.setAttribute("isSupported", aemCapabilityHelper.isOak());
    pageContext.setAttribute("pagePath", resourceResolver.map(currentPage.getPath()));
    pageContext.setAttribute("resourcePath", resourceResolver.map(resource.getPath()));

%><!doctype html>
<html>
<head>
    <meta charset="UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">

    <title>Explain Query | ACS AEM Tools</title>

    <cq:includeClientLib css="acs-tools.explain-query.app"/>
</head>

<body>
    <div id="acs-tools-explain-query-app">

        <header class="top">

            <div class="logo">
                <a href="/"><i class="icon-marketingcloud medium"></i></a>
            </div>

            <nav class="crumbs">
                <a href="/miscadmin">Tools</a>
                <a href="${pagePath}.html">Explain Query</a>
            </nav>
        </header>

        <div class="page" role="main"
             ng-controller="MainCtrl"
             ng-init="app.uri = '${resourcePath}.explain.json'; init();">

            <div class="content">
                <div class="content-container">
                    <div class="content-container-inner">

                        <h1>Explain Query</h1>

                        <c:choose>
                            <c:when test="${isSupported}">

                                <p>Find the query plan used for executing any Query</p>

                                <div ng-show="app.running">
                                    <div class="alert notice">
                                        <strong>Running</strong>
                                        <div>Please be patient. Large or expensive queries may cause longer
                                            explanation times.</div>
                                    </div>
                                </div>

                                <div ng-show="notifications.length > 0">
                                    <div ng-repeat="notification in notifications">
                                        <div class="alert {{ notification.type }}">
                                            <button class="close" data-dismiss="alert">&times;</button>
                                            <strong>{{ notification.title }}</strong>

                                            <div>{{ notification.message }}</div>
                                        </div>
                                    </div>
                                </div>

                                <form ng-submit="explain()">

                                    <div class="form-row">
                                        <h4>Language</h4>

                                        <div class="selector">
                                            <select ng-model="form.language"
                                                    ng-required="true">
                                                <option value="xpath">xpath</option>
                                                <option value="sql">sql</option>
                                                <option value="JCR-SQL2">JCR-SQL2</option>
                                            </select>
                                        </div>
                                    </div>

                                    <div class="form-row">
                                        <h4>Query</h4>

                                        <span>
                                            <textarea
                                                    ng-model="form.statement"
                                                    rows="4"
                                                    cols="20"
                                                    ng-required="true"
                                                    placeholder="Query statement; must match the selected Language above"></textarea>
                                        </span>
                                    </div>

                                    <div class="form-row">
                                        <h4>Include execution time</h4>

                                        <span>
                                            <label><input
                                                    ng-model="form.executionTime"
                                                    type="checkbox"><span>
                                                Run query and report execution time.
                                                Long running queries will delay reporting the explanation.</span></label>
                                        </span>
                                    </div>

                                    <div class="form-row">
                                        <div class="form-left-cell">&nbsp;</div>
                                        <button class="primary">Explain</button>
                                    </div>
                                </form>

                                <div class="section result"
                                     ng-show="result.explain">
                                    <h2>Query Explanation</h2>

                                    <div class="call-out warning" ng-show="result.explain.slow">
                                        Warning! This query has characteristics that may cause performance issues when
                                        executed against large repositories.
                                    </div>

                                    <div class="call-out" ng-show="result.timing">
                                        Total query execution: {{ result.timing.totalTime }} ms

                                        <ul>
                                            <li>query.execute(): {{ result.timing.executeTime }} ms</li>
                                            <li>queryResult.getNodes(): {{ result.timing.getNodesTime }} ms</li>
                                        </ul>
                                    </div>

                                    <div class="call-out"
                                        ng-show="result.explain.propertyIndexes || result.explain.traversal">
                                        <div ng-show="result.explain.propertyIndexes">
                                            Oak indexes used:
                                            <span
                                                    ng-repeat="propertyIndex in result.explain.propertyIndexes">{{propertyIndex}}{{$last ? '' : ', '}}</span>
                                        </div>

                                        <div ng-show="result.explain.traversal">
                                            Traversal query
                                        </div>

                                        <div ng-show="result.explain.aggregate">
                                            Full-text index used
                                        </div>
                                    </div>

                                    <div class="call-out">{{ result.explain.plan }}</div>

                                    <!-- TODO Simple impl which just dumps the logs as bullet list-->
                                    <div class="call-out">
                                        <ul ng-repeat="log in result.explain.logs" >
                                            <li>{{ log }}</li>
                                        </ul>
                                        <div ng-show="result.explain.logsTruncated">
                                        ...
                                        </div>
                                    </div>

                                </div>

                                <%-- Slow Queries --%>
                                <div class="section" ng-show="queries.slow.length > 0">

                                    <h2>Slow Queries</h2>

                                    <p>Click on a query below to load into explanation form above</p>

                                    <table class="data">
                                        <thead>
                                            <tr>
                                                <th>Duration (ms)</th>
                                                <th>Occurrence Count</th>
                                                <th>Language</th>
                                                <th>Statement</th>
                                                <th></th>
                                            </tr>
                                        </thead>

                                        <tbody>
                                            <tr ng-repeat="query in queries.slow"
                                                ng-class="{ expanded : query.expanded }">
                                                <td class="num"
                                                    ng-click="load(query)">
                                                    <div>{{ query.duration }}</div>
                                                </td>
                                                <td class="num"
                                                    ng-click="load(query)">
                                                    <div>{{ query.occurrenceCount }}</div>
                                                </td>
                                                <td ng-click="load(query)">
                                                    <div>{{ query.language }}</div>
                                                </td>
                                                <td ng-click="load(query)">
                                                    <div>{{ query.statement }}</div>
                                                </td>
                                                <td>
                                                    <a href="#"
                                                       ng-click="query.expanded = !query.expanded"
                                                       ng-class="query.expanded ? 'icon-treecollapse' : 'icon-treeexpand'">
                                                       </a>
                                                </td>
                                            </tr>
                                        </tbody>
                                    </table>
                                </div>

                                <%-- Popular Queries --%>
                                <div class="section" ng-show="queries.popular.length > 0">

                                    <h2>Popular Queries</h2>

                                    <p>Click on a query below to load into explanation form above</p>

                                    <table class="data">
                                        <thead>
                                            <tr>
                                                <th>Duration (ms)</th>
                                                <th>Occurrence Count</th>
                                                <th>Language</th>
                                                <th>Statement</th>
                                                <th></th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <tr ng-repeat="query in queries.popular"
                                                ng-class="{ expanded : query.expanded }">
                                                <td class="num"
                                                    ng-click="load(query)">
                                                    <div>{{ query.duration }}</div>
                                                </td>
                                                <td class="num"
                                                    ng-click="load(query)">
                                                    <div>{{ query.occurrenceCount }}</div>
                                                </td>
                                                <td ng-click="load(query)">
                                                    <div>{{ query.language }}</div>
                                                </td>
                                                <td ng-click="load(query)">
                                                    <div>{{ query.statement }}</div>
                                                </td>
                                                <td>
                                                    <a href="#"
                                                       ng-click="query.expanded = !query.expanded"
                                                       ng-class="query.expanded ? 'icon-treecollapse' : 'icon-treeexpand'">
                                                    </a>
                                                </td>
                                            </tr>
                                        </tbody>
                                    </table>
                                </div>

                                <cq:includeClientLib js="acs-tools.explain-query.app"/>

                                <%-- Register angular app; Decreases chances of collisions w other angular apps on the page (ex. via injection) --%>
                                <script type="text/javascript">
                                    angular.bootstrap(document.getElementById('acs-tools-explain-query-app'),
                                            ['explainQueryApp']);
                                </script>

                            </c:when>
                            <c:otherwise>

                                <div class="alert notice large">
                                    <strong>Incompatible version of AEM</strong>

                                    <div>Explain Query is only supported on AEM installs running Apache Jackrabbit Oak based
                                        repositories.
                                    </div>
                                </div>

                            </c:otherwise>
                        </c:choose>

                    </div>
                </div>
            </div>
        </div>
    </div>
</body>
</html>