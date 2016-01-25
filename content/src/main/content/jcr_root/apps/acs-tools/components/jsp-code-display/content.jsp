<%--
  ~ #%L
  ~ ACS AEM Tools Bundle
  ~ %%
  ~ Copyright (C) 2015 Adobe
  ~ %%
  ~ Licensed under the Apache License, Version 2.0 (the "License");
  ~ you may not use this file except in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~   http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  ~ #L%
  --%>
<%@include file="/libs/foundation/global.jsp"%><%

    /* ACE JS Base path */
    pageContext.setAttribute("aceEditorBasePath",
            resourceResolver.map(slingRequest, "/etc/clientlibs/acs-tools/vendor/aceeditor"));

%><div ng-controller="MainCtrl"
     ng-init="app.uri='${resourcePath}.fetch.json'">

    <form class="coral-Form coral-Form--vertical">
        <section class="coral-Form-fieldset">

            <label class="coral-Form-fieldlabel" for="line">Enter a line from a stack trace like:
                <br/>
                <code>  org.apache.jsp.apps.geometrixx.components.contentpage.content_jsp._jspService(content_jsp.java:75)</code>
                <br/>
                and see the Java code below
            </label>

            <input class="coral-Form-field coral-Textfield" type="text" name="line" ng-model="line"/>

            <div>
                <button class="coral-Button coral-Button--primary" ng-click="debug()">Go</button>
            </div>
        </section>
    </form>

    <div id="editor" ace-editor ace-editor-base-path="${aceEditorBasePath}"></div>
</div>
