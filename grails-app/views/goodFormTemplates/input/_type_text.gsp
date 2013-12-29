<g:if test="${fieldAttributes.size > 100}">
  <g:render template="/goodFormTemplates/input/form_textarea"
            model="${[name: name, fieldAttributes:[value: fieldAttributes.value, cols: 80, rows: ((int)fieldAttributes.size / 80 + 1)]]}"/>
</g:if>
<g:else>
  <g:render template="/goodFormTemplates/input/form_input"/>
</g:else>