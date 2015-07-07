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

angular.module('qeServices').
    factory('Crx', ['$http', '$q',
        function ($http, $q) {
            'use strict';

            var canceler = $q.defer(),
                config = {
                    queryBuilderPath: '/bin/querybuilder.json',
                    nodeTypesPath: '/crx/de/nodetypes.jsp',
                    fileSearchPath: '/crx/de/filesearch.jsp',
                    predicatesPath: '/bin/acs-tools/qe/predicates.json'
                };

            return {
                init: function(cfg) {
                    config = cfg;
                },
                query: function (params) {
                    canceler.resolve();
                    canceler = $q.defer();
                    return $http.get(config.queryBuilderPath, {
                        params: params,
                        timeout: canceler.promise
                    });
                },
                nodetypes: function () {
                    return $http.get(config.nodeTypesPath);
                },
                filesearch: function (name) {
                    return $http.get(config.fileSearchPath, {
                        params: { name: name }
                    });
                },
                predicates: function () {
                    return $http.get(config.predicatesPath);
                }
            };
        }
    ]);
