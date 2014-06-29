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


angular.module('testPageGeneratorApp',[]).controller('MainCtrl', function($scope, $http, $timeout) {

    $scope.app = {
        uri: ''
    };

    $scope.notifications = [];

    $scope.form = {
        properties: [
            { name: '', value: '', multi: false }
        ]
    };

    $scope.results = {};

    $scope.addProperty = function(properties) {
        properties.push( { name:'', value:'' } );
    };

    $scope.removeProperty = function(properties, index) {
        properties.splice(index, 1);
    };

    $scope.generatePages = function() {
        $scope.results = {};
        $scope.app.running = true;

        $http({
            method: 'POST',
            url: $scope.app.uri,
            data: 'json=' + encodeURIComponent(JSON.stringify($scope.form)),
            headers: {'Content-Type': 'application/x-www-form-urlencoded'}
        }).
        success(function(data, status, headers, config) {
            $scope.results = data || {};
            $scope.app.running = false;
        }).
        error(function(data, status, headers, config) {
            $scope.addNotification('error', 'ERROR', 'Check your params and your error logs and try again.');
            $scope.app.running = false;
        });
    };

    $scope.addNotification = function (type, title, message) {
        var timeout = 10000;

        if(type === 'success')  {
            timeout = timeout / 2;
        }

        $scope.notifications.unshift({
            type: type,
            title: title,
            message: message
        });

        $timeout(function() {
            $scope.notifications.shift();
        }, timeout);
    };
});
