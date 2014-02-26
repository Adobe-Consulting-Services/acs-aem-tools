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

/*global angular: false, ace: false */

angular.module('qeControllers').
    controller('QueryEditorCtrl', ['$scope', 'Crx', 'debounce',
        function ($scope, Crx, debounce) {
            'use strict';

            $scope.running = true;

            $scope.autoQuery = true;

            $scope.status = {
                requesting: false,
                duration: 0
            };

            $scope.source = 'type=nt:file\n' +
                'nodename=*.jar\n' +
                'orderby=@jcr:content/jcr:lastModified\n' +
                'orderby.sort=desc';

            $scope.json = '{}';

            function params(source) {
                var o = {};
                source.replace(/^\s*(\S*)\s*=\s*(\S*)\s*$/gm, function ($0, $1, $2) {
                    o[$1] = $2;
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