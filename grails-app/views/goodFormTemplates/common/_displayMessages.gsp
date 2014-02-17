<%@ page import="com.nerderg.goodForm.GoodFormService" %>
<g:if test="${formData?.fieldErrors}">
  <div class="alert alert-danger alert-dismissable errorsAlert">
    <button type="button" class="close" data-dismiss="alert" aria-hidden="true">&times;</button>
    <span class="glyphicon glyphicon-warning-sign"></span>&nbsp;<g:message code="goodform.field.errors"
                                                                           args="[formData.fieldErrors.size().toString()]"/>
  </div>
</g:if>
<g:if test="${flash.message}">
  <div class="alert alert-info">
    <g:if test="${GoodFormService.isCollectionOrArray(flash.message)}">
      <ul>
        <g:each in="${flash.message}" var="message">
          <li>${message.encodeAsHTML()}</li>
        </g:each>
      </ul>
    </g:if>
    <g:else>
      <div class="message">${flash.message.encodeAsHTML()}</div>
    </g:else>
  </div>
</g:if>