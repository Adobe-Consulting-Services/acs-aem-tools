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

/*global angular,CUI: false */

angular
.module('acs-tools-vlt-rcp-app', ['ngAnimate','acsCoral', 'ACS.Tools.notifications'])
.controller('MainCtrl', ['$scope', '$http', '$timeout', '$interval', 'NotificationsService',
function ($scope, $http, $timeout, $interval, NotificationsService) {

    $scope.rcp_uris = ['/system/jackrabbit/filevault/rcp'];

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

    $scope.taskExpandedStatuses = [];
    
    /** false in case the scope refers to an existing task, otherwise true */
    $scope.isNew = true; 

   /**
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

    /**
     * Refresh tasks
     */
    $scope.refresh = function () {
        $http.get($scope.app.uri,
            {
                params: {ck: (new Date()).getTime()}
            }
        )
        .success(function (data, status, headers, config) {
            $scope.tasks = data.tasks || [];
        })
        .error(function (data, status, headers, config) {
            NotificationsService.add('error', 'ERROR', 'Could not refresh tasks');
        });
    };

    $scope.editCredentials = function (task) {
        $scope.task_id = task.id;
        $scope.task_src_username = "";
        $scope.task_src_password = "";
    };

    /**
     * Set the scope values from a task
     * @param {*} task The task to edit
     */
    $scope.edit = function (task) {
        $scope.task_id = task.id;
        $scope.task_src = task.src;
        $scope.task_dst = task.dst;
        $scope.task_batchSize = task.batchsize;
        $scope.task_throttle = task.throttle;
        $scope.checkboxModel = {
            recursive: task.recursive,
            update: task.update,
            onlyNewer: task.onlyNewer,
            noOrdering: task.noOrdering,
            autoRefresh: false,
            useSystemProperties: task.useSystemProperties,
            allowSelfSignedCertificate: task.allowSelfSignedCertificate,
            disableHostnameVerification: task.disableHostnameVerification
        };

        if (task.excludes) {
            $scope.excludes = task.excludes.map(function(exclude){
                return {value: exclude};
            });
        } else if (task.filter) {
            $scope.filter = task.filter;
        }

        if (task.resumeFrom) {
            $scope.task_resumeFrom = task.resumeFrom;
        }
        $scope.isNew = false;
    };


    /**
     * Set the scope values from a task
     * Used for the duplicate functionality
     * @param {*} task The task to duplicate
     */
    $scope.duplicate = function (task) {
        $scope.edit(task);
        // different task id
        $scope.task_id = task.id + "-copy"; // unique
        $scope.isNew = true;
    };

   /**
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
                NotificationsService.add('error', 'ERROR', 'Error while starting task: '+task.id+'. Please check logs');
            });
    };

   /**
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

   /**
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

    /**
     * Create new task
     */
    $scope.create = function () {
        var i = 0,
            cmd = {
                "cmd": "create",
                "id": $scope.task_id,
                "src": $scope.task_src,
                "srcCreds": $scope.task_src_username + ":" + $scope.task_src_password,
                "dst": $scope.task_dst,
                "batchsize": $scope.task_batchSize || 1024,
                "update": $scope.checkboxModel.update,
                "onlyNewer": $scope.checkboxModel.onlyNewer,
                "recursive": $scope.checkboxModel.recursive,
                "noOrdering": $scope.checkboxModel.noOrdering,
                "useSystemProperties": $scope.checkboxModel.useSystemProperties,
                "allowSelfSignedCertificate": $scope.checkboxModel.allowSelfSignedCertificate,
                "disableHostnameVerification": $scope.checkboxModel.disableHostnameVerification,
                "throttle": $scope.task_throttle || 0
            };
        if ($scope.task_resumeFrom !== "") {
            cmd.resumeFrom = $scope.task_resumeFrom;
        }

        if ($scope.excludes.length > 0) {
            cmd.excludes = $scope.excludes.map(function(exclude){
                return exclude.value;
            });
        } else if ($scope.filter && $scope.filter.length > 0) {
            cmd.filter = $scope.filter;
        }
        $http
        .post($scope.app.uri, cmd)
        .success(function (data, status, headers, config) {
            NotificationsService.add('info', 'INFO', 'Task created.');

            $scope.refresh();
            angular.element('#create-new-task-modal').modal('hide');
            $scope.reset();
        })
        .error(function (data, status, headers, config) {
            NotificationsService.add('error', 'ERROR', data.message);
        });
    };

    /**
     * Create new task
     */
    $scope.save = function () {
        var i = 0,
            cmd = {
                "cmd": "edit",
                "id": $scope.task_id,
                "src": $scope.task_src,
                "srcCreds": $scope.task_src_username + ":" + $scope.task_src_password,
                "dst": $scope.task_dst,
                "batchsize": $scope.task_batchSize || 1024,
                "update": $scope.checkboxModel.update,
                "onlyNewer": $scope.checkboxModel.onlyNewer,
                "recursive": $scope.checkboxModel.recursive,
                "noOrdering": $scope.checkboxModel.noOrdering,
                "useSystemProperties": $scope.checkboxModel.useSystemProperties,
                "allowSelfSignedCertificate": $scope.checkboxModel.allowSelfSignedCertificate,
                "disableHostnameVerification": $scope.checkboxModel.disableHostnameVerification,
                "throttle": $scope.task_throttle || 0
            };
        if ($scope.task_resumeFrom !== "") {
            cmd.resumeFrom = $scope.task_resumeFrom;
        }

        if ($scope.excludes.length > 0) {
            cmd.excludes = $scope.excludes.map(function(exclude){
                return exclude.value;
            });
        } else if ($scope.filter && $scope.filter.length > 0) {
            cmd.filter = $scope.filter;
        }
        $http
        .post($scope.app.uri, cmd)
        .success(function (data, status, headers, config) {
            NotificationsService.add('info', 'INFO', 'Task modified.');

            $scope.refresh();
            angular.element('#create-new-task-modal').modal('hide');
            $scope.reset();
        })
        .error(function (data, status, headers, config) {
            NotificationsService.add('error', 'ERROR', data.message);
        });
    };

    /**
     * Set credentials task
     */
     $scope.set_credentials = function () {
         var cmd = {
             "cmd": "set-credentials",
             "id": $scope.task_id,
             "srcCreds": $scope.task_src_username + ":" + $scope.task_src_password
         };

         $http.post($scope.app.uri, cmd).
             success(function (data, status, headers, config) {
                 NotificationsService.add('info', 'INFO', 'Set credentials for task ' + $scope.task_id + '.');
                 $scope.refresh();
             }).
             error(function (data, status, headers, config) {
                 NotificationsService.add('error', 'ERROR', 'Could not set credentials for task.');
             });
     };

    $scope.reset = function() {
        $scope.task_id = '';
        $scope.task_src = 'http://localhost:4502/crx/server/crx.default/jcr:root/content/dam/my-site';
        $scope.task_src_username = '';
        $scope.task_src_password = '';
        $scope.task_dst = '/content/dam/my-site';
        $scope.task_batchSize = '1024';
        $scope.task_throttle = '';
        $scope.checkboxModel = {
            recursive: false,
            update: false,
            onlyNewer: false,
            noOrdering: false,
            autoRefresh: false,
            useSystemProperties: false,
            allowSelfSignedCertificate: false,
            disableHostnameVerification: false
        };
        $scope.excludes = [];
        $scope.filter = '';
        $scope.isNew = true;
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
})
.filter('truncate', function() {
    return function(input, limit) {
        input = input || '';

        if(input.length < limit) {
            return input;
        } else {
            var half = limit / 2,
                left = input.substring(0, half),
                right = input.substring(input.length - half, input.length);
            return left + '...' + right; 
        }
    };
});
