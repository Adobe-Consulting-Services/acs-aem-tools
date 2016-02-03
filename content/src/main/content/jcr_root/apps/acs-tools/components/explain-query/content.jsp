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

%><div ng-controller="MainCtrl"
     ng-init="app.uri = '${resourcePath}.explain.json'; init();">

    <c:choose>
        <c:when test="${isSupported}">

            <p>Find the query plan used for executing any Query</p>

            <cq:include script="includes/form.jsp"/>

            <cq:include script="includes/explanation.jsp"/>

            <cq:include script="includes/slow-queries.jsp"/>

            <cq:include script="includes/popular-queries.jsp"/>

        </c:when>
        <c:otherwise>

            <div acs-coral-alert data-alert-type="notice" data-alert-size="large" data-alert-title="Incompatible version of AEM">
                Explain Query is only supported on AEM installs running Apache Jackrabbit Oak based
                    repositories.
            </div>

        </c:otherwise>
    </c:choose>

</div>
