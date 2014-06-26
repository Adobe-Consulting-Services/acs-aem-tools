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

var findAndReplace = angular.module('findAndReplace', []);



findAndReplace.controller('MainCtrl', function($scope, $http, $timeout) {
    $scope.searchPath = '';
    $scope.updateReferences ='dryrun';
    $scope.running = false;
    $scope.error = false;
    $scope.errorMessage = '';
    $scope.success = false;
    $scope.successMessage = '';
	 $scope.notifications = [];

  $scope.addNotification = function (type, title, message) {
        var timeout = 100000;

        if(type === 'success') {
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

    $scope.submitSNP = function() {
        $scope.error = false;
        $scope.running = true;
        if(	!$scope.searchPath  || !$scope.searchComponent  ||
        !$scope.searchString || !$scope.searchElement
        ){
			$scope.error = true;
            $scope.success = false;
            $scope.errorMessage = "All fields are mandatory";
            $scope.running = false;
            $scope.addNotification('error', 'ERROR', 'All fields are mandatory.');
        }else{
        $http({
            method: 'POST',
            url: $('body').data("post-url"),
            data: $.param({ search_element : $scope.searchElement,
                            search_path : $scope.searchPath,
                            search_string : $scope.searchString,
							replace_string : $scope.replaceString,
							search_component : $scope.searchComponent,
                           update_references: $scope.updateReferences
                          }),
            headers: {'Content-Type': 'application/x-www-form-urlencoded'}
        }).success(function(data, status, headers, config) {
            if (data.success) {
                $scope.success = true;
                $scope.successMessage = data.successMessage;
                $scope.addNotification('success', 'SUCCESS', data.successMessage);

            } else {
                $scope.error = true;
                $scope.success = false;
                $scope.errorMessage = data.error;
                $scope.addNotification('error', 'ERROR', data.error);

            }
            $scope.running = false;
        }).error(function(data, status, headers, config) {
            $scope.error = true;
            $scope.errorMessage = status;
        });
        }
    };
});

