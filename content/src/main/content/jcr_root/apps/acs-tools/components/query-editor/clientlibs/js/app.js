/*global angular: false, ace: false */

(function() {

    'use strict';

    ace.require("ace/ext/language_tools");

    angular.module('qeApp', [
        'ui.ace',
        'qeControllers',
        'qeServices',
        'qeDirectives'
    ]);

}());