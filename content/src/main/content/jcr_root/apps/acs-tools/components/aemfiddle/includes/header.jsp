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
<header class="top">
    <div class="logo">
        <a href="/"><i class="icon-marketingcloud medium"></i></a>
    </div>

    <nav class="crumbs">
        <a href="/miscadmin">Tools</a>
        <a href="<%= currentPage.getPath() %>.html">AEM Fiddle</a>
    </nav>

    <div class="right icongroup">
        <input type="text" data-resource placeholder="Absolute path of resource to execute code against"/>
        <a href="#run" class="button primary" title="OS X: Command-K / Windows: Ctrl-K">Run Code</a>

        <span class="clean">
            <a href="#clean" class="icon-refresh" title="Create a clean slate">Clean</a>
            <div class="tooltip success arrow-top"></div>
        </span>


        <span class="update">
            <a href="#update" class="disabled icon-download" title="Update current fiddle">Update</a>
            <div class="tooltip success arrow-top"></div>
        </span>

        <span class="save">
            <a href="#save" class="icon-add" data-toggle="save-modal" title="Create new fiddle">Save</a>
            <div class="tooltip success arrow-top"></div>
            <%@include file="savemodal.jsp"%>
        </span>

        <a href="#list" title="Click to display saved fiddles"
             data-url="<%= saveToPath %>"
             class="badge <%= savedCount < 1 ? "empty" : "" %>"><%= savedCount %>
        </a>

    </div>
</header>

<div id="slow-down-alert" class="alert notice">
    <button class="close" data-dismiss="alert">&times;</button>
    <strong>SLOW DOWN!</strong><div> Stopping trying to run your code so fast. CQ needs a few seconds to catch its breathe!</div>
</div>