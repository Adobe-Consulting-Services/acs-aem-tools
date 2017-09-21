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
  --%><%@include file="/libs/foundation/global.jsp" %><%

%><div class="section result" ng-show="result.explain">

    <h2 acs-coral-heading>Query Explanation
        ( <a target="_blank"
             ng-href="data:application/json;,{{ exportAsJSON(result) }}"/>
        Download as JSON</a> )</h2>

    <div class="call-out warning" ng-show="result.explain.slow">
        Warning! This query has characteristics that may cause performance issues when
        executed against large repositories.
    </div>

    <div class="call-out" ng-show="result.heuristics">
        Total query execution: {{ result.heuristics.totalTime }} ms

        <ul>
            <li>query.execute(): {{ result.heuristics.executeTime }} ms</li>
            <li>queryResult.getNodes(): {{ result.heuristics.getNodesTime }} ms</li>

            <li ng-show="result.heuristics.count !== undefined">Result count:
                {{ result.heuristics.count }}</li>
            <li ng-show="result.heuristics.countTime !== undefined">Result count time:
                {{ result.heuristics.countTime }} ms</li>
        </ul>
    </div>

    <div class="call-out"
         ng-show="result.explain.propertyIndexes || result.explain.traversal">
        <div ng-show="result.explain.propertyIndexes">
            Oak indexes used:
            <span ng-repeat="propertyIndex in result.explain.propertyIndexes track by $index">{{propertyIndex}}{{$last ? '' : ', '}}</span>
        </div>

        <div ng-show="result.explain.traversal">
            Traversal query
        </div>

        <div ng-show="result.explain.aggregate">
            Full-text index used
        </div>
    </div>

    <div class="call-out">{{ result.explain.plan }}</div>

    <div class="call-out">
        <ul class="log-messages"
            ng-repeat="log in result.explain.logs track by $index">
            <li>{{ log }}</li>
        </ul>
        <div ng-show="result.explain.logsTruncated">
            ...
        </div>
    </div>

</div>