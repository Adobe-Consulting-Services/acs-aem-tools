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
            resourceResolver.map(slingRequest, "/libs/granite/packaging/rcp"));

    pageContext.setAttribute("legacyRcpPath",
            resourceResolver.map(slingRequest, "/system/jackrabbit/filevault/rcp"));

%><div  ng-controller="MainCtrl"
        ng-init="init(['${rcpPath}', '${legacyRcpPath}']);">

    <%-- VLT-RCP Not installed --%>
    <div ng-show="vltMissing">
        <div class="alert error large">
            <strong>VLT-RCP servlet missing, inactive or unreachable</strong>

            <div>
                VLT-RCP endpoint could not be reached.

                <ol>
                    <li>
                        <a href="http://mirrors.ibiblio.org/maven2/org/apache/jackrabbit/vault/org.apache.jackrabbit.vault.rcp"
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
    </div>

    <div ng-show="!vltMissing">

        <div class="button-group top-button-group">
            <div class="selector">
                <label class="auto-refresh-label"><input type="checkbox" ng-model="checkboxModel.autoRefresh"><span>
                    <i class="icon-refresh"></i>Auto Refresh</span></label>
            </div>

            <button
                    data-target="#create-new-task-modal"
                    data-toggle="modal"><i class="icon-add"></i> Add Task</button>
        </div>

        <%-- No Tasks Defined --%>
        <div class="section" ng-show="tasks.length == 0">

            <%-- No Tasks Defined --%>
            <div class="alert notice large">
                <strong>No tasks defined</strong>
                <div>
                    No VLT-RCP tasks have been defined.

                    <ul>
                        <li><a href="#"
                                 data-target="#create-new-task-modal"
                                 data-toggle="modal">Create a new VLT-RCP task.</a></li>
                    </ul>

                </div>
            </div>

        </div>

        <%-- Tasks Defined --%>
        <div class="section" ng-show="tasks.length > 0">

            <h2>Current Tasks</h2>

            <p>
                Click on the <i class="icon-treeexpand"></i> to view the details for each Task.
            </p>

            <table class="data tasks">
                <thead>
                    <tr>
                        <th>Task Id</th>
                        <th>Status</th>
                        <th>Settings</th>
                        <th>Actions</th>
                    </tr>
                </thead>

                <tbody>
                    <tr ng-repeat="task in tasks"
                        ng-class="{ expanded : task.expanded }">
                        <td>
                            {{ task.id }}
                        </td>

                        <td>
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
                        <td>
                            <ul>
                                <li>Source: {{ task.src }}</li>
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
                                        <li ng-repeat="exclude in task.excludes track by $index">{{exclude
                                            }}</li>
                                    </ul>
                                </li>
                            </ul>
                        </td>
                        <td class="actions">

                            <div class="action-button">
                                <a href="#" ng-click="task.expanded = !task.expanded"
                                   ng-class="task.expanded ? 'icon-treecollapse' : 'icon-treeexpand'"></a>
                            </div>

                            <div class="action-button">
                                <a href="#" ng-show="task.status.state == 'NEW'"
                                   ng-click="start(task)"
                                   class="icon-play-circle"></a>
                            </div>

                            <div class="action-button">
                                <a href="#" ng-show="task.status.state == 'RUNNING'"
                                   ng-click="stop(task)"
                                   class="icon-stop"></a>
                            </div>

                            <div class="action-button">
                                <a href="#" ng-click="remove(task)"
                                   class="icon-delete">
                                </a>
                            </div>
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>

        <cq:include script="includes/create-task-modal.jsp"/>
    </div>

</div>