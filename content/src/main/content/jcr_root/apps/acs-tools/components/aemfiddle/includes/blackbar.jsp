<nav class="endor-Panel-header endor-BlackBar">
    <div class="endor-BlackBar-title">{{ui.getLanguage(data.src.scriptExt)}} <span class="script-extension">.{{data.src.scriptExt}}</span></div>
    <%-- Resource Execution Context --%>
    <input ng-model="data.src.resource"
        type="text"
        placeholder="Absolute resource path"
        class="endor-BlackBar-item coral-Textfield resource"/>
    <button ng-click="app.run('<%= runURL %>')"
        class="endor-BlackBar-item coral-Button coral-Button--primary run-code-button">
        <span ng-hide="data.app.running">Run Code</span>
        <span ng-show="data.app.running">Running Code...</span>
    </button>

    <div class="endor-BlackBar-right">
        <!-- New -->
        <button class="endor-BlackBar-item coral-Button coral-Button--square coral-Button--quiet" data-target="#popover-new" data-toggle="popover">
            <i class="coral-Icon coral-Icon--addCircle"></i>
        </button>
         <button class="js-endor-navrail-toggle endor-BlackBar-nav is-active" title="Toggle Rail"><i class="coral-Icon coral-Icon--navigation"></i></button>
    </div>
</nav>