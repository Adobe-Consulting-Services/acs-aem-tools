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

/*global angular: false, _: false */

angular.module('ACS.Tools.notifications', []).directive('notifications', function ($timeout) {
    return {
        restrict: 'E',
        scope: {
            size: '@size',
            dismissible: '@dismissible',
            items: '=items'
        },
        template: ' <div ng-show="items.length > 0" class="notifications">'
                        + '<div ng-repeat="item in items">'
                            + '<div class="alert {{ item.type }} {{ size }}">'
                                + '<button ng-hide="dismissible === \'false\'" class="close"'
                                + ' data-dismiss="alert">&times;</button>'
                                + '<strong>{{ item.title }}</strong>'
                                + '<div>{{ item.message }}</div>'
                            + '</div>'
                        + '</div>'
                    + '</div>',
        replace: true,
        link: function (scope, element, attrs) {

            var timeout = attrs.timeout || 20; // seconds
            timeout = timeout * 1000;

            scope.$watchCollection('items', function(newItems, oldItems) {
                var notificationTimeout = timeout,
                    newItem;

                if((newItems.length || 0) > (oldItems.length || 0)) {
                    newItem = newItems[newItems.length - 1];

                    if(newItem.type === 'success') {
                        notificationTimeout = timeout / 2;
                    }

                    $timeout(function() {
                        var index = scope.items.indexOf(newItem);

                        if(index > -1) {
                            scope.items.splice(index, 1);
                        }
                    }, notificationTimeout);
                }
            });
        }
    };
});