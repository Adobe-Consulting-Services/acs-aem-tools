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
    controller('QueryOutputCtrl', ['$scope',
        function ($scope) {
            'use strict';

            $scope.initEditor = function (editor) {
                var Range = ace.require("ace/range").Range,
                    event = ace.require("ace/lib/event"), markerId;

                ace.config.set('basePath', $scope.aceEditorBasePath);

                editor.setOptions({
                    enableMultiselect: false
                });

                function getToken(e) {
                    var pos = e.getDocumentPosition();
                    return e.editor.session.getTokenAt(pos.row, pos.column);
                }

                function linkable(token) {
                    var re = /^"\/.*"/;
                    return token.type === "string" && re.test(token.value);
                }

                editor.on("click", function (e) {
                    if (!e.domEvent.metaKey) {
                        return;
                    }

                    var token = getToken(e), path;

                    if (linkable(token)) {
                        path = /^"(.*)"/.exec(token.value)[1];
                        window.open("/crx/de/index.jsp#" + path, "crxde");
                    }
                });

                editor.on("mousemove", function (e) {
                    if (!e.domEvent.metaKey) {
                        return;
                    }

                    e.editor.session.removeMarker(markerId);

                    var token = getToken(e), pos, range;

                    if (linkable(token)) {
                        pos = e.getDocumentPosition();
                        range = new Range(pos.row, token.start, pos.row, token.start + token.value.length);

                        markerId = e.editor.session.addMarker(range, 'link');
                    }

                });

                event.addListener(document, "keyup", function () {
                    editor.session.removeMarker(markerId);
                });
            };
        }
    ]);