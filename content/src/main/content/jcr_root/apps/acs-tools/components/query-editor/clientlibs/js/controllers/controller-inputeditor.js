/*global angular: false, ace: false */
/*jslint regexp: false */

angular.module('qeControllers').
    controller('QueryInputCtrl', ['$scope', 'Crx',
        function ($scope, Crx) {
            'use strict';

            $scope.initEditor = function (editor) {
                var langTools = ace.require("ace/ext/language_tools");

                editor.setOptions({
                    enableBasicAutocompletion: true,
                    enableSnippets: true
                });

                function filesearch(prefix, callback) {
                    Crx.filesearch(prefix).then(function(resp) {
                        var items = [];
                        angular.forEach(resp.data, function(value) {
                            items.push({
                                value: value['jcr:path'],
                                meta: 'filesearch'
                            });
                        });
                        if(items.length) { callback(null, items); }
                    });
                }

                function typesearch(prefix, callback) {
                    if($scope.nodetypes) {
                        callback(null, $scope.nodetypes);
                    } else {
                        Crx.nodetypes().then(function(resp) {
                            var items = [];
                            angular.forEach(resp.data, function(value, key) {
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

                langTools.addCompleter({
                    getCompletions: function(editor, session, pos, prefix, callback) {
                        var line = editor.session.getLine(pos.row);

                        if(/path/.exec(line)) {
                            filesearch(prefix, callback);
                        } else if(/type/.exec(line)) {
                            typesearch(prefix, callback);
                        } else if(/^[^=]$/.exec(line)) {
                            callback(null, []);
                        }
                    }
                });
            };


        }
    ]);