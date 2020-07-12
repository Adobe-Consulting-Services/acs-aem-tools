<%--
  ~ #%L
  ~ ACS AEM Tools Bundle
  ~ %%
  ~ Copyright (C) 2020 Konrad Windszus, Netcentric
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
<div id="set-credentials-modal" class="coral-Modal">

    <div class="coral-Modal-header">
        <i class="coral-Modal-typeIcon coral-Icon coral-Icon--sizeS"></i>
        <h2 class="coral-Modal-title coral-Heading coral-Heading--2">Set credentials</h2>
        <button type="button" class="coral-MinimalButton coral-Modal-closeButton" title="Close" data-dismiss="modal">
            <i class="coral-Icon coral-Icon--sizeXS coral-Icon--close coral-MinimalButton-icon "></i>
        </button>
    </div>

    <div class="coral-Modal-body">

        <form name="myForm" class="coral-Form coral-Form--aligned" autocomplete="off" novalidate>
            <section class="create-new-task">
                
                <!-- Credential Fields -->
                <div class="coral-Form-fieldwrapper">
                    <label class="coral-Form-fieldlabel">Source User Name</label>
                    <input class="coral-Form-field coral-Textfield" type="text"
                            autocomplete="username"
                            ng-model="task_src_username"
                            name="username"/>
                </div>

                <div class="coral-Form-fieldwrapper">
                    <label class="coral-Form-fieldlabel">Source Password</label>
                    <input class="coral-Form-field coral-Textfield" ng-attr-type="{{showpassword ? 'text' : 'password'}}"
                            ng-model="task_src_password"
                            name="passsword"/>
                </div>
                <label class="coral-Form-fieldlabel">Show Password</label>
                <span class="coral-Form-field coral-Switch">
                    <input class="coral-Switch-input" type="checkbox"
                            name="showpassword"
                            ng-model="showpassword"><span class="coral-Switch-offLabel">No</span><span class="coral-Switch-onLabel">Yes</span>
                </span>
                <!-- // Credential Fields -->

            </section>
        </form>
    </div>

    <div class="coral-Modal-footer">
        <button class="coral-Button coral-Button--primary" data-dismiss="modal" ng-click="set_credentials()">Set credentials</button>
        <button class="coral-Button" data-dismiss="modal" ng-click="reset();">Cancel</button>
    </div>

</div>