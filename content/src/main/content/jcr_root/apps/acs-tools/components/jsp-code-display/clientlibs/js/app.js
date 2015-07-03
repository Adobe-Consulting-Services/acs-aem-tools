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

var jspCodeDisplay = angular.module('acs-tools-jsp-code-display-app', ['ACS.Tools.notifications']);

jspCodeDisplay.controller('MainCtrl', ['$scope', '$http', 'NotificationsService', function($scope, $http, NotificationsService) {

        $scope.app = {
            uri: ''
        };

        $scope.line = '';

    $scope.debug = function() {

        jspCodeDisplay.editor.setValue('');

        NotificationsService.running(true);

        $http({
                method: 'POST',
                url: $scope.app.uri,
                data: $.param({ line : $scope.line }),
                headers: {'Content-Type': 'application/x-www-form-urlencoded'
            }
        }).success(function(data, status, headers, config) {
            if (data.success) {

                jspCodeDisplay.editor.setValue(data.code);
                jspCodeDisplay.editor.gotoLine(data.lineNumber, 0, true);

                NotificationsService.add('success', 'Success', 'The line was identified');

            } else {
                NotificationsService.add('error', 'Error', data.error);
            }

            NotificationsService.running(false);

        }).error(function(data, status, headers, config) {
            NotificationsService.add('error', 'Error', status);
        });
    };
}]);


(function(){
    var editor = ace.edit("editor");

    ace.config.set("basePath", '/etc/clientlibs/acs-tools/vendor/aceeditor');

    editor.setTheme("ace/theme/vibrant_ink");
    editor.getSession().setMode("ace/mode/java");
    editor.setReadOnly(true);

    jspCodeDisplay.editor = editor;
}());


$(function() {
    function resizeEditor() {
        var $editor = $('#editor'),
            buffer = 45;
        $editor.css('height', (window.innerHeight - $editor.offset().top - buffer) + 'px');
        jspCodeDisplay.editor.resize();
    }

    $(window).resize(function() {
        resizeEditor();
    });

    resizeEditor();
});