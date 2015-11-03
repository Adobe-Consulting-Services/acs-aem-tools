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
        <h4>Import Strategy</h4>

        <div class="selector">
            <label><input
                    ng-model="form.importStrategy"
                    type="radio"
                    name="importStrategy"
                    value="FULL"><span>Full</span></label>
            <label><input
                    ng-model="form.importStrategy"
                    type="radio"
                    name="importStrategy"
                    value="DELTA"><span>Delta</span></label>
        </div>
    </div>

    <div class="form-row">
        <h4>Absolute File Dump Location</h4>

        <span>
            <input type="text"
                   name="fileLocation"
                   ng-model="form.fileLocation"
                   ng-required="true"
                   required
                   placeholder="[ Required ] Absolute path on AEM file system where files can be located"/>
        </span>
    </div>

    <div class="form-row">
        <h4>Relative FS Path Column Name</h4>

        <span>
            <input type="text"
                   name="relSrcPathProperty"
                   ng-model="form.relSrcPathProperty"
                   placeholder="Defaults to relSrcPath"/>
        </span>
    </div>

    <div class="form-row"
            ng-show="form.importStrategy === 'DELTA'">
        <h4>Update Binary</h4>

        <div class="selector">
            <label><input
                    ng-model="form.updateBinary"
                    type="radio"
                    name="updateBinary"
                    value="true"><span>True</span></label>
            <label><input
                    ng-model="form.updateBinary"
                    type="radio"
                    name="updateBinary"
                    value="false"><span>No</span></label>
        </div>
    </div>

    <div class="form-row">
        <h4>Asset Uniqueness Column Name</h4>
        
        <span>
            <input type="text"
                   name="uniqueProperty"
                   ng-model="form.uniqueProperty"
                   placeholder="CSV Column name that uniquely identifies an Asset"/>
        </span>
    </div>
    
    <div class="form-row">
        <h4>Mime-Type Column Name</h4>

        <span>
            <input type="text"
                   name="mimeTypeProperty"
                   ng-model="form.mimeTypeProperty"
                   placeholder="Defaults to mimeType"/>
        </span>
    </div>

    <div class="form-row">
        <h4>Skip Column Name</h4>

        <span>
            <input type="text"
                   name="skipProperty"
                   ng-model="form.skipProperty"
                   placeholder="[ Optional ] Column name indicating if a row should be skipped"/>
        </span>
    </div>
    
    <div class="form-row">
        <h4>Absolute Dest Path Column Name</h4>

        <span>
            <input type="text"
                   name="absTargetPathProperty"
                   ng-model="form.absTargetPathProperty"
                   placeholder="Defaults to absTargetPath"/>
        </span>
    </div>

    <div class="form-row">
        <h4>Multi-value Delimiter</h4>

        <span>
            <input type="text"
                   name="multiDelimiter"
                   ng-model="form.multiDelimiter"
                   placeholder="Defaults to |"/>
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
        <h4>Ignore Columns</h4>

        <span>
            <input type="text"
                   name="ignoreProperties"
                   ng-model="form.ignoreProperties"
                   placeholder="[ Optional ] Comma separated; Usually set to absTargetPath,relSrcPath,mimeType"/>
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
        <h4>Batch Size</h4>

        <span>
            <input type="text"
                   name="batchSize"
                   ng-model="form.batchSize"
                   placeholder="Defaults to 1000"/>
        </span>
    </div>

    <div class="form-row">
        <h4>Throttle in MS</h4>

        <span>
            <input type="text"
                   name="throttle"
                   ng-model="form.throttle"
                   placeholder="Milliseconds to wait after saving a Batch. Defaults to 0"/>
        </span>
    </div>

    <div class="form-row">
        <div class="form-left-cell">&nbsp;</div>
        <button class="primary">Import Assets</button>
    </div>
</form>

