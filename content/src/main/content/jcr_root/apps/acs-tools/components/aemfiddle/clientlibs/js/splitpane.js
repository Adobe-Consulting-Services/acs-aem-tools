/*
 * Copyright 2012 david gonzalez.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

/*global editor: false, outputSrc: false */
$(function () {
    var MIN_WIDTH = 250,
        $left = $('#editor-wrapper'),
        $right = $('#output-wrapper'),
        $window = $(window);

    function resize(event, ui) {
        var percent = (ui.size.width / $window.width()) * 100;

        $left.css('right', (100 - percent) + '%');
        $right.css('left', percent + '%');

        $left.data('width', 100 - percent);
        $right.data('width', percent);

        resizeOutputHeader();
    };

    $left.resizable({ handles: 'e', resize: resize, stop: stop });

    reset();



    function stop(event, ui) {
        var leftPercent = ((ui.size.width / $window.width())) * 100 ;
        $left.css('right', (100 - leftPercent) + '%');

        var rightPercent = ($left.width() / $window.width()) * 100;
        $right.css('left', rightPercent + '%');

        $left.data('width', leftPercent);
        $right.data('width', rightPercent);

        editor.resize();
        outputSrc.resize(true);

        reset();
    };

    function resizeOutputHeader() {
        var width = $right.width() - 100;
        $('.output-status, .output-status-empty').css('width', width + 'px');
    };

    function reset() {
        var maxWidth = $window.width() - MIN_WIDTH;

        $left.resizable("option", "maxWidth", maxWidth);
        $left.resizable("option", "minWidth", MIN_WIDTH);

        $left.css('height', $right.height()).css('bottom', 0);

        resizeOutputHeader();
    };

    $(window).resize(function() {
        // Set to auto to allow Window.resize to work
        $left.css('width', 'auto');

        if($window.width() < MIN_WIDTH * 2) {
            // Hide Right Pane and Handle
            if(!$right.data('hidden')) {
                var $handle = $('.ui-resizable-handle');

                $right.hide();
                $right.data('hidden', true);
                $handle.hide();
                $left.css('right', 0);
            }
        } else {
            // Re-Show Right Pane and Handle
            if($right.data('hidden')) {
                var $handle = $('.ui-resizable-handle');

                var leftPercent = $left.data('percent');
                var rightPercent = $right.data('percent');

                $left.css('right', leftPercent + '%');
                $right.css('left', rightPercent + '%');
                $right.show();
                $right.data('hidden', false);

                $handle.show();

                editor.resize();
                outputSrc.resize();
            }
        }

        reset();
    });
});
