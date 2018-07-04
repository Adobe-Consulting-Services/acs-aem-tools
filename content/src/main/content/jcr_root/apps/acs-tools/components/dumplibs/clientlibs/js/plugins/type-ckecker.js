
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
/*global clearTimeout, Vue */
/*jslint browser:true */
/**
 * A utility to check javascript types.
 * Credit: https://webbjocke.com/javascript-check-data-types/
 */
(function () {
    'use strict';
    Vue.use({
        install: function (Vue) {
            // Returns helper functions to check types
            Vue.type = {
                // Returns if a value is a string
                isString: function isString(value) {
                    return typeof value === 'string' || value instanceof String;
                },
                // Returns if a value is an object
                isObject: function (value) {
                    return value && typeof value === 'object' && value.constructor === Object;
                }
            };
        }
    });
}());