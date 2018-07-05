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
/*global Vue, axios, console, localStorage */
(function () {
    'use strict';
    Vue.component('clientlib-table', {
        data: function () {
            return {
                clientlibs: [],
                filteredClientlibs: [],
                filters: {
                    path: '',
                    categories: '',
                    types: '',
                    channels: ''
                },
                loading: true,
                error: false
            };
        },
        watch: {
            filters: {
                handler: function (filters) {
                    // store filters to localstorage
                    localStorage.setItem('dumplibs-filters', JSON.stringify(filters));
                    this.debouncedFilterClientlibs(filters);
                },
                deep: true
            }
        },
        methods: {
            filterClientlibs: function (filters) {

                if (!this.clientlibs) {
                    return;
                }

                // no filters, no need to filter; use all clientlibs
                if (!filters) {
                    this.filteredClientlibs = this.clientlibs;
                    return;
                }
                var predicates = Object.keys(filters)
                    // Convert filters to predicates
                    .map(function (filterName) {
                        var filterValue = filters[filterName];
                        if (filterValue) {
                            return {
                                property: filterName,
                                match: filterValue.trim()
                            };
                        } else {
                            return null;
                        }
                    })
                    // filter null predicates
                    .filter(function (predicate) {
                        return !!predicate;
                    });

                this.filteredClientlibs = this.clientlibs.filterWithPredicates(predicates);
            },
            // open the modal with a clienlib based on path
            openModalByPath: function (clientlib, header, params) {
                this.eventHub.$emit('open-modal-path', { open: true, header: header, clientlib: clientlib, params: params });
            },
            // open the modal with a clienlib/s based on category
            openModalByCategory: function (clientlib, header, params) {
                this.eventHub.$emit('open-modal-category', { open: true, header: header, clientlib: clientlib, params: params });
            }
        },
        mounted: function () {
            var that = this,
                filters = localStorage.getItem('dumplibs-filters');
            if (filters) { // load filters from localstorage, if any
                that.filters = JSON.parse(filters);
            }
            // register debounced clientlib filter function
            this.debouncedFilterClientlibs = Vue.debounce(this.filterClientlibs, 300, false);
            // get all clientlibs, then filter them
            Vue.getClientlibs()
            .then(function (response) {
                // JSON responses are automatically parsed.
                that.clientlibs = response.data.slice(0);
                that.filterClientlibs();
                that.loading = false;
                return;
            })['catch'](function (e) { // jslint did not like .catch (reserved word).. smh
                that.loading = false;
                that.error = true;
                console.error(e);
            });
        }
    });
}());