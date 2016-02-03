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

    <h2 acs-coral-heading>Popular Queries</h2>

    <p>Click on a query below to load into explanation form above</p>

    <table class="data coral-Table coral-Table--hover">
        <thead>
        <tr class="coral-Table-row">
            <th class="coral-Table-headerCell">Duration (ms)</th>
            <th class="coral-Table-headerCell">Occurrence Count</th>
            <th class="coral-Table-headerCell">Language</th>
            <th class="coral-Table-headerCell">Statement</th>
            <th class="coral-Table-headerCell"></th>
        </tr>
        </thead>
        <tbody>
        <tr ng-repeat="query in queries.popular track by $index"
            class="coral-Table-row"
            ng-class="{ expanded : query.expanded }">
            <td class="coral-Table-cell num"
                ng-click="load(query)">
                <div>{{ query.duration }}</div>
            </td>
            <td class="coral-Table-cell num"
                ng-click="load(query)">
                <div>{{ query.occurrenceCount }}</div>
            </td>
            <td class="coral-Table-cell" ng-click="load(query)">
                <div>{{ query.language }}</div>
            </td>
            <td class="coral-Table-cell" ng-click="load(query)">
                <div>{{ query.statement }}</div>
            </td>
            <td class="coral-Table-cell">
                <a ng-click="query.expanded = !query.expanded"
                   class="coral-Icon"
                   ng-class="query.expanded ? 'coral-Icon--treeCollapse' : 'coral-Icon--treeExpand'">
                </a>
            </td>
        </tr>
        </tbody>
    </table>
</div>