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
<form ng-submit="importAssets()">

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
        <h4 acs-coral-heading>Import Strategy</h4>

        <div class="coral-Selector">
            <label class="coral-Selector-option"><input
                    ng-model="form.importStrategy"
                    type="radio"
                    class="coral-Selector-input"
                    name="importStrategy"
                    value="FULL"><span class="coral-Selector-description">Full</span></label>
            <label class="coral-Selector-option"><input
                    ng-model="form.importStrategy"
                    type="radio"
                    class="coral-Selector-input"
                    name="importStrategy"
                    value="DELTA"><span class="coral-Selector-description">Delta</span></label>
        </div>
    </div>

    <div class="form-row"
         ng-hide="form.fileLocation">
        <h4 acs-coral-heading>&nbsp;</h4>
        <span>
            <div class="coral-Alert coral-Alert--info">
                <i class="coral-Alert-typeIcon coral-Icon coral-Icon--sizeS coral-Icon--infoCircle"></i>
                <div class="coral-Alert-message">If no source files are available, the importer will <strong>only update properties</strong> on existing assets.</div>
            </div>
        </span>
    </div>

    <div class="form-row">
        <h4 acs-coral-heading>Absolute File Dump Location</h4>

        <span>
            <input type="text"
                   name="fileLocation"
                   class="coral-Textfield"
                   ng-model="form.fileLocation"
                   placeholder="[ Optional ] Absolute path on AEM file system where files can be located"/>
        </span>
    </div>

    <div class="form-row">
        <h4 acs-coral-heading>Relative FS Path Column Name</h4>

        <span>
            <input type="text"
                   name="relSrcPathProperty"
                   class="coral-Textfield"
                   ng-model="form.relSrcPathProperty"
                   placeholder="[ Optional ] Defaults to relSrcPath"/>
        </span>
    </div>

    <div class="form-row"
            ng-show="form.importStrategy === 'DELTA'">
        <h4 acs-coral-heading>Update Binary</h4>

        <div class="coral-Selector">
            <label class="coral-Selector-option"><input
                    ng-model="form.updateBinary"
                    type="radio"
                    class="coral-Selector-input"
                    name="updateBinary"
                    value="true"><span class="coral-Selector-description">Yes</span></label>
            <label class="coral-Selector-option"><input
                    ng-model="form.updateBinary"
                    type="radio"
                    class="coral-Selector-input"
                    name="updateBinary"
                    value="false"><span class="coral-Selector-description">No</span></label>
        </div>
    </div>

    <div class="form-row"
         ng-show="form.uniqueProperty">
        <h4 acs-coral-heading>&nbsp;</h4>
        <span>
            <div class="coral-Alert coral-Alert--notice">
                <i class="coral-Alert-typeIcon coral-Icon coral-Icon--sizeS coral-Icon--alert"></i>
                <strong class="coral-Alert-title">Attention</strong>
                <div class="coral-Alert-message">An Oak index <strong>must</strong> exist for this property, otherwise <strong>very long</strong> query times, per row, will execute.</div>
            </div>
        </span>
    </div>

    <div class="form-row">
        <h4 acs-coral-heading>Asset Uniqueness Column Name</h4>
        
        <span>
            <input type="text"
                   name="uniqueProperty"
                   class="coral-Textfield"
                   ng-model="form.uniqueProperty"
                   placeholder="CSV Column name that uniquely identifies an Asset"/>
        </span>
    </div>

    <div class="form-row">
        <h4  acs-coral-heading>Mime-Type Column Name</h4>

        <span>
            <input type="text"
                   name="mimeTypeProperty"
                   class="coral-Textfield"
                   ng-model="form.mimeTypeProperty"
                   placeholder="Defaults to mimeType"/>
        </span>
    </div>

    <div class="form-row">
        <h4  acs-coral-heading>Skip Column Name</h4>

        <span>
            <input type="text"
                   name="skipProperty"
                   class="coral-Textfield"
                   ng-model="form.skipProperty"
                   placeholder="[ Optional ] Column name indicating if a row should be skipped"/>
        </span>
    </div>
    
    <div class="form-row">
        <h4  acs-coral-heading>Absolute Dest Path Column Name</h4>

        <span>
            <input type="text"
                   name="absTargetPathProperty"
                   class="coral-Textfield"
                   ng-model="form.absTargetPathProperty"
                   placeholder="Defaults to absTargetPath"/>
        </span>
    </div>

    <div class="form-row">
        <h4  acs-coral-heading>Multi-value Delimiter</h4>

        <span>
            <input type="text"
                   name="multiDelimiter"
                   class="coral-Textfield"
                   ng-model="form.multiDelimiter"
                   placeholder="Defaults to |"/>
        </span>
    </div>


    <div class="form-row">
        <h4  acs-coral-heading>Ignore Columns</h4>

        <span>
            <input type="text"
                   name="ignoreProperties"
                   class="coral-Textfield"
                   ng-model="form.ignoreProperties"
                   placeholder="[ Optional ] Comma separated; Usually set to absTargetPath,relSrcPath,mimeType"/>
        </span>
    </div>

    <div class="form-row">
        <h4  acs-coral-heading>Batch Size</h4>

        <span>
            <input type="text"
                   name="batchSize"
                   class="coral-Textfield"
                   ng-model="form.batchSize"
                   placeholder="Defaults to 1000"/>
        </span>
    </div>

    <div class="form-row">
        <h4  acs-coral-heading>Throttle in MS</h4>

        <span>
            <input type="text"
                   name="throttle"
                   class="coral-Textfield"
                   ng-model="form.throttle"
                   placeholder="Milliseconds to wait after saving a Batch. Defaults to 0"/>
        </span>
    </div>

    <div class="form-row">
        <h4  acs-coral-heading>Charset</h4>

        <span>
            <input type="text"
                   name="charset"
                   class="coral-Textfield"
                   ng-model="form.charset"
                   placeholder="Defaults to UTF-8"/>
        </span>
    </div>

    <div class="form-row">
        <h4  acs-coral-heading>Field Separator</h4>

        <span>
            <input type="text"
                   name="separator"
                   class="coral-Textfield"
                   ng-model="form.separator"
                   placeholder="Defaults to ,"/>
        </span>
    </div>


    <div class="form-row">
        <h4  acs-coral-heading>Field Delimiter</h4>

        <span>
            <input type="text"
                   name="delimiter"
                   class="coral-Textfield"
                   ng-model="form.delimiter"
                   placeholder="Defaults to &quot;"/>
        </span>
    </div>


    <div class="form-row">
        <div class="form-left-cell">&nbsp;</div>
        <button class="coral-Button coral-Button--primary">Import Assets</button>
    </div>
</form>

