<div class="formField ${error ? 'error' : ''}" title="${error}">

  <label for="${name}"
         class="${labelClass}">
    ${label.encodeAsHTML()}
  </label>

  <g:if test="${prefix}"><span class='prefix'>${prefix}</span></g:if>
  <g:render template="/goodFormTemplates/display/type_${type.toLowerCase()}"/>
  <g:if test="${units}"><span class='units'>${units}</span></g:if>
</div>