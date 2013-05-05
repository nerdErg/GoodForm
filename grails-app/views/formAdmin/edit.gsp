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
        <span class="title">Create new version of ${formVersion.formDefinition.name} from version ${formVersion?.formVersionNumber}</span>
        <a href="${g.createLink(action: 'index', id: formVersion.formDefinition.id)}">Cancel</a>
    </div>

    <div class="formCodeEdit">
      <g:form action="updateFormDefinition" >
        <input type="hidden" name="id" value="${formVersion.id}"/>
        <g:textArea name="formDefinition" value="${formVersion.formDefinitionDSL}" rows="25" cols="100" wrap="off"/>

        <span class="menuButton" style="text-align: right">
          <g:submitButton name="updateFormDefinition" value="${message(code: "goodform.button.save", default: 'save')}"/>
        </span>

      </g:form>

    </div>
  </div>
</div>
</body>
</html>