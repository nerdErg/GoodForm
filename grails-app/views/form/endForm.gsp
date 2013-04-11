<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
  <meta name="layout" content="main"/>
  <title>Form</title>
</head>

<body>
<div class="goodFormContainer">

  <g:if test="${flash.message}">
    <div class="message">${flash.message}</div>
  </g:if>

  <h1>Form complete</h1>
  <g:render template="/form/viewCommon" model="[formInstance: formInstance, formData: formData]"/>
  <form:displayText formInstance="${formInstance}" store="${formData}" readOnly="${formInstance.readOnly}"/>
</div>
</body>
</html>