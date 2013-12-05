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
        resourcePath: $('#app-data').data('resource-path'),
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
    
    /* Defaults */    
    $scope.data.ui.scriptExtOption = 'jsp';
    $scope.data.ui.scriptExtOptions = [];

    /* Execution Data */
    $scope.data.execution = {
        count: 0,
        running: false,
        initialSrc: '' /* Init's from DOM */
    };
    $scope.data.execution.params = {
        resource: '',
        scriptExt: 'jsp'
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

    /* Templates */
    $scope.data.templates = [];



    /* Watchers */
    $scope.$watch('data.execution.params.scriptExt', function(newValue, oldValue) {
        $scope.data.ui.scriptExtOption = newValue;
        aemFiddle.ace.input.setMode(newValue);
    });


    /* Method namespaces */
    $scope.app = {};
    $scope.execution = {};
    $scope.myfiddles = {};
    $scope.ui = {};
    $scope.util = {};

    /* Core Execution Methods */

    $scope.app.run = function(runURL) {
        $scope.data.execution.running = true;

        $http({
            method: 'POST',
            url: runURL,
            headers: {
                'Accept': '*/*',
                'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'
            },
            data: $.param({
                'scriptdata': aemFiddle.ace.input.editor.getValue(),
                'scriptext' : $scope.data.execution.params.scriptExt,
                'resource': $scope.data.execution.params.resource
            })
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

        }).error(function(data, status, headers, config) {
            $scope.data.execution.result = {
                success: false,
                executedAt: new Date().getTime(),
                resource: $scope.data.execution.params.resource || $scope.data.app.currentPagePath,
                data: data
            };

            aemFiddle.ace.output.load(data);
            $scope.data.ui.output.hasData = true;
            $scope.data.execution.running = false;
            $scope.data.execution.count++;

            $scope.ui.notify('notice', 'Warning', 'Your code contains errors. See output for details.');        
        });
    };

    /* Core App Methods */
    $scope.app['new'] = function(skipConfirm) {
        var scriptExt = $scope.data.ui.scriptExtOption,
            resetConfirmed = false;

        if(skipConfirm || !aemFiddle.ace.input.isDirty()) {
            resetConfirmed = true;
        } else {
            resetConfirmed = confirm("Are you sure you want a new fiddle? All unsaved code will be lost.");
        }

        if(resetConfirmed) {
            // Reset input to default code
            aemFiddle.ace.input.load($scope.util.getDefaultTemplate(scriptExt), scriptExt);
            $scope.data.execution.params.scriptExt = scriptExt;

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
                './scriptdata': aemFiddle.ace.input.editor.getValue(),
                './scriptext': $scope.data.execution.params.scriptExt
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

        if(aemFiddle.ace.input.isDirty()) {
            if(!confirm("Loading this fiddle will lose all unsaved changes.")) {
                return;
            }
        }

        $http({
            method: 'GET',
            url: url + '.json',
            params: {
                t: new Date().getTime()
            }
        }).success(function(data, status, headers, config) {
            $scope.data.myfiddles.current = fiddle;

            $scope.myfiddles.markAsActive(
                    $scope.data.myfiddles.list,
                    $scope.data.myfiddles.current);

            // Set extension    
            $scope.data.execution.params.scriptExt = data.scriptext || 'jsp';
            
            // Reload input
            aemFiddle.ace.input.load(data.scriptdata, data.scriptext);

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
        if(fiddle && fiddle.path) { url = fiddle.path; }

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
                'scriptdata': aemFiddle.ace.input.editor.getValue(),
                'scriptext':  $scope.data.execution.params.scriptExt
            })
        }).success(function(data, status, headers, config) {
            $scope.myfiddles.list($scope.data.app.myFiddlesPath);            
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
                var myfiddle = {};
                if(typeof value !== 'object') { return; }

                myfiddle = {
                    title: value.title || moment(value['jcr:created']).format('M/D/YYYY, h:mm:ss a'),
                    path: url + '/' + key,
                    scriptExt: value.scriptext || 'jsp',
                    active:  false                    
                };

                $scope.data.myfiddles.list.push(myfiddle);
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
        if(fiddle && fiddle.path) { url = fiddle.path; }

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


    $scope.util.getDefaultTemplate = function(scriptExt) {
        var templateData = '';
        angular.forEach($scope.data.templates, function(template, key) {
            if(!templateData && template['default'] && (template.scriptExt === scriptExt)) {
                templateData = template.scriptData;
            }
        });

        return templateData;
    };


    /* App Initialization */

    var init = function () {
        $scope.myfiddles.list($scope.data.app.myFiddlesPath);
        /* Store initial input src for use during reset */

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

            $scope.app['new'](true);
        });
    };

    init();  

}]);
