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

<form   name="tpgForm"
        novalidate
        ng-submit="generatePages(tpgForm.$valid)">

    <div class="form-row">
        <h4>Content Root</h4>

        <span>
            <input type="text"
                   name="rootPath"
                   ng-model="form.rootPath"
                   placeholder="Root path [ Default: /content/<current-timestamp> ]"/>
        </span>
    </div>

    <div class="form-row">
        <h4>Template</h4>

        <span>
            <input type="text"
                   name="template"
                   ng-model="form.template"
                   placeholder="Template path [ Optional ]"/>
        </span>
    </div>

    <div class="form-row">
        <h4>Total Pages</h4>

        <span>
            <input type="text"
                   name="total"
                   ng-required="true"
                   ng-pattern="/^\d+$/"
                   ng-model="form.total"
                   placeholder="Total number of pages to generate"/>
        </span>
    </div>

    <div class="form-row">
        <h4>Bucket Size</h4>

        <span>
            <input type="text"
                   name="bucketSize"
                   ng-pattern="/(^[2-9]\d*)|(^[1-9]\d+)/"
                   ng-model="form.bucketSize"
                   placeholder="Number of pages to generate per folder. Must be greater than 1. [ Default: 100 ]"/>
        </span>
    </div>

    <div class="form-row">
        <h4>Bucket Type</h4>

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
        <h4>Save Threshold</h4>

        <span>
            <input type="text"
                   name="saveThreshold"
                   ng-pattern="/^\d+$/"
                   ng-model="form.saveThreshold"
                   placeholder="Save batch size [ Default: 1000 ]"/>
        </span>
    </div>

    <div class="form-row">
        <h4>Properties</h4>

        <table class="data properties-table">
            <thead>
            <tr>
                <th class="property-multi">Multi</th>
                <th class="property-name">Name</th>
                <th class="property-value">Value</th>
                <th class="property-remove"></th>
            </tr>
            </thead>
            <tbody>
            <tr ng-repeat="property in form.properties">

                <td class="property-multi"><label><input
                        ng-model="property.multi" type="checkbox"><span></span></label></td>

                <td class="property-name"><input type="text"
                                                 ng-model="property.name"
                                                 placeholder=""/></td>

                <td class="property-value"><input type="text"
                                                  ng-model="property.value"
                                                  placeholder=""/></td>

                <td class="property-remove">
                    <i      ng-show="form.properties.length > 1"
                            ng-click="removeProperty(form.properties, $index)"
                            class="icon-minus-circle">Remove</i>
                </td>
            </tr>

            <tr>
                <td colspan="4" class="property-add">
                    <i ng-click="addProperty(form.properties)"
                       class="icon-add-circle withLabel">Add Property</i>
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
        <button ng-show="!app.running && tpgForm.$invalid" disabled>Generate Test Pages</button>
        <button ng-show="!app.running && tpgForm.$valid" class="primary">Generate Test Pages</button>
        <button ng-show="app.running" disabled>Generating Test Pages...</button>
    </div>
</form>