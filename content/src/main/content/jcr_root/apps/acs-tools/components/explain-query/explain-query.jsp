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
%><%@page session="false"
          import="com.adobe.acs.tools.util.AEMCapabilityHelper" %><%

    final AEMCapabilityHelper aemCapabilityHelper = sling.getService(AEMCapabilityHelper.class);

    pageContext.setAttribute("isSupported", aemCapabilityHelper.isOak());
    pageContext.setAttribute("pagePath", resourceResolver.map(currentPage.getPath()));
    pageContext.setAttribute("resourcePath", resourceResolver.map(resource.getPath()));

%><!doctype html>
<html>
<head>
    <meta charset="UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">

    <title>Explain Query | ACS AEM Tools</title>

    <cq:includeClientLib css="acs-tools.explain-query.app"/>
</head>

<body>
    <div id="acs-tools-explain-query-app">

        <header class="top">

            <div class="logo">
                <a href="/"><i class="icon-marketingcloud medium"></i></a>
            </div>

            <nav class="crumbs">
                <a href="/miscadmin">Tools</a>
                <a href="${pagePath}.html">Explain Query</a>
            </nav>
        </header>

        <div class="page" role="main"
             ng-controller="MainCtrl"
             ng-init="app.uri = '${resourcePath}.explain.json'; init();">

            <div class="content">
                <div class="content-container">
                    <div class="content-container-inner">

                        <h1>Explain Query</h1>

                        <c:choose>
                            <c:when test="${isSupported}">

                                <p>Find the query plan used for executing any Query</p>

                                <div ng-show="app.running">
                                    <div class="alert notice">
                                        <strong>Running</strong>
                                        <div>Please be patient. Large or expensive queries may cause longer
                                            explanation times.</div>
                                    </div>
                                </div>

                                <cq:include script="includes/notifications.jsp"/>

                                <cq:include script="includes/form.jsp"/>

                                <cq:include script="includes/explanation.jsp"/>

                                <cq:include script="includes/slow-queries.jsp"/>

                                <cq:include script="includes/popular-queries.jsp"/>

                                <cq:includeClientLib js="acs-tools.explain-query.app"/>

                                <%-- Register angular app; Decreases chances of collisions w other angular apps on the page (ex. via injection) --%>
                                <script type="text/javascript">
                                    angular.bootstrap(document.getElementById('acs-tools-explain-query-app'),
                                            ['explainQueryApp']);
                                </script>

                            </c:when>
                            <c:otherwise>

                                <div class="alert notice large">
                                    <strong>Incompatible version of AEM</strong>

                                    <div>Explain Query is only supported on AEM installs running Apache Jackrabbit Oak based
                                        repositories.
                                    </div>
                                </div>

                            </c:otherwise>
                        </c:choose>

                    </div>
                </div>
            </div>
        </div>
    </div>
</body>
</html>