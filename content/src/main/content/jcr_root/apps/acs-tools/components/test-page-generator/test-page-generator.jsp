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
%><%@page session="false" %><%

    pageContext.setAttribute("pagePath", resourceResolver.map(currentPage.getPath()));
    pageContext.setAttribute("resourcePath", resourceResolver.map(resource.getPath()));

%><!doctype html>
<html>
<head>
    <meta charset="UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">

    <title>Test Page Generator | ACS AEM Tools</title>

    <cq:includeClientLib css="acs-tools.test-page-generator.app"/>
</head>

<body id="acs-tools-test-page-generator-app">

    <header class="top">

        <div class="logo">
            <a href="/"><i class="icon-marketingcloud medium"></i></a>
        </div>

        <nav class="crumbs">
            <a href="/miscadmin">Tools</a>
            <a href="${pagePath}.html">Test Page Generator</a>
        </nav>
    </header>

    <div class="page" role="main"
         ng-controller="MainCtrl"
         ng-init="app.uri = '${resourcePath}.generate-pages.json'; init();">

        <div class="content">
            <div class="content-container">

                <h1>Test Page Generator</h1>

                <p>Test Page Generator will create any number of pages in a bucketed structure.</p>

                <div ng-show="notifications.length > 0">
                    <div ng-repeat="notification in notifications">
                        <div class="alert {{ notification.type }}">
                            <button class="close" data-dismiss="alert">&times;</button>
                            <strong>{{ notification.title }}</strong>

                            <div>{{ notification.message }}</div>
                        </div>
                    </div>
                </div>


                <div ng-show="app.running">

                    <div class="alert notice large">
                        <strong>Creating pages</strong>
                        <div>
                            <i class="spinner large"></i>
                            Please be patient while the system creates your pages; depending on the total
                            number of pages requested to be created this process could take a long time.
                        </div>
                    </div>
                </div>

                <div ng-show="results.success"
                     class="results alert success large">
                    <strong>Results Summary</strong>

                    <div>
                        <ul>
                            <li>Created under: {{ results.rootPath }}</li>
                            <li>Bucket depth: {{ results.depth }}</li>
                            <li>Bucket size: {{ results.bucketSize }}</li>
                            <li>Save threshold: {{ results.saveThreshold }}</li>
                            <li>Total pages created: {{ results.count }}</li>
                            <li>Total time: {{ results.totalTime }} s </li>
                        </ul>

                        <a x-cq-linkchecker="skip"
                           target="_blank"
                           href="<%= "/crx/de/index.jsp#" %>{{ results.rootPath }}">
                            Open in CRXDE Lite</a>
                    </div>
                </div>


                <form ng-submit="generatePages()">

                    <div class="form-row">
                        <h4>Content Root</h4>

                        <span>
                            <input type="text"
                                   ng-model="form.rootPath"
                                   placeholder="Root path [ Default: /content/<current-timestamp> ]"/>
                        </span>
                    </div>

                    <div class="form-row">
                        <h4>Template</h4>

                        <span>
                            <input type="text"
                                   ng-model="form.template"
                                   placeholder="Template path [ Optional ]"/>
                        </span>
                    </div>

                    <div class="form-row">
                        <h4>Total Pages</h4>

                        <span>
                            <input type="text"
                                    ng-required="true"
                                    ng-model="form.total"
                                    placeholder="Total number of pages to generate"/>
                        </span>
                    </div>

                    <div class="form-row">
                        <h4>Bucket Size</h4>

                        <span>
                            <input type="text"
                                   ng-model="form.bucketSize"
                                   placeholder="Number of pages to generate per folder [ Default: 100 ]"/>
                        </span>
                    </div>

                    <div class="form-row">
                        <h4>Save Threshold</h4>

                        <span>
                            <input type="text"
                                   ng-model="form.saveThreshold"
                                   placeholder="Save batch size [ Default: 1000 ]"/>
                        </span>
                    </div>

                    <div class="form-row">
                        <h4>Properties</h4>

                        <table class="data properties-table">
                            <thead>
                            <tr>
                                <th class="property-multi">Multi</th>
                                <th class="property-name">Name</th>
                                <th class="property-value">Value</th>
                                <th class="property-remove"></th>
                            </tr>
                            </thead>
                            <tbody>
                            <tr ng-repeat="property in form.properties">

                                <td class="property-multi"><label><input
                                        ng-model="property.multi"type="checkbox"><span></span></label></td>

                                <td class="property-name"><input type="text"
                                            ng-model="property.name"
                                            placeholder=""/></td>

                                <td class="property-value"><input type="text"
                                            ng-model="property.value"
                                            placeholder=""/></td>

                                <td class="property-remove">
                                    <i      ng-show="form.properties.length > 1"
                                            ng-click="removeProperty(form.properties, $index)"
                                       class="icon-minus-circle">Remove</i>
                                </td>
                            </tr>

                            <tr>
                                <td colspan="4" class="property-add">
                                    <i ng-click="addProperty(form.properties)"
                                       class="icon-add-circle withLabel">Add Property</i>
                                </td>
                            </tr>
                            </tbody>
                        </table>
                    </div>

                    <div class="form-row">
                        <h4></h4>
                        <span class="instructions">
                            Properties marked as "Multi" will split the Value on commas ( , )
                            turning the resulting segments into a String Array.
                        </span>
                    </div>


                    <div class="form-row">
                        <div class="form-left-cell">&nbsp;</div>
                        <button ng-hide="app.running" class="primary">Generate Pages</button>
                        <button ng-show="app.running" disabled>Generating Pages...</button>
                    </div>
                </form>


                <cq:includeClientLib js="acs-tools.test-page-generator.app"/>

                <%-- Register angular app; Decreases chances of collisions w other angular apps on the page (ex. via injection) --%>
                <script type="text/javascript">
                    angular.bootstrap(document.getElementById('acs-tools-test-page-generator-app'),
                            ['testPageGeneratorApp']);
                </script>

            </div>
        </div>
    </div>
</body>
</html>