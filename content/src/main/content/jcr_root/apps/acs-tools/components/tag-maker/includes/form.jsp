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


<div acs-coral-alert
     data-alert-type="notice"
     data-alert-size="large"
     data-alert-title="Tag Maker has moved to ACS AEM Commons' Tag Creator"
     class="coral-Alert coral-Alert--notice coral-Alert--large">

    <div class="coral-Alert-message">
        Tag Maker has been moved to ACS AEM Commons as <strong><a href="https://adobe-consulting-services.github.io/acs-aem-commons/features/mcp-tools/tag-creator/index.html" target="_blank">Tag Creator</a></strong>.
        <br/>
        <br/>
        Please prefer ACS AEM Commons' <strong><a href="https://adobe-consulting-services.github.io/acs-aem-commons/features/mcp-tools/tag-creator/index.html" target="_blank">Tag Creator</a></strong> over ACS AEM Tools Tag Maker. Do note that Tag Creator accepts Excel files rather than CSV files.
    </div>
</div>


<form ng-submit="makeTags()">

    <div class="form-row">
        <h4 acs-coral-heading>Primary Converter</h4>

        <span>
            <select
                    name="converter"
                    ng-model="form.converter"
                    ng-required="true"
                    required>
                    <option ng-repeat="converter in converters"
                            ng-selected="converter.value === form.converter"
                            value="{{ converter.value }}">{{ converter.label }}</option>
            </select>
        </span>
    </div>

    <div class="form-row">
        <h4 acs-coral-heading>Fallback Converter</h4>

        <span>
            <select
                    name="fallbackConverter"
                    ng-model="form.fallbackConverter">
                <option ng-repeat="fallbackConverter in fallbackConverters"
                        ng-selected="fallbackConverter.value === form.fallbackConverter"
                        value="{{ fallbackConverter.value }}">{{ fallbackConverter.label }}</option>
            </select>
        </span>
    </div>

    <div class="form-row">
        <h4 acs-coral-heading>CSV File</h4>

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
        <h4 acs-coral-heading>Field Separator</h4>

        <span>
            <input type="text"
                   name="separator"
                   class="coral-Textfield"
                   ng-model="form.separator"
                   placeholder="Defaults to ,"/>
        </span>
    </div>


    <div class="form-row">
        <h4 acs-coral-heading>Field Delimiter</h4>

        <span>
            <input type="text"
                   name="delimiter"
                   class="coral-Textfield"
                   ng-model="form.delimiter"
                   placeholder="Defaults to &quot;"/>
        </span>
    </div>

    <div class="form-row">
        <h4 acs-coral-heading>Charset</h4>

        <span>
            <input type="text"
                   name="charset"
                   class="coral-Textfield"
                   ng-model="form.charset"
                   placeholder="Defaults to UTF-8"/>
        </span>
    </div>

    <div class="form-row">
        <h4 acs-coral-heading>Auto-clean CSV</h4>

        <div class="coral-Selector">
            <label class="coral-Selector-option"><input
                    ng-model="form.clean"
                    type="radio"
                    class="coral-Selector-input"
                    name="clean"
                    value="true"><span class="coral-Selector-description">Yes</span></label>
            <label class="coral-Selector-option"><input
                    ng-model="form.clean"
                    type="radio"
                    class="coral-Selector-input"
                    name="clean"
                    value="false"><span class="coral-Selector-description">No</span></label>
        </div>
    </div>

    <div class="form-row">
        <div class="form-left-cell">&nbsp;</div>
        <button class="coral-Button coral-Button--primary">Make Tags</button>
    </div>
</form>

