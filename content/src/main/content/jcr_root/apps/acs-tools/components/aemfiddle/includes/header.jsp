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
%><%@page session="false"%>

<%-- Header --%>

<coral-shell-header
        class="coral--dark granite-shell-header coral3-Shell-header"
        role="region"
        aria-label="Header Bar"
        aria-hidden="false">
    <coral-shell-header-home class="globalnav-toggle"
                             data-globalnav-toggle-href="/"
                             role="heading"
                             aria-level="2">
        <a is="coral-shell-homeanchor"
           style="display: inline-block; padding-right: 0;"
           icon="adobeExperienceManagerColor"
           href="/"
           class="coral3-Shell-homeAnchor">
            <coral-icon ng-hide="!data.app.running"
                       class="coral3-Icon coral3-Shell-homeAnchor-icon coral3-Icon--sizeM coral3-Icon--adobeExperienceManagerColor"
                       icon="adobeExperienceManagerColor"
                       size="M"
                       role="img"
                       aria-label="adobe experience manager color"></coral-icon>
            <coral-icon ng-hide="data.app.running"
                        class="coral3-Icon coral3-Shell-homeAnchor-icon coral3-Icon--sizeM"
                        icon="circle"
                        size="M"
                        role="img"
                        aria-label="running color"></coral-icon>


            <coral-shell-homeanchor-label>Adobe Experience Manager</coral-shell-homeanchor-label>
        </a>
        <span style="line-height: 2.375rem;">/ ACS AEM Tools / AEM Fiddle</span>
    </coral-shell-header-home>
    <%@include file="blackbar.jsp" %>

</coral-shell-header>