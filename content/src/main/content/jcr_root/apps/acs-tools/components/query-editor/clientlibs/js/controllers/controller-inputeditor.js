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
/*jslint regexp: false */

angular.module('qeControllers').
    controller('QueryInputCtrl', ['$scope', 'Crx',
        function ($scope, Crx) {
            'use strict';

            $scope.initEditor = function (editor) {
                var langTools;

                ace.config.set('basePath', $scope.aceEditorBasePath);

                langTools = ace.require("ace/ext/language_tools");

                editor.setOptions({
                    enableBasicAutocompletion: true,
                    enableSnippets: true
                });

                function filesearch(prefix, callback) {
                    Crx.filesearch(prefix).then(function (resp) {
                        var items = [];
                        angular.forEach(resp.data, function (value) {
                            items.push({
                                value: value['jcr:path'],
                                meta: 'filesearch'
                            });
                        });
                        if (items.length) {
                            callback(null, items);
                        }
                    });
                }

                function typesearch(prefix, callback) {
                    if ($scope.nodetypes) {
                        callback(null, $scope.nodetypes);
                    } else {
                        Crx.nodetypes().then(function (resp) {
                            var items = [];
                            angular.forEach(resp.data, function (value, key) {
                                items.push({
                                    value: key,
                                    meta: 'nodetype'
                                });
                            });
                            $scope.nodetypes = items;
                            callback(null, $scope.nodetypes);
                        });
                    }
                }
                
                function predicatesearch(callback) {
                    if ($scope.predicates) {
                        callback(null, $scope.predicates);
                    } else {
                        Crx.predicates().then(function (resp) {
                            var items = [];
                            angular.forEach(resp.data, function (value) {
                                items.push({
                                    value: value,
                                    meta: 'predicate'
                                });
                            });
                            $scope.predicates = items;
                            callback(null, $scope.predicates);
                        });
                    }
                }

                langTools.addCompleter({
                    getCompletions: function (editor, session, pos, prefix, callback) {
                        var line = editor.session.getLine(pos.row);

                        if (/path/.exec(line)) {
                            filesearch(prefix, callback);
                        } else if (/type/.exec(line)) {
                            typesearch(prefix, callback);
                        } else if (line === '') {
                            predicatesearch(callback);
                        } else if (/^[^=]$/.exec(line)) {
                            callback(null, []);
                        }
                    }
                });
            };


        }
    ]);