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

<%-- Application Messaging --%>
<div id="messages">

    <%-- Message to display if the HTTP POST of the code to CQ fails --%>
    <div id="code-persist-failed">
        <h2>An error occurred :(</h2>

        <p>This error occurred while trying to save your code to <%= fiddleScript %> and NOT trying to execute your
            code.</p>

        <h3>Please verify the following:</h3>

        <ul>
            <li>You are logged in as <strong>admin</strong>
                ( currently logged in as: <%= resourceResolver.getUserID()%> )
            </li>
            <li>You are accessing AEM directly (not through dispatcher)</li>
            <li>You are running this thing on a local dev!</li>
        </ul>

        <p>When all else fails, check your AEM logs!</p>
    </div>


    <div id="code-save-failed">
        <h2>An error occurred :(</h2>
        <p>For some reason your Fiddle could not be saved!</p>
    </div>

    <%-- Message to display is the user tries to navigate away from this page --%>
    <div id="navigate-away">Leaving this page will cause you to lose all the code you've entered.</div>

    <div id="remove-success">Delete successful</div>
    <div id="save-success">Creation successful</div>
    <div id="update-success">Update successful</div>
    <div id="update-failure">Nothing to update</div>
    <div id="clean-success">Clear successful</div>

    <div id="run-code">Run Code</div>
    <div id="running-code">Running Code</div>

    <div id="no-saved-fiddles">
        <section>
            <h4>You have not saved any fiddles!</h4>

            <div class="smallText greyText lightText">Click the + above to save the current code</div>
        </section>
    </div>

    <div id="slow-down"></div>


    <div id="instructions">
<div class="instructions">
    <h1>Welcome to AEM Fiddle!</h1>

    <p>
        AEM Fiddle lets you write code, execute it, and immediately see the results!
    </p>

    <p>
        Say goodbye to long Maven builds, creating throw-away components in CRXDE Lite, and random &quot;test
        pages&quot;.
    </p>

    <p>
        <ol>
            <li>Simply write some JSP code in the editor screen
                <br/>(<-- over there)</li>
            <li>click Run Code (up above)</li>
            <li>and see the result of the execution right here!</li>
        </ol>
    </p>

    <p>
        You can also Save, Load, Update and Delete code snippets using the controls at the top-right!
    </p>
</div>
    </div>

</div>

