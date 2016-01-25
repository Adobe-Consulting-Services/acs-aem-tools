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

<div ng-show="result.assets.length > 0 || result.failures.length > 0"
     class="results">

    <h2 acs-coral-heading>{{ result.assets.length }} Assets imported
        <span ng-show="result.failures > 0"> with {{ result.failures.length }} failures</span>
    </h2>

    <%-- Success paths --%>
    <div ng-show="result.assets && result.assets.length > 0">
        <h4 acs-coral-heading>Success</h4>

        <ul ng-repeat="asset in result.assets track by $index">
            <li>{{ asset }}</li>
        </ul>
    </div>

    <%-- Failures paths --%>
    <div ng-show="result.failures && result.failures.length > 0">
        <h4 acs-coral-heading>Failures</h4>
    
        <ul ng-repeat="asset in result.failures track by $index">
            <li>{{ asset }}</li>
        </ul>
    </div>
</div>

