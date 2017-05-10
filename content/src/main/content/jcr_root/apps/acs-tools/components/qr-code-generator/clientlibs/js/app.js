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

angular.module('acs-tools-qr-code-generator-app', ['acsCoral', 'ACS.Tools.notifications']).controller('MainCtrl', ['$scope', '$http', '$timeout', 'NotificationsService', function ($scope, $http, $timeout, NotificationsService) {

    $scope.app = {
        uri: ''
    };

    $scope.form = {
        bucketType: 'sling:Folder',
        properties: [
            {
                name: '',
                value: '',
                multi: false
            }
            ]
    };

    $scope.results = {};

    $scope.addProperty = function (properties) {
        properties.push({
            name: '',
            value: ''
        });
    };

    $scope.removeProperty = function (properties, index) {
        properties.splice(index, 1);
    };

    $scope.saveConfig = function (isValid) {

        if (!isValid) {
            NotificationsService.add('error', "Error", "Form is invalid. Please correct and resubmit.");
            return;
        }

        $scope.results = {};
        $scope.app.running = true;
        NotificationsService.running($scope.app.running);
        $(".coral-Button").attr('disabled');
        $http({
            method: 'POST',
            url: $scope.app.uri,
            data: 'config=' + encodeURIComponent(JSON.stringify($scope.form)),
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            }
        }).
        success(function (data, status, headers, config) {
            $scope.results = data || {};
            $scope.app.running = false;
            NotificationsService.add('success', "Success", "Your Configurations has been saved");
            NotificationsService.running($scope.app.running);

            // Feature is enabled in configurations, so removing disabled attribute
            $(".coral-Button").removeAttr('disabled');
        }).
        error(function (data, status, headers, config) {
            NotificationsService.add('error', 'ERROR', data.title + '. ' + data.message);
            $scope.app.running = false;
            NotificationsService.running($scope.app.running);
        });
    };
    }]).directive('qrCodeConfig', function ($http, NotificationsService) {
    return {
        restrict: 'A',
        link: function ($scope, $elem, attrs) {

            // Fetch previous saved configurations
            $http({
                method: 'GET',
                url: qrCode.default_url,
                headers: {
                    'Accept': '*/*',
                    'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'
                },
            }).success(function (data, status, headers, config) {
                parsedResponse = JSON.parse(data.config);
                $scope.form.properties = parsedResponse.properties;
                $scope.form.enable = parsedResponse.enable;

            }).error(function (data, status, headers, config) {
                // Response code 404 will be when configs are not available
                if (status !== 404) {
                    NotificationsService.add('error', "Error", "Something went wrong while fetching previous configurations");
                }
            });

        }
    }
});