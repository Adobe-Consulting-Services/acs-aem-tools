<%--
* Copyright 2013 david gonzalez.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
--%>
<div id="save-modal" class="popover arrow-pos-right alignleft arrow-top">
    <h3>
        Save code as new Fiddle?
    </h3>

    <div>
        <label>Name:</label>
        <input type="text" name="jcr:title">
    </div>
    <div class="user_dialog_footer">
        <a class="button" role="button" href="#cancel-save">Cancel</a>
        <a class="button primary" role="button" href="#confirm-save" data-url="<%= saveToPath %>/*">Save</a>
    </div>
</div>