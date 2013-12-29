<select name="${name}" id="${name}"
<g:each in="${fieldAttributes}" var="attribute">
  <g:if test="${attribute.key != 'options'}">
    ${attribute.key}="${attribute.value}"
  </g:if>
</g:each>
>
<g:each in="${fieldAttributes.options}" var="opt">
  <g:if test="${fieldAttributes.value == opt}">
    <option selected="selected">${opt.encodeAsHTML()}</option>
  </g:if>
  <g:else>
    <option>${opt.encodeAsHTML()}</option>
  </g:else>
</g:each>
</select>
