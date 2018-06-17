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

    pageContext.setAttribute("rcpPath",
            resourceResolver.map(slingRequest, "/system/jackrabbit/filevault/rcp"));

    pageContext.setAttribute("legacyRcpPath",
            resourceResolver.map(slingRequest, "/libs/granite/packaging/rcp"));

%><div  ng-controller="MainCtrl"
        ng-init="init(['${rcpPath}', '${legacyRcpPath}']);">

    <%-- VLT-RCP Not installed --%>
    <div ng-show="vltMissing">
        <div acs-coral-alert data-alert-type="error" data-alert-size="large"
            data-alert-title="VLT-RCP servlet missing, inactive or unreachable">
                VLT-RCP endpoint could not be reached.

                <ol>
                    <li>
                        <a href="http://search.maven.org/#search%7Cga%7C1%7Corg.apache.jackrabbit.vault.rcp"
                           target="_blank">Download and install</a>
                        VLT-RCP on this AEM instance.
                    </li>
                    <li>Ensure the  <a href="/system/console/bundles"  x-cq-linkchecker="skip"
                                       target="_blank">Apache Jackrabbit FileVault RCP Server
                        Bundle is Active</a>.
                    </li>
                    <li>
                        The VLT-RCP endpoint URL must be accessible and not blocked via
                        Dispatcher or another reverse proxy. Note: The VLT RCP servlet
                        endpoint changed from &quot;/system/jackrabbit/filevault/rcp&quot; to
                        &quot;/libs/granite/packaging/rcp&quot; in VLT-RCP 3.1.6.
                    </li>
                </ol>
        </div>
    </div>

    <div ng-show="!vltMissing" class="auto-refresh-section">

        <div class="coral-ButtonGroup top-button-group">
            <div class="coral-Selector">
                <label class="coral-Selector-option auto-refresh-label">
                    <input class="coral-Selector-input" type="checkbox" ng-model="checkboxModel.autoRefresh">
                    <span class="coral-Selector-description auto-refresh-description">
                        <i class="coral-Icon coral-Icon--refresh coral-Selector-icon"></i>
                        <span class="auto-refresh-text">Auto Refresh</span>
                    </span>
                </label>
            </div>

            <button class="coral-Button"
                    data-target="#create-new-task-modal"
                    data-toggle="modal"><i class="icon-add"></i> Add Task</button>
        </div>

        <%-- No Tasks Defined --%>
        <div class="section" ng-show="tasks.length == 0">

            <%-- No Tasks Defined --%>
            <div acs-coral-alert data-alert-type="notice" data-alert-size="large"
                 data-alert-title="No tasks defined" data-dismissible="false">
                    No VLT-RCP tasks have been defined.

                    <ul>
                        <li><a href="#"
                                 data-target="#create-new-task-modal"
                                 data-toggle="modal">Create a new VLT-RCP task.</a></li>
                    </ul>

            </div>

        </div>

        <%-- Tasks Defined --%>
        <div class="section" ng-show="tasks.length > 0">

            <h2 acs-coral-heading>Current Tasks</h2>

            <p>
                Click on the <i class="coral-Icon coral-Icon--treeExpand"></i> to view the details for each Task.
            </p>

            <table class="coral-Table data tasks">
                <thead>
                    <tr class="coral-Table-row">
                        <th class="coral-Table-headerCell">Task Id</th>
                        <th class="coral-Table-headerCell">Status</th>
                        <th class="coral-Table-headerCell">Settings</th>
                        <th class="coral-Table-headerCell">Actions</th>
                    </tr>
                </thead>

                <tbody>
                    <tr class="coral-Table-row" ng-repeat="task in tasks"
                        ng-class="{ expanded : task.expanded }">
                        <td class="coral-Table-cell">
                            {{ task.id }}
                        </td>

                        <td class="coral-Table-cell">
                            <div>{{ task.status.state }}</div>
                            <div ng-show="task.expanded">

                                <ul>
                                    <li>Current path: {{ task.status.currentPath || 'N/A' }}</li>
                                    <li>Last saved path:
                                        {{ task.status.lastSavedPath || 'N/A' }}</li>
                                    <li>Total nodes: {{ task.status.totalNodes }}</li>
                                    <li>Total size: {{ task.status.totalSize }}</li>
                                    <li>Current size: {{ task.status.currentSize }}</li>
                                    <li>Current nodes: {{ task.status.currentNodes }}</li>
                                </ul>
                            </div>
                        </td>
                        <td class="coral-Table-cell">
                            <ul>
                                <li>Source: {{ task.src | removeCredentials
                                    }}</li>
                                <li>Destination: {{ task.dst }}</li>
                            </ul>

                            <ul ng-show="task.expanded">
                                <li>Recursive: {{ task.recursive }}</li>
                                <li>Batch size: {{ task.batchsize }}</li>
                                <li>Update: {{ task.update }}</li>
                                <li>Only newer: {{ task.onlyNewer }}</li>
                                <li>No ordering: {{ task.noOrdering }}</li>
                                <li>Throttle: {{ task.throttle || 0}} seconds</li>
                                <li>Resume from: {{ task.resumeFrom || 'Not set'}}</li>

                                <li ng-show="task.excludes.length > 0">
                                    Excludes:
                                    <ul>
                                        <li ng-repeat="exclude in task.excludes track by $index">{{exclude}}</li>
                                    </ul>
                                </li>
                            </ul>
                        </td>
                        <td class="coral-Table-cell actions">
                            <button class="coral-Button coral-Button--square coral-Button--quiet"
                                ng-click="task.expanded = !task.expanded">
                              <i class="coral-Icon" ng-class="task.expanded ? 'coral-Icon--treeCollapse' : 'coral-Icon--treeExpand'"></i>
                            </button>
                            <button class="coral-Button coral-Button--square coral-Button--quiet"
                                ng-click="duplicate(task)"
                                data-target="#create-new-task-modal"
                                data-toggle="modal">
                                <i  class="coral-Icon coral-Icon--duplicate"
                                    data-init="quicktip" 
                                    data-quicktip-type="info" 
                                    data-quicktip-arrow="top" 
                                    data-quicktip-content="New duplicate task"></i>
                            </button>
                            <button class="coral-Button coral-Button--square coral-Button--quiet"
                                ng-show="task.status.state == 'NEW'"
                                ng-click="start(task)">
                              <i class="coral-Icon coral-Icon--playCircle"></i>
                            </button>
                            <button class="coral-Button coral-Button--square coral-Button--quiet"
                                ng-show="task.status.state == 'RUNNING'"
                                ng-click="stop(task)">
                              <i class="coral-Icon coral-Icon--stopCircle"></i>
                            </button>
                            <button class="coral-Button coral-Button--square coral-Button--quiet"
                                ng-click="remove(task)">
                              <i class="coral-Icon coral-Icon--delete"></i>
                            </button>
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>

        <cq:include script="includes/create-task-modal.jsp"/>
    </div>

</div>
