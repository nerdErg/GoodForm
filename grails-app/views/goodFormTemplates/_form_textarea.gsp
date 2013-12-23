<textarea
    name="${name}"
    id="${name}"
<g:each in="${fieldAttributes}" var="attribute">
  ${attribute.key}="${attribute.value}"
</g:each>
>${fieldAttributes.value ?: ''}</textarea>
%{--Do not put a new line before or after the value
    or it will be included in the value when submitted--}%
