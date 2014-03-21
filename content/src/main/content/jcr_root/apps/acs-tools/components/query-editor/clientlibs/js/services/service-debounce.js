/*
 * debounce from https://github.com/angular/angular.js/issues/2690
 */

/*global angular: false, ace: false */

angular.module('qeServices').
    factory('debounce', ['$timeout',
        function ($timeout) {
            'use strict';

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
