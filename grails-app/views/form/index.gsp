<%@ page import="com.nerderg.goodForm.FormInstance" contentType="text/html;charset=UTF-8" %>
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
  <meta name="layout" content="main"/>
</head>

<body>
<div class="goodFormContainer">

  <h1>My Forms</h1>
  <g:if test="${flash.message}">
    <div class="message">${flash.message}</div>
  </g:if>

  <a href="createForm">New form</a>

  <ul>
    <g:each in="${FormInstance.list()}" var="formInstance">
      <li><a href="${g.createLink(action: 'continueForm', id: formInstance.id)}">
        (${formInstance.id}) ${formInstance.instanceDescription}
      </a>
      </li>
    </g:each>
  </ul>
</div>
</body>
</html>