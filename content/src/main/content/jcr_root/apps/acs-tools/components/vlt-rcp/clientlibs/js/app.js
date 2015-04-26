/*
 * #%L
 * ACS AEM Tools Package
 * %%
 * Copyright (C) 2014 Adobe
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

/*global angular: false */

var vltApp = angular.module('vltRcpApp', ['ngDialog']);

vltApp.controller('MainCtrl', function ($scope, $http, $timeout, $interval, $q, ngDialog) {

    $scope.rcp_uris = ['/libs/granite/packaging/rcp', '/system/jackrabbit/filevault/rcp'];

    $scope.rcp_uri = '/libs/granite/packaging/rcp';

    $scope.task_src = 'http://admin:admin@localhost:4502/crx/server/-/jcr:root/content/dam/geometrixx';

    $scope.task_dst = '/content/dam/geometrixx2';

    $scope.task_batchSize = "2048";

    $scope.task_throttle = "1";

    $scope.checkboxModel = {
        recursive: true,
        update: true,
        onlyNewer: true,
        noOrdering: false,
        autoRefresh: false
    };
    $scope.app = {
        uri: '',
        running: false
    };

    $scope.notifications = [];

    $scope.tasks = [];

    $scope.excludes = [];

    $scope.vltMissing = true;

    /*
     * Loads the tasks
     */
    $scope.init = function () {

        angular.forEach($scope.rcp_uris, function (uri) {

            $http.get(uri,
                {
                    params: {ck: (new Date()).getTime()}
                }
            ).
                success(function (data, status, headers, config) {
                    if (status === 200) {
                        $scope.rcp_uri = uri;
                        $scope.vltMissing = false;
                        $scope.tasks = data.tasks || [];
                    }
                });
        });
    };

    $scope.refresh = function () {
        $http.get($scope.rcp_uri,
            {
                params: {ck: (new Date()).getTime()}
            }
        ).
            success(function (data, status, headers, config) {
                $scope.tasks = data.tasks || [];
            })
            .error(function (data, status, headers, config) {
                $scope.addNotification('error', 'ERROR', 'Could not refresh tasks.');
            });
    };

    $scope.addNotification = function (type, title, message) {
        var timeout = 10000;

        if (type === 'success') {
            timeout = timeout / 2;
        }

        $scope.notifications.unshift({
            type: type,
            title: title,
            message: message
        });

        $timeout(function () {
            $scope.notifications.shift();
        }, timeout);
    };

    /*
     * Start task
     */
    $scope.start = function (task) {
        var cmd = {
            "cmd": "start",
            "id": task.id
        };

        $http.post($scope.rcp_uri, cmd).
            success(function (data, status, headers, config) {
                $scope.addNotification('info', 'INFO', 'Task ' + task.id + ' started.');
                $scope.refresh();
            }).
            error(function (data, status, headers, config) {
                $scope.addNotification('error', 'ERROR', 'Could not retrieve tasks.');
            });
    };

    /*
     * Stop task
     */
    $scope.stop = function (task) {
        var cmd = {
            "cmd": "stop",
            "id": task.id
        };

        $http.post($scope.rcp_uri, cmd).
            success(function (data, status, headers, config) {
                $scope.addNotification('info', 'INFO', 'Task ' + task.id + ' stopped.');
                $scope.refresh();
            }).
            error(function (data, status, headers, config) {
                $scope.addNotification('error', 'ERROR', 'Could not retrieve tasks.');
            });
    };

    /*
     * Remove task
     */
    $scope.remove = function (task) {
        var cmd = {
            "cmd": "remove",
            "id": task.id
        };

        $http.post($scope.rcp_uri, cmd).
            success(function (data, status, headers, config) {
                $scope.addNotification('info', 'INFO', 'Task ' + task.id + ' removed.');
                $scope.refresh();
            }).
            error(function (data, status, headers, config) {
                $scope.addNotification('error', 'ERROR', 'Could not retrieve tasks.');
            });
    };

    $scope.confirm = function () {
        var i = 0, excludes = [], cmd = {
            "cmd": "create",
            "id": $scope.task_id,
            "src": $scope.task_src,
            "dst": $scope.task_dst,
            "batchsize": $scope.task_batchSize,
            "update": $scope.checkboxModel.update,
            "onlyNewer": $scope.checkboxModel.onlyNewer,
            "recursive": $scope.checkboxModel.recursive,
            "noOrdering": $scope.checkboxModel.noOrdering,
            "throttle": $scope.task_throttle
        };

        if ($scope.task_resumeFrom !== "") {
            cmd.resumeFrom = $scope.task_resumeFrom;
        }

        if ($scope.excludes.length > 0) {
            for (; i < $scope.excludes.length; i++) {
                excludes.push($scope.excludes[i].value);
            }
            cmd.excludes = excludes;
        }

        $http.post($scope.rcp_uri, cmd).
            success(function (data, status, headers, config) {
                $scope.addNotification('info', 'INFO', 'Task created.');
                $scope.closeThisDialog();
            }).
            error(function (data, status, headers, config) {
                $scope.addNotification('error', 'ERROR', 'Could not create the tasks.');
            });
    };


    /*
     * Create task
     */
    $scope.createTask = function () {
        ngDialog.open({template: 'createTaskTemplate', controller: 'MainCtrl'});
    };

    $scope.addExclude = function () {
        $scope.excludes.push({value: ""});
    };

    $scope.removeExclude = function (index) {
        $scope.excludes.splice(index, 1);
    };

    $scope.$on('ngDialog.opened', function (event, $dialog) {
        $dialog.find('.ngdialog-content').css('width', '80%');

    });

    $scope.$on('ngDialog.closed', function (event, $dialog) {
        $scope.refresh();
    });

    $interval(function () {
        if ($scope.checkboxModel.autoRefresh) {
            $scope.refresh();
        }
    }, 5000);
});
