<select name="${name}" id="${name}"
  <gf:addAttributes fieldAttr="${fieldAttributes}" class="form-control" skip="['value', 'options']"/> >
<g:each in="${fieldAttributes.options}" var="opt">
  <g:if test="${fieldAttributes.value == opt}">
    <option selected="selected">${opt.encodeAsHTML()}</option>
  </g:if>
  <g:else>
    <option>${opt.encodeAsHTML()}</option>
  </g:else>
</g:each>
</select>
