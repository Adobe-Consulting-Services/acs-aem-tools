/*
 * #%L
 * ACS AEM Tools Package
 * %%
 * Copyright (C) 2013 Adobe
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

/*global angular: false, ace: false */
var aemFiddle = angular.module('aemFiddle',['ngSanitize']).config(function($sceProvider) {
    // Completely disable SCE.
    $sceProvider.enabled(false);
});


/* ACE Editors */

aemFiddle.ace = {
    input: {
        editor: ace.edit("ace-input"),
        init: function() {
            aemFiddle.ace.input.editor.setTheme("ace/theme/vibrant_ink");
            aemFiddle.ace.input.editor.getSession().setMode("ace/mode/jsp");
            aemFiddle.ace.input.editor.setDisplayIndentGuides(true);
            aemFiddle.ace.input.editor.gotoLine(12);
            aemFiddle.ace.input.editor.commands.addCommand({
                name: 'RunCodeCommand',
                bindKey: { win: 'Ctrl-S', mac: 'Command-K' },
                exec: function (editor) {
                    var runURL = $('#app-data').data('run-url'),
                    executeURL = $('#app-data').data('execute-url');
                    angular.element($('body')).scope().app.run(runURL, executeURL);

                 },
                readOnly: true // false if this command should not apply in readOnly mode
            });
        },
        load: function(data) {
            aemFiddle.ace.input.editor.setValue(data);
            aemFiddle.ace.input.editor.scrollToLine(0);
            aemFiddle.ace.input.editor.scrollToRow(0);
            aemFiddle.ace.input.editor.gotoLine(12, 0, false);
            aemFiddle.ace.input.editor.getSelection().clearSelection();
        }
    },
    output: {
        editor: ace.edit("ace-output"),
        init: function() {
            aemFiddle.ace.output.editor.setTheme("ace/theme/chrome");
            aemFiddle.ace.output.editor.getSession().setMode("ace/mode/html");
            aemFiddle.ace.output.editor.setReadOnly(true);
            aemFiddle.ace.output.editor.setValue(' ');
            aemFiddle.ace.output.editor.gotoLine(1);
        },
        load: function(data) {
            if(!data) {
                // Set empty data to a space so editor displays
                data = ' ';
            }

            aemFiddle.ace.output.editor.setValue(data);
            aemFiddle.ace.output.editor.scrollToLine(0);
            aemFiddle.ace.output.editor.scrollToRow(0);
            aemFiddle.ace.output.editor.gotoLine(11, 0, false);
            aemFiddle.ace.output.editor.getSelection().clearSelection();
        },
        reload: function() {
            aemFiddle.ace.output.editor.setValue(aemFiddle.ace.output.editor.getValue());
        }
    }
};

aemFiddle.ace.input.init();
aemFiddle.ace.output.init();

window.onbeforeunload = function () {
    return "Leaving this page will cause you to lose any unsaved code you've entered.";
};