<%--
  #%L
  ACS AEM Tools Package
  %%
  Copyright (C) 2014 Adobe
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
    pageContext.setAttribute("favicon", resourceResolver.map("/libs/cq/tagging/content/tagadmin.ico"));
    pageContext.setAttribute("pagePath", resourceResolver.map(currentPage.getPath()));
    pageContext.setAttribute("resourcePath", resourceResolver.map(resource.getPath()));

    pageContext.setAttribute("fileApiJS", resourceResolver.map("/etc/clientlibs/acs-tools/vendor/FileAPI.min.js"));
    pageContext.setAttribute("fileApiSWF",
            resourceResolver.map("/etc/clientlibs/acs-tools/vendor/FileAPI.min.js/FileAPI.flash.swf"));

%><!doctype html>
<html>
    <head>
        <meta charset="UTF-8">
        <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">

        <title>Tag Maker | ACS AEM Tools</title>

        <link rel="shortcut icon" href="${favicon}"/>

        <script>
            // Need to be loaded before angular-file-upload-shim(.min).js
            FileAPI = {
                jsUrl: '${fileApiJS}',
                flashUrl: '${fileApiSWF}'
            }
        </script>

        <cq:includeClientLib css="acs-tools.tag-maker.app"/>
    </head>

    <body id="acs-tools-tag-maker-app">

        <header class="top">

            <div class="logo">
                <a href="/"><i class="icon-marketingcloud medium"></i></a>
            </div>

            <nav class="crumbs">
                <a href="/miscadmin">Tools</a>
                <a href="${pagePath}.html">Tag Maker</a>
            </nav>
        </header>

        <div class="page" role="main"
             ng-controller="MainCtrl"
             ng-init="app.uri = '${resourcePath}';init();">

            <div class="content">
                <div class="content-container">
                    <div class="content-container-inner">

                        <cq:include script="includes/notifications.jsp"/>

                        <h1>Tag Maker</h1>

                        <cq:include script="includes/form.jsp"/>

                        <cq:include script="includes/results.jsp"/>

                    </div>
                </div>
            </div>

        <cq:includeClientLib js="acs-tools.tag-maker.app"/>

        <%-- Register angular app; Decreases chances of collisions w other angular apps on the page (ex. via injection) --%>
        <script type="text/javascript">
            angular.bootstrap(document.getElementById('acs-tools-tag-maker-app'),
                    ['tagMakerApp']);
        </script>
    </body>
</html>