<g:if test="${fieldAttributes.remove('pick1')}">
  <input
      type="radio"
      name="${fieldAttributes.remove('parentName')}"
      value="${label.encodeAsHTML()}"
  <g:each in="${fieldAttributes}" var="attribute">
    ${attribute.key}="${attribute.value}"
  </g:each>
  />
</g:if>
<g:else>
  <input
      type="checkbox"
      name="${name}"
      id="${name}"
      value="${label.encodeAsHTML()}"
  <g:each in="${fieldAttributes}" var="attribute">
    ${attribute.key}="${attribute.value}"
  </g:each>
  />
</g:else>