<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
  <meta name="layout" content="main"/>
  <r:script>
  goodform.baseContextPath = "${request.getContextPath()}";
  </r:script>
</head>

<body>
<div class="goodFormContainer">

  <form:showMessages/>

  <g:message code="goodform.form.version" args="${[form.version]}"/>

  <div class="roundbox">
    <g:form action="next" enctype="multipart/form-data">
      <input type="hidden" name="instanceId" value="${formInstance.id}"/>
      <g:each in="${questions}" var="question" status="order">
        <input type="hidden" name="${question.ref}.order" value="${order}"/>
        <form:element element="${question.formElement}" store="${formData}"/>
      </g:each>
      <div class="menuButton" style="text-align: right">
        <g:submitButton name="next" value="${message(code: "goodform.button.submit")}"/>
      </div>
    </g:form>
  </div>

  <form:answered formInstance="${formInstance}" store="${formData}"/>
</div>
</body>
</html>