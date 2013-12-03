<%@include file="/libs/foundation/global.jsp" %><%
%><%@page session="false"%>

<%-- Header --%>
<header class="top">

    <%-- Logo --%>
    <div class="logo">
        <span ng-hide="data.execution.running"><a href="/"><i class="icon-marketingcloud medium"></i></a></span>
        <span ng-show="data.execution.running"><span class="spinner"></span></span>
    </div>

    <%-- Limited Breadcrumbs --%>
    <nav class="crumbs">
        <a href="/miscadmin">Tools</a>
        <a href="<%= currentPagePath %>.html">AEM Fiddle</a>
    </nav>

    <div class="drawer">
        <%-- Resource Execution Context --%>
        <input ng-model="data.execution.params.resource"
               type="text"
               placeholder="Absolute path to resource"
               class="resource"/>

        <%-- Run Code Button --%>
        <button ng-click="app.run('<%= runURL %>', '<%= executeURL %>')"
                class="primary run-code-button">
            <span ng-hide="data.execution.running">Run Code</span>
            <span ng-show="data.execution.running">Running Code...</span>
        </button>

        <span class="divider"></span>

        <%-- Rail Toggle Button --%>
        <a ng-click="ui.toggleRail()"
              class="toggle-rail-button medium icon-viewlist">Show/Hide MyFiddles</a>
    </div>

</header>