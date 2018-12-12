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
<%@include file="/libs/foundation/global.jsp" %>
<%@page session="false"
        import="com.adobe.granite.security.user.UserProperties,
                com.adobe.granite.security.user.UserPropertiesManager,
                org.apache.jackrabbit.api.security.user.Authorizable,
                org.apache.sling.api.resource.Resource" %><%

    final String SAVE_TO = "fiddles";

    final UserPropertiesManager upm = resourceResolver.adaptTo(UserPropertiesManager.class);
    final Authorizable authorizable = resourceResolver.adaptTo(Authorizable.class);
    final UserProperties userProperties = upm.getUserProperties(authorizable, "profile");

    /* App Data */
    final String resourcePath = resourceResolver.map(slingRequest, resource.getPath());
    final String runURL = resourcePath + ".run.html";
    final String myFiddlesPath = resourceResolver.map(slingRequest,
            userProperties.getNode().getPath() + "/" + SAVE_TO);
    final String currentPagePath = resourceResolver.map(slingRequest, currentPage.getPath());

    /* ACE JS Base path */
    final String aceBasePath = resourceResolver.map(slingRequest, "/etc/clientlibs/acs-tools/vendor/aceeditor");

    /* Favicon */
    final String faviconPath = resourceResolver.map(slingRequest, component.getPath() + "/clientlibs/images/favicon.png");
%>
<!doctype html>
<html class="coral-App">
    <head>
        <meta charset="UTF-8">
        <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">

        <title>AEM Fiddle | ACS AEM Tools</title>
        <link rel="shortcut icon" href="<%= faviconPath %>"/>

        <cq:includeClientLib css="aemfiddle.app"/>
    </head>


    <body class="coral--light" id="acs-tools-aemfiddle-app" ng-controller="MainCtrl">
        <%@include file = "includes/header.jsp" %>

        <%@include file="includes/rail.jsp" %>

        <div class="" role="main">
            <div id="left-pane">
                <%@include file="includes/input.jsp" %>

                <%-- Resize strip/handle; Requires classes: 'ui-resizable-handle' and 'ui-resizable-e' --%>
                <div id="handle" class="ui-resizable-handle ui-resizable-e"></div>
            </div>

            <div id="right-pane">
                <%@include file="includes/output.jsp" %>
            </div>
        </div>

        <%@include file="includes/notifications.jsp" %>

        <cq:includeClientLib js="jquery,jquery-ui,aemfiddle.app"/>

        <div id="popover-new" class="coral-Popover">
            <div class="coral-Popover-content u-coral-padding">
                <ul class="coral-List coral-List--minimal">
                    <li  class="coral-List-item" ng-repeat="option in data.ui.scriptExtOptions">
                        <a ng-click="app.new(option.value, false)"
                           href="#new-{{option.value}}">{{option.label}} <span
                                class="script-extension">.{{option.value}}</span></a>
                    </li>
                </ul>
            </div>
        </div>
        <div id="app-data"

             data-ace-editor-base-path="<%= aceBasePath %>"
             data-run-url="<%= runURL %>"
             data-resource-path="<%= resourcePath %>"
             data-myfiddles-path="<%= myFiddlesPath %>"
             data-current-page-path="<%= currentPagePath %>"
        ></div>
        <%-- Register angular app; Decreases chances of collisions w other angular apps on the page (ex. via injection) --%>

        <script type="text/javascript">
            angular.bootstrap(document.getElementById('acs-tools-aemfiddle-app'), ['aemFiddle']);
        </script>
    </body>
</html>