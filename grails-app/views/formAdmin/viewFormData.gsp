<%@ page import="grails.converters.JSON" contentType="text/html;charset=UTF-8" %>
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

    <pre><code>"${(form.storedFormData() as JSON).toString(true)}"</code></pre>

    <a href="${g.createLink(action: 'listFormDefinitions')}">Back</a>
</div>
</body>
</html>