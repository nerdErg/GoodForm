<div class="formField ${error ? 'error' : ''}" title="${error}">

  <g:if test="${preamble}"><div class='preamble'>${preamble.encodeAsHTML()}</div></g:if>

  <label for="${name}"
         class="${labelClass}">
    ${label.encodeAsHTML()}
  </label>

  <g:render template="/templates/form_${baseType.toLowerCase()}"
            model="${[name: name, fieldAttributes: fieldAttributes]}"/>

  <g:if test="${units}"><span class='units'>${units}</span></g:if>
  <g:if test="${required}"><span class='required'>${required ? '*' : ''}</span></g:if>
  <g:if test="${hint}"><span class='hint'>${hint}</span></g:if>
</div>