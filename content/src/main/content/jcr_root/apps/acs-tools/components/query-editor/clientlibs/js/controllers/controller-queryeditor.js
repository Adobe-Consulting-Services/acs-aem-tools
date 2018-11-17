/*
 * #%L
 * ACS AEM Tools Package
 * %%
 * Copyright (C) 2014 Adobe
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

/*global angular: false, ace: false, $timeout: false, _: false */

angular.module('qeControllers').
    controller('QueryEditorCtrl', ['$scope', 'Crx', 'debounce', '$timeout',
        function ($scope, Crx, debounce, $timeout) {
            'use strict';

            $scope.running = true;

            $scope.autoQuery = false;
            $scope.showAutoQueryWarning = false;

            $scope.status = {
                requesting: false,
                duration: 0
            };

            $scope.source = 'type=cq:Page\n' +
                'fulltext=experience\n' +
                'orderby=@jcr:content/jcr:lastModified\n' +
                'orderby.sort=desc';

            $scope.json = '{}';

            $scope.init = function(config) {
                Crx.init(config);
            };

            $scope.$watch('autoQuery', function(newValue, oldValue) {
                var promise;

                if(newValue) {
                    $scope.showAutoQueryWarning = true;

                    promise = $timeout(function() {
                        $scope.showAutoQueryWarning = false;
                    }, 6000);
                } else {
                    $timeout.cancel(promise);
                    $scope.showAutoQueryWarning = false;
                }
            });

            function params(source) {
                var o = {}; 
                _.each(source.split(/\n/), function(line) {
                    line.replace(/([\w\W]*?)=([\w\W]*)/, function($0, $1, $2) {
                        o[$1] = $2;
                    });
                });
                return o;
            }

            $scope.query = function() {
                var time = new Date().getTime();
                $scope.status.requesting = true;
                Crx.query(params($scope.source)).
                    then(function (resp) {
                        $scope.json = angular.toJson(resp.data, true);
                    })['finally'](function () {
                    $scope.status.requesting = false;
                    $scope.status.duration = new Date().getTime() - time;
                });
            };

            $scope.refresh = debounce(function () {
                if($scope.autoQuery) {
                    $scope.query();
                }
            }, 500);

        }
    ]);