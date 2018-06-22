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

<%@include file="/libs/foundation/global.jsp" %>
<div id="acs-tools-dumplibs">
  <div is="clientlib-table" inline-template>
      <table class="coral-Table coral-Table--bordered">
          <thead>
            <tr class="coral-Table-row">
              <th class="coral-Table-headerCell">Path</th>
              <th class="coral-Table-headerCell">Types</th>
              <th class="coral-Table-headerCell">Catigories</th>
              <th class="coral-Table-headerCell">Channels</th>
            </tr>
          </thead>
          <tbody>
            <tr class="coral-Table-row" v-for="clientlib in clientlibs">
              <td class="coral-Table-cell">{{clientlib.path}}</td>
              <td class="coral-Table-cell">{{clientlib.types.join(', ')}}</td>
              <td class="coral-Table-cell">{{clientlib.categories.join(', ')}}</td>
              <td class="coral-Table-cell">{{clientlib.channels.join(', ')}}</td>
            </tr>
          </tbody>
      </table>
  </div>
</div>
