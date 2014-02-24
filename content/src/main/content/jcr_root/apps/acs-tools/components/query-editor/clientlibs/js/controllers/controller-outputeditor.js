/*global angular: false, ace: false */
/*jslint regexp: false */

angular.module('qeControllers').
    controller('QueryOutputCtrl', ['$scope',
        function ($scope) {
            'use strict';

            $scope.initEditor = function (editor) {
                var Range = ace.require("ace/range").Range,
                    event = ace.require("ace/lib/event"), markerId;

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

                editor.on("click", function(e) {
                    if(!e.domEvent.metaKey) { return; }

                    var token = getToken(e), path;

                    if(linkable(token)) {
                        path = /^"(.*)"/.exec(token.value)[1];
                        window.open("/crx/de/index.jsp#" + path, "crxde");
                    }
                });

                editor.on("mousemove", function(e) {
                    if(!e.domEvent.metaKey) { return; }

                    e.editor.session.removeMarker(markerId);

                    var token = getToken(e), pos, range;

                    if(linkable(token)) {
                        pos = e.getDocumentPosition();
                        range = new Range(pos.row, token.start, pos.row, token.start + token.value.length);

                        markerId = e.editor.session.addMarker(range, 'link');
                    }

                });

                event.addListener(document, "keyup", function() {
                    editor.session.removeMarker(markerId);
                });
            };
        }
    ]);