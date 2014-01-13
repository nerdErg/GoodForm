<g:if test="${fieldAttributes.remove('pick1')}">
  <input
      type="radio"
      name="${fieldAttributes.remove('parentName')}"
      value="${label.encodeAsHTML()}"
  <g:each in="${fieldAttributes}" var="attribute">
    <g:if test="${attribute.key != 'value'}">
      ${attribute.key}="${attribute.value.encodeAsHTML()}"
    </g:if>
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
    <g:if test="${attribute.key != 'value'}">
      ${attribute.key}="${attribute.value.encodeAsHTML()}"
    </g:if>
  </g:each>
  />
</g:else>