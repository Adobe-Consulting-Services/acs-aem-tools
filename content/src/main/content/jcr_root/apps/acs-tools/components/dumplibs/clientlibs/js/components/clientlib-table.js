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
Vue.component('clientlib-table', {
    data: function () {
        'use strict';
        return {
            clientlibs: [],
            filteredClientlibs: [],
            filters: {
                path: "",
                categories: "",
                types: "",
                channels: ""
            },
            loading: true,
            error: false
        };
    },
    watch: {
        filters: {
            handler: function (filters) {
                'use strict';
                this.debouncedFilterClientlibs(filters);
            },
            deep: true
        }
    },
    methods: {
        filterClientlibs: function (filters) {
            'use strict';
            console.log("filtering clibs");

            if (!this.clientlibs) {
                return;
            }

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
        openModalByPath: function (clientlib, header, params) {
            'use strict';
            this.eventHub.$emit('open-modal-path', {open: true, header: header, clientlib: clientlib, params: params});
        },
        openModalByCategory: function (clientlib, header, params) {
            'use strict';
            this.eventHub.$emit('open-modal-category', {open: true, header: header, clientlib: clientlib, params: params});
        }
    },
    mounted: function () {
        'use strict';
        var that = this;
        this.debouncedFilterClientlibs = Vue.debounce(this.filterClientlibs, 300, false);
        axios.get('/bin/acs-tools/dumplibs.json')
            .then(function (response) {
                // JSON responses are automatically parsed.
                that.clientlibs = response.data.slice(0);
                that.debouncedFilterClientlibs();
                that.loading = false;
            })
            .catch(function (e) {
                that.loading = false;
                that.error = true;
                console.error(e);
            });
    }
});