<!-- <div>%{--this is a bug fix for compressing textarea--}%</div> -->
<textarea
    name="${name}"
    id="${name}"
<gf:addAttributes fieldAttr="${fieldAttributes}" class="form-control" skip="['value', 'cols', 'rows']"/>
>${(fieldAttributes.value?.encodeAsHTML() ?: '')}</textarea>
%{--Do not put a new line before or after the value
    or it will be included in the value when submitted--}%
