<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
  <meta name="layout" content="main"/>
  <title>Form - ${formInstance.formVersion.formDefinition.name}</title>
</head>

<body>
<div class="goodFormContainer display">

  <form:showMessages/>

  <h1><g:message code="goodform.view.form"/></h1>
  <g:render template="/form/viewCommon" model="[formInstance: formInstance, formData: formData]"/>
  <form:displayText formInstance="${formInstance}" store="${formData}" readOnly="${formInstance.readOnly}"/>
</div>
</body>
</html>