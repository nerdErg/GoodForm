<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <meta name="layout" content="main"/>
</head>

<body>
<g:message code="${flash.message}"/>
<div class="goodFormContainer">
    <g:form action="updateFormDefinition" enctype="multipart/form-data">
        <input type="hidden" name="id" value="${formDefinition.id}"/>
        <g:textArea name="formDefinition" value="${formDefinition.formDefinition}" rows="40" cols="200"
                    style="height: 350px; width: 850px"/>
        <a href="${g.createLink(action: 'listFormDefinitions')}">Back</a>
        <span class="menuButton" style="text-align: right">
            <g:submitButton name="updateFormDefinition" value="${message(code: "goodform.button.submit")}"/>
        </span>
    </g:form>
</div>
</body>
</html>