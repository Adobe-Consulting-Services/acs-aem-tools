<%--
  ~ #%L
  ~ ACS AEM Tools Bundle
  ~ %%
  ~ Copyright (C) 2015 Adobe
  ~ %%
  ~ Licensed under the Apache License, Version 2.0 (the "License");
  ~ you may not use this file except in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~   http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  ~ #L%
  --%>

<%@include file="/libs/foundation/global.jsp" %><%

%><%-- Popular Queries --%>
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
        <tr ng-repeat="query in queries.popular track by $index"
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