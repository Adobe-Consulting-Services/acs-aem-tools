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
<form ng-submit="makeTags()">

    <div class="form-row">
        <h4>Tag Data Converter</h4>

        <span>
            <select
                    name="converter"
                    ng-model="form.converter"
                    ng-required="true"
                    required>
                    <option ng-repeat="converter in converters"
                            value="{{ converter.value }}">{{ converter.label }}</option>
            </select>
        </span>
    </div>

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
        <h4>Auto-clean CSV</h4>

        <div class="selector">
            <label><input
                    ng-model="form.clean"
                    type="radio"
                    name="clean"
                    value="true"><span>Yes</span></label>
            <label><input
                    ng-model="form.clean"
                    type="radio"
                    name="clean"
                    value="false"><span>No</span></label>
        </div>
    </div>

    <div class="form-row">
        <div class="form-left-cell">&nbsp;</div>
        <button class="primary">Make Tags</button>
    </div>
</form>

