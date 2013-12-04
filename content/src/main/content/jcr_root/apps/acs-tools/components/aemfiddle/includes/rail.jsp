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

<%-- Rail --%>

<div class="rail right" ng-show="data.ui.rail.visible" role="complementary">
    <div class="wrap">

        <%-- Title --%>
        <section>
            <span ng-class="data.myfiddles.list.length < 1 ? 'empty' : ''"
                  class="myfiddles-count badge">{{data.myfiddles.list.length}}</span>
            <h4>My Fiddles</h4>
        </section>

        <%-- Controls --%>
        <section>
            <a href="#update"
               ng-disabled="(!data.myfiddles.current || !data.myfiddles.current.path) || data.ui.myfiddles.createFiddle.visible"
               ng-click="myfiddles.update(data.myfiddles.current)"
               class="icon-download"
               title="Update current fiddle">Update</a>

            <span class="divider"></span>

            <a href="#create"
               ng-disabled="data.ui.myfiddles.createFiddle.visible"
               ng-click="ui.showCreateFiddle()"
               class="icon-add"
               title="Create new fiddle">Create</a>

            <span class="divider"></span>

            <a href="#reset"
               ng-disabled="data.ui.myfiddles.createFiddle.visible"
               ng-click="app.reset()"
               class="icon-refresh"
               title="Reset">Reset</a>
        </section>

        <%-- Create Fiddle Form --%>
        <section class="create-fiddle" ng-show="data.ui.myfiddles.createFiddle.visible">
            <h4>Save code as new Fiddle?</h4>

            <div class="fields">
                <label>Name:</label>
                <input ng-model="data.myfiddles.new.title" type="text"/>
            </div>

            <div class="footer">
                <a ng-click="ui.hideCreateFiddle()"
                   class="button"
                   role="button"
                   href="#cancel-create">Cancel</a>

                <a ng-click="myfiddles.create('<%= myFiddlesPath %>')"
                   class="button primary"
                   role="button"
                   href="#confirm-create">Save</a>
            </div>
        </section>

        <%-- My Fiddles List --%>
        <section class="myfiddles-list" ng-hide="data.ui.myfiddles.createFiddle.visible">
            <ul ng-show="data.myfiddles.list.length > 0">
                <li ng-repeat="fiddle in data.myfiddles.list"
                    ng-class="fiddle.active?'active':''">

                    <a ng-click="myfiddles.delete(fiddle)"
                       class="delete-button icon-close"
                       href="#delete">Delete</a>

                    <a ng-click="myfiddles.load(fiddle)"
                       href="#load">{{fiddle.title}}<span class="script-extension">.{{fiddle.scriptExt}}</span></a>

                </li>
            </ul>
            <div ng-hide="data.myfiddles.list.length > 0">
                <br/><br/>
                <div class="greyText" style="text-align:center">No Saved Fiddles Found</div>

            </div>
        </section>

    </div>
</div>
<%-- End Rail --%>