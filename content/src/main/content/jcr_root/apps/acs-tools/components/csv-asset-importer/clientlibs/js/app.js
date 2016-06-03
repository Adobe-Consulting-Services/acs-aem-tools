/*
 * #%L
 * ACS AEM Tools Bundle
 * %%
 * Copyright (C) 2015 Adobe
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

/*global angular: false, Upload: false */

angular.module('acs-tools-csv-asset-importer-app', ['ngFileUpload', 'acsCoral', 'ACS.Tools.notifications']).controller('MainCtrl',
        ['$scope', '$http', '$timeout', 'Upload', 'NotificationsService', function ($scope, $http, $timeout, Upload, NotificationsService) {

    $scope.app = {
        uri: ''
    };

    $scope.form = {
        locale: '',
        charset: '',
        uniqueProperty: '',
        converter: 'default',
        separator: '',
        importStrategy: 'FULL',
        updateBinary: 'false',
        absTargetPathProperty: '',
        relSrcPathProperty: '',
        mimeTypeProperty: '',
        skipProperty: '',
        batchSize: '',
        throttle: '',
        multiDelimiter: '',
        ignoreProperties: 'absTargetPath,relSrcPath,mimeType,skip'
    };

    $scope.result = {
        message: '',
        assets: []
    };

    $scope.importAssets = function () {
        NotificationsService.running(true);

        $scope.result = {
            message: '',
            assets: []
        };

        Upload.upload({
            url: $scope.app.uri + '.import.json',
            fields: {
                'charset': $scope.form.charset || 'UTF-8',
                'uniqueProperty' : $scope.form.uniqueProperty || '',
                'fileLocation': $scope.form.fileLocation || '/dev/null',
                'importStrategy': $scope.form.importStrategy || 'FULL',
                'updateBinary': $scope.form.updateBinary || 'false',
                'delimiter': $scope.form.delimiter || '',
                'separator': $scope.form.separator || '',
                'absTargetPathProperty': $scope.form.absTargetPathProperty || 'absTargetPath',
                'relSrcPathProperty': $scope.form.relSrcPathProperty || 'relSrcPath',
                'mimeTypeProperty': $scope.form.mimeTypeProperty || 'mimeType',
                'skipProperty': $scope.form.skipProperty || '',
                'batchSize': $scope.form.batchSize || 1000,
                'throttle': $scope.form.throttle || 0,
                'ignoreProperties' : $scope.form.ignoreProperties || '',
                'multiDelimiter' : $scope.form.multiDelimiter || '|',
                '_charset_' : $scope.form.charset || 'UTF-8'
            },
            file: $scope.files[0]
        }).success(function (data, status, headers, config) {
            NotificationsService.running(false);

            $scope.result = data || {};
            $scope.result.assets = $scope.result.assets || [];
            $scope.result.failures = $scope.result.failures || 0;

            if($scope.result.failures > 0 || $scope.result.assets.length === 0) {
                if($scope.result.assets.length === 0) {
                    NotificationsService.add('notice',
                        'Warning!', 'Could not find any asset entries that can be processed'
                        + ' Scroll down to see details.');
                } else if($scope.result.failures > 0) {
                    NotificationsService.add('notice',
                        'Warning!',  'No assets entries could be processed, but errors occurred'
                        + ' Scroll down to see details.');
                }
            } else {
                NotificationsService.add('success',
                    'Success! ', (data.message || 'Your assets have been imported.' )
                    + ' Scroll down to see details.');
            }

        }).error(function (data, status, headers, config) {
            NotificationsService.running(false);

            NotificationsService.add('error',
                'Error! ',  (data.message || 'An unknown error occurred' ));
        });
    };
}]);

