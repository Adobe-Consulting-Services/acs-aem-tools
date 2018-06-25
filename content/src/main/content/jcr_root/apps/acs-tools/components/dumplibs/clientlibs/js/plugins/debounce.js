
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
 * Credit: https://davidwalsh.name/javascript-debounce-function
 */
(function () {
    'use strict';
    Vue.use({
        install: function (Vue) {
            // Returns a function, that, as long as it continues to be invoked, will not
            // be triggered. The function will be called after it stops being called for
            // N milliseconds. If `immediate` is passed, trigger the function on the
            // leading edge, instead of the trailing.
            Vue.debounce = function debounce(func, wait, immediate) {
                var timeout;
                return function () {
                    var context = this,
                        args = arguments,
                        later = function () {
                            timeout = null;
                            if (!immediate) {
                                func.apply(context, args);
                            }
                        },
                        callNow = immediate && !timeout;
                    clearTimeout(timeout);
                    timeout = setTimeout(later, wait);
                    if (callNow) {
                        func.apply(context, args);
                    }
                };
            };
        }
    });
}());
