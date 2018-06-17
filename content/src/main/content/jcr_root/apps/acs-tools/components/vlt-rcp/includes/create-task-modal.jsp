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
<div id="create-new-task-modal" class="coral-Modal">

    <div class="coral-Modal-header">
        <i class="coral-Modal-typeIcon coral-Icon coral-Icon--sizeS"></i>
        <h2 class="coral-Modal-title coral-Heading coral-Heading--2">Create a new task</h2>
        <button type="button" class="coral-MinimalButton coral-Modal-closeButton" title="Close" data-dismiss="modal" ng-click="reset();">
            <i class="coral-Icon coral-Icon--sizeXS coral-Icon--close coral-MinimalButton-icon "></i>
        </button>
    </div>

    <div class="coral-Modal-body">

        <form name="myForm" class="coral-Form coral-Form--aligned" autocomplete="off" novalidate>
            <section class="create-new-task">

                <div class="coral-Form-fieldwrapper">
                    <label class="coral-Form-fieldlabel">Task Id</label>
                    <input class="coral-Form-field coral-Textfield" type="text"
                            ng-model="task_id"
                            name="task_id"/>
                </div>

                <div class="coral-Form-fieldwrapper">
                    <label class="coral-Form-fieldlabel">Source</label>
                    <input class="coral-Form-field coral-Textfield" type="text"
                            ng-model="task_src"
                            name="src"
                            placeholder="http://localhost:4502/crx/server/-/jcr:root/content/dam/my-site"/>
                </div>

                <!-- Credintial Fields -->
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
                <!-- // Credintial Fields -->

                <!-- Switch Options -->
                <label class="coral-Form-fieldlabel">Destination</label>
                <input class="coral-Form-field coral-Textfield" type="text"
                       ng-model="task_dst"
                       name="dst"
                       placeholder="/content/dam/my-site"/>

                <label class="coral-Form-fieldlabel">Recursive</label>
                <span class="coral-Form-field coral-Switch">
                    <input class="coral-Switch-input" type="checkbox"
                           name="recursive"
                           ng-model="checkboxModel.recursive"><span class="coral-Switch-offLabel">No</span><span class="coral-Switch-onLabel">Yes</span>
                </span>

                <label class="coral-Form-fieldlabel">Update</label>
                <span class="coral-Form-field coral-Switch">
                    <input class="coral-Switch-input" type="checkbox"
                           name="update"
                           ng-model="checkboxModel.update"><span class="coral-Switch-offLabel">No</span><span class="coral-Switch-onLabel">Yes</span>
                </span>

                <label class="coral-Form-fieldlabel">Only newer</label>
                <span class="coral-Form-field coral-Switch">
                    <input class="coral-Switch-input" type="checkbox"
                           name="onlyNewer"
                           ng-model="checkboxModel.onlyNewer"><span class="coral-Switch-offLabel">No</span><span class="coral-Switch-onLabel">Yes</span>
                </span>

                <label class="coral-Form-fieldlabel">No ordering</label>
                <span class="coral-Form-field coral-Switch">
                    <input class="coral-Switch-input" type="checkbox"
                           name="noOrdering"
                           ng-model="checkboxModel.noOrdering"><span class="coral-Switch-offLabel">No</span><span class="coral-Switch-onLabel">Yes</span>
                </span>
                <!-- // Switch Options -->

                <!-- Extra Options -->
                <label class="coral-Form-fieldlabel">Resume from</label>
                <input class="coral-Form-field coral-Textfield" type="text"
                       ng-model="task_resumeFrom"
                       name="resumeFrom"
                       placeholder="/content/dam/my-site"/>

                <label class="coral-Form-fieldlabel">Batch size</label>
                <input class="coral-Form-field coral-Textfield" type="text"
                       ng-model="task_batchSize"
                       name="batchSize"
                       placeholder="1024"/>

                <label class="coral-Form-fieldlabel">Throttle</label>
                <input class="coral-Form-field coral-Textfield" type="text"
                       ng-model="task_throttle"
                       name="throttle"
                       placeholder="in seconds"/>

                <label class="coral-Form-fieldlabel">Excludes</label>
                <div class="coral-Form-field excludes">
                    <a class="addButton" ng-click="addExclude()">
                        <i class="coral-Icon coral-Icon--addCircle"></i>
                    </a>
                    <div class="add-remove-list" ng-show="excludes.length > 0">
                        <ul ng-repeat="exclude in excludes track by $index">
                            <li>
                                <input class="coral-Form-field coral-Textfield" type="text"
                                       ng-model="exclude.value"
                                       placeholder="/content/dam/my-site/(en|fr)/documents(/.*)?"/>

                                <a class="removeButton" ng-click="removeExclude($index)"><i class="coral-Icon coral-Icon--minusCircle"></i></a>
                            </li>
                        </ul>
                    </div>
                </div>
                <!-- // Extra Options -->
            </section>
        </form>
    </div>

    <div class="coral-Modal-footer">
        <button class="coral-Button coral-Button--primary" data-dismiss="modal" ng-click="create();">Create New Task</button>
        <button class="coral-Button" data-dismiss="modal" ng-click="reset();">Cancel</button>
    </div>

</div>