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

<script type="text/ng-template" id="createTaskTemplate">
    <h1>Create a new task</h1>

    <form name="myForm" ng-controller="MainCtrl">
        <table class="create-new-task">
            <tr>
                <td class="label-col">
                    <label>Task Id</label>
                </td>
                <td class="field-col">
                    <input type="text"
                           ng-model="task_id"
                           name="id"/>
                </td>
            </tr>
            <tr>
                <td class="label-col">
                    <label>Source</label>
                </td>
                <td class="field-col">
                    <input type="text"
                           ng-model="task_src"
                           name="src"
                           placeholder="http://admin:admin@localhost:4502/crx/server/-/jcr:root/content/dam/my-site"/>
                </td>
            </tr>
            <tr>
                <td class="label-col">
                    <label>Destination</label>
                </td>
                <td class="field-col">
                    <input type="text"
                           ng-model="task_dst"
                           name="dst"
                           placeholder="/content/dam/my-site"/></td>
            </tr>
            <tr>
                <td class="label-col">
                    <label>Recursive</label>
                </td>
                <td class="field-col">
                    <label class="switch">
                        <input type="checkbox"
                               name="recursive"
                               ng-model="checkboxModel.recursive"><span>No</span><span>Yes</span>
                    </label>
                </td>
            </tr>
            <tr>
                <td class="label-col">
                    <label>Update</label>
                </td>
                <td class="field-col">
                    <label class="switch">
                        <input type="checkbox"
                               name="update"
                               ng-model="checkboxModel.update"><span>No</span><span>Yes</span>
                    </label>
                </td>
            </tr>
            <tr>
                <td class="label-col">
                    <label>Only newer</label>
                </td>
                <td class="field-col">
                    <label class="switch">
                        <input type="checkbox"
                               name="onlyNewer"
                               ng-model="checkboxModel.onlyNewer"><span>No</span><span>Yes</span>
                    </label>
                </td>
            </tr>
            <tr>
                <td class="label-col">
                    <label>No ordering</label>
                </td>
                <td class="field-col">
                    <label class="switch">
                        <input type="checkbox"
                               name="noOrdering"
                               ng-model="checkboxModel.noOrdering"><span>No</span><span>Yes</span>
                    </label>
                </td>
            </tr>
            <tr>
                <td class="label-col">
                    <label>Resume from</label>
                </td>
                <td class="field-col">
                    <input type="text"
                           ng-model="task_resumeFrom"
                           name="resumeFrom"
                           placeholder="/content/dam/my-site"/>
                </td>
            </tr>
            <tr>
                <td class="label-col">
                    <label>Batch size</label>
                </td>
                <td class="field-col">
                    <input type="text"
                           ng-model="task_batchSize"
                           name="resumeFrom"
                           placeholder="1024"/>
                </td>
            </tr>
            <tr>
                <td class="label-col">
                    <label>Throttle</label>
                </td>
                <td class="field-col">
                    <input type="text"
                           ng-model="task_throttle"
                           name="throttle"
                           placeholder="in seconds"/>
                </td>
            </tr>

            <tr>
                <td class="label-col" valign="top">
                    <label>Excludes</label>
                </td>

                <td class="field-col" style="padding-top: .75em">

                    <div>
                        <a class="add" ng-click="addExclude()">
                            <i class="icon-add-circle withLabel">Add exclude rule</i>
                        </a>
                    </div>

                    <div class="add-remove-list" ng-show="excludes.length > 0">
                        <ul ng-repeat="exclude in excludes track by $index">
                            <li>
                                <input type="text"
                                        ng-model="exclude.value"
                                        placeholder="/content/dam/my-site/(en|fr)/documents(/.*)?"/>

                                <a ng-click="removeExclude($index)" class="remove icon-minus-circle"></a>
                            </li>
                        </ul>
                    </div>
                </td>
            </tr>
            <tr>
                <td></td>
                <td>

                    <hr/>

                    <button class="primary"
                            ng-click="confirm()">Create New Task</button>

                    <a class="button"
                       role="button"
                       href="#"
                       ng-click="closeThisDialog()">Cancel</a>
                </td>
            </tr>
        </table>
    </form>
</script>