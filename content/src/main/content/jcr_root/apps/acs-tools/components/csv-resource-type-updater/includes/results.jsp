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

<div ng-show="result.processed && (result.success.length > 0 || result.failure.length > 0)"
     class="results">

    <div ng-show="result.success.length > 0">
        <h2 acs-coral-heading>{{ result.success.length }} resources successfully processed</h2>

        <ul ng-repeat="path in result.success track by $index">
            <li>{{ path }}</li>
        </ul>
    </div>

    <div ng-show="result.failure.length > 0">
        <h2 acs-coral-heading>{{ result.failure.length }} resources failed to process</h2>

        <ul ng-repeat="path in result.failure track by $index">
            <li>{{ path }}</li>
        </ul>
    </div>

</div>

<div ng-show="result.processed && result.success.length === 0 && result.failure.length === 0" class="results">
    <h2 acs-coral-heading>No matching resources could be found to update</h2>

    <p>Check to ensure the configuration parameters and the CSV file are correct.</p>
</div>