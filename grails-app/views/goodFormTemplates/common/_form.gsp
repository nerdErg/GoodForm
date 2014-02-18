<div class="goodFormContainer">

  <g:render template="/goodFormTemplates/common/displayMessages"/>

  <div class="formVersion">
    <g:message code="goodform.form.version" args="${[form.version.formVersionNumber]}"/>
  </div>

  <gf:answered formInstance="${formInstance}" store="${formData}"/>
  <a id="form"><hr></a>

  <div class="panel panel-info">
    <div class="panel-heading">
      <span><i class='fa fa-edit'></i> Please answer these questions.</span>
      <span class="text text-muted">Questions marked with a <span
          class='required'>*</span> are required before continuing.</span>
    </div>

    <div class="panel-body">
      <div class="formContainer">
        <g:form action="next" enctype="multipart/form-data" role="form">
          <input type="hidden" name="instanceId" value="${formInstance.id}"/>
          <g:each in="${questions}" var="question" status="i">
            <gf:element element="${question.formElement}" store="${formData}"/>
          </g:each>
          <div class="menuButton formSubmit">
            <button type="submit" class="btn btn-primary">
              <i class='fa fa-refresh'></i> <span>${message(code: "goodform.button.submit")}</span>
            </button>
          </div>
        </g:form>
      </div>
    </div>
  </div>

</div>