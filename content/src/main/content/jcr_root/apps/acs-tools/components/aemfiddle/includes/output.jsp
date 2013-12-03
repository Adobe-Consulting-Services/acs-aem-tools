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
<div id="output-wrapper" data-hidden="false" data-percent="50">

    <nav class="toolbar">
        <div class="left icongroup">
            <a href="#toggle-output" class="icon-reply" title="Toggle Output/HTML">Toggle Output/HTML</a>
            <span class="divider"></span>
            <span class="output-status">
                Executed at
                [ <span class="executed-at"></span> ]
                against [ <span class="executed-against" data-default="<%= resource.getPath() %>"></span> ]
            </span>
            <span class="output-status-empty"><-- Click to toggle HTML source/normal output!</span>
        </div>
    </nav>

    <div id="output"><div class="inner"></div></div>
    <div id="output-src"></div>
</div>