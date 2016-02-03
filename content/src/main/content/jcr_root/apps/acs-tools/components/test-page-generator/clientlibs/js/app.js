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

/*global JSON: false, angular: false */

angular.module('acs-tools-test-page-generator-app', ['acsCoral','ACS.Tools.notifications']).controller('MainCtrl',
    ['$scope', '$http', '$timeout', 'NotificationsService', function ($scope, $http, $timeout, NotificationsService) {

        NotificationsService.init(
            "Creating pages",
            "Please be patient while the system creates your pages; " +
            "depending on the total number of pages requested to be created this process could take a long time.");

        $scope.app = {
            uri: ''
        };

        $scope.form = {
            bucketType: 'sling:Folder',
            properties: [
                {name: '', value: '', multi: false}
            ]
        };

        $scope.results = {};

        $scope.addProperty = function (properties) {
            properties.push({name: '', value: ''});
        };

        $scope.removeProperty = function (properties, index) {
            properties.splice(index, 1);
        };

        $scope.generatePages = function (isValid) {

            if (!isValid) {
                NotificationsService.add('error', "Error", "Form is invalid. Please correct and resubmit.");
                return;
            }

            $scope.results = {};
            $scope.app.running = true;
            NotificationsService.running($scope.app.running);

            $http({
                method: 'POST',
                url: $scope.app.uri,
                data: 'json=' + encodeURIComponent(JSON.stringify($scope.form)),
                headers: {'Content-Type': 'application/x-www-form-urlencoded'}
            }).
                success(function (data, status, headers, config) {
                    $scope.results = data || {};
                    $scope.app.running = false;
                    NotificationsService.running($scope.app.running);
                }).
                error(function (data, status, headers, config) {
                    NotificationsService.add('error', 'ERROR', data.title + '. ' + data.message);
                    $scope.app.running = false;
                    NotificationsService.running($scope.app.running);
                });
        };
    }]);