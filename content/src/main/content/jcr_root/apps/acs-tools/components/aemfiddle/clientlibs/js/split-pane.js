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

/*global aemFiddle: false, moment: false, angular: false, confirm: false */
$(function () {
    var MIN_WIDTH = 250,
        $left = $('#left-pane'),
        $right = $('#right-pane'),
        $window = $(window);

    function resizeOutputHeader() {
        var width = $right.width() - 100;
        $('.output-status').css('width', width + 'px');
    }

    function reset() {
        var maxWidth = $window.width() - MIN_WIDTH;

        $left.resizable("option", "maxWidth", maxWidth);
        $left.resizable("option", "minWidth", MIN_WIDTH);

        $left.css('height', $right.height()).css('bottom', 0);

        resizeOutputHeader();
    }

    function resize(event, ui) {
        if($right.data('hidden')) { return; }

        var percent = (ui.size.width / $window.width()) * 100;

        $left.css('right', (100 - percent) + '%');
        $right.css('left', percent + '%');

        /* Widths */
        $left.data('width', 100 - percent);
        $right.data('width', percent);

        resizeOutputHeader();
    }

    function stop(event, ui) {
        var leftPercent, rightPercent;
        if($right.data('hidden')) { return; }

        leftPercent = ((ui.size.width / $window.width())) * 100 ;
        $left.css('right', (100 - leftPercent) + '%');

        rightPercent = ($left.width() / $window.width()) * 100;
        $right.css('left', rightPercent + '%');

        /* Widths */
        $left.data('width', leftPercent);
        $right.data('width', rightPercent);

        aemFiddle.ace.output.editor.resize();
        aemFiddle.ace.input.editor.resize(true);

        reset();
    }

    $left.resizable({ handles: 'e', resize: resize, stop: stop });

    $(window).resize(function() {
        var $handle;
        // Set to auto to allow Window.resize to work
        $left.css('width', 'auto');

        if($window.width() < MIN_WIDTH * 2) {
            // Hide Right Pane and Handle
            if(!$right.data('hidden')) {
                $handle = $('#handle');

                $right.hide();
                $right.data('hidden', true);
                $handle.css('display', 'none');
                $left.css('right', 0);
            }
        } else {
            // Re-Show Right Pane and Handle
            if($right.data('hidden')) {
                $handle = $('#handle');

                $left.css('right', '50%');
                $right.css('left', '50%');
                $right.show();
                $right.data('hidden', false);

                $handle.css('display', 'block');

                aemFiddle.ace.output.editor.resize();
                aemFiddle.ace.input.editor.resize();
            }
        }

        reset();
    });
});
