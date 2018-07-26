
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
 * Plugin to convert an object to a key/value array
 * example {"type": "popcorn"} becomes [{key:"type", value:"popcorn"}]
 */
(function () {
    'use strict';
    Vue.use({
        install: function (Vue) {
            // Returns helper function to transform an object to key/value pair array
            Vue.toKeyValArray = function (object) {
                // empty object
                if(Object.keys(object).length === 0 && object.constructor === Object){
                    return [];
                }
                return Object.keys(object).map(function (key) {
                    return {
                        key: key,
                        val: object[key]
                    };
                });
            };
        }
    });
}());
