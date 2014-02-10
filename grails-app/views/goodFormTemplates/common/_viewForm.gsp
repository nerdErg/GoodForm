<div class="goodFormContainer display">

  <gf:showMessages/>

  <h1><g:message code="goodform.view.form"/></h1>
  <g:render template="/goodFormTemplates/common/viewCommon" model="[formInstance: formInstance, formData: formData]"/>
  <gf:tidy text="${gf.displayText(formInstance: formInstance, store: formData, readOnly: formInstance.readOnly)}"/>
</div>