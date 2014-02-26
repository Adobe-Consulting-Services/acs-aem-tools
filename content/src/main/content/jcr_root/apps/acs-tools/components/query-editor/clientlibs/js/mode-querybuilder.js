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

/*global ace: false */

ace.define('ace/mode/querybuilder', function (require, exports, module) {

    var oop = require("ace/lib/oop"),
        TextMode = require("ace/mode/text").Mode,
        QueryBuilderHighlightRules = require("ace/mode/querybuilder_highlight_rules").QueryBuilderHighlightRules;

    function Mode() {
        this.HighlightRules = QueryBuilderHighlightRules;
    }

    oop.inherits(Mode, TextMode);

    exports.Mode = Mode;
});

ace.define('ace/mode/querybuilder_highlight_rules', function (require, exports, module) {

    var oop = require("ace/lib/oop"),
        TextHighlightRules = require("ace/mode/text_highlight_rules").TextHighlightRules;

    function QueryBuilderHighlightRules() {

        var keywordMapper = this.createKeywordMapper({
            "variable.language": "orderby|orderby.sort|p.hits|p.limit|p.nodedepth|p.offset|" + // General
                "timeZone|" + // Date range
                "fulltext|relPath|" + // Fulltext
                "boolproperty|" + // Bool property
                "property|value|and|operation|" + // Property
                "nodename|" + // Nodename
                "path|exact|flat|self|" + // Path
                "group|group.p.or|" + // Group
                "lowerBound|lowerOperation|upperBound|upperOperation|decimal|" + // Range
                "savedquery|" + // Saved query
                "similar|local|" + // Similarity
                "type|" + // Type
                "tagid", // Other

            "constant.language": "full|selective|desc"
        }, "identifier");

        this.$rules = {
            "start": [
                {
                    token: "comment",
                    regex: /(^|\s)(#[\w\W]*)$/
                },
                {
                    token: "keyword.operator",
                    regex: /\=/,
                    next: "value"
                },
                {
                    token: keywordMapper,
                    regex: /^\s*([\w\.]*)/
                },
                {
                    defaultToken: "identifier"
                }
            ],
            "value": [
                {
                    token: "string",
                    regex: /$/,
                    next: "start"
                },
                {
                    token: "constant.language.escape",
                    regex: /\\u[0-9a-fA-F]{4}|\\/
                },
                {
                    defaultToken: "string"
                }
            ]
        };

    }

    oop.inherits(QueryBuilderHighlightRules, TextHighlightRules);

    exports.QueryBuilderHighlightRules = QueryBuilderHighlightRules;
});
