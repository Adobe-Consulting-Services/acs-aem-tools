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

    <body id="acs-tools-clientlibs-optimizer-app">

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
             ng-init="app.uri = '${resourcePath}.optimize.json';">

                <div class="content">
                    <div class="content-container">

                        <notifications items="notifications"></notifications>

                        <h1>Client Library Optimizer</h1>

                        <p>Find all transitive dependencies for a set of Client Library categories.</p>
                        <p>The following link gives an overview over the dependencies of currently installed client libraries: <a href="/libs/granite/ui/content/dumplibs.html">/libs/granite/ui/content/dumplibs.html</a></p>


                        <form ng-submit="optimize()">

                            <div class="form-row">
                                <h4>Library Type</h4>

                                <div class="selector">
                                    <label><input
                                            ng-model="form.js"
                                            ng-change="validateTypes()"
                                            ng-class="{ error : app.formErrors.types }"
                                            type="checkbox" name="js"><span>JavaScript</span></label>
                                    <label><input
                                            ng-model="form.css"
                                            ng-change="validateTypes()"
                                            ng-class="{ error : app.formErrors.types }"
                                            type="checkbox" name="css"><span>CSS</span></label>
                                </div>

                                <span   ng-show="app.formErrors.types"
                                        class="form-error" data-init="quicktip" data-quicktip-arrow="left"
                                        data-quicktip-type="error">Select at least one Library Type</span>

                            </div>

                            <div class="form-row">
                                <h4>Categories</h4>

                                <span>
                                    <input  ng-model="form.categories"
                                            ng-blur="validateCategories()"
                                            ng-class="{ error : app.formErrors.categories }"
                                            type="text" placeholder="Comma-delimited list of categories">
                                </span>

                                <%-- Cannot use pure CoralUI display as it destorys the span after first use --%>
                                <span   ng-show="app.formErrors.categories"
                                        class="form-error" data-init="quicktip" data-quicktip-arrow="right"
                                        data-quicktip-type="error">Enter at least one category</span>

                            </div>

                            <div class="form-row">
                                <div class="form-left-cell">&nbsp;</div>
                                <button class="primary">Optimize</button>
                            </div>
                        </form>

                        <div class="results" ng-show="result.categories">
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
            angular.bootstrap(document.getElementById('acs-tools-clientlibs-optimizer-app'),
                    ['clientLibsOptimizerApp']);
        </script>
    </body>
</html>