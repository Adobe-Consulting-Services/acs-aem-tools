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

aemFiddle.controller('CodeCtrl', ['$scope', '$http', '$timeout', function($scope, $http, $timeout){

    /* Data */
    $scope.data = {};

    /* App Data */
    $scope.data.app = {
        runURL: $('#app-data').data('run-url'),
        executeURL: $('#app-data').data('execute-url'),
        myFiddlesPath: $('#app-data').data('myfiddles-path'),
        currentPagePath:  $('#app-data').data('current-page-path')
    };

    /* UI Data */
    $scope.data.ui = {};
    $scope.data.ui.output = {
        hasData: false,
        htmlView: false
    };
    $scope.data.ui.rail = {
        visible: false
    };
    $scope.data.ui.myfiddles = {
        createFiddle: {
            visible: false
        }
    };

    /* Execution Data */
    $scope.data.execution = {
        count: 0,
        running: false,
        initialSrc: '' /* Init's from DOM */
    };
    $scope.data.execution.params = {
        resource: ''
    };
    $scope.data.execution.result = {
        data: '',
        executedAt: 0,
        resource: '',
        success: false
    };

    /* MyFiddles Data */
    $scope.data.myfiddles = {};
    $scope.data.myfiddles.list = [];
    $scope.data.myfiddles.current = null;
    $scope.data.myfiddles['new'] = {
        title: ''
    };

    /* Notifications */
    $scope.data.notifications = [];

    /* Method namespaces */
    $scope.app = {};
    $scope.execution = {};
    $scope.myfiddles = {};
    $scope.ui = {};

    /* Core Execution Methods */

    $scope.app.run = function(runURL, executeURL) {
        if($scope.app.throttle()) {
            return;
        }

        $scope.data.execution.running = true;

        $http({
            method: 'POST',
            url: runURL,
            headers: {
                'Accept': '*/*',
                'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'
            },
            data: $.param({
                './jcr:primaryType': 'nt:file',
                './jcr:content/jcr:primaryType': 'nt:resource',
                './jcr:content/jcr:mimeType': 'text/plain',
                './jcr:content/jcr:encoding': 'utf-8',
                './jcr:content/jcr:lastModified': moment().format(),
                './jcr:content/jcr:lastModified@TypeHint': 'Date',
                './jcr:content/jcr:data': aemFiddle.ace.input.editor.getValue(),
                './jcr:content/jcr:data@TypeHint': 'Binary'
            })
        }).success(function(data, status, headers, config) {
            $scope.app.execute(executeURL);
        }).error(function(data, status, headers, config) {
            $scope.data.execution.result.data = data;
            aemFiddle.ace.output.load(data);

            $scope.data.execution.running = false;
            $scope.ui.notify('error', 'Error', 'Your code could not be saved to AEM for execution.');
        });
    };


    /**
     * Execute current fiddle against the provided resource
     **/
    $scope.app.execute = function(url) {

        $http({
            method: 'GET',
            url: url,
            headers: {
                'Accept': '*/*',
                'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'
            },
            params: {
                't': new Date().getTime(),
                'resource': $scope.data.execution.params.resource
            }
        }).success(function(data, status, headers, config) {
            $scope.data.execution.result = {
                success: true,
                executedAt: new Date().getTime(),
                resource: $scope.data.execution.params.resource || $scope.data.app.currentPagePath,
                data: data
            };
            aemFiddle.ace.output.load(data);

            $scope.data.ui.output.hasData = true;
            $scope.data.execution.running = false;
            $scope.data.execution.count++;

            if(status !== 200) {
                $scope.ui.notify('notice', 'Warning', 'Your code contains errors. See output for details.');
            }

        }).error(function(data, status, headers, config) {
            $scope.data.execution.result = {
                success: false,
                executedAt: new Date().getTime(), //moment().format('h:mm:ss a'),
                resource: $scope.data.execution.params.resource,
                data: data
            };

            aemFiddle.ace.output.load(data);

            $scope.data.execution.running = false;
            $scope.ui.notify('notice', 'Warning', 'Your code contains errors. See output for details.');
        });
    };

    $scope.app.throttle = function() {
        var throttleInMs = 1000,
        now = new Date().getTime();

        if(!$scope.data.execution.result.executedAt) {
            return false;
        } else if((now - $scope.data.execution.result.executedAt) < throttleInMs) {
            $scope.ui.notify('notice', 'Warning', 'Please wait ' + (throttleInMs) / 1000  + ' second between code executions.');
            return true;
        } else {
            return false;
        }
    };

    /* Core App Methods */
    $scope.app.reset = function() {
        var confirmReset = confirm("Are you sure you want to reset? All unsaved code will be lost.");

        if(confirmReset) {
            // Reset input to default code
            aemFiddle.ace.input.load($scope.data.execution.initialSrc);
            // Clear output
            aemFiddle.ace.output.load('');
            $scope.data.execution.result = {
                data: '',
                executedAt: 0,
                resource: '',
                success: false
            };

            $scope.data.myfiddles.current = null;
            $scope.data.ui.output.hasData = false;
            $scope.myfiddles.markAsActive($scope.data.myfiddles.list, { path: '' });
        }
    };

    /* Core MyFiddles Methods */

    $scope.myfiddles.create = function(url) {
        var title = $scope.data.myfiddles['new'].title;

        if(!title) {
            title = moment().format('M/D/YYYY, h:mm:ss a');
        }

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
                './code': aemFiddle.ace.input.editor.getValue()
            })
        }).success(function(data, status, headers, config) {
            $scope.data.myfiddles.current = {
                title: title,
                path: data.path,
                active: true
            };

            $scope.myfiddles.list(url);

            $scope.ui.hideCreateFiddle();
            $scope.ui.notify('success', 'Saved', 'Your code was saved as "' + title + '".');
        }).error(function(data, status, headers, config) {
            $scope.ui.notify('error', 'Error', 'Your code could not be saved.');
        });
    };


    $scope.myfiddles['delete'] = function(fiddle) {
        var url = '',
            confirmDelete = confirm("Are you sure you want to delete this?");
        if(fiddle && fiddle.path) {  url = fiddle.path; }

        if(confirmDelete) {
            $http({
                method: 'POST',
                url: url,
                headers: {
                    'Accept': 'application/json',
                    'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'
                },
                data: $.param({
                    ':operation': 'delete'
                })
            }).success(function(data, status, headers, config) {
                if($scope.data.myfiddles.current
                        && $scope.data.myfiddles.current.path === fiddle.path) {
                    $scope.data.myfiddles.current = null;
                }

                $scope.myfiddles.list($scope.data.app.myFiddlesPath);

                $scope.ui.notify('info', 'Deleted', '"' + fiddle.title + '" was deleted.');
            }).error(function(data, status, headers, config) {
                $scope.ui.notify('error', 'Error', '"' + fiddle.title + '" could not be deleted.');
            });
        }
    };

    $scope.myfiddles.load = function(fiddle) {
        var url = '';
        if(fiddle && fiddle.path) {  url = fiddle.path; }

        $http({
            method: 'GET',
            url: url + '/code',
            params: {
                t: new Date().getTime()
            }
        }).success(function(data, status, headers, config) {
            $scope.data.myfiddles.current = fiddle;

            $scope.myfiddles.markAsActive(
                    $scope.data.myfiddles.list,
                    $scope.data.myfiddles.current);

            // Reload input
            aemFiddle.ace.input.load(data);

             // Clear output
            aemFiddle.ace.output.load('');
            $scope.data.execution.result = {
                data: '',
                executedAt: 0,
                resource: '',
                success: false
            };

            $scope.data.ui.output.hasData = false;
            $scope.ui.notify('info', 'Loaded', '"' + fiddle.title + '" was loaded.');
        }).error(function(data, status, headers, config) {
            $scope.ui.notify('error', "Error", '"' + fiddle.title + '" could not be loaded.');
        });
    };

    $scope.myfiddles.update = function(fiddle) {
        var url = '';
        if(fiddle && fiddle.path) {  url = fiddle.path; }

        if($scope.data.ui.myfiddles.createFiddle.visible) { return; }

        if(!$scope.data.myfiddles.current || !$scope.data.myfiddles.current.path) {
            $scope.ui.notify('info', 'Info', 'Create a new fiddle using the "+" sign before updating.');
            return;
        }

        $http({
            method: 'POST',
            url: url,
            headers: {
                'Accept': '*/*',
                'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'
            },
            data: $.param({
                'code': aemFiddle.ace.input.editor.getValue()
            })
        }).success(function(data, status, headers, config) {
            $scope.ui.notify('success', 'Updated', '"' + fiddle.title + '" was updated.');
        }).error(function(data, status, headers, config) {
            $scope.ui.notify('error', 'Error', '"' + fiddle.title + '" could not be updated.');
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

            angular.forEach(data, function(value, key) {
                if(typeof value !== 'object') { return; }

                value.title = value.title || value['jcr:created'];
                value.path = url + '/' + key;
                value.active = false;

                $scope.data.myfiddles.list.push(value);
            });

            $scope.data.myfiddles.list.reverse();
            $scope.myfiddles.markAsActive(
                    $scope.data.myfiddles.list,
                    $scope.data.myfiddles.current);
        }).error(function(data, status, headers, config) {
            $scope.ui.notify('error', "Error", "Could not retrieve your code.");
        });
    };

    $scope.myfiddles.markAsActive = function(fiddles, fiddle) {
        var url = '',
            i = 0,
            l = fiddles.length;
        if(fiddle && fiddle.path) {  url = fiddle.path; }


        for(i = 0; i < l; i++) {
            if(fiddles[i].path === url) {
                fiddles[i].active = true;
            } else {
                fiddles[i].active = false;
            }
        }
    };

    /* UI Methods */
    $scope.ui.toggleRail = function() {
        var visible = $scope.data.ui.rail.visible;
        if(visible) {
            $scope.ui.hideCreateFiddle();
        }
        $scope.data.ui.rail.visible = !visible;
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

    /* Initialization */
    $scope.myfiddles.list($scope.data.app.myFiddlesPath);
    /* Store initial input src for use during reset */
    $scope.data.execution.initialSrc = aemFiddle.ace.input.editor.getValue();
}]);
