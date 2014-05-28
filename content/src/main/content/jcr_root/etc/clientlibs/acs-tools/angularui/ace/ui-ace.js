/**
 * The MIT License
 * 
 * Copyright (c) 2012 the AngularUI Team, http://angular-ui.github.com
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
'use strict';
angular.module('ui.ace', []).constant('uiAceConfig', {}).directive('uiAce', [
  'uiAceConfig',
  function (uiAceConfig) {
    if (angular.isUndefined(window.ace)) {
      throw new Error('ui-ace need ace to work... (o rly?)');
    }
    return {
      restrict: 'EA',
      require: '?ngModel',
      link: function (scope, elm, attrs, ngModel) {
        var options, opts, acee, session, onChange;
        options = uiAceConfig.ace || {};
        opts = angular.extend({}, options, scope.$eval(attrs.uiAce));
        acee = window.ace.edit(elm[0]);
        session = acee.getSession();
        onChange = function (callback) {
          return function (e) {
            var newValue = session.getValue();
            if (newValue !== scope.$eval(attrs.value) && !scope.$$phase && !scope.$root.$$phase) {
              if (angular.isDefined(ngModel)) {
                scope.$apply(function () {
                  ngModel.$setViewValue(newValue);
                });
              }
              if (angular.isDefined(callback)) {
                scope.$apply(function () {
                  if (angular.isFunction(callback)) {
                    callback(e, acee);
                  } else {
                    throw new Error('ui-ace use a function as callback.');
                  }
                });
              }
            }
          };
        };
        if (angular.isDefined(opts.showGutter)) {
          acee.renderer.setShowGutter(opts.showGutter);
        }
        if (angular.isDefined(opts.useWrapMode)) {
          session.setUseWrapMode(opts.useWrapMode);
        }
        if (angular.isFunction(opts.onLoad)) {
          opts.onLoad(acee);
        }
        if (angular.isString(opts.theme)) {
          acee.setTheme('ace/theme/' + opts.theme);
        }
        if (angular.isString(opts.mode)) {
          session.setMode('ace/mode/' + opts.mode);
        }
        attrs.$observe('readonly', function (value) {
          acee.setReadOnly(value === 'true');
        });
        if (angular.isDefined(ngModel)) {
          ngModel.$formatters.push(function (value) {
            if (angular.isUndefined(value) || value === null) {
              return '';
            } else if (angular.isObject(value) || angular.isArray(value)) {
              throw new Error('ui-ace cannot use an object or an array as a model');
            }
            return value;
          });
          ngModel.$render = function () {
            session.setValue(ngModel.$viewValue);
          };
        }
        session.on('change', onChange(opts.onChange));
        elm.on('$destroy', function () {
          acee.session.$stopWorker();
          acee.destroy();
        });
        scope.$watch(function () {
          return [
            elm[0].offsetWidth,
            elm[0].offsetHeight
          ];
        }, function () {
          acee.resize();
          acee.renderer.updateFull();
        }, true);
      }
    };
  }
]);