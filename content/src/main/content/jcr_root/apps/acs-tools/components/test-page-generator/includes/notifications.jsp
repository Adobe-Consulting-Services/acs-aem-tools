<%--
  ~ #%L
  ~ ACS AEM Tools Bundle
  ~ %%
  ~ Copyright (C) 2013 Adobe
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

<div ng-show="notifications.length > 0">
    <div ng-repeat="notification in notifications">
        <div class="alert {{ notification.type }}">
            <button class="close" data-dismiss="alert">&times;</button>
            <strong>{{ notification.title }}</strong>

            <div>{{ notification.message }}</div>
        </div>
    </div>
</div>


<div ng-show="app.running">

    <div class="alert notice large">
        <strong>Creating pages</strong>
        <div>
            <i class="spinner large"></i>
            Please be patient while the system creates your pages; depending on the total
            number of pages requested to be created this process could take a long time.
        </div>
    </div>
</div>

<div ng-show="results.success"
     class="results alert success large">
    <strong>Results Summary</strong>

    <div>
        <ul>
            <li>Created under: {{ results.rootPath }}</li>
            <li>Bucket depth: {{ results.depth }}</li>
            <li>Bucket size: {{ results.bucketSize }}</li>
            <li>Save threshold: {{ results.saveThreshold }}</li>
            <li>Total pages created: {{ results.count }}</li>
            <li>Total time: {{ results.totalTime }} s </li>
        </ul>

        <a x-cq-linkchecker="skip"
           target="_blank"
           href="<%= "/crx/de/index.jsp#" %>{{ results.rootPath }}">
            Open in CRXDE Lite</a>
    </div>
</div>