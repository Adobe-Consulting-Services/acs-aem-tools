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

var jspCodeDisplay = angular.module('jspCodeDisplay', []);

(function(){
    var editor = ace.edit("editor");

    editor.setTheme("ace/theme/vibrant_ink");
    editor.getSession().setMode("ace/mode/java");
    editor.setReadOnly(true);
    
    jspCodeDisplay.editor = editor;
}());


jspCodeDisplay.controller('MainCtrl', function($scope, $http) {
    $scope.line = '';
    $scope.running = false;
    $scope.error = false;
    $scope.errorMessage = '';
    
    $scope.submitLine = function() {
        $scope.error = false;
        jspCodeDisplay.editor.setValue('');
        $scope.running = true;
        $http({
            method: 'POST',
            url: $('body').data("post-url"),
            data: $.param({ line : $scope.line }),
            headers: {'Content-Type': 'application/x-www-form-urlencoded'}
        }).success(function(data, status, headers, config) {
            if (data.success) {
                jspCodeDisplay.editor.setValue(data.code);
                jspCodeDisplay.editor.gotoLine(data.lineNumber, 0, true);
            } else {
                $scope.error = true;
                $scope.errorMessage = data.error;
            }
            $scope.running = false;
        }).error(function(data, status, headers, config) {
            $scope.error = true;
            $scope.errorMessage = status;
        });
    };
});