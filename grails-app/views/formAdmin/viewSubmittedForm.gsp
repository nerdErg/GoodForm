<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <meta name="layout" content="main"/>
</head>

<body>
<g:if test="${flash.message}">
    <div class="message">${flash.message}</div>
</g:if>
<div class="goodFormContainer">

    <g:textArea name="formContents" value="${form.formData}" rows="40" cols="200"
                style="height: 350px; width: 850px"/>
    <a href="${g.createLink(action: 'listFormDefinitions')}">Back</a>
</div>
</body>
</html>