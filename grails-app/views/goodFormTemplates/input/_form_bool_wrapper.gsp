<div class="col-12-xs col-8-md col-6-lg formField form-group ${error ? 'has-error' : ''}" title="${error}">
  <g:if test="${preamble}"><p class='preamble text-info'>${preamble.encodeAsHTML()}</p></g:if>
  <g:if test="${prefix}"><span class='prefix'>${prefix}</span></g:if>

  <g:if test="${fieldAttributes.remove('pick1')}">
    <label class="${labelClass}">
      <input
          type="radio"
          name="${fieldAttributes.parentName}"
          value="${label.encodeAsHTML()}"
        ${gf.addAttributes(fieldAttr: fieldAttributes, class: "form-control", skip: ['value', 'parentName'])}/>&nbsp;${label.encodeAsHTML()}</label>
  </g:if>

  <g:else>
    <label for="${name}" class="${labelClass}">
      <input
          type="checkbox"
          name="${name}"
          id="${name}"
          value="${label.encodeAsHTML()}"
        ${gf.addAttributes(fieldAttr: fieldAttributes, class: "form-control", skip: ['value'])}/>&nbsp;${label.encodeAsHTML()}</label>
  </g:else>

  <g:if test="${units}"><span class='units'>${units}</span></g:if>
  <g:if test="${required}"><span class='required'>${required ? '*' : ''}</span></g:if>
  <g:if test="${hint}"><p class='hint help-block'>${hint}</p></g:if>
</div>

