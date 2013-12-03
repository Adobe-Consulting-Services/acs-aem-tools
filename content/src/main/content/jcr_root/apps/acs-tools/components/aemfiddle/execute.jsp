<%@include file="/libs/foundation/global.jsp" %><%
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