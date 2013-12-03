<%@include file="/libs/foundation/global.jsp" %><%
%><%@page session="false"%>

<div id="notifications">
    <ul>
        <li ng-repeat="notification in data.notifications">
            <div class="alert {{notification.type}}">
                <button class="close" data-dismiss="alert">&times;</button>
                <strong>{{notification.title}}</strong>
                <div>{{notification.message}}</div>
            </div>
        </li>
    </ul>
</div>