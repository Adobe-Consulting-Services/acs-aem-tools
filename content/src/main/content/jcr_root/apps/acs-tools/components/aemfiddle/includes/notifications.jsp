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

<div id="notifications">
    <ul>
        <li ng-repeat="notification in data.notifications">
            <div class="coral-Alert coral-Alert--{{notification.type}}">
                <button type="button" class="coral-MinimalButton coral-Alert-closeButton" title="Close" data-dismiss="alert">
                    <i class="coral-Icon coral-Icon--sizeXS coral-Icon--close coral-MinimalButton-icon"></i>
                </button>
                <i class="coral-Alert-typeIcon coral-Icon coral-Icon--sizeS coral-Icon--alert"></i>
                <strong class='coral-Alert-title'>{{notification.title}}</strong>
                <div class='coral-Alert-message'>{{notification.message}}</div>
            </div>
        </li>
    </ul>
</div>