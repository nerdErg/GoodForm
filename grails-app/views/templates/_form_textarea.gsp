<textarea
    name="${name}"
    id="${name}"
<g:each in="${fieldAttributes}" var="attribute">
  ${attribute.key}="${attribute.value}"
</g:each>
>
${fieldAttributes.value ?: ''}
</textarea>
