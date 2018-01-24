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

/*global angular: false, window: false */

angular.module('acs-tools-explain-query-app', ['ACS.Tools.notifications', 'acsCoral']).config([

    '$compileProvider',
    function ($compileProvider) {
        $compileProvider.aHrefSanitizationWhitelist(/^\s*(data):/);
    }

]).controller('MainCtrl',
    ['$scope', '$http', 'NotificationsService', function ($scope, $http, NotificationsService) {

        $scope.app = {
            uri: ''
        };

        $scope.queries = {
            slow: [],
            popular: []
        };

        $scope.form = {
            statement: '',
            language: 'JCR-SQL2',
            executionTime: false
        };

        $scope.result = {};

        $scope.load = function (query) {
            $scope.form.language = query.language;
            $scope.form.statement = query.statement;
        };

        $scope.explain = function () {
            NotificationsService.running(true);

            $http({
                method: 'POST',
                url: $scope.app.uri,
                data: 'statement=' + encodeURIComponent($scope.form.statement)
                + '&language=' + encodeURIComponent($scope.form.language)
                + '&executionTime=' + encodeURIComponent($scope.form.executionTime)
                + '&resultCount=' + encodeURIComponent($scope.form.executionTime && $scope.form.resultCount),
                headers: {'Content-Type': 'application/x-www-form-urlencoded'}
            }).
                success(function (data, status, headers, config) {
                    $scope.result = data || {};
                    NotificationsService.running(false);
                    NotificationsService.add('success', 'SUCCESS', 'Review your query explanation');

                }).
                error(function (data, status, headers, config) {
                    NotificationsService.running(false);
                    NotificationsService.add('error', 'ERROR', 'Check your query and try again.');
                });

        };

        $scope.exportAsJSON = function (data) {
            return window.encodeURIComponent(JSON.stringify(data, null, 4).replace(/\\n/gim, ''));
        };

        /*
         * Loads the Slow and Popular Queries
         */
        $scope.init = function () {
            $http({
                method: 'GET',
                url: $scope.app.uri,
                params: {ck: (new Date()).getTime()}
            }).
                success(function (data, status, headers, config) {
                    $scope.queries.slow = data.slowQueries || [];
                    $scope.queries.popular = data.popularQueries || [];
                }).
                error(function (data, status, headers, config) {
                    NotificationsService.add('error', 'ERROR', 'Could not retrieve Slow and Popular queries.');
                });


            NotificationsService.init("Running",
                "Please be patient. Large or expensive queries may cause longer explanation times.");
        };
    }]);

