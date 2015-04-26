/*
 * #%L
 * ACS AEM Tools Package
 * %%
 * Copyright (C) 2013 Adobe
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

/*global angular: false, Upload: false */


var tagMakerApp = angular.module('tagMakerApp', ['ngFileUpload']);

tagMakerApp.controller('MainCtrl', ['$scope', '$http', '$timeout', 'Upload', function ($scope, $http, $timeout, Upload) {

    $scope.app = {
        uri: ''
    };

    $scope.form = {
        locale: '',
        charset: '',
        clean: 'true',
        converter: 'default',
        separator: ''
    };

    $scope.result = {
        message: '',
        tagIds: []
    };

    $scope.converters = [];

    $scope.notifications = [];

    $scope.init = function () {
        $http.get($scope.app.uri + '.init.json').
            success(function (data, status, headers, config) {
                $scope.converters = data;
            }).
            error(function (data, status, headers, config) {
                $scope.addNotification('error',
                    'Error! Could not initialize Tag Maker');
            });
    };

    $scope.makeTags = function () {
        $scope.result = {
            message: '',
            tagIds: []
        };

        Upload.upload({
            url: $scope.app.uri + '.make-tags.json',
            fields: {
                'charset': $scope.form.charset || '',
                'clean': $scope.form.clean || 'false',
                'converter': $scope.form.converter || 'default',
                'delimiter': $scope.form.delimiter || '',
                'separator': $scope.form.separator || ''
            },
            file: $scope.files[0]
        }).success(function (data, status, headers, config) {
            $scope.result = data;
            if($scope.result.tagIds.length === 0) {
                $scope.addNotification('notice',
                    'Warning! Could not find any tag entries that can be processed by the selected converter');
            } else {
                $scope.addNotification('success',
                    'Success! ' + (data.message || 'Your tags have been created' ));
            }
        }).error(function (data, status, headers, config) {
            $scope.addNotification('error',
                'Error! ' + (data.message || 'An unknown error occurred' ));
        });
    };

    $scope.addNotification = function (type, title, message) {
        var timeout = 30000;

        if (type === 'success') {
            timeout = timeout / 2;
        }

        $scope.notifications.push({
            type: type,
            title: title,
            message: message
        });

        $timeout(function () {
            $scope.notifications.shift();
        }, timeout);
    };
}]);

