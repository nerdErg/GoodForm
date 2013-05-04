<%@ page import="com.nerderg.goodForm.FormVersion; com.nerderg.goodForm.FormDefinition" contentType="text/html;charset=UTF-8" %>
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
  <table>
    <tr>
      <th>Form Name</th>
      <th>Version</th>
      <th colspan="2">Actions</th>
    </tr>
    <g:each in="${formDefinitions}" var="formDefinition">
      <tr>
        <td>${formDefinition.name}</td>
        <td>${formDefinition.formVersions.max{ FormVersion f -> f.formVersionNumber }}</td>
        <td><a href="${g.createLink(action: 'showFormDefinition', id: formDefinition.id)}">
          Definition
        </a></td>
        <td><a href="${g.createLink(action: 'listForms', id: formDefinition.id)}">
          Forms
        </a></td>
      </tr>
    </g:each>
  </table>
</div>
</body>
</html>