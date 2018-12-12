<coral-shell-header-actions class="header-actions">

    <div class="header-item">{{ui.getLanguage(data.src.scriptExt)}}
        <span class="script-extension">.{{data.src.scriptExt}}</span>
    </div>

    <%-- Resource Execution Context --%>

    <div class="header-item">
        <input ng-model="data.src.resource"
               type="text"
               placeholder="Absolute resource path"
               class="coral-Textfield resource"/>
    </div>

    <div class="header-item">
        <button ng-click="app.run('<%= runURL %>')"
                class="coral-Button coral-Button--primary run-code-button">
            <span ng-hide="data.app.running">Run Code</span>
            <span ng-show="data.app.running">Running Code...</span>
        </button>
    </div>

    <div class="header-item">
        <!-- New -->
        <button class="coral-Button coral-Button--square coral-Button--quiet"
                data-target="#popover-new"
                data-toggle="popover">
            <i class="coral-Icon coral-Icon--addCircle"></i>
        </button>

        <button class="coral-Button coral-Button--square coral-Button--quiet"
                data-target="#popover-new-rail"
                data-toggle="popover">
            <i class="coral-Icon coral-Icon--navigation"></i>
        </button>

    </div>

</coral-shell-header-actions>
