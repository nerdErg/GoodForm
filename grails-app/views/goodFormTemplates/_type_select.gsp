<select name="${name}" id="${name}"
<g:each in="${fieldAttributes}" var="attribute">
  ${attribute.key}="${attribute.value}"
</g:each>
>
<g:each in="${fieldAttributes.remove('options')}" var="opt">
  <g:if test="${fieldAttributes.value == opt}">
    <option selected="selected">${opt.encodeAsHTML()}</option>
  </g:if>
  <g:else>
    <option>${opt.encodeAsHTML()}</option>
  </g:else>
</g:each>
</select>
