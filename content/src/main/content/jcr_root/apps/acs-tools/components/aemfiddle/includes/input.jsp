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

