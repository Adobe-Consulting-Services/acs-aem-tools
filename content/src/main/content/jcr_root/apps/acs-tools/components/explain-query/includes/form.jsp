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

<%@include file="/libs/foundation/global.jsp" %><%

%><form class="no-separator"
        ng-submit="explain()">

    <div class="form-row">
        <h4>Language</h4>

        <div class="selector">
            <select ng-model="form.language"
                    ng-required="true">
                <option value="xpath">xpath</option>
                <option value="sql">sql</option>
                <option value="JCR-SQL2">JCR-SQL2</option>
                <option value="queryBuilder">QueryBuilder</option>
            </select>
        </div>
    </div>

    <div class="form-row">
        <h4>Query</h4>

        <span>
            <textarea
                    ng-model="form.statement"
                    rows="4"
                    cols="20"
                    ng-required="true"
                    placeholder="Query statement; must match the selected Language above"></textarea>
        </span>
    </div>

    <div class="form-row">
        <h4 class="no-height">Include execution time</h4>

        <span>
            <label><input
                    ng-model="form.executionTime"
                    type="checkbox"><span>
                Run query and report execution time.
                Long running queries will delay reporting the explanation.</span></label>
        </span>
    </div>

    <div class="form-row"
         ng-show="form.executionTime">
        <h4>Include result count</h4>

        <span>
            <label><input
                ng-model="form.resultCount"
                type="checkbox"><span>
            Count the total number of results.
            Large result sets will delay reporting the explanation.</span></label>
        </span>
    </div>

    <div class="form-row">
        <div class="form-left-cell">&nbsp;</div>
        <button class="primary">Explain</button>
    </div>
</form>
