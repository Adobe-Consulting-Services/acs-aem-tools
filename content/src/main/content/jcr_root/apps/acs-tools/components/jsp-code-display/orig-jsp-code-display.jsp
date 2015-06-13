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
<html ng-app="jspCodeDisplay">
<head>
<meta charset="UTF-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">

<title>JSP Code Display | ACS AEM Tools</title>

<cq:includeClientLib css="jsp-code-display.app" />
</head>

<body ng-controller="MainCtrl" data-post-url="<%= resource.getPath() %>.fetch.json">
    <header class="top">
    
        <div class="logo">
            <span ng-hide="running"><a href="/"><i class="icon-marketingcloud medium"></i></a></span>
            <span ng-show="running"><span class="spinner"></span></span>
        </div>
    
        <nav class="crumbs">
            <a href="/miscadmin">Tools</a>
            <a href="<%= currentPage.getPath() %>.html">JSP Code Display</a>
        </nav>
    
    </header>

    <div class="page" role="main">

        <div class="content">

            <form class="vertical">
                <section class="fieldset">
                    <h1>JSP Code Display</h1>
                    <label class="fieldlabel" for="line">Enter a line from a stack trace like:
                        <code>
                            org.apache.jsp.apps.geometrixx.components.contentpage.content_jsp._jspService(content_jsp.java:75)</code>
                        and see the Java code below</label>
                    <input class="field" type="text" name="line" ng-model="line"/><br/>
                    <button class="primary" ng-click="submitLine()">Go</button>
                </section>
            </form>
        
            <div class="alert error" ng-show="error">
                <button class="close" data-dismiss="alert">&times;</button>
                <strong>ERROR</strong><div>{{errorMessage}}</div>
            </div>
            
            <div id="editor"></div>

        </div>
    </div>

    <cq:includeClientLib js="jquery,jquery-ui,jsp-code-display.app" />
</body>
</html>