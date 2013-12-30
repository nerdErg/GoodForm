<input
    type="${type}"
    name="${name}"
    id="${name}"
<g:each in="${fieldAttributes}" var="attribute">
  ${attribute.key}="${attribute.value.encodeAsHTML()}"
</g:each>
/>