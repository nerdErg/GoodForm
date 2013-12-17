<input type="text" name="${name}.date" id="${name}.date" value="${fieldAttributes.value?.date}" class="date"
<g:each in="${fieldAttributes}" var="attribute">
  <g:if test="${attribute.key != 'value'}">
    ${attribute.key}="${attribute.value}"
  </g:if>
</g:each>
/>

<input type="text" name="${name}.time" id="${name}.time" value="${fieldAttributes.value?.time}" class="time"
<g:each in="${fieldAttributes}" var="attribute">
  <g:if test="${attribute.key != 'value'}">
    ${attribute.key}="${attribute.value}"
  </g:if>
</g:each>
/>
