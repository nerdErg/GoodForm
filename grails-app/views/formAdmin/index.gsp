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
      <g:if test="${formVersion}">
        <span class="title">View ${formVersion.formDefinition.name}</span>
        <span class="versionNav">

          <g:if test="${formVersion.formDefinition.formVersions.size() > 0}">

            <g:if test="${formVersion.formDefinition.formVersions.size() > formVersion.formVersionNumber}">
              <a href="${g.createLink(action: 'index', id: formVersion.formDefinition.id, params: [version: (formVersion.formVersionNumber + 1)])}">&lt;-</a>
            </g:if>
            <g:else>&lt;-</g:else>

            <span class="title">version ${formVersion.formVersionNumber}</span>

            <g:if test="${formVersion.formVersionNumber > 1}">
              <a href="${g.createLink(action: 'index', id: formVersion.formDefinition.id, params: [version: (formVersion.formVersionNumber - 1)])}">-&gt;</a>
            </g:if>
            <g:else>-&gt;</g:else>

          </g:if>

        </span>
        <a href="${g.createLink(action: 'edit', id: formVersion.formDefinition.id, params: [version: formVersion.formVersionNumber])}">New&nbsp;version</a>
      </g:if>
      <g:else>
        <span class="title">View (version not found)</span>
      </g:else>
    </div>

    <div class="formCodeDisplay">
      <g:if test="${formVersion}">
${formVersion.formDefinitionDSL.encodeAsHTML()}
      </g:if>
    </div>
  </div>
</div>
</body>
</html>