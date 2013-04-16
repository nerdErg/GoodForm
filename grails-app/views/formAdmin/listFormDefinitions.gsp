<%@ page import="com.nerderg.goodForm.FormDefinition" contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <meta name="layout" content="main"/>
</head>

<body>
<g:message code="${flash.message}"/>

<div class="goodFormContainer">
    <table>
        <tr>
            <th>Form Name</th>
            <th colspan="2">Actions</th>
        </tr>
        <g:each in="${formDefinitions}" var="formDefinition">
            <tr>
                <td>${formDefinition.name}</td>
                <td><a href="${g.createLink(action: 'showFormDefinition', id: formDefinition.id)}">
                    Show Form Definition
                </a></td>
                <td><a href="${g.createLink(action: 'listSubmittedForms', id: formDefinition.id)}">
                    Submitted Forms
                </a></td>
            </tr>
        </g:each>
    </table>
</div>
</body>
</html>