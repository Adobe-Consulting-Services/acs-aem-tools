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

<p>
    The CSV file is in the format of
    <code>&lt;source property value>,&lt;dest property value></code>
</p>

<form class="coral-Form coral-Form--vertical" ng-submit="update()">

<section class="coral-Form-fieldset">

    <div class="coral-Form-fieldwrapper">
        <label class="coral-Form-fieldlabel">CSV File</label>

            <input
                    accept="*/*"
                    type="file"
                    name="csv"
                    ngf-select
                    ng-model="files"
                    ng-required="true"
                    required
                    placeholder="Select a CSV file"/>
    </div>

    <div class="coral-Form-fieldwrapper">
        <label class="coral-Form-fieldlabel">Path Restriction</label>

            <input type="text"
                   name="path"
                   class="coral-Form-field coral-Textfield"
                   ng-model="form.path"
                   placeholder="Defaults to /content"/>
    </div>


    <div class="coral-Form-fieldwrapper">
        <label class="coral-Form-fieldlabel">Property Name</label>

            <select
                    name="propertyName"
                    ng-model="form.propertyName">
                <option value="sling:resourceType">sling:resourceType</option>
                <option value="cq:template">cq:template</option>
            </select>
        
    </div>


    <div class="coral-Form-fieldwrapper">
        <label class="coral-Form-fieldlabel">Field Separator</label>

            <input type="text"
                   class="coral-Form-field coral-Textfield"
                   name="separator"
                   ng-model="form.separator"
                   placeholder="Defaults to ,"/>
    </div>


    <div class="coral-Form-fieldwrapper">
        <label class="coral-Form-fieldlabel">Field Delimiter</label>

            <input type="text"
                   class="coral-Form-field coral-Textfield"
                   name="delimiter"
                   ng-model="form.delimiter"
                   placeholder="Defaults to &quot;"/>
    </div>

    <div class="coral-Form-fieldwrapper">
        <label class="coral-Form-fieldlabel">Charset</label>

            <input type="text"
                   class="coral-Form-field coral-Textfield"
                   name="charset"
                   ng-model="form.charset"
                   placeholder="Defaults to UTF-8"/>
    </div>


    <div class="coral-Form-fieldwrapper">
        <div class="form-left-cell">&nbsp;</div>
        <button class="coral-Button coral-Button--primary">Update {{ form.propertyName }}</button>
    </div>

</section>
</form>