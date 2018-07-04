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
/*jslint this: true */
/*jslint browser:true */

/**
 * Built this to filters a large array based on predicates.
 * It provides a declarative way to filter an array of objects whose properties are strings.
 * But it can be adapted and improved for other types.
 * I created a gist for it here: https://gist.github.com/ahmed-musallam/5b8c4f97b85e76e576203e284c0fb820
 */
(function () {
    'use strict';
    Array.prototype.filterWithPredicates = function (predicates, operator) {

        // no predicates, return all
        if (!predicates) {
            return this;
        }

        // determine predicate evaluation operation
        if (operator && operator === "OR") {
            operator = 'some';
        } else {
            operator = 'every';
        }

        return this.filter(function (item) {
            return predicates
                // test predicates against array item and return an predicateEvaluation array
                .map(function (predicate) {
                    var value = item[predicate.property];
                    // item is array, check if atleast one element matches
                    if (Array.isArray(value)) {
                        return value.some(function (valueItem) {
                            return valueItem.indexOf && valueItem.indexOf(predicate.match) > -1;
                        });
                    }
                    else if (value.indexOf) {
                        return value.indexOf(predicate.match) > -1;
                    }
                // use array some/every depending on operator to determine OR/AND operation
                })[operator](function (predicateEvaluation) {
                    return predicateEvaluation === true;
                });
        });
    };
}());