<%@ page import="com.nerderg.goodForm.FormInstance" contentType="text/html;charset=UTF-8" %>
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
  <meta name="layout" content="main"/>
</head>

<body>
<h1>My Forms</h1>
<g:if test="${flash.message}">
  <div class="message">${flash.message}</div>
</g:if>

<ul>
  <g:each in="${FormInstance.list()}" var="formInstance">
    <li>${formInstance.givenNames} ${formInstance.lastName}</li>
  </g:each>
</ul>
</body>
</html>