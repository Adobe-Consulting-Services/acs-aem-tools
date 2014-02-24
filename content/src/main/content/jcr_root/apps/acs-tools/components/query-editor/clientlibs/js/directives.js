/*global angular: false, ace: false */

(function () {

    'use strict';

    var module = angular.module('qeDirectives', []);

    module.directive('uiAceStatusbar', function () {
        return  {
            transclude: true,
            template: '<div class="status" ng-transclude></div>',
            link: function (scope, elm, attrs) {
                var StatusBar = ace.require('ace/ext/statusbar').StatusBar,
                    aceEditor = angular.element(attrs.uiAceStatusbar),
                    statusbar = new StatusBar(ace.edit(aceEditor[0]), elm[0]);
            }
        };
    });

}());