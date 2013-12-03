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
--%>
<%-- Editor Form used to POST required data to AEM --%>
<div id="editor-wrapper" data-percent="50">
    <form id="editor-form" action="<%= fiddleScript %>"
          method="post" enctype="multipart/form-data"
          data-reload="<%= resource.getPath() %>.execute">
        <input type="hidden" name="jcr:primaryType" value="nt:file"/>

        <input type="hidden" name="jcr:content/jcr:mimeType" value="text/plain"/>
        <input type="hidden" name="jcr:content/jcr:primaryType" value="nt:resource"/>

        <input type="hidden" name="jcr:content/jcr:lastModified" data-last-modified/>
        <input type="hidden" name="jcr:content/jcr:lastModified@TypeHint" value="Date"/>

        <input type="hidden" data-jcr-data name="jcr:content/jcr:data" value=""/>
        <input type="hidden" name="jcr:content/jcr:data@TypeHint" value="Binary"/>

<%-- Ace Editor --%>
<pre id="editor">

&lt;%@include file="/libs/foundation/global.jsp"%&gt;&lt;%
%&gt;&lt;&#37;@page session="false" contentType="text/html; charset=utf-8" pageEncoding="UTF-8"
import="org.apache.sling.api.resource.*,
        java.util.*,
        javax.jcr.*,
        com.day.cq.search.*,
        com.day.cq.wcm.api.*,
        com.day.cq.dam.api.*"%&gt;&lt;%

    // Code here

%&gt;

</pre>
    </form>
    <div id="handle" class="ui-resizable-handle ui-resizable-e"></div>
</div>