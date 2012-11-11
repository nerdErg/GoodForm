<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <meta name="layout" content="main"/>
    <title>Form</title>
</head>

<body>
Form version ${form.version}

<div class="roundbox">
%{--<g:form action="next" enctype="multipart/form-data">--}%
    <g:formRemote id="form" name="goodform" update="form" method="POST"
                  action="${createLink(controller: 'form', action: 'next')}"
                  url="[controller: 'form', action: 'next']">
        <input type="hidden" name="instanceId" value="${instance.id}"/>
        <g:each in="${questions}" var="question" status="order">
            <input type="hidden" name="${question.ref}.order" value="${order}"/>
            <form:element element="${question.formElement}" store="${formData}"/>
        </g:each>
        <div class="menuButton" style="text-align: right">
            <g:submitButton name="next" value="Submit"/>
            %{--<g:submitToRemote name="applyNext" value="Submit" update="form"/>--}%
        </div>
    </g:formRemote>
%{--</g:form>--}%
</div>

<form:answered application="${app}" store="${formData}"/>

</body>
</html>