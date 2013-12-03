<%@include file="/libs/foundation/global.jsp" %>
<%@page session="false"
        import="java.util.Iterator,
                com.adobe.granite.security.user.UserProperties,
                com.adobe.granite.security.user.UserPropertiesManager,
                org.apache.jackrabbit.api.security.user.Authorizable,
                org.apache.sling.api.resource.Resource" %>
<%
    final String SAVE_TO = "fiddles";
    final String fiddleScript = "/apps/acs-tools/components/aemfiddle/fiddles/execute.jsp";

    final UserPropertiesManager upm = resourceResolver.adaptTo(UserPropertiesManager.class);
    final Authorizable authorizable = resourceResolver.adaptTo(Authorizable.class);
    final UserProperties userProperties = upm.getUserProperties(authorizable, "profile");

    final Resource saveToResource = userProperties.getResource(SAVE_TO);

    // Used if Profile cannot be retrieved
    String saveToPath = "/var/" + SAVE_TO;

    int savedCount = 0;
    if (saveToResource != null) {
        saveToPath = saveToResource.getPath();
        final Iterator<Resource> children = saveToResource.listChildren();
        while (children.hasNext()) {
            children.next();
            savedCount++;
        }
    } else {
        saveToPath = userProperties.getNode().getPath() + "/" + SAVE_TO;
    }
%><!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <link rel="shortcut icon" href="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAAA2hpVFh0WE1MOmNvbS5hZG9iZS54bXAAAAAAADw/eHBhY2tldCBiZWdpbj0i77u/IiBpZD0iVzVNME1wQ2VoaUh6cmVTek5UY3prYzlkIj8+IDx4OnhtcG1ldGEgeG1sbnM6eD0iYWRvYmU6bnM6bWV0YS8iIHg6eG1wdGs9IkFkb2JlIFhNUCBDb3JlIDUuMy1jMDExIDY2LjE0NTY2MSwgMjAxMi8wMi8wNi0xNDo1NjoyNyAgICAgICAgIj4gPHJkZjpSREYgeG1sbnM6cmRmPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5LzAyLzIyLXJkZi1zeW50YXgtbnMjIj4gPHJkZjpEZXNjcmlwdGlvbiByZGY6YWJvdXQ9IiIgeG1sbnM6eG1wTU09Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC9tbS8iIHhtbG5zOnN0UmVmPSJodHRwOi8vbnMuYWRvYmUuY29tL3hhcC8xLjAvc1R5cGUvUmVzb3VyY2VSZWYjIiB4bWxuczp4bXA9Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC8iIHhtcE1NOk9yaWdpbmFsRG9jdW1lbnRJRD0ieG1wLmRpZDowMjgwMTE3NDA3MjA2ODExODA4Mzg1QkVDMDdCMTk1OCIgeG1wTU06RG9jdW1lbnRJRD0ieG1wLmRpZDo0OTRCQUU3RURBN0QxMUUyQUE3NzgzNzUxMTU5RTYyQyIgeG1wTU06SW5zdGFuY2VJRD0ieG1wLmlpZDo0OTRCQUU3RERBN0QxMUUyQUE3NzgzNzUxMTU5RTYyQyIgeG1wOkNyZWF0b3JUb29sPSJBZG9iZSBQaG90b3Nob3AgQ1M2IChNYWNpbnRvc2gpIj4gPHhtcE1NOkRlcml2ZWRGcm9tIHN0UmVmOmluc3RhbmNlSUQ9InhtcC5paWQ6MDE4MDExNzQwNzIwNjgxMTgwODNCRUQ5QjhGQzgxQzIiIHN0UmVmOmRvY3VtZW50SUQ9InhtcC5kaWQ6MDI4MDExNzQwNzIwNjgxMTgwODM4NUJFQzA3QjE5NTgiLz4gPC9yZGY6RGVzY3JpcHRpb24+IDwvcmRmOlJERj4gPC94OnhtcG1ldGE+IDw/eHBhY2tldCBlbmQ9InIiPz4B+tbuAAABe0lEQVR42pSTPU8CQRCGx8METUxAKxs7Ck0sSDR+QY2VP8H4FSuNhYkNDYW1Mf4VQSOoldpZQGMjKIgkakFET9jjbm/cOXcJp3w5yZvZ7O3zzkxuV4N/RD76itntAoplQGiI9rRe4VgshrxWB/ODQSZyey+2gmTSk0HtZBp3J5MsWtgDs1yFevkLrscSV2TS1aB2OoPIOXg0feBo6Y5vve04JlaF0edQXyeYnc2ibZpAQpmr7xoPH9qeXC4XF0du+tvCyTkHRstqEgevlwm4dCyOZMig5QgsNY8KULIpcwtGNktxCSeF0n8MjPMFdCD+A9hCKDW88ZyQcEpm3WVgXISwAbpaF/B6keC0qkyw6x4Yl2EH/g1SN/61p5Zww6CYDqAWikCruf2r+bZww2AsmAXLqEB9atw1t3/lsSPsGmHQdwAWK8PnhNfpxLf80BVuDlSicUSmX7UvFFaPpl2om0iva1Fmihe6JL1UVgZUZbSpmi5N9G5v5VuAAQARSw91DwEPugAAAABJRU5ErkJggg=="/>
    <title>AEM Fiddle</title>
    <cq:includeClientLib css="aemfiddle"/>
</head>
<body>
    <div class="page" role="main">
        <%-- Header --%>
        <%@include file = "includes/header.jsp" %>

        <%-- Left Column - Ace Editor --%>
        <%@include file = "includes/editor.jsp" %>

        <%-- Right Column - Output Area --%>
        <%@include file = "includes/output.jsp" %>

        <%-- Right Rail - View Toggled --%>
        <%@include file="includes/rail.jsp" %>
    </div>

    <%-- Application Alerting (Hidden) --%>
    <%@include file="includes/alerts.jsp" %>

    <%-- Application Messaging (Hidden) --%>
    <%@include file="includes/messages.jsp" %>

    <%-- JavaScripts and Ace Editor initialization --%>
    <cq:includeClientLib js="aemfiddle.ace, aemfiddle, jquery-ui"/>
</body>
</html>