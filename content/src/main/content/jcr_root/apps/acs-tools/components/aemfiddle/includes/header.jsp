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

<%-- Header --%>
<div class="endor-Panel-header endor-BreadcrumbBar">

    <nav class="endor-Crumbs">
        <a class="endor-Crumbs-item" href="/" ng-hide="data.app.running">
            <i class="endor-Crumbs-item-icon coral-Icon coral-Icon--adobeExperienceManager coral-Icon--sizeM"></i>
        </a>
        <a class="endor-Crumbs-item" href="#" ng-show="data.app.running">
            <i class="endor-Crumbs-item-icon coral-Icon coral-Icon--circle coral-Icon--sizeM"></i>
        </a>
        <a class="endor-Crumbs-item" href="/miscadmin">Tools</a>
        <a class="endor-Crumbs-item" href="<%= currentPagePath %>.html">AEM Fiddle</a>
    </nav>

</div>