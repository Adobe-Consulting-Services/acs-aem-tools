/*global angular: false, ace: false */

(function () {

    'use strict';

    var module = angular.module('qeServices', []);

    module.factory('Crx', ['$http',
        function ($http) {
            return {
                query: function(params) {
                    return $http.get('/bin/querybuilder.json', {
                        params: params
                    });
                },
                nodetypes: function() {
                    return $http.get('/crx/de/nodetypes.jsp');
                },
                filesearch: function(name) {
                    return $http.get('/crx/de/filesearch.jsp', {
                        params: { name: name }
                    });
                }
            };
        }
    ]);

    // debounce from https://github.com/angular/angular.js/issues/2690
    module.factory('debounce', ['$timeout',
        function ($timeout) {
            return function (fn, timeout, apply) { // debounce fn
                timeout = angular.isUndefined(timeout) ? 0 : timeout;
                apply = angular.isUndefined(apply) ? true : apply; // !!default is true! most suitable to my experience
                var nthCall = 0;
                return function () { // intercepting fn
                    var that = this,
                        argz = arguments,
                        later;
                    nthCall++;
                    later = (function (version) {
                        return function () {
                            if (version === nthCall) {
                                return fn.apply(that, argz);
                            }
                        };
                    }(nthCall));
                    return $timeout(later, timeout, apply);
                };
            };
        }
    ]);

}());