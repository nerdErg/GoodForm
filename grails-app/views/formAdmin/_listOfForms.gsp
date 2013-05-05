<div class="listOfForms">
  <div class="title">Forms</div>
  <br/>
  <ul>
    <g:each in="${forms}" var="form">
      <li><a href="${g.createLink(action: 'index', id: form.id)}">${form.name}</a></li>
    </g:each>
  </ul>

  <div class="buttonBar">
    <g:form action="create">
      <input type="text" name="formName" placeholder="Form name" size="12">
      <span class="menuButton" style="text-align: right">
        <g:submitButton name="create" value="${message(code: "goodform.button.newform", default: 'New form')}"/>
      </span>
    </g:form>
  </div>
</div>
