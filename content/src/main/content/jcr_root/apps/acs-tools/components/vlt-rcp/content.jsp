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

    <!-- VLT-RCP Not installed Message -->
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
    <!-- // VLT-RCP Not installed Message -->


    <div ng-show="!vltMissing" class="auto-refresh-section">
        
        <!-- Header buttons: add/refresh -->
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
        <!-- // Header buttons: add/refresh -->

        <!-- No tasks defined message -->
        <div class="section" ng-show="tasks.length == 0">
            <div acs-coral-alert data-alert-type="notice" data-alert-size="large"
                 data-alert-title="No tasks defined" data-dismissible="false">
                    No VLT-RCP tasks have been defined.
                    <ul>
                        <li><a href="#"
                               data-target="#create-new-task-modal"
                               data-toggle="modal">Create a new VLT-RCP task.</a>
                        </li>
                    </ul>
            </div>
        </div>
        <!-- // No tasks defined message -->

        <!-- Tasks accordion section -->
        <div class="section" ng-show="tasks.length > 0">

            <h2 acs-coral-heading>Current Tasks</h2>
            <p>
                Click on the <i class="coral-Icon coral-Icon--duplicate"></i> icon to create a new task with duplicate values.
            </p>
            <p>
                When the "<i class="coral-Icon coral-Icon--refresh"></i> Auto Refresh" button is enabled, tasks will flash to indicate an update
            </p>

            <ul class="coral-Accordion coral-Collapsible--block tasks"  id="tasks">
                <li class="coral-Accordion-item" 
                    ng-class="{ 'is-active' :  task.expanded}"
                    ng-repeat="task in tasks"
                    ng-init="task.expanded = taskExpandedStatuses[$index]">
                    <h3 class="coral-Accordion-header" 
                        ng-click="taskExpandedStatuses[$index] = !taskExpandedStatuses[$index]; task.expanded = taskExpandedStatuses[$index]">
                        <i class="coral-Icon coral-Icon--sizeS" ng-class="{'coral-Icon--chevronRight': !task.expanded, 'coral-Icon--chevronDown': task.expanded}"></i>
                        <span class="coral-Accordion-title">{{ task.id }}</span>
                        <span class="coral-Accordion-subtitle">
                            status: {{ task.status.state }} | <b>src: </b>{{ task.src | removeCredentials | truncate:50 }} | <b>dest: </b> {{ task.dst  | truncate:50 }}
                        </span>
                        <!-- Task Action buttons -->
                        <span class="task-actions">
                            <button class="coral-Button--quiet"
                                title="Create new duplicate task with this tasks values"
                                ng-click="duplicate(task)"
                                data-target="#create-new-task-modal"
                                data-toggle="modal">
                                <i  class="coral-Icon coral-Icon--sizeS coral-Icon--duplicate"></i>
                            </button>
                            <button class="coral-Button--quiet"
                                title="Start task"
                                ng-show="task.status.state == 'NEW'"
                                ng-click="start(task)">
                                <i class="coral-Icon coral-Icon--sizeS coral-Icon--playCircle"></i>
                            </button>
                            <button class="coral-Button--quiet"
                                title="Stop task"
                                ng-show="task.status.state == 'RUNNING'"
                                ng-click="stop(task)">
                                <i class="coral-Icon coral-Icon--sizeS coral-Icon--stopCircle"></i>
                            </button>
                            <button class="coral-Button--quiet"
                                title="Delete Task"
                                ng-click="remove(task)">
                                <i class="coral-Icon coral-Icon--sizeS coral-Icon--delete"></i>
                            </button>
                        </span>
                        <!-- // Task Action buttons -->
                        <br/>
                    </h3>
                    <div class="coral-Accordion-content" ng-style="{'display' : task.expanded ? 'block' : 'none'}">
                        
                        <div class="task-status">
                            <h3>Task Status</h3>
                            <ul>
                                <li><b>State:</b> {{ task.status.state }}</li>
                                <li><b>Current path:</b> {{ task.status.currentPath || 'N/A' }}</li>
                                <li><b>Last saved path:</b> {{ task.status.lastSavedPath || 'N/A' }}</li>
                                <li><b>Total nodes:</b> {{ task.status.totalNodes }}</li>
                                <li><b>Total size:</b> {{ task.status.totalSize }}</li>
                                <li><b>Current size:</b> {{ task.status.currentSize }}</li>
                                <li><b>Current nodes:</b> {{ task.status.currentNodes }}</li>
                            </ul>
                        </div>

                        <div class="task-settings">
                            <h3>Task Settings</h3>
                            <ul>
                                <li><b>Source:</b> {{ task.src | removeCredentials}}</li>
                                <li><b>Destination:</b> {{ task.dst }}</li>
                                <li><b>Recursive:</b> {{ task.recursive }}</li>
                                <li><b>Batch size:</b> {{ task.batchsize }}</li>
                                <li><b>Update:</b> {{ task.update }}</li>
                                <li><b>Only newer:</b> {{ task.onlyNewer }}</li>
                                <li><b>No ordering:</b> {{ task.noOrdering }}</li>
                                <li><b>Throttle:</b> {{ task.throttle || 0}} seconds</li>
                                <li><b>Resume from:</b> {{ task.resumeFrom || 'Not set'}}</li>
                                <li ng-show="task.excludes.length > 0">
                                    Excludes:
                                    <ul>
                                        <li ng-repeat="exclude in task.excludes track by $index">{{exclude}}</li>
                                    </ul>
                                </li>
                            </ul>
                        </div>
                    </div>
                </li>
            </ul>
        </div>
        <!-- // Tasks accordion section -->

        <cq:include script="includes/create-task-modal.jsp"/>
    </div>

</div>
