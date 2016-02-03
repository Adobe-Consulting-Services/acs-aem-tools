/*
 * #%L
 * ACS AEM Tools Package
 * %%
 * Copyright (C) 2013 Adobe
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

/*global aemFiddle: false, moment: false, angular: false, confirm: false */

aemFiddle.controller('MainCtrl', ['$scope', '$http', '$timeout', function($scope, $http, $timeout){

    /* Data/Models */
    $scope.data = {
        app: {},
        defaults: {},
        myfiddles: {},
        result: {},
        src: {},
        ui: {}
    };

    /* App Data */
    $scope.data.app = {
        runURL: $('#app-data').data('run-url'),
        resourcePath: $('#app-data').data('resource-path'),
        myFiddlesPath: $('#app-data').data('myfiddles-path'),
        currentPagePath:  $('#app-data').data('current-page-path'),
        running: false,
        count: 0
    };


    /* Defaults */
    $scope.data.defaults.scriptExt = 'jsp';

    /* UI Data */
    $scope.data.ui = {};
    $scope.data.ui.output = {
        hasData: false,
        htmlView: false
    };
    $scope.data.ui.myfiddles = {
        createFiddle: {
            visible: false
        }
    };
    $scope.data.ui.cursor = {
        row: 0,
        column: 0
    };
    $scope.data.ui.scriptExtOptions = [];


    /* Models */

    /* Src; drives input */
    $scope.data.src = {
        lastModifiedAt: 0,
        resource: '',
        scriptData: '',
        scriptExt: $scope.data.defaults.scriptExt
    };

    /* Results; Drives output view */
    $scope.data.result = {
       lastModifiedAt: 0, 
        resource: '',
        executedAt: 0,
        data: '',
        success: true
    };

    /* MyFiddles  */
    $scope.data.myfiddles = {
        list: [],
        current: null,
        'new': {
            title: ''
        }
    };

    /* Notifications */
    $scope.data.notifications = [];

    /* Templates */
    $scope.data.templates = [];



    /* Watchers */

    /* Update on notifications */
    $scope.$watch('data.result', function(newValue, oldValue) {
        aemFiddle.ace.output.load(newValue.data);

        if(!newValue.success) {
            $scope.ui.notify('notice', 'Warning', 'Your code contains errors. See output for details.');
        }
    });

    /* Handles changes to Src */
    $scope.$watch('data.src', function(newValue, oldValue) {
        aemFiddle.ace.input.load(newValue.scriptData, newValue.scriptExt);
    });

    $scope.$watch('data.src.scriptExt', function(newValue, oldValue) {
        aemFiddle.ace.input.setMode(newValue);
    });

    $scope.$watch('data.myfiddles.current', function(newValue, oldValue) {
        $scope.myfiddles.markAsActive($scope.data.myfiddles.list, newValue);
    });


    /* Method namespaces */
    $scope.app = {};
    $scope.myfiddles = {};
    $scope.ui = {};
    $scope.util = {};

    /* Core Execution Methods */

    $scope.app.run = function(runURL) {
        $scope.data.app.running = true;

        $http({
            method: 'POST',
            url: runURL + "?wcmmode=disabled",
            headers: {
                'Accept': '*/*',
                'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'
            },
            data: $.param({
                'scriptdata': aemFiddle.ace.input.editor.getValue(),
                'scriptext' : $scope.data.src.scriptExt,
                'resource': $scope.data.src.resource
            })
        }).success(function(data, status, headers, config) {
                /*
                 Watchers handle:
                 > !success notifications
                 > loading output editors
                 */
                $scope.data.result = $scope.util.buildResult(data, status === 200);

                $scope.data.ui.output.hasData = true;
                $scope.data.app.running = false;
                $scope.data.app.count++;
            }).error(function(data, status, headers, config) {
                /*
                 Watchers handle:
                 > !success notifications
                 > loading output editors
                 */
                $scope.data.result = $scope.util.buildResult(data, false);

                $scope.data.ui.output.hasData = true;
                $scope.data.app.running = false;
                $scope.data.app.count++;
            });

    };

    /* Core App Methods */
    $scope.app['new'] = function(scriptExt, skipConfirm) {
        var resetConfirmed = false,
            newPopover = $("#popover-new").data("popover");

        if (newPopover) {
            newPopover.toggleVisibility();
        }

        if(skipConfirm || !aemFiddle.ace.input.isDirty()) {
            resetConfirmed = true;
        } else {
            resetConfirmed = confirm("Are you sure you want a new fiddle? All unsaved changes will be lost.");
        }

        if(!resetConfirmed) { return; }

        // Reset input to default code

        $scope.data.src = $scope.util.buildSrc(null, $scope.util.getDefaultTemplate(scriptExt), scriptExt);

        // Clear output
        $scope.data.result = $scope.util.resetResult();
        $scope.data.ui.output.hasData = false;
        $scope.data.myfiddles.current = null;
    };

    /* Core MyFiddles Methods */

    $scope.myfiddles.create = function(url) {
        var title = $scope.data.myfiddles['new'].title || moment().format('M/D/YYYY, h:mm:ss a');

        $http({
            method: 'POST',
            url: url + '/*',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'
            },
            data: $.param({
                './jcr:primaryType': 'nt:unstructured',
                './title': title,
                './jcr:created': moment().format(),
                './jcr:created@TypeHint': 'Date',
                './scriptdata': aemFiddle.ace.input.editor.getValue(),
                './scriptext': $scope.data.src.scriptExt
            })
        }).success(function(data, status, headers, config) {
                $scope.data.myfiddles.current = data.path;

                aemFiddle.ace.input.setDirty(false);

                $scope.myfiddles.list(url);
                $scope.ui.hideCreateFiddle();

                $scope.ui.notify('success', 'Saved', 'Your code was saved as "' + title + '".');
            }).error(function(data, status, headers, config) {
                $scope.ui.notify('error', 'Error', 'Your code could not be saved.');
            });
    };


    $scope.myfiddles['delete'] = function(fiddle) {

        if(!confirm("Are you sure you want to delete this?")) { return; }

        $http({
            method: 'POST',
            url: fiddle.path,
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'
            },
            data: $.param({
                ':operation': 'delete'
            })
        }).success(function(data, status, headers, config) {
                if($scope.data.myfiddles.current === fiddle.path) {
                    $scope.data.myfiddles.current = null;
                }

                $scope.myfiddles.list($scope.data.app.myFiddlesPath);

                $scope.ui.notify('info', 'Deleted', '"' + fiddle.title + '" was deleted.');
            }).error(function(data, status, headers, config) {
                $scope.ui.notify('error', 'Error', '"' + fiddle.title + '" could not be deleted.');
            });
    };

    $scope.myfiddles.load = function(fiddle) {
        if(aemFiddle.ace.input.isDirty()) {
            if(!confirm("Loading this fiddle will lose all unsaved changes.")) {
                return;
            }
        }

        $http({
            method: 'GET',
            url: fiddle.path + '.json',
            params: {
                t: new Date().getTime()
            }
        }).success(function(data, status, headers, config) {
                $scope.data.src = $scope.util.buildSrc($scope.data.src.resource,
                    data.scriptdata, data.scriptext);

                $scope.data.result = $scope.util.resetResult();

                $scope.data.myfiddles.current = fiddle.path;
                $scope.data.ui.output.hasData = false;

                $scope.ui.notify('info', 'Loaded', '"' + fiddle.title + '" was loaded.');
            }).error(function(data, status, headers, config) {
                $scope.ui.notify('error', "Error", '"' + fiddle.title + '" could not be loaded.');
            });
    };

    $scope.myfiddles.update = function(fiddlePath) {
        var myFiddle;
        if($scope.data.ui.myfiddles.createFiddle.visible) { return; }

        myFiddle = $scope.util.getMyFiddle(fiddlePath);

        if(!myFiddle) {
            $scope.ui.notify('info', 'Info', 'Create a new fiddle using the "+" sign before updating.');
            return;
        }

        $http({
            method: 'POST',
            url: myFiddle.path,
            headers: {
                'Accept': '*/*',
                'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'
            },
            data: $.param({
                'scriptdata': aemFiddle.ace.input.editor.getValue(),
                'scriptext':  $scope.data.src.scriptExt
            })
        }).success(function(data, status, headers, config) {
                aemFiddle.ace.input.setDirty(false);
                $scope.ui.notify('success', 'Updated', '"' + myFiddle.title + '" was updated.');
            }).error(function(data, status, headers, config) {
                $scope.ui.notify('error', 'Error', '"' + myFiddle.title + '" could not be updated.');
            });
    };

    $scope.myfiddles.list = function(url) {
        $http({
            method: 'GET',
            url: url + '.1.json',
            params: {
                t: new Date().getTime()
            }
        }).success(function(data, status, headers, config) {
                $scope.data.myfiddles.list = [];

                angular.forEach(data, function(entry, key) {
                    var myfiddle = {};

                    if(typeof entry !== 'object') { return; }

                    myfiddle = $scope.util.buildFiddle(entry.title,
                        entry['jcr:created'],
                        url + '/' + key,
                        entry.scriptext);

                    $scope.data.myfiddles.list.push(myfiddle);
                });

                $scope.data.myfiddles.list.reverse();

                $scope.myfiddles.markAsActive(
                    $scope.data.myfiddles.list,
                    $scope.data.myfiddles.current);
            }).error(function(data, status, headers, config) {
                // 404 indicates the fiddles node is not created; most likely a first time user.
                if(status !== 404) {
                    $scope.ui.notify('error', "Error", "Could not retrieve your code.");
                }
            });
    };

    $scope.myfiddles.markAsActive = function(fiddles, currentFiddlePath) {
        var i = 0,
            l = fiddles.length;

        for(i = 0; i < l; i++) {
            if(fiddles[i].path === currentFiddlePath) {
                fiddles[i].active = true;
            } else {
                fiddles[i].active = false;
            }
        }
    };

    $scope.ui.toggleOutput = function() {
        $scope.data.ui.output.htmlView = !($scope.data.ui.output.htmlView);
        aemFiddle.ace.output.reload();
    };

    $scope.ui.showCreateFiddle = function() {
        $scope.data.ui.myfiddles.createFiddle.visible = true;
    };

    $scope.ui.hideCreateFiddle = function() {
        $scope.data.ui.myfiddles.createFiddle.visible = false;
        $scope.data.myfiddles['new'].title = '';
    };

    $scope.ui.updateCursor = function(row, column) {
        $scope.data.ui.cursor.row = row;
        $scope.data.ui.cursor.column = column;
    };

    $scope.ui.notify = function(type, title, message) {
        var notification = {
            type: type,
            title: title,
            message: message
        }, timeout = 5000;

        $scope.data.notifications.unshift(notification);

        // Remove notification after N seconds
        $timeout(function() { $scope.data.notifications.pop(); }, timeout);
    };

    $scope.ui.getLanguage = function(scriptExt) {
        var i = 0,
            item;
        scriptExt = scriptExt || $scope.data.defaults.scriptExt;

        for(i; i  < $scope.data.ui.scriptExtOptions.length; i++) {
            item = $scope.data.ui.scriptExtOptions[i];
            if(item.value === scriptExt) {
                return item.label;
            }
        }

        return "Java Server Pages";
    };


    /* Utils */

    $scope.util.buildResult = function(data, success) {
        return {
            lastModifiedAt: new Date().getTime(),            
            success: success,
            executedAt: new Date().getTime(),
            resource: ($scope.data.src.resource || $scope.data.app.currentPagePath),
            data: data
        };
    };

    $scope.util.resetResult = function() {
        return $scope.util.buildResult('', true);
    };

    $scope.util.buildSrc = function(resource, scriptData, scriptExt) {
        return {
            lastModifiedAt: new Date().getTime(),
            resource: resource,
            scriptData: scriptData || '',
            scriptExt: scriptExt || $scope.data.defaults.scriptExt
        };
    };

    $scope.util.buildFiddle = function(title, createdAt, path, scriptExt) {
        return {
            title: title || moment(createdAt).format('M/D/YYYY, h:mm:ss a'),
            createdAt: createdAt,
            path: path,
            scriptExt: scriptExt  || $scope.data.defaults.scriptExt,
            active: false
        };
    };

    $scope.util.buildEmptyFiddle = function() {
        return {
            title: '',
            path: '',
            scriptExt: $scope.data.defaults.scriptExt,
            active: false
        };
    };

    $scope.util.getDefaultTemplate = function(scriptExt) {
        var templateData = '';
        angular.forEach($scope.data.templates, function(template, key) {
            if(!templateData && template['default'] && (template.scriptExt === scriptExt)) {
                templateData = template.scriptData;
            }
        });

        return templateData;
    };

    $scope.util.getMyFiddle = function(fiddlePath) {
        var i = 0,
            item;

        for(i; i  < $scope.data.myfiddles.list.length; i++) {
            item = $scope.data.myfiddles.list[i];
            if(item.path === fiddlePath) {
                return item;
            }
        }

        return $scope.util.buildEmptyFiddle();
    };

    /* App Initialization */

    var init = function () {
        $scope.myfiddles.list($scope.data.app.myFiddlesPath);
        /* Store initial input src for use during reset */

        /* Update input cursor location */

        aemFiddle.ace.input.editor.getSession().selection.on('changeCursor', function(e) {
            var cursor = aemFiddle.ace.input.editor.getCursorPosition();

            $timeout(function() {
                $scope.ui.updateCursor(cursor.row + 1, cursor.column + 1);
            });
        });

        /* Get script language options from Server */
        $http.get(
            $scope.data.app.resourcePath + '.configuration.script-options.json'
        ).then(function (response) {
            $scope.data.ui.scriptExtOptions = [];

            angular.forEach(response.data, function(value, key) {
                $scope.data.ui.scriptExtOptions.push(value);
            });
        });

        /* Get code templates */
        $http.get(
            $scope.data.app.resourcePath + '.configuration.code-templates.json'
        ).then(function (response) {
            $scope.data.templates = [];

            angular.forEach(response.data, function(value, key) {
                $scope.data.templates.push(value);
            });

            $scope.app['new']($scope.data.defaults.scriptExt, true);
        });
    };

    init();

}]);
