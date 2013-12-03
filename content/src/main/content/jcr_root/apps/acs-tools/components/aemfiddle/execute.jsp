<%--
* Copyright 2013 david gonzalez.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
--%><%@include file="/libs/foundation/global.jsp" %><%
%><%@page session="false"
        import="org.apache.sling.api.request.RequestDispatcherOptions"%><%
    String path = slingRequest.getParameter("resource");

    if(path == null || "".equals(path)) {
        path = resource.getPath();
    } else if(resourceResolver.resolve(path) == null) {
        path = resource.getPath();
    }

    final RequestDispatcherOptions options = new RequestDispatcherOptions();
    options.setForceResourceType("acs-tools/components/aemfiddle/fiddles");
    options.setReplaceSelectors("execute");
    sling.forward(path, options);
%>