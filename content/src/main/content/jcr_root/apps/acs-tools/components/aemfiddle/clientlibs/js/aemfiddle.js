/*
* Copyright 2013 david gonzalez.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
$(function() {
    var THROTTLE = 1500;

    var $alerts = $('#alerts');
    var $messages = $('#messages');
    var $output = $('#output .inner');
    var $outputSrc = $('#output-src');
    var $rail = $('.rail');
    var $railList = $rail.find('.list');
    var $badge = $('a[href="#list"].badge');
    var $saveTooltip = $('header .save .tooltip');
    var $updateTooltip = $('header .update .tooltip');
    var $cleanTooltip = $('header .clean .tooltip');
    var $saveModal = $('#save-modal');
    var $editorForm = $('#editor-form');
    var $slowDownAlert = $('#slow-down-alert');
    var initalEditorData = editor.getValue();

    var outputStatus = {}
    outputStatus.$message = $('#output-wrapper .output-status');
    outputStatus.$empty = $('#output-wrapper .output-status-empty');
    outputStatus.$time = $('#output-wrapper .output-status .executed-at');
    outputStatus.$resource = $('#output-wrapper .output-status .executed-against');

    var messages = {};
    messages.navigateAway = $messages.find("#navigate-away").text();
    messages.saveSuccess = $messages.find("#save-success").text();
    messages.updateSuccess = $messages.find("#update-success").text();
    messages.removeSuccess = $messages.find("#remove-success").text();
    messages.codePersistFailed = $messages.find("#code-persist-failed").html();
    messages.codeSaveFailed = $messages.find("#code-save-failed").html();
    messages.noSavedFiddles = $messages.find("#no-saved-fiddles").html();
    messages.runCode = $messages.find("#run-code").html();
    messages.runningCode = $messages.find("#running-code").html();
    messages.updateFailure = $messages.find("#update-failure").text();
    messages.cleanSuccess = $messages.find("#clean-success").text();
    messages.initialOutput = $messages.find("#instructions").html();

    var alerts = {};
    alerts.throttle = {};
    alerts.throttle.$notice = $alerts.find('.throttle .notice');

    var links = {};
    links.$update = $('a[href="#update"]');
    links.$clean = $('a[href="#clean"]');
    links.$list = $('a[href="#list"]');
    links.$run = $('a[href="#run"]');
    links.$save = $('a[href="#save"]');
    links.$cancelSave = $('a[href="#cancel-save"]');
    links.$confirmSave = $('a[href="#confirm-save"]');
    links.$toggleOutput = $('a[href="#toggle-output"]');


    /* Events */

    links.$list.toggle(function () {
        /* Show */
        list($(this).data('url'));
        highlightActiveLoad(links.$update.data('url'));

        $rail.show().animate({ right: '0' }, 100);
        return false;
    }, function() {
        /* Hide */
        $rail.animate({ right: '-16rem' }, 100,
            function() {
                $rail.hide()
            }
        );
        return false;
    });

    links.$toggleOutput.click(function () {
        $output.toggle();
        $outputSrc.toggle();
        return false;
    });

    links.$clean.click(function () {
        clearUpdate();
        clearOutputStatus();
        unhighlightActiveLoad();

        loadAceEditor(outputSrc, messages.initialOutput, 1, false);
        loadAceEditor(editor, initalEditorData, ACE_EDITOR_LINE, false);

        $output.html(messages.initialOutput);

        showTooltip($cleanTooltip, 'success', messages.cleanSuccess);
        return false;
    });

    links.$update.click(function () {
        update($(this).data('url'));
        return false;
    });

    links.$run.click(function() {
        run($editorForm);
        return false;
    });

    links.$save.toggle(function () {
        $saveModal.show();
        return false;
    }, function() {
        $saveModal.hide();
        clearInput($saveModal.find('input[type="text"]'));
        return false;
    });

    links.$cancelSave.click(function () {
        $saveModal.hide();
        clearInput($saveModal.find('input[type="text"]'));
        return false;
    });

    links.$confirmSave.click(function () {
        var url = $(this).data('url');
        var title = $saveModal.find('input[name="jcr:title"]').val();

        save($editorForm, url, title);
        list($('a[href="#list"]').data('url'));

        $saveModal.hide();
        return false;
    });

    $('body').on('submit', '#editor-form', function () {
        run($(this));
        return false;
    });

    $('body').on('click', 'a[href="#load"]', function () {
        load($(this).data('url'));
        highlightActiveLoad($(this).data('url'));
        return false;
    });

    $('body').on('click', 'a[href="#remove"]', function () {
        remove($(this).data('url'));
        list(links.$list.data('url'));
        return false;
    });

    window.onbeforeunload = function () {
        return messages.navigateAway;
    }

    /* Functionality */

    /**
     * Excutes the code in the editor
     *
     * @param $form
     */
    var lastRun = 0;
    function run($form) {
        if((new Date().getTime() - lastRun) < THROTTLE) {
            showAlert(alerts.throttle.$notice);

            return;
        }

        /* Collect content from editor form */
        var action = $form.attr('action');

        /* Save Ace editor data to the Form for submission */
        // Do not create a new line for this, as it will mess up exception line matching with ace editor
        var content = "<%-- File created by AEM Fiddle --%>" + editor.getValue();
        $form.find('input[data-jcr-data]').val(content);
        $form.find('input[data-last-modified]').val(moment().format());

        /* Save the code via Sling POST Servlet to CRX */
        $.post(action, $form.serialize()).success(function (data) {
            runningCodeOn();
            var url = $form.data('reload') + "?t=" + new Date().getTime();

            var resourcePath = $('input[data-resource]').val();

            /* Execute the submitted code */
            $.get(url, { resource: resourcePath }, function (data) {
                /* Write the output of the submitted code to the #output panel */
                updateOutputStatus(resourcePath);

                $output.html(data.trim());
                loadAceEditor(outputSrc, data, 1, true);

                runningCodeOff();
            });

            lastRun = new Date().getTime();
        }).fail(function () {
            /* Error occurred while storing the code to CRX */
            $output.html(messages.codePersistFailed);
        });
    }

    /**
     * Saves the code in the editor to profile node
     *
     * @param $form
     * @param url
     */
    function save($form, url, title) {
        if(!title) {
            title = moment().format('M/D/YYYY, h:mm:ss a');
        }

        var data = {
            ":http-equiv-accept" : "application/json",
            "jcr:primaryType" : "nt:unstructured",
            "jcr:title" : title.trim(),
            "jcr:created" : moment().format(),
            "jcr:created@TypeHint" : "Date",
            "code" : editor.getValue()
        }

        /* Save the code via Sling POST Servlet to CRX */
        $.post(url, data).success(function (data) {
            list(links.$list.data('url'));
            setUpdate(data.path);
            showTooltip($saveTooltip, 'success', messages.saveSuccess);
        }, "json").fail(function () {
            /* Error occurred while storing the code to CRX */
            $output.html(messages.codeSaveFailed);
        });
    }

    function remove(url) {
        var data = {
            ":operation" : "delete"
        };

        var doDelete = confirm("Are you sure you want to delete this fiddle?");

        if(doDelete === true) {
            $.post(url, data).success(function(data) {
                list(links.$list.data('url'));
                if(url === links.$update.data('url')) {
                    clearUpdate();
                }
                showTooltip($saveTooltip, 'notice', messages.removeSuccess);
            });
        }
    }


    function update(url) {
        if(!url) {
            showTooltip($updateTooltip, 'error', messages.updateFailure);
            return;
        }

        var data = {
            "code": editor.getValue()
        };

        $.post(url, data).success(function (data) {
            showTooltip($updateTooltip, 'success', messages.updateSuccess);
        });
    }


    /**
     * Loads code from saved fiddles into the editor
     *
     * @param url
     */
    function load(url) {
        $.get(url + "/code", function (data) {
            loadAceEditor(editor, data, 1, false);

            setUpdate(url);
            clearOutputStatus();
        });
    }

    /**
     * Lists all saved fiddles
     *
     * @param url
     */
    function list(url) {
        var postUrl = url + ".1.json?t=" + new Date().getTime();

        $.get(postUrl, function (data) {
            /* Write the output of the submitted code to the #output panel */
            var $list = $('.rail .list');
            $list.html('');

            var count = 0;
            $.each(data, function(key, value) {
                if(typeof value !== 'object') { return; }
                var path = url + '/' + key;
                /* Gross use of markup in JS; should use moustache; oh well */
                var $entry = $('<section>'
                    + '<h4><a href="#load" data-url="' + path + '">'
                    + value['jcr:title']
                    + '</a>'
                    + '<a href="#remove" data-url="' + path + '" class="icon-minus-circle">Remove</a>'
                    +'</h4>'
                    + '</section>');

                $list.prepend($entry);
                count++;
            });

            if(count < 1) {
                $list.prepend(messages.noSavedFiddles);
            } else {
                highlightActiveLoad(links.$update.data('url'));
            }

            updateBadgeCount(count);
        }, "json");
    }

    function loadAceEditor(aceEditor, data, line, trim) {
        if(trim) {
            data = data.trim();
        }
        aceEditor.setValue(data);
        aceEditor.scrollToLine(0);
        aceEditor.scrollToRow(0);
        aceEditor.gotoLine(line, 0, false);
        aceEditor.getSelection().clearSelection();
    }

    function setUpdate(url) {
        links.$update.data('url', url);
        links.$update.removeClass('disabled');
    }

    function clearUpdate() {
        links.$update.data('url', '');
        links.$update.addClass('disabled');
    }

    function clearInput($input) {
        $input.val('');
    }

    function showTooltip($tooltip, style, message) {
        $tooltip.removeClass('error notice success').addClass(style);
        $tooltip.text(message);
        $tooltip.show().delay(1500).fadeOut();
    }

    /* Displays Running Code Status */

    function updateBadgeCount(count) {
        if(count < 1) {
            $badge.addClass('empty');
        } else {
            $badge.removeClass('empty');
        }
        $badge.text(count);
    }

    function runningCodeOn() {
        links.$run.text(messages.runningCode);
    }

    function runningCodeOff() {
        links.$run.text(messages.runCode);
    }

    function highlightActiveLoad(url) {
        unhighlightActiveLoad();
        $railList.find('a[data-url="' + url + '"]').addClass('active');
    }

    function unhighlightActiveLoad() {
        $railList.find('a').removeClass('active');
    }

    function updateOutputStatus(resourcePath) {
        outputStatus.$time.text(moment().format('h:mm:ss a'));

        if (!resourcePath) {
            resourcePath = outputStatus.$resource.data('default');
        }
        outputStatus.$resource.text(resourcePath);
        outputStatus.$empty.hide();
        outputStatus.$message.show();
        outputStatus.$message.css('display', 'block');
    }

    function clearOutputStatus(resourcePath) {
        outputStatus.$message.hide();
        outputStatus.$empty.show();
    }

    function showAlert($alert) {
        if(!$alert.data('visible')) {
            $alert.data('visible', true);
            $alert.fadeIn(250).delay(3250).fadeOut(1000, function() {
                $(this).data('visible', '');
            });
        }
    }

    /* Initialization */

    $('#editor').show();
    $output.html(messages.initialOutput);
    loadAceEditor(outputSrc, messages.initialOutput, 1, false);

});

/* Initialize Ace Editor */
var ACE_EDITOR_LINE = 11;
var editor = ace.edit("editor");
editor.setTheme("ace/theme/vibrant_ink");
editor.getSession().setMode("ace/mode/jsp");
editor.setDisplayIndentGuides(true);
editor.gotoLine(ACE_EDITOR_LINE);

editor.commands.addCommand({
    name: 'RunCodeCommand',
    bindKey: {win: 'Ctrl-K', mac: 'Command-K'},
    exec: function (editor) {
        $('#editor-form').submit();
    },
    readOnly: true // false if this command should not apply in readOnly mode
});

/* Initialize Output HTML Src editor */
var outputSrc = ace.edit("output-src");
outputSrc.setReadOnly(true);
outputSrc.getSession().setMode("ace/mode/html");
outputSrc.setTheme("ace/theme/chrome");