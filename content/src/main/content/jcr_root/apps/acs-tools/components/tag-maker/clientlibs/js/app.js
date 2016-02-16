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

angular.module('acs-tools-tag-maker-app', ['ngFileUpload', 'acsCoral', 'ACS.Tools.notifications']).controller('MainCtrl',
    ['$scope', '$http', '$timeout', 'Upload', 'NotificationsService', function ($scope, $http, $timeout, Upload, NotificationsService) {

        $scope.app = {
            uri: ''
        };

        $scope.form = {
            locale: '',
            charset: '',
            clean: 'true',
            converter: 'default',
            fallbackConverter: 'acs-commons-none',
            separator: ''
        };

        $scope.result = {
            message: '',
            tagIds: []
        };

        $scope.converters = [];
        $scope.fallbackConverters = [];

        $scope.init = function () {
            $http.get($scope.app.uri + '.init.json').
                success(function (data, status, headers, config) {
                    angular.copy(data, $scope.converters);
                    angular.copy(data, $scope.fallbackConverters);

                    $scope.fallbackConverters.push({
                        label: "None",
                        value: "acs-commons-none"
                    });

                }).
                error(function (data, status, headers, config) {
                    NotificationsService.add('error',
                        'Error', 'Could not initialize Tag Maker');
                });
        };

        $scope.makeTags = function () {
            $scope.result = {
                message: '',
                tagIds: []
            };

            NotificationsService.add('info',
                        'Info', 'Import initiated.');
            NotificationsService.running(true);

            Upload.upload({
                url: $scope.app.uri + '.make-tags.json',
                fields: {
                    'charset': $scope.form.charset || '',
                    'clean': $scope.form.clean || 'false',
                    'converter': $scope.form.converter || 'default',
                    'fallbackConverter': $scope.form.fallbackConverter || 'default',
                    'delimiter': $scope.form.delimiter || '',
                    'separator': $scope.form.separator || ''
                },
                file: $scope.files[0]
            }).success(function (data, status, headers, config) {
                NotificationsService.running(false);
                $scope.result = data;
                if ($scope.result.tagIds.length === 0) {
                    NotificationsService.add('notice',
                        'Warning', 'Could not find any tag entries that can be processed by the selected converter');
                } else {
                    NotificationsService.add('success',
                        'Success', (data.message || 'Your tags have been created' ));
                }
            }).error(function (data, status, headers, config) {
                NotificationsService.running(false);
                NotificationsService.add('error',
                    'Error ' + (data.message || 'An unknown error occurred' ));
            });
        };
    }]);

