/*
 * #%L
 * ACS AEM Tools Package - Audit Log Search
 * %%
 * Copyright (C) 2017 Dan Klco
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

angular.module('acs-tools-audit-log-search-app', ['ACS.Tools.notifications', 'acsCoral']).config([
    '$compileProvider',
    function ($compileProvider) {
        $compileProvider.aHrefSanitizationWhitelist(/^\s*((http):|(https):|(data):|(\/))/);
    }

]).controller('MainCtrl',
    ['$scope', '$http', 'NotificationsService', function ($scope, $http, NotificationsService) {

        $scope.app = {
            uri: ''
        };

        $scope.form = {
            contentRoot: '',
            includeChildren: 'true',
            type: '',
            user: '',
            startDate: '',
            endDate: '',
            order: ''
        };

        $scope.result = {};

        $scope.search = function () {
            NotificationsService.running(true);
            $scope.result = {};
            $http({
                method: 'GET',
                url: $scope.app.uri+ '?contentRoot=' + encodeURIComponent($scope.form.contentRoot)
                    + '&children=' + encodeURIComponent($scope.form.includeChildren)
                    + '&type=' + encodeURIComponent($scope.form.type)
                    + '&user=' + encodeURIComponent($scope.form.user)
                    + '&startDate=' + encodeURIComponent($scope.form.startDate)
                    + '&endDate=' + encodeURIComponent($scope.form.endDate)
                    + '&order=' + encodeURIComponent($scope.form.order)
            }).success(function (data, status, headers, config) {
                $scope.result = data || {};
                NotificationsService.running(false);
                NotificationsService.add('success', 'SUCCESS', 'Found '+data.count+' audit events!');

            }).error(function (data, status, headers, config) {
                NotificationsService.running(false);
                NotificationsService.add('error', 'ERROR', 'Unable to search audit logs!');
            });

        };

    }]);

