<%--
  User: pmcneil
  Date: 5/05/13
--%>

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
  <g:render template="listOfForms" model="[forms: forms]"/>

  <div class="formDefinitionContainer">
    <div class="buttonBar">
        <span class="title">Create new form named ${formName}</span>
        <a href="${g.createLink(action: 'index')}">Cancel</a>
    </div>

    <div class="formCodeEdit">
      <g:form action="saveNewFormDefinition" >
        <input type="hidden" name="formName" value="${formName}"/>
        <g:textArea name="formDefinition" value="form {}" rows="25" cols="100" wrap="off"/>

        <span class="menuButton" style="text-align: right">
          <g:submitButton name="saveFormDefinition" value="${message(code: "goodform.button.save", default: 'save')}"/>
        </span>

      </g:form>

    </div>
  </div>
</div>
</body>
</html>