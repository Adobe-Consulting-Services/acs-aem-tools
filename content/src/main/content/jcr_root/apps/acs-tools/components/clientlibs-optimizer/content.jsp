<%--
  #%L
  ACS AEM Tools Package
  %%
  Copyright (C) 2014 Adobe
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

%><div ng-controller="MainCtrl"
     ng-init="app.uri = '${resourcePath}.optimize.json';">

    <p>Find all transitive dependencies for a set of Client Library categories.</p>

    <p>The following link gives an overview over the dependencies of currently installed client libraries:
        <a href="/libs/granite/ui/content/dumplibs.html">/libs/granite/ui/content/dumplibs.html</a></p>


    <form ng-submit="optimize()">

        <div class="form-row">
            <h4 acs-coral-heading>Library Type</h4>

            <div class="coral-Selector">
                <label class="coral-Selector-option"><input
                        ng-model="form.js"
                        ng-change="validateTypes()"
                        class="coral-Selector-input"
                        ng-class="{ error : app.formErrors.types }"
                        type="checkbox" name="js"><span class="coral-Selector-description">JavaScript</span></label>
                <label class="coral-Selector-option"><input
                        ng-model="form.css"
                        ng-change="validateTypes()"
                        class="coral-Selector-input"
                        ng-class="{ error : app.formErrors.types }"
                        type="checkbox" name="css"><span class="coral-Selector-description">CSS</span></label>
            </div>

            <span   ng-show="app.formErrors.types"
                    class="coral-Icon coral-Icon--alert" data-init="quicktip" data-quicktip-arrow="left"
                    data-quicktip-type="error" data-quicktip-content="Select at least one Library Type"></span>

        </div>

        <div class="form-row">
            <h4 acs-coral-heading>Categories</h4>

            <span>
                <input  ng-model="form.categories"
                        ng-blur="validateCategories()"
                        ng-class="{ error : app.formErrors.categories }"
                        class="coral-Textfield"
                        type="text" placeholder="Comma-delimited list of categories">
            </span>

            <%-- Cannot use pure CoralUI display as it destorys the span after first use --%>
            <span   ng-show="app.formErrors.categories"
                    class="coral-Icon coral-Icon--alert" data-init="quicktip" data-quicktip-arrow="right"
                    data-quicktip-type="error" data-quicktip-content="Enter at least one category"></span>

        </div>

        <div class="form-row">
            <div class="form-left-cell">&nbsp;</div>
            <button class="coral-Button coral-Button--primary">Optimize</button>
        </div>
    </form>

    <div class="results" ng-show="result.categories">
        <h2 acs-coral-heading>Optimized Client Library Definition</h2>

        <section class="well">

            <code>
                &lt;?xml version="1.0" encoding="UTF-8"?&gt;<br/>
                &lt;jcr:root xmlns:cq="http://www.day.com/jcr/cq/1.0" xmlns:jcr="http://www.jcp.org/jcr/1.0"<br/>
                &nbsp;&nbsp;&nbsp;&nbsp;jcr:primaryType="cq:ClientLibraryFolder"<br/>
                &nbsp;&nbsp;&nbsp;&nbsp;categories="&lt;WRAPPING CATEGORY NAME&gt;"<br/>
                &nbsp;&nbsp;&nbsp;&nbsp;embed="[<em>{{ result.categories }}</em>]"/&gt;<br/>
            </code>

        </section>
    </div>
</div>