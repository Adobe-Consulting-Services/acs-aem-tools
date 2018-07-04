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
/*global Vue, axios, console */

/**
 * A component that shows an object/string/array as a list
 * The property `value` must be an array, it's a list after all.
 * Each entry in the `value` array has to be a `key`/`value` object.
 * Each `value` in each entry can be:
 *  1. object:
 *      The object is transformed to an array of key/value strings to render as a list (recursively)
 *  2. array:
 *      The array must be an array of key/value strings to render as a list
 *  3. string:
 *      rendered as is
 */
(function () {
    'use strict';
    Vue.component('list', {
        props: ['value', 'header'],
        methods: {
            isObject: function (val){
                return Vue.type.isObject(val);
            },
            isString: function (arg) {
                return Vue.type.isString(arg);
            },
            toKeyValArray: function (object) {
                return Vue.toKeyValArray(object);
            }
        },
        template: '<div>'
                + '  <h3>{{header}}</h3>'
                + '  <ul class="coral-List">'
                + '    <template v-for="entry in value">'
                + '      <li class="coral-List-item" v-if="entry && entry.val">'
                           // entry is a string
                + '        <span v-if="isString(entry.val)">'
                + '          <b>{{entry.key}}:</b> <crx-link :link="entry.val"> </crx-link>'
                + '        </span>'
                           // entry is an array
                + '       <span v-else-if="Array.isArray(entry.val)">'
                + '         <b>{{entry.key}}:</b>'
                + '          <ul class="coral-List">'
                + '            <li class="coral-List-item" v-for="item in entry.val"><crx-link :link="item"> </crx-link></li>'
                + '          </ul>'
                + '        </span>'
                           // entry is an Object - recurse
                + '        <list style="padding-left: 2em;" v-else-if="isObject(entry.val)" :value="toKeyValArray(entry.val)" :header="entry.key"></list>'
                + '      </li>'
                + '    </template>'
                + '  </ul>'
                + '</div>'
    });
}());