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
 * Coral UI accordion as a vue component
 */
(function () {
    'use strict';
    Vue.component('accordion', {
        template: '<ul class="coral-Accordion coral-Collapsible--block">'
                + '  <slot></slot>'
                + '</ul>'
    });

    Vue.component('accordion-item', {
        props: ["headerTitle", "headerSubtitle"],
        data: function () {
            return {
                open: false
            };
        },
        template: '<li class="coral-Accordion-item is-active" style="border: 1px #dfdfdf solid;">'
                + '  <item-heading @click.native="open = !open" :active="open"'
                + '                :header-title="headerTitle"'
                + '                :header-subtitle="headerSubtitle"></item-heading>'
                + '  <item-content v-show="open">'
                + '    <slot></slot>'
                + '  </item-content>'
                + '</li>',
        /**
         * Internal sub-components of accordion-item
         */
        components: {
            'item-heading': {
                props: ["active", "headerTitle", "headerSubtitle"],
                template: '<h3 class="coral-Accordion-header">'
                        + '  <i style="min-width: 18px;" class="coral-Icon coral-Icon--sizeS"'
                        + '    :class="{\'coral-Icon--chevronRight\': !active, \'coral-Icon--chevronDown\': active}"></i>'
                        + '  <span class="coral-Accordion-title">{{headerTitle}}</span>'
                        + '  <span class="coral-Accordion-subtitle">{{headerSubtitle}}</span>'
                        + ' </h3>'
            },
            'item-content': {
                template: '<div class="coral-Accordion-content">'
                        + '  <slot></slot>'
                        + '</div>'
            }
        }
    });
}());