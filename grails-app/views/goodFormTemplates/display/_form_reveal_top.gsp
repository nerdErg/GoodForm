<div class="formField ${error ? 'error' : ''}" title="${error}">

  %{--<g:if test="${preamble}"><div class='preamble'>${preamble.encodeAsHTML()}</div></g:if>--}%

  <label for="${name}"
         class="${labelClass}">
    ${label.encodeAsHTML()}
  </label>

  <g:if test="${prefix}"><span class='prefix'>${prefix}</span></g:if>

  <g:if test="${fieldAttributes.remove('pick1')}">
      <input
          type="radio"
          name="${fieldAttributes.remove('parentName')}"
          value="${label.encodeAsHTML()}"
          class="${fieldAttributes.remove('class')} hiddenFormRadio"

      <g:each in="${fieldAttributes}" var="attribute">
        ${attribute.key}="${attribute.value.encodeAsHTML()}"
      </g:each>
      />
    </g:if>
    <g:else>
      <input
          type="checkbox"
          name="${name}.yes"
          id="${name}"
          value="${label.encodeAsHTML()}"
          class="${fieldAttributes.remove('class')} hiddenFormCheckbox"

      <g:each in="${fieldAttributes}" var="attribute">
        ${attribute.key}="${attribute.value.encodeAsHTML()}"
      </g:each>
      />
    </g:else>
  <g:if test="${units}"><span class='units'>${units}</span></g:if>
</div>
  <g:if test="${fieldAttributes.checked}">
    <div>
  </g:if>
  <g:else>
    <div class='hiddenForm'>
  </g:else>