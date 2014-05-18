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


var clientLibsOptimizerApp = angular.module('clientLibsOptimizerApp',[]);

clientLibsOptimizerApp.controller('MainCtrl', function($scope, $http) {

    $scope.app = {
        uri: '',
        formErrors: {
            categories: false,
            types: false
        }
    };

    $scope.form = {
        categories: '',
        js: true,
        css: true
    };

    $scope.result = {
        erring: false,
        categories: ''
    };

    $scope.$watch('form.categories', function(newValue, oldValue) {
        if(newValue && newValue.indexOf('"') >= 0) {
            $scope.form.categories = $scope.form.categories.replace(/\"/g, '');
        }
    });


    $scope.optimize = function() {
        if(!$scope.validate()) {
            $scope.result = {};
            return;
        }

        $http({
            method: 'GET',
            url: $scope.app.uri,
            params: $scope.form
        }).
        success(function(data, status, headers, config) {
            $scope.result.categories = data.categories.join() || '';
            $scope.result.erring = false;
        }).
        error(function(data, status, headers, config) {
            $scope.result.erring = true;
        });

        $scope.app.formErrors = {
            categories: false,
            types: false
        };
    };

    /* Validators */

    $scope.validate = function() {
       var validCategories = $scope.validateCategories(),
           validTypes = $scope.validateTypes();

        return validCategories && validTypes;
    };

    $scope.validateCategories = function() {
        /* Categories */
        if(!$scope.form.categories || $scope.form.categories.length === 0) {
            // Categories are not valid
            $scope.app.formErrors.categories = true;
        } else {
            // Categories are valid
            $scope.app.formErrors.categories = false;
        }

        return !$scope.app.formErrors.categories;
    };

    $scope.validateTypes = function() {
        /* Types */
        if(!$scope.form.css && !$scope.form.js) {
            $scope.app.formErrors.types = true;
        } else {
            $scope.app.formErrors.types = false;
        }

        return !$scope.app.formErrors.types;
    };
});
