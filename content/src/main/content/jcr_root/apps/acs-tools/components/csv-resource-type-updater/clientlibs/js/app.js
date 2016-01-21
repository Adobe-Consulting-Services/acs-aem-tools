/*
 * #%L
 * ACS AEM Tools Bundle
 * %%
 * Copyright (C) 2015 Adobe
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

/*global angular: false, Upload: false */

angular.module('acs-tools-csv-resource-type-updater-app', ['acsCoral', 'ngFileUpload', 'ACS.Tools.notifications']).controller('MainCtrl',
    ['$scope', '$http', '$timeout', 'Upload', 'NotificationsService', function ($scope, $http, $timeout, Upload, NotificationsService) {

        $scope.app = {
            uri: ''
        };

        $scope.form = {
            path: '',
            propertyName: 'sling:resourceType'
        };

        $scope.result = {
            processed: false,
            success: [],
            failure: []
        };

        $scope.update = function () {
            if (NotificationsService.isRunning()) { return; }

            NotificationsService.running(true);

            $scope.result = {
                success: [],
                failure: [],
                processed: false
            };

            Upload.upload({
                url: $scope.app.uri + '.update.json',
                fields: {
                    'path': $scope.form.path || '/content',
                    'propertyName': $scope.form.propertyName || 'sling:resourceType'
                },
                file: $scope.files[0]
            }).success(function (data, status, headers, config) {
                $scope.result = data;
                $scope.result.processed = true;

                if ($scope.result.success && $scope.result.success.length > 0) {
                    NotificationsService.add('success',
                        'Success', (data.message || ( $scope.result.success.length + ' resources were updated' )));
                }

                if ($scope.result.failure && $scope.result.failure.length > 0) {
                    NotificationsService.add('error',
                        'Error', (data.message || ( $scope.result.failure.length + ' resources were unable' +
                        ' to be updated' )));
                }


                if ((!$scope.result.success || $scope.result.success.length === 0)
                        && (!$scope.result.failure && $scope.result.failure.length === 0)) {

                    NotificationsService.add('notice',
                        'Notice', ('No matching resources could be found'));
                }

                NotificationsService.running(false);

            }).error(function (data, status, headers, config) {
                NotificationsService.add('error',
                    'Error ' + (data.message || 'An unknown error occurred' ));

                NotificationsService.running(false);
            });
        };
    }]);

