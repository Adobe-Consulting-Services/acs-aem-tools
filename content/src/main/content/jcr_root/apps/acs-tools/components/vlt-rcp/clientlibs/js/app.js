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

angular
.module('acs-tools-vlt-rcp-app', ['acsCoral', 'ACS.Tools.notifications'])
.controller('MainCtrl', ['$scope', '$http', '$timeout', '$interval', 'NotificationsService',
function ($scope, $http, $timeout, $interval, NotificationsService) {

    $scope.rcp_uris = ['/system/jackrabbit/filevault/rcp', '/libs/granite/packaging/rcp'];

    $scope.task_src = 'http://localhost:4502/crx/server/crx.default/jcr:root/content/dam/my-site';

    $scope.task_dst = '/content/dam/my-site';

    $scope.task_batchSize = '1024';

    $scope.task_throttle = '';

    $scope.checkboxModel = {
        recursive: false,
        update: false,
        onlyNewer: false,
        noOrdering: false,
        autoRefresh: false
    };

    $scope.app = {
        uri: '/system/jackrabbit/filevault/rcp',
        running: false
    };

    $scope.tasks = [];

    $scope.excludes = [];

    $scope.vltMissing = true;

    /*
    * Loads the tasks
    */
    $scope.init = function (rcpUris) {

        $scope.rcp_uris = rcpUris || $scope.rcp_uris;

        angular.forEach($scope.rcp_uris, function (uri) {

            $http.get(uri,
                {
                    params: {ck: (new Date()).getTime()}
                }
            ).success(function (data, status, headers, config) {
                    if (status === 200) {
                        if ($scope.vltMissing) {
                            // Only set the app.uri if a valid end point has not been found
                            $scope.app.uri = uri;
                            $scope.vltMissing = false;
                            $scope.tasks = data.tasks || [];
                        }
                    }
                });
        });
    };

    $scope.refresh = function () {
        $http.get($scope.app.uri,
            {
                params: {ck: (new Date()).getTime()}
            }
        ).
            success(function (data, status, headers, config) {
                $scope.tasks = data.tasks || [];
            })
            .error(function (data, status, headers, config) {
                NotificationsService.add('error', 'ERROR', 'Could not refresh tasks');
            });
    };

    /*
    * Start task
    */
    $scope.start = function (task) {
        var cmd = {
            "cmd": "start",
            "id": task.id
        };

        $http.post($scope.app.uri, cmd).
            success(function (data, status, headers, config) {
                NotificationsService.add('info', 'INFO', 'Task ' + task.id + ' started.');
                $scope.refresh();
            }).
            error(function (data, status, headers, config) {
                NotificationsService.add('error', 'ERROR', 'Could not retrieve tasks');
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

        $http.post($scope.app.uri, cmd).
            success(function (data, status, headers, config) {
                NotificationsService.add('info', 'INFO', 'Task ' + task.id + ' stopped.');
                $scope.refresh();
            }).
            error(function (data, status, headers, config) {
                NotificationsService.add('error', 'ERROR', 'Could not retrieve tasks');
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

        $http.post($scope.app.uri, cmd).
            success(function (data, status, headers, config) {
                NotificationsService.add('info', 'INFO', 'Task ' + task.id + ' removed.');
                $scope.refresh();
            }).
            error(function (data, status, headers, config) {
                NotificationsService.add('error', 'ERROR', 'Could not retrieve tasks');
            });
    };

    $scope.create = function () {
        var i = 0,
            excludes = [],
            cmd = {
                "cmd": "create",
                "id": $scope.task_id,
                "src": $scope.task_src,
                "srcCreds": $scope.task_src_credentials,
                "dst": $scope.task_dst,
                "batchsize": $scope.task_batchSize || 1024,
                "update": $scope.checkboxModel.update,
                "onlyNewer": $scope.checkboxModel.onlyNewer,
                "recursive": $scope.checkboxModel.recursive,
                "noOrdering": $scope.checkboxModel.noOrdering,
                "throttle": $scope.task_throttle || 0
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

        $http.post($scope.app.uri, cmd).
            success(function (data, status, headers, config) {
                NotificationsService.add('info', 'INFO', 'Task created.');

                $scope.refresh();
                angular.element('#create-new-task-modal').modal('hide');
                $scope.reset();
            }).
            error(function (data, status, headers, config) {
                NotificationsService.add('error', 'ERROR', data.message);
            });
    };


    $scope.reset = function() {
        var taskSrc = 'http://localhost:4502/crx/server/crx.default/jcr:root/content/dam/my-site',
            taskDst = '/content/dam/my-site',
            taskBatchSize = '1024',
            taskThrottle = '',
            checkboxModel = {
                recursive: false,
                update: false,
                onlyNewer: false,
                noOrdering: false,
                autoRefresh: false
            };

        $scope.task_id = '';
        $scope.task_src = taskSrc;
        $scope.task_dst = taskDst;
        $scope.task_batchSize = taskBatchSize;
        $scope.task_throttle = taskThrottle;
        $scope.checkboxModel = checkboxModel;
        $scope.excludes = [];
    };

    $scope.addExclude = function () {
        $scope.excludes.push({value: ""});
    };

    $scope.removeExclude = function (index) {
        $scope.excludes.splice(index, 1);
    };

    $interval(function () {
        if ($scope.checkboxModel.autoRefresh) {
            $scope.refresh();
        }
    }, 5000);

}])
.filter('removeCredentials', function() {
    return function(input) {
        /*
        The regex causes jslint error
        You can 'fix' the warning by telling JSLint to ignore it: add regexp: true to your JSLint settings at the top of the file.
        http://stackoverflow.com/questions/10793814/how-to-rectify-insecure-error-in-jslint
        */
        var segments = input.match(/(\bhttps?:\/\/[\S]+:)([\S]+)(@\S*)/);
        if (segments && segments.length === 4) {
        return segments[1] + '******' + segments[3];
        } else {
            return input;
        }
        /* jshint ignore:end */
    };
});
