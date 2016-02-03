<%--
  #%L
  ACS AEM Tools Package
  %%
  Copyright (C) 2013 Adobe
  %%
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
  
       http://www.apache.org/licenses/LICENSE-2.0
  
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
  #L%
  --%>
<%@include file="/libs/foundation/global.jsp" %><%
%><%@page session="false"%>

<%-- Pane containing rendered output and output HTML view --%>

<div id="output" class="{{!data.result.success ? 'output-error' : ''}}">

    <%-- Output Toolbar and Status --%>
    <nav class="toolbar">
        <div class="icongroup">
            <a  ng-click="ui.toggleOutput()"
                class="toggle-output-icon"
                href="#toggle-output"
                title="Toggle Output/HTML"><i class="coral-Icon coral-Icon--reply" data-init="quicktip" data-quicktip-content="Toggle Output/HTML"></i></a>
        </div>
        <span class="output-status">

            <span ng-show="data.ui.output.hasData">
                Executed at
                [ {{data.result.executedAt | date:'h:mm:ss a'}} ]
                against
                [ {{data.result.resource}} ]
            </span>

            <span ng-hide="data.ui.output.hasData">
                <-- Click to toggle HTML source/normal output!
            </span>
        </span>
    </nav>

    <%-- Code Output --%>
    <div id="ace-output" class="output-html" ng-show="data.ui.output.htmlView"></div>

    <div class="output-rendered" ng-hide="data.ui.output.htmlView">

        <%-- Initial Welcome Message --%>
        <div ng-show="!data.ui.output.hasData">
            <div ng-show="!data.app.count"
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
                    You can also Create, Load, Update and Delete code snippets by clicking on
                    <span class="coral-Icon coral-Icon--navigation coral-Icon--sizeS"></span>
                    in the header!
                </p>
            </div>

            <%-- Initial Welcome Message --%>
            <div ng-show="data.app.count"
                 class="placeholder-bg">
            </div>
         </div>

        <div ng-bind-html="data.result.data" ng-show="data.ui.output.hasData"></div>
    </div>
</div>
