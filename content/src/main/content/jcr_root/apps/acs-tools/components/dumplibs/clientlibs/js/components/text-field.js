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
 * Coral UI text field as a vue component
 */
(function () {
    Vue.component('text-field', {
        props: ['value', 'quicktip', 'placeholder', 'label'],
        template: '<div class="coral-Form-fieldwrapper">'
                + '  <label class="coral-Form-fieldlabel">{{label ? label : placeholder}}</label>'
                + '  <input class="coral-Form-field coral-Textfield"'
                + '         @input="$emit(\'update:value\', $event.target.value)"'
                + '         type="text" :placeholder="placeholder ? placeholder : label"'
                + '         :value="value">'
                + '  <span class="coral-Form-fieldinfo coral-Icon coral-Icon--infoCircle coral-Icon--sizeS"'
                + '        data-init="quicktip" data-quicktip-type="info"'
                + '        data-quicktip-arrow="right"'
                + '        :data-quicktip-content="quicktip"></span>'
                + '</div>'
    });
}());