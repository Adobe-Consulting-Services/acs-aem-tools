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
<%@include file="/libs/foundation/global.jsp"%>
<!doctype html>
<html ng-app="findAndReplace">
<head>
<meta charset="UTF-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">

<title> Find & Replace | ACS AEM Tools</title>

<cq:includeClientLib css="find-and-replace.app" />
</head>



<body ng-controller="MainCtrl" data-post-url="<%= resource.getPath() %>.findreplace.json">
    <header class="top">

        <div class="logo">
            <span ng-hide="running"><a href="/"><i class="icon-marketingcloud medium"></i></a></span>
            <span ng-show="running"><span class="spinner"></span></span>
        </div>

        <nav class="crumbs">
            <a href="/miscadmin">Tools</a>
            <a href="<%= currentPage.getPath() %>.html">Search & Replace</a>
        </nav>

    </header>

    <div class="page" role="main">

        <div class="content">

            <form class="vertical" >
                <section class="fieldset">
                    <!--h1>Search & Replace</h1-->


                    <label class="fieldlabel" for="searchPath" >Search Path</label>
                    <input  class="field" type="text" name="searchPath" placeholder="/content/geometrixx" ng-model="searchPath" min=1 /><br/>


                    <label class="fieldlabel" for="searchComponent">Search Component</label>
                    <input class="field" type="text" name="searchComponent" placeholder="foundation/components/text" ng-model="searchComponent" /><br/>

                    <label class="fieldlabel" for="searchElement">Search Element</label>
                    <input class="field" type="text" name="searchElement" placeholder="nt:unstructured" ng-model="searchElement" /><br/>


                    <label class="fieldlabel" for="searchString">Search String  (Dynamic Example: sab=\"[0-9]*\")</label>
                    <input class="field" type="text" name="searchString"  ng-model="searchString" /><br/>

                    <label class="fieldlabel" for="replaceString">Replace String</label>
                    <input class="field" type="text" name="replaceString" ng-model="replaceString" /><br/>

                    <label><input class="field" name="updateReferences" value="replace" checked="" ng-model="updateReferences" type="radio"><span>Replace?</span></label>
                    <label><input class="field" name="updateReferences" value="dryrun" ng-model="updateReferences" type="radio"><span>Dry run?</span></label>
                    <label><input class="field" name="updateReferences" value="package" ng-model="updateReferences" type="radio"><span>Create Backup package of replacing nodes?</span></label>
                    <br/>

                    <button class="primary" ng-click="submitSNP()">Go</button>
                </section>
            </form>



						<div ng-show="notifications.length > 0">
                            <div ng-repeat="notification in notifications">
                                <div class="alert {{ notification.type }}">
                                    <button class="close" data-dismiss="alert">&times;</button>
                                    <strong>{{ notification.title }}</strong>

                                    <div>{{ notification.message }}</div>
                                </div>
                            </div>
                        </div>

        </div>
    </div>

    <cq:includeClientLib js="jquery,jquery-ui,find-and-replace.app" />
</body>
</html>