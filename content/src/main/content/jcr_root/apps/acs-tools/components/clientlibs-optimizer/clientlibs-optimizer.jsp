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
    //pageContext.setAttribute("favicon", component.getPath() + "/clientlibs/images/favicon.png");
    pageContext.setAttribute("pagePath", resourceResolver.map(currentPage.getPath()));
    pageContext.setAttribute("resourcePath", resourceResolver.map(resource.getPath()));

%><!doctype html>
<html>
    <head>
        <meta charset="UTF-8">
        <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">

        <title>Client Library Optimizer | ACS AEM Tools</title>

        <link rel="shortcut icon" href="${favicon}"/>

        <cq:includeClientLib css="acs-tools.clientlibs-optimizer.app"/>
    </head>

    <body id="acs-tools-clientlib-optimizer-app">

        <header class="top">

            <div class="logo">
                <a href="/"><i class="icon-marketingcloud medium"></i></a>
            </div>

            <nav class="crumbs">
                <a href="/miscadmin">Tools</a>
                <a href="${pagePath}.html">Client Library Optimizer</a>
            </nav>
        </header>

        <div class="page" role="main"
             ng-controller="MainCtrl"
             ng-init="uri = '${resourcePath}.optimize.json';">

                <div class="content">
                    <div class="content-container">

                        <h1>Client Library Optimizer</h1>

                        <p>The Client Library Optimizer provides a list of client library categories to
                            embed into a "wrapping" clientlib create a single HTTP-request-able file.</p>

                        <p>This tool works by identiyfing all dependency categories, allowing them to be embedeed.</p>

                        <form ng-submit="optimize()">

                            <div class="form-row">
                                <h4>Library Type</h4>

                                <div class="selector">
                                    <label><input
                                            ng-model="form.type"
                                            value="JS"
                                            type="radio" name="type" checked><span>JavaScript</span></label>
                                    <label><input
                                            ng-model="form.type"
                                            value="CSS"
                                            type="radio" name="type"><span>CSS</span></label>
                                </div>
                            </div>

                            <div class="form-row">
                                <h4>Categories</h4>

                                <input  ng-model="form.categories"
                                        type="text" placeholder="Comma delimited list of categories">
                            </div>

                            <div class="form-row">
                                <div class="form-left-cell">&nbsp;</div>
                                <button class="primary">Optimize</button>
                            </div>
                        </form>

                        <div class="error" ng-show="result.erring">
                            <h2>An error occurred.</h2>
                            <p>Please report the issue at the <a
                                    href="https://github.com/Adobe-Consulting-Services/acs-aem-tools/issues/new"
                                    target="_blank">ACS AEM Tools Issues</a> site.</p>
                        </div>

                        <div class="results" ng-show="result.categories && !result.erring">
                            <h2>Optimized Client Library Definition</h2>

                            <section class="well">

                                <code>
                                    &lt;?xml version="1.0" encoding="UTF-8"?&gt;<br/>
                                    &lt;jcr:root xmlns:cq="http://www.day.com/jcr/cq/1.0" xmlns:jcr="http://www.jcp.org/jcr/1.0"<br/>
                                    &nbsp;&nbsp;&nbsp;&nbsp;jcr:primaryType="cq:ClientLibraryFolder"<br/>
                                    &nbsp;&nbsp;&nbsp;&nbsp;categories="&lt;WRAPPING CATEGORY NAME&gt;"<br/>
                                    &nbsp;&nbsp;&nbsp;&nbsp;embed="[<em>{{result.categories}}</em>]"/&gt;<br/>
                                </code>

                            </section>
                        </div>
                    </div>
                </div>
            </div>

        <cq:includeClientLib js="acs-tools.clientlibs-optimizer.app"/>

        <%-- Register angular app; Decreases chances of collisions w other angular apps on the page (ex. via injection) --%>
        <script type="text/javascript">
            angular.bootstrap(document.getElementById('acs-tools-clientlib-optimizer-app'), ['clientLibsOptimizerApp']);
        </script>
    </body>
</html>