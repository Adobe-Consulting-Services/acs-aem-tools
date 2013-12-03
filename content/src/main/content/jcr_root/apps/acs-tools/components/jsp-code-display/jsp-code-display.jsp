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
<%@page session="false" import="java.util.regex.*,
                                org.apache.sling.api.resource.Resource,
                                org.apache.commons.io.IOUtils,
                                java.io.InputStream"
%><%@taglib prefix="sling" uri="http://sling.apache.org/taglibs/sling/1.0" %><%
%><sling:defineObjects/>

<html>
<head>
<title>JSP Code Display</title>
<link href="http://alexgorbatchev.com/pub/sh/current/styles/shThemeDefault.css" rel="stylesheet" type="text/css" />
<script src="http://alexgorbatchev.com/pub/sh/current/scripts/shCore.js" type="text/javascript"></script>
<script src="http://alexgorbatchev.com/pub/sh/current/scripts/shAutoloader.js" type="text/javascript"></script>
<script src="http://alexgorbatchev.com/pub/sh/current/scripts/shBrushJava.js" type="text/javascript"></script>
<script src="http://code.jquery.com/jquery-1.9.1.min.js"></script>
</head>
<body>
<h1>JSP Code Display</h1>
<p>Enter a line from a stack trace like: <code>org.apache.jsp.apps.geometrixx.components.contentpage.content_jsp._jspService(content_jsp.java:75)</code> and see the Java code below.</p>
<form>
<input type="text" name="line" />
<input type="submit"/>
</form>
<%
String line = request.getParameter("line");
if (line != null) {
    Pattern pattern = Pattern.compile("^(.+)\\.(\\w+)\\.(\\w+)\\((\\w+\\.java):(\\d+)\\)$");
    Matcher matcher = pattern.matcher(line);
    if (matcher.matches()) {
        String packageName = matcher.group(1);
        String fileName = matcher.group(4);
        String lineNumber = matcher.group(5);
        
        Resource fileResource = resourceResolver.getResource("/var/classes/" + packageName.replace('.', '/') + "/" + fileName);
        InputStream instream = fileResource.adaptTo(InputStream.class);
%>
<pre class="brush: java">
<%= IOUtils.toString(instream) %>
</pre>
<script>
$(window).load(function() {
    SyntaxHighlighter.highlight();
    // not sure why this needs to happen 1 second later.
    setTimeout(function() {
        var line = $(".line.number<%= lineNumber %>");
        if (line.size() > 0) {
            $(document).scrollTop(line.position().top);
        }
    }, 1000);
});
</script>
<%
        instream.close();
    } else {
%>
This does not appear to be a stack trace line: <code><%= line %></code>.
<%  }
} %>
</body>
</html>