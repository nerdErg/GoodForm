<input
    type="text"
    name="${name}"
    class="date"
<g:each in="${fieldAttributes}" var="attribute">
  ${attribute.key}="${attribute.value.encodeAsHTML()}"
</g:each>
/>
