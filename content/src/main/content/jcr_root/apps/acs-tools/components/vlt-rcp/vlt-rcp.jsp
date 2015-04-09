<%--
  #%L
  ACS AEM Tools Package
  %%
  Copyright (C) 2014 Adobe
  %%
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
  #L%
  --%>

<%@include file="/libs/foundation/global.jsp"%>
<%
    final String faviconPath = resourceResolver.map(component.getPath() + "/clientlibs/images/favicon.png");
%>

<!doctype html>
<html ng-app="vltApp">
<head>
<meta charset="UTF-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">

<title>VLT-RCP | ACS AEM Tools</title>

<link rel="shortcut icon" href="<%= faviconPath %>" />

<cq:includeClientLib css="vlt-rcp.app" />

</head>

<body id="acs-tools-vlt-rcp">

	<div id=vltApp" ng-controller="MainCtrl">
		<header class="top">

			<div class="logo">
				<a href="/"><i class="icon-marketingcloud medium"></i></a> <span
					ng-show="running" class="spinner icon-spinner medium"></span>
			</div>

			<nav class="crumbs">
				<a href="/miscadmin">Tools</a> <a
					href="<%= currentPage.getPath() %>.html">VTL-RCP</a>
			</nav>

	        <div class="drawer theme-dark">	
	            <label>Auto refresh </label><input class="opaque" type="checkbox" ng-model="checkboxModel.autoRefresh">
	            <a href="#" class="icon-add medium" ng-click="createTask()"></a>
	        </div>
		</header>

		<div class="page" role="main" ng-init="init();">
			<div class="content">
				<div class="section" ng-show="tasks.length == 0">

					<h2>No Task defined</h2>

					<p>Click on <a href="#" class="icon-add medium" ng-click="createTask()"></a> to create a task</p>

				</div>

				<div class="section" ng-show="tasks.length > 0">

					<h2>Current Tasks</h2>

					<p>Click on a task below to view the current status</p>

					<table class="data fullwidth">
						<thead>
							<tr>
								<th>ID</th>
								<th>Status</th>
								<th>Settings</th>
								<th>Action</th>
							</tr>
						</thead>

						<tbody>
							<tr ng-repeat="task in tasks"
								ng-class="{ expanded : task.expanded }">
								<td class="num">
									<div>{{ task.id }}</div>
								</td>
								<td class="num">
									<div>{{ task.status.state }}</div>
									<div ng-show="task.expanded">
										Current Path: {{ task.status.currentPath }}<br>
										Last Saved Path: {{ task.status.lastSavedPath }}<br>
										Total Nodes: {{ task.status.totalNodes }}<br>
										Total Size: {{ task.status.totalSize }}<br>
										Current Size: {{ task.status.currentSize }}<br>
										Current Nodes: {{ task.status.currentNodes }}<br>
									</div>
								</td>
								<td class="num">
									<div>
										Source: {{ task.src }}<br>
										Destination: {{ task.dst }}<br>
									</div>
									<div ng-show="task.expanded">
										Recursive: {{ task.recursive }}<br>
										Batch Size: {{ task.batchsize }}<br>
										Update: {{ task.update }}<br>
										Only Newer: {{ task.onlyNewer }}<br>
										No ordering: {{ task.noOrdering }}<br>
										Throttle: {{ task.throttle }}<br>
										Resume from: {{ task.resumeFrom }}<br>
									</div>
								</td>
								<td><a href="#" ng-click="task.expanded = !task.expanded"
									ng-class="task.expanded ? 'icon-treecollapse' : 'icon-treeexpand'">
								</a><a href="#" ng-show="task.status.state == 'NEW'" ng-click="start(task)"
									class="icon-play-circle">
								</a><a href="#" ng-show="task.status.state == 'RUNNING'" ng-click="stop(task)"
									class="icon-stop">
								</a><a href="#" ng-click="remove(task)"
									class="icon-delete">
								</a></td>
							</tr>
						</tbody>
					</table>
				</div>
			</div>
		</div>

	</div>

	<cq:includeClientLib js="vlt-rcp.app" />
	<script type="text/ng-template" id="createTaskTemplate">
    <h1>Create a new task</h1>
	<form name="myForm" ng-controller="MainCtrl">
	<table class="fullwidth">
        <tr><td><label>Id</label></td><td><input type="text" ng-model="task_id" name="id" size="30"></td></tr>
        <tr><td><label>Source</label></td><td class="fullwidth"><input type="text" class="fullwidth" ng-model="task_src" name="src" placeholder="http://admin:admin@localhost:4502/crx/server/-/jcr:root/content/dam/geometrixx"></td></tr>
        <tr><td><label>Destination</label></td><td><input type="text" class="fullwidth" ng-model="task_dst" name="dst" placeholder="/content/geometrixx2"></td></tr>
        <tr><td><label>Recursive</label></td><td><input class="opaque" type="checkbox" ng-model="checkboxModel.recursive"></td></tr>
        <tr><td><label>Update</label></td><td><input class="opaque" type="checkbox" ng-model="checkboxModel.update"></td></tr>
        <tr><td><label>Only&nbsp;newer</label></td><td><input class="opaque" type="checkbox" ng-model="checkboxModel.onlyNewer"></td></tr>
        <tr><td><label>No&nbsp;ordering</label></td><td><input class="opaque" type="checkbox" ng-model="checkboxModel.noOrdering"></td></tr>
        <tr><td><label>Resume&nbsp;from</label></td><td><input type="text" class="fullwidth" ng-model="task_resumeFrom" name="resumeFrom" placeholder="/content/geometrixx2"></td></tr>
	    <tr><td></td><td><input type="button" value="Create" ng-click="confirm()"/><input type="button" value="Cancel" ng-click="closeThisDialog()"/></td></tr>
	</table>
    </form>
	</script>

</body>
</html>