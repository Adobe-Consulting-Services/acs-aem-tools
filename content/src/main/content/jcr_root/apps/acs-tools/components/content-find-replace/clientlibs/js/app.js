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

/*global angular: false, ace: false */

var jspCodeDisplay = angular.module('contentFindReplace', []);


jspCodeDisplay.controller('MainCtrl', function($scope, $http) {
    $scope.search_path = '';
    $scope.update_references ='dryrun';
    $scope.search_type ='static';
    $scope.running = false;
    $scope.error = false;
    $scope.errorMessage = '';
    $scope.success = false;
    $scope.successMessage = '';

    $scope.submitSNP = function() {
        $scope.error = false;
        $scope.running = true;
        if($scope.search_path=="" || $scope.search_component == null || $scope.search_string == null || $scope.search_element == null ){
			$scope.error = true;
            $scope.errorMessage = "All fields are mandatory";
        }else{
        $http({
            method: 'POST',
            url: $('body').data("post-url"),
            data: $.param({ search_element : $scope.search_element,
                            search_path : $scope.search_path,
                          	search_string : $scope.search_string,
							replace_string : $scope.replace_string,
							search_component : $scope.search_component,
							search_type : $scope.search_type,
                           update_references: $scope.update_references
                          }),
            headers: {'Content-Type': 'application/x-www-form-urlencoded'}
        }).success(function(data, status, headers, config) {
            if (data.success) {
                $scope.success = true;
                $scope.successMessage = data.successMessage;

            } else {
                $scope.error = true;
                $scope.success = false;
                $scope.errorMessage = data.error;
            }
            $scope.running = false;
        }).error(function(data, status, headers, config) {
            $scope.error = true;
            $scope.errorMessage = status;
        });
    	}
    };
});

