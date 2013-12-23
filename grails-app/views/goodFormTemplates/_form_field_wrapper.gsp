<div class="formField ${error ? 'error' : ''}" title="${error}">

  <g:if test="${preamble}"><div class='preamble'>${preamble.encodeAsHTML()}</div></g:if>

  <label for="${name}"
         class="${labelClass}">
    ${label.encodeAsHTML()}
  </label>

  <g:if test="${prefix}"><span class='prefix'>${prefix}</span></g:if>
  <g:render template="/goodFormTemplates/type_${type.toLowerCase()}"/>
  <g:if test="${units}"><span class='units'>${units}</span></g:if>
  <g:if test="${required}"><span class='required'>${required ? '*' : ''}</span></g:if>
  <g:if test="${hint}"><span class='hint'>${hint}</span></g:if>
</div>