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

<form ng-submit="update()">

    <div class="form-row">
        <h4>CSV File</h4>

        <span>
            <input
                    accept="*/*"
                    type="file"
                    name="csv"
                    ngf-select
                    ng-model="files"
                    ng-required="true"
                    required
                    placeholder="Select a CSV file"/>
        </span>
    </div>

    <div class="form-row">
        <h4>Path Restriction</h4>

        <span>
            <input type="text"
                   name="path"
                   ng-model="form.path"
                   placeholder="Defaults to /content"/>
        </span>
    </div>


    <div class="form-row">
        <h4>Property Name</h4>

        <span>
            <select
                    name="propertyName"
                    ng-model="form.propertyName">
                <option value="sling:resourceType">sling:resourceType</option>
                <option value="cq:template">cq:template</option>
            </select>
        </span>
    </div>


    <div class="form-row">
        <h4>Field Separator</h4>

        <span>
            <input type="text"
                   name="separator"
                   ng-model="form.separator"
                   placeholder="Defaults to ,"/>
        </span>
    </div>


    <div class="form-row">
        <h4>Field Delimiter</h4>

        <span>
            <input type="text"
                   name="delimiter"
                   ng-model="form.delimiter"
                   placeholder="Defaults to &quot;"/>
        </span>
    </div>

    <div class="form-row">
        <h4>Charset</h4>

        <span>
            <input type="text"
                   name="charset"
                   ng-model="form.charset"
                   placeholder="Defaults to UTF-8"/>
        </span>
    </div>


    <div class="form-row">
        <div class="form-left-cell">&nbsp;</div>
        <button class="primary">Update {{ form.propertyName }}</button>
    </div>
</form>

