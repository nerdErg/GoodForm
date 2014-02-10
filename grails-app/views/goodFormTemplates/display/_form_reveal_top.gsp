<div class="formField ${error ? 'error' : ''}" title="${error}">

  <g:if test="${prefix}"><span class='prefix'>${prefix}</span></g:if>
  <g:if test="${fieldAttributes.checked}">
    <fieldset class="reset reveal">
        <legend>${label.encodeAsHTML()} <span class="text-success"><span class="glyphicon glyphicon-ok"></span></span></legend>
    <div>
  </g:if>
  <g:else>
    <dt>${label.encodeAsHTML()}</dt>
    <dd class="text-muted"><span class="glyphicon glyphicon-remove"></span></dd>
    <div class='hiddenForm'>
  </g:else>