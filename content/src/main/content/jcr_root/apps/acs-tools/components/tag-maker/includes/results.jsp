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

<div ng-show="result.tagIds.length > 0"
     class="results">

    <h2 acs-coral-heading>{{ result.tagIds.length }} Tags processed</h2>

    <p>
        Please review these tags for correctness using the <a href="/tagging" tarsget="_blank"/>AEM Tagging console</a>.
    </p>

    <ul ng-repeat="tagId in result.tagIds track by $index">
        <li>{{ tagId }}</li>
    </ul>
</div>

