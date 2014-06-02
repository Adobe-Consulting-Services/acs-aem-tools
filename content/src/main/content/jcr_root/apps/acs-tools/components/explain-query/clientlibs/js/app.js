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

/*global angular: false */


var explainQueryApp = angular.module('explainQueryApp',[]);

explainQueryApp.controller('MainCtrl', function($scope, $http, $timeout) {

    $scope.app = {
        uri: ''
    };

    $scope.notifications = [];

    $scope.queries = {
        slow: [],
        popular: []
    };

    $scope.form = {
        statement: '',
        language: ''
    };

    $scope.result = {
        statement: '',
        language: '',
        plan: ''
    };

    $scope.load = function(query) {
        $scope.form.language = query.language;
        $scope.form.statement = query.statement;
    };

    $scope.explain = function() {

        $http({
            method: 'POST',
            url: $scope.app.uri,
            data: 'statement=' + encodeURIComponent($scope.form.statement) + '&language=' + encodeURIComponent($scope.form.language),
            headers: {'Content-Type': 'application/x-www-form-urlencoded'}
        }).
        success(function(data, status, headers, config) {
            $scope.result = data || {};
            $scope.addNotification('success', 'Success', 'Review your query explanation');

        }).
        error(function(data, status, headers, config) {
            $scope.addNotification('error', 'Error', 'Check your query and try again.');
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

    /*
    * Init
    *
    * Loads the Slow and Popular Queries
    */
    $scope.init = function() {
        $http({
            method: 'GET',
            url: $scope.app.uri,
            params: { ck: (new Date()).getTime() }
        }).
        success(function(data, status, headers, config) {
            $scope.queries.slow = data.slowQueries || [];
            $scope.queries.popular = data.popularQueries || [];
        }).
        error(function(data, status, headers, config) {
            $scope.addNotification('error', 'Error', 'Could not retrieve Slow and Popular Queries.');
        });
    };
});
