<div class="goodFormContainer">

  <g:render template="/goodFormTemplates/common/displayMessages"/>

  <div class="formVersion">
    <g:message code="goodform.form.version" args="${[form.version.formVersionNumber]}"/>
  </div>

  <a id="form"><hr></a>

  <div id="formIndex">
    <div class='panel panel-info' title='${g.message(code: "goodform.scroll.top")}'>
      <div class="panel-heading">
        <a href="#form" class="animated"><i class='fa fa-arrow-up'></i>&nbsp;<i class='fa fa-edit'></i>&nbsp;${i}
        </a>
      </div>
    </div>
    <gf:answeredIndex formInstance="${formInstance}" store="${formData}"/>
  </div>

  <div class="formData">
    <div class="panel panel-info">
      <div class="panel-heading">
        <span><i class='fa fa-edit'></i> <g:message code="goodform.form.please.answer"/></span>
        <span class="text text-muted"><g:message code="goodform.form.required"/></span>
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

    <gf:answered formInstance="${formInstance}" store="${formData}"/>
  </div>
</div>
