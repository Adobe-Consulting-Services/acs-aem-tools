<%@include file="/libs/foundation/global.jsp" %><%
%><%@page session="false"%>

<%-- Pane containing rendered output and output HTML view --%>

<div id="output">

    <%-- Output Toolbar and Status --%>
    <nav class="toolbar">
        <div class="left icongroup">
            <a  ng-click="ui.toggleOutput()"
                class="toggle-output-icon icon-reply"
                href="#toggle-output"
                title="Toggle Output/HTML">Toggle Output/HTML</a>

            <span class="divider"></span>

            <span class="output-status">
                <span ng-show="data.ui.output.hasData">
                    Executed at
                    [ {{data.execution.result.executedAt | date:'h:mm:ss a'}} ]
                    against
                    [ {{data.execution.result.resource}} ]
                </span>

                <span ng-hide="data.ui.output.hasData">
                    <-- Click to toggle HTML source/normal output!
                </span>
            </span>
        </div>
    </nav>

    <%-- Code Output --%>
    <div id="ace-output" class="output-html" ng-show="data.ui.output.htmlView"></div>

    <div class="output-rendered" ng-hide="data.ui.output.htmlView">

        <%-- Initial Welcome Message --%>
        <div ng-show="!data.ui.output.hasData">
            <div ng-show="!data.execution.count"
                 class="welcome">

                <h1>Welcome to AEM Fiddle!</h1>

                <p>
                    AEM Fiddle lets you write code, execute it, and immediately see the results!
                </p>

                <p>
                    Say goodbye to long Apache Maven builds, creating throw-away components in CRXDE Lite, and random &quot;test
                    pages&quot;.
                </p>

                <p>
                <ol>
                    <li>Simply write some JSP code in the editor screen
                        <br/>(<-- over there)</li>
                    <li>click Run Code (up above)</li>
                    <li>and see the result of the execution right here!</li>
                </ol>
                </p>

                <p>
                    You can also Create, Load, Update and Delete code snippets by clicking on the
                    <span class="icon-viewlist"></span>
                    in the header!
                </p>
            </div>

            <%-- Initial Welcome Message --%>
            <div ng-show="data.execution.count"
                 class="placeholder-bg">
            </div>
         </div>

        <div ng-bind-html="data.execution.result.data" ng-show="data.ui.output.hasData"></div>
    </div>
</div>
