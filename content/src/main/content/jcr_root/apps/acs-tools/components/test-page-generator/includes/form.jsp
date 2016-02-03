<%--
  ~ #%L
  ~ ACS AEM Tools Bundle
  ~ %%
  ~ Copyright (C) 2013 Adobe
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

<form   class="no-separator"
        name="tpgForm"
        novalidate
        ng-submit="generatePages(tpgForm.$valid)">

    <div class="form-row">
        <h4 acs-coral-heading>Content Root</h4>

        <span>
            <input type="text"
                   name="rootPath"
                   class="coral-Textfield"
                   ng-model="form.rootPath"
                   placeholder="Root path [ Default: /content/<current-timestamp> ]"/>
        </span>
    </div>

    <div class="form-row">
        <h4>Template</h4>

        <span>
            <input type="text"
                   name="template"
                   class="coral-Textfield"
                   ng-model="form.template"
                   placeholder="Template path [ Optional ]"/>
        </span>
    </div>

    <div class="form-row">
        <h4 acs-coral-heading>Total Pages</h4>

        <span>
            <input type="text"
                   name="total"
                   class="coral-Textfield"
                   ng-required="true"
                   ng-pattern="/^\d+$/"
                   ng-model="form.total"
                   placeholder="Total number of pages to generate"/>
        </span>
    </div>

    <div class="form-row">
        <h4 acs-coral-heading>Bucket Size</h4>

        <span>
            <input type="text"
                   name="bucketSize"
                   class="coral-Textfield"
                   ng-pattern="/(^[2-9]\d*)|(^[1-9]\d+)/"
                   ng-model="form.bucketSize"
                   placeholder="Number of pages to generate per folder. Must be greater than 1. [ Default: 100 ]"/>
        </span>
    </div>

    <div class="form-row">
        <h4 acs-coral-heading>Bucket Type</h4>

        <span>
            <select ng-model="form.bucketType">
                <option value="sling:Folder">Folder</option>
                <option value="cq:Page">Page</option>
            </select>
        </span>
    </div>

    <div class="form-row"
         ng-show="form.bucketType === 'cq:Page'">
        <div class="instructions">
            <p>
                Bucket pages are NOT included in the total pages to generate.
            </p>
        </div>
    </div>

    <div class="form-row">
        <h4 acs-coral-heading>Save Threshold</h4>

        <span>
            <input type="text"
                   name="saveThreshold"
                   class="coral-Textfield"
                   ng-pattern="/^\d+$/"
                   ng-model="form.saveThreshold"
                   placeholder="Save batch size [ Default: 1000 ]"/>
        </span>
    </div>

    <div class="form-row">
        <h4>Properties</h4>

        <table class="coral-Table coral-Table--hover coral-Table--bordered properties-table">
            <thead>
            <tr class="coral-Table-row">
                <th class="coral-Table-headerCell property-multi">Multi</th>
                <th class="coral-Table-headerCell property-name">Name</th>
                <th class="coral-Table-headerCell property-value">Value</th>
                <th class="coral-Table-headerCell property-remove"></th>
            </tr>
            </thead>
            <tbody>
            <tr class="coral-Table-row" ng-repeat="property in form.properties">

                <td class="coral-Table-cell property-multi"><label acs-coral-checkbox><input
                        ng-model="property.multi" type="checkbox"><span></span></label></td>

                <td class="coral-Table-cell property-name"><input type="text"
                                                 class="coral-Textfield"
                                                 ng-model="property.name"
                                                 placeholder=""/></td>

                <td class="coral-Table-cell property-value"><input type="text"
                                                  class="coral-Textfield"
                                                  ng-model="property.value"
                                                  placeholder=""/></td>

                <td class="coral-Table-cell property-remove">
                    <span ng-show="form.properties.length > 1"
                           ng-click="removeProperty(form.properties, $index)">
                            <i class="coral-Icon coral-Icon--minusCircle"></i>&nbsp;Remove</span>
                </td>
            </tr>

            <tr class="coral-Table-row">
                <td colspan="4" class="coral-Table-cell property-add">
                    <span ng-click="addProperty(form.properties)">
                       <i class="coral-Icon coral-Icon--addCircle"></i>&nbsp;Add Property</span>
                </td>
            </tr>
            </tbody>
        </table>
    </div>

    <div class="form-row">
        <div class="instructions"
             ng-non-bindable>
            <p>
                Properties marked as "Multi" will split the Value on commas ( , )
                turning the resulting segments into a String Array.
            </p>
            <p>
                JavaScript evaluated server-side can be used to generate values for property values.
                Place JavaScript between {{ }} to be evaluated.
                <br/>
                JavaScript expressions should evaluate to Strings or Numbers and not Objects.
                <br/>
                Example: {{ new Date().getTime() }}
            </p>
        </div>
    </div>

    <div class="form-row">
        <div class="form-left-cell">&nbsp;</div>
        <button ng-show="!app.running && tpgForm.$invalid" class="coral-Button coral-Button--primary" disabled>Generate Test Pages</button>
        <button ng-show="!app.running && tpgForm.$valid" class="coral-Button coral-Button--primary">Generate Test Pages</button>
        <button ng-show="app.running" class="coral-Button coral-Button--primary" disabled>Generating Test Pages...</button>
    </div>
</form>