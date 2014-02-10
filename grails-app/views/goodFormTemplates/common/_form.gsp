<div class="goodFormContainer">

  <g:render template="/goodFormTemplates/common/displayMessages"/>

  <div class="formVersion">
    <g:message code="goodform.form.version" args="${[form.version.formVersionNumber]}"/>
  </div>

  <div class="panel panel-default">
    <div class="panel-body">
      <div class="formContainer">
        <g:form action="next" enctype="multipart/form-data" role="form">
          <input type="hidden" name="instanceId" value="${formInstance.id}"/>
          <g:each in="${questions}" var="question">
            <gf:tidy text="${gf.element(element: question.formElement, store: formData)}"/>
          </g:each>
          <div class="menuButton formSubmit">
            <g:submitButton name="next" class="btn btn-default" value="${message(code: "goodform.button.submit")}"/>
          </div>
        </g:form>
      </div>
    </div>
  </div>
  <gf:answered formInstance="${formInstance}" store="${formData}"/>

</div>