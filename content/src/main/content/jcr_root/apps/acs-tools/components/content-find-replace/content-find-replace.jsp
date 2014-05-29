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
<html ng-app="contentFindReplace">
<head>
<meta charset="UTF-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">

<title> Search & Replace | ACS AEM Tools</title>

<cq:includeClientLib css="content-find-replace.app" />
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


                    <label class="fieldlabel" for="search_path" >Search Path</label>
                    <input  class="field" type="text" name="search_path" placeholder="/content/geometrixx" ng-model="search_path" min=1 /><br/>


                    <label class="fieldlabel" for="search_component">Search Component</label>
                    <input class="field" type="text" name="search_component" placeholder="foundation/components/text" ng-model="search_component" /><br/>

                    <label class="fieldlabel" for="search_element">Search Element</label>
                    <input class="field" type="text" name="search_element" placeholder="nt:unstructured" ng-model="search_element" /><br/>

                    <!--label class="fieldlabel" for="search_type">Search Type</label>
                    <input class="field" type="text" name="search_type" placeholder="static" ng-model="search_type" /><br/ -->

                    <!--span class="select" for="search_type" data-init="select">
     					<button type="button">Select</button>
				     	<select>
    		     				<option value="static">Static</option>
         						<option value="dynamic">Dynamic</option>
   						</select>
					</span-->
                    <label class="fieldlabel" for="search_type">Search Type</label>
					<span class="field" data-init="select">
     					<select class="field" name="search_type" ng-model="search_type" >
    						<option  class="field" value="static" selected>Static</option>
         					<option class="field" value="dynamic">Dynamic</option>
    					</select>
					</span><br/> <br/>

                    <label class="fieldlabel" for="search_string">Search String  (Dynamic Example: sab=\"[0-9]*\")</label>
                    <input class="field" type="text" name="search_string"  ng-model="search_string" /><br/>

                    <label class="fieldlabel" for="replace_string">Replace String</label>
                    <input class="field" type="text" name="replace_string" ng-model="replace_string" /><br/>

                    <label><input class="field" name="update_references" value="replace" checked="" ng-model="update_references" type="radio"><span>Replace?</span></label>
                    <label><input class="field" name="update_references" value="dryrun" ng-model="update_references" type="radio"><span>Dry run?</span></label>
                    <label><input class="field" name="update_references" value="package" ng-model="update_references" type="radio"><span>Create Backup package of replacing nodes?</span></label>
                    <br/>

                    <button class="primary" ng-click="submitSNP()">Go</button>
                </section>
            </form>

            <div class="alert error" ng-show="error">
                <button class="close" data-dismiss="alert">&times;</button>
                <strong>ERROR</strong><div>{{errorMessage}}</div>
            </div>
			<div class="alert success" ng-show="success">
				<button class="close" data-dismiss="alert">×</button>
				<strong>Success</strong><div>{{successMessage}}</div>
			</div>
        </div>
    </div>

    <cq:includeClientLib js="jquery,jquery-ui,content-find-replace.app" />
</body>
</html>