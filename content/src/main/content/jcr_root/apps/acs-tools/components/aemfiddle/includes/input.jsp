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
%><%@page session="false"%><%

    String includePath = component.getPath() + "/code-templates/";
    final String mode = slingRequest.getParameter("mode");

    if("pro".equals(mode) || currentPage.getPath().endsWith("-pro")) {
        includePath += "pro.jsp";
    } else {
        includePath += "basic.jsp";
    }
%>

<%-- Pane containing input code editor --%>

<div id="input">
    <%-- ACE Editor bound to #ace-input --%>
    <div id="ace-input"
         class="code-editor"><cq:include script="<%= includePath %>" /></div>
</div>

