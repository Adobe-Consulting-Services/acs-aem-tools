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

(function () {
    'use strict';
    Vue.component('clientlib-modal', {
        data: function () {
            return {
                open: false,
                clientlib: {},
                clientlibs: {
                    js: [],
                    css: []
                },
                header: "",
                loading: true,
                error: false
            };
        },
        methods: {
            isEmpty: function (object) {
                return Object.keys(object).length === 0 && object.constructor === Object;
            },
            /**
             * Get more details about an array of clientlibraries using their paths
             */
            getClientlibDetails: function (libs, type, callback) {
                var that = this,
                    returnLibs = [],
                    promises = libs.map(function (lib) {
                        return Vue.getClientlibs({ path: lib.path, type: type });
                    });
                return axios.all(promises)
                    .then(axios.spread(function () {
                        var responses = Array.prototype.slice.call(arguments);
                        responses.forEach(function (response, i) {
                            returnLibs[i] = Object.assign({}, libs[i], response.data, {types: ""});
                        });
                        callback(returnLibs);
                    }))['catch'](function (e) { // jslint did not like .catch (reserved word).. smh
                        that.loading = false;
                        that.error = true;
                        console.error(e);
                    });
            },
            /**
             *  Open modal that shows a singlr clientlib with a certain path
             */
            openModalByPath: function (data) {
                this.open = data.open;
                this.header = data.header;
                var that = this,
                    promises = [
                        Vue.getClientlibs(Object.assign({}, data.params, { type: "JS" })),
                        Vue.getClientlibs(Object.assign({}, data.params, { type: "CSS" }))
                    ];
                axios.all(promises)
                    .then(axios.spread(function () {
                        var jsClientLibs = arguments[0].data,
                            cssClientLibs = arguments[1].data;

                        jsClientLibs = that.isEmpty(jsClientLibs) ? [] : [jsClientLibs].map(Vue.toKeyValArray);
                        cssClientLibs = that.isEmpty(cssClientLibs) ? [] : [cssClientLibs].map(Vue.toKeyValArray);
                        
                        that.$set(that.clientlibs, 'js', jsClientLibs);
                        that.$set(that.clientlibs, 'css', cssClientLibs);
                        that.loading = false;
                    }))['catch'](function (e) { // jslint did not like .catch (reserved word).. smh
                        that.loading = false;
                        that.error = true;
                        console.error(e);
                    });
            },
            /**
             *  Open modal that shows all clientlibs with a certain category
             */
            openModalByCategory: function (data) {
                this.open = data.open;
                this.header = data.header;
                this.loading = true;
                var that = this;
                // get all clientlibs with catigory
                Vue.getClientlibs(data.params)
                    .then(function (response) {
                        var promises = [
                            that.getClientlibDetails(response.data, "JS", function (jsLibs) {
                                that.$set(that.clientlibs, 'js', jsLibs.map(Vue.toKeyValArray));
                            }),
                            that.getClientlibDetails(response.data, "CSS", function (cssLibs) {
                                that.$set(that.clientlibs, 'css', cssLibs.map(Vue.toKeyValArray));
                            })
                        ];
                        // once all promises are resolved, hide loading animation
                        axios.all(promises)
                            .then(axios.spread(function () {
                                that.loading = false;
                            }));
                    })['catch'](function (e) { // jslint did not like .catch (reserved word).. smh
                        that.loading = false;
                        that.error = true;
                        console.error(e);
                    });
            }
        },
        created: function () {
            // react to published events to open modal component with different options
            // The events are published primarely from the table component, but can be published 
            // from anywhere in the app
            this.eventHub.$on('open-modal-path', this.openModalByPath);
            this.eventHub.$on('open-modal-category', this.openModalByCategory);
        }
    });
}());
