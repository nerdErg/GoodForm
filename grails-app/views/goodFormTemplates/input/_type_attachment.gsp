<input
    type="file"
    name="${name}"
    id="${name}"
<g:each in="${fieldAttributes}" var="attribute">
  <g:if test="${attribute.key != 'value'}">
    ${attribute.key}="${attribute.value.encodeAsHTML()}"
  </g:if>
</g:each>
/>
&nbsp;${fieldAttributes.fileName.encodeAsHTML()}

