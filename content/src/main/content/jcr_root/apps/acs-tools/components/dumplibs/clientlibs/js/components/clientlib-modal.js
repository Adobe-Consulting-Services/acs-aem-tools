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
Vue.component('clientlib-modal', {
    data: function () {
        'use strict';
        return {
            open: false,
            clientlib: {},
            clientlibs: [],
            header: "",
            loading: true,
            error: false
        }
    },
    methods: {
        getClientlibs: function (params) {
            return axios.get('/bin/acs-tools/dumplibs.json', {
                params: params
            })
        },
        openModalByPath(data) {
            this.open = data.open;
            this.header = data.header;
            var that = this;
            this.getClientlibs(data.params)
            .then(function (response) {
                that.clientlibs = [response.data];
                console.log("openModalByPath - that.clientlibs:", that.clientlibs);
                that.clientlibs = that.clientlibs.map(Vue.toKeyValArray)
                console.log(that.clientlibs)
                that.loading = false;
            })
            .catch(function (e) {
                that.loading = false;
                that.error = true;
                console.error(e);
            });
        },
        openModalByCategory: function(data) {
            this.open = data.open;
            this.header = data.header;
            var that = this;
            this.getClientlibs(data.params)
            .then(function (response) {
                that.clientlibs = response.data;
                var jsPromises = that.clientlibs.map(function (lib) {
                    return that.getClientlibs({path: lib.path, type: "JS"})
                });
                var cssPromises = that.clientlibs.map(function (lib) {
                    return that.getClientlibs({path: lib.path, type: "CSS"})
                });

                axios.all(jsPromises)
                .then(axios.spread((...jsResponses) => {
                    for (let i = 0; i < jsResponses.length; i++) {
                        that.clientlibs[i].js =  jsResponses[i].data
                    }
                }))
                .then(function(){
                    axios.all(cssPromises)
                    .then(axios.spread((...cssResponses) => {
                        for (let i = 0; i < cssResponses.length; i++) {
                            that.clientlibs[i].css =  cssResponses[i].data
                        }
                        
                        that.clientlibs = that.clientlibs.map(Vue.toKeyValArray)
                        that.loading = false;
                    }))
                    .catch(function (e) {
                        that.loading = false;
                        that.error = true;
                        console.error(e);
                    });
                })
                .catch(function (e) {
                    that.loading = false;
                    that.error = true;
                    console.error(e);
                });
            })
            .catch(function (e) {
                that.loading = false;
                that.error = true;
                console.error(e);
            });
        }
    },
    created: function () {
        this.eventHub.$on('open-modal-path', this.openModalByPath)
        this.eventHub.$on('open-modal-category', this.openModalByCategory)
        
    }
});