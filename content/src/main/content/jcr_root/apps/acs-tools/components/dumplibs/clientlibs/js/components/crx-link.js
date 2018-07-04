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
 * Takes a url as a property, if it starts with `/` it renders a link (<a> tag) and an adjacent icon to open that url in crx/de
 * If it does not start with `/` renders a <span>
 */
(function () {
    'use strict';
    Vue.component('crx-link', {
        props: ['link'],
        methods: {
            isLink: function (l) {

                return Vue.type.isString(l) && l.startsWith('/');
            },
            crxLink: function (link) {
                return '/crx/de/index.jsp#' + link;
            }
        },
        template: '<span>'
                + '  <a class="coral-Link" v-if="isLink(link)" :href="link" target="_blank">{{link}}</a>'
                + '  <a v-if="isLink(link)" :href="crxLink(link)" target="_blank"><i role="image" class="coral-Icon coral-Icon--editCircle coral-Icon--sizeS" tabindex="0" data-init="quicktip" data-quicktip-arrow="left" data-quicktip-type="info" data-quicktip-content="open in crx/de"></i></a>'
                + '  <span v-else>{{link}}</span>'
                + '</span>'
    });
}());