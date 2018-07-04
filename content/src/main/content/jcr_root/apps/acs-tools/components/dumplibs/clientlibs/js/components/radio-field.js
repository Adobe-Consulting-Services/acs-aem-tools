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
/*global Vue, console */
/**
 * Coral UI radio group as a vue component
 */
(function () {
    'use strict';
    Vue.component('radio-field', {
        props: ['value', 'options', 'quicktip', 'label', 'emptyOption'],
        data: function () {
            return {
                // using a value to allow radio checked status to change
                // This value is not used for anything else
                radioValue: ''
            };
        },
        template: '<div class="coral-Form-fieldwrapper">'
                + '  <label class="coral-Form-fieldlabel">{{label}}</label>'
                + '  <div class="coral-Form-field coral-RadioGroup coral-RadioGroup--labelsBelow">'
                + '    <label class="coral-Radio" v-for="opt in options">'
                + '      <input type="radio" :value="opt" v-model="radioValue" @change="$emit(\'update:value\', $event.target.value)" class="coral-Radio-input" />'
                + '      <span class="coral-Radio-checkmark"></span>'
                + '      <span class="coral-Radio-description">{{opt ? opt : emptyOption}}</span>'
                + '    </label>'
                + '  <span class="coral-Form-fieldinfo coral-Icon coral-Icon--infoCircle coral-Icon--sizeS" data-init="quicktip" data-quicktip-type="info" data-quicktip-arrow="right" v-if="quicktip" :data-quicktip-content="quicktip"></span>'
                + '  </div>'
                + '</div>'
    });
}());